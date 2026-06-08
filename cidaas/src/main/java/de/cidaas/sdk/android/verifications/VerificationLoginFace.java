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
 * <strong>Legacy</strong> multi-step face login. Preferred: {@link VerificationLogin#face(Object, EventResult)
 * cidaas.verifications().login().face(loginRequest, callback)} (initiate → push acknowledge / allow → one capture →
 * tokens).
 * <ol>
 *   <li>{@link #initiate(Object, EventResult)} — POST
 *   {@code /verification-srv/v2/authenticate/initiate/face}.</li>
 *   <li>{@link #verifyWithCameraWizard} — {@code push_acknowledge/face} → {@code allow/face}, then one camera capture
 *   and {@code authenticate/face}.</li>
 *   <li>{@link #continueLogin(Object, Object, EventResult)} — POST {@code /login-srv/verification/login} for tokens.</li>
 * </ol>
 */
public final class VerificationLoginFace {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private final Context context;

    VerificationLoginFace(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    public void initiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFace.initiate"));
            return;
        }
        invokeLoginFaceInitiate(loginRequest, callback);
    }

    public void verifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        invokeLoginFaceVerifyWithCameraWizardShort(activity, loginRequest, exchangeId, callback);
    }

    public void verifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "dialogTitle must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        invokeLoginFaceVerifyWithCameraWizardTitleMessage(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, callback);
    }

    public void verifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "dialogTitle must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        invokeLoginFaceVerifyWithCameraWizardWithTheme(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, dialogThemeResId, callback);
    }

    public void verifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            int initialFaceAttempt,
            @NonNull EventResult<?> callback) {
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "activity must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "dialogTitle must not be null", "VerificationLoginFace.verifyWithCameraWizard"));
            return;
        }
        invokeLoginFaceVerifyWithCameraWizardFull(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, dialogThemeResId, initialFaceAttempt, callback);
    }

    public void continueLogin(
            @NonNull Object loginRequest,
            @NonNull Object authenticateResponse,
            @NonNull EventResult<?> callback) {
        if (loginRequest == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "loginRequest must not be null", "VerificationLoginFace.continueLogin"));
            return;
        }
        if (authenticateResponse == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "authenticateResponse must not be null", "VerificationLoginFace.continueLogin"));
            return;
        }
        invokeLoginFaceContinueLogin(loginRequest, authenticateResponse, callback);
    }

    private void invokeLoginFaceInitiate(@NonNull Object loginRequest, @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod("loginFaceInitiate", loginRequestClass, EventResult.class);
            m.invoke(inst, loginRequest, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginFace.initiate delegation failed.", cause);
        }
    }

    private void invokeLoginFaceVerifyWithCameraWizardShort(
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
                    "loginFaceVerifyWithCameraWizard",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
                    EventResult.class);
            m.invoke(inst, activity, loginRequest, exchangeId, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginFace.verifyWithCameraWizard delegation failed.", cause);
        }
    }

    private void invokeLoginFaceVerifyWithCameraWizardTitleMessage(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginFaceVerifyWithCameraWizard",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
                    String.class,
                    String.class,
                    EventResult.class);
            m.invoke(inst, activity, loginRequest, exchangeId, dialogTitle, dialogMessage, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginFace.verifyWithCameraWizard delegation failed.", cause);
        }
    }

    private void invokeLoginFaceVerifyWithCameraWizardWithTheme(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginFaceVerifyWithCameraWizard",
                    FragmentActivity.class,
                    loginRequestClass,
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
                    dialogThemeResId,
                    callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginFace.verifyWithCameraWizard delegation failed.", cause);
        }
    }

    private void invokeLoginFaceVerifyWithCameraWizardFull(
            @NonNull FragmentActivity activity,
            @NonNull Object loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            int initialFaceAttempt,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> loginRequestClass =
                    Class.forName(
                            "de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest");
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "loginFaceVerifyWithCameraWizard",
                    FragmentActivity.class,
                    loginRequestClass,
                    String.class,
                    String.class,
                    String.class,
                    int.class,
                    int.class,
                    EventResult.class);
            m.invoke(
                    inst,
                    activity,
                    loginRequest,
                    exchangeId,
                    dialogTitle,
                    dialogMessage,
                    dialogThemeResId,
                    initialFaceAttempt,
                    callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginFace.verifyWithCameraWizard delegation failed.", cause);
        }
    }

    private void invokeLoginFaceContinueLogin(
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
                    "loginFaceContinueLogin", loginRequestClass, authResponseClass, EventResult.class);
            m.invoke(inst, loginRequest, authenticateResponse, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().login().face(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("VerificationLoginFace.continueLogin delegation failed.", cause);
        }
    }
}
