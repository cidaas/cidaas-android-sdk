package de.cidaas.sdk.android.helper.crypthelper

import android.content.Context
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.json.JSONObject
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * EC P-256 in Android Keystore with biometric auth per signature. Used for device registration
 * and MFA fingerprint enrollment/login (same {@link #DEVICE_BIOMETRIC_KEY_ALIAS}).
 */
class BiometricP256Signer @JvmOverloads constructor(
    private val context: Context,
    private val keyAlias: String = DEVICE_BIOMETRIC_KEY_ALIAS,
) {

    fun ensureKey() {
        KeystoreEcP256StrongBoxHelper.ensureEcP256SignKey(keyAlias) {
            setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            setDigests(KeyProperties.DIGEST_NONE)
            setUserAuthenticationRequired(true)
            setInvalidatedByBiometricEnrollment(true)
        }
    }

    fun jwkThumbprintSha256(): String {
        ensureKey()
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val der = ks.getCertificate(keyAlias).publicKey.encoded
        return EcP256JwkThumbprint.sha256ThumbprintFromSpkiDer(der)
    }

    /** SubjectPublicKeyInfo DER of the biometric EC key, standard Base64 (RFC 4648, no line wraps). */
    fun publicKeyDerBase64(): String {
        ensureKey()
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val der = ks.getCertificate(keyAlias).publicKey.encoded
        return android.util.Base64.encodeToString(der, android.util.Base64.NO_WRAP)
    }

    fun proofJwt(
        activity: FragmentActivity,
        httpMethod: String,
        requestUrlString: String,
        listener: BiometricProofListener,
    ) {
        activity.runOnUiThread {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    listener.onSuccess(proofJwt(activity, httpMethod, requestUrlString))
                } catch (e: Exception) {
                    listener.onFailure(e)
                }
            }
        }
    }

    suspend fun proofJwt(activity: FragmentActivity, httpMethod: String, requestUrlString: String): String {
        ensureKey()
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
        val pub = entry.certificate.publicKey as ECPublicKey
        val jwk = JSONObject(EcP256JwkThumbprint.canonicalJwkJson(pub))
        val header = JSONObject()
            .put("typ", "biometric+jwt")
            .put("alg", "ES256")
            .put("jwk", jwk)
        val iat = System.currentTimeMillis() / 1000
        val jti = UUID.randomUUID().toString().lowercase()
        val payload = JSONObject()
            .put("htm", httpMethod.uppercase())
            .put("htu", canonicalHtu(requestUrlString))
            .put("iat", iat)
            .put("jti", jti)
        val headerB64 = header.toString().toByteArray(StandardCharsets.UTF_8).toBase64UrlNoPad()
        val payloadB64 = payload.toString().toByteArray(StandardCharsets.UTF_8).toBase64UrlNoPad()
        val signingInput = "$headerB64.$payloadB64"
        val digest = MessageDigest.getInstance("SHA-256").digest(signingInput.toByteArray(StandardCharsets.US_ASCII))
        val derSig = signDigest(activity, digest)
        val joseSig = derToJoseEs256(derSig)
        return "$signingInput.${joseSig.toBase64UrlNoPad()}"
    }

    private suspend fun signDigest(activity: FragmentActivity, digest32: ByteArray): ByteArray =
        suspendCoroutine { cont ->
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify device")
                .setSubtitle("Biometric required to register this device")
                .setNegativeButtonText("Cancel")
                .build()

            val executor = ContextCompat.getMainExecutor(context)
            val cryptoObject = try {
                val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                val priv = ks.getKey(keyAlias, null) as java.security.PrivateKey
                val sig = Signature.getInstance("NONEwithECDSA")
                sig.initSign(priv)
                BiometricPrompt.CryptoObject(sig)
            } catch (e: Exception) {
                cont.resumeWithException(e)
                return@suspendCoroutine
            }
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        try {
                            val sig = result.cryptoObject?.signature
                                ?: throw IllegalStateException("missing signature cryptoObject")
                            sig.update(digest32)
                            cont.resume(sig.sign())
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        cont.resumeWithException(IllegalStateException(errString.toString()))
                    }
                },
            )
            prompt.authenticate(promptInfo, cryptoObject)
        }

    private fun canonicalHtu(urlString: String): String {
        val u = URI(urlString)
        val scheme = u.scheme ?: "http"
        val host = u.host ?: return urlString.trim()
        val port = u.port
        val path = u.rawPath.ifEmpty { "/" }
        val authority = if (port > 0 && !((scheme == "http" && port == 80) || (scheme == "https" && port == 443))) {
            "$host:$port"
        } else {
            host
        }
        return "$scheme://$authority$path"
    }

    private fun derToJoseEs256(der: ByteArray): ByteArray {
        var idx = 0
        fun readByte(): Int {
            require(idx < der.size)
            return der[idx++].toInt() and 0xff
        }
        fun readLen(): Int {
            val b = readByte()
            if (b and 0x80 == 0) return b
            val n = b and 0x7f
            require(n in 1..2)
            var v = 0
            repeat(n) { v = (v shl 8) or readByte() }
            return v
        }
        require(readByte() == 0x30)
        readLen()
        require(readByte() == 0x02)
        val rLen = readLen()
        val r = der.copyOfRange(idx, idx + rLen)
        idx += rLen
        require(readByte() == 0x02)
        val sLen = readLen()
        val s = der.copyOfRange(idx, idx + sLen)
        return toFixed32(r) + toFixed32(s)
    }

    private fun toFixed32(v: ByteArray): ByteArray {
        var b = v
        while (b.size > 32 && b[0] == 0.toByte()) {
            b = b.copyOfRange(1, b.size)
        }
        require(b.size <= 32) { "unexpected int length ${b.size}" }
        if (b.size < 32) {
            return ByteArray(32 - b.size) { 0 } + b
        }
        return b.copyOf(32)
    }

    private fun ByteArray.toBase64UrlNoPad(): String =
        android.util.Base64.encodeToString(this, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            .trimEnd('=')

    companion object {
        /** Keystore alias shared with device registration; MFA fingerprint must use the same key. */
        const val DEVICE_BIOMETRIC_KEY_ALIAS = "cidaas.device.biometric.ecdsa"

        /** @deprecated Use {@link #DEVICE_BIOMETRIC_KEY_ALIAS} — same alias as device registration. */
        const val VERIFICATION_FINGERPRINT_KEY_ALIAS = DEVICE_BIOMETRIC_KEY_ALIAS

        @JvmStatic
        fun decodeChallengeB64(challengeB64: String): ByteArray {
            val t = challengeB64.trim()
            runCatching {
                android.util.Base64.decode(t, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            }.getOrNull()?.let { return it }
            runCatching {
                android.util.Base64.decode(t, android.util.Base64.DEFAULT)
            }.getOrNull()?.let { return it }
            throw IllegalArgumentException("invalid challenge base64")
        }
    }
}

interface BiometricProofListener {
    fun onSuccess(proof: String)
    fun onFailure(error: Throwable)
}
