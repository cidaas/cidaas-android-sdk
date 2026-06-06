package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.scanned.ScannedController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.setup.SetupController;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.crypthelper.BiometricP256Signer;
import de.cidaas.sdk.android.helper.crypthelper.BiometricProofListener;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;

import java.util.Dictionary;

/**
 * Fingerprint MFA enrollment: {@code setup/initiate} → {@code setup/scan} → biometric Keystore proof JWT →
 * {@code setup/enroll} with {@code attestation}.
 */
public final class FingerprintAttestationEnrollmentController {

    private static FingerprintAttestationEnrollmentController shared;
    private final Context context;

    private FingerprintAttestationEnrollmentController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized FingerprintAttestationEnrollmentController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new FingerprintAttestationEnrollmentController(context);
        }
        return shared;
    }

    /**
     * Runs POST {@code /verification-srv/v2/setup/initiate/touchid/}, then {@code .../setup/scan/touchid/}, then
     * creates (if needed) an EC P-256 Keystore key bound to biometric authentication, signs a proof JWT for the
     * enroll URL, and POSTs {@code .../setup/enroll/touchid/} with {@code attestation} set to that JWT.
     */
    public void enrollWithBiometricAttestation(
            @NonNull final FragmentActivity activity,
            @NonNull final String sub,
            @NonNull final EventResult<EnrollResponse> callback) {
        final String methodName = "FingerprintAttestationEnrollmentController:enrollWithBiometricAttestation()";
        if (sub.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        final String verificationType = AuthenticationType.FINGERPRINT;
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

                    ScannedEntity scannedEntity = new ScannedEntity(sub, setupExchangeId, verificationType);
                    ScannedController.getShared(context).scannedVerification(scannedEntity, new EventResult<ScannedResponse>() {
                        @Override
                        public void success(ScannedResponse scannedResult) {
                            try {
                                if (scannedResult == null || scannedResult.getData() == null
                                        || scannedResult.getData().getExchange_id() == null) {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE, 0,
                                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                    return;
                                }
                                String scannedExchangeId = scannedResult.getData().getExchange_id().getExchange_id();
                                if (scannedExchangeId == null || scannedExchangeId.isEmpty()) {
                                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                            "exchange_id missing from scan response",
                                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                    return;
                                }

                                CidaasProperties.getShared(context).checkCidaasProperties(
                                        new EventResult<Dictionary<String, String>>() {
                                            @Override
                                            public void success(Dictionary<String, String> loginPropertiesResult) {
                                                String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                                                if (baseurl == null || baseurl.isEmpty()) {
                                                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                                            "Domain URL must not be empty",
                                                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                                    return;
                                                }
                                                String enrollUrl = VerificationURLHelper.getShared().getEnrollURL(
                                                        baseurl, verificationType);
                                                final BiometricP256Signer signer = new BiometricP256Signer(context,
                                                        BiometricP256Signer.VERIFICATION_FINGERPRINT_KEY_ALIAS);
                                                signer.ensureKey();
                                                signer.proofJwt(activity, "POST", enrollUrl, new BiometricProofListener() {
                                                    @Override
                                                    public void onSuccess(String proofJwt) {
                                                        EnrollEntity enrollEntity = new EnrollEntity();
                                                        enrollEntity.setExchange_id(scannedExchangeId);
                                                        enrollEntity.setVerificationType(verificationType);
                                                        enrollEntity.setAttestation(proofJwt);
                                                        EnrollController.getShared(context).enrollVerification(
                                                                enrollEntity, callback);
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable error) {
                                                        callback.failure(WebAuthError.getShared(context).methodException(
                                                                VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                                                                WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                                                                error != null ? error.getMessage() : "biometric proof failed"));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void failure(WebAuthError error) {
                                                callback.failure(error);
                                            }
                                        });
                            } catch (Exception e) {
                                callback.failure(WebAuthError.getShared(context).methodException(
                                        VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                                        WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE, e.getMessage()));
                            }
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callback.failure(error);
                        }
                    });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.SETUP_VERIFICATION_FAILURE, e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }
}
