package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.CreatePublicKeyCredentialResponse;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Locale;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.Fido2Entity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.setup.SetupController;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * FIDO2 / passkey MFA enrollment: POST
 * {@code /verification-actions-srv/setup/fido2/initiation} (no scan) →
 * {@link CredentialManager} {@code createCredential} with
 * {@code data.fido2_entity.server_challenge} as
 * WebAuthn {@code PublicKeyCredentialCreationOptions} JSON → POST
 * {@code .../setup/fido2/verification} with
 * {@link EnrollEntity#setAttestation(String)} set to
 * {@link CreatePublicKeyCredentialResponse#getRegistrationResponseJson()}.
 *
 * <p>Setup runs on a Retrofit background thread; Credential Manager is started on the activity main thread.
 * If you see {@code The incoming request cannot be validated}, configure Digital Asset Links on the host
 * matching {@code rp.id} in {@code server_challenge}. See {@code PASSKEYS.md} in this module and the Android
 * Credential Manager troubleshooting guide (developer.android.com identity / credential-manager-troubleshooting).</p>
 */
public final class PasskeyEnrollmentController {

    private static PasskeyEnrollmentController shared;
    private final Context context;

    private PasskeyEnrollmentController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized PasskeyEnrollmentController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new PasskeyEnrollmentController(context);
        }
        return shared;
    }

    /**
     * Runs setup initiation for {@link AuthenticationType#FIDO}, then the platform
     * passkey / WebAuthn registration
     * UI, then enroll with the returned credential JSON.
     */
    public void enrollWithPasskey(
            @NonNull final FragmentActivity activity,
            @NonNull final String sub,
            @NonNull final EventResult<EnrollResponse> callback) {
        final String methodName = "PasskeyEnrollmentController:enrollWithPasskey()";
        if (sub.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        final String verificationType = AuthenticationType.FIDO;
        SetupEntity setupEntity = new SetupEntity(sub, verificationType);
        SetupController.getShared(context).setupVerification(setupEntity, new EventResult<SetupResponse>() {
            @Override
            public void success(SetupResponse setupResult) {
                try {
                    if (setupResult == null || setupResult.getData() == null
                            || setupResult.getData().getExchange_id() == null) {
                        callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                WebAuthErrorCode.SETUP_VERIFICATION_FAILURE, 0,
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                    String setupExchangeId = setupResult.getData().getExchange_id().getExchange_id();
                    if (setupExchangeId == null || setupExchangeId.isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "exchange_id missing from setup response",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }

                    Fido2Entity fido2 = setupResult.getData().getFido2_entity();
                    if (fido2 == null || fido2.getServer_challenge() == null || fido2.getServer_challenge().isNull()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "fido2_entity.server_challenge missing from setup response",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }

                    JsonNode challenge = fido2.getServer_challenge();
                    final String requestJson = challenge.toString();
                    if (requestJson.isEmpty() || "{}".equals(requestJson.trim())) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "server_challenge JSON is empty",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }

                    final String rpId = rpIdFromServerChallenge(challenge);

                    final String authenticatorClientId = setupResult.getData().getAuthenticator_client_id();
                    final String fidoRequestId = fido2.getFidoRequestId();
                    final String statusId = setupResult.getData().getStatus_id();

                    // Retrofit delivers this callback on a background thread; Credential Manager / WebAuthn UI must
                    // be started from the main thread or the system flow may not run (and some devices report
                    // "The incoming request cannot be validated").
                    activity.runOnUiThread(() -> {
                        if (activity.isFinishing() || activity.isDestroyed()) {
                            callback.failure(WebAuthError.getShared(context).customException(
                                    WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                                    "Activity is not available for passkey UI",
                                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                            return;
                        }
                        try {
                            CreatePublicKeyCredentialRequest request = new CreatePublicKeyCredentialRequest(requestJson);
                            CredentialManager credentialManager = CredentialManager.create(activity);
                            CancellationSignal cancellationSignal = new CancellationSignal();

                            credentialManager.createCredentialAsync(
                                    activity,
                                    request,
                                    cancellationSignal,
                                    ContextCompat.getMainExecutor(activity),
                                    new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
                                        @Override
                                        public void onResult(@NonNull CreateCredentialResponse response) {
                                            if (activity.isFinishing() || activity.isDestroyed()) {
                                                return;
                                            }
                                            if (!(response instanceof CreatePublicKeyCredentialResponse)) {
                                                callback.failure(WebAuthError.getShared(context).customException(
                                                        WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                                                        "Unexpected credential response type",
                                                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                                return;
                                            }
                                            CreatePublicKeyCredentialResponse pk =
                                                    (CreatePublicKeyCredentialResponse) response;
                                            String registrationJson = pk.getRegistrationResponseJson();
                                            if (registrationJson == null || registrationJson.isEmpty()) {
                                                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                                        "Empty registrationResponseJson from passkey creation",
                                                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                                return;
                                            }

                                            EnrollEntity enrollEntity = new EnrollEntity();
                                            enrollEntity.setExchange_id(setupExchangeId);
                                            enrollEntity.setSub(sub);
                                            enrollEntity.setVerificationType(verificationType);
                                            enrollEntity.setAttestation(registrationJson);
                                            if (authenticatorClientId != null && !authenticatorClientId.isEmpty()) {
                                                enrollEntity.setAuthenticator_client_id(authenticatorClientId);
                                            }
                                            if (fidoRequestId != null && !fidoRequestId.isEmpty()) {
                                                enrollEntity.setFidoRequestId(fidoRequestId);
                                            }
                                            if (statusId != null && !statusId.isEmpty()) {
                                                enrollEntity.setStatus_id(statusId);
                                            }

                                            EnrollController.getShared(context).enrollVerification(enrollEntity, callback);
                                        }

                                        @Override
                                        public void onError(@NonNull CreateCredentialException e) {
                                            if (activity.isFinishing() || activity.isDestroyed()) {
                                                return;
                                            }
                                            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                                            String detail = passkeyErrorDetail(msg, rpId);
                                            callback.failure(WebAuthError.getShared(context).customException(
                                                    WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                                                    msg + detail,
                                                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                        }
                                    });
                        } catch (IllegalArgumentException | IllegalStateException ex) {
                            callback.failure(WebAuthError.getShared(context).customException(
                                    WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                                    ex.getMessage() != null ? ex.getMessage() : "Invalid passkey request JSON",
                                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.SETUP_VERIFICATION_FAILURE,
                            e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private static String passkeyErrorDetail(@Nullable String message, @Nullable String rpId) {
        if (message == null) {
            return "";
        }
        String m = message.toLowerCase(Locale.US);
        if (m.contains("cannot be validated") || m.contains("security_err")) {
            String host = (rpId != null && !rpId.isEmpty()) ? rpId : "the rp.id host from server_challenge";
            return " Publish https://" + host + "/.well-known/assetlinks.json with this app's package name and"
                    + " SHA-256 signing certificate (Play App Signing cert for store builds). See cidaasverification/PASSKEYS.md"
                    + " and https://developer.android.com/identity/sign-in/credential-manager-troubleshooting-guide";
        }
        return "";
    }

    /**
     * Reads {@code rp.id} from WebAuthn {@code PublicKeyCredentialCreationOptions} JSON for clearer diagnostics.
     */
    @Nullable
    private static String rpIdFromServerChallenge(@Nullable JsonNode challenge) {
        if (challenge == null || !challenge.isObject()) {
            return null;
        }
        JsonNode rp = challenge.get("rp");
        if (rp == null || !rp.isObject() || !rp.has("id") || rp.get("id").isNull()) {
            return null;
        }
        String id = rp.get("id").asText().trim();
        return id.isEmpty() ? null : id;
    }
}
