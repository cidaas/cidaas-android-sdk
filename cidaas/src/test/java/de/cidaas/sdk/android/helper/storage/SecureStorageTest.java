package de.cidaas.sdk.android.helper.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.nio.charset.StandardCharsets;

import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.models.dbmodel.AccessTokenModel;

@RunWith(RobolectricTestRunner.class)
public class SecureStorageTest {

    private Context context;

    @Before
    public void setUp() {
        SecureStorage.resetForTest();
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("cidaas_preference", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("cidaas_storage_meta", Context.MODE_PRIVATE).edit().clear().commit();
        DBHelper.preferences = null;
        DBHelper.setConfig(context);
        SecureStorage.setCipherForTest(new FakeXorCipher());
    }

    @After
    public void tearDown() {
        SecureStorage.resetForTest();
        DBHelper.preferences = null;
    }

    @Test
    public void default_isPlaintext_backwardCompatible() {
        Assert.assertFalse(SecureStorage.isEnabled());
        String sub = "user-sub-1";
        DBHelper.getShared().addSecret("my-secret", sub);
        Assert.assertEquals("my-secret", DBHelper.getShared().getSecret(sub));

        SharedPreferences prefs = context.getSharedPreferences("cidaas_preference", Context.MODE_PRIVATE);
        Assert.assertEquals("my-secret", prefs.getString("Secret_info" + sub, null));
    }

    @Test
    public void secureMode_encryptsStringValues_keyedBySub() {
        String sub = "user-sub-2";
        AccessTokenModel model = new AccessTokenModel();
        model.setUserId(sub);
        model.setAccess_token("access-abc");
        model.setRefresh_token("refresh-xyz");

        SecureStorage.setEnabled(context, true);
        Assert.assertTrue(SecureStorage.isEnabled());

        DBHelper.getShared().setAccessToken(model);
        AccessTokenModel loaded = DBHelper.getShared().getAccessToken(sub);
        Assert.assertNotNull(loaded);
        Assert.assertEquals("access-abc", loaded.getAccess_token());
        Assert.assertEquals("refresh-xyz", loaded.getRefresh_token());

        SharedPreferences prefs = context.getSharedPreferences("cidaas_preference", Context.MODE_PRIVATE);
        String raw = prefs.getString("cidaas_user_details_" + sub, null);
        Assert.assertNotNull(raw);
        Assert.assertTrue(raw.startsWith(SecureStorage.CIPHER_PREFIX));
        Assert.assertFalse(raw.contains("access-abc"));
    }

    @Test
    public void enabling_migratesExistingPlaintext() {
        String sub = "user-sub-3";
        DBHelper.getShared().addSecret("plain-secret", sub);

        SecureStorage.setEnabled(context, true);

        SharedPreferences prefs = context.getSharedPreferences("cidaas_preference", Context.MODE_PRIVATE);
        String raw = prefs.getString("Secret_info" + sub, null);
        Assert.assertNotNull(raw);
        Assert.assertTrue(raw.startsWith(SecureStorage.CIPHER_PREFIX));
        Assert.assertEquals("plain-secret", DBHelper.getShared().getSecret(sub));
    }

    @Test
    public void disabling_decryptsBackToPlaintext() {
        String sub = "user-sub-4";
        SecureStorage.setEnabled(context, true);
        DBHelper.getShared().addSecret("secret-to-plain", sub);

        SharedPreferences prefs = context.getSharedPreferences("cidaas_preference", Context.MODE_PRIVATE);
        Assert.assertTrue(prefs.getString("Secret_info" + sub, "").startsWith(SecureStorage.CIPHER_PREFIX));

        SecureStorage.setEnabled(context, false);
        Assert.assertFalse(SecureStorage.isEnabled());

        String raw = prefs.getString("Secret_info" + sub, null);
        Assert.assertEquals("secret-to-plain", raw);
        Assert.assertEquals("secret-to-plain", DBHelper.getShared().getSecret(sub));
    }

    /** Deterministic stand-in for Tink so unit tests do not require Android Keystore. */
    private static final class FakeXorCipher implements LocalValueCipher {
        @NonNull
        @Override
        public String encrypt(@NonNull String preferenceKey, @NonNull String plaintext) {
            return android.util.Base64.encodeToString(xor(preferenceKey, plaintext), android.util.Base64.NO_WRAP);
        }

        @NonNull
        @Override
        public String decrypt(@NonNull String preferenceKey, @NonNull String ciphertext) {
            byte[] raw = android.util.Base64.decode(ciphertext, android.util.Base64.NO_WRAP);
            return new String(xorBytes(preferenceKey, raw), StandardCharsets.UTF_8);
        }

        private static byte[] xor(@NonNull String key, @NonNull String data) {
            return xorBytes(key, data.getBytes(StandardCharsets.UTF_8));
        }

        private static byte[] xorBytes(@NonNull String key, @NonNull byte[] data) {
            byte[] kb = key.getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                out[i] = (byte) (data[i] ^ kb[i % kb.length]);
            }
            return out;
        }
    }
}
