package de.cidaas.sdk.android.helper.crypthelper

/**
 * Minimum acceptable key backing for Keystore-generated keys.
 *
 * Set via [KeystoreEcP256StrongBoxHelper] before key generation. The policy is enforced
 * *after* generation by inspecting [android.security.keystore.KeyInfo]: if the key doesn't
 * meet the minimum, it is deleted and a [SecurityException] is thrown.
 *
 * The SDK still always *tries* StrongBox first regardless of policy — the policy only
 * controls what happens when StrongBox isn't available and the key falls back to a lower tier.
 */
enum class KeyBackingPolicy {
    /** StrongBox → TEE → Software. Accept whatever the device provides. */
    BEST_AVAILABLE,
    /** TEE or StrongBox. Reject software-only keys. */
    REQUIRE_HARDWARE,
}
