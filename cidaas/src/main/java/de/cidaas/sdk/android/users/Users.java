package de.cidaas.sdk.android.users;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;

import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.controller.UserProfileController;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.entity.UserInfo.UserInfoEntity;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;

/**
 * User self-service on {@link de.cidaas.sdk.android.Cidaas}. Password reset,
 * change password, registration, and
 * account verification delegate to {@code cidaasnative} via reflection so the
 * core module does not depend on native at
 * compile time.
 * {@link #fetch(String, EventResult)} loads profile data via
 * {@link UserProfileController} in this module.
 *
 * <p>
 * Add the {@code cidaasnative} dependency for reflected flows and use the
 * documented native entity types at
 * runtime.
 * </p>
 *
 * <pre>{@code
 * cidaas.users().passwordReset().initiate(requestEntity, callback);
 * cidaas.users().accountVerification().initiate(initiateRequestEntity, callback);
 * cidaas.users().accountVerification().validate(verifyRequestEntity, callback);
 * cidaas.users().changePassword(sub, changePasswordRequestEntity, callback);
 * cidaas.users().setPassword(sub, "MyNewPassword#1", callback);
 * cidaas.users().fetch(sub, callback);
 * cidaas.users().register(registrationEntity, callback);
 * cidaas.users().register(requestId, registrationEntity, callback);
 * cidaas.users().verifications().fetch(sub, callback);
 * }</pre>
 */
public final class Users {

    private static final String NATIVE_RESET_CONTROLLER = "de.cidaas.sdk.android.cidaasnative.domain.controller.resetpassword.ResetPasswordController";
    private static final String NATIVE_CIDAAS_NATIVE = "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";
    private static final String REGISTRATION_ENTITY_CLASS = "de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity";
    private static final String CHANGE_PASSWORD_REQUEST_ENTITY_CLASS = "de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.changepassword.ChangePasswordRequestEntity";
    private static final String SET_PASSWORD_REQUEST_ENTITY_CLASS = "de.cidaas.sdk.android.cidaasnative.data.entity.setpassword.SetPasswordRequestEntity";

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
     * Account verification (initiate challenge, then validate code). See
     * {@link AccountVerification}.
     */
    @NonNull
    public AccountVerification accountVerification() {
        return new AccountVerification(this);
    }

    /**
     * User-scoped verification setup (e.g. configured methods for the signed-in
     * user). See {@link Verifications}.
     */
    @NonNull
    public Verifications verifications() {
        return new Verifications(this);
    }

    /**
     * Initial password setup for users without an existing password. Loads the access token for {@code sub},
     * then POST {@code /password-srv/password} with {@code password} and {@code confirmPassword} (same value).
     */
    public void setPassword(@NonNull String sub, @NonNull String password, @NonNull EventResult<?> callback) {
        setPasswordInternal(sub, password, callback);
    }

