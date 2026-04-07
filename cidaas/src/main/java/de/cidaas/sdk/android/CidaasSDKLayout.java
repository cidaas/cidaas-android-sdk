package de.cidaas.sdk.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.controller.LoginController;
import de.cidaas.sdk.android.controller.LogoutController;
import de.cidaas.sdk.android.entities.SocialAccessTokenEntity;
import de.cidaas.sdk.android.helper.CidaasSDKHelper;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.general.FileHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.interfaces.ICidaasFacebook;
import de.cidaas.sdk.android.interfaces.ICidaasGoogle;
import de.cidaas.sdk.android.interfaces.ILoader;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import timber.log.Timber;

public class CidaasSDKLayout extends RelativeLayout {


    private static CidaasSDKLayout cidaasInstance;

    private static Context GLOBAL_CONTEXT;
    private static Activity GLOBAL_ACTIVITY;

    public boolean isAlreadyEvaluated = false;
    public static boolean ENABLE_LOG = false;
    public boolean ENABLE_PKCE = false;
    public boolean ENABLE_NATIVE_FACEBOOK = false;
    public boolean ENABLE_NATIVE_GOOGLE = false;


    public WebAuthError webAuthError = null;
    public String DomainURL = "";


    private static String GLOBAL_CODE_VERIFIER = "";
    private static String GLOBAL_INITIAL_CODE_VERIFIER = "";
    private static String GLOBAL_PRE_AUTH_CODE = "";


    public WebView webViewInstance;
    private TextView textViewInstance;
    private ImageView imageViewInstance;
    private Button buttonInstance;


    public static ICidaasFacebook iCidaasFacebook;
    public static ICidaasGoogle iCidaasGoogle;


    public static ILoader loader;
    private static boolean displayLoader = false;


    public EventResult<AccessTokenEntity> logincallback;


    Dictionary<String, String> loginProperties = new Hashtable<>();

    public static CidaasSDKLayout getInstance(Context YourActivitycontext) {
        if (cidaasInstance == null) {
            cidaasInstance = new CidaasSDKLayout(YourActivitycontext);
        }

        return cidaasInstance;
    }

    public CidaasSDKLayout(Context context) {
        super(context);
        init(context);
    }


