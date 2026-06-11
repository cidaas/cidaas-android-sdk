package de.cidaas.sdk.android.service.repository.AccessToken;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import de.cidaas.sdk.android.R;
import de.cidaas.sdk.android.entities.SocialAccessTokenEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.general.GenralHelper;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.helper.crypthelper.DpopP256Keystore;
import de.cidaas.sdk.android.service.CidaassdkService;
import de.cidaas.sdk.android.service.ICidaasSDKService;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.entity.socialprovider.SocialProviderEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AccessTokenService {

    private Context context;

    public static AccessTokenService shared;

    CidaassdkService service;
    public ObjectMapper objectMapper = new ObjectMapper();

    public AccessTokenService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaassdkService(context);
        }

        // setValue for authenticationType

    }

    public static AccessTokenService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new AccessTokenService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    // Get Access Token by code
    public void getAccessTokenByCode(String baseurl, String Code,
            final EventResult<AccessTokenEntity> acessTokencallback) {
        getAccessTokenByCode(baseurl, Code, false, acessTokencallback);
    }

    public void getAccessTokenByCode(String baseurl, String Code, boolean useDpop,
            final EventResult<AccessTokenEntity> acessTokencallback) {
        String methodName = "AccessToken service :-getAccessTokenByCode()";
        try {

            if (baseurl != null && !baseurl.equals("")) {

                // Construct URL For RequestId
                String getAccessTokenUrl = baseurl + URLHelper.getShared().getTokenUrl();

                // Header Generation
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentType);

                if (useDpop) {
                    try {
                        String dpopProof = DpopP256Keystore.proofJwt(context, "POST", getAccessTokenUrl);
                        headers.put("DPoP", dpopProof);
                    } catch (Exception e) {
                        acessTokencallback.failure(WebAuthError.getShared(context).methodException(
                                CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                                WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                                e.getMessage()));
                        return;
                    }
                }

                // Get Verifier and loginProperties
                Dictionary<String, String> challengeProperties = DBHelper.getShared().getChallengeProperties();
                Dictionary<String, String> loginProperties = DBHelper.getShared().getLoginProperties(baseurl);

                Map<String, String> querymap = new Hashtable<>();

                // Get Properties From DB

                // Add Body Parameter
                // generate Body Parameter
                querymap.put("grant_type", "authorization_code");
                querymap.put("code", Code);
                querymap.put("redirect_uri", loginProperties.get(CidaasConstants.REDIRECT_URL));
                querymap.put("client_id", loginProperties.get(CidaasConstants.CLIENT_ID));
                querymap.put("code_verifier", challengeProperties.get(CidaasConstants.VERIFIER));

                // Service call
                serviceForGetAccessTokenByCode(getAccessTokenUrl, headers, querymap, acessTokencallback);

            } else {
                acessTokencallback.failure(WebAuthError.getShared(context).propertyMissingException(
                        context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            acessTokencallback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForGetAccessTokenByCode(String getAccessTokenUrl, Map<String, String> headers,
            Map<String, String> querymap,
            final EventResult<AccessTokenEntity> acessTokencallback) {
        final String methodName = "AccessToken service :-serviceForGetAccessTokenByCode()";
        try {
            // call service
            ICidaasSDKService cidaasSDKService = service.getInstance();
            cidaasSDKService.getAccessTokenByCode(getAccessTokenUrl, headers, querymap)
                    .enqueue(new Callback<AccessTokenEntity>() {
                        @Override
                        public void onResponse(Call<AccessTokenEntity> call, Response<AccessTokenEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    acessTokencallback.success(response.body());
                                } else {
                                    acessTokencallback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, response.code(),
                                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                acessTokencallback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                        WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, response,
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<AccessTokenEntity> call, Throwable t) {
                            acessTokencallback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, t.getMessage(),
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            acessTokencallback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

    // Get Access Token by Clientid and Client Secret
    public void getAccessTokenByIdAndSecret(String ClientId, String ClientSecret,
            EventResult<AccessTokenEntity> callback) {

    }

    // --------------------------------------------------------Get AccessToken by
    // Refresh Token-----------------------------------------------------------
    public void getAccessTokenByRefreshToken(String refreshToken, Dictionary<String, String> loginProperties,
            final EventResult<AccessTokenEntity> callback) {
        String methodName = "AccessToken service :-getAccessTokenByRefreshToken()";
        try {
            // Local Variables
            String baseurl = loginProperties.get("DomainURL");
            if (baseurl != null && !baseurl.equals("")) {

                // url Value assign
                String url = baseurl + URLHelper.getShared().getTokenUrl();

                // Header generation
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentType);

                // Challenge Generation
                Dictionary<String, String> challengeProperties = DBHelper.getShared().getChallengeProperties();

                // Get Properties From DB
                if (loginProperties == null) {
                    callback.failure(WebAuthError.getShared(context)
                            .loginURLMissingException(CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }

                // Check Verifier,If null set it as empty String
                if (challengeProperties.get(CidaasConstants.VERIFIER) == null) {
                    challengeProperties.put(CidaasConstants.VERIFIER, "");
                }

                // Add Body Parameter
                Map<String, String> querymap = new Hashtable<>();
                querymap.put("grant_type", "refresh_token");
                querymap.put("redirect_uri", loginProperties.get("RedirectURL"));
                querymap.put("client_id", loginProperties.get("ClientId"));
                querymap.put("code_verifier", challengeProperties.get(CidaasConstants.VERIFIER));
                querymap.put("refresh_token", refreshToken);

                // Service call
                serviceForAccessTokenService(url, headers, querymap, callback);
            }
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForAccessTokenService(String url, Map<String, String> headers, Map<String, String> querymap,
            final EventResult<AccessTokenEntity> callback) {
        final String methodName = "AccessToken service :-getAccessTokenByRefreshToken()";
        try {
            // call service
            ICidaasSDKService cidaasSDKService = service.getInstance();
            cidaasSDKService.getAccessTokenByRefreshToken(url, headers, querymap)
                    .enqueue(new Callback<AccessTokenEntity>() {
                        @Override
                        public void onResponse(Call<AccessTokenEntity> call, Response<AccessTokenEntity> response) {
                            if (response.isSuccessful()) {

                                // save the accessToken in Storage helper
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, response.code(),
                                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                        WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, response,
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<AccessTokenEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, t.getMessage(),
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));

                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

    // ----------------------------------------------------------get Access Token by
    // Social---------------------------------------------------------------
    public void getAccessTokenBySocial(SocialAccessTokenEntity socialAccessTokenEntity,
            Dictionary<String, String> loginProperties,
            final EventResult<SocialProviderEntity> callback) {
        String methodName = "AccessToken service :-getAccessTokenBySocial()";
        try {

            // URL Generation
            String getAccessTokenBySocialURL = GenralHelper.getShared()
                    .constructSocialServiceURl(socialAccessTokenEntity, "token", loginProperties);
            getAccessTokenBySocialURL = getAccessTokenBySocialURL + URLHelper.getShared().getPreAuthCode()
                    + socialAccessTokenEntity.getRequestId();

            // Headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, null);

            // Service call
            serviceForGetAccessTokenBySocial(getAccessTokenBySocialURL, headers, callback);

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForGetAccessTokenBySocial(String baseURL, Map<String, String> headers,
            final EventResult<SocialProviderEntity> callback) {
        final String methodName = "AccessToken service :-serviceForGetAccessTokenBySocial()";
        try {
            ICidaasSDKService cidaasSDKService = service.getInstance();
            cidaasSDKService.getAccessTokenBySocial(baseURL, headers).enqueue(new Callback<SocialProviderEntity>() {

                @Override
                public void onResponse(Call<SocialProviderEntity> call, Response<SocialProviderEntity> response) {

                    if (response.isSuccessful()) {

                        // t save the accesstoken in Storage helper
                        if (response.code() == 200) {
                            callback.success(response.body());
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                    WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, response.code(),
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {

                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, response,
                                CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<SocialProviderEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE, t.getMessage(),
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));

                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ACCESSTOKEN_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

}
