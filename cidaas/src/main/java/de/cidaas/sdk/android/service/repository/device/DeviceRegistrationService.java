package de.cidaas.sdk.android.service.repository.device;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.cidaas.sdk.android.R;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.entities.StandardErrorEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.crypthelper.BiometricP256Signer;
import de.cidaas.sdk.android.helper.crypthelper.DpopP256Keystore;
import de.cidaas.sdk.android.helper.crypthelper.FirebaseAppAttestationHelper;
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
     * Initiates device registration, reads {@code nonce}, {@code session_id} / {@code sessionId}, and {@code provider}
     * from the initiation response, then performs attestation (Play Integrity when {@code provider} is {@code google},
     * Firebase App Check when {@code firebase}) and calls the verification API.
     *
     * @param activity hosting FragmentActivity (retained for API compatibility; verification no longer sends a separate Biometric header JWT)
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
                        dispatchRegistrationApiError(
                                WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE,
                                response,
                                callback,
                                methodName);
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
                final String provider = attestationProviderFromInitiationResponse(initiationData);
                final PlayIntegrityTokenListener afterAttestation = new PlayIntegrityTokenListener() {
                    @Override
                    public void onSuccess(String attestationToken) {
                        try {
                            final BiometricP256Signer signer = new BiometricP256Signer(context);
                            signer.ensureKey();
                            DpopP256Keystore.ensureKey(context);

                            final String verificationUrl = baseUrl.replaceAll("/$", "")
                                    + URLHelper.getShared().getDeviceRegistrationURL() + "/verification";
                            final String dpopThumb = DpopP256Keystore.jwkThumbprintSha256(context);
                            final String bioThumb = signer.jwkThumbprintSha256();
                            final String biometricPublicKeyDer = signer.publicKeyDerBase64();
                            final String dpopAttestationJwt = DpopP256Keystore.proofJwtForDeviceRegistration(
                                    context,
                                    "POST",
                                    verificationUrl,
                                    initiationData.getSessionId(),
                                    initiationData.getNonce(),
                                    attestationToken,
                                    biometricPublicKeyDer);

                            enqueueVerification(
                                    verificationUrl,
                                    headers,
                                    api,
                                    initiationData.getSessionId(),
                                    dpopAttestationJwt,
                                    dpopThumb,
                                    bioThumb,
                                    provider,
                                    callback,
                                    methodName);
                        } catch (Exception e) {
                            notifyVerificationFailure(callback, methodName, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        notifyVerificationFailure(callback, methodName, error.getMessage());
                    }
                };

                if (isGoogleProvider(provider)) {
                    PlayIntegrityHelper.requestToken(
                            context,
                            initiationData.getNonce(),
                            playIntegrityCloudProjectNumber,
                            afterAttestation);
                } else if (isFirebaseProvider(provider)) {
                    FirebaseAppAttestationHelper.requestToken(context, afterAttestation);
                } else {
                    notifyVerificationFailure(callback, methodName,
                            "Unsupported attestation provider: " + provider + " (expected google or firebase)");
                }
            } catch (Exception e) {
                notifyVerificationFailure(callback, methodName, e.getMessage());
            }
        });
    }

    /**
     * {@code provider} is read from the initiation response; defaults to {@code google} if absent (older backends).
     */
    private static String attestationProviderFromInitiationResponse(
            @NonNull DeviceRegistrationInitiationDataEntity initiationData) {
        String p = initiationData.getProvider();
        if (p == null || p.trim().isEmpty()) {
            return "google";
        }
        return p.trim();
    }

    private static boolean isGoogleProvider(@Nullable String provider) {
        return provider != null && "google".equalsIgnoreCase(provider.trim());
    }

    private static boolean isFirebaseProvider(@Nullable String provider) {
        return provider != null && "firebase".equalsIgnoreCase(provider.trim());
    }

    private void enqueueVerification(final String verificationUrl,
                                     final Map<String, String> headers,
                                     final ICidaasSDKService api,
                                     final String sessionId,
                                     final String dpopAttestationJwt,
                                     final String dpopThumb,
                                     final String bioThumb,
                                     final String attestationProvider,
                                     final EventResult<DeviceRegistrationResponseEntity> callback,
                                     final String methodName) {
        try {
            DeviceRegistrationVerificationRequestEntity body = new DeviceRegistrationVerificationRequestEntity(
                    sessionId,
                    dpopAttestationJwt,
                    dpopThumb,
                    bioThumb,
                    readAppVersion(),
                    "android",
                    attestationProvider);
            api.verifyDeviceRegistration(verificationUrl, headers, body)
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
                                dispatchRegistrationApiError(
                                        WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE,
                                        response,
                                        callback,
                                        methodName);
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
            throw new IllegalArgumentException(
                    "initiation response missing data (expected nonce, session_id/sessionId, provider)");
        }
        DeviceRegistrationInitiationDataEntity data = objectMapper.convertValue(
                initiation.getData(), DeviceRegistrationInitiationDataEntity.class);
        if (data.getSessionId() == null || data.getSessionId().isEmpty()
                || data.getNonce() == null || data.getNonce().isEmpty()) {
            throw new IllegalArgumentException(
                    "initiation response missing data.nonce and/or data.session_id (or data.sessionId)");
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

    /**
     * {@link CidaasConstants#DEVICE_ALREADY_REGISTERED_ERROR_CODE} means the device is already registered;
     * the SDK treats that as success for initiation and verification.
     */
    private void dispatchRegistrationApiError(int webAuthErrorCode,
                                              Response<DeviceRegistrationResponseEntity> response,
                                              EventResult<DeviceRegistrationResponseEntity> callback,
                                              String methodName) {
        try {
            if (response.errorBody() == null) {
                callback.failure(WebAuthError.getShared(context).emptyResponseException(
                        webAuthErrorCode, response.code(), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            String errorResponse = response.errorBody().string();
            if (isDeviceAlreadyRegisteredErrorBody(errorResponse)) {
                callback.success(deviceAlreadyRegisteredSuccessResponse(response.code()));
                return;
            }
            callback.failure(CommonError.getShared(context).generateCommonErrorEntityFromBody(
                    webAuthErrorCode, errorResponse, CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, webAuthErrorCode, e.getMessage()));
        }
    }

    private static DeviceRegistrationResponseEntity deviceAlreadyRegisteredSuccessResponse(int httpStatus) {
        DeviceRegistrationResponseEntity entity = new DeviceRegistrationResponseEntity();
        entity.setSuccess(true);
        entity.setStatus(httpStatus > 0 ? httpStatus : 200);
        return entity;
    }

    private boolean isDeviceAlreadyRegisteredErrorBody(String errorResponse) {
        if (errorResponse == null || errorResponse.isEmpty()) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(errorResponse);
            if (matchesDeviceAlreadyRegisteredCode(root.get("code"))
                    || matchesDeviceAlreadyRegisteredCode(root.get("errorcode"))
                    || matchesDeviceAlreadyRegisteredCode(root.get("errorCode"))) {
                return true;
            }
            JsonNode error = root.get("error");
            if (error != null && !error.isNull()) {
                if (error.isTextual()
                        && matchesDeviceAlreadyRegisteredCode(error.asText())) {
                    return true;
                }
                if (matchesDeviceAlreadyRegisteredCode(error.get("code"))
                        || matchesDeviceAlreadyRegisteredCode(error.get("type"))
                        || matchesDeviceAlreadyRegisteredCode(error.get("errorcode"))
                        || matchesDeviceAlreadyRegisteredCode(error.get("errorCode"))) {
                    return true;
                }
            }
            StandardErrorEntity standard = objectMapper.readValue(errorResponse, StandardErrorEntity.class);
            return matchesDeviceAlreadyRegisteredCode(standard.getCode());
        } catch (Exception ignored) {
            return errorResponse.contains(CidaasConstants.DEVICE_ALREADY_REGISTERED_ERROR_CODE);
        }
    }

    private static boolean matchesDeviceAlreadyRegisteredCode(JsonNode node) {
        return node != null && !node.isNull()
                && CidaasConstants.DEVICE_ALREADY_REGISTERED_ERROR_CODE.equalsIgnoreCase(node.asText().trim());
    }

    private static boolean matchesDeviceAlreadyRegisteredCode(String value) {
        return value != null
                && CidaasConstants.DEVICE_ALREADY_REGISTERED_ERROR_CODE.equalsIgnoreCase(value.trim());
    }
}
