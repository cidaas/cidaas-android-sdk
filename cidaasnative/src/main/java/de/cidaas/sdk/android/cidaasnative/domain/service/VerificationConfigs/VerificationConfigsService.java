package de.cidaas.sdk.android.cidaasnative.domain.service.VerificationConfigs;

import android.content.Context;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.verificationconfig.VerificationConfigsResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class VerificationConfigsService {

    private CidaasNativeService service;
    private Context context;

    public static VerificationConfigsService shared;

    public VerificationConfigsService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasNativeService(context);
        }
    }

    public static VerificationConfigsService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new VerificationConfigsService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void getVerificationConfigs(String baseurl, String accessToken,
            final EventResult<VerificationConfigsResponseEntity> callback) {
        String methodName = NativeConstants.METHOD_VERIFICATION_CONFIGS;
        try {
            if (baseurl == null || baseurl.equals("")) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            if (accessToken == null || accessToken.isEmpty()) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Access token must not be empty", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            String url = baseurl + NativeURLHelper.getShared().getVerificationConfigs();
            Map<String, String> headers =
                    Headers.getShared(context).getHeaders(accessToken, false, NativeURLHelper.contentTypeJson);
            executeGet(url, headers, callback, methodName);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE, e.getMessage()));
        }
    }

    private void executeGet(String fullUrl, Map<String, String> headers,
            final EventResult<VerificationConfigsResponseEntity> callback, final String methodName) {
        try {
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.getVerificationConfigs(fullUrl, headers)
                    .enqueue(new Callback<VerificationConfigsResponseEntity>() {
                        @Override
                        public void onResponse(Call<VerificationConfigsResponseEntity> call,
                                Response<VerificationConfigsResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE, response.code(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if (response.errorBody() != null) {
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                            WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE, response,
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                } else {
                                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                            WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE,
                                            "HTTP " + response.code() + ": " + response.message(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VerificationConfigsResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE, e.getMessage()));
        }
    }
}
