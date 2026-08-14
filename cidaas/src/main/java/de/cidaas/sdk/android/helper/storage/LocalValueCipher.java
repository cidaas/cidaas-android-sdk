package de.cidaas.sdk.android.helper.storage;

import androidx.annotation.NonNull;

/**
 * Encrypts/decrypts SharedPreferences string values. Associated data should bind ciphertext
 * to the preference key so values cannot be swapped across keys.
 */
public interface LocalValueCipher {

    @NonNull
    String encrypt(@NonNull String preferenceKey, @NonNull String plaintext) throws Exception;

    @NonNull
    String decrypt(@NonNull String preferenceKey, @NonNull String ciphertext) throws Exception;
}
