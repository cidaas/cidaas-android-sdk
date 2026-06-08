package de.cidaas.sdk.android.helper.enums;

public final class UsageType {
    public static final String PASSWORDLESS = "INITIAL_AUTHENTICATION";
    public static final String MFA = "MULTIFACTOR_AUTHENTICATION";

    private UsageType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
