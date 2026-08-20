package de.cidaas.sdk.android.cidaasnative.domain.controller.registration;

import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserDataEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserResponseEntity;

/**
 * Tenant {@code auto_login_after_register} is reflected on the register response as
 * {@code data.action = AUTO_LOGIN} plus a {@code trackId} for
 * {@code POST /login-srv/login/handle/afterregister/:track_id}.
 */
public final class AutoLoginAfterRegister {

    public static final String ACTION_AUTO_LOGIN = "AUTO_LOGIN";

    private AutoLoginAfterRegister() {
    }

    public static boolean shouldAttempt(RegisterNewUserResponseEntity response) {
        if (response == null || !response.isSuccess() || response.getData() == null) {
            return false;
        }
        RegisterNewUserDataEntity data = response.getData();
        String trackId = data.getTrackId();
        if (trackId == null || trackId.trim().isEmpty()) {
            return false;
        }
        return isAutoLoginAction(data.getAction()) || isAutoLoginAction(data.getSuggested_action());
    }

    public static String trackId(RegisterNewUserResponseEntity response) {
        if (response == null || response.getData() == null || response.getData().getTrackId() == null) {
            return "";
        }
        return response.getData().getTrackId().trim();
    }

    private static boolean isAutoLoginAction(String action) {
        return action != null && ACTION_AUTO_LOGIN.equalsIgnoreCase(action.trim());
    }
}