    /**
     * Change password for the user identified by {@code sub}. Loads the access
     * token from local storage (same idea as
     * {@link #fetch(String, EventResult)}), then delegates to
     * {@code CidaasNative.changePassword}. {@code
     * changePasswordRequestEntity} must be a
     * {@code de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.changepassword.ChangePasswordRequestEntity}.
     */
    public void changePassword(@NonNull String sub, @NonNull Object changePasswordRequestEntity,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException("Sub must not be null or empty",
                    "Users.changePassword"));
            return;
        }
        AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
            @Override
            public void success(AccessTokenEntity accessTokenEntity) {
                String token = accessTokenEntity.getAccess_token();
                if (token == null || token.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            "Access Token must not be empty", "Users.changePassword"));
                    return;
                }
                changePasswordInternal(token, changePasswordRequestEntity, callback);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    /**
     * Fetch user profile for {@code sub}. Same as
     * {@link de.cidaas.sdk.android.Cidaas#getUserInfo(String, EventResult)}.
     */
    public void fetch(@NonNull String sub, @NonNull EventResult<UserInfoEntity> callback) {
        UserProfileController.getShared(context).getUserProfile(sub, callback);
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
     * Same as {@link #register(Object, EventResult)} with optional URL/query
     * parameters forwarded to
     * {@code getRequestId} (same semantics as
     * {@code CidaasNative.registerUser(..., extraParams)}).
     */
    @SuppressWarnings("unchecked")
    public void register(@NonNull Object registrationEntity, @NonNull EventResult<?> callback,
            @NonNull HashMap<String, String> extraParams) {
        registerSubmitInternal(registrationEntity, callback, new HashMap[] { extraParams });
    }

    /**
     * Register when you already have an auth {@code requestId} (e.g. after
     * {@code getRegistrationFields}). Delegates
     * to
     * {@code CidaasNative.registerUser(String, RegistrationEntity, EventResult)}.
     * {@code registrationEntity} must
     * be a
     * {@code de.cidaas.sdk.android.cidaasnative.data.entity.register.RegistrationEntity}.
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
                    "cidaasnative is required for Users native APIs. Add implementation project(':cidaasnative') (or "
                            + "your published cidaasnative artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve CidaasNative from cidaasnative.", e);
        }
    }

    private void accountVerificationInitiateInternal(@NonNull Object initiateRequestEntity,
            @NonNull EventResult<?> callback) {
        Object cidaasNative = nativeCidaasNative(context);
        invoke(cidaasNative, "initiateAccountVerification",
                new Class<?>[] { initiateRequestEntity.getClass(), EventResult.class },
                new Object[] { initiateRequestEntity, callback });
    }

    private void accountVerificationValidateInternal(@NonNull String code, @NonNull String accvid,
            @NonNull EventResult<?> callback) {
        Object cidaasNative = nativeCidaasNative(context);
        invoke(cidaasNative, "verifyAccount",
                new Class<?>[] { String.class, String.class, EventResult.class },
                new Object[] { code, accvid, callback });
    }

    private void accountVerificationValidateFromEntityInternal(@NonNull Object verifyAccountRequestEntity,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> ec = verifyAccountRequestEntity.getClass();
            String code = (String) ec.getMethod("getCode").invoke(verifyAccountRequestEntity);
            String accvid = (String) ec.getMethod("getAccvid").invoke(verifyAccountRequestEntity);
            accountVerificationValidateInternal(code, accvid, callback);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException(
                    "accountVerification().validate: entity must expose getCode() and getAccvid().", cause);
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

    private void changePasswordInternal(@NonNull String accessToken, @NonNull Object changePasswordRequestEntity,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> reqClazz = Class.forName(CHANGE_PASSWORD_REQUEST_ENTITY_CLASS);
            if (!reqClazz.isAssignableFrom(changePasswordRequestEntity.getClass())) {
                throw new IllegalArgumentException(
                        "changePasswordRequestEntity must be an instance of " + CHANGE_PASSWORD_REQUEST_ENTITY_CLASS);
            }
            Object cidaasNative = nativeCidaasNative(context);
            invoke(cidaasNative, "changePassword",
                    new Class<?>[] { String.class, changePasswordRequestEntity.getClass(), EventResult.class },
                    new Object[] { accessToken, changePasswordRequestEntity, callback });
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("users().changePassword delegation failed.", cause);
        }
    }

    private void setPasswordInternal(@NonNull String sub, @NonNull String password,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException("Sub must not be null or empty",
                    "Users.setPassword"));
            return;
        }
        if (password == null || password.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "password must not be null or empty", "Users.setPassword"));
            return;
        }
        AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
            @Override
            public void success(AccessTokenEntity accessTokenEntity) {
                String token = accessTokenEntity.getAccess_token();
                if (token == null || token.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            "Access Token must not be empty", "Users.setPassword"));
                    return;
                }
                invokeSetPassword(token, password, callback);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private void invokeSetPassword(@NonNull String accessToken, @NonNull String password,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> reqClazz = Class.forName(SET_PASSWORD_REQUEST_ENTITY_CLASS);
            Object requestEntity = reqClazz.getDeclaredConstructor().newInstance();
            reqClazz.getMethod("setPassword", String.class).invoke(requestEntity, password);
            reqClazz.getMethod("setConfirmPassword", String.class).invoke(requestEntity, password);
            Object cidaasNative = nativeCidaasNative(context);
            invoke(cidaasNative, "setPassword",
                    new Class<?>[] { String.class, reqClazz, EventResult.class },
                    new Object[] { accessToken, requestEntity, callback });
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("users().setPassword delegation failed.", cause);
        }
    }

    void userConfiguredVerificationsListInternal(@NonNull String sub, @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException("Sub must not be null or empty",
                    "Users.verifications().fetch"));
            return;
        }
        AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
            @Override
            public void success(AccessTokenEntity accessTokenEntity) {
                String token = accessTokenEntity.getAccess_token();
                if (token == null || token.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            "Access Token must not be empty", "Users.verifications().fetch"));
                    return;
                }
                invokeUserConfiguredVerificationsList(token, callback);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private void invokeUserConfiguredVerificationsList(@NonNull String accessToken,
            @NonNull EventResult<?> callback) {
        try {
            Object cidaasNative = nativeCidaasNative(context);
            invoke(cidaasNative, "getUserConfiguredVerificationsList",
                    new Class<?>[] { String.class, EventResult.class },
                    new Object[] { accessToken, callback });
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("users().verifications().fetch delegation failed.", cause);
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

    /**
     * Account verification from {@link Users#accountVerification()}. Delegates to
     * {@code CidaasNative}
     * ({@code initiateAccountVerification}, {@code verifyAccount}).
     */
    public static final class AccountVerification {

        private final Users users;

        AccountVerification(@NonNull Users users) {
            this.users = users;
        }

        /**
         * Start account verification. {@code initiateRequestEntity} must be a
         * {@code de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.InitiateAccountVerificationRequestEntity}.
         */
        public void initiate(@NonNull Object initiateRequestEntity, @NonNull EventResult<?> callback) {
            users.accountVerificationInitiateInternal(initiateRequestEntity, callback);
        }

        /**
         * Validate the verification code. {@code verifyAccountRequestEntity} must be a
         * {@code de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.VerifyAccountRequestEntity}
         * (or
         * otherwise expose {@code getCode()} and {@code getAccvid()}).
         */
        public void validate(@NonNull Object verifyAccountRequestEntity, @NonNull EventResult<?> callback) {
            users.accountVerificationValidateFromEntityInternal(verifyAccountRequestEntity, callback);
        }

        /**
         * Same as {@link #validate(Object, EventResult)} with explicit {@code code} and
         * {@code accvid} (account
         * verification id).
         */
        public void validate(@NonNull String code, @NonNull String accvid, @NonNull EventResult<?> callback) {
            users.accountVerificationValidateInternal(code, accvid, callback);
        }
    }

    /**
     * Configured verification methods for the current user (delegates to
     * {@code CidaasNative}).
     */
    public static final class Verifications {

        private final Users users;

        Verifications(@NonNull Users users) {
            this.users = users;
        }

        /**
         * GET {@code verification-actions-srv/setup/users}. Resolves access token for
         * {@code sub} from local storage.
         *
         * <p>
         * On success, the callback receives
         * {@code de.cidaas.sdk.android.cidaasnative.data.entity.userconfiguredverification.UserConfiguredVerificationsListResponseEntity}.
         * </p>
         */
        public void fetch(@NonNull String sub, @NonNull EventResult<?> callback) {
            users.userConfiguredVerificationsListInternal(sub, callback);
        }
    }
}
