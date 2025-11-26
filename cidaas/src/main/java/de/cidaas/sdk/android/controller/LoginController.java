package de.cidaas.sdk.android.controller;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import de.cidaas.sdk.android.helper.customtab.CustomTabHelper;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.helper.pkce.OAuthChallengeGenerator;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.repository.Login.LoginService;
import timber.log.Timber;

public class LoginController {

    private Context context;
    // public String loginURL;
    //public String DomainURL="";
    public String redirectUrl;
    public EventResult<AccessTokenEntity> logincallback;
    public EventResult<Boolean> logoutcallback;

    public static LoginController shared;

    public LoginController(Context contextFromCidaas) {

        //Set Callback Null;
        logincallback = null;
        logoutcallback = null;
        context = contextFromCidaas;

        //Make Call back null

    }

    public static LoginController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new LoginController(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    String codeVerifier = "";
    String codeChallenge = "";

    // Generate Code Challenge and Code verifier
    public void generateChallenge() {

        OAuthChallengeGenerator generator = new OAuthChallengeGenerator();

        Dictionary<String, String> savedProperties = new Hashtable<>();

        codeVerifier = generator.getCodeVerifier();
        codeChallenge = generator.getCodeChallenge(codeVerifier);

        savedProperties.put("Verifier", codeVerifier);
        savedProperties.put(CidaasConstants.CHALLENGE, codeChallenge);
        savedProperties.put("Method", generator.codeChallengeMethod);

        DBHelper.getShared().addChallengeProperties(savedProperties);
    }

    public void getLogoutURL(@NonNull final String baseurl, @NonNull final  String accessToken,@Nullable String post_logout_redirect_uri ,@NonNull final EventResult<String> callbackResult) {
        final String methodName = "LoginController :getLoginURL()";
        try {

            LoginService.getShared(context).getURLList(baseurl, new EventResult<Object>() {
                @Override
                public void success(Object result) {

                    // Here the Error may occur due to Challange is empty
                    if (accessToken != null && !accessToken.equals("") ) {
                        String LogoutURL="";
                        if (post_logout_redirect_uri != null && !post_logout_redirect_uri.equals("")) {
                            LogoutURL = baseurl + URLHelper.getShared().getLogoutURLForEmbeddedBrowser() + "?access_token_hint=" + accessToken + "&post_logout_redirect_uri=" + post_logout_redirect_uri;

                        } else {
                            LogoutURL = baseurl + URLHelper.getShared().getLogoutURLForEmbeddedBrowser() + "?access_token_hint=" + accessToken;

                        }

                        if (LogoutURL != null && !LogoutURL.equals("")) {
                            callbackResult.success(LogoutURL);
                        } else {
                            callbackResult.failure(WebAuthError.getShared(context).loginURLMissingException(CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        callbackResult.failure(WebAuthError.getShared(context).propertyMissingException("Sub or accesstoken  must not be empty"
                                , CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbackResult.failure(error);
                }
            });
        } catch (Exception e) {
            callbackResult.failure(WebAuthError.getShared(context)
                    .methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.GET_LOGIN_URL_FAILURE, e.getMessage()));
        }
    }



    //Get login URL for Custom browser and Webview
    public void getLoginURL(@NonNull final String baseurl, Dictionary<String, String> loginProperties,
                            final Dictionary<String, String> challengePropertiesfromparam, @NonNull final EventResult<String> callbackResult) {
        final String methodName = "LoginController :getLoginURL()";
        try {

            LoginService.getShared(context).getURLList(baseurl, new EventResult<Object>() {
                @Override
                public void success(Object result) {

                    LinkedHashMap<String, String> urlList = new LinkedHashMap<>();
                    urlList = (LinkedHashMap<String, String>) result;

                    Dictionary<String, String> loginProperties = DBHelper.getShared().getLoginProperties(baseurl);

                    //////////////////This is for testcase purpose
                    Dictionary<String, String> challengeProperties = new Hashtable<>();

                    if (challengePropertiesfromparam == null) {
                        challengeProperties = DBHelper.getShared().getChallengeProperties();
                    } else if (challengePropertiesfromparam != null) {
                        challengeProperties = challengePropertiesfromparam;
                    }

                    if (challengeProperties.size() == 0 || challengeProperties.isEmpty() || challengeProperties.get(CidaasConstants.CHALLENGE) == null) {
                        generateChallenge();
                        challengeProperties = DBHelper.getShared().getChallengeProperties();
                    }


                    String authzURL = urlList.get("authorization_endpoint");
                    String clientId = loginProperties.get(CidaasConstants.CLIENT_ID);
                    String redirectURL = loginProperties.get(CidaasConstants.REDIRECT_URL);
                    String challenge = challengeProperties.get(CidaasConstants.CHALLENGE);

                    // Here the Error may occur due to Challange is empty
                    if (clientId != null && !clientId.equals("") && redirectURL != null && !redirectURL.equals("") && challenge != null && !challenge.equals("")) {

                        String finalURL = URLHelper.getShared().constructLoginURL(authzURL, clientId, redirectURL, challenge, "login");
                        if (finalURL != null && !finalURL.equals("")) {
                            callbackResult.success(finalURL);
                        } else {
                            callbackResult.failure(WebAuthError.getShared(context).loginURLMissingException(CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        callbackResult.failure(WebAuthError.getShared(context).propertyMissingException("ClientId or RedirectURL or Challenge must not be empty"
                                , CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbackResult.failure(error);
                }
            });
        } catch (Exception e) {
            callbackResult.failure(WebAuthError.getShared(context)
                    .methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.GET_LOGIN_URL_FAILURE, e.getMessage()));
        }
    }


    //Get login URL for Custom browser
    public void getSocialLoginURL(final String provider, final String requestId, @NonNull final EventResult<String> callbackResult) {
        final String methodName = "LoginController :getSocialLoginURL()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {
                    if (provider != null && !provider.equals("") && requestId != null && !requestId.equals("")) {
                        String finalURL = URLHelper.getShared().constructSocialURL(result.get(CidaasConstants.DOMAIN_URL), provider, requestId);

                        if (finalURL != null && !finalURL.equals("")) {
                            callbackResult.success(finalURL);
                        } else {
                            callbackResult.failure(WebAuthError.getShared(context).loginURLMissingException("Error " + methodName));
                        }

                    } else {
                        callbackResult.failure(WebAuthError.getShared(context).customException(WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE,
                                "BaseURL or provider or requestid must not be null", "Error " + methodName));
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbackResult.failure(WebAuthError.getShared(context).cidaasPropertyMissingException("", "Error" + methodName));
                }
            });

        } catch (Exception e) {
            callbackResult.failure(WebAuthError.getShared(context).methodException("Exception : " + methodName, WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE,
                    e.getMessage()));
        }
    }

    public void logoutWithBrowser(@NonNull final Context activityContext,@NonNull final String sub, @Nullable final String post_redirect_uri, @Nullable final String color, final EventResult<Boolean> callbacktoMain) {
        final String methodName = "LoginController :logoutWithBrowser()";
        try {

            if(sub!=null && sub!="") {
                AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
                            @Override
                            public void success(AccessTokenEntity result) {
                                getLogoutURL(result.getAccess_token(),post_redirect_uri, new EventResult<String>() {
                                    @Override
                                    public void success(String result) {
                                        String logoutURL = result;

                                        logoutcallback = callbacktoMain;
                                        if (logoutURL != null) {
                                            launchCustomTab(activityContext, logoutURL, color);
                                        } else {
                                            // callback Failure
                                            callbacktoMain.failure(WebAuthError.getShared(context).loginWithBrowserFailureException(WebAuthErrorCode.LOGOUTWITH_BROWSER_FAILURE,
                                                    "EMPTY URL", methodName));
                                            // Clear callback after failure
                                            logoutcallback = null;
                                        }
                                    }

                                    @Override
                                    public void failure(WebAuthError error) {
                                        callbacktoMain.failure(error);
                                        // Clear callback after failure
                                        logoutcallback = null;
                                    }
                                });

                            }

                            @Override
                            public void failure(WebAuthError error) {
                                callbacktoMain.failure(error);
                                // Clear callback after failure
                                logoutcallback = null;
                            }
                        });

            }else{
                callbacktoMain.failure(WebAuthError.getShared(context).loginWithBrowserFailureException(WebAuthErrorCode.LOGOUTWITH_BROWSER_FAILURE,
                        "EMPTY SUB", methodName));
                // Clear callback after failure
                logoutcallback = null;
            }


        } catch (Exception e) {
            // : Handle Exception

            callbacktoMain.failure(WebAuthError.getShared(context)
                    .methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE, e.getMessage()));
            // Clear callback after exception
            logoutcallback = null;
        }


    }

    //Get Login With Browser
    public void loginWithBrowser(@NonNull final Context activityContext, @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        final String methodName = "LoginController :loginWithBrowser()";
        try {

            getLoginURL(new EventResult<String>() {
                @Override
                public void success(String result) {
                    String loginURL = result;
                    logincallback = callbacktoMain;
                    if (loginURL != null) {
                        launchCustomTab(activityContext, loginURL, color);
                    } else {
                        // callback Failure
                        callbacktoMain.failure(WebAuthError.getShared(context).loginWithBrowserFailureException(WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE,
                                "EMPTY URL", methodName));
                        // Clear callback after failure
                        logincallback = null;
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbacktoMain.failure(error);
                    // Clear callback after failure
                    logincallback = null;
                }
            });


        } catch (Exception e) {
            // : Handle Exception

            callbacktoMain.failure(WebAuthError.getShared(context)
                    .methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE, e.getMessage()));
            // Clear callback after exception
            logincallback = null;
        }


    }

    private void launchCustomTab(@NonNull Context activityContext, @NonNull String url, @Nullable String color) {
        final String methodName = "LoginController :launchCustomTab()";
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);//TO show title

            //  CustomTabsClient.getPackageName()
            builder.setStartAnimations(activityContext, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            if (color != null) {

                builder.setToolbarColor(Color.parseColor(color));
            }

            CustomTabsIntent customTabsIntent = builder.build();

            String packageName = CustomTabHelper.getShared().getPackageNameToUse(context);

            if (packageName != null && !packageName.equals("")) {
                customTabsIntent.intent.setPackage(packageName);
            }
            customTabsIntent.launchUrl(activityContext, Uri.parse(url));
        } catch (Exception e) {
            WebAuthError.getShared(context).methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SAVE_LOGIN_PROPERTIES, e.getMessage());
        }
    }

   /* public void loginWithSocial(@NonNull final Context activityContext, @NonNull final String requestId, @NonNull final String provider,
                                @Nullable final String color, finalEventResult<AccessTokenEntity> callbacktoMain)
    {
      final String methodName="LoginController :loginWithSocial()";
      try
      {
         getSocialLoginURL(requestId, provider, new EventResult<String>() {
                        @Override
                        public void success(String socialLoginURL) {
                            logincallback = callbacktoMain;
                            if (socialLoginURL != null) {
                               launchCustomTab(activityContext,socialLoginURL, color);
                            } else {
                               callbacktoMain.failure(WebAuthError.getShared(context).loginWithBrowserFailureException(WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE,
                                       "EMPTY SOCIAL URL","Error"+methodName));
                            }
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callbacktoMain.failure(error);
                        }
                    });

      }
      catch (Exception e)
      {
   callbacktoMain.failure( WebAuthError.getShared(context).methodException("Exception : "+methodName,WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE,e.getMessage()));
      }

    }*/

    //Private Methods
    private String getCodeFromUrl(String url) {
        try {
            String code = null;
            if (url.contains("code=")) {
                String[] codeComponents = url.split("code=");
                if (codeComponents.length > 1) {
                    codeComponents = codeComponents[1].split("&");
                    if (codeComponents.length > 0) {
                        code = codeComponents[0];
                        return code;
                    } else {
                        return code;
                    }
                }
            }
            return code;
        } catch (Exception e) {
            if (logincallback != null) {
                logincallback.failure(WebAuthError.getShared(context).methodException("Exception :LoginController :getCodeFromUrl() ",
                        WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE, e.getMessage()));
                // Clear callback after failure
                logincallback = null;
            }
            return null;
        }
    }


    public void handleToken(String code) {   /*,Result<AccessTokenEntity> callbacktoMain*/

        if (logincallback != null) {
            getLoginCode(code, logincallback);
        }
        if(logoutcallback != null){
            logoutcallback.success(true);
            // Clear both callbacks after logout callback is invoked
            logoutcallback = null;
            logincallback = null;
        }
    }

    // Belongs to Core
    public void getLoginCode(String url, EventResult<AccessTokenEntity> callback) {
        try {
            // showLoader();
            String code = getCodeFromUrl(url);
            if (code != null) {
                //  hideLoader();

                // Wrap callback to clear logincallback after invocation
                final EventResult<AccessTokenEntity> wrappedCallback = new EventResult<AccessTokenEntity>() {
                    @Override
                    public void success(AccessTokenEntity result) {
                        callback.success(result);
                        // Clear callback after successful invocation
                        logincallback = null;
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        callback.failure(error);
                        // Clear callback after failure invocation
                        logincallback = null;
                    }
                };

                AccessTokenController.getShared(context).getAccessTokenByCode(code, wrappedCallback);
            } else {
                // hideLoader();
                String loggerMessage = "Request-Id params to dictionary conversion failure : " + "Error Code - ";
                //+error.errorCode + ", Error Message - " + error.getErrorMessage() + ", Status Code - " +  error.statusCode;
                LogFile.getShared(context).addFailureLog(loggerMessage);
                // Clear callback when code is null
                logincallback = null;
            }
        } catch (Exception e) {

            callback.failure(WebAuthError.getShared(context)
                    .methodException("Exception :LoginController :getCodeFromUrl ", WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE, e.getMessage()));

            // Clear callback after exception
            logincallback = null;
            // handle Exception
        }
    }

    //Get Login URL without any Argument
    public void getLoginURL(final EventResult<String> callback) {
        try {
            //Check requestId is not null
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {

                    //This is to generate the Code Challenge

                    /////This is for test Case purpose
                    Dictionary<String, String> challengeProperties = DBHelper.getShared().getChallengeProperties();

                    if (challengeProperties.size() == 0 || challengeProperties == null || challengeProperties.isEmpty()) {
                        generateChallenge();
                        challengeProperties = DBHelper.getShared().getChallengeProperties();
                    }


                    getLoginURL(CidaasHelper.baseurl, result, challengeProperties, new EventResult<String>() {
                        @Override
                        public void success(String result) {

                            callback.success(result);
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callback.failure(error);
                        }
                    });
                }

                @Override
                public void failure(WebAuthError error) {
                    callback.failure(error);
                }
            });


        } catch (Exception e) {
            // Handle Error
            callback.failure(WebAuthError.getShared(context)
                    .methodException("Exception :LoginController :getLoginURL() ", WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE, e.getMessage()));
        }
    }

    //get Logout url
    public void getLogoutURL(final String accessToken,final String post_redirect_uri,  final EventResult<String> callback) {
        try {
            //Check requestId is not null
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result){
                    getLogoutURL(CidaasHelper.baseurl, accessToken,post_redirect_uri, new EventResult<String>() {
                        @Override
                        public void success(String result) {

                            callback.success(result);
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callback.failure(error);
                        }
                    });
                }

                @Override
                public void failure(WebAuthError error) {
                    callback.failure(error);
                }
            });


        } catch (Exception e) {
            // Handle Error
            callback.failure(WebAuthError.getShared(context)
                    .methodException("Exception :LoginController :getLogoutURL() ", WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE, e.getMessage()));
        }
    }

    public void setURL(@NonNull final Dictionary<String, String> loginproperties, EventResult<String> result, String methodNameFromApp) {
        String methodName = "LoginController:setURL()";
        try {
            if (loginproperties != null) {


                if (DBHelper.getShared().addLoginProperties(loginproperties)) {
                    CidaasHelper.baseurl = loginproperties.get(CidaasConstants.DOMAIN_URL);
                    CidaasHelper.IS_SETURL_CALLED = true;
                    result.success("SetURL is Successfully configured " + methodNameFromApp);
                    LogFile.getShared(context).addSuccessLog(methodName, "SetURL is Successfully configured Baseurl:-" + loginproperties.get(CidaasConstants.DOMAIN_URL)
                            + " Method Name:- " + methodNameFromApp);
                    LogFile.getShared(context).addInfoLog(methodName, "SetURL is Successfully configured Baseurl:-" + loginproperties.get(CidaasConstants.DOMAIN_URL)
                            + " Method Name:- " + methodNameFromApp);
                } else {
                    String errorMessage = "Saving Failed in LocalDB";
                    result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException(errorMessage, methodName));
                }

            } else {
                String errorMessage = "Login properties in null";
                result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException(errorMessage, methodName));
            }
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(methodName, WebAuthErrorCode.CIDAAS_PROPERTY_MISSING, e.getMessage()));
        }
    }


    //Get Login URL without any Argument
    public void getRegistrationURL(final EventResult<String> callback) {
        try {
            //Check requestId is not null
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {

                    //This is to generate the Code Challenge

                    /////This is for test Case purpose
                    Dictionary<String, String> challengeProperties = DBHelper.getShared().getChallengeProperties();

                    if (challengeProperties.size() == 0 || challengeProperties == null || challengeProperties.isEmpty()) {
                        generateChallenge();
                        challengeProperties = DBHelper.getShared().getChallengeProperties();
                    }


                    getRegistrationURL(CidaasHelper.baseurl, result, challengeProperties, new EventResult<String>() {
                        @Override
                        public void success(String result) {

                            callback.success(result);
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callback.failure(error);
                        }
                    });
                }

                @Override
                public void failure(WebAuthError error) {
                    callback.failure(error);
                }
            });


        } catch (Exception e) {
            // Handle Error
            callback.failure(WebAuthError.getShared(context)
                    .methodException("Exception :LoginController :getLoginURL() ", WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE, e.getMessage()));
        }
    }

    //Get Registration URL for Custom browser and Webview
    public void getRegistrationURL(@NonNull final String baseurl, Dictionary<String, String> loginProperties,
                                   final Dictionary<String, String> challengePropertiesfromparam, @NonNull final EventResult<String> callbackResult) {
        final String methodName = "LoginController :getRegistrationURL()";
        try {

            LoginService.getShared(context).getURLList(baseurl, new EventResult<Object>() {
                @Override
                public void success(Object result) {

                    LinkedHashMap<String, String> urlList = new LinkedHashMap<>();
                    urlList = (LinkedHashMap<String, String>) result;

                    Dictionary<String, String> loginProperties = DBHelper.getShared().getLoginProperties(baseurl);

                    //////////////////This is for testcase purpose
                    Dictionary<String, String> challengeProperties = new Hashtable<>();

                    if (challengePropertiesfromparam == null) {
                        challengeProperties = DBHelper.getShared().getChallengeProperties();
                    } else if (challengePropertiesfromparam != null) {
                        challengeProperties = challengePropertiesfromparam;
                    }

                    if (challengeProperties.size() == 0 || challengeProperties.isEmpty() || challengeProperties.get(CidaasConstants.CHALLENGE) == null || challengeProperties.get(CidaasConstants.CHALLENGE).equals("")) {
                        generateChallenge();
                        challengeProperties = DBHelper.getShared().getChallengeProperties();
                    }


                    String authzURL = urlList.get("authorization_endpoint");
                    String clientId = loginProperties.get(CidaasConstants.CLIENT_ID);
                    String redirectURL = loginProperties.get(CidaasConstants.REDIRECT_URL);
                    String challenge = challengeProperties.get(CidaasConstants.CHALLENGE);

                    // Here the Error may occur due to Challange is empty
                    if (clientId != null && !clientId.equals("") && redirectURL != null && !redirectURL.equals("") && challenge != null && !challenge.equals("")) {

                        String finalURL = URLHelper.getShared().constructLoginURL(authzURL, clientId, redirectURL, challenge, "register");
                        if (finalURL != null && !finalURL.equals("")) {
                            callbackResult.success(finalURL);
                        } else {
                            callbackResult.failure(WebAuthError.getShared(context).loginURLMissingException(CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        callbackResult.failure(WebAuthError.getShared(context).propertyMissingException("ClientId or RedirectURL or Challenge must not be empty"
                                , CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbackResult.failure(error);
                }
            });
        } catch (Exception e) {
            callbackResult.failure(WebAuthError.getShared(context)
                    .methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.GET_LOGIN_URL_FAILURE, e.getMessage()));
        }
    }


    //Get Register With Browser
    public void registerWithBrowser(@NonNull final Context activityContext, @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        final String methodName = "LoginController :loginWithBrowser()";
        try {

            getRegistrationURL(new EventResult<String>() {
                @Override
                public void success(String result) {
                    String registrationURL = result;
                    logincallback = callbacktoMain;
                    if (registrationURL != null) {
                        launchCustomTab(activityContext, registrationURL, color);
                    } else {
                        // callback Failure
                        callbacktoMain.failure(WebAuthError.getShared(context).loginWithBrowserFailureException(WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE,
                                "EMPTY URL", methodName));
                        // Clear callback after failure
                        logincallback = null;
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbacktoMain.failure(error);
                    // Clear callback after failure
                    logincallback = null;
                }
            });


        } catch (Exception e) {
            // : Handle Exception

            callbacktoMain.failure(WebAuthError.getShared(context)
                    .methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE, e.getMessage()));
            // Clear callback after exception
            logincallback = null;
        }


    }

    public void loginWithSocial(@NonNull final Context activityContext, @NonNull final String requestId, @NonNull final String provider,
                                @Nullable final String color, final EventResult<AccessTokenEntity> callbacktoMain) {
        final String methodName = "LoginController :loginWithSocial()";
        try {
            getSocialLoginURL(provider, requestId, new EventResult<String>() {
                @Override
                public void success(String socialLoginURL) {
                    logincallback = callbacktoMain;
                    if (socialLoginURL != null) {
                        launchCustomTab(activityContext, socialLoginURL, color);
                    } else {
                        callbacktoMain.failure(WebAuthError.getShared(context).loginWithBrowserFailureException(WebAuthErrorCode.LOGINWITH_BROWSER_FAILURE,
                                "EMPTY SOCIAL URL", "Error" + methodName));
                        // Clear callback after failure
                        logincallback = null;
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    callbacktoMain.failure(error);
                    // Clear callback after failure
                    logincallback = null;
                }
            });

        } catch (Exception e) {
            callbacktoMain.failure(WebAuthError.getShared(context).methodException("Exception : " + methodName, WebAuthErrorCode.GET_SOCIAL_LOGIN_URL_FAILURE, e.getMessage()));
            // Clear callback after exception
            logincallback = null;
        }

    }

}
