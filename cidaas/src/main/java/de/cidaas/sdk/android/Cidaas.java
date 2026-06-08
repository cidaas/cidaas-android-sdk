package de.cidaas.sdk.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.cidaas.sdk.android.browser.WebAuth;
import de.cidaas.sdk.android.device.Device;
import de.cidaas.sdk.android.device.Devices;
import de.cidaas.sdk.android.users.Users;
import de.cidaas.sdk.android.verifications.Verifications;
import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.controller.DocumentScannnerController;
import de.cidaas.sdk.android.controller.LocalAuthenticationController;
import de.cidaas.sdk.android.controller.LoginController;
import de.cidaas.sdk.android.controller.UserLoginInfoController;
import de.cidaas.sdk.android.controller.UserProfileController;
import de.cidaas.sdk.android.entities.LocalAuthenticationEntity;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.entities.SocialAccessTokenEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.loaders.ICustomLoader;
import de.cidaas.sdk.android.library.biometricauthentication.BiometricCallback;
import de.cidaas.sdk.android.library.biometricauthentication.BiometricEntity;
import de.cidaas.sdk.android.service.entity.UserInfo.UserInfoEntity;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.entity.documentscanner.DocumentScannerServiceResultEntity;
import de.cidaas.sdk.android.service.entity.userlogininfo.UserLoginInfoEntity;
import de.cidaas.sdk.android.service.entity.userlogininfo.UserLoginInfoResponseEntity;
import rx.android.BuildConfig;

public class Cidaas {

    EventResult<LocalAuthenticationEntity> localAuthenticationEntityCallback;

    public Context context;
    public Activity activityFromCidaas;

    public static String usagePass = "";
    public static ICustomLoader loader;

    public static final String FIDO_VERSION = "U2F_V2";

    public WebAuthError webAuthError = null;

    // Confirm it must be a static one
    // Extra parameter that is passed in URL
    public static HashMap<String, String> extraParams = new HashMap<>();

    // Create a Shared Instance
    private static Cidaas cidaasInstance;

    public static Cidaas getInstance(Context YourActivitycontext) {
        if (cidaasInstance == null) {
            cidaasInstance = new Cidaas(YourActivitycontext);
        }

        return cidaasInstance;
    }

    // Constructor
    public Cidaas(Context yourActivityContext) {
        this.context = yourActivityContext;
        CidaasHelper.getShared(yourActivityContext).initialiseObject();
    }

    // -----------------------------------------_Common For Cidaas
    // Instances-------------------------------------------------------------------------

    public boolean isENABLE_PKCE() {
        return CidaasHelper.getShared(context).isENABLE_PKCE();
    }

    public void setENABLE_PKCE(boolean ENABLE_PKCE) {
        CidaasHelper.getShared(context).setENABLE_PKCE(ENABLE_PKCE);
    }

    // enableLog

    public boolean isLogEnable() {
        return CidaasHelper.getShared(context).isLogEnable();
    }

    public String enableLog() {
        return CidaasHelper.getShared(context).enableLog();
    }

    public String disableLog() {
        return CidaasHelper.getShared(context).disableLog();
    }

    /**
     * Enable optional certificate pinning for Retrofit/OkHttp calls to the cidaas
     * instance.
     * Pins are applied to the host from the configured domain URL.
     *
     * @param pinHashes SHA-256 pins in OkHttp format, e.g.
     *                  {@code sha256/AAAAAAAA...=}
     */
    public void setCertificatePinning(@NonNull String... pinHashes) {
        CidaasHelper.setCertificatePinning(pinHashes);
    }

    /**
     * Enable optional certificate pinning for a specific host.
     */
    public void setCertificatePinning(@NonNull String host, @NonNull String... pinHashes) {
        CidaasHelper.setCertificatePinning(host, pinHashes);
    }

    public void clearCertificatePinning() {
        CidaasHelper.clearCertificatePinning();
    }

