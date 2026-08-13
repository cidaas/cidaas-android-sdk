package de.cidaas.sdk.android.helper.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Map;

import timber.log.Timber;

/**
 * Process-wide secure local storage toggle. When enabled, string values written through
 * {@link de.cidaas.sdk.android.helper.general.DBHelper} are encrypted with Keystore-backed Tink
 * AEAD. Boolean flags stay plaintext. Default is disabled (backward compatible).
 *
 * <p>Call {@link #setEnabled(Context, boolean)} after SDK init (or once a {@link Context} is
 * available). Enabling migrates existing plaintext string prefs to ciphertext; disabling
 * decrypts them back.
 */
public final class SecureStorage {

    private static final String META_PREFS = "cidaas_storage_meta";
    private static final String META_ENABLED = "secure_storage_enabled";
    /** Marks ciphertext written by this SDK so legacy plaintext can still be read. */
    static final String CIPHER_PREFIX = "cidaas_enc_v1:";

    private static volatile boolean enabled;
    private static volatile LocalValueCipher cipher;
    private static volatile Context appContext;

    private SecureStorage() {
    }

    /**
     * Loads persisted flag and prepares the cipher if secure mode was already on.
     * Invoked from {@link de.cidaas.sdk.android.helper.general.DBHelper#setConfig(Context)}.
     */
    public static synchronized void init(@NonNull Context context) {
        appContext = context.getApplicationContext();
        SharedPreferences meta = appContext.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
        enabled = meta.getBoolean(META_ENABLED, false);
        if (enabled) {
            ensureCipherLocked();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables secure storage. When turning on, existing string values in
     * {@code cidaas_preference} are encrypted in place. When turning off, they are decrypted.
     *
     * @throws IllegalStateException if Keystore/Tink cannot be initialized while enabling
     */
    public static synchronized void setEnabled(@NonNull Context context, boolean secure) {
        appContext = context.getApplicationContext();
        SharedPreferences meta = appContext.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);

        if (secure == enabled && (!secure || cipher != null)) {
            meta.edit().putBoolean(META_ENABLED, secure).apply();
            enabled = secure;
            return;
        }

        if (secure) {
            ensureCipherLocked();
            enabled = true;
            meta.edit().putBoolean(META_ENABLED, true).apply();
            migrateAllStringValues(true);
        } else {
            if (cipher == null) {
                try {
                    ensureCipherLocked();
                } catch (Exception e) {
                    Timber.e(e, "SecureStorage: cannot decrypt existing values while disabling");
                }
            }
            migrateAllStringValues(false);
            enabled = false;
            meta.edit().putBoolean(META_ENABLED, false).apply();
            cipher = null;
        }
    }

    /**
     * For persist: encrypt when secure mode is on (and value is not already marked ciphertext).
     */
    @Nullable
    public static String wrapForPersist(@NonNull String preferenceKey, @Nullable String value) {
        if (value == null || value.isEmpty() || !enabled) {
            return value;
        }
        if (value.startsWith(CIPHER_PREFIX)) {
            return value;
        }
        LocalValueCipher c = cipher;
        if (c == null) {
            throw new IllegalStateException("Secure storage enabled but cipher is not initialized");
        }
        try {
            return CIPHER_PREFIX + c.encrypt(preferenceKey, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt preference value for key=" + preferenceKey, e);
        }
    }

    /**
     * For read: decrypt when value carries {@link #CIPHER_PREFIX}; otherwise return as plaintext
     * (legacy / secure-off).
     */
    @Nullable
    public static String unwrapFromPersist(
            @NonNull String preferenceKey,
            @Nullable String stored,
            @Nullable String defaultValue) {
        if (stored == null) {
            return defaultValue;
        }
        if (!stored.startsWith(CIPHER_PREFIX)) {
            return stored;
        }
        String payload = stored.substring(CIPHER_PREFIX.length());
        LocalValueCipher c = cipher;
        if (c == null) {
            try {
                ensureCipherLocked();
                c = cipher;
            } catch (Exception e) {
                Timber.e(e, "SecureStorage: cipher unavailable for decrypt key=%s", preferenceKey);
                return defaultValue;
            }
        }
        try {
            return c.decrypt(preferenceKey, payload);
        } catch (Exception e) {
            Timber.e(e, "SecureStorage: decrypt failed for key=%s", preferenceKey);
            return defaultValue;
        }
    }

    private static void ensureCipherLocked() {
        if (cipher != null) {
            return;
        }
        if (appContext == null) {
            throw new IllegalStateException("SecureStorage requires Context; call DBHelper.setConfig / Cidaas init first");
        }
        try {
            cipher = new TinkKeystoreLocalValueCipher(appContext);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Keystore/Tink local storage cipher", e);
        }
    }

    private static void migrateAllStringValues(boolean toEncrypted) {
        if (appContext == null) {
            return;
        }
        SharedPreferences prefs = appContext.getSharedPreferences("cidaas_preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        boolean changed = false;
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                continue;
            }
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if (value == null) {
                continue;
            }
            if (toEncrypted) {
                if (value.startsWith(CIPHER_PREFIX)) {
                    continue;
                }
                try {
                    editor.putString(key, CIPHER_PREFIX + cipher.encrypt(key, value));
                    changed = true;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to migrate key to secure storage: " + key, e);
                }
            } else {
                if (!value.startsWith(CIPHER_PREFIX)) {
                    continue;
                }
                if (cipher == null) {
                    continue;
                }
                try {
                    String plain = cipher.decrypt(key, value.substring(CIPHER_PREFIX.length()));
                    editor.putString(key, plain);
                    changed = true;
                } catch (Exception e) {
                    Timber.e(e, "SecureStorage: failed to decrypt key during disable: %s", key);
                }
            }
        }
        if (changed) {
            editor.commit();
        }
    }

    @VisibleForTesting
    static synchronized void setCipherForTest(@Nullable LocalValueCipher testCipher) {
        cipher = testCipher;
    }

    @VisibleForTesting
    static synchronized void resetForTest() {
        enabled = false;
        cipher = null;
        appContext = null;
    }
}
