![Logo](./logo.jpg)

## About cidaas

[cidaas](https://www.cidaas.com) is a fast and secure Cloud Identity & Access Management solution that standardises what’s important and simplifies what’s complex.

## Feature set includes

- Single Sign On (SSO) based on OAuth 2.0, OpenID Connect, SAML 2.0
- Multi-Factor Authentication with many methods, including TOTP and FIDO2
- Passwordless Authentication
- Social Login and Enterprise Identity Providers (SAML, AD, and more)
- Security for Machine-to-Machine (M2M) and IoT

# Cidaas Android SDK

[![Platform](https://img.shields.io/badge/Platforms-android-4E4E4E.svg?colorA=28a745)](#get-started)

## Table of contents

- [Requirements](#requirements)
- [Get started](#get-started)
  - [1. Requirements](#1-requirements)
  - [2. Add the SDK (Gradle)](#2-add-the-sdk-gradle)
  - [3. Android manifest](#3-android-manifest)
  - [4. Register the app in the cidaas admin dashboard](#4-register-the-app-in-the-cidaas-admin-dashboard)
  - [5. App configuration (`cidaas.xml`)](#5-app-configuration-cidaasxml)
  - [6. Initialise the SDK](#6-initialise-the-sdk)
  - [7. Sign in with the browser](#7-sign-in-with-the-browser)
  - [8. Sign out (logout)](#8-sign-out-logout)
  - [9. Handle the redirect back to your app](#9-handle-the-redirect-back-to-your-app)
  - [10. Run the app](#10-run-the-app)

---

## Requirements

- **minSdkVersion 23** (Android 6.0)
- **AndroidX** (this library targets AndroidX; migrate your app if needed: Android Studio **Refactor → Migrate to AndroidX**)

---

## Get started

### 1. Requirements

Use Android Studio with a Phone and Tablet project (Empty Activity is fine). **Kotlin** or **Java** are both supported.

### 2. Add the SDK (Gradle)

Add JitPack to the project `settings.gradle` / `build.gradle` repositories (if not already present):

```gradle
dependencyResolutionManagement {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

In the app module `build.gradle`, add the cidaas artifact (core SDK):

```gradle
dependencies {
    implementation 'com.github.Cidaas:cidaas-android-sdk:cidaas:3.2.16'
}
```

Pick the [latest tag on JitPack](https://jitpack.io/#Cidaas/cidaas-android-sdk) if you want a newer version than the example above.

Sync Gradle (**File → Sync Project with Gradle Files**).

### 3. Android manifest

Add network permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

For login (and optional post-logout redirects), you will use **redirect URIs** that must match what you configure in the admin dashboard (see step 4). For the activity that receives the redirect, use **`android:launchMode="singleTop"`** and resume the SDK from `onNewIntent` (see step 9).

### 4. Register the app in the cidaas admin dashboard

Configure an Android application in the **cidaas admin portal** so the values below match your Android package and redirect flow.

| What you need         | Where it comes from (admin)                                                                                                       |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **Base URL / tenant** | Instance or tenant **domain URL** (HTTPS base for your cidaas deployment). Used as `DomainURL` in the SDK.                        |
| **Client ID**         | Application **Client ID** after creating the app.                                                                                 |
| **Redirect URL**      | **Allowed redirect URI** for your Android app (custom scheme or HTTPS App Link). Must match the intent-filter / manifest you use. |

**Checklist:**

1. Open the **cidaas admin dashboard** and go to **Apps**.
2. **Create** an application with the correct **type** for a native Android client.
3. Copy **Client ID** and note the **domain / issuer base URL**.
4. Register your **redirect URI** exactly as the app will receive it (scheme + host + path if any), e.g. `packagename://login-callback` or your **HTTPS App Link** URL including `applicationId` path if you follow hosted-link patterns.
5. Enable the **login methods** and **scopes** your flow needs (OIDC / OAuth settings in the same app).

If redirect URIs do not match, the browser will not return control to your app or token exchange will fail.

### 5. App configuration (`cidaas.xml`)

The SDK loads **`assets/cidaas.xml`** (file name must be exactly `cidaas.xml`).

1. In Android Studio: **app → New → Folder → Assets folder**.
2. Create **`cidaas.xml`** under `src/main/assets/`.

Minimal example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="DomainURL" type="string">https://your-tenant.cidaas.com</item>
    <item name="ClientId" type="string">YOUR_CLIENT_ID</item>
    <item name="RedirectURL" type="string">packagename://login-callback</item>
</resources>
```

- **`DomainURL`**: HTTPS base URL of your cidaas instance (no trailing slash issues are normally tolerated; follow your tenant’s format).
- **`ClientId`**: From the dashboard.
- **`RedirectURL`**: Same value you allowed for the app in the dashboard.

### 6. Initialise the SDK

Use **one** shared instance per application process (singleton). The constructor loads properties from **`cidaas.xml`** via `CidaasHelper`.

```kotlin
val cidaas = Cidaas.getInstance(applicationContext)
```

Initialise as early as reasonable (e.g. `Application.onCreate` or your root `Activity`), using the **same** `Context` you use for the rest of the app.

### 7. Sign in with the browser

```kotlin
import de.cidaas.sdk.android.helper.enums.EventResult
import de.cidaas.sdk.android.helper.extension.WebAuthError
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity

cidaas.webAuth(this)
    .signIn(object : EventResult<AccessTokenEntity> {
        override fun success(result: AccessTokenEntity) { /* ... */ }
        override fun failure(error: WebAuthError) { /* ... */ }
    })
```

### 8. Sign out (logout)

End the user’s **hosted** session in the browser (Chrome Custom Tab), Use the **`sub`** (subject) from the user’s ID token or userinfo after login.

```kotlin
import de.cidaas.sdk.android.helper.enums.EventResult
import de.cidaas.sdk.android.helper.extension.WebAuthError

cidaas.webAuth(this).signOut(sub, object : EventResult<Boolean> {
    override fun success(result: Boolean) {
        // Clear local tokens / navigate to logged-out UI
    }

    override fun failure(error: WebAuthError) { /* ... */ }
})
```

Optional **post-logout redirect** (must be allowed for your app in the **cidaas admin dashboard**, like an allowed logout URL):

```kotlin
cidaas.webAuth(this).signOut(
    sub,
    "packagename://logout-complete", // post_logout_redirect_uri, or null to skip
    object : EventResult<Boolean> {
        override fun success(result: Boolean) { /* ... */ }
        override fun failure(error: WebAuthError) { /* ... */ }
    },
)
```

After a successful logout, clear any **locally stored** access or refresh tokens in your app.

### 9. Handle the redirect back to your app

Use a **deep link** or **Android App Links** so the browser can return to your activity. The redirect URL must match **cidaas admin** and **`RedirectURL`** in `cidaas.xml`.

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    String data = intent.getDataString();
    if (data != null) {
        cidaas.handleToken(data);
    }
}
```

Declare the intent filter on the activity that receives the callback; set **`android:launchMode="singleTop"`** on that activity.

### 10. Run the app

From the project root:

```bash
./gradlew :app:installDebug
```

Or use **Run** in Android Studio. You should see the custom tab open, complete login at cidaas, and return to the app with tokens handled in your success callback.

---
