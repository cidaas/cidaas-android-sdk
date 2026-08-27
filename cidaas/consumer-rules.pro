#Rx Internal
-keep class rx.internal.operators.OnSubscribeFlatMapCompletable** { *; }
-keep class rx.internal.operators.OnSubscribeFlatMapSingle** { *; }


-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier