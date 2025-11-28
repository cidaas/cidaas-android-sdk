package de.cidaas.sdk.android.library.common;

public final class Privacy {
    private static volatile boolean locationEnabled = true;

    private Privacy() {}

    /** Enable/disable any SDK path that depends on location permission. */
    public static void setLocationEnabled(boolean enabled) {
        locationEnabled = enabled;
    }

    /** Current location toggle state (true = enabled / legacy behavior). */
    public static boolean isLocationEnabled() {
        return locationEnabled;
    }
}
