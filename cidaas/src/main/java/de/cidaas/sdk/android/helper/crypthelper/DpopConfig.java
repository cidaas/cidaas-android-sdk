package de.cidaas.sdk.android.helper.crypthelper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

/**
 * Process-wide DPoP (RFC 9449) toggle. When enabled, every OkHttp/Retrofit call from the SDK
 * receives a {@code DPoP} proof header via {@link DpopInterceptor}, and hosted login / request-id
 * flows include {@code dpop_jkt}.
 *
 * <p>Default is {@code false} (backward compatible). Enable once via
 * {@link de.cidaas.sdk.android.Cidaas#setDpopEnabled(boolean)}.
 */
public final class DpopConfig {

    private static final String META_PREFS = "cidaas_storage_meta";
    private static final String META_ENABLED = "dpop_enabled";

    private static volatile boolean enabled;
    private static volatile Context appContext;

    private DpopConfig() {
    }

    /**
     * Loads the persisted flag. Invoked from {@link de.cidaas.sdk.android.helper.general.DBHelper#setConfig(Context)}.
     */
    public static synchronized void init(@NonNull Context context) {
        appContext = context.getApplicationContext();
        SharedPreferences meta = appContext.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
        enabled = meta.getBoolean(META_ENABLED, false);
        if (enabled) {
            try {
                DpopP256Keystore.ensureKey(appContext);
            } catch (Exception e) {
                // Key is created lazily on first proof; interceptor proceeds without DPoP if that fails.
                Timber.e(e, "DpopConfig: DPoP key not ready at init; will retry on first proof");
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables DPoP for all SDK Retrofit/OkHttp requests and related auth params.
     *
     * @throws IllegalStateException if the DPoP key cannot be prepared while enabling
     */
    public static synchronized void setEnabled(@NonNull Context context, boolean secure) {
        appContext = context.getApplicationContext();
        SharedPreferences meta = appContext.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
        if (secure) {
            try {
                DpopP256Keystore.ensureKey(appContext);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize DPoP keystore key", e);
            }
        }
        enabled = secure;
        meta.edit().putBoolean(META_ENABLED, secure).apply();
    }

    @Nullable
    public static Context getAppContext() {
        return appContext;
    }
}
