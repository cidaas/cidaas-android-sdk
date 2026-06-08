package de.cidaas.sdk.android.cidaasverification.data.service.helper;

public class VerificationURLHelper {
    public static String contentType = "application/x-www-form-urlencoded";
    public static String contentTypeJson = "application/json";

    // Shared Instances
    public static VerificationURLHelper shared;

    public static VerificationURLHelper getShared() {
        if (shared == null) {
            shared = new VerificationURLHelper();
        }
        return shared;
    }

    // V2-Verification-Setup
    String setupURL = "/verification-actions-srv/setup/";

    // V2-Verification-initiate
    String initiateURL = "/verification-srv/v2/authenticate/initiate/";

    // V2-Verification-Push
    private String pushAcknowledgeURL = "/verification-srv/v2/authenticate/push_acknowledge/";
    private String pushAllowURL = "/verification-srv/v2/authenticate/allow/";
    private String pushRejectURL = "/verification-srv/v2/authenticate/reject/";

    // V2-Verification-Authenticate
    private String authenticateURL = "/verification-srv/v2/authenticate/authenticate/";

    // V2-Verification-Delete
    private String deleteURL = "/verification-srv/v2/setup/device/configured/remove/";
    private String deleteAllURL = "/verification-srv/v2/setup/device/configured/removeallbydeviceid/";

    // V2-Verification-Get Details
    private String getConfiguredListURL = "/verification-srv/v2/setup/device/configured/list";

    private String getPendingNotificationURL = "/verification-srv/v2/setup/device/pending/auth/list";

    private String getAuthentictedHistoryURL = "/verification-srv/v2/setup/device/authenticated/list";
    private String userHistoryURL = "/verification-srv/v2/device/mfa/history";
    private String userHistoryDetailURL = "/verification-srv/v2/device/mfa/timeline";
    // Update FCM Token
    private String updateFCMTokenURL = "/verification-srv/v2/setup/device/update/pushid";

    // Login Continue call Passwordless
    private String passwordlessContinueUrl = "/login-srv/verification/sdk/login/";

    /** Resume login after v2 authenticate (OTP login); POST body same shape as passwordless continue. */
    private String verificationLoginUrl = "/login-srv/verification/login";

    // Login Continue call MFA
    private String mfaContinueCallUrl = "/login-srv/precheck/continue/sdk/";

    private String userDevices = "/verification-srv/v2/manage/mfa/device/list";
    private String userDevicesUnlink = "/verification-srv/v2/manage/mfa/device/unlink";
    String setUpCancelURL = "/verification-srv/v2/setup/cancel/";

    public String getSetupURL(String baseurl, String verificationType) {
        return baseurl + setupURL + (verificationType.toLowerCase()) + "/initiation";
    }

    public String getScannedURL(String baseurl, String verificationType) {
        return baseurl + setupURL + (verificationType.toLowerCase()) + "/scan";
    }

    public String getEnrollURL(String baseurl, String verificationType) {
        return baseurl + setupURL + (verificationType.toLowerCase()) + "/verification";
    }

    public String getInitiateURL(String baseurl, String verificationType) {
        return baseurl + initiateURL + (verificationType.toLowerCase());
    }

    public String getPushAcknowledgeURL(String baseurl, String verificationType) {
        return baseurl + pushAcknowledgeURL + (verificationType.toLowerCase());
    }

    public String getPushAllowURL(String baseurl, String verificationType) {
        return baseurl + pushAllowURL + (verificationType.toLowerCase());
    }

    public String getPushDenyURL(String baseurl, String verificationType) {
        return baseurl + pushRejectURL + (verificationType.toLowerCase());
    }

    public String getAuthenticateURL(String baseurl, String verificationType) {
        return baseurl + authenticateURL + (verificationType.toLowerCase());
    }

    public String getDeleteURL(String baseurl, String verificationType, String sub) {
        return baseurl + deleteURL + (verificationType.toLowerCase()) + "/" + sub;
    }

    public String getDeleteAllURL(String baseurl, String deviceId) {
        return baseurl + deleteAllURL + deviceId;
    }

    public String getConfiguredListURL(String baseurl) {
        return baseurl + getConfiguredListURL;
    }

    public String getPendingNotificationURL(String baseurl) {
        return baseurl + getPendingNotificationURL;
    }

    public String getAuthentictedHistoryURL(String baseurl) {
        return baseurl + getAuthentictedHistoryURL;
    }

    public String getAuthentictedHistoryURLNew(String baseurl) {
        return baseurl + userHistoryURL;
    }

    public String getAuthentictedHistoryDetailURL(String baseurl) {
        return baseurl + userHistoryDetailURL;
    }

    public String getUpdateFCMTokenURL(String baseurl) {
        return baseurl + updateFCMTokenURL;
    }

    public String getPasswordlessContinueUrl(String baseurl) {
        return baseurl + passwordlessContinueUrl;
    }

    public String getVerificationLoginUrl(String baseurl) {
        return baseurl + verificationLoginUrl;
    }

    public String getMfaContinueCallUrl(String baseurl, String trackId) {
        return baseurl + mfaContinueCallUrl + trackId;
    }

    public String getDevicesList(String baseurl) {
        // return baseurl + getAuthentictedHistoryURL;
        return baseurl + userDevices;
    }

    public String getDevicesRemove(String baseurl) {
        // return baseurl + getAuthentictedHistoryURL;
        return baseurl + userDevicesUnlink;
    }

    public String getSetUpCancelURL(String baseurl, String verificationType) {
        return baseurl + setUpCancelURL + (verificationType);
    }
}