    // ****** LOGIN WITH Document
    // *****-------------------------------------------------------------------------------------------------------
    public void VerifyDocument(final File photo, final String sub,
            final EventResult<DocumentScannerServiceResultEntity> resultEntityResult) {
        DocumentScannnerController.getShared(context).sendtoServicecall(photo, sub, resultEntityResult);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // @Override
    public void getAccessToken(String sub, EventResult<AccessTokenEntity> result) {
        AccessTokenController.getShared(context).getAccessToken(sub, result);
    }

    public void getAccessTokenFromRefreshToken(String refershtoken, EventResult<AccessTokenEntity> result) {
        AccessTokenController.getShared(context).getAccessTokenByRefreshToken(refershtoken, result);
    }

    public void getAccessTokenBySocial(SocialAccessTokenEntity socialAccessTokenEntity,
            EventResult<AccessTokenEntity> result) {
        AccessTokenController.getShared(context).getAccessTokenBySocial(socialAccessTokenEntity, result);
    }

    // For Authenticator App
    public void setAccessToken(final AccessTokenEntity accessTokenEntity,
            final EventResult<LoginCredentialsResponseEntity> result) {
        AccessTokenController.getShared(context).setAccessToken(accessTokenEntity, result);
    }

    // Get userinfo Based on Access Token
    // @Override
    public void getUserInfo(String sub, final EventResult<UserInfoEntity> callback) {
        UserProfileController.getShared(context).getUserProfile(sub, callback);
    }

    // Resume After open App From Broswer
    public void handleToken(String code) {
        LoginController.getShared(context).handleToken(code);
    }

    // Custom Tab
    public void loginWithBrowser(@NonNull final Context activityContext, @Nullable final String color,
            final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).loginWithBrowser(activityContext, color, callbacktoMain);
    }

