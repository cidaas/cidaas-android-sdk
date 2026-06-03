package de.cidaas.sdk.android.helper.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.util.List;

import de.cidaas.sdk.android.helper.general.CidaasHelper;
import okhttp3.CertificatePinner;

public final class CertificatePinningHelper {

    private CertificatePinningHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Nullable
    public static CertificatePinner buildCertificatePinner() {
        CertificatePinningConfig config = CidaasHelper.certificatePinningConfig;
        if (config == null || !config.hasPins()) {
            return null;
        }

        String host = config.getHost();
        if (host == null || host.isEmpty()) {
            host = extractHost(CidaasHelper.baseurl);
        }
        if (host == null || host.isEmpty()) {
            return null;
        }

        CertificatePinner.Builder builder = new CertificatePinner.Builder();
        List<String> pins = config.getPinHashes();
        builder.add(host, pins.toArray(new String[0]));
        return builder.build();
    }

    @Nullable
    public static String extractHost(@Nullable String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return null;
        }
        try {
            URI uri = URI.create(baseUrl.trim());
            return uri.getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
