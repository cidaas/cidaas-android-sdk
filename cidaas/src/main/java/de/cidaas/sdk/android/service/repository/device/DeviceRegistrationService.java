package de.cidaas.sdk.android.service.repository.device;

import android.content.Context;

import java.util.Dictionary;
import java.util.Map;

import de.cidaas.sdk.android.R;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.CidaassdkService;
import de.cidaas.sdk.android.service.ICidaasSDKService;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationRequestEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationResponseEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class DeviceRegistrationService {

    private final CidaassdkService service;
    private final Context context;

    private static DeviceRegistrationService shared;

    public DeviceRegistrationService(Context contextFromCidaas) {
        context = contextFromCidaas;
        service = new CidaassdkService(context);
    }

    public static DeviceRegistrationService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new DeviceRegistrationService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void startRegistration(final String pushId,
                                  final EventResult<DeviceRegistrationResponseEntity> callback) {
        final String methodName = "DeviceRegistrationService :startRegistration()";
        DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
        String deviceId = deviceInfo != null ? deviceInfo.getDeviceId() : null;
        if (deviceId == null || deviceId.isEmpty()
                || pushId == null || pushId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "device id from deviceInfo and pushId must not be empty",
                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(Dictionary<String, String> loginProperties) {
                String baseurl = loginProperties.get(CidaasConstants.DOMAIN_URL);
                if (baseurl == null || baseurl.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            context.getString(R.string.EMPTY_BASE_URL_SERVICE), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
                String clientId = loginProperties.get(CidaasConstants.CLIENT_ID);
                if (clientId == null || clientId.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            "ClientId from saved properties must not be empty",
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
                String url = baseurl + URLHelper.getShared().getDeviceRegistrationURL() + "/initiation";
                Map<String, String> headers = Headers.getShared(context).getHeaders(
                        null, true, URLHelper.contentTypeJson);
                DeviceRegistrationRequestEntity body = new DeviceRegistrationRequestEntity(deviceId, pushId, clientId);
                enqueue(url, headers, body, callback, methodName);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(WebAuthError.getShared(context).cidaasPropertyMissingException(
                        error.getErrorMessage(), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        });
    }

    private void enqueue(String url, Map<String, String> headers, DeviceRegistrationRequestEntity body,
                         final EventResult<DeviceRegistrationResponseEntity> callback, final String methodName) {
        try {
            ICidaasSDKService api = service.getInstance();
            api.initiateDeviceRegistration(url, headers, body).enqueue(new Callback<DeviceRegistrationResponseEntity>() {
                @Override
                public void onResponse(Call<DeviceRegistrationResponseEntity> call,
                                       Response<DeviceRegistrationResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200 || response.code() == 201) {
                            DeviceRegistrationResponseEntity entity = response.body();
                            if (entity == null) {
                                entity = new DeviceRegistrationResponseEntity();
                                entity.setSuccess(true);
                                entity.setStatus(response.code());
                            }
                            callback.success(entity);
                        } else if (response.code() == 204) {
                            DeviceRegistrationResponseEntity entity = new DeviceRegistrationResponseEntity();
                            entity.setSuccess(true);
                            entity.setStatus(response.code());
                            callback.success(entity);
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                    WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response.code(),
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response,
                                "Error :DeviceRegistrationService :startRegistration()"));
                    }
                }

                @Override
                public void onFailure(Call<DeviceRegistrationResponseEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, t.getMessage(),
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE,
                    e.getMessage()));
        }
    }
}
