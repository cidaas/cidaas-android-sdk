package de.cidaas.sdk.android.helper.crypthelper

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import timber.log.Timber

/**
 * Obtains a Firebase App Check token for backends that expect Firebase-based app attestation
 * during device registration (provider=firebase).
 *
 * The host application must call [FirebaseApp.initializeApp] before requesting a token.
 *
 * **Debuggable apps** (typical local debug installs): installs [DebugAppCheckProviderFactory] so you can
 * copy the debug secret from logcat and register it under Firebase Console → App Check → Manage debug tokens.
 *
 * **Non-debuggable builds** (typical release): installs [PlayIntegrityAppCheckProviderFactory].
 *
 * If the host app already calls [FirebaseAppCheck.installAppCheckProviderFactory], installation here may be
 * skipped when the SDK detects an [IllegalStateException] from a duplicate install.
 *
 * **403 App attestation failed (server):** ensure initiation `provider` matches the token you send (`google` →
 * Play Integrity JWT in `attestation`; `firebase` → App Check JWT). For debug builds, register the App Check debug
 * token in Firebase Console. For `google`, set the Play Integrity cloud project number and link the app in Play Console.
 */
object FirebaseAppAttestationHelper {

    private val installLock = Any()

    @Volatile
    private var appCheckProviderInstalled = false

    @JvmStatic
    private fun ensureAppCheckProvider(context: Context) {
        if (appCheckProviderInstalled) return
        synchronized(installLock) {
            if (appCheckProviderInstalled) return
            val appCheck = FirebaseAppCheck.getInstance()
            val factory =
                if (isAppDebuggable(context)) {
                    Timber.d(
                        "Firebase App Check: using DebugAppCheckProviderFactory. " +
                            "Find the debug token in logcat and add it under Firebase Console → App Check → debug tokens.",
                    )
                    DebugAppCheckProviderFactory.getInstance()
                } else {
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                }
            try {
                appCheck.installAppCheckProviderFactory(factory)
                Log.w("AppCheck", "Initialized DEBUG provider (debug build). Register the printed debug token in Firebase Console.")
            } catch (e: IllegalStateException) {
                Timber.d(
                    e,
                    "Firebase App Check: installAppCheckProviderFactory did not run (often already set in Application).",
                )
            }
            appCheckProviderInstalled = true
        }
    }

    private fun isAppDebuggable(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    @JvmStatic
    fun requestToken(context: Context, listener: PlayIntegrityTokenListener) {
        try {
            FirebaseApp.getInstance()
        } catch (_: IllegalStateException) {
            listener.onFailure(
                IllegalStateException(
                    "Firebase is not initialized. Call FirebaseApp.initializeApp(context) before device registration with provider=firebase.",
                ),
            )
            return
        }
        ensureAppCheckProvider(context.applicationContext)
        // Force refresh so the token is minted after installAppCheckProviderFactory (avoids stale/empty cache).
        FirebaseAppCheck.getInstance()
            .getAppCheckToken(true)
            .addOnSuccessListener { token -> listener.onSuccess(token.token) }
            .addOnFailureListener {
                e ->

                Log.w("AppCheck", "App check token failed")
                listener.onFailure(e) }
    }
}
