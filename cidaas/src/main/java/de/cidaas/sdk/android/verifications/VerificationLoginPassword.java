package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Password-based verification login: same v2 endpoints as OTP, with verification type {@code password}.
 * <ol>
 *   <li>{@link #initiate(Object, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/initiate/password}.</li>
 *   <li>{@link #verify(String, Object, String, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/authenticate/password} with {@code password}
 *   (not {@code pass_code}). On success the callback receives
 *   {@code de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse}.</li>
 *   <li>{@link #continueLogin(Object, Object, EventResult)} — POST {@code /login-srv/verification/login}, then code
 *   exchange for tokens. On success the callback receives
 *   {@code de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity}.</li>
 * </ol>
 *
 * <p>Requires a {@code de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest} with
 * {@code identifier}, {@code requestId}, and {@code usageType}; for MFA also set {@code trackId}.</p>
 */
public final class VerificationLoginPassword {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationLoginPassword(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Starts the password login step (exchange id for authenticate).
     *
     * @param loginRequest {@code LoginRequest} from {@code cidaasverification} (typed as {@link Object} so the main
     *                     module does not hard-depend on that artifact at compile time for consumers who omit it).
     */
    public void initiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPassword.initiate"));
            return;
        }
        invokeLoginPasswordInitiate(loginRequest, callback);
    }

    /**
     * v2 authenticate/password only (no login continue). On success, {@code callback} receives
     * {@code AuthenticateResponse} from {@code cidaasverification}.
     */
    public void verify(
            @NonNull String password,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<?> callback) {
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginPassword.verify"));
            return;
        }
        if (password == null || password.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "password must not be null or empty", "VerificationLoginPassword.verify"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPassword.verify"));
            return;
        }
        invokeLoginPasswordVerify(password, loginRequest, exchangeId, callback);
    }

    /**
     * Login continue after {@link #verify}: {@code /login-srv/verification/login}, then code exchange for tokens.
     */
    public void continueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPassword.continueLogin"));
            return;
        }
        if (authenticateResponse == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "authenticateResponse must not be null", "VerificationLoginPassword.continueLogin"));
            return;
        }
        invokeLoginPasswordContinueLogin(loginRequest, authenticateResponse, callback);
    }

    private void invokeLoginPasswordInitiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginPasswordInitiate", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().password().initiate(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().password().initiate delegation failed.", cause);
        }
    }

    private void invokeLoginPasswordVerify(
            @NonNull String password,
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
                    "loginPasswordVerify", String.class, loginRequestClass, String.class, EventResult.class);
            m.invoke(inst, password, loginRequest, exchangeId, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().password().verify(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().login().password().verify delegation failed.", cause);
        }
    }

    private void invokeLoginPasswordContinueLogin(
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
                    "loginOtpContinueLogin",
                    loginRequestClass,
                    String.class,
                    authResponseClass,
                    EventResult.class);
            m.invoke(inst, loginRequest, AuthenticationType.PASSWORD, authenticateResponse, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().password().continueLogin(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException(
                    "verifications().login().password().continueLogin delegation failed.", cause);
        }
    }
}
