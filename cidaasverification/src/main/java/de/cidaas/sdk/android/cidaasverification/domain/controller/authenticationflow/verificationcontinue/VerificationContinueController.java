package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.verificationcontinue;

import android.content.Context;

import java.util.Dictionary;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinue;
import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinueResponseEntity;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.domain.service.verificationcontinue.VerificationContinueService;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.UsageType;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;


public class VerificationContinueController {
    //Local Variables
    private Context context;


    public static VerificationContinueController shared;

    public VerificationContinueController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }


    public static VerificationContinueController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new VerificationContinueController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("VerificationContinueController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }

    //--------------------------------------------VerificationContinue--------------------------------------------------------------
    public void verificationContinue(final VerificationContinue verificationContinueEntity, final EventResult<LoginCredentialsResponseEntity> verificationContinueResponseResult) {
        checkVerificationContinueEntity(verificationContinueEntity, verificationContinueResponseResult);
    }


    //-------------------------------------checkVerificationContinueEntity-----------------------------------------------------------
    private void checkVerificationContinueEntity(final VerificationContinue verificationContinueEntity, final EventResult<LoginCredentialsResponseEntity> VerificationContinueResult) {
        String methodName = "VerificationContinueController:-checkVerificationContinueEntity()";
        try {
            if (verificationContinueEntity.getVerificationType() != null && !verificationContinueEntity.getVerificationType().equals("") &&
                    verificationContinueEntity.getSub() != null && !verificationContinueEntity.getSub().equals("")) {

                if (verificationContinueEntity.getStatus_id() == null || verificationContinueEntity.getStatus_id().equals("")
                        && verificationContinueEntity.getRequestId() == null || verificationContinueEntity.getRequestId().equals("")) {
                    VerificationContinueResult.failure(WebAuthError.getShared(context).propertyMissingException("Status_id or RequestId must not be null",
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }

                if (verificationContinueEntity.getUsageType().equals(UsageType.MFA)) {
                    if (verificationContinueEntity.getTrackId() == null || verificationContinueEntity.getTrackId().equals("")) {
                        VerificationContinueResult.failure(WebAuthError.getShared(context).propertyMissingException("TrackId must not be null",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                }

                addProperties(verificationContinueEntity, VerificationContinueResult);
            } else {
                VerificationContinueResult.failure(WebAuthError.getShared(context).propertyMissingException("VerificationType or Sub must not be null",
                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
        } catch (Exception e) {
            VerificationContinueResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }


    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addProperties(final VerificationContinue verificationContinueEntity, final EventResult<LoginCredentialsResponseEntity> verificationContinueResponseResult) {
        String methodName = "VerificationContinueController:-addProperties()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(final Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get("DomainURL");

                    if (verificationContinueEntity.isUseVerificationLoginPath()) {
                        String verificationContinueUrl =
                                VerificationURLHelper.getShared().getVerificationLoginUrl(baseurl);
                        callVerificationContinue(verificationContinueUrl, verificationContinueEntity, verificationContinueResponseResult);
                    } else if (verificationContinueEntity.getUsageType().equals(UsageType.MFA)) {
                        //call VerificationContinue call
                        String verificationContinueUrl = VerificationURLHelper.getShared().getMfaContinueCallUrl(baseurl, verificationContinueEntity.getTrackId());
                        callVerificationContinue(verificationContinueUrl, verificationContinueEntity, verificationContinueResponseResult);
                    } else if (verificationContinueEntity.getUsageType().equals(UsageType.PASSWORDLESS)) {
                        String verificationContinueUrl = VerificationURLHelper.getShared().getPasswordlessContinueUrl(baseurl);
                        callVerificationContinue(verificationContinueUrl, verificationContinueEntity, verificationContinueResponseResult);
                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    verificationContinueResponseResult.failure(error);
                }
            });
        } catch (Exception e) {
            verificationContinueResponseResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.RESUME_LOGIN_FAILURE, e.getMessage()));
        }
    }

    //-------------------------------------------Call VerificationContinue Service-----------------------------------------------------------
    private void callVerificationContinue(String resumeURL, final VerificationContinue verificationContinueEntity, final EventResult<LoginCredentialsResponseEntity> verificationContinueResult) {
        String methodName = "VerificationContinueController:-callPasswordlessVerificationContinue()";
        try {
            //App properties
            DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
            verificationContinueEntity.setDevice_id(deviceInfoEntity.getDeviceId());
            verificationContinueEntity.setPush_id(deviceInfoEntity.getPushNotificationId());

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //VerificationContinue Service call
            VerificationContinueService.getShared(context).callVerificationContinueService(resumeURL, headers, verificationContinueEntity, new EventResult<VerificationContinueResponseEntity>() {
                @Override
                public void success(VerificationContinueResponseEntity result) {
                    getAccessTokenFromCode(result, verificationContinueResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    verificationContinueResult.failure(error);
                }
            });
        } catch (Exception e) {
            verificationContinueResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    public void getAccessTokenFromCode(VerificationContinueResponseEntity result, final EventResult<LoginCredentialsResponseEntity> verificationContinueResult) {
        String methodName = "VerificationContinueController:-getAccessTokenFromCode()";
        try {
            if (result == null || result.getData() == null) {
                verificationContinueResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Login continue response missing data", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            String code = result.getData().getCode();
            if (code == null || code.isEmpty()) {
                verificationContinueResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Login continue response missing authorization code", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            AccessTokenController.getShared(context).getAccessTokenByCode(code, new EventResult<AccessTokenEntity>() {
                @Override
                public void success(AccessTokenEntity accessTokenresult) {
                    LoginCredentialsResponseEntity loginCredentialsResponseEntity = new LoginCredentialsResponseEntity();
                    loginCredentialsResponseEntity.setData(accessTokenresult);
                    loginCredentialsResponseEntity.setStatus(200);
                    loginCredentialsResponseEntity.setSuccess(true);
                    verificationContinueResult.success(loginCredentialsResponseEntity);
                }

                @Override
                public void failure(WebAuthError error) {
                    verificationContinueResult.failure(error);
                }
            });
        } catch (Exception e) {
            verificationContinueResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

}
