package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.domain.helper.biometrichandler.BiometricHandler;
import de.cidaas.sdk.android.cidaasverification.domain.service.enroll.EnrollService;
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

public class EnrollController {
    //Local Variables
    private Context context;


    public static EnrollController shared;

    public EnrollController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }


    public static EnrollController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new EnrollController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("EnrollController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }


    //--------------------------------------------Enroll--------------------------------------------------------------
    public void enrollVerification(final EnrollEntity enrollEntity, final EventResult<EnrollResponse> enrollResult) {
        checkEnrollEntity(enrollEntity, enrollResult);
    }


    //-------------------------------------checkEnrollEntity-----------------------------------------------------------
    private void checkEnrollEntity(final EnrollEntity enrollEntity, final EventResult<EnrollResponse> enrollResult) {
        String methodName = "EnrollController:-checkEnrollEntity()";
        try {
            if (enrollEntity.getVerificationType() != null && !enrollEntity.getVerificationType().equals("") &&
                    enrollEntity.getExchange_id() != null && !enrollEntity.getExchange_id().equals("")) {
                handleVerificationTypes(enrollEntity, enrollResult);
            } else {
                enrollResult.failure(WebAuthError.getShared(context).propertyMissingException("Verification type or ExchangeId must not be empty",
                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-----------------------------------------------handleVerificationTypes---------------------------------------------------------------
    private void handleVerificationTypes(EnrollEntity enrollEntity, EventResult<EnrollResponse> enrollResult) {
        //Handle if Passcode is send for finger
        String methodName = "EnrollController:-handleVerificationTypes()";
        try {

            switch (enrollEntity.getVerificationType()) {

                case AuthenticationType.FINGERPRINT: {
                    if (enrollEntity.getAttestation() != null && !enrollEntity.getAttestation().isEmpty()) {
                        addProperties(enrollEntity, enrollResult);
                    } else {
                        callFingerPrintAuthentication(enrollEntity, enrollResult);
                    }
                    break;
                }
                case AuthenticationType.FIDO: {
                    if (enrollEntity.getAttestation() != null && !enrollEntity.getAttestation().isEmpty()) {
                        addProperties(enrollEntity, enrollResult);
                    } else {
                        enrollResult.failure(WebAuthError.getShared(context).propertyMissingException(
                                "attestation (WebAuthn registration JSON) must not be empty for FIDO2 / passkey enroll",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                    break;
                }
                case AuthenticationType.FACE: {

                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), enrollEntity.getFileToSend());
                    MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", "de.cidaas.png", requestFile);

                    addPropertiesForFaceOrVoice(photo, enrollEntity, enrollResult);
                    break;
                }
                case AuthenticationType.VOICE: {

                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), enrollEntity.getFileToSend());
                    MultipartBody.Part voice = MultipartBody.Part.createFormData("voice", "Audio.fav", requestFile);

                    addPropertiesForFaceOrVoice(voice, enrollEntity, enrollResult);
                    break;
                }
                default: {
                    if (enrollEntity.getPass_code() != null && !enrollEntity.getPass_code().equals("")) {

                        addProperties(enrollEntity, enrollResult);
                    } else {
                        enrollResult.failure(WebAuthError.getShared(context).propertyMissingException("Passcode must not be empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                }
            }

        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }


    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void callFingerPrintAuthentication(final EnrollEntity enrollEntity, final EventResult<EnrollResponse> enrollResult) {
        String methodName = "EnrollController:-callFingerPrintAuthentication()";
        try {
            BiometricHandler biometricHandler = new BiometricHandler(enrollEntity.getFingerPrintEntity().getContext());

            biometricHandler.callFingerPrint(enrollEntity.getFingerPrintEntity(), methodName, new EventResult<String>() {
                @Override
                public void success(String result) {
                    //Todo Check set pass code as device id and call enroll call

                    addProperties(enrollEntity, enrollResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    enrollResult.failure(error);
                }
            });

        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }


    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addProperties(final EnrollEntity enrollEntity, final EventResult<EnrollResponse> enrollResult) {
        String methodName = "EnrollController:-addProperties()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                    String clientId = loginPropertiesResult.get(VerificationConstants.CLIENT_ID);

                    //App properties
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
                    enrollEntity.setDevice_id(deviceInfoEntity.getDeviceId());
                    enrollEntity.setPush_id(deviceInfoEntity.getPushNotificationId());
                    enrollEntity.setClient_id(clientId);

                    final String sub = enrollEntity.getSub();
                    if (sub != null && !sub.trim().isEmpty()) {
                        AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
                            @Override
                            public void success(AccessTokenEntity accessTokenresult) {
                                String token = accessTokenresult.getAccess_token();
                                callEnroll(baseurl, enrollEntity, token, enrollResult);
                            }

                            @Override
                            public void failure(WebAuthError error) {
                                enrollResult.failure(error);
                            }
                        });
                    } else {
                        callEnroll(baseurl, enrollEntity, null, enrollResult);
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    enrollResult.failure(error);
                }
            });


        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }


    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addPropertiesForFaceOrVoice(final MultipartBody.Part filetosend, final EnrollEntity enrollEntity, final EventResult<EnrollResponse> enrollResult) {
        String methodName = "EnrollController:-addPropertiesForFaceOrVoice()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get("DomainURL");
                    String clientId = loginPropertiesResult.get("ClientId");

                    //Change To Hashmap and Add Properties
                    HashMap<String, RequestBody> enrollHashmap = new HashMap<>();
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();

                    //Optional Sub
                    //  enrollHashmap.put("",StringtoRequestBody(enrollEntity.))
                    enrollHashmap.put("exchange_id", StringtoRequestBody(enrollEntity.getExchange_id()));
                    enrollHashmap.put("device_id", StringtoRequestBody(deviceInfoEntity.getDeviceId()));
                    enrollHashmap.put("client_id", StringtoRequestBody(clientId));
                    enrollHashmap.put("push_id", StringtoRequestBody(deviceInfoEntity.getPushNotificationId()));
                    enrollHashmap.put("face_attempt", StringtoRequestBody("" + enrollEntity.getAttempt() + ""));


                    //call enroll call
                    callEnrollForFaceandVoice(baseurl, filetosend, enrollHashmap, enrollEntity.getVerificationType(), enrollResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    enrollResult.failure(error);
                }
            });


        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-------------------------------------------Call enroll Service-----------------------------------------------------------
    private void callEnroll(
            String baseurl,
            final EnrollEntity enrollEntity,
            @Nullable String accessToken,
            final EventResult<EnrollResponse> enrollResult) {
        String methodName = "EnrollController:-enroll()";
        try {
            String enrollUrl = VerificationURLHelper.getShared().getEnrollURL(baseurl, enrollEntity.getVerificationType());

            //headers Generation — match setup: send access_token when enrolling for a known sub (e.g. passkey, email OTP)
            Map<String, String> headers = Headers.getShared(context).getHeaders(accessToken, false, URLHelper.contentTypeJson);

            //Enroll Service call
            EnrollService.getShared(context).callEnrollService(enrollUrl, headers, enrollEntity, enrollResult);

        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-------------------------------------------Call enroll Service-----------------------------------------------------------
    private void callEnrollForFaceandVoice(String baseurl, final MultipartBody.Part file, final HashMap<String, RequestBody> enrollHashmap,
                                           final String verificationType, final EventResult<EnrollResponse> enrollResult) {
        String methodName = "EnrollController:-enroll()";
        try {
            String enrollUrl = VerificationURLHelper.getShared().getEnrollURL(baseurl, verificationType);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, null);

            //Enroll Service call
            EnrollService.getShared(context).callEnrollServiceForFaceOrVoice(file, enrollUrl, headers, enrollHashmap, enrollResult);

        } catch (Exception e) {
            enrollResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //---------------------------------------------------String to requestBodyConversion-------------------------------------
    public RequestBody StringtoRequestBody(String value) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), value);
        return body;
    }

}
