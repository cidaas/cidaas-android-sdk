package de.cidaas.sdk.android.helper.enums;

/**
 * Created by widasrnarayanan on 16/1/18.
 */

public final class WebAuthErrorCode {

    //General
    public static final int DEFAULT = 10000;
    public static final int FILE_NOT_FOUND = 10001;
    public static final int NO_CONTENT_IN_FILE = 10002;
    public static final int PROPERTY_MISSING = 10003;
    public static final int REQUEST_ID_SERVICE_FAILURE = 10004;
    public static final int EMPTY_REQUEST_ID_SERVICE = 10005;
    public static final int EMPTY_LOGIN_URL = 10006;
    public static final int EMPTY_REDIRECT_URL = 10007;
    public static final int EMPTY_DELEGATE = 10008;
    public static final int USER_CANCELLED_LOGIN = 10009;
    public static final int CODE_NOT_FOUND = 10010;

    //AccessToken
    public static final int ACCESSTOKEN_SERVICE_FAILURE = 10011;
    public static final int EMPTY_LOGIN_URL_SERVICE = 10012;
    public static final int ERROR_JSON_PARSING = 10013;
    public static final int USER_INFO_SERVICE_FAILURE = 10014;
    public static final int EMPTY_CALLBACK = 10015;
    public static final int NO_USER_FOUND = 10016;
    public static final int REFRESH_TOKEN_SERVICE_FAILURE = 10017;
    public static final int ACCESS_TOKEN_CONVERSION_FAILURE = 10200;
    public static final int SET_ACCESS_TOKEN = 10201;

    //Basic calls
    public static final int EMPTY_TENANT_INFO_SERVICE = 10018;
    public static final int EMPTY_CLIENT_INFO_SERVICE = 10019;
    public static final int TENANT_INFO_FAILURE = 10020;
    public static final int CLIENT_INFO_FAILURE = 10021;
    public static final int REQUEST_ID_MISSING = 10022;

    public static final int EMPTY_LOGIN_WITH_CREDENTIALS_FAILURE = 10218;
    public static final int LOGINWITH_CREDENTIALS_FAILURE = 10221;
    public static final int LOGOUT  = 10134;
    public static final int REGISTRATION_SETUP_FAILURE = 10222;
    public static final int USER_PROFILE_UPDATE_FAILURE = 10134;


    public static final int CONSENT_URL_FAILURE = 10023;
    public static final int CONSENT_STRING_FAILURE = 10024;
    public static final int ACCEPT_CONSENT_FAILURE = 10025;
    public static final int CONSENT_DETAILS_FAILURE = 10225;

    public static final int MFA_LIST_FAILURE = 10026;

    public static final int INITIATE_EMAIL_MFA_FAILURE = 10027;
    public static final int AUTHENTICATE_EMAIL_MFA_FAILURE = 10028;

    public static final int INITIATE_RESET_PASSWORD_FAILURE = 10029;
    public static final int RESET_PASSWORD_VALIDATE_CODE_FAILURE = 10030;
    public static final int RESET_NEWPASSWORD_FAILURE = 10031;
    public static final int CHANGE_PASSWORD_FAILURE = 10032;

    public static final int INITIATE_SMS_MFA_FAILURE = 10033;
    public static final int AUTHENTICATE_SMS_MFA_FAILURE = 10034;

    public static final int INITIATE_IVR_MFA_FAILURE = 10035;
    public static final int AUTHENTICATE_IVR_MFA_FAILURE = 10036;

    public static final int INITIATE_BACKUPCODE_MFA_FAILURE = 10037;
    public static final int AUTHENTICATE_BACKUPCODE_MFA_FAILURE = 10038;

    public static final int RESUME_LOGIN_FAILURE = 10039;

    public static final int INITIATE_FACE_MFA_FAILURE = 10040;
    public static final int AUTHENTICATE_FACE_MFA_FAILURE = 10041;

