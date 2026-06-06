package de.cidaas.sdk.android.cidaasverification.util;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 digests as lowercase hexadecimal (64 characters), UTF-8 input encoding.
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /**
     * @return 64-character lowercase hex string (no {@code 0x} prefix)
     */
    @NonNull
    public static String sha256HexUtf8(@NonNull String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return toLowerHex(digest);
    }

    @NonNull
    private static String toLowerHex(@NonNull byte[] digest) {
        char[] alphabet = "0123456789abcdef".toCharArray();
        char[] out = new char[digest.length * 2];
        for (int i = 0, j = 0; i < digest.length; i++) {
            int v = digest[i] & 0xFF;
            out[j++] = alphabet[v >>> 4];
            out[j++] = alphabet[v & 0x0F];
        }
        return new String(out);
    }
}
