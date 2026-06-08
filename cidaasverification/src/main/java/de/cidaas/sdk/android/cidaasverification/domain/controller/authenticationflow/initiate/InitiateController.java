package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.initiate;

import android.content.Context;

import java.util.Dictionary;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.domain.service.initiate.InitiateService;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;


public class InitiateController {
    //Local Variables
    private Context context;


    public static InitiateController shared;

    public InitiateController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }


    public static InitiateController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new InitiateController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("InitiateController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }


    //--------------------------------------------Initiate--------------------------------------------------------------
    public void initiateVerification(final InitiateEntity initiateEntity, final EventResult<InitiateResponse> initiateResult) {
        checkInitiateEntity(initiateEntity, initiateResult);
    }


    //-------------------------------------checkInitiateEntity-----------------------------------------------------------
    private void checkInitiateEntity(final InitiateEntity initiateEntity, final EventResult<InitiateResponse> initiateResult) {
        String methodName = "InitiateController:-checkInitiateEntity()";
        try {
            if (initiateEntity.getVerificationType() != null && !initiateEntity.getVerificationType().equals("")) {
                if (initiateEntity.getRequest_id() != null && !initiateEntity.getRequest_id().equals("") &&
                        initiateEntity.getIdentifier() != null && !initiateEntity.getIdentifier().equals("") &&
                        initiateEntity.getUsage_type() != null && !initiateEntity.getUsage_type().equals("")) {
                    addProperties(initiateEntity, initiateResult);
                } else {
                    initiateResult.failure(WebAuthError.getShared(context).propertyMissingException("requestId, identifier, or UsageType must not be null",
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }

            } else {
                initiateResult.failure(WebAuthError.getShared(context).propertyMissingException("MediumId or Verification type must not be null",
                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            initiateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.INITIATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }


    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addProperties(final InitiateEntity initiateEntity, final EventResult<InitiateResponse> initiateResult) {
        String methodName = "InitiateController:-addProperties()";
        try {
            //App properties
            DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
            initiateEntity.setDevice_id(deviceInfoEntity.getDeviceId());
            initiateEntity.setPush_id(deviceInfoEntity.getPushNotificationId());

            //call initiate call
            callInitiate(initiateEntity, initiateResult);
        } catch (Exception e) {
            initiateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.INITIATE_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-------------------------------------------Call initiate Service-----------------------------------------------------------
    private void callInitiate(final InitiateEntity initiateEntity, final EventResult<InitiateResponse> initiateResult) {
        String methodName = "InitiateController:-initiate()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);

                    String initiateUrl = VerificationURLHelper.getShared().getInitiateURL(baseurl, initiateEntity.getVerificationType());

                    //headers Generation
                    Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

                    //Initiate Service call
                    InitiateService.getShared(context).callInitiateService(initiateUrl, headers, initiateEntity, initiateResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    initiateResult.failure(error);
                }
            });
        } catch (Exception e) {
            initiateResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.INITIATE_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
}
