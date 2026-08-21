package de.cidaas.sdk.android.cidaasnative.domain.service.Login;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginCredentialsRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginCredentialsResponseErrorEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class NativeLoginService {


    private Context context;

    public static NativeLoginService shared;

    CidaasNativeService service;
    public ObjectMapper objectMapper = new ObjectMapper();

    public NativeLoginService(Context contextFromCidaas) {
        if (service == null) {
            service = new CidaasNativeService(contextFromCidaas);
        }

        context = contextFromCidaas;

    }

    public static NativeLoginService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new NativeLoginService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }


    //----------------------------------------------------------Login With Credentials-----------------------------------------------------------------------------
    public void loginWithCredentials(final String baseurl, final LoginCredentialsRequestEntity loginCredentialsRequestEntity,
                                     final EventResult<LoginCredentialsResponseEntity> callback) {
        //Local Variables

        String methodName = "LoginService :serviceForLoginWithCredentials()";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                String loginUrl = baseurl + NativeURLHelper.getShared().getLoginWithCredentials();

                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);

                //Call Service-getRequestId
                serviceForLoginWithCredentials(loginUrl, loginCredentialsRequestEntity, headers, callback);

            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE, e.getMessage()));
        }
    }

    public void logout(final String baseurl, final String accessToken, final EventResult<Boolean>callback){
        String methodName = "loginService : serviceForLogout()";
        try{
            if (baseurl != null && !baseurl.equals("")) {
                String logoutUrl = baseurl + NativeURLHelper.getShared().getLogoutUrl(accessToken);

                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);

                // Call Service-Logout
                serviceForLogout(logoutUrl, headers, callback);
            }

        }catch (Exception e){
            callback.failure((WebAuthError.getShared(context).methodException("Exception:"+methodName, WebAuthErrorCode.LOGOUT_ERROR, e.getMessage())));
        }
    }
    private void serviceForLogout(String logoutUrl, Map<String, String> headers,final EventResult<Boolean> callback){
        String methodName = "loginService : serviceForLogout()";
        try {
            final ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.logout(logoutUrl, headers).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                    if (response.code() == 200){
                        callback.success(true);
                    } else {
                        callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.LOGOUT_ERROR,
                                response.code(), "Error" + methodName));
                    }
                } else {
                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.LOGOUT_ERROR, response
                                , NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                       // callback.failure(WebAuthError.getShared(context).customException(WebAuthErrorCode.LOGOUT_ERROR, "Exception in LogoutProcess",methodName));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.LOGOUT_ERROR, t.getMessage(),
                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGOUT_ERROR, e.getMessage()));
        }
    }

    private void serviceForLoginWithCredentials(String loginUrl, LoginCredentialsRequestEntity loginCredentialsRequestEntity, Map<String, String> headers,
                                                final EventResult<LoginCredentialsResponseEntity> callback) {
        final String methodName = "Error :LoginService :serviceForLoginWithCredentials()";
        try {
            final ICidaasNativeService cidaasNativeService = service.getInstance();

            cidaasNativeService.loginWithCredentials(loginUrl, headers, loginCredentialsRequestEntity).enqueue(new Callback<LoginCredentialsResponseEntity>() {
                @Override
                public void onResponse(Call<LoginCredentialsResponseEntity> call, Response<LoginCredentialsResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            callback.success(response.body());
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE,
                                    response.code(), "Error" + methodName));
                        }
                    } else {
                        assert response.errorBody() != null;
                        try {

                            // Handle proper error message
                            String errorResponse = response.errorBody().source().readByteString().utf8();
                            final LoginCredentialsResponseErrorEntity loginCredentialsResponseErrorEntity;
                            loginCredentialsResponseErrorEntity = objectMapper.readValue(errorResponse, LoginCredentialsResponseErrorEntity.class);


                            callback.failure(WebAuthError.getShared(context).loginFailureException(WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE,
                                    loginCredentialsResponseErrorEntity.getError().getError(), loginCredentialsResponseErrorEntity.getStatus(),
                                    loginCredentialsResponseErrorEntity.getError(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));

                        } catch (Exception e) {
                            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE,
                                    e.getMessage()));
                            // Timber.e("response"+response.message()+e.getMessage());
                        }

                    }
                }

                @Override
                public void onFailure(Call<LoginCredentialsResponseEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE, t.getMessage(),
                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE, e.getMessage()));
        }
    }

}