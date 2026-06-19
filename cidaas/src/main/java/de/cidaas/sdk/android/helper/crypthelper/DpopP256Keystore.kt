package de.cidaas.sdk.android.helper.crypthelper

import android.content.Context
import android.security.keystore.KeyProperties
import org.json.JSONObject
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.UUID

/**
 * Non-biometric P-256 key in Android Keystore for DPoP proof JWTs during device registration.
 * New keys prefer **StrongBox** (API 28+); if unavailable, generation falls back to the default
 * Keystore implementation (typically **TEE**-backed on production devices).
 */
object DpopP256Keystore {

    private const val alias = "cidaas.device.dpop.ecdsa"

    @JvmStatic
    fun ensureKey(context: Context) {
        KeystoreEcP256StrongBoxHelper.ensureEcP256SignKey(alias) {
            setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            setDigests(KeyProperties.DIGEST_SHA256)
            setUserAuthenticationRequired(false)
        }
    }

    @JvmStatic
    fun jwkThumbprintSha256(context: Context): String {
        ensureKey(context)
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val der = ks.getCertificate(alias).publicKey.encoded
        return EcP256JwkThumbprint.sha256ThumbprintFromSpkiDer(der)
    }

    @JvmStatic
    fun proofJwt(context: Context, httpMethod: String, requestUrlString: String): String =
        proofJwtInternal(context, httpMethod, requestUrlString, null, null, null, null)

    /**
     * DPoP proof for device-registration verification: standard DPoP claims plus {@code session_id},
     * {@code nonce}, {@code attestation} (Play Integrity / App Check token), and {@code biometric_public_key_der}
     * (standard Base64 of SubjectPublicKeyInfo DER).
     */
    @JvmStatic
    fun proofJwtForDeviceRegistration(
        context: Context,
        httpMethod: String,
        requestUrlString: String,
        sessionId: String,
        initiationNonce: String,
        appAttestationToken: String,
        biometricPublicKeyDerBase64: String,
    ): String = proofJwtInternal(
        context,
        httpMethod,
        requestUrlString,
        sessionId,
        initiationNonce,
        appAttestationToken,
        biometricPublicKeyDerBase64,
    )

    private fun proofJwtInternal(
        context: Context,
        httpMethod: String,
        requestUrlString: String,
        sessionId: String?,
        initiationNonce: String?,
        appAttestationToken: String?,
        biometricPublicKeyDerBase64: String?,
    ): String {
        ensureKey(context)
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        val priv = entry.privateKey
        val pub = entry.certificate.publicKey as ECPublicKey
        val jwk = JSONObject(EcP256JwkThumbprint.canonicalJwkJson(pub))
        val header = JSONObject()
            .put("typ", "dpop+jwt")
            .put("alg", "ES256")
            .put("jwk", jwk)
        val iat = System.currentTimeMillis() / 1000
        val jti = UUID.randomUUID().toString().lowercase()
        val payload = JSONObject()
            .put("htm", httpMethod.uppercase())
            .put("htu", canonicalHtu(requestUrlString))
            .put("iat", iat)
            .put("jti", jti)
        if (!sessionId.isNullOrEmpty()) {
            payload.put("session_id", sessionId)
        }
        if (!initiationNonce.isNullOrEmpty()) {
            payload.put("nonce", initiationNonce)
        }
        if (!appAttestationToken.isNullOrEmpty()) {
            payload.put("attestation", appAttestationToken)
        }
        if (!biometricPublicKeyDerBase64.isNullOrEmpty()) {
            payload.put("biometric_public_key_der", biometricPublicKeyDerBase64)
        }
        val headerB64 = header.toString().toByteArray(StandardCharsets.UTF_8).toBase64UrlNoPad()
        val payloadB64 = payload.toString().toByteArray(StandardCharsets.UTF_8).toBase64UrlNoPad()
        val signingInput = "$headerB64.$payloadB64"
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(priv)
        sig.update(signingInput.toByteArray(StandardCharsets.US_ASCII))
        val derSig = sig.sign()
        val joseSig = derToJoseEs256(derSig)
        return "$signingInput.${joseSig.toBase64UrlNoPad()}"
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
}
