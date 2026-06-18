package de.cidaas.sdk.android.cidaasnative.domain.controller.setpassword;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.changepassword.ChangePasswordResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.setpassword.SetPasswordRequestEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.SetPassword.SetPasswordService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.properties.CidaasProperties;

public class SetPasswordController {

    private Context context;

    public static SetPasswordController shared;

    public SetPasswordController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static SetPasswordController getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new SetPasswordController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas)
                    .addFailureLog("SetPasswordController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }

    public void setPassword(
            @NonNull final SetPasswordRequestEntity setPasswordRequestEntity,
            final EventResult<ChangePasswordResponseEntity> callback) {
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {
                    String baseurl = result.get(NativeConstants.DOMAIN_URL);
                    checkAndSetPasswordService(baseurl, setPasswordRequestEntity, callback);
                }

                @Override
                public void failure(WebAuthError error) {
                    callback.failure(error);
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    "Exception :SetPasswordController :setPassword()",
                    WebAuthErrorCode.SET_PASSWORD_FAILURE,
                    e.getMessage()));
        }
    }

    private void checkAndSetPasswordService(
            String baseurl,
            @NonNull final SetPasswordRequestEntity setPasswordRequestEntity,
            final EventResult<ChangePasswordResponseEntity> callback) {
        String methodName = "SetPasswordController :checkAndSetPasswordService()";
        try {
            String password = setPasswordRequestEntity.getPassword();
            String confirmPassword = setPasswordRequestEntity.getConfirmPassword();
            if (password == null || password.isEmpty()) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "password must not be null or empty", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "confirmPassword must not be null or empty",
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            if (!password.equals(confirmPassword)) {
                callback.failure(WebAuthError.getShared(context).customException(
                        WebAuthErrorCode.SET_PASSWORD_FAILURE,
                        "password and confirmPassword must match",
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            if (baseurl == null || baseurl.isEmpty()) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Baseurl must not be null", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            SetPasswordService.getShared(context).setPassword(setPasswordRequestEntity, baseurl, null, callback);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.SET_PASSWORD_FAILURE,
                    e.getMessage()));
        }
    }
}
