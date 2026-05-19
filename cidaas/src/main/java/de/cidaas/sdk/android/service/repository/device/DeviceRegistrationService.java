package de.cidaas.sdk.android.service.repository.device;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.cidaas.sdk.android.R;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.crypthelper.BiometricP256Signer;
import de.cidaas.sdk.android.helper.crypthelper.BiometricProofListener;
import de.cidaas.sdk.android.helper.crypthelper.DpopP256Keystore;
import de.cidaas.sdk.android.helper.crypthelper.PlayIntegrityHelper;
import de.cidaas.sdk.android.helper.crypthelper.PlayIntegrityTokenListener;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.CidaassdkService;
import de.cidaas.sdk.android.service.ICidaasSDKService;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationInitiationDataEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationRequestEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationResponseEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationVerificationRequestEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class DeviceRegistrationService {

    private final CidaassdkService service;
    private final Context context;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService verificationExecutor = Executors.newSingleThreadExecutor();

    private static DeviceRegistrationService shared;

    public DeviceRegistrationService(Context contextFromCidaas) {
        context = contextFromCidaas;
        service = new CidaassdkService(context);
    }

    public static DeviceRegistrationService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new DeviceRegistrationService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    /**
     * Initiates device registration, then performs Play Integrity attestation and calls the verification API.
     *
     * @param activity hosting FragmentActivity for biometric proof JWT signing
     * @param pushId   FCM push token
     * @param playIntegrityCloudProjectNumber optional GCP project number linked in Play Console; when null,
     *                                        read from manifest meta-data {@link CidaasConstants#PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER}
     */
    public void startRegistration(@NonNull final FragmentActivity activity,
                                  @NonNull final String pushId,
                                  @Nullable final Long playIntegrityCloudProjectNumber,
                                  @NonNull final EventResult<DeviceRegistrationResponseEntity> callback) {
        final String methodName = "DeviceRegistrationService :startRegistration()";
        DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
        String deviceId = deviceInfo != null ? deviceInfo.getDeviceId() : null;
        if (deviceId == null || deviceId.isEmpty()
                || pushId == null || pushId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "device id from deviceInfo and pushId must not be empty",
                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(Dictionary<String, String> loginProperties) {
                String baseurl = loginProperties.get(CidaasConstants.DOMAIN_URL);
                if (baseurl == null || baseurl.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            context.getString(R.string.EMPTY_BASE_URL_SERVICE), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
                String clientId = loginProperties.get(CidaasConstants.CLIENT_ID);
                if (clientId == null || clientId.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            "ClientId from saved properties must not be empty",
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
                String initiationUrl = baseurl + URLHelper.getShared().getDeviceRegistrationURL() + "/initiation";
                Map<String, String> headers = Headers.getShared(context).getHeaders(
                        null, true, URLHelper.contentTypeJson);
                DeviceRegistrationRequestEntity body = new DeviceRegistrationRequestEntity(deviceId, pushId, clientId);
                enqueueInitiation(activity, baseurl, initiationUrl, headers, body, playIntegrityCloudProjectNumber, callback, methodName);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(WebAuthError.getShared(context).cidaasPropertyMissingException(
                        error.getErrorMessage(), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        });
    }

    private void enqueueInitiation(final FragmentActivity activity,
                                   final String baseUrl,
                                   String url,
                                   Map<String, String> headers,
                                   DeviceRegistrationRequestEntity body,
                                   @Nullable final Long playIntegrityCloudProjectNumber,
                                   final EventResult<DeviceRegistrationResponseEntity> callback,
                                   final String methodName) {
        try {
            ICidaasSDKService api = service.getInstance();
            api.initiateDeviceRegistration(url, headers, body).enqueue(new Callback<DeviceRegistrationResponseEntity>() {
                @Override
                public void onResponse(Call<DeviceRegistrationResponseEntity> call,
                                       Response<DeviceRegistrationResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200 || response.code() == 201) {
                            DeviceRegistrationResponseEntity entity = response.body();
                            if (entity == null) {
                                callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                        WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response.code(),
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                                return;
                            }
                            completeVerificationAfterInitiation(
                                    activity,
                                    baseUrl,
                                    entity,
                                    headers,
                                    api,
                                    playIntegrityCloudProjectNumber,
                                    callback,
                                    methodName);
                        } else if (response.code() == 204) {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                    WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response.code(),
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                    WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response.code(),
                                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response,
                                "Error :DeviceRegistrationService :startRegistration()"));
                    }
                }

                @Override
                public void onFailure(Call<DeviceRegistrationResponseEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, t.getMessage(),
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void completeVerificationAfterInitiation(final FragmentActivity activity,
                                                     final String baseUrl,
                                                     final DeviceRegistrationResponseEntity initiation,
                                                     final Map<String, String> headers,
                                                     final ICidaasSDKService api,
                                                     @Nullable final Long playIntegrityCloudProjectNumber,
                                                     final EventResult<DeviceRegistrationResponseEntity> callback,
                                                     final String methodName) {
        verificationExecutor.execute(() -> {
            try {
                final DeviceRegistrationInitiationDataEntity initiationData = parseInitiation(initiation);
                final byte[] challengeBytes = BiometricP256Signer.decodeChallengeB64(initiationData.getNonce());

                PlayIntegrityHelper.requestToken(
                        context,
                        challengeBytes,
                        playIntegrityCloudProjectNumber,
                        new PlayIntegrityTokenListener() {
                            @Override
                            public void onSuccess(String playToken) {
                                try {
                                    final BiometricP256Signer signer = new BiometricP256Signer(context);
                                    signer.ensureKey();
                                    DpopP256Keystore.ensureKey(context);

                                    final String verificationUrl = baseUrl.replaceAll("/$", "")
                                            + URLHelper.getShared().getDeviceRegistrationURL() + "/verification";
                                    final String dpopThumb = DpopP256Keystore.jwkThumbprintSha256(context);
                                    final String bioThumb = signer.jwkThumbprintSha256();
                                    final String dpopProof = DpopP256Keystore.proofJwt(
                                            context, "POST", verificationUrl);

                                    signer.proofJwt(activity, "POST", verificationUrl, new BiometricProofListener() {
                                        @Override
                                        public void onSuccess(String bioProof) {
                                            verificationExecutor.execute(() -> enqueueVerification(
                                                    verificationUrl,
                                                    headers,
                                                    api,
                                                    initiationData.getSessionId(),
                                                    playToken,
                                                    dpopThumb,
                                                    bioThumb,
                                                    dpopProof,
                                                    bioProof,
                                                    callback,
                                                    methodName));
                                        }

                                        @Override
                                        public void onFailure(Throwable error) {
                                            notifyVerificationFailure(callback, methodName, error.getMessage());
                                        }
                                    });
                                } catch (Exception e) {
                                    notifyVerificationFailure(callback, methodName, e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                notifyVerificationFailure(callback, methodName, error.getMessage());
                            }
                        });
            } catch (Exception e) {
                notifyVerificationFailure(callback, methodName, e.getMessage());
            }
        });
    }

    private void enqueueVerification(final String verificationUrl,
                                     final Map<String, String> headers,
                                     final ICidaasSDKService api,
                                     final String sessionId,
                                     final String playToken,
                                     final String dpopThumb,
                                     final String bioThumb,
                                     final String dpopProof,
                                     final String bioProof,
                                     final EventResult<DeviceRegistrationResponseEntity> callback,
                                     final String methodName) {
        try {
            DeviceRegistrationVerificationRequestEntity body = new DeviceRegistrationVerificationRequestEntity(
                    sessionId,
                    playToken,
                    dpopThumb,
                    bioThumb,
                    readAppVersion(),
                    "android");
            api.verifyDeviceRegistration(verificationUrl, headers, dpopProof, bioProof, body)
                    .enqueue(new Callback<DeviceRegistrationResponseEntity>() {
                        @Override
                        public void onResponse(Call<DeviceRegistrationResponseEntity> call,
                                               Response<DeviceRegistrationResponseEntity> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                callback.success(response.body());
                            } else if (response.isSuccessful()) {
                                DeviceRegistrationResponseEntity entity = new DeviceRegistrationResponseEntity();
                                entity.setSuccess(true);
                                entity.setStatus(response.code());
                                callback.success(entity);
                            } else {
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                        WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE, response,
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<DeviceRegistrationResponseEntity> call, Throwable t) {
                            notifyVerificationFailure(callback, methodName, t.getMessage());
                        }
                    });
        } catch (Exception e) {
            notifyVerificationFailure(callback, methodName, e.getMessage());
        }
    }

    private DeviceRegistrationInitiationDataEntity parseInitiation(DeviceRegistrationResponseEntity initiation)
            throws IllegalArgumentException {
        if (initiation.getData() == null) {
            throw new IllegalArgumentException("initiation response missing data.session_id and data.nonce");
        }
        DeviceRegistrationInitiationDataEntity data = objectMapper.convertValue(
                initiation.getData(), DeviceRegistrationInitiationDataEntity.class);
        if (data.getSessionId() == null || data.getSessionId().isEmpty()
                || data.getNonce() == null || data.getNonce().isEmpty()) {
            throw new IllegalArgumentException("initiation response missing data.session_id and data.nonce");
        }
        return data;
    }

    private String readAppVersion() {
        try {
            PackageManager pm = context.getPackageManager();
            String version = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            return version != null ? version : "unknown";
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    private void notifyVerificationFailure(EventResult<DeviceRegistrationResponseEntity> callback,
                                           String methodName,
                                           String message) {
        callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE,
                message,
                CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
    }
}
