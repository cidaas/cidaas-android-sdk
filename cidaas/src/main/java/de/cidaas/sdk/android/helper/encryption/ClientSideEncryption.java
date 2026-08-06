package de.cidaas.sdk.android.helper.encryption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nimbusds.jose.jwk.ECKey;

import java.util.Locale;

import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.library.common.Privacy;
import timber.log.Timber;

/**
 * Client-side encryption facade used before sensitive fields are sent on the wire.
 */
public final class ClientSideEncryption {

    private ClientSideEncryption() {
    }

    /**
     * When encryption is disabled or {@code value} is null/empty, returns {@code value} unchanged.
     * Otherwise returns a compact JWE string (with {@code kid}, {@code iat}, {@code jti}).
     *
     * <p><strong>Fail-closed:</strong> when encryption is enabled, JWKS fetch / key selection / JWE
     * failures throw {@link IllegalStateException}. There is no plaintext fallback — callers' try/catch
     * surfaces this as a service failure. Prefer warming the cache with
     * {@link JwksClient#prefetchAsync} or {@code Cidaas.setEncryptionEnabled(true)} at startup.
     * After the soft TTL expires, a previously fetched JWKS may still be used on the main thread while
     * a background refresh runs (stale-while-revalidate).
     *
     * @param baseUrlOverride optional domain URL; falls back to {@link de.cidaas.sdk.android.helper.general.CidaasHelper#baseurl}
     * @throws IllegalStateException if encryption is on and no JWKS is available (never prefetched),
     *         has no suitable key, or JWE construction fails
     */
    @Nullable
    public static String encryptIfEnabled(@Nullable String value, @Nullable String baseUrlOverride) {
        if (!Privacy.isEncryptionEnabled()) {
            return value;
        }
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            String jwksJson = JwksClient.getJwksJson(baseUrlOverride);
            ECKey encKey = JwksEncKeySelector.selectEncryptionKey(jwksJson);
            if (encKey == null) {
                throw new IllegalStateException("No use=enc ECDH-ES P-256 key found in JWKS");
            }
            return JweFieldEncryptor.encrypt(value, encKey);
        } catch (Exception e) {
            String detail = describe(e);
            Timber.e(e, "Client-side encryption failed: %s", detail);
            throw new IllegalStateException("Client-side encryption failed: " + detail, e);
        }
    }

    @Nullable
    public static String encryptIfEnabled(@Nullable String value) {
        return encryptIfEnabled(value, null);
    }

    /**
     * Encrypts verification {@code pass_code} when CSE is on — only for BACKUPCODE and PATTERN.
     * EMAIL/SMS/IVR OTPs and other methods stay plaintext.
     */
    @Nullable
    public static String encryptPassCodeIfEnabled(@Nullable String passCode, @Nullable String verificationType) {
        if (!shouldEncryptVerificationPassCode(verificationType)) {
            return passCode;
        }
        return encryptIfEnabled(passCode);
    }

    /**
     * Only BACKUPCODE and PATTERN {@code pass_code} values are client-encrypted.
     * Passwords use {@link #encryptIfEnabled(String)} separately (login / register / change password).
     */
    public static boolean shouldEncryptVerificationPassCode(@Nullable String verificationType) {
        if (verificationType == null || verificationType.isEmpty()) {
            return false;
        }
        String type = verificationType.trim().toUpperCase(Locale.US);
        return AuthenticationType.BACKUPCODE.equals(type) || AuthenticationType.PATTERN.equals(type);
    }

    /**
     * Encrypts in-place style helper for non-null fields; returns original when encryption is off.
     */
    @NonNull
    public static String requireEncryptIfEnabled(@NonNull String value, @Nullable String baseUrlOverride) {
        String encrypted = encryptIfEnabled(value, baseUrlOverride);
        return encrypted != null ? encrypted : value;
    }

    @NonNull
    private static String describe(@Nullable Throwable e) {
        if (e == null) {
            return "unknown error";
        }
        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName());
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            sb.append(": ").append(e.getMessage());
        }
        Throwable cause = e.getCause();
        if (cause != null && cause != e) {
            sb.append(" | cause ").append(cause.getClass().getSimpleName());
            if (cause.getMessage() != null && !cause.getMessage().isEmpty()) {
                sb.append(": ").append(cause.getMessage());
            }
        }
        return sb.toString();
    }
}
