package de.cidaas.sdk.android.service.repository.Login;

import android.content.Context;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.R;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.general.GenralHelper;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.service.CidaassdkService;
import de.cidaas.sdk.android.service.ICidaasSDKService;
import de.cidaas.sdk.android.service.entity.par.ParResponseEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Pushed Authorization Request (RFC 9126) against {@code POST /authz-srv/par}.
 * Uses PKCE only (no ClientSecret / Basic auth).
 */
public class ParService {

    private static final String DEFAULT_SCOPE = "openid profile";

    private final Context context;
    private CidaassdkService service;

    public static ParService shared;

    public ParService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaassdkService(contextFromCidaas);
        }
    }

    public static ParService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new ParService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    /**
     * Pushes authorization parameters with PKCE and returns {@link ParResponseEntity}
     * with {@code request_uri}. No ClientSecret / Basic auth.
     */
    public void generatePAR(String baseurl,
                            Dictionary<String, String> loginProperties,
                            Dictionary<String, String> challengeProperties,
                            Map<String, String> extraParams,
                            final EventResult<ParResponseEntity> callback) {
        final String methodName = "ParService :generatePAR()";
        try {
            if (baseurl == null || baseurl.equals("")) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

            String clientId = loginProperties != null ? loginProperties.get(CidaasConstants.CLIENT_ID) : null;
            String redirectUri = loginProperties != null ? loginProperties.get(CidaasConstants.REDIRECT_URL) : null;
            String codeChallenge = challengeProperties != null
                    ? challengeProperties.get(CidaasConstants.CHALLENGE) : null;

            if (clientId == null || clientId.isEmpty()
                    || redirectUri == null || redirectUri.isEmpty()
                    || codeChallenge == null || codeChallenge.isEmpty()) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "ClientId, RedirectURL and Challenge must not be empty for PAR",
                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

            String state = UUID.randomUUID().toString().replace("-", "");
            if (state.length() > 20) {
                state = state.substring(0, 20);
            }

            Dictionary<String, String> updatedChallenge = new Hashtable<>();
            if (challengeProperties != null) {
                Enumeration<String> keys = challengeProperties.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    updatedChallenge.put(key, challengeProperties.get(key));
                }
            }
            updatedChallenge.put("State", state);
            DBHelper.getShared().addChallengeProperties(updatedChallenge);

            Map<String, String> form = buildParFormBody(
                    clientId, redirectUri, codeChallenge, state, extraParams);

            Map<String, String> headers = Headers.getShared(context)
                    .getHeaders(null, false, URLHelper.contentType);

            String parUrl = baseurl + URLHelper.getShared().getParURL();
            serviceForPushAuthorizationRequest(parUrl, headers, form, callback);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.PAR_FAILURE, e.getMessage()));
        }
    }

    /**
     * Builds the form-urlencoded PAR body (RFC 9126). Visible for unit tests.
     */
    public static Map<String, String> buildParFormBody(String clientId,
                                                       String redirectUri,
                                                       String codeChallenge,
                                                       String state,
                                                       Map<String, String> extraParams) {
        Map<String, String> form = new HashMap<>();
        form.put("client_id", clientId);
        form.put("response_type", "code");
        form.put("redirect_uri", redirectUri);
        form.put("code_challenge", codeChallenge);
        form.put("code_challenge_method", GenralHelper.codeChallengeMethod);
        form.put("state", state);

        String scope = DEFAULT_SCOPE;
        if (extraParams != null && extraParams.get("scope") != null && !extraParams.get("scope").isEmpty()) {
            scope = extraParams.get("scope");
        } else if (Cidaas.extraParams != null && Cidaas.extraParams.get("scope") != null
                && !Cidaas.extraParams.get("scope").isEmpty()) {
            scope = Cidaas.extraParams.get("scope");
        }
        form.put("scope", scope);

        if (Cidaas.extraParams != null) {
            for (Map.Entry<String, String> entry : Cidaas.extraParams.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null
                        && !form.containsKey(entry.getKey())) {
                    form.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (extraParams != null) {
            for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null
                        && !"scope".equals(entry.getKey())) {
                    form.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return form;
    }

    private void serviceForPushAuthorizationRequest(String parUrl,
                                                    Map<String, String> headers,
                                                    Map<String, String> form,
                                                    final EventResult<ParResponseEntity> callback) {
        final String methodName = "ParService :serviceForPushAuthorizationRequest()";
        try {
            ICidaasSDKService cidaasSDKService = service.getInstance();
            cidaasSDKService.pushAuthorizationRequest(parUrl, headers, form)
                    .enqueue(new Callback<ParResponseEntity>() {
                        @Override
                        public void onResponse(Call<ParResponseEntity> call, Response<ParResponseEntity> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ParResponseEntity body = response.body();
                                if (body.getRequestUri() != null && !body.getRequestUri().isEmpty()) {
                                    callback.success(body);
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.PAR_FAILURE, response.code(),
                                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else if (response.isSuccessful()) {
                                callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                        WebAuthErrorCode.PAR_FAILURE, response.code(),
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                            } else {
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                        WebAuthErrorCode.PAR_FAILURE, response,
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<ParResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.PAR_FAILURE,
                                    t != null ? t.getMessage() : "PAR request failed",
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.PAR_FAILURE, e.getMessage()));
        }
    }
}
