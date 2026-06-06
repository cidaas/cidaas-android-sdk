package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Verification MFA enrollment from {@link Verifications#enrolment()}.
 */
public final class VerificationEnrolment {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationEnrolment(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Fingerprint (TOUCHID) enrollment: {@code /verification-srv/v2/setup/initiate/touchid/} →
     * {@code .../setup/scan/touchid/} → biometric Keystore proof JWT as {@code attestation} on
     * {@code .../setup/enroll/touchid/}. Requires {@code cidaasverification} and a {@link FragmentActivity} for the
     * biometric signing prompt.
     *
     * <p>On success, {@code callback.success(...)} receives
     * {@code de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse}.</p>
     */
    public void fingerprint(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolment.fingerprint"));
            return;
        }
        invokeEnrolFingerprint(activity, sub, callback);
    }

    private void invokeEnrolFingerprint(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrolFingerprintWithAttestation",
                    FragmentActivity.class,
                    String.class,
                    EventResult.class);
            m.invoke(inst, activity, sub, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().fingerprint(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().fingerprint delegation failed.", cause);
        }
    }
}
