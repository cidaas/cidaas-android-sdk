package de.cidaas.sdk.android.users;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;

import de.cidaas.sdk.android.helper.enums.EventResult;

/**
 * User self-service on {@link de.cidaas.sdk.android.Cidaas}. These flows run in {@code cidaasnative}; this class
 * forwards via reflection so the core module does not depend on native at compile time.
 *
 * <p>Add the {@code cidaasnative} dependency and use the documented native entity types at runtime.</p>
 *
 * <pre>{@code
 * cidaas.users().passwordReset().initiate(requestEntity, callback);
 * cidaas.users().register(registrationEntity, callback);
 * cidaas.users().register(requestId, registrationEntity, callback);
 * }</pre>
 */
public final class Users {

    private static final String NATIVE_RESET_CONTROLLER =
            "de.cidaas.sdk.android.cidaasnative.domain.controller.resetpassword.ResetPasswordController";
    private static final String NATIVE_CIDAAS_NATIVE =
            "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";
    private static final String REGISTRATION_ENTITY_CLASS =
            "de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity";

    private final Context context;

    public Users(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Password reset flow. See {@link PasswordReset}.
     */
    @NonNull
    public PasswordReset passwordReset() {
        return new PasswordReset(this);
    }

    /**
     * Register a new user (fetches request id, then creates the user). Delegates to
     * {@code CidaasNative.registerUser}. {@code registrationEntity} must be a
     * {@code de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity}.
     */
    @SuppressWarnings("unchecked")
    public void register(@NonNull Object registrationEntity, @NonNull EventResult<?> callback) {
        registerSubmitInternal(registrationEntity, callback, new HashMap[0]);
    }

    /**
     * Same as {@link #register(Object, EventResult)} with optional URL/query parameters forwarded to
     * {@code getRequestId} (same semantics as {@code CidaasNative.registerUser(..., extraParams)}).
     */
    @SuppressWarnings("unchecked")
    public void register(@NonNull Object registrationEntity, @NonNull EventResult<?> callback,
            @NonNull HashMap<String, String> extraParams) {
        registerSubmitInternal(registrationEntity, callback, new HashMap[] { extraParams });
    }

    /**
     * Register when you already have an auth {@code requestId} (e.g. after {@code getRegistrationFields}). Delegates
     * to {@code CidaasNative.registerUser(String, RegistrationEntity, EventResult)}. {@code registrationEntity} must
     * be a {@code de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity}.
     */
    public void register(@NonNull String requestId, @NonNull Object registrationEntity,
            @NonNull EventResult<?> callback) {
        registerWithRequestIdInternal(requestId, registrationEntity, callback);
    }

    private static String acceptLanguage(@NonNull Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0).getLanguage();
        }
        return configuration.locale.getLanguage();
    }

    private static Object nativeResetController(@NonNull Context context) {
        try {
            Class<?> clazz = Class.forName(NATIVE_RESET_CONTROLLER);
            Method getShared = clazz.getMethod("getShared", Context.class);
            return getShared.invoke(null, context);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasnative is required for password reset. Add implementation project(':cidaasnative') (or your "
                            + "published cidaasnative artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve ResetPasswordController from cidaasnative.", e);
        }
    }

    private static Object nativeCidaasNative(@NonNull Context context) {
        try {
            Class<?> clazz = Class.forName(NATIVE_CIDAAS_NATIVE);
            Method getInstance = clazz.getMethod("getInstance", Context.class);
            return getInstance.invoke(null, context);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasnative is required for registration. Add implementation project(':cidaasnative') (or your "
                            + "published cidaasnative artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve CidaasNative from cidaasnative.", e);
        }
    }

    private static void invoke(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(methodName, paramTypes);
            m.invoke(target, args);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Native delegation failed: " + methodName, cause);
        }
    }

    private void initiateInternal(@NonNull Object requestEntity, @NonNull EventResult<?> callback) {
        Object controller = nativeResetController(context);
        invoke(controller, "initiateResetPasswordWithEntity",
                new Class<?>[] { requestEntity.getClass(), String.class, EventResult.class },
                new Object[] { requestEntity, acceptLanguage(context), callback });
    }

    private void validateInternal(@NonNull Object validateCodeRequestEntity, @NonNull EventResult<?> callback) {
        try {
            Class<?> ec = validateCodeRequestEntity.getClass();
            String code = (String) ec.getMethod("getCode").invoke(validateCodeRequestEntity);
            String rprq = (String) ec.getMethod("getResetRequestId").invoke(validateCodeRequestEntity);
            Object controller = nativeResetController(context);
            invoke(controller, "resetPasswordValidateCode",
                    new Class<?>[] { String.class, String.class, EventResult.class },
                    new Object[] { code, rprq, callback });
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException(
                    "passwordReset().validate: entity must expose getCode() and getResetRequestId().", cause);
        }
    }

    private void completeInternal(@NonNull Object resetPasswordEntity, @NonNull EventResult<?> callback) {
        Object controller = nativeResetController(context);
        invoke(controller, "resetNewPassword",
                new Class<?>[] { resetPasswordEntity.getClass(), EventResult.class },
                new Object[] { resetPasswordEntity, callback });
    }

    @SuppressWarnings("unchecked")
    private void registerSubmitInternal(@NonNull Object registrationEntity, @NonNull EventResult<?> callback,
            @NonNull HashMap<String, String>[] extraParamsForRequestId) {
        try {
            Class<?> regClazz = Class.forName(REGISTRATION_ENTITY_CLASS);
            if (!regClazz.isAssignableFrom(registrationEntity.getClass())) {
                throw new IllegalArgumentException(
                        "registrationEntity must be an instance of " + REGISTRATION_ENTITY_CLASS);
            }
            Object cidaasNative = nativeCidaasNative(context);
            Class<?> nativeClazz = cidaasNative.getClass();
            Method m = nativeClazz.getMethod("registerUser", regClazz, EventResult.class, HashMap[].class);
            m.invoke(cidaasNative, registrationEntity, callback, extraParamsForRequestId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("users().register delegation failed.", cause);
        }
    }

    private void registerWithRequestIdInternal(@NonNull String requestId, @NonNull Object registrationEntity,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> regClazz = Class.forName(REGISTRATION_ENTITY_CLASS);
            if (!regClazz.isAssignableFrom(registrationEntity.getClass())) {
                throw new IllegalArgumentException(
                        "registrationEntity must be an instance of " + REGISTRATION_ENTITY_CLASS);
            }
            Object cidaasNative = nativeCidaasNative(context);
            Class<?> nativeClazz = cidaasNative.getClass();
            Method m = nativeClazz.getMethod("registerUser", String.class, regClazz, EventResult.class);
            m.invoke(cidaasNative, requestId, registrationEntity, callback);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("users().register(requestId, ...) delegation failed.", cause);
        }
    }

    /**
     * Scoped password-reset actions from {@link Users#passwordReset()}.
     */
    public static final class PasswordReset {

        private final Users users;

        PasswordReset(@NonNull Users users) {
            this.users = users;
        }

        public void initiate(@NonNull Object requestEntity, @NonNull EventResult<?> callback) {
            users.initiateInternal(requestEntity, callback);
        }

        public void validate(@NonNull Object validateCodeRequestEntity, @NonNull EventResult<?> callback) {
            users.validateInternal(validateCodeRequestEntity, callback);
        }

        public void complete(@NonNull Object resetPasswordEntity, @NonNull EventResult<?> callback) {
            users.completeInternal(resetPasswordEntity, callback);
        }
    }
}
