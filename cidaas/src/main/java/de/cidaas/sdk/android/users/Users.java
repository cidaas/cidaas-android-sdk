package de.cidaas.sdk.android.users;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.enums.EventResult;

/**
 * User self-service on {@link de.cidaas.sdk.android.Cidaas}. Password reset runs in {@code cidaasnative}; this class
 * forwards via reflection so the core module does not depend on native at compile time.
 *
 * <p>Use entity types from {@code de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword} (and subpackages) at
 * runtime, and add the {@code cidaasnative} dependency to your app module.</p>
 *
 * <pre>{@code
 * cidaas.users().passwordReset().initiate(requestEntity, callback);
 * cidaas.users().passwordReset().validate(validateCodeEntity, callback);
 * cidaas.users().passwordReset().complete(resetPasswordEntity, callback);
 * }</pre>
 */
public final class Users {

    private static final String NATIVE_RESET_CONTROLLER =
            "de.cidaas.sdk.android.cidaasnative.domain.controller.resetpassword.ResetPasswordController";

    private final Context context;

    public Users(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Password reset flow entry. Call {@link PasswordReset#initiate}, {@link PasswordReset#validate}, or
     * {@link PasswordReset#complete} on the returned object.
     */
    @NonNull
    public PasswordReset passwordReset() {
        return new PasswordReset(this);
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

    private static void invoke(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(methodName, paramTypes);
            m.invoke(target, args);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Password reset delegation failed: " + methodName, cause);
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

    /**
     * Scoped password-reset actions from {@link Users#passwordReset()}.
     */
    public static final class PasswordReset {

        private final Users users;

        PasswordReset(@NonNull Users users) {
            this.users = users;
        }

        /**
         * Start password reset. {@code requestEntity} must be a
         * {@code de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.ResetPasswordRequestEntity}.
         */
        public void initiate(@NonNull Object requestEntity, @NonNull EventResult<?> callback) {
            users.initiateInternal(requestEntity, callback);
        }

        /**
         * Validate the reset code. {@code validateCodeRequestEntity} must be a
         * {@code de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetpasswordvalidatecode.ResetPasswordValidateCodeRequestEntity}.
         */
        public void validate(@NonNull Object validateCodeRequestEntity, @NonNull EventResult<?> callback) {
            users.validateInternal(validateCodeRequestEntity, callback);
        }

        /**
         * Complete password reset with the new password. {@code resetPasswordEntity} must be a
         * {@code de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetnewpassword.ResetPasswordEntity}.
         */
        public void complete(@NonNull Object resetPasswordEntity, @NonNull EventResult<?> callback) {
            users.completeInternal(resetPasswordEntity, callback);
        }
    }
}
