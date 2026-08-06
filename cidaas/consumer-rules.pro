#Rx Internal
-keep class rx.internal.operators.OnSubscribeFlatMapCompletable** { *; }
-keep class rx.internal.operators.OnSubscribeFlatMapSingle** { *; }


-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# Nimbus JOSE JWT (client-side JWE / JWKS) — uses reflection for alg / JWK parsing
-keep class com.nimbusds.** { *; }
-dontwarn com.nimbusds.**