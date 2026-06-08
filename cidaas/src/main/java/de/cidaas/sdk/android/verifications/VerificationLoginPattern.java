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
 * <strong>Legacy</strong> multi-step pattern login (initiate / verify / continue). Supported usage is a single call:
 * {@link VerificationLogin#pattern(Object, EventResult) cidaas.verifications().login().pattern(loginRequest, callback)}
 * which runs initiate (including optional {@code LoginRequest#setMediumId}), the pattern lock UI, and login continue.
 * <ol>
 *   <li>{@link #initiate(Object, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/initiate/pattern}.</li>
 *   <li>{@link #verifyWithPatternLockDialog} or {@link #verify(String, Object, String, EventResult)} —
 *   {@code push_acknowledge/pattern} → {@code allow/pattern}, then POST
 *   {@code /verification-srv/v2/authenticate/authenticate/pattern} with SHA-256 (lowercase hex) of
 *   {@code PREFIX[d1,d2,...]} as {@code pass_code}. On success the callback receives
 *   {@code de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse}.</li>
 *   <li>{@link #continueLogin(Object, Object, EventResult)} — POST {@code /login-srv/verification/login}, resolve
 *   {@code code}, exchange for tokens. On success the callback receives
 *   {@code de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity}.</li>
 * </ol>
 *
 * <p>Step 1 requires a {@code de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest}
 * with {@code identifier}, {@code requestId}, and {@code usageType} set; for MFA also set {@code trackId}.
 * Optional: {@code setMediumId(...)} for the configured medium sent as {@code medium_id} on initiate.</p>
 */
public final class VerificationLoginPattern {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationLoginPattern(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Starts pattern login (v2 authenticate initiate for {@code pattern}).
     */
    public void initiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPattern.initiate"));
            return;
        }
        invokeLoginPatternInitiate(loginRequest, callback);
    }

    /**
     * Authenticate with a pre-computed pattern {@code pass_code} (SHA-256 hex), same as the hash produced in
     * enrollment’s pattern modal.
     */
    public void verify(
            @NonNull String passCodeSha256Hex,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<?> callback) {
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginPattern.verify"));
            return;
        }
        if (passCodeSha256Hex == null || passCodeSha256Hex.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "passCodeSha256Hex must not be null or empty", "VerificationLoginPattern.verify"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPattern.verify"));
            return;
        }
        invokeLoginPatternVerifyPassCode(passCodeSha256Hex, loginRequest, exchangeId, callback);
    }

    public void verifyWithPatternLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<?> callback) {
        verifyWithPatternLockDialog(activity, loginRequest, exchangeId, dialogTitle, dialogMessage, null, 0, callback);
    }

    public void verifyWithPatternLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        verifyWithPatternLockDialog(activity, loginRequest, exchangeId, dialogTitle, dialogMessage, null, dialogThemeResId, callback);
    }

    public void verifyWithPatternLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @NonNull EventResult<?> callback) {
        verifyWithPatternLockDialog(activity, loginRequest, exchangeId, dialogTitle, dialogMessage, patternCodePrefix, 0, callback);
    }

    /**
     * @param patternCodePrefix optional prefix before hashing (default {@code RED} when null)
     */
    public void verifyWithPatternLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginPattern.verifyWithPatternLockDialog"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginPattern.verifyWithPatternLockDialog"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPattern.verifyWithPatternLockDialog"));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "dialogTitle must not be null", "VerificationLoginPattern.verifyWithPatternLockDialog"));
            return;
        }
        invokeLoginPatternVerifyWithLockDialog(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, patternCodePrefix, dialogThemeResId, callback);
    }

    /**
     * Login continue after {@link #verify} or {@link #verifyWithPatternLockDialog}.
     */
    public void continueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPattern.continueLogin"));
            return;
        }
        if (authenticateResponse == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "authenticateResponse must not be null", "VerificationLoginPattern.continueLogin"));
            return;
        }
        invokeLoginPatternContinueLogin(loginRequest, authenticateResponse, callback);
    }

    private void invokeLoginPatternInitiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginPatternInitiate", loginRequestClass, EventResult.class);
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

    private void invokeLoginPatternVerifyPassCode(
            @NonNull String passCodeSha256Hex,
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
                    "loginPatternVerifyPassCode",
                    String.class,
                    loginRequestClass,
                    String.class,
                    EventResult.class);
            m.invoke(inst, passCodeSha256Hex, loginRequest, exchangeId, callback);
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

    private void invokeLoginPatternVerifyWithLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginPatternVerifyWithLockDialog",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    EventResult.class);
            m.invoke(
                    inst,
                    activity,
                    loginRequest,
                    exchangeId,
                    dialogTitle,
                    dialogMessage,
                    patternCodePrefix,
                    dialogThemeResId,
                    callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().pattern(loginRequest, callback). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException(
                    "verifications().login().pattern(loginRequest, callback) delegation failed.", cause);
        }
    }

    private void invokeLoginPatternContinueLogin(
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
                    "loginPatternContinueLogin", loginRequestClass, authResponseClass, EventResult.class);
            m.invoke(inst, loginRequest, authenticateResponse, callback);
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
