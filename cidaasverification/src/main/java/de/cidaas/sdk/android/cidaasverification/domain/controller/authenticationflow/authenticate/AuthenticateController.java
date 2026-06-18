package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.authenticate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.domain.helper.biometrichandler.BiometricHandler;
import de.cidaas.sdk.android.cidaasverification.domain.service.authenticate.AuthenticateService;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AuthenticateController {
    //Local Variables
    private Context context;


    public static AuthenticateController shared;

    public AuthenticateController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }


    public static AuthenticateController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new AuthenticateController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("AuthenticateController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }


    //--------------------------------------------Authenticate--------------------------------------------------------------
    public void authenticateVerification(final AuthenticateEntity authenticateEntity, final EventResult<AuthenticateResponse> authenticateResult) {
        checkAuthenticateEntity(authenticateEntity, authenticateResult);
    }


    //-------------------------------------checkAuthenticateEntity-----------------------------------------------------------
    private void checkAuthenticateEntity(final AuthenticateEntity authenticateEntity, final EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-checkAuthenticateEntity()";
        try {

            if (authenticateEntity.getVerificationType() != null && !authenticateEntity.getVerificationType().equals("") &&
                    authenticateEntity.getExchange_id() != null && !authenticateEntity.getExchange_id().equals("")) {
                LogFile.getShared(context).addInfoLog(methodName, " Verification Type:- " + authenticateEntity.getVerificationType() +
                        "ExchangeId:- " + authenticateEntity.getExchange_id());
                // Handle Verification
                handleVerificationTypes(authenticateEntity, authenticateResult);
            } else {
                authenticateResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "ExchangeId or Verification Type must not be null", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }


    //-----------------------------------------------handleVerificationTypes---------------------------------------------------------------
    private void handleVerificationTypes(AuthenticateEntity authenticateEntity, EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-handleVerificationTypes() ";
        try {
            switch (authenticateEntity.getVerificationType()) {

                case AuthenticationType.FINGERPRINT: {
                    if (authenticateEntity.getAttestation() != null && !authenticateEntity.getAttestation().isEmpty()) {
                        addProperties(authenticateEntity, authenticateResult);
                    } else {
                        callFingerPrintAuthentication(authenticateEntity, authenticateResult);
                    }
                    break;
                }
                case AuthenticationType.FACE: {
                    Bitmap finalimg = BitmapFactory.decodeFile(authenticateEntity.getFileToSend().getAbsolutePath());

                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), authenticateEntity.getFileToSend());
                    MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", "de.cidaas.png", requestFile);

                    addPropertiesForFaceOrVoice(photo, authenticateEntity, authenticateResult);
                    break;
                }
                case AuthenticationType.VOICE: {

                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), authenticateEntity.getFileToSend());
                    MultipartBody.Part voice = MultipartBody.Part.createFormData("voice", "Audio.fav", requestFile);

                    addPropertiesForFaceOrVoice(voice, authenticateEntity, authenticateResult);
                    break;
                }
                case AuthenticationType.PASSWORD: {
                    if (authenticateEntity.getPassword() == null || authenticateEntity.getPassword().isEmpty()) {
                        authenticateResult.failure(WebAuthError.getShared(context).propertyMissingException(
                                "password must not be null or empty for password verification",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                    addProperties(authenticateEntity, authenticateResult);
                    break;
                }
                case AuthenticationType.SMS:
                case AuthenticationType.EMAIL:
                case AuthenticationType.IVR:
                case AuthenticationType.CHAT:
                case AuthenticationType.TOTP:
                case AuthenticationType.BACKUPCODE: {
                    if (authenticateEntity.getPass_code() == null || authenticateEntity.getPass_code().isEmpty()) {
                        authenticateResult.failure(WebAuthError.getShared(context).propertyMissingException(
                                "pass_code must not be null or empty",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                    addProperties(authenticateEntity, authenticateResult);
                    break;
                }
                default: {
                    if (authenticateEntity.getPass_code() != null && !authenticateEntity.getPass_code().equals("")) {

                        addProperties(authenticateEntity, authenticateResult);
                    } else {
                        authenticateResult.failure(WebAuthError.getShared(context).propertyMissingException("Passcode must not be empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                }
            }

        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void callFingerPrintAuthentication(final AuthenticateEntity authenticateEntity, final EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-callFingerPrintAuthentication() ";
        try {
            BiometricHandler biometricHandler = new BiometricHandler(authenticateEntity.getFingerPrintEntity().getContext());
            biometricHandler.callFingerPrint(authenticateEntity.getFingerPrintEntity(), methodName, new EventResult<String>() {
                @Override
                public void success(String result) {
                    //call authenticate call
                    addProperties(authenticateEntity, authenticateResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    authenticateResult.failure(error);
                }
            });

        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }


    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addProperties(final AuthenticateEntity authenticateEntity, final EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-addProperties()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                    final String clientId = loginPropertiesResult.get(VerificationConstants.CLIENT_ID);

                    //App properties
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
                    authenticateEntity.setDevice_id(deviceInfoEntity.getDeviceId());
                    authenticateEntity.setPush_id(deviceInfoEntity.getPushNotificationId());
                    authenticateEntity.setClient_id(clientId);

                    //call authenticate call
                    callAuthenticate(baseurl, authenticateEntity, authenticateResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    authenticateResult.failure(error);
                }
            });

        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    //-------------------------------------------Call authenticate Service-----------------------------------------------------------
    private void callAuthenticate(String baseurl, final AuthenticateEntity authenticateEntity, final EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-authenticate()";
        try {
            String authenticateUrl = VerificationURLHelper.getShared().getAuthenticateURL(baseurl, authenticateEntity.getVerificationType());

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //Authenticate Service call
            AuthenticateService.getShared(context).callAuthenticateService(authenticateUrl, headers, authenticateEntity, authenticateResult);

        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addPropertiesForFaceOrVoice(final MultipartBody.Part filetosend, final AuthenticateEntity authenticateEntity, final EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-addPropertiesForFaceOrVoice() ";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                    final String clientId = loginPropertiesResult.get(VerificationConstants.CLIENT_ID);

                    //Change To Hashmap and Add Properties
                    HashMap<String, RequestBody> authenticateHashmap = new HashMap<>();
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();

                    //Optional Sub
                    //  authenticateHashmap.put("",StringtoRequestBody(authenticateEntity.))
                    authenticateHashmap.put("exchange_id", StringtoRequestBody(authenticateEntity.getExchange_id()));
                    authenticateHashmap.put("device_id", StringtoRequestBody(deviceInfoEntity.getDeviceId()));
                    authenticateHashmap.put("client_id", StringtoRequestBody(clientId));
                    authenticateHashmap.put("push_id", StringtoRequestBody(deviceInfoEntity.getPushNotificationId()));
                    authenticateHashmap.put("face_attempt", StringtoRequestBody("" + authenticateEntity.getFace_attempt() + ""));
                    authenticateHashmap.put("sub", StringtoRequestBody(authenticateEntity.getSub()));

                    //call authenticate call
                    callAuthenticateForFaceandVoice(baseurl, filetosend, authenticateHashmap, authenticateEntity.getVerificationType(), authenticateResult);

                }

                @Override
                public void failure(WebAuthError error) {
                    authenticateResult.failure(error);
                }
            });

        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    //-------------------------------------------Call authenticate Service-----------------------------------------------------------
    private void callAuthenticateForFaceandVoice(String baseurl, final MultipartBody.Part file, final HashMap<String, RequestBody> authenticateHashmap,
                                                 final String verificationType, final EventResult<AuthenticateResponse> authenticateResult) {
        String methodName = "AuthenticateController:-authenticate()";
        try {
            //Authenticate URL
            String authenticateUrl = VerificationURLHelper.getShared().getAuthenticateURL(baseurl, verificationType);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, null);

            //Authenticate Service call
            AuthenticateService.getShared(context).callAuthenticateServiceForFaceOrVoice(file, authenticateUrl, headers, authenticateHashmap, authenticateResult);
        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    //---------------------------------------------------String to requestBodyConversion-------------------------------------
    public RequestBody StringtoRequestBody(String value) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), value);
        return body;
    }
}
