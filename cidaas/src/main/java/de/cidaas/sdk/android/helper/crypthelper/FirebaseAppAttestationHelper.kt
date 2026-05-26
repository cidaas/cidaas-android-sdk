package de.cidaas.sdk.android.helper.crypthelper

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck

/**
 * Obtains a Firebase App Check token for backends that expect Firebase-based app attestation
 * during device registration (provider=firebase).
 *
 * The host application must initialize Firebase and register an App Check provider
 * (for example Play Integrity via [com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory]).
 */
object FirebaseAppAttestationHelper {

    @JvmStatic
    fun requestToken(listener: PlayIntegrityTokenListener) {
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
        FirebaseAppCheck.getInstance()
            .getAppCheckToken(false)
            .addOnSuccessListener { token -> listener.onSuccess(token.token) }
            .addOnFailureListener { e -> listener.onFailure(e) }
    }
}
