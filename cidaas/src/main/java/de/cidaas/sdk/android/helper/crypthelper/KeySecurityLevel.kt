package de.cidaas.sdk.android.helper.crypthelper

/**
 * Describes the hardware backing of an Android Keystore key.
 *
 * [wireValue] is a stable string for serialization into JWTs, JSON payloads, and server-side
 * risk decisions. [isHardware] is used by policy enforcement; [isStrongBox] is informational
 * (reliable only on API 31+, where the platform can distinguish StrongBox from TEE).
 */
enum class KeySecurityLevel(@JvmField val wireValue: String) {
    STRONGBOX("STRONGBOX"),
    TEE("TEE"),
    /** API 23-30: hardware-backed, but the API can't distinguish StrongBox from TEE. */
    HARDWARE("HARDWARE"),
    SOFTWARE("SOFTWARE"),
    /** Android reports the key is secure but doesn't specify how. Treated as hardware. */
    UNKNOWN_SECURE("UNKNOWN_SECURE");

    /**
     * Returns true when the key is not software-only.
     * Note: [UNKNOWN_SECURE] is treated as hardware-backed because Android reports
     * the key is in a secure environment, even though the specific type (TEE vs StrongBox)
     * cannot be determined. Integrators with strict attestation requirements should
     * check [isStrongBox] or compare [wireValue] directly.
     */
    fun isHardware(): Boolean = this != SOFTWARE

    fun isStrongBox(): Boolean = this == STRONGBOX
}
