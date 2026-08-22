package de.cidaas.sdk.android.cidaasnative.domain.controller.login;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginCredentialsRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.Login.NativeLoginService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.models.dbmodel.AccessTokenModel;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import timber.log.Timber;

public class NativeLoginController {
    private Context context;
    // public String loginURL;
    //public String DomainURL="";
    public String redirectUrl;
    public EventResult<AccessTokenEntity> logincallback;

    public static NativeLoginController shared;

    public NativeLoginController(Context contextFromCidaas) {

        //Set Callback Null;
        logincallback = null;
        context = contextFromCidaas;
        //Make Call back null
    }

    public static NativeLoginController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new NativeLoginController(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }


    //Service call for loginWithCredentials
    public void loginwithCredentials(@NonNull final String requestId, @NonNull final LoginEntity loginEntity,
                                     @NonNull final EventResult<LoginCredentialsResponseEntity> result) {
        String methodName = "LoginController :loginwithCredentials()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    LoginCredentialsRequestEntity loginCredentialsRequestEntity = getLoginCredentialsRequestEntity(requestId, loginEntity, result);
                    if (loginCredentialsRequestEntity != null) {
                        NativeLoginService.getShared(context).loginWithCredentials(loginPropertiesResult.get(NativeConstants.DOMAIN_URL), loginCredentialsRequestEntity,
                                new EventResult<LoginCredentialsResponseEntity>() {
                                    @Override
                                    public void success(LoginCredentialsResponseEntity serviceresult) {
                                        getAccessTokenAfterLogin(serviceresult, result);
                                    }

                                    @Override
                                    public void failure(WebAuthError error) {
                                        result.failure(error);
                                    }
                                });
                    }
                }
                @Override
                public void failure(WebAuthError error) {
                    result.failure(error);
                }
            });
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE, e.getMessage()));
        }
    }

    public void logout(@NonNull final String sub, @NonNull final EventResult<Boolean> result){
        String methodName = "LoginController :logout()";
        try{
        CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(Dictionary<String, String> loginPropertiesResult) {
                if (sub != null){
                    AccessTokenModel accessTokenModel= DBHelper.getShared().getAccessToken(sub);
                    if(accessTokenModel !=null && accessTokenModel.getAccess_token() !=null) {
                        NativeLoginService.getShared(context).logout(loginPropertiesResult.get(NativeConstants.DOMAIN_URL), accessTokenModel.getAccess_token(), result);
                    }
                    else {
                        result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException( "Invalid Sub passed","Exception: "+ methodName));
                    }
                }
                else {
                    result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException( "Sub must not be empty ","Exception: "+ methodName));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException( error.getMessage(),"Exception: "+ methodName));

            }

        });
        } catch(Exception e){
            result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException( e.getMessage(),"Exception: "+ methodName));
        }
    }




    //Get Login Credentials Request Entity
    private LoginCredentialsRequestEntity getLoginCredentialsRequestEntity(String requestId, LoginEntity loginEntity, final EventResult<LoginCredentialsResponseEntity> result) {

        String methodName = "LoginController :getLoginCredentialsRequestEntity()";
        try {
            LoginCredentialsRequestEntity loginCredentialsRequestEntity = new LoginCredentialsRequestEntity();
            if (loginEntity.getUsername_type() == null || loginEntity.getUsername_type().equals("")) {
                loginEntity.setUsername_type("email");
            }

            if (loginEntity.getPassword() != null && !loginEntity.getPassword().isEmpty() && loginEntity.getUsername() != null
                    && !loginEntity.getUsername().isEmpty() && requestId != null && !requestId.isEmpty()) {

                loginCredentialsRequestEntity.setUsername(loginEntity.getUsername());
                loginCredentialsRequestEntity.setUsername_type(loginEntity.getUsername_type());
                loginCredentialsRequestEntity.setPassword(loginEntity.getPassword());
                loginCredentialsRequestEntity.setRequestId(requestId);
                return loginCredentialsRequestEntity;

            } else {

                String errorMessage = "Username or password or RequestId must not be empty";
                result.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, NativeConstants.ERROR_LOGGING_PREFIX + errorMessage));
                return null;
            }
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.LOGINWITH_CREDENTIALS_FAILURE, e.getMessage()));
            return null;
        }
    }


    private void getAccessTokenAfterLogin(LoginCredentialsResponseEntity serviceresult, final EventResult<LoginCredentialsResponseEntity> result) {
        //Access Token After Login
        AccessTokenController.getShared(context).getAccessTokenByCode(serviceresult.getData().getCode(), new EventResult<AccessTokenEntity>() {
            @Override
            public void success(AccessTokenEntity accessTokenresult) {

                LoginCredentialsResponseEntity loginCredentialsResponseEntity = new LoginCredentialsResponseEntity();
                loginCredentialsResponseEntity.setData(accessTokenresult);
                loginCredentialsResponseEntity.setStatus(200);
                loginCredentialsResponseEntity.setSuccess(true);
                result.success(loginCredentialsResponseEntity);
            }

            @Override
            public void failure(WebAuthError error) {

                result.failure(error);
            }
        });
    }

}
