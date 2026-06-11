package de.cidaas.sdk.android.helper.crypthelper

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.ProviderException

/**
 * Generates EC P-256 signing keys in Android Keystore, preferring **StrongBox** when supported
 * (API 28+), otherwise the default secure implementation (typically **TEE** on OEM devices).
 *
 * Existing keys at [keyAlias] are left unchanged (no migration from a prior non-StrongBox key).
 */
internal object KeystoreEcP256StrongBoxHelper {

    fun ensureEcP256SignKey(keyAlias: String, configure: KeyGenParameterSpec.Builder.() -> Unit) {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(keyAlias)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateKeyPair(keyAlias, configure, preferStrongBox = true)
                return
            } catch (_: StrongBoxUnavailableException) {
                // fall through to TEE / default backing
            } catch (e: ProviderException) {
                if (!rootCauseIsStrongBoxUnavailable(e)) throw e
            }
        }
        generateKeyPair(keyAlias, configure, preferStrongBox = false)
    }

    private fun generateKeyPair(
        keyAlias: String,
        configure: KeyGenParameterSpec.Builder.() -> Unit,
        preferStrongBox: Boolean,
    ) {
        val builder = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_SIGN)
        builder.configure()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && preferStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }
        val spec = builder.build()
        val gen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        gen.initialize(spec)
        gen.generateKeyPair()
    }

    private fun rootCauseIsStrongBoxUnavailable(e: Throwable): Boolean {
        var t: Throwable? = e
        while (t != null) {
            if (t is StrongBoxUnavailableException) return true
            t = t.cause
        }
        return false
    }
}
