package de.cidaas.sdk.android.cidaasnative.util;

public final class NativeConstants {

    private NativeConstants(){}

    public static final String ERROR_LOGGING_PREFIX = "Error :";
    public static final String EXCEPTION_LOGGING_PREFIX = "Exception :";
    public static final String ACCEPT_LANGUAGE = "accept-language";
    public static final String DEVICE_VERSION = "device-version";
    public static final String DEVICE_MODEL = "device-model";
    public static final String DEVICE_MAKE = "device-make";
    public static final String DEVICE_ID = "device-id";
    public static final String DEVICE_LATTITUDE = "lat";
    public static final String DEVICE_LONGITUDE = "lon";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String USER_AGENT = "user-agent";

    public static final String DOMAIN_URL = "DomainURL";
    public static final String CLIENT_ID = "ClientId";
    public static final String REDIRECT_URL = "RedirectURL";
    public static final String CLIENT_SECRET = "ClientSecret";

    public static final String DOMAIN_REDIRECT_URL_CLIENT_ID_EMPTY_MESSAGE = "DomainURL or ClientId or RedirectURL must not be empty";
    public static final String EXCEPTION_REQUEST_ID_MESSAGE = "Exception :Cidaas :getRequestId()";
    public static final String ERROR_RESET_PASSWORD_INITIATE = "Error :ResetPasswordService :initiateresetPassword()";
    public static final String EXCEPTION_RESET_PASSWORD_NEW = "Exception :ResetPasswordService :resetNewPassword()";
    public static final String METHOD_VERIFY_ACCOUNT_VERFICATION = "AccountVerificationService :verifyAccountVerification()";
    public static final String METHOD_VERIFY_ACCOUNT_VERFICATION_REGISTRATION_SERVICE = "RegistrationService :verifyAccountVerification()";
    public static final String METHOD_VERIFICATION_CONFIGS = "VerificationConfigsService :getVerificationConfigs()";
}