    public static final int INITIATE_FINGERPRINT_MFA_FAILURE = 10042;
    public static final int AUTHENTICATE_FINGERPRINT_MFA_FAILURE = 10043;

    public static final int INITIATE_VOICE_MFA_FAILURE = 10044;
    public static final int AUTHENTICATE_VOICE_MFA_FAILURE = 10045;

    public static final int INITIATE_PATTERN_MFA_FAILURE = 10046;
    public static final int AUTHENTICATE_PATTERN_MFA_FAILURE = 10047;

    public static final int INITIATE_SMARTPUSH_MFA_FAILURE = 10048;
    public static final int AUTHENTICATE_SMARTPUSH_MFA_FAILURE = 10049;

    public static final int INITIATE_TOTP_MFA_FAILURE = 10050;
    public static final int AUTHENTICATE_TOTP_MFA_FAILURE = 10051;

    public static final int INITIATE_FIDO_MFA_FAILURE = 10052;
    public static final int AUTHENTICATE_FIDO_MFA_FAILURE = 10053;

    public static final int RESUME_CONSENT_FAILURE = 10054;

    public static final int SETUP_SMS_MFA_FAILURE = 10055;
    public static final int ENROLL_SMS_MFA_FAILURE = 10056;

    public static final int SETUP_IVR_MFA_FAILURE = 10057;
    public static final int ENROLL_IVR_MFA_FAILURE = 10058;

    public static final int SETUP_BACKUPCODE_MFA_FAILURE = 10059;
    public static final int ENROLL_BACKUPCODE_MFA_FAILURE = 10060;

    public static final int SETUP_FACE_MFA_FAILURE = 10061;
    public static final int ENROLL_FACE_MFA_FAILURE = 10062;

    public static final int SETUP_FINGERPRINT_MFA_FAILURE = 10063;
    public static final int ENROLL_FINGERPRINT_MFA_FAILURE = 10064;

    public static final int SETUP_VOICE_MFA_FAILURE = 10065;
    public static final int ENROLL_VOICE_MFA_FAILURE = 10066;

    public static final int SETUP_PATTERN_MFA_FAILURE = 10067;
    public static final int ENROLL_PATTERN_MFA_FAILURE = 10068;

    public static final int SETUP_SMARTPUSH_MFA_FAILURE = 10069;
    public static final int ENROLL_SMARTPUSH_MFA_FAILURE = 10070;

    public static final int SETUP_TOTP_MFA_FAILURE = 10071;
    public static final int ENROLL_TOTP_MFA_FAILURE = 10072;

    public static final int SETUP_FIDO_MFA_FAILURE = 10073;
    public static final int ENROLL_FIDO_MFA_FAILURE = 10074;

    public static final int SETUP_EMAIL_MFA_FAILURE = 10075;
    public static final int ENROLL_EMAIL_MFA_FAILURE = 10076;

    public static final int INTERNAL_USER_PROFILE_FAILURE = 10077;

    public static final int INITIATE_ACCOUNT_VERIFICATION_FAILURE = 10078;
    public static final int VERIFY_ACCOUNT_VERIFICATION_FAILURE = 10079;


    public static final int SCANNED_PATTERN_MFA_FAILURE = 10081;
    public static final int SCANNED_FACE_MFA_FAILURE = 10082;
    public static final int SCANNED_VOICE_MFA_FAILURE = 10083;
    public static final int SCANNED_TOTP_MFA_FAILURE = 10084;
    public static final int SCANNED_FINGERPRINT_MFA_FAILURE = 10085;
    public static final int SCANNED_SMARTPUSH_MFA_FAILURE = 10086;
    public static final int SCANNED_FIDO_MFA_FAILURE = 10087;

    public static final int DEDUPLICATION_LIST_FAILURE = 10088;
    public static final int DEDUPLICATION_REGISTRATION_FAILURE = 10089;

    public static final int DELETE_MFA_FAILURE = 10090;

