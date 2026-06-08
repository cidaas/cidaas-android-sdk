package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * <strong>Legacy</strong> multi-step fingerprint (touchid) login. Preferred:
 * {@link VerificationLogin#fingerprint(Object, EventResult) cidaas.verifications().login().fingerprint(loginRequest, callback)}.
 * <ol>
 *   <li>{@link #initiate(Object, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/initiate/touchid}.</li>
 *   <li>{@link #verifyWithBiometricAttestation} — {@code push_acknowledge/touchid} → {@code allow/touchid}, then
 *   biometric proof JWT as {@code attestation} on {@code authenticate/touchid}.</li>
 *   <li>{@link #continueLogin(Object, Object, EventResult)} — verification login continue to tokens.</li>
 * </ol>
 */
public final class VerificationLoginFingerprint {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationLoginFingerprint(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    public void initiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFingerprint.initiate"));
            return;
        }
        invokeLoginFingerprintInitiate(loginRequest, callback);
    }

    public void verifyWithBiometricAttestation(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginFingerprint.verifyWithBiometricAttestation"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginFingerprint.verifyWithBiometricAttestation"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFingerprint.verifyWithBiometricAttestation"));
            return;
        }
        invokeLoginFingerprintVerify(activity, loginRequest, exchangeId, callback);
    }

    public void continueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFingerprint.continueLogin"));
            return;
        }
        if (authenticateResponse == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "authenticateResponse must not be null", "VerificationLoginFingerprint.continueLogin"));
            return;
        }
        invokeLoginFingerprintContinueLogin(loginRequest, authenticateResponse, callback);
    }

    private void invokeLoginFingerprintInitiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginFingerprintInitiate", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().fingerprint(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().fingerprint delegation failed.", cause);
        }
    }

    private void invokeLoginFingerprintVerify(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginFingerprintVerifyWithBiometricAttestation",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
                    EventResult.class);
            m.invoke(inst, activity, loginRequest, exchangeId, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().fingerprint(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().fingerprint verify delegation failed.", cause);
        }
    }

    private void invokeLoginFingerprintContinueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> authResponseClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginFingerprintContinueLogin", loginRequestClass, authResponseClass, EventResult.class);
            m.invoke(inst, loginRequest, authenticateResponse, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().fingerprint(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().fingerprint continueLogin delegation failed.", cause);
        }
    }
}
