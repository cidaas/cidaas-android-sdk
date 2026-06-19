package de.cidaas.sdk.android.helper.crypthelper

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

/**
 * RFC 7638 JWK thumbprint (SHA-256, Base64url, no padding) for P-256 EC keys.
 * Member order is lexicographic: {@code crv}, {@code kty}, {@code x}, {@code y}.
 */
object EcP256JwkThumbprint {

    private const val P256_COORD_BYTES = 32

    @JvmStatic
    fun sha256Thumbprint(publicKey: ECPublicKey): String =
        sha256Thumbprint(canonicalJwkJson(publicKey))

    /** Same thumbprint a verifier derives from SPKI DER ({@code SubjectPublicKeyInfo}). */
    @JvmStatic
    fun sha256ThumbprintFromSpkiDer(der: ByteArray): String {
        val pub = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(der)) as ECPublicKey
        return sha256Thumbprint(pub)
    }

    @JvmStatic
    fun canonicalJwkJson(publicKey: ECPublicKey): String {
        val w = publicKey.w
        val xStr = coordToBase64Url(w.affineX)
        val yStr = coordToBase64Url(w.affineY)
        return """{"crv":"P-256","kty":"EC","x":"$xStr","y":"$yStr"}"""
    }

    private fun sha256Thumbprint(canonicalJwkJson: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonicalJwkJson.toByteArray(StandardCharsets.UTF_8))
        return digest.toBase64UrlNoPad()
    }

    private fun coordToBase64Url(n: BigInteger): String = coordFixed32(n).toBase64UrlNoPad()

    private fun coordFixed32(n: BigInteger): ByteArray {
        var b = n.toByteArray()
        if (b.isNotEmpty() && b[0] == 0.toByte() && b.size > 1) {
            b = b.copyOfRange(1, b.size)
        }
        require(b.size <= P256_COORD_BYTES) { "unexpected EC coordinate length ${b.size}" }
        if (b.size < P256_COORD_BYTES) {
            return ByteArray(P256_COORD_BYTES - b.size) { 0 } + b
        }
        return b
    }

    private fun ByteArray.toBase64UrlNoPad(): String =
        android.util.Base64.encodeToString(this, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            .trimEnd('=')
}
