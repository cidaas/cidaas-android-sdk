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
    //Extra parameter that is passed in URL
    public static HashMap<String, String> extraParams = new HashMap<>();

    //Create a Shared Instance
    private static Cidaas cidaasInstance;

    public static Cidaas getInstance(Context YourActivitycontext) {
        if (cidaasInstance == null) {
            cidaasInstance = new Cidaas(YourActivitycontext);
        }

        return cidaasInstance;
    }

    //Constructor
    public Cidaas(Context yourActivityContext) {
        this.context = yourActivityContext;
        CidaasHelper.getShared(yourActivityContext).initialiseObject();
    }

    //-----------------------------------------_Common For Cidaas Instances-------------------------------------------------------------------------

    public boolean isENABLE_PKCE() {
        return CidaasHelper.getShared(context).isENABLE_PKCE();
    }

    public void setENABLE_PKCE(boolean ENABLE_PKCE) {
        CidaasHelper.getShared(context).setENABLE_PKCE(ENABLE_PKCE);
    }

    //enableLog

    public boolean isLogEnable() {
        return CidaasHelper.getShared(context).isLogEnable();
    }

    public String enableLog() {
        return CidaasHelper.getShared(context).enableLog();
    }

    public String disableLog() {
        return CidaasHelper.getShared(context).disableLog();
    }

    // ****** LOGIN WITH Document *****-------------------------------------------------------------------------------------------------------
    public void VerifyDocument(final File photo, final String sub, final EventResult<DocumentScannerServiceResultEntity> resultEntityResult) {
        DocumentScannnerController.getShared(context).sendtoServicecall(photo, sub, resultEntityResult);
    }

    //----------------------------------------------------------------------------------------------------------------------------------------

    //@Override
    public void getAccessToken(String sub, EventResult<AccessTokenEntity> result) {
        AccessTokenController.getShared(context).getAccessToken(sub, result);
    }

    public void getAccessTokenFromRefreshToken(String refershtoken, EventResult<AccessTokenEntity> result) {
        AccessTokenController.getShared(context).getAccessTokenByRefreshToken(refershtoken, result);
    }

    public void getAccessTokenBySocial(SocialAccessTokenEntity socialAccessTokenEntity, EventResult<AccessTokenEntity> result) {
        AccessTokenController.getShared(context).getAccessTokenBySocial(socialAccessTokenEntity, result);
    }

    //For Authenticator App
    public void setAccessToken(final AccessTokenEntity accessTokenEntity, final EventResult<LoginCredentialsResponseEntity> result) {
        AccessTokenController.getShared(context).setAccessToken(accessTokenEntity, result);
    }

    //Get userinfo Based on Access Token
    // @Override
    public void getUserInfo(String sub, final EventResult<UserInfoEntity> callback) {
        UserProfileController.getShared(context).getUserProfile(sub, callback);
    }

    //Resume After open App From Broswer
    public void handleToken(String code) {
        LoginController.getShared(context).handleToken(code);
    }

    // Custom Tab
    public void loginWithBrowser(@NonNull final Context activityContext, @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).loginWithBrowser(activityContext, color, callbacktoMain);
    }

    public void loginWithBrowser(@NonNull final Context activityContext, @Nullable final String color, @Nullable final Map<String, String> extraParams,
                                 final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).loginWithBrowser(activityContext, color, extraParams, callbacktoMain);
    }

    public void logoutWithBrowser(@NonNull final Context activityContext,@NonNull final String sub,@Nullable final String post_redirect_uri, @Nullable final String color, final EventResult<Boolean> callbacktoMain) {
        LoginController.getShared(context).logoutWithBrowser(activityContext,sub,post_redirect_uri, color, callbacktoMain);
    }

    //Get Login URL
    public void getLoginURL(final EventResult<String> callback) {
        LoginController.getShared(context).getLoginURL(callback);
    }

    public void getLoginURL(@Nullable final Map<String, String> extraParams, final EventResult<String> callback) {
        LoginController.getShared(context).getLoginURL(extraParams, callback);
    }

    //Get Registration URL
    public void getRegistrationURL(final EventResult<String> callback) {
        LoginController.getShared(context).getRegistrationURL(callback);
    }

    public void getRegistrationURL(@Nullable final Map<String, String> extraParams, final EventResult<String> callback) {
        LoginController.getShared(context).getRegistrationURL(extraParams, callback);
    }

    // Custom Tab
    public void RegisterWithBrowser(@NonNull final Context activityContext, @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).registerWithBrowser(activityContext, color, callbacktoMain);
    }

    public void RegisterWithBrowser(@NonNull final Context activityContext, @Nullable final String color, @Nullable final Map<String, String> extraParams,
                                    final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).registerWithBrowser(activityContext, color, extraParams, callbacktoMain);
    }
    //------------------------------------------------------------------------------------------Local Authentication----------------------------------------

    //Cidaas Set OnActivityEventResult For Handling Device Authentication
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LocalAuthenticationController.getShared(context).onActivityResult(requestCode, resultCode, data);
    }

    //Show the Alert Dilog Which is go to settings
    private void showDialogToSetupLock(final Activity activity, EventResult<LocalAuthenticationEntity> result) {
        LocalAuthenticationController.getShared(context).showDialogToSetupLock(activity, result);
    }

    //Method for Local Authentocation

    public void localAuthentication(final Activity activity, EventResult<LocalAuthenticationEntity> result) {
        LocalAuthenticationController.getShared(context).localAuthentication(activity, result);
    }


    //Method for Local Biometric Authentocation
    @TargetApi(Build.VERSION_CODES.P)
    public void localBiometricAuthentication(final BiometricEntity biometricBuilder, BiometricCallback callback) {
        LocalAuthenticationController.getShared(context).localBiometricAuthenticate(biometricBuilder, callback);
    }

    //------------------------------------------------------------------------------------------XXXXXXX----------------------------------------

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


    //----------------------------------LocationHistory------------------------------------------------------------------------------------------------------
    // Add Logs
    public void getUserLoginInfo(final UserLoginInfoEntity userLoginInfoEntity, final EventResult<UserLoginInfoResponseEntity> result) {
        UserLoginInfoController.getShared(context).getUserLoginInfo(userLoginInfoEntity, result);
    }

    // Ask Ganehs
    public void loginWithSocial(@NonNull final Context activityContext, @NonNull final String requestId, @NonNull final String provider,
                                @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        LoginController.getShared(context).loginWithSocial(activityContext, requestId, provider, color, callbacktoMain);
    }

    //Get Social Login URL
    public void getSocialLoginURL(final String requestId, final String provider, final EventResult<String> callback) {
        LoginController.getShared(context).getSocialLoginURL(provider, requestId, callback);
    }

}