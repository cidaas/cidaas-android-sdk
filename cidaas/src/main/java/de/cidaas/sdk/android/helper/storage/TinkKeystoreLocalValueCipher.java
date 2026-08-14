package de.cidaas.sdk.android.helper.storage;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;

import java.nio.charset.StandardCharsets;

/**
 * Tink AES-GCM AEAD with a master key in Android Keystore (TEE, not StrongBox).
 * Keyset material is stored in a dedicated SharedPreferences file encrypted by that master key.
 */
public final class TinkKeystoreLocalValueCipher implements LocalValueCipher {

    private static final String KEYSET_NAME = "cidaas_tink_keyset";
    private static final String PREF_FILE = "cidaas_tink_prefs";
    private static final String MASTER_KEY_URI = "android-keystore://cidaas_sdk_local_storage_master_key";

    private final Aead aead;

    public TinkKeystoreLocalValueCipher(@NonNull Context context) throws Exception {
        AeadConfig.register();
        Context app = context.getApplicationContext();
        aead = new AndroidKeysetManager.Builder()
                .withSharedPref(app, KEYSET_NAME, PREF_FILE)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .getKeysetHandle()
                .getPrimitive(Aead.class);
    }

    @NonNull
    @Override
    public String encrypt(@NonNull String preferenceKey, @NonNull String plaintext) throws Exception {
        byte[] cipher = aead.encrypt(
                plaintext.getBytes(StandardCharsets.UTF_8),
                preferenceKey.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(cipher, Base64.NO_WRAP);
    }

    @NonNull
    @Override
    public String decrypt(@NonNull String preferenceKey, @NonNull String ciphertext) throws Exception {
        byte[] plain = aead.decrypt(
                Base64.decode(ciphertext, Base64.NO_WRAP),
                preferenceKey.getBytes(StandardCharsets.UTF_8));
        return new String(plain, StandardCharsets.UTF_8);
    }
}
