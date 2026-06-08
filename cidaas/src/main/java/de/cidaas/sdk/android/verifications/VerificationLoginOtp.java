package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * OTP-based login (SMS, email, IVR, chat, TOTP): three client steps.
 * <ol>
 *   <li>{@link #initiate(Object, AcceptMethod, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/initiate/&lt;method&gt;}.</li>
 *   <li>{@link #verify(String, Object, String, AcceptMethod, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/authenticate/&lt;method&gt;} with the OTP as {@code pass_code}.
 *   On success the callback receives
 *   {@code de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse}.</li>
 *   <li>{@link #continueLogin(Object, Object, AcceptMethod, EventResult)} — POST
 *   {@code /login-srv/verification/login}, resolve {@code code} from JSON or a {@code 302 Location} query, exchange for
 *   tokens. On success the callback receives {@code de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity};
 *   use {@code getData()} for {@code de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity}.</li>
 * </ol>
 *
 * <p>Step 1 requires a {@code de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest}
 * with {@code identifier}, {@code requestId}, and {@code usageType} set; for MFA also set {@code trackId}.
 * Optional {@code setMediumId} is sent as {@code medium_id} on initiate when set.</p>
 * <p>On {@link #initiate} success, the callback receives
 * {@code de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateResponse}
 * (use {@code data.exchange_id.exchange_id} for step 2; {@code data.status_id} and {@code data.sub} are the
 * subject and status from the server; {@code data.medium_text} is the masked channel target when present).</p>
 */
public final class VerificationLoginOtp {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    /**
     * Channel for OTP login; maps to {@link AuthenticationType} and the v2 authenticate URL segment
     * ({@code sms}, {@code email}, {@code ivr}, {@code chat}, {@code totp}).
     */
    public enum AcceptMethod {
        SMS(AuthenticationType.SMS),
        EMAIL(AuthenticationType.EMAIL),
        IVR(AuthenticationType.IVR),
        CHAT(AuthenticationType.CHAT),
        TOTP(AuthenticationType.TOTP);

        private final String verificationType;

        AcceptMethod(@NonNull String verificationType) {
            this.verificationType = verificationType;
        }

        @NonNull
        public String verificationType() {
            return verificationType;
        }
    }

    private final Context context;

    VerificationLoginOtp(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Sends the login OTP for the chosen channel (or starts TOTP authentication).
     *
     * @param loginRequest {@code LoginRequest} from {@code cidaasverification} (typed as {@link Object} so the main
     *                     module does not hard-depend on that artifact at compile time).
     */
    public void initiate(
            @NonNull Object loginRequest,
            @NonNull AcceptMethod accept,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginOtp.initiate"));
            return;
        }
        if (accept == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "accept must not be null", "VerificationLoginOtp.initiate"));
            return;
        }
        invokeLoginOtpInitiate(loginRequest, accept.verificationType(), callback);
    }

    /**
     * v2 authenticate only (no login continue). On success, {@code callback} receives
     * {@code AuthenticateResponse} from {@code cidaasverification}.
     */
    public void verify(
            @NonNull String otp,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull AcceptMethod accept,
            @NonNull EventResult<?> callback) {
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginOtp.verify"));
            return;
        }
        if (otp == null || otp.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "otp must not be null or empty", "VerificationLoginOtp.verify"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginOtp.verify"));
            return;
        }
        if (accept == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "accept must not be null", "VerificationLoginOtp.verify"));
            return;
        }
        invokeLoginOtpVerify(otp, loginRequest, exchangeId, accept.verificationType(), callback);
    }

    /**
     * Login continue after {@link #verify}: {@code /login-srv/verification/login}, then code exchange for tokens.
     * {@code authenticateResponse} must be the {@code AuthenticateResponse} from {@link #verify}.
     */
    public void continueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull AcceptMethod accept,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginOtp.continueLogin"));
            return;
        }
        if (authenticateResponse == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "authenticateResponse must not be null", "VerificationLoginOtp.continueLogin"));
            return;
        }
        if (accept == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "accept must not be null", "VerificationLoginOtp.continueLogin"));
            return;
        }
        invokeLoginOtpContinueLogin(loginRequest, authenticateResponse, accept.verificationType(), callback);
    }

    private void invokeLoginOtpInitiate(
            @NonNull Object loginRequest,
            @NonNull String verificationType,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginOtpInitiate", loginRequestClass, String.class, EventResult.class);
            m.invoke(inst, loginRequest, verificationType, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().otp().initiate(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().otp().initiate delegation failed.", cause);
        }
    }

    private void invokeLoginOtpVerify(
            @NonNull String otp,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String verificationType,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginOtpVerify", String.class, loginRequestClass, String.class, String.class, EventResult.class);
            m.invoke(inst, otp, loginRequest, exchangeId, verificationType, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().otp().verify(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().otp().verify delegation failed.", cause);
        }
    }

    private void invokeLoginOtpContinueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull String verificationType,
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
                    "loginOtpContinueLogin", loginRequestClass, String.class, authResponseClass, EventResult.class);
            m.invoke(inst, loginRequest, verificationType, authenticateResponse, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().otp().continueLogin(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().otp().continueLogin delegation failed.", cause);
        }
    }
}
