package de.cidaas.sdk.android.cidaasnative.domain.service.DeviceConfiguredVerifications;

import android.content.Context;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification.DeviceConfiguredVerificationsListRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification.DeviceConfiguredVerificationsListResponseEntity;
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

public class DeviceConfiguredVerificationsListService {

    private CidaasNativeService service;
    private Context context;

    public static DeviceConfiguredVerificationsListService shared;

    public DeviceConfiguredVerificationsListService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasNativeService(context);
        }
    }

    public static DeviceConfiguredVerificationsListService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new DeviceConfiguredVerificationsListService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void postDeviceConfiguredVerificationsList(String baseurl,
            DeviceConfiguredVerificationsListRequestEntity body,
            final EventResult<DeviceConfiguredVerificationsListResponseEntity> callback) {
        String methodName = NativeConstants.METHOD_DEVICE_CONFIGURED_VERIFICATIONS_LIST;
        try {
            if (baseurl == null || baseurl.equals("")) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            if (body == null) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Request body must not be null", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            String url = baseurl + NativeURLHelper.getShared().getDeviceConfiguredVerificationsList();
            Map<String, String> headers =
                    Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);
            executePost(url, headers, body, callback, methodName);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }

    private void executePost(String fullUrl, Map<String, String> headers,
            DeviceConfiguredVerificationsListRequestEntity body,
            final EventResult<DeviceConfiguredVerificationsListResponseEntity> callback, final String methodName) {
        try {
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.postDeviceConfiguredVerificationsList(fullUrl, headers, body)
                    .enqueue(new Callback<DeviceConfiguredVerificationsListResponseEntity>() {
                        @Override
                        public void onResponse(Call<DeviceConfiguredVerificationsListResponseEntity> call,
                                Response<DeviceConfiguredVerificationsListResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE,
                                            response.code(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if (response.errorBody() != null) {
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                            WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE, response,
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                } else {
                                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                            WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE,
                                            "HTTP " + response.code() + ": " + response.message(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<DeviceConfiguredVerificationsListResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }
}
