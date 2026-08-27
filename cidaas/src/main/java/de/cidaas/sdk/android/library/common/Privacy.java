package de.cidaas.sdk.android.library.common;

/**
 * Process-wide privacy / security toggles for the SDK.
 */
public final class Privacy {
    private static volatile boolean locationEnabled = true;
    private static volatile boolean encryptionEnabled = false;

    private Privacy() {
    }

    /** Enable/disable any SDK path that depends on location permission. */
    public static void setLocationEnabled(boolean enabled) {
        locationEnabled = enabled;
    }

    /** Current location toggle state (true = enabled / legacy behavior). */
    public static boolean isLocationEnabled() {
        return locationEnabled;
    }

    /**
     * When {@code true}, sensitive fields ({@code password}, {@code pass_code}, etc.) are
     * JWE-encrypted before they are sent on the wire.
     * Default is {@code false} for backward compatibility.
     */
    public static void setEncryptionEnabled(boolean enabled) {
        encryptionEnabled = enabled;
    }

    public static boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
}
