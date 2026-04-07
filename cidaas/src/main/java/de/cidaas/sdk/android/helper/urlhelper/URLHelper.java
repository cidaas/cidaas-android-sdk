package de.cidaas.sdk.android.helper.urlhelper;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.UUID;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.general.GenralHelper;

/**
 * Created by widasrnarayanan on 5/2/18.
 */

public class URLHelper {
    public static String contentType = "application/x-www-form-urlencoded";
    public static String contentTypeJson = "application/json";

    //Shared Instances
    public static URLHelper shared;


    public static URLHelper getShared() {
        if (shared == null) {
            shared = new URLHelper();
        }
        return shared;
    }


    String logoutURLForEmbeddedBrowser = "/session/end_session";
    String userLoginInfoURL = "/verification-srv/verificationstatus/status/search/sdk";


    String tokenUrl = "/token-srv/token";
    String socialTokenURL = "/login-srv/social/token";

    String preAuthCode = "&preAuthCode=";

    String scannedPatternURL = "/verification-srv/pattern/scanned";
    String scannedFaceURL = "/verification-srv/face/scanned";
    String scannedFingerprintURL = "/verification-srv/touchid/scanned";
    String scannedSmartPushURL = "/verification-srv/push/scanned";
    String scannedTOTPURL = "/verification-srv/totp/scanned";
    String scannedVoiceURL = "/verification-srv/voice/scanned";

    String scannedFIDOURL = "/verification-srv/fidou2f/mobile/scanned";

    String socialLoginURL = "/login-srv/social/login/";

    String internaluserProfileURL = "/users-srv/internal/userinfo/profile/";
    String userInfoURL = "/users-srv/userinfo";


    String documentScanner = "/access-control-srv/ocr/validate";


    String openIdURL = "/.well-known/openid-configuration";

    String setupEmailMFA = "/verification-srv/email/callSetup";
    String enrollEmailMFA = "/verification-srv/email/enroll";
    String initiateemailMFA = "/verification-srv/email/initiate";
    String authenticateemailMFA = "/verification-srv/email/authenticate";


    String setupSMSMFA = "/verification-srv/sms/callSetup";
    String enrollSMSMFA = "/verification-srv/sms/enroll";
    String initiateSMSMFA = "/verification-srv/sms/initiate";
    String authenticateSMSMFA = "/verification-srv/sms/authenticate";

    String setupIVRMFA = "/verification-srv/ivr/callSetup";
    String enrollIVRMFA = "/verification-srv/ivr/enroll";
    String initiateIVRMFA = "/verification-srv/ivr/initiate";
    String authenticateIVRMFA = "/verification-srv/ivr/authenticate";

    String setupBackupCodeMFA = "/verification-srv/backupcode/callSetup";
    String enrollBackupCodeMFA = "/verification-srv/backupcode/enroll";
    String initiateBackupCodeMFA = "/verification-srv/backupcode/initiate";
    String authenticateBackupCodeMFA = "/verification-srv/backupcode/authenticate";

    String setupFaceMFA = "/verification-srv/face/callSetup";
    String enrollFaceMFA = "/verification-srv/face/enroll";
    String initiateFaceMFA = "/verification-srv/face/initiate";
    String authenticateFaceMFA = "/verification-srv/face/authenticate";

    String setupFingerprintMFA = "/verification-srv/touchid/callSetup";
    String enrollFingerprintMFA = "/verification-srv/touchid/enroll";
    String initiateFingerprintMFA = "/verification-srv/touchid/initiate";
    String authenticateFingerprintMFA = "/verification-srv/touchid/authenticate";

    String setupFIDOMFA = "/verification-srv//fidou2f/mobile/callSetup";
    String enrollFIDOMFA = "/verification-srv/fidou2f/mobile/enroll";
    String initiateFIDOMFA = "/verification-srv/fidou2f/mobile/initiate";
    String authenticateFIDOMFA = "/verification-srv/fidou2f/mobile/authenticate";

    String setupPatternMFA = "/verification-srv/pattern/callSetup";
    String enrollPatternMFA = "/verification-srv/pattern/enroll";
    String initiatePatternMFA = "/verification-srv/pattern/initiate";
    String authenticatePatternMFA = "/verification-srv/pattern/authenticate";