    private void init(Context context) {
        GLOBAL_CONTEXT = context;
        DBHelper.setConfig(context);

        CidaasHelper.baseurl = DomainURL;


        saveLoginProperties(new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(Dictionary<String, String> loginResult) {

                loginProperties = loginResult;
                DomainURL = loginResult.get("DomainURL");

                checkPKCEFlow(loginResult, new EventResult<Dictionary<String, String>>() {
                    @Override
                    public void success(Dictionary<String, String> result) {
                        DBHelper.getShared().addLoginProperties(result);
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        String loggerMessage = CidaasConstants.CIDAAS_CONSTRUCTOR_FAILURE + CidaasConstants.ERROR_CODE
                                + error.getErrorCode() + ", Error Message - " + error.getErrorMessage() + CidaasConstants.STATUS_CODE + error.getStatusCode()
                                + "CidaasSDKLayout:init()";
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(loggerMessage);

                    }
                });
            }

            @Override
            public void failure(WebAuthError error) {
                String loggerMessage = CidaasConstants.CIDAAS_CONSTRUCTOR_FAILURE + CidaasConstants.ERROR_CODE
                        + error.getErrorCode() + CidaasConstants.ERROR_MESSAGE + error.getErrorMessage() + CidaasConstants.STATUS_CODE + error.getStatusCode();
                LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(loggerMessage);
            }
        });
    }


    //Get LoginCode
    //NOSONAR
    private void getLoginCode(final String urlFromCode) {
        final String methodName = "cidaasSDKLayout:getLoginCode()";
        try {


            checkSavedProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {

                    String url = urlFromCode;


                    if (url.contains(CidaasConstants.CODE_EQUAL)) {

                        if (url.contains(result.get(CidaasConstants.REDIRECT_URL)) && !isAlreadyEvaluated) {

                            isAlreadyEvaluated = true;

                            // show loader
                            showLoader();


                            // replace redirect url with empty string
                            url = url.replace(result.get(CidaasConstants.REDIRECT_URL), "");
                            String[] stringComponents = url.split(CidaasConstants.CODE_EQUAL);
                            if (stringComponents.length > 1) {
                                stringComponents = stringComponents[1].split("&");
                                if (stringComponents.length > 0) {
                                    CidaasSDKLayout.GLOBAL_INITIAL_CODE_VERIFIER = CidaasSDKLayout.GLOBAL_CODE_VERIFIER;
                                    AccessTokenController.getShared(GLOBAL_CONTEXT).getAccessTokenByCode(stringComponents[0], new EventResult<AccessTokenEntity>() {
                                        @Override
                                        public void success(AccessTokenEntity result) {
                                            hideLoader();

                                            if (logincallback != null) {
                                                logincallback.success(result);
                                            } else {
                                                Timber.d("Login Call back must not be empty");
                                            }

                                        }

                                        @Override
                                        public void failure(WebAuthError error) {
                                            hideLoader();
                                            if (logincallback != null) {
                                                logincallback.failure(error);
                                            } else {
                                                Timber.d("Login Call back must not be empty");
                                            }

                                        }
                                    });
                                    String loggerMessage = "Success Login Code";
                                    LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(loggerMessage);
                                } else {
                                    hideLoader();
                                    WebAuthError.getShared(GLOBAL_CONTEXT).customException(400, "Invlaid URL", CidaasConstants.ERROR_LOGGING_PREFIX + methodName);

                                    LogFile.getShared(GLOBAL_CONTEXT).addFailureLog("Invalid URL");
                                }
                            } else {
                                hideLoader();
                                WebAuthError.getShared(GLOBAL_CONTEXT).customException(400, "Invlaid URL", CidaasConstants.ERROR_LOGGING_PREFIX + methodName);
                                LogFile.getShared(GLOBAL_CONTEXT).addFailureLog("Invalid URL");
                            }
                        } else {
                            String removable_string = result.get(CidaasConstants.DOMAIN_URL) + "/user-ui/#/login?code=";
                            String pre_string = url.replace(removable_string, "");
                            String[] com = pre_string.split("&");
                            if (com.length > 0) {
                                String pre_auth_code = com[0];
                                if (!"".equals(CidaasSDKLayout.GLOBAL_PRE_AUTH_CODE)) {
                                    CidaasSDKLayout.GLOBAL_PRE_AUTH_CODE = pre_auth_code;
                                }
                                CidaasSDKLayout.GLOBAL_INITIAL_CODE_VERIFIER = CidaasSDKLayout.GLOBAL_CODE_VERIFIER;
                            }
                        }
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(error.getErrorMessage());
                }
            });


        } catch (Exception e) {
            String loggerMessage = CidaasConstants.CIDAAS_CONSTRUCTOR_FAILURE + CidaasConstants.ERROR_CODE
                    + e.getLocalizedMessage() + CidaasConstants.ERROR_MESSAGE + e.getMessage() + CidaasConstants.STATUS_CODE + e.getCause();
            Timber.e(e.getMessage());
            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(loggerMessage);
        }
    }


    //Get Login URL without any Argument
    public void getLoginURL(final EventResult<String> callback) {
        getLoginURL(null, callback);
    }

    public void getLoginURL(@Nullable final Map<String, String> extraParams, final EventResult<String> callback) {
        try {
            //Check requestId is not null

            checkSavedProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {

                    loginProperties = result;

                    //This is to generate the Code Challenge

                    LoginController.getShared(GLOBAL_CONTEXT).getLoginURL(DomainURL, result, null, extraParams, new EventResult<String>() {
                        @Override
                        public void success(String result) {

                            callback.success(result);
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callback.failure(error);
                            String loggerMessage = "Login URL service failure : " + CidaasConstants.ERROR_CODE
                                    + error.getErrorCode() + CidaasConstants.ERROR_MESSAGE + error.getErrorMessage() + CidaasConstants.STATUS_CODE + error.getStatusCode();
                            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(loggerMessage);
                        }
                    });
                }

                @Override
                public void failure(WebAuthError error) {
                    callback.failure(error);
                }
            });


        } catch (Exception ex) {
            // Handle Error
            Timber.d(ex.getMessage());
        }
    }


    class CidaasWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {
            try {

                if (url.startsWith("de.cidaas://")) {

                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    // The following flags launch the app outside the current app
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    GLOBAL_CONTEXT.startActivity(intent);

                    return true;
                }

                return false;
            } catch (Exception e) {
                LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(e.getMessage());
                return false;
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("URL", url);

            if (url.contains(CidaasConstants.CODE_EQUAL)) {
                getLoginCode(url);

            }

            if (ENABLE_NATIVE_FACEBOOK) {

                if (url.contains("social/login/facebook")) {

                    // hide loader
                    hideLoader();
                    webViewInstance.setVisibility(View.GONE);
                    webViewInstance.stopLoading();
                    facebookSDKFlow();
                    return;
                }
                if (url.contains("social/register/facebook")) {

                    // hide loader
                    hideLoader();
                    webViewInstance.setVisibility(View.GONE);
                    webViewInstance.stopLoading();
                    facebookSDKFlow();
                    return;
                }
            }

            if (ENABLE_NATIVE_GOOGLE) {
                if (url.contains("social/login/google")) {

                    // hide loader
                    hideLoader();
                    webViewInstance.setVisibility(View.GONE);
                    webViewInstance.stopLoading();
                    googleSDKFlow();
                    return;
                }
                if (url.contains("social/register/google")) {

                    // hide loader
                    hideLoader();
                    webViewInstance.setVisibility(View.GONE);
                    webViewInstance.stopLoading();
                    googleSDKFlow();
                    return;
                }
            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showLoader();
            Log.d("Fb", url);

            Timber.i("FacebookURL" + url);


            if (url.contains(loginProperties.get(CidaasConstants.REDIRECT_URL))) {
                view.setVisibility(GONE);
            } else {
                view.setVisibility(VISIBLE);
            }

            if (url.contains(CidaasConstants.CODE_EQUAL)) {
                getLoginCode(url);

            }
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            imageViewInstance.setVisibility(GONE);
            textViewInstance.setVisibility(GONE);
            buttonInstance.setVisibility(GONE);

            if (url.contains("requestId")) {
                Uri uri = Uri.parse(url);
                CidaasSDKLayout.GLOBAL_PRE_AUTH_CODE = uri.getQueryParameter("requestId");
            }

            // hide loader
            hideLoader();

            if (url.contains(CidaasConstants.CODE_EQUAL)) {
                getLoginCode(url);

            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    private void googleSDKFlow() {
        try {
            showLoader();
            iCidaasGoogle.login(new EventResult<AccessTokenEntity>() {
                @Override
                public void success(AccessTokenEntity result) {
                    hideLoader();
                    if (logincallback != null) {
                        logincallback.success(result);
                    } else {
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(CidaasConstants.CALLBACK_MUST_NOT_BE_EMPTY);

                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    hideLoader();
                    if (logincallback != null) {
                        logincallback.failure(error);
                    } else {
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(CidaasConstants.CALLBACK_MUST_NOT_BE_EMPTY);

                    }
                    LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(error.getErrorMessage());
                }

            });

        } catch (Exception e) {
            hideLoader();
            if (logincallback != null) {
                logincallback.failure(WebAuthError.getShared(GLOBAL_CONTEXT).methodException("Exception: CidaasSDKLayout: googleksdkflow():- ", 417, e.getMessage()));
            } else {
                LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(CidaasConstants.CALLBACK_MUST_NOT_BE_EMPTY);

            }
            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog("Exception: CidaasSDKLayout: googlesdkflow():- " + e.getMessage());
        }
    }


    private void facebookSDKFlow() {
        try {
            showLoader();


            iCidaasFacebook.login(new EventResult<AccessTokenEntity>() {
                @Override
                public void success(AccessTokenEntity result) {
                    hideLoader();
                    if (logincallback != null) {
                        logincallback.success(result);
                    } else {
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(CidaasConstants.CALLBACK_MUST_NOT_BE_EMPTY);

                    }

                }

                @Override
                public void failure(WebAuthError error) {
                    hideLoader();
                    if (logincallback != null) {
                        logincallback.failure(error);
                    } else {
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(CidaasConstants.CALLBACK_MUST_NOT_BE_EMPTY);

                    }
                    LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(error.getErrorMessage());
                }
            });

        } catch (Exception e) {
            hideLoader();
            if (logincallback != null) {
                logincallback.failure(WebAuthError.getShared(GLOBAL_CONTEXT).methodException("Exception: CidaasSDKLayout: facebooksdkflow():- ", 417, e.getMessage()));
            } else {
                LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(CidaasConstants.CALLBACK_MUST_NOT_BE_EMPTY);

            }
            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog("Exception: CidaasSDKLayout: facebooksdkflow():- " + e.getMessage());
        }
    }


    // Get Login
    private void login(@Nullable final Map<String, String> extraParams) {
        try {

            getLoginURL(extraParams, new EventResult<String>() {
                @Override
                public void success(String result) {
                    String url = result;
                    webViewInstance.loadUrl(url);
                    WebSettings settings = webViewInstance.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setAllowFileAccess(true);
                    settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    }
                    settings.setUserAgentString("\"Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\"");
                    webViewInstance.setWebViewClient(new CidaasWebViewClient());
                    LogFile.getShared(GLOBAL_CONTEXT).addFailureLog("Get Login");
                }

                @Override
                public void failure(WebAuthError error) {
                    logincallback.failure(error);
                }
            });


        } catch (Exception e) {
            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(e.getMessage());
        }
    }


    //Login with WEBVIEW
    public void loginWithEmbeddedBrowser(final RelativeLayout relativeLayout, final EventResult<AccessTokenEntity> loginResultcallback) {
        loginWithEmbeddedBrowser(relativeLayout, null, loginResultcallback);
    }

    public void loginWithEmbeddedBrowser(final RelativeLayout relativeLayout, @Nullable final Map<String, String> extraParams,
                                         final EventResult<AccessTokenEntity> loginResultcallback) {
        try {

            webViewInstance = getWebViewInstance();
            textViewInstance = getTextViewInstance();
            buttonInstance = getButtonInstance();
            imageViewInstance = getImageViewInstance();

            RelativeLayout.LayoutParams image, textView, button, webView;

            webView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            image = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            image.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            image.addRule(RelativeLayout.CENTER_HORIZONTAL);
            image.topMargin = 100;

            textView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.addRule(RelativeLayout.CENTER_IN_PARENT);

            button = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            button.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            button.addRule(RelativeLayout.CENTER_HORIZONTAL);
            button.bottomMargin = 100;


            relativeLayout.removeAllViews();

            relativeLayout.addView(webViewInstance, webView);
            relativeLayout.addView(imageViewInstance, image);
            relativeLayout.addView(textViewInstance, textView);
            relativeLayout.addView(buttonInstance, button);

            logincallback = loginResultcallback;


            checkSavedProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> lpresult) {


                    if (!CidaasSDKHelper.isInternetAvailable(GLOBAL_CONTEXT)) {
                        imageViewInstance.setImageDrawable(GLOBAL_CONTEXT.getResources().getDrawable(R.drawable.no_internet_bg));
                        webViewInstance.setVisibility(GONE);
                        imageViewInstance.setVisibility(VISIBLE);
                        textViewInstance.setVisibility(GONE);
                        buttonInstance.setText("Retry");
                        buttonInstance.setTextColor(getResources().getColor(R.color.colorWhite));
                        buttonInstance.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                        buttonInstance.setVisibility(VISIBLE);
                        buttonInstance.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!CidaasSDKHelper.isInternetAvailable(GLOBAL_CONTEXT)) {
                                    GLOBAL_CONTEXT.startActivity(new Intent(Settings.ACTION_SETTINGS));
                                } else {
                                    relativeLayout.removeAllViews();
                                    loginWithEmbeddedBrowser(relativeLayout, extraParams, logincallback);
                                }
                            }
                        });
                    } else if ("".equals(lpresult.get(CidaasConstants.DOMAIN_URL))) {
                        imageViewInstance.setImageDrawable(GLOBAL_CONTEXT.getResources().getDrawable(R.drawable.settings));
                        textViewInstance.setText("AuthorizationURL is missing");
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(textViewInstance.getText().toString());
                        webViewInstance.setVisibility(GONE);
                        imageViewInstance.setVisibility(VISIBLE);
                        textViewInstance.setVisibility(VISIBLE);
                        buttonInstance.setVisibility(GONE);
                    } else if ("".equals(lpresult.get(CidaasConstants.REDIRECT_URL))) {
                        imageViewInstance.setImageDrawable(GLOBAL_CONTEXT.getResources().getDrawable(R.drawable.settings));
                        textViewInstance.setText("RedirectURI is missing");
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(textViewInstance.getText().toString());
                        webViewInstance.setVisibility(GONE);
                        imageViewInstance.setVisibility(VISIBLE);
                        textViewInstance.setVisibility(VISIBLE);
                        buttonInstance.setVisibility(GONE);
                    } else if ("".equals(lpresult.get(CidaasConstants.CLIENT_ID) )) {
                        imageViewInstance.setImageDrawable(GLOBAL_CONTEXT.getResources().getDrawable(R.drawable.settings));
                        textViewInstance.setText("Client Id is missing");
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(textViewInstance.getText().toString());
                        webViewInstance.setVisibility(GONE);
                        imageViewInstance.setVisibility(VISIBLE);
                        textViewInstance.setVisibility(VISIBLE);
                        buttonInstance.setVisibility(GONE);
                    } else {
                        webViewInstance.setVisibility(VISIBLE);
                        imageViewInstance.setVisibility(GONE);
                        textViewInstance.setVisibility(GONE);
                        buttonInstance.setVisibility(GONE);
                        login(extraParams);
                        LogFile.getShared(GLOBAL_CONTEXT).addFailureLog("Login loaded Sucessfully");
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    loginResultcallback.failure(error);
                }
            });


        } catch (Exception e) {
            loginResultcallback.failure(WebAuthError.getShared(GLOBAL_CONTEXT).methodException("Exception: CidaasSDKLayout: WebAuthError", 400, "" + e.getMessage()));
        }
    }


    //GetAccessToken bySocial
    public void getAccessTokenBySocialWithLoader(final String token, final String provider, String DomainUrl, final String viewType, final EventResult<AccessTokenEntity> accessTokenCallback, final HashMap<String, String>... extraParams) {
        showLoader();

        SocialAccessTokenEntity socialAccessTokenEntity = new SocialAccessTokenEntity();
        socialAccessTokenEntity.setToken(token);
        socialAccessTokenEntity.setDomainURL(DomainURL);
        socialAccessTokenEntity.setProvider(provider);
        socialAccessTokenEntity.setViewType(viewType);

        Cidaas.getInstance(GLOBAL_ACTIVITY).getAccessTokenBySocial(socialAccessTokenEntity, new EventResult<AccessTokenEntity>() {
            @Override
            public void success(AccessTokenEntity result) {
                hideLoader();
                accessTokenCallback.success(result);
            }

            @Override
            public void failure(WebAuthError error) {
                hideLoader();
                accessTokenCallback.failure(error);
            }
        });
    }


    //Instances for webview
    private WebView getWebViewInstance() {
        if (webViewInstance == null) {
            webViewInstance = new WebView(GLOBAL_CONTEXT);
        }
        return webViewInstance;
    }

    private TextView getTextViewInstance() {
        if (textViewInstance == null) {
            textViewInstance = new TextView(GLOBAL_CONTEXT);
        }
        return textViewInstance;
    }

    private ImageView getImageViewInstance() {
        if (imageViewInstance == null) {
            imageViewInstance = new ImageView(GLOBAL_CONTEXT.getApplicationContext());
        }
        return imageViewInstance;
    }

    private Button getButtonInstance() {
        if (buttonInstance == null) {
            buttonInstance = new Button(GLOBAL_CONTEXT);
        }
        return buttonInstance;
    }

    //Show Loader
    private static void showLoader() {
        try {
            if (loader != null) {
                if (displayLoader == false) {
                    displayLoader = true;
                    loader.showLoader();
                }
            }
        } catch (Exception e) {
            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(e.getMessage());
        }

    }

    //Hide Loader
    private static void hideLoader() {
        try {
            if (loader != null) {
                if (displayLoader == true)
                    displayLoader = false;
                loader.hideLoader();
            }
        } catch (Exception e) {
            LogFile.getShared(GLOBAL_CONTEXT).addFailureLog(e.getMessage());
        }
    }

    //resumeCall

    public void resume(String URL) {
        getLoginCode(URL);
    }

    private void saveLoginProperties(final EventResult<Dictionary<String, String>> result) {
        final String methodName = "CidaasSDKLayout:saveLoginProperties()";
        try {
            readFromFile(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginProperties) {
                    if (loginProperties == null || loginProperties.get(CidaasConstants.DOMAIN_URL) == null || loginProperties.get(CidaasConstants.DOMAIN_URL).equals("")) {
                        result.failure(WebAuthError.getShared(GLOBAL_CONTEXT).propertyMissingException("Domain URL must not be null", CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                    if (loginProperties == null || loginProperties.get(CidaasConstants.CLIENT_ID) == null || loginProperties.get(CidaasConstants.CLIENT_ID).equals("")) {
                        result.failure(WebAuthError.getShared(GLOBAL_CONTEXT).propertyMissingException(CidaasConstants.CLIENTID_MUST_NOT_BE_NULL, CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                    if (loginProperties == null || loginProperties.get(CidaasConstants.REDIRECT_URL) == null || loginProperties.get(CidaasConstants.REDIRECT_URL).equals("")) {
                        result.failure(WebAuthError.getShared(GLOBAL_CONTEXT).propertyMissingException("Redirect URL must not be null", CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                    CidaasHelper.baseurl = loginProperties.get(CidaasConstants.DOMAIN_URL);
                    DBHelper.getShared().addLoginProperties(loginProperties);
                    result.success(loginProperties);

                }

                @Override
                public void failure(WebAuthError error) {
                    result.failure(error);
                }
            });
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(GLOBAL_CONTEXT).methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SAVE_LOGIN_PROPERTIES, e.getMessage()));
        }
    }


    //ReadFromXML File
    private void readFromFile(final EventResult<Dictionary<String, String>> loginPropertiesResult) {
        FileHelper fileHelper = FileHelper.getShared(GLOBAL_CONTEXT);
        fileHelper.readProperties(GLOBAL_CONTEXT.getAssets(), "cidaas.xml", new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(final Dictionary<String, String> loginProperties) {

                //on successfully completion of file reading add it to LocalDB(shared Preference) and call requestIdByloginProperties
                checkPKCEFlow(loginProperties, new EventResult<Dictionary<String, String>>() {
                    @Override
                    public void success(Dictionary<String, String> savedLoginProperties) {
                        loginPropertiesResult.success(savedLoginProperties);
                        DomainURL = savedLoginProperties.get(CidaasConstants.DOMAIN_URL);
                        DBHelper.getShared().addLoginProperties(savedLoginProperties);

                    }

                    @Override
                    public void failure(WebAuthError error) {
                        loginPropertiesResult.failure(error);

                    }
                });


            }

            @Override
            public void failure(WebAuthError error) {
                //Return File Reading Error
                loginPropertiesResult.failure(error);
            }
        });
    }


    //Method to check the pkce flow and save it to DB
    private void checkPKCEFlow(Dictionary<String, String> loginproperties, EventResult<Dictionary<String, String>> savedResult) {
        String methodName = "checkPKCEFlow";
        try {

            webAuthError = WebAuthError.getShared(GLOBAL_CONTEXT);

            //Check all the login Properties are Correct
            if (loginproperties.get(CidaasConstants.DOMAIN_URL) == null || loginproperties.get(CidaasConstants.DOMAIN_URL).equals("")
                    || !((Hashtable) loginproperties).containsKey(CidaasConstants.DOMAIN_URL)) {
                webAuthError = webAuthError.propertyMissingException("DomainURL must not be null", methodName);

                savedResult.failure(webAuthError);

                return;
            }
            if ( null == loginproperties.get(CidaasConstants.CLIENT_ID) || loginproperties.get(CidaasConstants.CLIENT_ID).equals("")
                    || !((Hashtable) loginproperties).containsKey(CidaasConstants.CLIENT_ID)) {
                webAuthError = webAuthError.propertyMissingException(CidaasConstants.CLIENTID_MUST_NOT_BE_NULL, methodName);
                savedResult.failure(webAuthError);
                return;
            }
            if (!((Hashtable) loginproperties).containsKey(CidaasConstants.REDIRECT_URL) || null == loginproperties.get(CidaasConstants.REDIRECT_URL)
                    || loginproperties.get(CidaasConstants.REDIRECT_URL).equals("")) {
                webAuthError = webAuthError.propertyMissingException("RedirectURL must not be null", methodName);
                savedResult.failure(webAuthError);
                return;
            }


            //Get enable Pkce Flag
            ENABLE_PKCE = DBHelper.getShared().getEnablePKCE();

            // Check Client Secret if the PKCE Flow is Disabled
            if (!ENABLE_PKCE) {
                if (loginproperties.get(CidaasConstants.CLIENT_SECRET) == null || loginproperties.get(CidaasConstants.CLIENT_SECRET) == "" ||
                        !((Hashtable) loginproperties).containsKey(CidaasConstants.CLIENT_SECRET)) {
                    webAuthError = webAuthError.propertyMissingException("ClientSecret must not be null", methodName);
                    savedResult.failure(webAuthError);
                } else {
                    loginproperties.put(CidaasConstants.CLIENT_SECRET, loginproperties.get(CidaasConstants.CLIENT_SECRET));
                }
            }
            DBHelper.getShared().addLoginProperties(loginproperties);
            savedResult.success(loginproperties);
        } catch (Exception e) {
            Timber.e("Check PKCE Flow  service exception : " + e.getMessage());
            savedResult.failure(webAuthError);
        }
    }


    public void checkSavedProperties(final EventResult<Dictionary<String, String>> result) {
        String methodName = "checkSavedProperties";
        if (DomainURL != null && !DomainURL.equals("")) {


            final Dictionary<String, String> loginProperties = DBHelper.getShared().getLoginProperties(DomainURL);

            if (loginProperties != null && !loginProperties.isEmpty() && loginProperties.size() > 0) {
                //check here for already saved properties

                if ( loginProperties.get(CidaasConstants.REDIRECT_URL) == null || loginProperties.get(CidaasConstants.REDIRECT_URL).equals("")) {
                    webAuthError = webAuthError.propertyMissingException("RedirectURL must not be null", methodName);
                    result.failure(webAuthError);
                    return;
                }
                if (loginProperties.get(CidaasConstants.CLIENT_ID) == null || loginProperties.get(CidaasConstants.CLIENT_ID).equals("")) {
                    webAuthError = webAuthError.propertyMissingException("ClientId must not be null", methodName);
                    result.failure(webAuthError);
                    return;
                }


                result.success(loginProperties);
            } else {
                //Read File from asset to get URL
                readFromFile(new EventResult<Dictionary<String, String>>() {
                    @Override
                    public void success(Dictionary<String, String> savedLoginProperties) {
                        //Call requestIdBy LoginProperties parameter
                        result.success(savedLoginProperties);
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        result.failure(error);
                    }
                });
            }
        } else {
            result.failure(WebAuthError.getShared(GLOBAL_CONTEXT).propertyMissingException("DomainURL must not be null", methodName));
        }

    }

    //on Back Pressed
    public void onBackPressed() {
        try {
            if (webViewInstance.canGoBack()) {
                webViewInstance.goBack();
            } else {
                ((Activity) GLOBAL_CONTEXT).finish();
            }
        } catch (Exception e) {
            //Exception onBackPressed()
        }
    }

    //Logout
    public void logout(final String sub, final String post_logout_redirect_url, final EventResult<String> response) {
        String methodName = "logout";
        try {
            // Get Access Token for service call
            AccessTokenController.getShared(GLOBAL_CONTEXT).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
                @Override
                public void success(AccessTokenEntity result) {
                    //Call Logout method
                    LogoutController.getShared(GLOBAL_CONTEXT).getLogoutURL(CidaasHelper.baseurl, result.getAccess_token(), post_logout_redirect_url, new EventResult<String>() {
                        @Override
                        public void success(String result) {
                            //Clear cookies

                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.removeAllCookies(value -> {
                                //Cookies are removed
                            });


                            //Logout From Social
                            if (iCidaasFacebook != null) {
                                iCidaasFacebook.logout();
                            }
                            if (iCidaasGoogle != null) {
                                iCidaasGoogle.logout();
                            }

                            //Clear in Shared Preferences
                            DBHelper.getShared().removeUserInfo(sub);


                            response.success("User Logged out");

                            webViewInstance.loadUrl(result);
                            hideLoader();
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.removeAllCookies(value -> {
                                //cookies are removed
                            });

                            //Logout From Social
                            if (iCidaasFacebook != null) {
                                iCidaasFacebook.logout();
                            }
                            if (iCidaasGoogle != null) {
                                iCidaasGoogle.logout();
                            }

                            //Clear in Shared Preferences
                            DBHelper.getShared().removeUserInfo(sub);

                            response.success("User Logged out");
                            hideLoader();
                        }
                    });
                }

                @Override
                public void failure(WebAuthError error) {
                    response.failure(error);
                }
            });



        } catch (Exception e) {
            response.failure(WebAuthError.getShared(GLOBAL_CONTEXT).methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGOUT_ERROR, e.getMessage()));
        }
    }

    public void authorize(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 9001) {
                //call google activity result
            } else {
                //call facebook activity result
            }
        } catch (Exception e) {
            //Exception authroze()
        }
    }

}
