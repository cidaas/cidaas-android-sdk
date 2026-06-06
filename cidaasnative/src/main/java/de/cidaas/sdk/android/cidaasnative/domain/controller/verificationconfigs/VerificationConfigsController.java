package de.cidaas.sdk.android.cidaasnative.domain.controller.verificationconfigs;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.verificationconfig.VerificationConfigsResponseEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.VerificationConfigs.VerificationConfigsService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;

public class VerificationConfigsController {

    private Context context;

    public static VerificationConfigsController shared;

    public VerificationConfigsController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static VerificationConfigsController getShared(Context contextFromCidaas) {
        if (shared == null) {
            shared = new VerificationConfigsController(contextFromCidaas);
        }
        return shared;
    }

    public void getVerificationConfigs(@NonNull final String accessToken,
            final EventResult<VerificationConfigsResponseEntity> result) {
        final String methodName = "VerificationConfigsController :getVerificationConfigs()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> lpresult) {
                    String baseurl = lpresult.get(NativeConstants.DOMAIN_URL);
                    if (accessToken != null && !accessToken.isEmpty()) {
                        VerificationConfigsService.getShared(context).getVerificationConfigs(baseurl, accessToken,
                                result);
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
                    WebAuthErrorCode.VERIFICATION_CONFIGS_FAILURE, e.getMessage()));
        }
    }
}
