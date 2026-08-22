package de.cidaas.sdk.android.service.repository.Login;

import android.content.Context;

import java.util.Map;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.service.CidaassdkService;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Completes auto-login after register:
 * {@code POST /login-srv/login/handle/afterregister/:trackId} → authorization {@code code}.
 */
public class AfterRegisterService {

    private final Context context;
    private CidaassdkService service;

    public static AfterRegisterService shared;

    public AfterRegisterService(Context contextFromCidaas) {
        context = contextFromCidaas;
        service = new CidaassdkService(contextFromCidaas);
    }

    public static AfterRegisterService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new AfterRegisterService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void handleAfterRegister(final String baseurl, final String trackId,
                                    final EventResult<String> callback) {
        String methodName = "AfterRegisterService :handleAfterRegister()";
        try {
            if (baseurl == null || baseurl.isEmpty() || trackId == null || trackId.trim().isEmpty()) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "baseurl and trackId must not be empty",
                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            String url = baseurl.replaceAll("/$", "")
                    + URLHelper.getShared().getLoginAfterRegisterUrl(trackId.trim());
            Map<String, String> headers = Headers.getShared(context)
                    .getHeaders(null, false, URLHelper.contentTypeJson);
            serviceForHandleAfterRegister(url, headers, callback);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTO_LOGIN_AFTER_REGISTER_FAILURE, e.getMessage()));
        }
    }

    private void serviceForHandleAfterRegister(String afterRegisterUrl, Map<String, String> headers,
                                               final EventResult<String> callback) {
        final String methodName = "AfterRegisterService :handleAfterRegister()";
        OkHttpClient noRedirectClient = service.getOKHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        RequestBody body = RequestBody.create("{}", MediaType.parse(URLHelper.contentTypeJson));
        Request.Builder requestBuilder = new Request.Builder().url(afterRegisterUrl).post(body);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        noRedirectClient.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                        WebAuthErrorCode.AUTO_LOGIN_AFTER_REGISTER_FAILURE, e.getMessage(),
                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try (okhttp3.Response r = response) {
                    String bodyText = null;
                    ResponseBody responseBody = r.body();
                    if (responseBody != null) {
                        try {
                            bodyText = responseBody.string();
                        } catch (java.io.IOException ignored) {
                            bodyText = null;
                        }
                    }
                    String code = AfterRegisterAuthorizationCodeParser.findAuthorizationCode(r, bodyText);
                    if (code != null && !code.isEmpty()) {
                        callback.success(code);
                        return;
                    }
                    String rawErr = bodyText != null && !bodyText.isEmpty() ? bodyText : r.message();
                    String err = rawErr != null && rawErr.length() > 500
                            ? rawErr.substring(0, 500) + "…"
                            : rawErr;
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.AUTO_LOGIN_AFTER_REGISTER_FAILURE,
                            "HTTP " + r.code() + " afterregister missing authorization code: " + err,
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.AUTO_LOGIN_AFTER_REGISTER_FAILURE, e.getMessage()));
                }
            }
        });
    }
}
