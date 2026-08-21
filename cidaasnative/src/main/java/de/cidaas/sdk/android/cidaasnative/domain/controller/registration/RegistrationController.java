package de.cidaas.sdk.android.cidaasnative.domain.controller.registration;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;
import java.util.Locale;

import de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.UpdateUserResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registrationsetup.RegistrationSetupRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registrationsetup.RegistrationSetupResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registrationsetup.RegistrationSetupResultDataEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.Registration.RegistrationService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;

public class RegistrationController {
    private Context context;
    private static RegistrationController shared;
    private RegistrationSetupResultDataEntity[] registerFields;
    private RegistrationSetupResponseEntity validateRegistrationFilelds = new RegistrationSetupResponseEntity();

    private RegistrationController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static RegistrationController getShared(Context contextFromCidaas) {
        if (shared == null) {
            shared = new RegistrationController(contextFromCidaas);
        }
        return shared;
    }

    public void getRegisterationFields(@NonNull final String requestId, final String locale,
                                       final EventResult<RegistrationSetupResponseEntity> registerFieldsresult) {
        final String methodName = "RegistrationController :getRegisterationFields()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    controlForGetRegistrationFields(requestId, locale, loginPropertiesResult, registerFieldsresult);
                }


                @Override
                public void failure(WebAuthError error) {
                    registerFieldsresult.failure(WebAuthError.getShared(context).cidaasPropertyMissingException("", methodName));
                }
            });
        } catch (Exception e) {

            registerFieldsresult.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE,
                    e.getMessage()));
        }
    }

    private void controlForGetRegistrationFields(@NonNull final String requestId, final String locale, Dictionary<String, String> loginPropertiesResult,
                                                 final EventResult<RegistrationSetupResponseEntity> registerFieldsresult) {
        String methodName = "RegistrationController :controlForGetRegistrationFields()";
        try {
            String baseurl = loginPropertiesResult.get(NativeConstants.DOMAIN_URL);
            String language;

            if (!requestId.equals("")) {

                final RegistrationSetupRequestEntity registrationSetupRequestEntity;

                registrationSetupRequestEntity = new RegistrationSetupRequestEntity();
                registrationSetupRequestEntity.setRequestId(requestId);

                if (locale == null || locale.equals("")) {
                    language = Locale.getDefault().getLanguage();
                    registrationSetupRequestEntity.setAcceptedLanguage(language);

                } else {
                    language = locale;
                    registrationSetupRequestEntity.setAcceptedLanguage(language);
                }

                getRegisterationFields(baseurl, registrationSetupRequestEntity, registerFieldsresult);

            } else {
                String errorMessage = "RequestId must not be empty";
                registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        } catch (Exception e) {
            registerFieldsresult.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE,
                    e.getMessage()));
        }
    }

    //Service call To Registration Setup
    public void getRegisterationFields(@NonNull String baseurl, @NonNull RegistrationSetupRequestEntity registrationSetupRequestEntity,
                                       final EventResult<RegistrationSetupResponseEntity> result) {
        String methodName = "RegistrationController :getRegisterationFields()";
        try {

            if (registrationSetupRequestEntity.getAcceptedLanguage() != null && !registrationSetupRequestEntity.getAcceptedLanguage().equals("") &&
                    registrationSetupRequestEntity.getRequestId() != null && !registrationSetupRequestEntity.getRequestId().equals("")
                    && baseurl != null && !baseurl.equals("")) {

                RegistrationService.getShared(context).getRegistrationSetup(baseurl, registrationSetupRequestEntity, null,
                        new EventResult<RegistrationSetupResponseEntity>() {
                            @Override
                            public void success(RegistrationSetupResponseEntity serviceresult) {
                                validateRegistrationFilelds = serviceresult;
                                registerFields = serviceresult.getData();
                                result.success(serviceresult);
                            }

                            @Override
                            public void failure(WebAuthError error) {
                                result.failure(error);
                            }
                        });
            } else {
                result.failure(WebAuthError.getShared(context).propertyMissingException("Accepted Language or requestId must not be null", methodName));
            }
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }

    public void registerNewUser(@NonNull final String requestId, final RegistrationEntity registrationEntity,
                                final EventResult<RegisterNewUserResponseEntity> registerFieldsresult) {
        final String methodName = "RegistrationController :registerNewUser()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {
                    String baseurl = result.get(NativeConstants.DOMAIN_URL);

                    if (!requestId.equals("")) {
                        if (registerFields != null) {
                            if (registerFields.length > 0) {
                                for (RegistrationSetupResultDataEntity dataEntity : registerFields) {

                                    checkNotNull(dataEntity, "email", registrationEntity.getEmail());
                                    checkNotNull(dataEntity, "given_name", registrationEntity.getGiven_name());
                                    checkNotNull(dataEntity, "family_name", registrationEntity.getFamily_name());
                                    checkNotNull(dataEntity, "mobile_number", registrationEntity.getMobile_number());
                                    checkNotNull(dataEntity, "username", registrationEntity.getUsername());
                                    checkNotNull(dataEntity, "birthdate", registrationEntity.getBirthdate().toString());
                                    if (dataEntity.getFieldKey().equals("password")
                                            && dataEntity.isRequired()
                                            && registrationEntity.getPassword().isEmpty()) {
                                        registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException("password must not be empty", methodName));
                                    }

                                    if (dataEntity.getFieldKey().equals("password_echo")) {
                                        if (dataEntity.isRequired() && registrationEntity.getPassword_echo().equals("")) {
                                            String errorMessage = "password_echo must not be empty";
                                            registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                                            return;

                                        }
                                        if (!registrationEntity.getPassword_echo().equals(registrationEntity.getPassword())) {

                                            String errorMessage = "Password and password_echo must be same";
                                            registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                                            return;
                                        }
                                    }

                                    if (registrationEntity.getProvider() != null && !registrationEntity.getProvider().equals("")) {
                                        String errorMessage = "Provider must not be empty";
                                        registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                                        return;

                                    }
                                }
                            }
                        }

                        final RegisterNewUserRequestEntity registerNewUserRequestEntity;

                        registerNewUserRequestEntity = new RegisterNewUserRequestEntity();
                        registerNewUserRequestEntity.setRequestId(requestId);
                        registrationEntity.setProvider("self");
                        registerNewUserRequestEntity.setRegistrationEntity(registrationEntity);


                        registerNewUser(baseurl, registerNewUserRequestEntity, registerFieldsresult);

                    } else {
                        String errorMessage = "RequestId must not be empty";

                        registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                    }

                }

                private void checkNotNull(RegistrationSetupResultDataEntity dataEntity, String key, String registrationEntityValue) {
                    if (dataEntity.getFieldKey().equals(key)
                            && dataEntity.isRequired()
                            && registrationEntityValue.isEmpty()) {
                        String errorMessage = key + " must not be empty";
                        registerFieldsresult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    registerFieldsresult.failure(WebAuthError.getShared(context).cidaasPropertyMissingException("", methodName));
                }
            });
        } catch (Exception e) {
            registerFieldsresult.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE,
                    e.getMessage()));
        }
    }

    //Register new User
    public void registerNewUser(String baseurl, RegisterNewUserRequestEntity registrationEntity, final EventResult<RegisterNewUserResponseEntity> result) {
        try {
            // Check for Not null
            registerWithNewUserService(baseurl, registrationEntity, result);
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException("Exception :RegistrationController :registerNewUser()",
                    WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }

    //Service call To Registration Setup
    public void registerWithNewUserService(@NonNull String baseurl, @NonNull RegisterNewUserRequestEntity registerNewUserRequestEntity,
                                           final EventResult<RegisterNewUserResponseEntity> result) {
        String methodName = "RegistrationController :registerWithNewUserService()";
        try {

            if (registerNewUserRequestEntity.getRequestId() != null && !registerNewUserRequestEntity.getRequestId().equals("") &&
                    registerNewUserRequestEntity.getRegistrationEntity() != null &&
                    !registerNewUserRequestEntity.getRegistrationEntity().getFamily_name().equals("") && baseurl != null && !baseurl.equals("")) {
                // Service call
                RegistrationService.getShared(context).registerNewUser(baseurl, registerNewUserRequestEntity, result);
            } else {
                String errorMessage = "RequestId must not be empty";
                result.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
            }
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }

    //update User

    public void updateUser(@NonNull final String access_token, final RegistrationEntity registrationEntity,
                           final EventResult<UpdateUserResponseEntity> registerFieldsResult) {
        final String methodName = "RegistrationController :updateUser()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {
                    String baseurl = result.get(NativeConstants.DOMAIN_URL);

                    if (access_token == null || access_token.equals("") || registrationEntity.getSub() == null || registrationEntity.getSub().equals("")) {
                        String errorMessage = "access_token or sub must not be empty";

                        registerFieldsResult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                    }

                    updateUserService(baseurl, access_token, registrationEntity, registerFieldsResult);

                }

                private void checkNotNull(RegistrationSetupResultDataEntity dataEntity, String key, String registrationEntityValue) {
                    if (dataEntity.getFieldKey().equals(key)
                            && dataEntity.isRequired()
                            && registrationEntityValue.equals("")) {
                        String errorMessage = key + " must not be empty";
                        registerFieldsResult.failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    registerFieldsResult.failure(WebAuthError.getShared(context).cidaasPropertyMissingException("", methodName));
                }
            });
        } catch (Exception e) {
            registerFieldsResult.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE,
                    e.getMessage()));
        }
    }

    //Service call To Registration Setup
    private void updateUserService(@NonNull String baseurl, @NonNull String access_token, @NonNull RegistrationEntity registerationEntity,
                                   final EventResult<UpdateUserResponseEntity> result) {
        String methodName = "RegistrationController :registerWithNewUserService()";
        try {

            // Service call
            RegistrationService.getShared(context).updateUserProfile(baseurl, access_token, registerationEntity, result);

        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REGISTRATION_SETUP_FAILURE, e.getMessage()));
        }
    }


}
