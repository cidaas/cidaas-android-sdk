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
}
