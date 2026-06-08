package de.cidaas.sdk.android.cidaasnative.domain.controller.publicconfiguredverifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist.PublicConfiguredListRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist.PublicConfiguredListResponseEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.publicconfiguredverifications.PublicConfiguredVerificationsListService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;

public class PublicConfiguredVerificationsListController {

    private Context context;

    public static PublicConfiguredVerificationsListController shared;

    public PublicConfiguredVerificationsListController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static PublicConfiguredVerificationsListController getShared(Context contextFromCidaas) {
        if (shared == null) {
            shared = new PublicConfiguredVerificationsListController(contextFromCidaas);
        }
        return shared;
    }

    /**
     * POST {@code /verification-srv/v2/setup/public/configured/list}. Body includes {@code identifier} (user handle),
     * {@code push_id} (FCM token from {@link DBHelper#getFCMToken()}), {@code request_id}, and {@code client_id}.
     */
    public void getPublicConfiguredVerificationsList(@NonNull final String requestId, @NonNull final String identifier,
            final EventResult<PublicConfiguredListResponseEntity> result) {
        final String methodName = "PublicConfiguredVerificationsListController :getPublicConfiguredVerificationsList()";
        try {
            if (requestId == null || requestId.isEmpty()) {
                result.failure(WebAuthError.getShared(context).propertyMissingException(
                        "requestId must not be empty", methodName));
                return;
            }
            if (identifier == null || identifier.isEmpty()) {
                result.failure(WebAuthError.getShared(context).propertyMissingException(
                        "identifier must not be empty", methodName));
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
                    PublicConfiguredListRequestEntity body = new PublicConfiguredListRequestEntity();
                    body.setRequest_id(requestId);
                    body.setIdentifier(identifier);
                    String fcmToken = DBHelper.getShared().getFCMToken();
                    body.setPush_id(fcmToken != null ? fcmToken : "");
                    body.setClient_id(clientId);
                    PublicConfiguredVerificationsListService.getShared(context)
                            .postPublicConfiguredVerificationsList(baseurl, body, result);
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
                    WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }
}
