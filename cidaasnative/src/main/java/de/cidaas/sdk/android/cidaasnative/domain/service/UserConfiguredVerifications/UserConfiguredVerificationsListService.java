package de.cidaas.sdk.android.cidaasnative.domain.service.UserConfiguredVerifications;

import android.content.Context;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.userconfiguredverification.UserConfiguredVerificationsListResponseEntity;
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

public class UserConfiguredVerificationsListService {

    private CidaasNativeService service;
    private Context context;

    public static UserConfiguredVerificationsListService shared;

    public UserConfiguredVerificationsListService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasNativeService(context);
        }
    }

    public static UserConfiguredVerificationsListService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new UserConfiguredVerificationsListService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void getUserConfiguredVerificationsList(String baseurl, String accessToken,
            final EventResult<UserConfiguredVerificationsListResponseEntity> callback) {
        String methodName = NativeConstants.METHOD_USER_CONFIGURED_VERIFICATIONS_LIST;
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
            String url = baseurl + NativeURLHelper.getShared().getUserConfiguredVerificationsList();
            Map<String, String> headers =
                    Headers.getShared(context).getHeaders(accessToken, false, NativeURLHelper.contentTypeJson);
            executeGet(url, headers, callback, methodName);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }

    private void executeGet(String fullUrl, Map<String, String> headers,
            final EventResult<UserConfiguredVerificationsListResponseEntity> callback, final String methodName) {
        try {
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.getUserConfiguredVerificationsList(fullUrl, headers)
                    .enqueue(new Callback<UserConfiguredVerificationsListResponseEntity>() {
                        @Override
                        public void onResponse(Call<UserConfiguredVerificationsListResponseEntity> call,
                                Response<UserConfiguredVerificationsListResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE,
                                            response.code(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if (response.errorBody() != null) {
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                            WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE, response,
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                } else {
                                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                            WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE,
                                            "HTTP " + response.code() + ": " + response.message(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UserConfiguredVerificationsListResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }
}