    String setupSmartPushMFA = "/verification-srv/push/callSetup";
    String enrollSmartPushMFA = "/verification-srv/push/enroll";
    String initiateSmartPushMFA = "/verification-srv/push/initiate";
    String authenticateSmartPushMFA = "/verification-srv/push/authenticate";

    String setupTOTPMFA = "/verification-srv/totp/callSetup";
    String enrollTOTPMFA = "/verification-srv/totp/enroll";
    String initiateTOTPMFA = "/verification-srv/totp/initiate";
    String authenticateTOTPMFA = "/verification-srv/totp/authenticate";

    String setupVoiceMFA = "/verification-srv/voice/callSetup";
    String enrollVoiceMFA = "/verification-srv/voice/enroll";
    String initiateVoiceMFA = "/verification-srv/voice/initiate";
    String authenticateVoiceMFA = "/verification-srv/voice/authenticate";

    public String getUserLoginInfoURL() {
        return userLoginInfoURL;
    }

    public String getUserInfoURL() {
        return userInfoURL;
    }

    public String getInternaluserProfileURL() {
        return internaluserProfileURL;
    }


    public String getSetupEmailMFA() {
        return setupEmailMFA;
    }

    public String getEnrollEmailMFA() {
        return enrollEmailMFA;
    }

    public String getSetupSMSMFA() {
        return setupSMSMFA;
    }

    public String getEnrollSMSMFA() {
        return enrollSMSMFA;
    }

    public String getSetupIVRMFA() {
        return setupIVRMFA;
    }

    public String getEnrollIVRMFA() {
        return enrollIVRMFA;
    }

    public String getSetupBackupCodeMFA() {
        return setupBackupCodeMFA;
    }

    public String getEnrollBackupCodeMFA() {
        return enrollBackupCodeMFA;
    }

    public String getSetupFaceMFA() {
        return setupFaceMFA;
    }

    public String getEnrollFaceMFA() {
        return enrollFaceMFA;
    }

    public String getSetupFingerprintMFA() {
        return setupFingerprintMFA;
    }

    public String getEnrollFingerprintMFA() {
        return enrollFingerprintMFA;
    }

    public String getSetupFIDOMFA() {
        return setupFIDOMFA;
    }

    public String getEnrollFIDOMFA() {
        return enrollFIDOMFA;
    }

    public String getSetupPatternMFA() {
        return setupPatternMFA;
    }

    public String getEnrollPatternMFA() {
        return enrollPatternMFA;
    }

    public String getSetupSmartPushMFA() {
        return setupSmartPushMFA;
    }

    public String getEnrollSmartPushMFA() {
        return enrollSmartPushMFA;
    }

    public String getSetupTOTPMFA() {
        return setupTOTPMFA;
    }

    public String getEnrollTOTPMFA() {
        return enrollTOTPMFA;
    }

    public String getSetupVoiceMFA() {
        return setupVoiceMFA;
    }

    public String getEnrollVoiceMFA() {
        return enrollVoiceMFA;
    }

    public String getInitiateFaceMFA() {
        return initiateFaceMFA;
    }

    public String getAuthenticateFaceMFA() {
        return authenticateFaceMFA;
    }

    public String getInitiateFingerprintMFA() {
        return initiateFingerprintMFA;
    }

    public String getAuthenticateFingerprintMFA() {
        return authenticateFingerprintMFA;
    }

    public String getInitiateFIDOMFA() {
        return initiateFIDOMFA;
    }

    public String getAuthenticateFIDOMFA() {
        return authenticateFIDOMFA;
    }

    public String getInitiatePatternMFA() {
        return initiatePatternMFA;
    }

    public String getAuthenticatePatternMFA() {
        return authenticatePatternMFA;
    }

    public String getInitiateSmartPushMFA() {
        return initiateSmartPushMFA;
    }

    public String getAuthenticateSmartPushMFA() {
        return authenticateSmartPushMFA;
    }

    public String getInitiateTOTPMFA() {
        return initiateTOTPMFA;
    }

    public String getAuthenticateTOTPMFA() {
        return authenticateTOTPMFA;
    }

