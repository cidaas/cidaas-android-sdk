package de.cidaas.sdk.android.cidaasnative.domain.controller.requestid;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import de.cidaas.sdk.android.cidaasnative.data.entity.authrequest.AuthRequestResponseEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.RequestId.RequestIdService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.pkce.OAuthChallengeGenerator;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationResponseEntity;
import de.cidaas.sdk.android.service.repository.device.DeviceRegistrationService;
import timber.log.Timber;


public class RequestIdController {

    private String statusId;
    private String authenticationType;
    private String sub;
    private String verificationType;
    private Context context;

    public static RequestIdController shared;

    public RequestIdController(Context contextFromCidaas) {
        sub = "";
        statusId = "";
        verificationType = "";
        authenticationType = "";
        context = contextFromCidaas;

    }

    String codeVerifier = "";
    String codeChallenge = "";
    String clientSecret = "";

    // Generate Code Challenge and Code verifier
    public void generateChallenge() {

        OAuthChallengeGenerator generator = new OAuthChallengeGenerator();

        Dictionary<String, String> savedProperties = new Hashtable<>();

        codeVerifier = generator.getCodeVerifier();
        codeChallenge = generator.getCodeChallenge(codeVerifier);

        savedProperties.put("Verifier", codeVerifier);
        savedProperties.put("Challenge", codeChallenge);
        savedProperties.put("Method", generator.codeChallengeMethod);

      /*  if(!ENABLE_PKCE && !clientSecret.equals("") && clientSecret!=null)
        {
            savedProperties.put(NativeConstants.CLIENT_SECRET,clientSecret);
        }
*/
        DBHelper.getShared().addChallengeProperties(savedProperties);
    }

    public static RequestIdController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new RequestIdController(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    //Service call for RequestID
    public void getRequestId(final Dictionary<String, String> loginproperties, final EventResult<AuthRequestResponseEntity> Primaryresult, @Nullable HashMap<String, String>... extraParams) {
        String methodName = "RequestIdController :getRequestId()";
        try {

            //Check all the login Properties are Correct
            if (checkNotnull(loginproperties, Primaryresult)) {
                return;
            }

            //Check for Code challenge and code Verifier
            if (codeChallenge.equals("") && codeVerifier.equals("")) {  //&& Cidaas.ENABLE_PKCE
                generateChallenge();
            }

            CidaasHelper.baseurl = loginproperties.get(NativeConstants.DOMAIN_URL);
            DBHelper.getShared().addLoginProperties(loginproperties);
            ensureDeviceRegisteredThenGetRequestId(loginproperties, Primaryresult, extraParams);
        } catch (Exception e) {
            Primaryresult.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * If the device is already registered, proceed to requestId (headers use DeviceInfo deviceId as
     * {@code cidaas_dr}). Otherwise run device registration (iOS-aligned initiation/verification),
     * then requestId.
     */
    private void ensureDeviceRegisteredThenGetRequestId(final Dictionary<String, String> loginproperties,
                                                        final EventResult<AuthRequestResponseEntity> Primaryresult,
                                                        @Nullable final HashMap<String, String>... extraParams) {
           if (DBHelper.getShared().isDeviceRegistered()) {
            RequestIdService.getShared(context).getRequestID(loginproperties, Primaryresult, extraParams);
            return;
        }

        // Optional attestation:register with biometric + DPoP only.
        DeviceRegistrationService.getShared(context).startRegistration(null, null,
                new EventResult<DeviceRegistrationResponseEntity>() {
                    @Override
                    public void success(DeviceRegistrationResponseEntity result) {
                        RequestIdService.getShared(context).getRequestID(loginproperties, Primaryresult, extraParams);
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        Primaryresult.failure(error);
                    }
                });
    }

    private boolean checkNotnull(Dictionary<String, String> loginproperties, EventResult<AuthRequestResponseEntity> Primaryresult) {
        String methodName = "RequestIdController:checkNotnull() ";
        if (loginproperties.get(NativeConstants.DOMAIN_URL) == null || loginproperties.get(NativeConstants.DOMAIN_URL).equals("")
                || !((Hashtable) loginproperties).containsKey(NativeConstants.DOMAIN_URL)) {
            Primaryresult.failure(CidaasProperties.getShared(context).getAuthError("DomainURL must not be null", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            return true;
        }
        if (loginproperties.get(NativeConstants.CLIENT_ID) == null || loginproperties.get(NativeConstants.CLIENT_ID).equals("")
                || !((Hashtable) loginproperties).containsKey(NativeConstants.CLIENT_ID)) {
            Primaryresult.failure(CidaasProperties.getShared(context).getAuthError("ClientId must not be null", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            return true;
        }
        if (!((Hashtable) loginproperties).containsKey("RedirectURL") || null == loginproperties.get("RedirectURL")
                || loginproperties.get("RedirectURL").equals("")) {
            Primaryresult.failure(CidaasProperties.getShared(context).getAuthError("RedirectURL must not be null", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            return true;
        }

        CidaasHelper.ENABLE_PKCE = DBHelper.getShared().getEnablePKCE();
        if (!CidaasHelper.ENABLE_PKCE) {
            if (loginproperties.get(NativeConstants.CLIENT_SECRET) == null || loginproperties.get(NativeConstants.CLIENT_SECRET).equals("") || loginproperties == null
                    || !((Hashtable) loginproperties).containsKey(NativeConstants.CLIENT_SECRET)) {
                Primaryresult.failure(CidaasProperties.getShared(context).getAuthError("PKCE flow is disabled ,ClientSecret must not be null",
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return true;
            } else {
                clientSecret = loginproperties.get(NativeConstants.CLIENT_SECRET);
            }
        }
        return false;
    }
}
