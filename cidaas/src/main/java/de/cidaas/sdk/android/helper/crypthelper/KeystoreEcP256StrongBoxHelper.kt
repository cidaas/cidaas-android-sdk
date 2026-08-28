package de.cidaas.sdk.android.helper.crypthelper

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.ProviderException

/**
 * Generates EC P-256 signing keys in Android Keystore, preferring **StrongBox** when supported
 * (API 28+), otherwise the default secure implementation (typically **TEE** on OEM devices).
 *
 * After generation, the key's actual backing is verified against [policy]. If the key doesn't
 * meet the minimum, it is deleted and a [SecurityException] is thrown.
 */
internal object KeystoreEcP256StrongBoxHelper {

    @Volatile
    private var policy: KeyBackingPolicy = KeyBackingPolicy.REQUIRE_HARDWARE

    @JvmStatic
    fun getPolicy(): KeyBackingPolicy = policy

    @JvmStatic
    fun setPolicy(p: KeyBackingPolicy) { policy = p }

    fun ensureEcP256SignKey(keyAlias: String, configure: KeyGenParameterSpec.Builder.() -> Unit) {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(keyAlias)) {
            // Intentional: enforce policy on existing keys so that a version upgrade
            // with a stricter default doesn't silently keep using a weak key.
            // Integrators who accept software-backed keys must call
            // Cidaas.setKeyBackingPolicy(BEST_AVAILABLE) before the first post-upgrade launch.
            enforcePolicy(keyAlias, ks)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateKeyPair(keyAlias, configure, preferStrongBox = true)
                enforcePolicy(keyAlias, ks)
                return
            } catch (_: StrongBoxUnavailableException) {
                // fall through to TEE / default backing
            } catch (e: ProviderException) {
                if (!rootCauseIsStrongBoxUnavailable(e)) throw e
            }
        }
        generateKeyPair(keyAlias, configure, preferStrongBox = false)
        enforcePolicy(keyAlias, ks)
    }

    /**
     * Returns the hardware backing level of an existing Keystore **private** key.
     * Only [KeyStore.PrivateKeyEntry] aliases are supported (EC/RSA keys used for DPoP and biometric signing).
     *
     * @throws IllegalStateException if [keyAlias] does not exist in the Keystore
     *         or is not a [KeyStore.PrivateKeyEntry]
     */
    fun getSecurityLevel(keyAlias: String): KeySecurityLevel {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return getSecurityLevel(keyAlias, ks)
    }

    private fun getSecurityLevel(keyAlias: String, ks: KeyStore): KeySecurityLevel {
        val entry = ks.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("No private key entry for alias: $keyAlias")
        val factory = KeyFactory.getInstance(entry.privateKey.algorithm, "AndroidKeyStore")
        val keyInfo = factory.getKeySpec(entry.privateKey, KeyInfo::class.java)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (keyInfo.securityLevel) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> KeySecurityLevel.STRONGBOX
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> KeySecurityLevel.TEE
                KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE -> KeySecurityLevel.UNKNOWN_SECURE
                else -> KeySecurityLevel.SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            if (keyInfo.isInsideSecureHardware) KeySecurityLevel.HARDWARE else KeySecurityLevel.SOFTWARE
        }
    }

    private fun enforcePolicy(keyAlias: String, ks: KeyStore) {
        val level = getSecurityLevel(keyAlias, ks)
        val violation = when (policy) {
            KeyBackingPolicy.BEST_AVAILABLE -> false
            KeyBackingPolicy.REQUIRE_HARDWARE -> !level.isHardware()
        }
        if (violation) {
            try {
                ks.deleteEntry(keyAlias)
            } catch (e: java.security.KeyStoreException) {
                throw SecurityException(
                    "Key \"$keyAlias\" backed by ${level.wireValue} does not meet policy $policy; " +
                    "automatic cleanup failed (${e.message}) — manual Keystore entry removal required"
                )
            }
            throw SecurityException(
                "Key \"$keyAlias\" backed by ${level.wireValue} does not meet policy $policy; re-enrollment required"
            )
        }
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
