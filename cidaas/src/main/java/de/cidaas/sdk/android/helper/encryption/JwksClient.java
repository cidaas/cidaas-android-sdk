package de.cidaas.sdk.android.helper.encryption;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.cidaas.sdk.android.helper.general.CidaasHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Fetches and caches JWKS from {@code {baseUrl}/.well-known/jwks.json}.
 *
 * <p>HTTP is never performed on the Android main thread.
 * Fresh entries are reused for {@link #CACHE_TTL_MS}. After expiry, the last JWKS for the same
 * domain is still returned on the main thread (stale-while-revalidate) while a background refresh
 * runs. Only a completely empty cache on the main thread fails fast — call
 * {@link #prefetchAsync} (also done by {@code Cidaas.setEncryptionEnabled(true)}) at startup.
 */
public final class JwksClient {

    private static final String TAG = "CIDAAS_JWKS";
    static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(15);

    private static final AtomicReference<CachedJwks> CACHE = new AtomicReference<>();
    private static final AtomicBoolean REFRESH_SCHEDULED = new AtomicBoolean(false);
    private static final ExecutorService FETCH_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cidaas-jwks-fetch");
        t.setDaemon(true);
        return t;
    });

    private static volatile OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private JwksClient() {
    }

    /** Clears cached JWKS (e.g. after domain change). */
    public static void clearCache() {
        CACHE.set(null);
        REFRESH_SCHEDULED.set(false);
    }

    /** For unit tests. */
    static void setHttpClient(@NonNull OkHttpClient client) {
        httpClient = client;
    }

    @VisibleForTesting
    static void putCacheForTest(@NonNull String baseUrl, @NonNull String jwksJson, long fetchedAtMs) {
        CACHE.set(new CachedJwks(normalizeBaseUrl(baseUrl), jwksJson, fetchedAtMs));
    }

    /**
     * Warm the JWKS cache on a background thread. Safe to call from the UI thread.
     */
    public static void prefetchAsync(@Nullable String baseUrlOverride) {
        final String baseUrl = normalizeBaseUrl(baseUrlOverride != null && !baseUrlOverride.isEmpty()
                ? baseUrlOverride
                : CidaasHelper.baseurl);
        if (baseUrl.isEmpty()) {
            Log.w(TAG, "prefetchAsync skipped: domain URL not configured");
            return;
        }
        FETCH_EXECUTOR.execute(() -> {
            try {
                // Background path always refreshes when not fresh.
                String json = getJwksJson(baseUrl);
                Log.i(TAG, "JWKS prefetch OK for " + baseUrl + " (" + json.length() + " bytes)");
            } catch (Exception e) {
                Log.e(TAG, "JWKS prefetch failed for " + baseUrl + ": " + e.getMessage(), e);
            }
        });
    }

    @NonNull
    public static String getJwksJson(@Nullable String baseUrlOverride) throws IOException {
        String baseUrl = normalizeBaseUrl(baseUrlOverride != null && !baseUrlOverride.isEmpty()
                ? baseUrlOverride
                : CidaasHelper.baseurl);
        if (baseUrl.isEmpty()) {
            throw new IOException("Domain URL is not configured");
        }

        String fresh = readFreshCache(baseUrl);
        if (fresh != null) {
            return fresh;
        }

        String stale = readStaleCache(baseUrl);
        if (isMainThread()) {
            if (stale != null) {
                // TTL expired (e.g. prefetch at 10:00, login at 10:17): encrypt with last keys,
                // refresh off the main thread so the user is not blocked / ANR'd.
                scheduleBackgroundRefresh(baseUrl);
                Log.i(TAG, "Using stale JWKS for " + baseUrl + " while refreshing in background");
                return stale;
            }
            throw new IOException(
                    "JWKS cache is empty and encryption was requested on the main thread. "
                            + "Call JwksClient.prefetchAsync() or Cidaas.setEncryptionEnabled(true) "
                            + "before performing sensitive operations.");
        }

        return fetchFromNetwork(baseUrl);
    }

    @Nullable
    private static String readFreshCache(@NonNull String baseUrl) {
        CachedJwks cached = CACHE.get();
        if (cached == null
                || !cached.baseUrl.equals(baseUrl)
                || cached.jwksJson == null
                || cached.jwksJson.isEmpty()) {
            return null;
        }
        if ((System.currentTimeMillis() - cached.fetchedAtMs) >= CACHE_TTL_MS) {
            return null;
        }
        return cached.jwksJson;
    }

    /** Last successful JWKS for this domain even if past {@link #CACHE_TTL_MS}. */
    @Nullable
    private static String readStaleCache(@NonNull String baseUrl) {
        CachedJwks cached = CACHE.get();
        if (cached == null
                || !cached.baseUrl.equals(baseUrl)
                || cached.jwksJson == null
                || cached.jwksJson.isEmpty()) {
            return null;
        }
        return cached.jwksJson;
    }

    private static void scheduleBackgroundRefresh(@NonNull String baseUrl) {
        if (!REFRESH_SCHEDULED.compareAndSet(false, true)) {
            return;
        }
        FETCH_EXECUTOR.execute(() -> {
            try {
                fetchFromNetwork(baseUrl);
                Log.i(TAG, "JWKS background refresh OK for " + baseUrl);
            } catch (Exception e) {
                Log.e(TAG, "JWKS background refresh failed for " + baseUrl + ": " + e.getMessage(), e);
            } finally {
                REFRESH_SCHEDULED.set(false);
            }
        });
    }

    @NonNull
    private static String fetchFromNetwork(@NonNull String baseUrl) throws IOException {
        String fresh = readFreshCache(baseUrl);
        if (fresh != null) {
            return fresh;
        }

        String url = baseUrl + "/.well-known/jwks.json";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Log.i(TAG, "Fetching JWKS " + url);
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("JWKS fetch failed with HTTP " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("JWKS response body is empty");
            }
            String jwksJson = body.string();
            if (jwksJson.trim().isEmpty()) {
                throw new IOException("JWKS response body is empty");
            }
            CACHE.set(new CachedJwks(baseUrl, jwksJson, System.currentTimeMillis()));
            return jwksJson;
        }
    }

    private static boolean isMainThread() {
        try {
            return Looper.getMainLooper().getThread() == Thread.currentThread();
        } catch (Exception ignored) {
            return false;
        }
    }

    @NonNull
    private static String normalizeBaseUrl(@Nullable String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static final class CachedJwks {
        final String baseUrl;
        final String jwksJson;
        final long fetchedAtMs;

        CachedJwks(String baseUrl, String jwksJson, long fetchedAtMs) {
            this.baseUrl = baseUrl;
            this.jwksJson = jwksJson;
            this.fetchedAtMs = fetchedAtMs;
        }
    }
}
