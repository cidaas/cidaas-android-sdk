package de.cidaas.sdk.android.cidaasnative.domain.controller.deviceconfiguredverifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification.DeviceConfiguredVerificationsListRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification.DeviceConfiguredVerificationsListResponseEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.DeviceConfiguredVerifications.DeviceConfiguredVerificationsListService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;

public class DeviceConfiguredVerificationsListController {

    private Context context;

    public static DeviceConfiguredVerificationsListController shared;

    public DeviceConfiguredVerificationsListController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static DeviceConfiguredVerificationsListController getShared(Context contextFromCidaas) {
        if (shared == null) {
            shared = new DeviceConfiguredVerificationsListController(contextFromCidaas);
        }
        return shared;
    }

    public void getDeviceConfiguredVerificationsList(@NonNull final String sub,
            final EventResult<DeviceConfiguredVerificationsListResponseEntity> result) {
        final String methodName = "DeviceConfiguredVerificationsListController :getDeviceConfiguredVerificationsList()";
        try {
            if (sub == null || sub.isEmpty()) {
                result.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Sub must not be empty", methodName));
                return;
            }
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> lpresult) {
                    String baseurl = lpresult.get(NativeConstants.DOMAIN_URL);
                    String clientId = lpresult.get(NativeConstants.CLIENT_ID);
                    if (clientId == null || clientId.isEmpty()) {
                        result.failure(WebAuthError.getShared(context).propertyMissingException(
                                "ClientId must not be empty", methodName));
                        return;
                    }
                    DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
                    if (deviceInfo == null || deviceInfo.getDeviceId() == null
                            || deviceInfo.getDeviceId().isEmpty()) {
                        result.failure(WebAuthError.getShared(context).propertyMissingException(
                                "Device id is not available; ensure device info is stored before this call.",
                                methodName));
                        return;
                    }
                    DeviceConfiguredVerificationsListRequestEntity body =
                            new DeviceConfiguredVerificationsListRequestEntity();
                    body.setDevice_id(deviceInfo.getDeviceId());
                    String pushId = deviceInfo.getPushNotificationId();
                    body.setPush_id(pushId != null ? pushId : "");
                    body.setClient_id(clientId);
                    body.setSub(sub);
                    body.setLinked_device_id("");
                    DeviceConfiguredVerificationsListService.getShared(context)
                            .postDeviceConfiguredVerificationsList(baseurl, body, result);
                }

                @Override
                public void failure(WebAuthError error) {
                    result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException("",
                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.DEVICE_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }
}
