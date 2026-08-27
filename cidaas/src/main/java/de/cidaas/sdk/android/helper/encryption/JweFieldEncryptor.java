package de.cidaas.sdk.android.helper.encryption;

import androidx.annotation.NonNull;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.jwk.ECKey;

import java.util.UUID;

/**
 * Builds compact JWE (ECDH-ES + A256GCM) with {@code kid}, {@code iat}, and {@code jti}
 * as required by verification-actions-srv decryption.
 */
public final class JweFieldEncryptor {

    private JweFieldEncryptor() {
    }

    @NonNull
    public static String encrypt(@NonNull String plaintext, @NonNull ECKey encryptionPublicKey) throws Exception {
        if (encryptionPublicKey.getKeyID() == null || encryptionPublicKey.getKeyID().trim().isEmpty()) {
            throw new IllegalArgumentException("Encryption JWK must include kid");
        }

        long iat = System.currentTimeMillis() / 1000L;
        String jti = UUID.randomUUID().toString();

        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES, EncryptionMethod.A256GCM)
                .keyID(encryptionPublicKey.getKeyID())
                .customParam("iat", iat)
                .customParam("jti", jti)
                .build();

        JWEObject jweObject = new JWEObject(header, new Payload(plaintext));
        jweObject.encrypt(new ECDHEncrypter(encryptionPublicKey.toECPublicKey()));
        return jweObject.serialize();
    }
}
