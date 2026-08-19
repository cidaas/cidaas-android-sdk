package de.cidaas.sdk.android.helper.network;

import android.os.Build;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.cidaas.sdk.android.BuildConfig;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Sets a consistent {@code User-Agent} header on every outgoing request.
 * The value is computed lazily on first use so that {@link CidaasHelper#APP_NAME}
 * and {@link CidaasHelper#APP_VERSION} are populated by SDK init before being read.
 * Caching is deferred until SDK init completes ({@link CidaasHelper#IS_SETURL_CALLED})
 * to avoid locking in default values.
 */
public final class UserAgentInterceptor implements Interceptor {

    private static volatile String cachedUserAgent;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        if (original.header("User-Agent") != null) {
            return chain.proceed(original);
        }
        Request request = original.newBuilder()
                .header("User-Agent", getUserAgent())
                .build();
        return chain.proceed(request);
    }

    /**
     * Clears the cached User-Agent so it is recomputed on the next request.
     * Call from {@link CidaasHelper#initialiseObject()} when SDK metadata may have changed.
     */
    public static void resetCache() {
        cachedUserAgent = null;
    }

    /**
     * Returns the current User-Agent string. Lazily computed from static SDK
     * metadata; cached once SDK init has completed (indicated by
     * {@link CidaasHelper#IS_SETURL_CALLED}).
     *
     * <p>If called before SDK init (before {@code Cidaas.setURL()}), returns a
     * best-effort string using default metadata. The value is not cached in
     * that case and will be recomputed on the next call.
     */
    public static String getUserAgent() {
        String ua = cachedUserAgent;
        if (ua != null) return ua;

        String built = buildUserAgent();

        if (CidaasHelper.IS_SETURL_CALLED) {
            cachedUserAgent = built;
        }
        return built;
    }

    private static String buildUserAgent() {
        String built = "Cidaas-" + CidaasHelper.APP_NAME
                + "/" + CidaasHelper.APP_VERSION + "_" + BuildConfig.VERSION_NAME
                + " Make:" + Build.MANUFACTURER + "_" + Build.DEVICE
                + " Model:" + Build.MODEL;
        String baseUa = System.getProperty("http.agent");
        if (baseUa != null) {
            built = built + " " + baseUa;
        }
        return built.replaceAll("[^\\x20-\\x7E]", "");
    }
}
