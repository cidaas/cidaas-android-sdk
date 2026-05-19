package de.cidaas.sdk.android.helper.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Optional SHA-256 certificate pins for the cidaas instance host.
 * Pin hashes must use OkHttp format, e.g. {@code sha256/AAAAAAAA...=}.
 */
public final class CertificatePinningConfig {

    @Nullable
    private final String host;
    @NonNull
    private final List<String> pinHashes;

    public CertificatePinningConfig(@Nullable String host, @NonNull String... pinHashes) {
        if (pinHashes.length == 0) {
            throw new IllegalArgumentException("At least one pin hash is required");
        }
        for (String pin : pinHashes) {
            if (pin == null || pin.isEmpty()) {
                throw new IllegalArgumentException("Pin hash must not be null or empty");
            }
            if (!pin.startsWith("sha256/")) {
                throw new IllegalArgumentException("Pin hash must start with sha256/: " + pin);
            }
        }
        this.host = host;
        this.pinHashes = Collections.unmodifiableList(Arrays.asList(pinHashes));
    }

    @Nullable
    public String getHost() {
        return host;
    }

    @NonNull
    public List<String> getPinHashes() {
        return pinHashes;
    }

    public boolean hasPins() {
        return !pinHashes.isEmpty();
    }
}
