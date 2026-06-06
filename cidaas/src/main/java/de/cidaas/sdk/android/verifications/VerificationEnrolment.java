package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Verification MFA enrollment from {@link Verifications#enrolment()}.
 * Includes fingerprint, push, pattern, face, OTP ({@link #otp()}), and passkey / FIDO2 ({@link #passkey(FragmentActivity, String, EventResult)}).
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
     * OTP enrollment for SMS, email, IVR, or chat: {@link VerificationEnrolmentOtp#initiate} then
     * {@link VerificationEnrolmentOtp#verify}.
     */
    @NonNull
    public VerificationEnrolmentOtp otp() {
        return new VerificationEnrolmentOtp(context);
    }

    /**
     * Passkey (FIDO2) enrollment: setup initiation (no scan) → Credential Manager with {@code fido2_entity.server_challenge}
     * → enroll with WebAuthn registration JSON. Requires {@code cidaasverification} and a {@link FragmentActivity}.
     *
     * <p>On success, {@code callback.success(...)} receives
     * {@code de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse}.</p>
     */
    public void passkey(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolment.passkey"));
            return;
        }
        invokeEnrolPasskey(activity, sub, callback);
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

    /**
     * Smart push (PUSH) enrollment: {@code /verification-srv/v2/setup/initiate/push/} →
     * {@code .../setup/scan/push/} → dialog with your title and message → on accept,
     * {@code .../setup/enroll/push/} with {@code pass_code} from setup {@code push_selected_number}.
     *
     * <p>On success, {@code callback.success(...)} receives
     * {@code de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse}.</p>
     *
     * @param acceptButtonText optional confirm button label; when null or blank, {@code "Accept"} is used
     * @param dialogThemeResId optional {@code AlertDialog} theme ({@code 0} = default from activity)
     */
    public void push(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolment.push"));
            return;
        }
        invokeEnrolPush(activity, sub, dialogTitle, dialogMessage, acceptButtonText, dialogThemeResId, callback);
    }

    /**
     * Same as {@link #push(FragmentActivity, String, String, String, String, int, EventResult)} with default theme
     * ({@code 0}).
     */
    public void push(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @NonNull EventResult<?> callback) {
        push(activity, sub, dialogTitle, dialogMessage, acceptButtonText, 0, callback);
    }

    /**
     * Same as {@link #push(FragmentActivity, String, String, String, String, int, EventResult)} with default
     * confirm label and the given dialog theme.
     */
    public void push(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        push(activity, sub, dialogTitle, dialogMessage, null, dialogThemeResId, callback);
    }

    /**
     * Same as {@link #push(FragmentActivity, String, String, String, String, int, EventResult)} with default
     * confirm button label {@code "Accept"} and default dialog theme.
     */
    public void push(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @NonNull EventResult<?> callback) {
        push(activity, sub, dialogTitle, dialogMessage, null, 0, callback);
    }

    /**
     * Pattern enrollment: after scan, shows a modal with a 9-dot pattern lock; {@code pass_code} is the SHA-256
     * hash (lowercase hex, UTF-8) of {@code PREFIX[d1,d2,...]} (1-based indices, default prefix {@code RED}).
     */
    public void pattern(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolment.pattern"));
            return;
        }
        invokeEnrolPattern(activity, sub, dialogTitle, dialogMessage, patternCodePrefix, dialogThemeResId, callback);
    }

    public void pattern(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<?> callback) {
        pattern(activity, sub, dialogTitle, dialogMessage, null, 0, callback);
    }

    public void pattern(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        pattern(activity, sub, dialogTitle, dialogMessage, null, dialogThemeResId, callback);
    }

    public void pattern(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @NonNull EventResult<?> callback) {
        pattern(activity, sub, dialogTitle, dialogMessage, patternCodePrefix, 0, callback);
    }

    /**
     * Face enrollment: after scan, opens a full-screen wizard (up to three camera captures). The flow continues
     * while the API indicates more images are needed and completes on the first fully successful response.
     * Requires runtime {@code CAMERA} permission and this module's
     * {@code FileProvider} (authority is the app {@code applicationId} suffixed with {@code .cidaasverification.fileprovider}).
     *
     * <p>On success, {@code callback.success(...)} receives
     * {@code de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse}.</p>
     *
     * @param faceAttempt sent as {@code face_attempt} on enroll (typically {@code 0}; use the overload with both
     *                     {@code faceAttempt} and {@code dialogThemeResId} when you need a non-zero value)
     * @param dialogThemeResId optional dialog theme ({@code 0} = default from activity)
     */
    public void face(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            int faceAttempt,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolment.face"));
            return;
        }
        invokeEnrolFace(activity, sub, dialogTitle, dialogMessage, faceAttempt, dialogThemeResId, callback);
    }

    public void face(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<?> callback) {
        face(activity, sub, dialogTitle, dialogMessage, 0, 0, callback);
    }

    public void face(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        face(activity, sub, dialogTitle, dialogMessage, 0, dialogThemeResId, callback);
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

    private void invokeEnrolPasskey(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrolPasskeyWithCredentialManager",
                    FragmentActivity.class,
                    String.class,
                    EventResult.class);
            m.invoke(inst, activity, sub, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().passkey(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().passkey delegation failed.", cause);
        }
    }

    private void invokeEnrolPush(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrolPushWithAcceptDialog",
                    FragmentActivity.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    EventResult.class);
            m.invoke(inst, activity, sub, dialogTitle, dialogMessage, acceptButtonText, dialogThemeResId, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().push(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().push delegation failed.", cause);
        }
    }

    private void invokeEnrolPattern(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrolPatternWithLockDialog",
                    FragmentActivity.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    EventResult.class);
            m.invoke(inst, activity, sub, dialogTitle, dialogMessage, patternCodePrefix, dialogThemeResId, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().pattern(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().pattern delegation failed.", cause);
        }
    }

    private void invokeEnrolFace(
            @NonNull FragmentActivity activity,
            @NonNull String sub,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            int faceAttempt,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrolFaceWithCameraDialog",
                    FragmentActivity.class,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    int.class,
                    EventResult.class);
            m.invoke(inst, activity, sub, dialogTitle, dialogMessage, faceAttempt, dialogThemeResId, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().face delegation failed.", cause);
        }
    }
}