    public String getInitiateVoiceMFA() {
        return initiateVoiceMFA;
    }

    public String getAuthenticateVoiceMFA() {
        return authenticateVoiceMFA;
    }

    public String getInitiateIVRMFA() {
        return initiateIVRMFA;
    }

    public String getAuthenticateIVRMFA() {
        return authenticateIVRMFA;
    }

    public String getInitiateBackupCodeMFA() {
        return initiateBackupCodeMFA;
    }

    public String getAuthenticateBackupCodeMFA() {
        return authenticateBackupCodeMFA;
    }

    public String getInitiateSMSMFA() {
        return initiateSMSMFA;
    }

    public String getAuthenticateSMSMFA() {
        return authenticateSMSMFA;
    }

    public String getAuthenticateemailMFA() {
        return authenticateemailMFA;
    }

    public String getInitiateemailMFA() {
        return initiateemailMFA;
    }

    public String getLogoutURLForEmbeddedBrowser() {
        return logoutURLForEmbeddedBrowser;
    }

    public String getSocialLoginURL() {
        return socialLoginURL;
    }

    public String getOpenIdURL() {
        return openIdURL;
    }


    public String getPreAuthCode() {
        return preAuthCode;
    }

    public String getSocialTokenURL() {
        return socialTokenURL;
    }


    public String getDocumentScanner() {
        return documentScanner;
    }


    public String getScannedFIDOURL() {
        return scannedFIDOURL;
    }

    public String getScannedFaceURL() {
        return scannedFaceURL;
    }

    public String getScannedFingerprintURL() {
        return scannedFingerprintURL;
    }

    public String getScannedSmartPushURL() {
        return scannedSmartPushURL;
    }

    public String getScannedTOTPURL() {
        return scannedTOTPURL;
    }

    public String getScannedVoiceURL() {
        return scannedVoiceURL;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }


    public String getScannedPatternURL() {
        return scannedPatternURL;
    }


    public String constructLoginURL(String authzURL, String clientId, String redirectURL, String codeChallenge, String viewType) {
        return constructLoginURL(authzURL, clientId, redirectURL, codeChallenge, viewType, null);
    }

    public String constructLoginURL(String authzURL, String clientId, String redirectURL, String codeChallenge, String viewType,
                                    @Nullable Map<String, String> extraParams) {
        try {
            Uri.Builder builder = new Uri.Builder();


            builder
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("view_type", viewType)
                    .appendQueryParameter("nonce", UUID.randomUUID().toString())
                    .appendQueryParameter("redirect_uri", redirectURL)
                    .appendQueryParameter("code_challenge", codeChallenge)
                    .appendQueryParameter("code_challenge_method", GenralHelper.codeChallengeMethod);

            for (Map.Entry<String, String> entry : Cidaas.extraParams.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

            if (extraParams != null) {
                for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        builder.appendQueryParameter(entry.getKey(), entry.getValue());
                    }
                }
            }

            return authzURL + builder.build().toString();
        } catch (Exception e) {
            return "URL Construction Failed Something went Wrong";
            // handle Exception
        }
    }

    //Perform Construct by Challenge
    public String constructURLbyChallenge(String challenge) {
        try {
            Uri.Builder builder = new Uri.Builder();

            String authority = challenge;

            builder.scheme("https")
                    .encodedAuthority(authority);

            // Add ClientId and otherinformation
            //.appendQueryParameter("client_id", CidaasSDKEntity.cidaasSDKEntityInstance.getClientId());


            for (Map.Entry<String, String> entry : Cidaas.extraParams.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

            return builder.build().toString();
        } catch (Exception e) {
            return "constructURLbyChallenge Fails something went wrong";
            //handle Exception
        }
    }


    //Construct url for social Login
    public String constructSocialURL(String baseURL, String provider, String requestId) {
        try {
            String finalSocialURL = baseURL + socialLoginURL + provider + "/" + requestId;

           /* Uri.Builder builder = new Uri.Builder();
            builder
                    .appendQueryParameter("provider", provider)
                    .appendQueryParameter("requestId", requestId);*/

            return finalSocialURL;/*+builder.build().toString()*/
        } catch (Exception e) {
            return "";
        }
    }
}
