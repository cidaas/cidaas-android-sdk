package de.cidaas.sdk.android.cidaasnative.domain.service.Registration;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.InitiateAccountVerificationRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.InitiateAccountVerificationResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.VerifyAccountRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.VerifyAccountResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.UpdateUserResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registrationsetup.RegistrationSetupRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registrationsetup.RegistrationSetupResponseEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.AccountVerification.AccountVerificationService;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class RegistrationService {
    CidaasNativeService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    //Local variables
    private String statusId;
    private String authenticationType;
    private String sub;
    private String verificationType;
    private Context context;

    public static RegistrationService shared;

    public RegistrationService(Context contextFromCidaas) {
        sub = "";
        statusId = "";
        verificationType = "";
        context = contextFromCidaas;
        authenticationType = "";


        if (service == null) {
            service = new CidaasNativeService(context);
        }

    }


    public static RegistrationService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new RegistrationService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    //-----------------------------------------------------Get Regsiteration Details-------------------------------------------------------------------
    public void getRegistrationSetup(String baseurl, final RegistrationSetupRequestEntity registrationSetupRequestEntity,
                                     DeviceInfoEntity deviceInfoEntityFromParam, final EventResult<RegistrationSetupResponseEntity> callback) {
        //Local Variables

        String methodName = "RegistrationService :getRegistrationSetup()";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                String RegistrationUrl = baseurl + NativeURLHelper.getShared().getRegistrationSetup(registrationSetupRequestEntity.getAcceptedLanguage(),
                        registrationSetupRequestEntity.getRequestId());


                //Call Service-getRequestId
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, null);

                serviceForGetRegistrationSetup(RegistrationUrl, headers, callback);

            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }


        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }

    private void serviceForGetRegistrationSetup(String registrationUrl, Map<String, String> headers, final EventResult<RegistrationSetupResponseEntity> callback) {
        final String methodName = "RegistrationService :getRegistrationSetup()";
        try {
            final ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.getRegistrationSetup(registrationUrl, headers).enqueue(new Callback<RegistrationSetupResponseEntity>() {
                @Override
                public void onResponse(Call<RegistrationSetupResponseEntity> call, Response<RegistrationSetupResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            callback.success(response.body());
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.REGISTRATION_SETUP_FAILURE,
                                    response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, response
                                , NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<RegistrationSetupResponseEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, t.getMessage(),
                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }


    //----------------------------------------------------------Register New User--------------------------------------------------------------------------
    public void registerNewUser(String baseurl, final RegisterNewUserRequestEntity registerNewUserRequestEntity, final EventResult<RegisterNewUserResponseEntity> callback) {
        //Local Variables

        String methodName = "";
        try {

            if (baseurl != null && !baseurl.equals("")) {

                //Construct URL For RequestId
                String RegisterNewUserUrl = baseurl + NativeURLHelper.getShared().getRegisterNewUserurl();

                //Header Generation
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson,
                        registerNewUserRequestEntity.getRequestId());

                //service
                serviceForRegisterNewUser(RegisterNewUserUrl, registerNewUserRequestEntity, headers, callback);

            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }


        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }

    private void serviceForRegisterNewUser(String registerNewUserUrl, RegisterNewUserRequestEntity registerNewUserRequestEntity, Map<String, String> headers,
                                           final EventResult<RegisterNewUserResponseEntity> callback) {
        final String methodName = "RegistrationService :registerNewUser()";
        try {
            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.registerNewUser(registerNewUserUrl, headers, registerNewUserRequestEntity.getRegistrationEntity()).enqueue(
                    new Callback<RegisterNewUserResponseEntity>() {
                        @Override
                        public void onResponse(Call<RegisterNewUserResponseEntity> call, Response<RegisterNewUserResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.REGISTRATION_SETUP_FAILURE
                                            , response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if( response.errorBody() != null ){
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.REGISTRATION_SETUP_FAILURE,
                                            response, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<RegisterNewUserResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }

    //----------------------------------------------------------Update User profile--------------------------------------------------------------------------
    public void updateUserProfile(String baseurl, String access_token, final RegistrationEntity registrationEntity, final EventResult<UpdateUserResponseEntity> callback) {
        //Local Variables

        String methodName = "RegistrationService:updateUserProfile";
        try {

            if (baseurl != null && !baseurl.equals("")) {

                //Construct URL For RequestId
                String updateUserProfileURL = baseurl + NativeURLHelper.getShared().getUpdateUserProfileURL(registrationEntity.getSub());

                //Header Generation
                Map<String, String> headers = Headers.getShared(context).getHeaders(access_token, false, NativeURLHelper.contentTypeJson);

                //service
                serviceForupdateUserProfile(updateUserProfileURL, registrationEntity, headers, callback);

            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }


        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.USER_PROFILE_UPDATE_FAILURE, e.getMessage()));
        }
    }

    private void serviceForupdateUserProfile(String updateUserProfileURL, RegistrationEntity registrationEntity, Map<String, String> headers,
                                             final EventResult<UpdateUserResponseEntity> callback) {
        final String methodName = "RegistrationService :serviceForupdateUserProfile()";
        try {
            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.updateUserProfile(updateUserProfileURL, headers, registrationEntity).enqueue(
                    new Callback<UpdateUserResponseEntity>() {
                        @Override
                        public void onResponse(Call<UpdateUserResponseEntity> call, Response<UpdateUserResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.USER_PROFILE_UPDATE_FAILURE
                                            , response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if(response.errorBody() != null){
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.USER_PROFILE_UPDATE_FAILURE,
                                            response, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UpdateUserResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.USER_PROFILE_UPDATE_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.USER_PROFILE_UPDATE_FAILURE, e.getMessage()));
        }
    }


    //----------------------------------------------------Register New User initiate  Account Verification--------------------------------------------------
    public void initiateAccountVerification(String baseurl, final InitiateAccountVerificationRequestEntity initiateAccountVerificationRequestEntity,
                                            final EventResult<InitiateAccountVerificationResponseEntity> callback) {

        String methodName = "RegistrationService :initiateAccountVerification()";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                AccountVerificationService.getShared(context).initiateAccountVerification(baseurl,
                        initiateAccountVerificationRequestEntity, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-----------------------------------------------------Register New User verify  Account Verification----------------------------------------------------
    public void verifyAccountVerification(String baseurl, final VerifyAccountRequestEntity verifyAccountRequestEntity,
                                          final EventResult<VerifyAccountResponseEntity> callback) {
        //Local Variables

        String methodName = NativeConstants.METHOD_VERIFY_ACCOUNT_VERFICATION_REGISTRATION_SERVICE;
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                String verifyAccountVerificationUrl = baseurl + NativeURLHelper.getShared().getRegisterUserAccountVerify();

                //Header
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);

                //Service
                serviceForVerifyAccountVerification(verifyAccountVerificationUrl, verifyAccountRequestEntity, headers, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForVerifyAccountVerification(String verifyAccountVerificationUrl, VerifyAccountRequestEntity
            verifyAccountRequestEntity, Map<String, String> headers, final EventResult<VerifyAccountResponseEntity> callback) {
        final String methodName = NativeConstants.METHOD_VERIFY_ACCOUNT_VERFICATION_REGISTRATION_SERVICE;
        try {
            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();

            cidaasNativeService.verifyAccountVerification(verifyAccountVerificationUrl, headers, verifyAccountRequestEntity)
                    .enqueue(new Callback<VerifyAccountResponseEntity>() {
                        @Override
                        public void onResponse(Call<VerifyAccountResponseEntity> call, Response<VerifyAccountResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                            response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                assert response.errorBody() != null;
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                        response, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<VerifyAccountResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                    t.getMessage(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

}
