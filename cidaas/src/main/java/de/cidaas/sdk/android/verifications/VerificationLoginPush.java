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
 * <strong>Legacy</strong> multi-step push login. Supported usage is a single call:
 * {@link VerificationLogin#push(Object, EventResult) cidaas.verifications().login().push(loginRequest, callback)}
 * which runs initiate, push acknowledge / allow, an accept-only modal, and login continue.
 * <ol>
 *   <li>{@link #initiate(Object, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/initiate/push}.</li>
 *   <li>{@link #verifyWithAcceptDialog} — {@code push_acknowledge/push} → {@code allow/push}, then accept-only modal;
 *   on accept POST {@code authenticate/push} with {@code passCode} (e.g. {@code push_selected_number} from initiate).
 *   On success the callback receives
 *   {@code de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse}.</li>
 *   <li>{@link #continueLogin(Object, Object, EventResult)} — POST {@code /login-srv/verification/login} for tokens.</li>
 * </ol>
 */
public final class VerificationLoginPush {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationLoginPush(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    public void initiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPush.initiate"));
            return;
        }
        invokeLoginPushInitiate(loginRequest, callback);
    }

    public void verifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (passCode == null || passCode.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "passCode must not be null or empty", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        invokeLoginPushVerifyWithAcceptDialogDefaultCopy(activity, loginRequest, exchangeId, passCode, callback);
    }

    public void verifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @NonNull EventResult<?> callback) {
        verifyWithAcceptDialog(
                activity, loginRequest, exchangeId, passCode, dialogTitle, dialogMessage, null, 0, callback);
    }

    public void verifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        verifyWithAcceptDialog(
                activity, loginRequest, exchangeId, passCode, dialogTitle, dialogMessage, null, dialogThemeResId, callback);
    }

    public void verifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (passCode == null || passCode.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "passCode must not be null or empty", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        if (dialogTitle == null || dialogMessage == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "dialogTitle and dialogMessage must not be null", "VerificationLoginPush.verifyWithAcceptDialog"));
            return;
        }
        invokeLoginPushVerifyWithAcceptDialogFull(
                activity, loginRequest, exchangeId, passCode, dialogTitle, dialogMessage, acceptButtonText, dialogThemeResId, callback);
    }

    public void continueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginPush.continueLogin"));
            return;
        }
        if (authenticateResponse == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "authenticateResponse must not be null", "VerificationLoginPush.continueLogin"));
            return;
        }
        invokeLoginPushContinueLogin(loginRequest, authenticateResponse, callback);
    }

    private void invokeLoginPushInitiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginPushInitiate", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().push(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginPush.initiate delegation failed.", cause);
        }
    }

    private void invokeLoginPushVerifyWithAcceptDialogDefaultCopy(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginPushVerifyWithAcceptDialog",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
                    String.class,
                    EventResult.class);
            m.invoke(inst, activity, loginRequest, exchangeId, passCode, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().push(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginPush.verifyWithAcceptDialog delegation failed.", cause);
        }
    }

    private void invokeLoginPushVerifyWithAcceptDialogFull(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginPushVerifyWithAcceptDialog",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
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
                    passCode,
                    dialogTitle,
                    dialogMessage,
                    acceptButtonText,
                    dialogThemeResId,
                    callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().push(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginPush.verifyWithAcceptDialog delegation failed.", cause);
        }
    }

    private void invokeLoginPushContinueLogin(
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
                    "loginPushContinueLogin", loginRequestClass, authResponseClass, EventResult.class);
            m.invoke(inst, loginRequest, authenticateResponse, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().push(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginPush.continueLogin delegation failed.", cause);
        }
    }
}
