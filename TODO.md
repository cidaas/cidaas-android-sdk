# SDK TODOs

## Embed `key_backing` claim in proof JWTs

Once the server-side contract is defined, embed the key's hardware backing level in all proof JWTs so the server can make risk-based decisions (e.g. reject software-backed keys for high-value transactions).

**Affected files:**
- `DpopP256Keystore.kt` — DPoP proof JWTs
- `BiometricP256Signer.kt` — biometric proof JWTs

**Implementation:**
```kotlin
payload.put("key_backing", KeystoreEcP256StrongBoxHelper.getSecurityLevel(alias).wireValue)
```

`KeySecurityLevel.wireValue` produces stable strings: `STRONGBOX`, `TEE`, `HARDWARE`, `SOFTWARE`, `UNKNOWN_SECURE`.

**Depends on:** server-side agreement on claim name, validation rules, and how `key_backing` interacts with existing DPoP/biometric verification.
