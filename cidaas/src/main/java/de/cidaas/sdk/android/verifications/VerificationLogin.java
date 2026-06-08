package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Login-time verification flows from {@link Verifications#login()}.
 */
public final class VerificationLogin {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationLogin(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * OTP login (SMS, email, IVR, chat, TOTP): {@link VerificationLoginOtp#initiate}, {@link VerificationLoginOtp#verify},
     * then {@link VerificationLoginOtp#continueLogin}.
     */
    @NonNull
    public VerificationLoginOtp otp() {
        return new VerificationLoginOtp(context);
    }

    /**
     * Pattern login in one call: initiate → pattern lock UI → login continue to tokens.
     * Use {@code cidaas.verifications().login().pattern(loginRequest, callback)} — set
     * {@link de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest#setMediumId(String)}
     * on {@code loginRequest} when the backend expects a configured {@code medium_id}.
     *
     * <p>When {@code Cidaas} was not created with a {@code androidx.fragment.app.FragmentActivity}, call
     * {@code ((LoginRequest) loginRequest).setPatternLoginHostActivity(activity)} first.</p>
     */
    public void pattern(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLogin.pattern"));
            return;
        }
        invokeLoginPatternOneShot(loginRequest, callback);
    }

    /**
     * Fingerprint (touchid) login in one call: initiate → push acknowledge / allow → biometric {@code attestation} JWT
     * → login continue to tokens.
     * When {@code Cidaas} was not created with a {@code androidx.fragment.app.FragmentActivity}, call
     * {@code ((LoginRequest) loginRequest).setFingerprintLoginHostActivity(activity)} first.
     */
    public void fingerprint(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLogin.fingerprint"));
            return;
        }
        invokeLoginFingerprintOneShot(loginRequest, callback);
    }

    /**
     * Push login in one call: initiate → push acknowledge / allow → accept-only modal → login continue to tokens.
     * The SDK reads {@code push_selected_number} from the initiate response and sends it as {@code pass_code} on
     * authenticate. When {@code Cidaas} was not created with a {@code androidx.fragment.app.FragmentActivity}, call
     * {@code ((LoginRequest) loginRequest).setPushLoginHostActivity(activity)} first.
     */
    public void push(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLogin.push"));
            return;
        }
        invokeLoginPushOneShot(loginRequest, callback);
    }

    /**
     * @deprecated Use {@link #push(Object, EventResult)} for the one-shot flow to tokens.
     */
    @Deprecated
    @NonNull
    public VerificationLoginPush push() {
        return new VerificationLoginPush(context);
    }

    /**
     * @deprecated Use {@link #fingerprint(Object, EventResult)} for the one-shot flow to tokens.
     */
    @Deprecated
    @NonNull
    public VerificationLoginFingerprint fingerprint() {
        return new VerificationLoginFingerprint(context);
    }

    /**
     * @deprecated Use {@link #pattern(Object, EventResult)} — {@code cidaas.verifications().login().pattern(loginRequest, callback)}.
     * This no-arg overload exposes legacy multi-step control only.
     */
    @Deprecated
    @NonNull
    public VerificationLoginPattern pattern() {
        return new VerificationLoginPattern(context);
    }

    private void invokeLoginPatternOneShot(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginPatternOneShot", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().pattern(loginRequest, callback). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().pattern(loginRequest, callback) delegation failed.", cause);
        }
    }

    private void invokeLoginFingerprintOneShot(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginFingerprintOneShot", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().fingerprint(loginRequest, callback). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException(
                    "verifications().login().fingerprint(loginRequest, callback) delegation failed.", cause);
        }
    }

    private void invokeLoginPushOneShot(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginPushOneShot", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().push(loginRequest, callback). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException(
                    "verifications().login().push(loginRequest, callback) delegation failed.", cause);
        }
    }
}
