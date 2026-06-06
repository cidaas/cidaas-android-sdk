# Cidaas Android SDK V2

[![Build Status](https://travis-ci.org/Cidaas/de.cidaas-android-sdk.svg?branch=development)](https://travis-ci.org/Cidaas/de.cidaas-android-sdk)
[![codecov.io](https://codecov.io/gh/Cidaas/de.cidaas-android-sdk/branch/development/graph/badge.svg)](https://codecov.io/gh/Cidaas/de.cidaas-android-sdk/branch/development)
[![jitpack](https://jitpack.io/v/Cidaas/de.cidaas-android-sdk.svg)](https://jitpack.io/#Cidaas/de.cidaas-android-sdk)
[![Platform](https://img.shields.io/badge/Platforms-android-4E4E4E.svg?colorA=28a745)](#installation)

The steps here will guide you through the migration changes

> ##### Note:- The Cidaas.xml will be renamed to cidaas.xml
>
> In the old version of the SDK < 2.1.0, the cidaas SDK was available as a single package. Now it is split into multiple sub-modules for your convenience - you can choose which part to include and which not

### Here you can see how the SDK is structured:

## Cidaas Native

The following methods under the former `cidaas` package are now available in `cidaasnative`

<!--ts-->

- [Getting RequestId](/PureNative.md#getting-request-id)
- [Getting Tenant Information](/PureNative.md#getting-tenant-info)
- [Getting Client Information](/PureNative.md#get-client-info)
- [Login](/PureNative.md#login)
    <!--ts-->
  - [Login with credentials](/PureNative.md#login-with-credentials)
  <!--te-->
- [Registration](/PureNative.md#registration)
    <!--ts-->
  - [Getting Registration Fields](/PureNative.md#getting-registration-fields)
  - [Register user](/PureNative.md#register-user)
  <!--te-->
- [De-duplication](/PureNative.md#de-duplication)
    <!--ts-->
  - [Get Deduplication Details](/PureNative.md#get-deduplication-details)
  - [Register user](/PureNative.md#register-user-1)
  - [Login With Deduplication](/PureNative.md#login-with-deduplication)
  <!--te-->
- [Account Verification](/PureNative.md#account-verification)
    <!--ts-->
  - [Initiate Email verification](/PureNative.md#initiate-email-verification)
  - [Initiate SMS verification](/PureNative.md#initiate-sms-verification)
  - [Initiate IVR verification](/PureNative.md#initiate-ivr-verification)
  - [Verify Account](/PureNative.md#verify-account)
  <!--te-->
- [Forgot Password](/PureNative.md#forgot-password)
  <!--ts-->
  _ [Initiate Reset Password](/PureNative.md#initiate-reset-password)
  _ [Handle Reset Password](/PureNative.md#handle-reset-password) \* [Reset Password](/PureNative.md#reset-password)
  <!--te-->
  <!--te-->

## Cidaas Verification

The following methods from the former `cidaas` package are now available in `cidaasverification`

- [Face](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [Voice](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [Fingerprint](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [Pattern](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [Smart Push](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [TOTP](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [Email](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [SMS](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [IVR](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->
- [Backupcode](/PasswordlessAndMFA.md)
    <!--ts-->
  - [Configuration](/PasswordlessAndMFA.md)
  - [Usage](/PasswordlessAndMFA.md)
  <!--te-->

## Cidaas

The following methods are available in the `cidaas` package

- [Native Browser Login](#native-browser-login)
  <!--ts-->
  _ [Classic Login](#classic-login)
  _ [Social Login](#social-login)
  <!--te-->
  - [Embedded Browser Login](#embedded-browser-login)
  - [Native UI Integration](/PureNative.md)
  - [Passwordless Authentication and MFA](/PasswordlessAndMFA.md)
  <!--te-->
