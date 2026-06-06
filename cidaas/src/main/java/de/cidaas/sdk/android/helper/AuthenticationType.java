package de.cidaas.sdk.android.helper;

/**
 * Created by Raja on 13/02/18.
 */

public class AuthenticationType {
    public static final String PATTERN = "PATTERN";
    public static final String FINGERPRINT = "TOUCHID";
    public static final String VOICE = "VOICE";
    public static final String FACE = "FACE";
    public static final String IRIS = "IRIS";
    public static final String TOTP = "TOTP";
    public static final String SMARTPUSH = "PUSH";
    public static final String FIDO = "FIDO2";
    public static final String EMAIL = "EMAIL";
    public static final String SMS = "SMS";
    public static final String IVR = "IVR";
    /**
     * OTP delivery over in-app / web chat channel (setup URL segment {@code chat}).
     */
    public static final String CHAT = "CHAT";
    public static final String BACKUPCODE = "BACKUPCODE";

    private AuthenticationType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
