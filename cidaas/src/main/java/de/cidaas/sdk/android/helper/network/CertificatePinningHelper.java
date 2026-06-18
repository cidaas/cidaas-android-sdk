package de.cidaas.sdk.android.helper.network;

import android.content.Context;

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

    /**
     * Builds a pinner using {@link CidaasHelper#networkSecurityPinningXmlResId} (parsed NSC XML) when set,
     * otherwise {@link CidaasHelper#certificatePinningConfig}. Uses {@link CidaasHelper#shared} for
     * {@code Context} when {@code context} is null.
     */
    @Nullable
    public static CertificatePinner buildCertificatePinner() {
        Context ctx = CidaasHelper.shared != null ? CidaasHelper.shared.context : null;
        return buildCertificatePinner(ctx);
    }

    @Nullable
    public static CertificatePinner buildCertificatePinner(@Nullable Context context) {
        if (CidaasHelper.networkSecurityPinningXmlResId != 0 && context != null) {
            CertificatePinner fromXml = NetworkSecurityPinConfigParser.parseFromResource(
                    context, CidaasHelper.networkSecurityPinningXmlResId);
            if (fromXml != null) {
                return fromXml;
            }
        }

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
