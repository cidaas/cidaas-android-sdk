package de.cidaas.sdk.android.cidaasnative.domain.controller.userconfiguredverifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.userconfiguredverification.UserConfiguredVerificationsListResponseEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.UserConfiguredVerifications.UserConfiguredVerificationsListService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;

public class UserConfiguredVerificationsListController {

    private Context context;

    public static UserConfiguredVerificationsListController shared;

    public UserConfiguredVerificationsListController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static UserConfiguredVerificationsListController getShared(Context contextFromCidaas) {
        if (shared == null) {
            shared = new UserConfiguredVerificationsListController(contextFromCidaas);
        }
        return shared;
    }

    public void getUserConfiguredVerificationsList(@NonNull final String accessToken,
            final EventResult<UserConfiguredVerificationsListResponseEntity> result) {
        final String methodName = "UserConfiguredVerificationsListController :getUserConfiguredVerificationsList()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> lpresult) {
                    String baseurl = lpresult.get(NativeConstants.DOMAIN_URL);
                    if (accessToken != null && !accessToken.isEmpty()) {
                        UserConfiguredVerificationsListService.getShared(context)
                                .getUserConfiguredVerificationsList(baseurl, accessToken, result);
                    } else {
                        result.failure(WebAuthError.getShared(context).propertyMissingException(
                                "Access token must not be empty", methodName));
                    }
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
                    WebAuthErrorCode.USER_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }
}
