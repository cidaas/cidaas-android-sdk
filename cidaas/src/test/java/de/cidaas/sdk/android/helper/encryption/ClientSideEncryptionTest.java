package de.cidaas.sdk.android.helper.encryption;

import com.nimbusds.jose.jwk.ECKey;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import de.cidaas.sdk.android.library.common.Privacy;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class ClientSideEncryptionTest {

    private static final String ENC_KEY_A = "72375b3f-34ce-48ac-a77b-f2c775999eb6";
    private static final String ENC_KEY_B = "a481611f-d215-42da-ba8f-e6254b9a7caa";

    private static final String SAMPLE_JWKS = "{"
            + "\"keys\":["
            + "{"
            + "\"kid\":\"" + ENC_KEY_A + "\","
            + "\"kty\":\"EC\","
            + "\"alg\":\"ECDH-ES\","
            + "\"crv\":\"P-256\","
            + "\"use\":\"enc\","
            + "\"x\":\"dR1oRGTcEuwdU_UgVW2mALpZAwId2ofyJeHTUEoyqZE\","
            + "\"y\":\"YxDzoQJo0WRcOG4_brxBF1CQhuYgt90zoLo041poWJ0\""
            + "},"
            + "{"
            + "\"kid\":\"" + ENC_KEY_B + "\","
            + "\"kty\":\"EC\","
            + "\"alg\":\"ECDH-ES\","
            + "\"crv\":\"P-256\","
            + "\"use\":\"enc\","
            + "\"x\":\"MB05y_sq4O2UygZ2k9Z4oQS2deKu-4aLhf5q2O-XvKY\","
            + "\"y\":\"NNhl1SCJNrys41ZoAO3o_GPVWwRwhlQD-BEc6FnxBbs\""
            + "},"
            + "{"
            + "\"kid\":\"sig-key\","
            + "\"kty\":\"RSA\","
            + "\"alg\":\"RS256\","
            + "\"use\":\"sig\","
            + "\"n\":\"someting\","
            + "\"e\":\"AQAB\""
            + "}"
            + "]"
            + "}";

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        Privacy.setEncryptionEnabled(false);
        JwksClient.clearCache();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        JwksClient.setHttpClient(new okhttp3.OkHttpClient());
    }

    @After
    public void tearDown() throws Exception {
        Privacy.setEncryptionEnabled(false);
        JwksClient.clearCache();
        mockWebServer.shutdown();
    }

    @Test
    public void selectEncryptionKey_picksAnyUseEncKey() throws Exception {
        ECKey key = JwksEncKeySelector.selectEncryptionKey(SAMPLE_JWKS);
        Assert.assertNotNull(key);
        Assert.assertTrue(ENC_KEY_A.equals(key.getKeyID()) || ENC_KEY_B.equals(key.getKeyID()));
    }

    @Test
    public void selectEncryptionKey_choosesRandomAmongMatchingKeys() throws Exception {
        // Force index 0 then 1 so both kids are exercised deterministically.
        JwksEncKeySelector.setRandom(new java.util.Random() {
            private int call;

            @Override
            public int nextInt(int bound) {
                return call++ % bound;
            }
        });
        try {
            ECKey first = JwksEncKeySelector.selectEncryptionKey(SAMPLE_JWKS);
            ECKey second = JwksEncKeySelector.selectEncryptionKey(SAMPLE_JWKS);
            Assert.assertNotNull(first);
            Assert.assertNotNull(second);
            Assert.assertEquals(ENC_KEY_A, first.getKeyID());
            Assert.assertEquals(ENC_KEY_B, second.getKeyID());
        } finally {
            JwksEncKeySelector.resetRandom();
        }
    }

    @Test
    public void selectEncryptionKey_rejectsMissingCrv() throws Exception {
        String jwksMissingCrv = "{"
                + "\"keys\":[{"
                + "\"kid\":\"no-crv\","
                + "\"kty\":\"EC\","
                + "\"alg\":\"ECDH-ES\","
                + "\"use\":\"enc\","
                + "\"x\":\"dR1oRGTcEuwdU_UgVW2mALpZAwId2ofyJeHTUEoyqZE\","
                + "\"y\":\"YxDzoQJo0WRcOG4_brxBF1CQhuYgt90zoLo041poWJ0\""
                + "}]"
                + "}";
        Assert.assertNull(JwksEncKeySelector.selectEncryptionKey(jwksMissingCrv));
    }

    @Test
    public void encrypt_includesKidIatJtiInProtectedHeader() throws Exception {
        ECKey key = JwksEncKeySelector.selectEncryptionKey(SAMPLE_JWKS);
        Assert.assertNotNull(key);

        String jwe = JweFieldEncryptor.encrypt("Secret123", key);
        Assert.assertTrue(jwe.split("\\.").length >= 5);

        JSONObject header = parseJweHeader(jwe);
        Assert.assertEquals("ECDH-ES", header.getString("alg"));
        Assert.assertEquals("A256GCM", header.getString("enc"));
        Assert.assertEquals(key.getKeyID(), header.getString("kid"));
        Assert.assertTrue(header.has("iat"));
        Assert.assertTrue(header.has("jti"));
        Assert.assertFalse(header.getString("jti").isEmpty());
    }

    @Test
    public void encryptIfEnabled_whenDisabled_returnsPlaintext() {
        Privacy.setEncryptionEnabled(false);
        Assert.assertEquals("plain-pass", ClientSideEncryption.encryptIfEnabled("plain-pass"));
    }

    @Test
    public void encryptIfEnabled_whenEnabled_returnsJweWithKid() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody(SAMPLE_JWKS).setResponseCode(200));
        Privacy.setEncryptionEnabled(true);

        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        String jwe = encryptOffMainThread("pass_code_value", baseUrl);

        Assert.assertNotNull(jwe);
        Assert.assertNotEquals("pass_code_value", jwe);
        Assert.assertEquals(1, mockWebServer.getRequestCount());

        JSONObject header = parseJweHeader(jwe);
        Assert.assertTrue(ENC_KEY_A.equals(header.getString("kid")) || ENC_KEY_B.equals(header.getString("kid")));
        Assert.assertTrue(header.has("iat"));
        Assert.assertTrue(header.has("jti"));
    }

    @Test
    public void encryptionEnabled_roundTripOnPrivacy() {
        Privacy.setEncryptionEnabled(true);
        Assert.assertTrue(Privacy.isEncryptionEnabled());
        Privacy.setEncryptionEnabled(false);
        Assert.assertFalse(Privacy.isEncryptionEnabled());
    }

    @Test
    public void encryptIfEnabled_samePlaintext_producesDistinctJti() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody(SAMPLE_JWKS).setResponseCode(200));
        Privacy.setEncryptionEnabled(true);

        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        String password = encryptOffMainThread("SamePass123!", baseUrl);
        String confirm = encryptOffMainThread("SamePass123!", baseUrl);

        Assert.assertEquals(1, mockWebServer.getRequestCount());
        Assert.assertNotEquals(password, confirm);
        Assert.assertNotEquals(parseJweHeader(password).getString("jti"),
                parseJweHeader(confirm).getString("jti"));
    }

    @Test
    public void encryptPassCodeIfEnabled_onlyBackupCodeAndPattern() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody(SAMPLE_JWKS).setResponseCode(200));
        Privacy.setEncryptionEnabled(true);

        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        Assert.assertEquals("123456",
                ClientSideEncryption.encryptPassCodeIfEnabled("123456", "EMAIL"));
        Assert.assertEquals("654321",
                ClientSideEncryption.encryptPassCodeIfEnabled("654321", "SMS"));
        Assert.assertFalse(ClientSideEncryption.shouldEncryptVerificationPassCode("email"));
        Assert.assertFalse(ClientSideEncryption.shouldEncryptVerificationPassCode("TOTP"));
        Assert.assertTrue(ClientSideEncryption.shouldEncryptVerificationPassCode("BACKUPCODE"));
        Assert.assertTrue(ClientSideEncryption.shouldEncryptVerificationPassCode("PATTERN"));

        Assert.assertNotEquals("pattern-secret",
                encryptOffMainThread("pattern-secret", baseUrl));
        Assert.assertNotEquals("backup-code",
                encryptOffMainThread("backup-code", baseUrl));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    /**
     * Encryption may run on Robolectric's main thread; JWKS fetch must not.
     * Warm/fetch via a worker so {@link ClientSideEncryption#encryptIfEnabled} succeeds.
     */
    private static String encryptOffMainThread(String value, String baseUrl) throws Exception {
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            try {
                result.set(ClientSideEncryption.encryptIfEnabled(value, baseUrl));
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        }, "cse-test-encrypt");
        worker.start();
        if (!latch.await(15, TimeUnit.SECONDS)) {
            throw new AssertionError("encrypt timed out");
        }
        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }

    private static JSONObject parseJweHeader(String jwe) throws Exception {
        String encoded = jwe.split("\\.")[0];
        byte[] decoded = java.util.Base64.getUrlDecoder().decode(encoded);
        return new JSONObject(new String(decoded, StandardCharsets.UTF_8));
    }
}
