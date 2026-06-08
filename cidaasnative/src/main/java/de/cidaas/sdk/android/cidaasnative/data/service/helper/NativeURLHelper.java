package de.cidaas.sdk.android.cidaasnative.data.service.helper;

import de.cidaas.sdk.android.helper.AuthenticationType;

public class NativeURLHelper {
    public static String contentType = "application/x-www-form-urlencoded";
    public static String contentTypeJson = "application/json";

    // Shared Instances
    public static NativeURLHelper shared;

    public static NativeURLHelper getShared() {
        if (shared == null) {
            shared = new NativeURLHelper();
        }
        return shared;
    }

    String request_id_url = "/authz-srv/authrequest/authz/generate";
    String clientUrl = "/public-srv/public/";
    String tenantUrl = "/public-srv/tenantinfo/basic";
    String loginWithCredentials = "/login-srv/login/sdk";
    String logoutUrl = "/session/end_session";

    String passwordlessContinueUrl = "/login-srv/verification/sdk/login/";
    String deduplicationList = "/users-srv/deduplication/info/";
    String registerdeduplication = "/users-srv/deduplication/register/";

    String registrationSetup = "/registration-setup-srv/public/list?acceptlanguage=";

    String registerNewUserurl = "/users-srv/register";

    String updateUserProfileURL = "/users-srv/user/profile";

    String mfa_URL = "/verification-srv/settings/list?sub=";
    String mfaList = "/verification-srv/settings/listbydeviceid";

    String deleteMFA = "/verification-srv/settings/delete/";
    String deleteAllMFA = "/verification-srv/settings/deleteall/";

    String denyNotification = "/verification-srv/notification/reject";
    String PendingNotificationURL = "/verification-srv/notification/initiated/";

    String configuredMFAListURL = "/verification-srv/settings/listbydeviceid";

    String updateFCMTokenURL = "/device-srv/device/updatefcm";

    String resetPasswordURL = "/password-srv/resetpassword";
    String ChangePasswordURL = "/password-srv/password";

    String RegisterUserAccountInitiate = "/verification-actions-srv/account/initiation";
    String RegisterUserAccountVerify = "/verification-actions-srv/account";
    String verificationConfigs = "/verification-actions-srv/config";
    String userConfiguredVerificationsList = "/verification-actions-srv/users/configurations";
    String deviceConfiguredVerificationsList = "/verification-actions-srv/setup/devices";

    /** POST body: {@code request_id}, {@code identifier}, {@code push_id} (FCM), {@code client_id}. */
    String publicConfiguredVerificationsList = "/verification-srv/v2/setup/public/configured/list";
    String accountVerificationList = "/users-srv/user/communication/status/";

    String validateDeviceURL = "/verification-srv/device/validate";

    // Token SRV
    String preLoginCheckURL = "/token-srv/prelogin/metadata/";

    String progressiveUpdateURL = "/login-srv/progressive/update/user";

    public String getProgressiveUpdateURL() {
        return progressiveUpdateURL;
    }

    public String getPreLoginCheckUrl(String trackId) {
        return preLoginCheckURL + trackId;
    }

    public String getRequest_id_url() {
        return request_id_url;
    }

    public String getClientUrl(String requestId) {
        return clientUrl + requestId;
    }

    public String getTenantUrl() {
        return tenantUrl;
    }

    public String getLoginWithCredentials() {
        return loginWithCredentials;
    }

    public String getLogoutUrl(String accessToken) {
        return logoutUrl + "?access_token_hint=" + accessToken;
    }

    public String getRegisterdeduplication() {
        return registerdeduplication;
    }

    public String getDeduplicationList() {
        return deduplicationList;
    }

    public String getPasswordlessContinueUrl() {
        return passwordlessContinueUrl;
    }

    public String getPendingNotificationURL() {
        return PendingNotificationURL;
    }

    public String getConfiguredMFAListURL() {
        return configuredMFAListURL;
    }

    public String getDeleteMFA(String userDeviceId, String verificationType) {
        return deleteMFA + userDeviceId + "/" + verificationType;
    }

    public String getUpdateFCMTokenURL() {
        return updateFCMTokenURL;
    }

    public String getDeleteAllMFA() {
        return deleteAllMFA;
    }

    public String getDenyNotification() {
        return denyNotification;
    }

    public String getDeleteEmailMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/EMAIL";
    }

    public String getDeleteSMSMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/SMS";
    }

    public String getDeleteIVRMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/IVR";
    }

    public String getDeleteBackupcodeMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/BACKUPCODE";
    }

    public String getDeletePatternMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/PATTERN";
    }

    public String getDeleteFingerprintMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/" + AuthenticationType.FINGERPRINT;
    }

    public String getDeleteSmartPushMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/" + AuthenticationType.SMARTPUSH;
    }

    public String getDeleteFaceMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/FACE";
    }

    public String getDeleteVoiceMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/VOICE";
    }

    public String getDeleteFIDOMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/FIDOU2F";
    }

    public String getDeleteTOTPMFA(String userDeviceId) {
        return deleteMFA + userDeviceId + "/TOTP";
    }

    public String getMfaList() {
        return mfaList;
    }

    public String getValidateDeviceURL() {
        return validateDeviceURL;
    }

    public String getRegisterUserAccountInitiate() {
        return RegisterUserAccountInitiate;
    }

    public String getRegisterUserAccountVerify() {
        return RegisterUserAccountVerify;
    }

    public String getVerificationConfigs() {
        return verificationConfigs;
    }

    public String getUserConfiguredVerificationsList() {
        return userConfiguredVerificationsList;
    }

    public String getDeviceConfiguredVerificationsList() {
        return deviceConfiguredVerificationsList;
    }

    public String getPublicConfiguredVerificationsList() {
        return publicConfiguredVerificationsList;
    }

    public String getAccountVerificationList(String sub) {
        return accountVerificationList + sub;
    }

    public String getChangePasswordURL() {
        return ChangePasswordURL;
    }

    public String getResetPassword() {
        return resetPasswordURL;
    }

    public String getMfa_URL() {
        return mfa_URL;
    }

    public String getRegisterNewUserurl() {
        return registerNewUserurl;
    }

    public String getRegistrationSetup(String acceptedLanguage, String requestId) {
        return registrationSetup + acceptedLanguage + "&requestId=" + requestId;
    }

    public String getUpdateUserProfileURL(String sub) {
        return updateUserProfileURL + "/" + sub;
    }
}
