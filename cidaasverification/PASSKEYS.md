# Passkeys (FIDO2) enrollment and Digital Asset Links

When you call `cidaas.verifications().enrolment().passkey(activity, sub, callback)`, the SDK uses Android **Credential Manager** with the `server_challenge` from Cidaas setup initiation. Android validates that your **app** is allowed to use the **relying party** hostname in `rp.id` (inside `server_challenge`).

If Credential Manager fails with **“The incoming request cannot be validated”**, this is almost always a **Digital Asset Links** issue, not a bug in the enrollment HTTP call.

## What you must host

On the host that matches **`rp.id`** exactly (for example `kube-nightlybuild-dev.cidaas.de`), this URL must return **HTTP 200**, **`Content-Type: application/json`**, and **no redirects**:

`https://<rp.id>/.well-known/assetlinks.json`

Example file (replace placeholders):

```json
[
  {
    "relation": [
      "delegate_permission/common.get_login_creds",
      "delegate_permission/common.handle_all_urls"
    ],
    "target": {
      "namespace": "android_app",
      "package_name": "com.yourcompany.yourapp",
      "sha256_cert_fingerprints": [
        "AA:BB:CC:..."
      ]
    }
  }
]
```

- **`package_name`**: your app’s `applicationId` from `build.gradle`.
- **`sha256_cert_fingerprints`**: SHA-256 of the **signing certificate** Android uses for this build:
  - **Debug**: from your debug keystore (`keytool` / Android Studio signing report).
  - **Play Store**: use the **App signing certificate** SHA-256 from Play Console (not necessarily your upload key).

## Validate

- [Digital Asset Links statement list generator](https://developers.google.com/digital-asset-links/tools/generator)
- [Credential Manager troubleshooting](https://developer.android.com/identity/sign-in/credential-manager-troubleshooting-guide)

## Tenant / product note

If Cidaas returns an **`rp.id`** on a **shared** hostname (for example a generic Cidaas domain), that host must either:

- publish `assetlinks.json` entries for **each** customer app (often not practical), or  
- your tenant should be configured so **`rp.id`** is a **hostname you control**, where you can publish `assetlinks.json` for your app only.

Coordinate with your Cidaas administrator if `rp.id` is not under your control.