    public void loginWithBrowser(@NonNull final Context activityContext, @Nullable final String color,
            @Nullable final Map<String, String> extraParams,
            final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).loginWithBrowser(activityContext, color, extraParams, callbacktoMain);
    }

    public void logoutWithBrowser(@NonNull final Context activityContext, @NonNull final String sub,
            @Nullable final String post_redirect_uri, @Nullable final String color,
            final EventResult<Boolean> callbacktoMain) {
        LoginController.getShared(context).logoutWithBrowser(activityContext, sub, post_redirect_uri, color,
                callbacktoMain);
    }

    /**
     * Browser-based login, logout, and social login. Pass the {@link Context} used
     * to launch the custom tab
     * (typically your current {@link Activity}). Example:
     * {@code cidaas.webAuth(this).extraParams(map).signIn(callback);}
     */
    @NonNull
    public WebAuth webAuth(@NonNull Context activityContext) {
        return new WebAuth(this, activityContext);
    }

    /**
     * OAuth / hosted-flow {@code requestId} (delegates to
     * {@code CidaasNative.getRequestId} at runtime). Example:
     * {@code cidaas.requestId().fetch(callback);}
     */
    @NonNull
    public AuthRequestId requestId() {
        return new AuthRequestId(this, null);
    }

    /**
     * User self-service (password reset, registration; delegates to
     * {@code cidaasnative} at runtime). Example:
     * {@code cidaas.users().passwordReset().initiate(requestEntity, callback);}
     * {@code cidaas.users().accountVerification().initiate(initiateRequestEntity, callback);}
     * {@code cidaas.users().accountVerification().validate(verifyRequestEntity, callback);}
     * {@code cidaas.users().changePassword(sub, changePasswordRequestEntity, callback);}
     * {@code cidaas.users().fetch(sub, callback);}
     * {@code cidaas.users().register(registrationEntity, callback);} or
     * {@code cidaas.users().register(requestId, registrationEntity, callback);}
     * {@code cidaas.users().verifications().fetch(sub, callback);}
     */
    @NonNull
    public Users users() {
        return new Users(context);
    }

    /**
     * Tenant verification methods configuration (GET
     * {@code verification-actions-srv/config}). Example:
     * {@code cidaas.verifications().fetch(sub, callback);}
     * {@code cidaas.verifications().enrolment().fingerprint(activity, sub, callback);}
     * {@code cidaas.verifications().login().otp().initiate(loginRequest, VerificationLoginOtp.AcceptMethod.SMS, cb);}
     * {@code cidaas.verifications().login().otp().verify(otp, loginRequest, exchangeId, VerificationLoginOtp.AcceptMethod.SMS, cb);}
     * {@code cidaas.verifications().login().otp().continueLogin(loginRequest, authenticateResponse, VerificationLoginOtp.AcceptMethod.SMS, cb);}
     * {@code cidaas.verifications().enrolment().push(activity, sub, dialogTitle, dialogMessage, R.style.MyPushDialog, callback);}
     * {@code cidaas.verifications().enrolment().pattern(activity, sub, dialogTitle, dialogMessage, R.style.MyPatternDialog, callback);}
     */
    @NonNull
    public Verifications verifications() {
        return new Verifications(context);
    }

    @NonNull
    public Device device() {
        return new Device(this);
    }

    /**
     * Device-scoped verification (configured methods on this device). Example:
     * {@code cidaas.devices().verifications().fetch(sub, callback);}
     * {@code cidaas.devices().verifications().remove(sub, verificationType, callback);}
     */
    @NonNull
    public Devices devices() {
        return new Devices(this);
    }

    /**
     * Persists the Firebase Cloud Messaging (FCM) registration token as the device
     * push id. The token is stored for
     * subsequent SDK calls that send {@code push_id} (for example device
     * registration and verification flows).
     * Equivalent to the internal
     * {@linkplain de.cidaas.sdk.android.helper.general.DBHelper#setFCMToken(String)}
     * storage.
     *
     * @param fcmToken the FCM token from {@code FirebaseMessaging#getToken()} or
     *                 your messaging delegate; null or
     *                 blank values are ignored
     */
    public void registerFCM(@Nullable String fcmToken) {
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            return;
        }
        DBHelper.getShared().setFCMToken(fcmToken);
    }

    // Get Login URL
    public void getLoginURL(final EventResult<String> callback) {
        LoginController.getShared(context).getLoginURL(callback);
    }

    public void getLoginURL(@Nullable final Map<String, String> extraParams, final EventResult<String> callback) {
        LoginController.getShared(context).getLoginURL(extraParams, callback);
    }

    // Get Registration URL
    public void getRegistrationURL(final EventResult<String> callback) {
        LoginController.getShared(context).getRegistrationURL(callback);
    }

    public void getRegistrationURL(@Nullable final Map<String, String> extraParams,
            final EventResult<String> callback) {
        LoginController.getShared(context).getRegistrationURL(extraParams, callback);
    }

    // Custom Tab
    public void RegisterWithBrowser(@NonNull final Context activityContext, @Nullable final String color,
            final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).registerWithBrowser(activityContext, color, callbacktoMain);
    }

    public void RegisterWithBrowser(@NonNull final Context activityContext, @Nullable final String color,
            @Nullable final Map<String, String> extraParams,
            final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).registerWithBrowser(activityContext, color, extraParams, callbacktoMain);
    }
    // ------------------------------------------------------------------------------------------Local
    // Authentication----------------------------------------

    // Cidaas Set OnActivityEventResult For Handling Device Authentication
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LocalAuthenticationController.getShared(context).onActivityResult(requestCode, resultCode, data);
    }

    // Show the Alert Dilog Which is go to settings
    private void showDialogToSetupLock(final Activity activity, EventResult<LocalAuthenticationEntity> result) {
        LocalAuthenticationController.getShared(context).showDialogToSetupLock(activity, result);
    }

    // Method for Local Authentocation

    public void localAuthentication(final Activity activity, EventResult<LocalAuthenticationEntity> result) {
        LocalAuthenticationController.getShared(context).localAuthentication(activity, result);
    }

    // Method for Local Biometric Authentocation
    @TargetApi(Build.VERSION_CODES.P)
    public void localBiometricAuthentication(final BiometricEntity biometricBuilder, BiometricCallback callback) {
        LocalAuthenticationController.getShared(context).localBiometricAuthenticate(biometricBuilder, callback);
    }

    // ------------------------------------------------------------------------------------------XXXXXXX----------------------------------------

    public static String getSDKVersion() {
        String version = "";
        try {

            version = "(" + BuildConfig.VERSION_NAME + ")";
        } catch (Exception e) {
            return "";
        }

        return version;
    }

    public String getUserAgent() {
        return DBHelper.getShared().getUserAgent();
    }

    // ----------------------------------LocationHistory------------------------------------------------------------------------------------------------------
    // Add Logs
    public void getUserLoginInfo(final UserLoginInfoEntity userLoginInfoEntity,
            final EventResult<UserLoginInfoResponseEntity> result) {
        UserLoginInfoController.getShared(context).getUserLoginInfo(userLoginInfoEntity, result);
    }

    // Ask Ganehs
    public void loginWithSocial(@NonNull final Context activityContext, @NonNull final String requestId,
            @NonNull final String provider,
            @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).loginWithSocial(activityContext, requestId, provider, color, callbacktoMain);
    }

    // Get Social Login URL
    public void getSocialLoginURL(final String requestId, final String provider, final EventResult<String> callback) {
        LoginController.getShared(context).getSocialLoginURL(provider, requestId, callback);
    }

}