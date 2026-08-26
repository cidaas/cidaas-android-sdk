package de.cidaas.sdk.android.service.repository.device;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
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
import de.cidaas.sdk.android.helper.enums.HttpStatusCode;
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
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationVerificationDataEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationVerificationRequestEntity;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Device registration aligned with the iOS requestId gate:
 * <pre>
 * requestId → isDeviceRegistered?
 *   YES → generate requestId
 *   NO  → initiation
 *           200 → verification → 200 SUCCESS / 409 FAILURE
 *           409 → resolve local device_id → found SUCCESS / missing FAILURE
 *         → persist device_id → mark complete (registration lock) → generate requestId
 * </pre>
 *
 * <p>When {@code useAppAttestation} is {@code true}, initiation sends {@code push_id} and Play Integrity /
 * Firebase App Check runs from initiation {@code provider}. When {@code false}, attestation is skipped;
 * verification uses biometric + DPoP only.
 */
public class DeviceRegistrationService {

    private final CidaassdkService service;
    private final Context context;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService verificationExecutor = Executors.newSingleThreadExecutor();

    /** Serializes completion and coalesces concurrent {@link #startRegistration} callers. */
    private final Object registrationLock = new Object();
    private boolean registrationInFlight = false;
    private final List<EventResult<DeviceRegistrationResponseEntity>> pendingRegistrationCallbacks = new ArrayList<>();

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
     * Same as {@link #startRegistration(FragmentActivity, boolean, String, Long, EventResult)} without an
     * activity. Used from requestId when the device is not yet registered. App attestation is enabled
     * when a non-empty {@code pushId} is supplied.
     */
    public void startRegistration(@Nullable final String pushId,
                                  @Nullable final Long playIntegrityCloudProjectNumber,
                                  @NonNull final EventResult<DeviceRegistrationResponseEntity> callback) {
        startRegistration(null, false, pushId, playIntegrityCloudProjectNumber, callback);
    }

    /**
     * Initiates device registration then verifies with a DPoP proof that always includes biometric
     * key material (thumbprint / public key DER).
     *
     * <p>When {@code useAppAttestation} is {@code true}, {@code pushId} is required, initiation sends
     * {@code push_id}, and Play Integrity / Firebase App Check runs from initiation {@code provider}.
     * When {@code false}, {@code push_id} is omitted and app attestation is skipped.
     *
     * <p>Initiation 409 resolves the local device id; verification 409 is a failure (iOS parity).
     *
     * @param activity                        optional hosting FragmentActivity (API compatibility)
     * @param useAppAttestation               enable FCM + Google/Firebase app attestation
     * @param pushId                          FCM token; required only when {@code useAppAttestation} is true
     * @param playIntegrityCloudProjectNumber optional GCP project number for Play Integrity
     */
    public void startRegistration(@Nullable final FragmentActivity activity,
                                  final boolean useAppAttestation,
                                  @Nullable final String pushId,
                                  @Nullable final Long playIntegrityCloudProjectNumber,
                                  @NonNull final EventResult<DeviceRegistrationResponseEntity> callback) {
        final String methodName = "DeviceRegistrationService :startRegistration()";
        synchronized (registrationLock) {
            pendingRegistrationCallbacks.add(callback);
            if (registrationInFlight) {
                return;
            }
            registrationInFlight = true;
        }

        DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
        String deviceId = deviceInfo != null ? deviceInfo.getDeviceId() : null;
        if (deviceId == null || deviceId.isEmpty()) {
            finishRegistrationFailure(WebAuthError.getShared(context).propertyMissingException(
                    "device id from deviceInfo must not be empty",
                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (useAppAttestation && (pushId == null || pushId.isEmpty())) {
            finishRegistrationFailure(WebAuthError.getShared(context).propertyMissingException(
                    "pushId must not be empty when useAppAttestation is true",
                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(Dictionary<String, String> loginProperties) {
                String baseurl = loginProperties.get(CidaasConstants.DOMAIN_URL);
                if (baseurl == null || baseurl.isEmpty()) {
                    finishRegistrationFailure(WebAuthError.getShared(context).propertyMissingException(
                            context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
                String clientId = loginProperties.get(CidaasConstants.CLIENT_ID);
                if (clientId == null || clientId.isEmpty()) {
                    finishRegistrationFailure(WebAuthError.getShared(context).propertyMissingException(
                            "ClientId from saved properties must not be empty",
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
                String initiationUrl = baseurl + URLHelper.getShared().getDeviceRegistrationURL() + "/initiation";
                Map<String, String> headers = Headers.getShared(context).getHeaders(
                        null, true, URLHelper.contentTypeJson);
                String pushForRequest = useAppAttestation ? pushId : null;
                DeviceRegistrationRequestEntity body =
                        new DeviceRegistrationRequestEntity(deviceId, pushForRequest, clientId);
                enqueueInitiation(
                        baseurl,
                        initiationUrl,
                        headers,
                        body,
                        useAppAttestation,
                        playIntegrityCloudProjectNumber,
                        methodName);
            }

            @Override
            public void failure(WebAuthError error) {
                finishRegistrationFailure(WebAuthError.getShared(context).cidaasPropertyMissingException(
                        error.getErrorMessage(), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        });
    }

    private void enqueueInitiation(final String baseUrl,
                                   String url,
                                   Map<String, String> headers,
                                   DeviceRegistrationRequestEntity body,
                                   final boolean useAppAttestation,
                                   @Nullable final Long playIntegrityCloudProjectNumber,
                                   final String methodName) {
        try {
            ICidaasSDKService api = service.getInstance();
            api.initiateDeviceRegistration(url, headers, body).enqueue(new Callback<DeviceRegistrationResponseEntity>() {
                @Override
                public void onResponse(Call<DeviceRegistrationResponseEntity> call,
                                       Response<DeviceRegistrationResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == HttpStatusCode.OK || response.code() == 201) {
                            DeviceRegistrationResponseEntity entity = response.body();
                            if (entity == null) {
                                finishRegistrationFailure(WebAuthError.getShared(context).emptyResponseException(
                                        WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response.code(),
                                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                                return;
                            }
                            completeVerificationAfterInitiation(
                                    baseUrl,
                                    entity,
                                    headers,
                                    api,
                                    useAppAttestation,
                                    playIntegrityCloudProjectNumber,
                                    methodName);
                            return;
                        }
                        finishRegistrationFailure(WebAuthError.getShared(context).emptyResponseException(
                                WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, response.code(),
                                CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }

                    // 409 / VAC1177: device already on server → resolve local device_id
                    String errorBody = readErrorBody(response);
                    if (response.code() == HttpStatusCode.CONFLICT
                            || isDeviceAlreadyRegisteredErrorBody(errorBody)) {
                        handleInitiationConflict(methodName);
                        return;
                    }
                    finishRegistrationFailureFromBody(
                            WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE,
                            response.code(),
                            errorBody,
                            methodName);
                }

                @Override
                public void onFailure(Call<DeviceRegistrationResponseEntity> call, Throwable t) {
                    finishRegistrationFailure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE, t.getMessage(),
                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            finishRegistrationFailure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.DEVICE_REGISTRATION_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * Initiation 409: use local DeviceInfo.deviceId if present; otherwise fail.
     */
    private void handleInitiationConflict(String methodName) {
        String localDeviceId = resolveLocalDeviceId();
        if (localDeviceId == null || localDeviceId.isEmpty()) {
            finishRegistrationFailure(WebAuthError.getShared(context).propertyMissingException(
                    "Device already registered on server (409) but local device_id is missing",
                    CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        finishRegistrationSuccess(localDeviceId, registrationCompleteSuccessResponse(HttpStatusCode.CONFLICT));
    }

    private void completeVerificationAfterInitiation(final String baseUrl,
                                                     final DeviceRegistrationResponseEntity initiation,
                                                     final Map<String, String> headers,
                                                     final ICidaasSDKService api,
                                                     final boolean useAppAttestation,
                                                     @Nullable final Long playIntegrityCloudProjectNumber,
                                                     final String methodName) {
        verificationExecutor.execute(() -> {
            try {
                final DeviceRegistrationInitiationDataEntity initiationData = parseInitiation(initiation);

                if (!useAppAttestation) {
                    continueVerification(
                            baseUrl,
                            initiationData,
                            headers,
                            api,
                            null,
                            null,
                            methodName);
                    return;
                }

                final String provider = attestationProviderFromInitiationResponse(initiationData);
                final PlayIntegrityTokenListener afterAttestation = new PlayIntegrityTokenListener() {
                    @Override
                    public void onSuccess(String attestationToken) {
                        continueVerification(
                                baseUrl,
                                initiationData,
                                headers,
                                api,
                                provider,
                                attestationToken,
                                methodName);
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        String detail = error.getMessage();
                        if (detail == null || detail.isEmpty()) {
                            detail = String.valueOf(error);
                        }
                        notifyVerificationFailure(methodName,
                                "App attestation failed (provider=" + provider + "): " + detail);
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
                    notifyVerificationFailure(methodName,
                            "Unsupported attestation provider: " + provider + " (expected google or firebase)");
                }
            } catch (Exception e) {
                notifyVerificationFailure(methodName, e.getMessage());
            }
        });
    }

    private void continueVerification(final String baseUrl,
                                      final DeviceRegistrationInitiationDataEntity initiationData,
                                      final Map<String, String> headers,
                                      final ICidaasSDKService api,
                                      @Nullable final String provider,
                                      @Nullable final String attestationToken,
                                      final String methodName) {
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
                    methodName);
        } catch (Exception e) {
            notifyVerificationFailure(methodName, e.getMessage());
        }
    }

    /**
     * {@code provider} from initiation; defaults to {@code google} when attestation is enabled and provider is absent.
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
                                     @Nullable final String attestationProvider,
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
                            if (response.isSuccessful()) {
                                String deviceId = extractDeviceIdFromVerification(response.body());
                                if (deviceId == null || deviceId.isEmpty()) {
                                    deviceId = resolveLocalDeviceId();
                                }
                                if (deviceId == null || deviceId.isEmpty()) {
                                    finishRegistrationFailure(WebAuthError.getShared(context).propertyMissingException(
                                            "Verification succeeded but device_id is missing locally and in response",
                                            CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                                    return;
                                }
                                DeviceRegistrationResponseEntity entity = response.body();
                                if (entity == null) {
                                    entity = registrationCompleteSuccessResponse(response.code());
                                }
                                finishRegistrationSuccess(deviceId, entity);
                                return;
                            }

                            // Verification 409 = FAILURE (unlike initiation 409)
                            String errorBody = readErrorBody(response);
                            finishRegistrationFailureFromBody(
                                    WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE,
                                    response.code(),
                                    errorBody,
                                    methodName);
                        }

                        @Override
                        public void onFailure(Call<DeviceRegistrationResponseEntity> call, Throwable t) {
                            notifyVerificationFailure(methodName, t.getMessage());
                        }
                    });
        } catch (Exception e) {
            notifyVerificationFailure(methodName, e.getMessage());
        }
    }

    private DeviceRegistrationInitiationDataEntity parseInitiation(DeviceRegistrationResponseEntity initiation)
            throws IllegalArgumentException {
        if (initiation.getData() == null) {
            throw new IllegalArgumentException(
                    "initiation response missing data (expected nonce, session_id/sessionId)");
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

    @Nullable
    private String extractDeviceIdFromVerification(@Nullable DeviceRegistrationResponseEntity response) {
        try {
            if (response == null || response.getData() == null) {
                return null;
            }
            DeviceRegistrationVerificationDataEntity data = objectMapper.convertValue(
                    response.getData(), DeviceRegistrationVerificationDataEntity.class);
            if (data == null || data.getDeviceId() == null || data.getDeviceId().trim().isEmpty()) {
                return null;
            }
            return data.getDeviceId().trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private String resolveLocalDeviceId() {
        try {
            DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
            if (deviceInfo == null) {
                return null;
            }
            String deviceId = deviceInfo.getDeviceId();
            return deviceId != null && !deviceId.trim().isEmpty() ? deviceId.trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Persist device_id into DeviceInfo (same store initiate/authenticate/continue use), then mark registered.
     * Must be called while holding {@link #registrationLock}.
     */
    private void persistDeviceIdAndMarkComplete(@NonNull String deviceId) {
        DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfoEntity();
        }
        deviceInfo.setDeviceId(deviceId);
        DBHelper.getShared().addDeviceInfo(deviceInfo);
        DBHelper.getShared().setDeviceRegistered(true);
    }

    private void finishRegistrationSuccess(@NonNull String deviceId,
                                           @NonNull DeviceRegistrationResponseEntity entity) {
        List<EventResult<DeviceRegistrationResponseEntity>> waiters;
        synchronized (registrationLock) {
            persistDeviceIdAndMarkComplete(deviceId);
            registrationInFlight = false;
            waiters = new ArrayList<>(pendingRegistrationCallbacks);
            pendingRegistrationCallbacks.clear();
        }
        for (EventResult<DeviceRegistrationResponseEntity> waiter : waiters) {
            waiter.success(entity);
        }
    }

    private void finishRegistrationFailure(@NonNull WebAuthError error) {
        List<EventResult<DeviceRegistrationResponseEntity>> waiters;
        synchronized (registrationLock) {
            registrationInFlight = false;
            waiters = new ArrayList<>(pendingRegistrationCallbacks);
            pendingRegistrationCallbacks.clear();
        }
        for (EventResult<DeviceRegistrationResponseEntity> waiter : waiters) {
            waiter.failure(error);
        }
    }

    private void notifyVerificationFailure(String methodName, String message) {
        finishRegistrationFailure(WebAuthError.getShared(context).serviceCallFailureException(
                WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE,
                message,
                CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
    }

    private void finishRegistrationFailureFromBody(int webAuthErrorCode,
                                                   int httpCode,
                                                   @Nullable String errorBody,
                                                   String methodName) {
        if (errorBody == null || errorBody.isEmpty()) {
            finishRegistrationFailure(WebAuthError.getShared(context).emptyResponseException(
                    webAuthErrorCode, httpCode, CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        finishRegistrationFailure(CommonError.getShared(context).generateCommonErrorEntityFromBody(
                webAuthErrorCode, errorBody, CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
    }

    @Nullable
    private static String readErrorBody(Response<?> response) {
        try {
            if (response.errorBody() == null) {
                return null;
            }
            return response.errorBody().string();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static DeviceRegistrationResponseEntity registrationCompleteSuccessResponse(int httpStatus) {
        DeviceRegistrationResponseEntity entity = new DeviceRegistrationResponseEntity();
        entity.setSuccess(true);
        entity.setStatus(httpStatus > 0 ? httpStatus : HttpStatusCode.OK);
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

    private String readAppVersion() {
        try {
            PackageManager pm = context.getPackageManager();
            String version = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            return version != null ? version : "unknown";
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }
}
