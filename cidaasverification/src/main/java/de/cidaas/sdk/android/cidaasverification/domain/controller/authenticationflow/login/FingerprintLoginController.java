package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.crypthelper.BiometricP256Signer;
import de.cidaas.sdk.android.helper.crypthelper.BiometricProofListener;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;

/**
 * Fingerprint (touchid) login after initiate: {@code push_acknowledge/touchid} → {@code allow/touchid} →
 * Keystore EC P-256 proof JWT as {@code attestation} on {@code authenticate/touchid}.
 */
public final class FingerprintLoginController {

    private static FingerprintLoginController shared;
    private final Context context;

    private FingerprintLoginController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized FingerprintLoginController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new FingerprintLoginController(context);
        }
        return shared;
    }

    /**
     * After fingerprint initiate: push acknowledge / allow for {@code TOUCHID}, then {@link BiometricP256Signer}
     * proof JWT over the authenticate URL, then v2 authenticate (authenticate only — call login continue next).
     */
    public void authenticateWithBiometricAttestationAfterPush(
            @NonNull final FragmentActivity activity,
            @NonNull final String initiateExchangeId,
            @NonNull final EventResult<AuthenticateResponse> callback) {
        final String methodName = "FingerprintLoginController:authenticateWithBiometricAttestationAfterPush()";
        if (initiateExchangeId == null || initiateExchangeId.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        AuthenticatePushAcknowledgeAllowHelper.run(
                context,
                AuthenticationType.FINGERPRINT,
                initiateExchangeId,
                new EventResult<String>() {
                    @Override
                    public void success(final String finalExchangeId) {
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
                                        String authenticateUrl = VerificationURLHelper.getShared().getAuthenticateURL(
                                                baseurl, AuthenticationType.FINGERPRINT);
                                        final BiometricP256Signer signer = new BiometricP256Signer(context,
                                                BiometricP256Signer.VERIFICATION_FINGERPRINT_KEY_ALIAS);
                                        signer.ensureKey();
                                        signer.proofJwt(activity, "POST", authenticateUrl, new BiometricProofListener() {
                                            @Override
                                            public void onSuccess(String proofJwt) {
                                                AuthenticateEntity authenticateEntity = new AuthenticateEntity();
                                                authenticateEntity.setExchange_id(finalExchangeId);
                                                authenticateEntity.setVerificationType(AuthenticationType.FINGERPRINT);
                                                authenticateEntity.setAttestation(proofJwt);
                                                PasswordlessLoginController.getShared(context).authenticateVerificationOnly(
                                                        authenticateEntity, callback);
                                            }

                                            @Override
                                            public void onFailure(Throwable error) {
                                                callback.failure(WebAuthError.getShared(context).methodException(
                                                        VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                                                        WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE,
                                                        error != null ? error.getMessage() : "biometric proof failed"));
                                            }
                                        });
                                    }

                                    @Override
                                    public void failure(WebAuthError error) {
                                        callback.failure(error);
                                    }
                                });
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        callback.failure(error);
                    }
                });
    }
}
