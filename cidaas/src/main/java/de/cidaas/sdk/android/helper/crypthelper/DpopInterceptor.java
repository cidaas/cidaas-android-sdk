package de.cidaas.sdk.android.helper.crypthelper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Adds a RFC 9449 {@code DPoP} proof JWT to every request when {@link DpopConfig} is enabled.
 * Skips if the request already has a {@code DPoP} header.
 *
 * <p>When an access token is present ({@code access_token} header or {@code Authorization:
 * Bearer|DPoP …}), the proof includes the {@code ath} claim (SHA-256 hash of the token).
 */
public final class DpopInterceptor implements Interceptor {

    private static final String HEADER_DPOP = "DPoP";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ACCESS_TOKEN = "access_token";

    @Nullable
    private final Context context;

    public DpopInterceptor(@Nullable Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        if (!DpopConfig.isEnabled()) {
            return chain.proceed(request);
        }

        if (request.header(HEADER_DPOP) != null
                && !request.header(HEADER_DPOP).isEmpty()) {
            return chain.proceed(request);
        }

        Context ctx = context != null
                ? context
                : DpopConfig.getAppContext();

        if (ctx == null) {
            Timber.e("DpopInterceptor: no Context; skipping DPoP header");
            return chain.proceed(request);
        }

        try {
            String accessToken = extractAccessToken(request);
            String proof = DpopP256Keystore.proofJwt(
                    ctx,
                    request.method(),
                    request.url().toString(),
                    accessToken
            );

            Request withDpop = request.newBuilder()
                    .header(HEADER_DPOP, proof)
                    .build();
            return chain.proceed(withDpop);

        } catch (Exception e) {
            Timber.e(e, "DpopInterceptor: failed to generate DPoP proof; proceeding without header");
            return chain.proceed(request);
        }
    }

    /**
     * Prefer this SDK's custom {@code access_token} header; also accept RFC 6750 / RFC 9449
     * {@code Authorization: Bearer|DPoP &lt;token&gt;}.
     */
    @Nullable
    static String extractAccessToken(@NonNull Request request) {
        String custom = request.header(HEADER_ACCESS_TOKEN);
        if (custom != null && !custom.isEmpty()) {
            return custom;
        }
        String authorization = request.header(HEADER_AUTHORIZATION);
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        String[] parts = authorization.split(" ", 2);
        if (parts.length < 2) {
            return null;
        }
        String scheme = parts[0];
        if (!"Bearer".equalsIgnoreCase(scheme) && !"DPoP".equalsIgnoreCase(scheme)) {
            return null;
        }
        String token = parts[1].trim();
        return token.isEmpty() ? null : token;
    }

}