    public static final int DENY_NOTIFICATION = 10091;
    public static final int PENDING_NOTIFICATION_FAILURE = 10092;

    public static final int CONFIGURED_LIST_MFA_FAILURE = 10093;

    public static final int UPDATE_FCM_TOKEN = 10094;

    public static final int GET_LOGIN_URL_FAILURE = 10095;
    public static final int GET_SOCIAL_LOGIN_URL_FAILURE = 10096;

    public static final int ON_CANCEL_FACEBOOK = 10097;

    public static final int GOOGLE_ERROR = 10098;

    public static final int LOGOUT_ERROR = 10099;


    //Common Error Code maintained in Authenticator app
    public static final int DEVICE_VERIFICATION_FAILURE = 10080;


    public static final int LOCAL_AUHTHENTICATION_CANCELLED = 10101;
    public static final int LOCAL_AUHTHENTICATION_FAILED = 10102;
    public static final int NO_LOCAL_AUHTHENTICATION_FOUND = 10103;


    public static final int FINGERPRINT_SDK_VERSION_NOT_SUPPORTED = 10104;
    public static final int FINGERPRINT_BIOMETRIC_AUTHENTICATION_NOT_SUPPORTED = 10105;
    public static final int FINGERPRINT_BIOMERTIC_AUTHENTICATION_NOT_AVAILABLE = 10106;
    public static final int FINGERPRINT_BIOMERTIC_AUTHENTICATION_PERMISSION_NOT_GRANTED = 10107;
    public static final int FINGERPRINT_BIOMERTIC_AUTHENTICATION_INTERNAL_ERROR = 10108;
    public static final int FINGERPRINT_AUTHENTICATION_FAILED = 10109;
    public static final int FINGERPRINT_AUTHENTICATION_CANCELLED = 10110;

    public static final int DOCUMENT_VERIFICATION_FAILURE = 10111;

    //V2-Verification Error Codes
    public static final int SETUP_VERIFICATION_FAILURE = 10112;
    public static final int SCANNED_VERIFICATION_FAILURE = 10113;
    public static final int ENROLL_VERIFICATION_FAILURE = 10114;

    public static final int INITIATE_VERIFICATION_FAILURE = 10115;
    public static final int PUSH_ACKNOWLEDGE_FAILURE = 10116;
    public static final int PUSH_ALLOW_FAILURE = 10117;
    public static final int PUSH_REJECT_FAILURE = 10118;
    public static final int AUTHENTICATE_VERIFICATION_FAILURE = 10119;

    public static final int DELETE_VERIFICATION_FAILURE = 10120;
    public static final int MFA_LIST_VERIFICATION_FAILURE = 10121;


    public static final int USER_LOGIN_INFO_SERVICE_FAILURE = 10122;

    public static final int PARAMS_TO_DICTIONARY_CONVERTER_ERROR = 10123;
    public static final int PARSE_XML = 10124;
    public static final int READ_PROPERTIES_ERROR = 10125;
    public static final int CIDAAS_PROPERTY_MISSING = 10126;
    public static final int DEDUPLICATION_LOGIN_FAILURE = 10127;
    public static final int LOGINWITH_BROWSER_FAILURE = 10128;
    public static final int SAVE_LOGIN_PROPERTIES = 10129;
    public static final int CONFIGURE_MFA_FAILURE = 10130;
    public static final int PASSWORDLESS_LOGIN_FAILURE = 10131;
    public static final int INVALID_PROPERTIES = 10132;


    public static final int TIME_OUT_ERROR = 10133;

    public static final int PRE_LOGIN_ERROR = 10134;
    public static final int PROGRESSIVE_UPDATE_USER = 10135;

    public static final int LOGOUTWITH_BROWSER_FAILURE = 10136;

    public static final int DEVICE_REGISTRATION_FAILURE = 10137;

    private WebAuthErrorCode() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }


}

