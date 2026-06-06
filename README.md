![Logo](logo.jpg)

## About cidaas:
[cidaas](https://www.cidaas.com)
 is a fast and secure Cloud Identity & Access Management solution that standardises what’s important and simplifies what’s complex.

## Feature set includes:
* Single Sign On (SSO) based on OAuth 2.0, OpenID Connect, SAML 2.0 
* Multi-Factor-Authentication with more than 14 authentication methods, including TOTP and FIDO2 
* Passwordless Authentication 
* Social Login (e.g. Facebook, Google, LinkedIn and more) as well as Enterprise Identity Provider (e.g. SAML or AD) 
* Security in Machine-to-Machine (M2M) and IoT

# Cidaas Android SDK

[![Build Status](https://travis-ci.org/Cidaas/de.cidaas-android-sdk.svg?branch=development)](https://travis-ci.org/Cidaas/de.cidaas-android-sdk)
[![codecov.io](https://codecov.io/gh/Cidaas/de.cidaas-android-sdk/branch/master/graph/badge.svg)](https://codecov.io/gh/Cidaas/de.cidaas-android-sdk/branch/master)
[![jitpack](https://jitpack.io/v/Cidaas/de.cidaas-android-sdk.svg)](https://jitpack.io/#Cidaas/de.cidaas-android-sdk)
[![Platform](https://img.shields.io/badge/Platforms-android-4E4E4E.svg?colorA=28a745)](#installation)


The steps here will guide you through setting up and managing authentication and authorization in your apps using the cidaas android SDK.

## Note:
This SDK was renamed from ```cidaas -v2-sdk-android``` to ```cidaas-android-sdk```. To migrate from the older version <2.1.0, please refer to the [Migration guide](/Migration.md)

## Table of Contents

<!--ts-->
* [Requirements](#requirements)
* [Installation](#installation)
* [Getting started](#getting-started)
* [Migrating to Cidaas V3](#migrating-to-cidaas-v3)
* [Getting Client Id and urls](#getting-client-id-and-urls)
* [Initialisation](#initialisation)
* [Usage](#usage)
    <!--ts-->
    * [Native Browser Login](#native-browser-login)
        <!--ts-->
        * [Classic Login](#classic-login)
        * [Social Login](#social-login)
        <!--te-->
    *  [Embedded Browser Login](#embedded-browser-login)
    * [Logout](#logout)
    * [Native UI Integration](/PureNative.md)
    * [Passwordless and MFA](/PasswordlessAndMFA.md)
    <!--te-->

## Requirements

    minSdkVersion 21

### Steps to integrate the native android SDK:

### Installation


The cidaas SDK is available through jitpack.io.  
Please ensure that you are using the latest version by [checking here](https://jitpack.io/#Cidaas/cidaas-android-sdk)

Add the following Gradle configuration to your Android project:

```java        
allprojects {
    repositories {
        ...
		maven { url 'https://jitpack.io' }
	}
}
```		
 Add the dependency to app module
 ```java
dependencies {
 implementation 'com.github.Cidaas:cidaas-android-sdk:3.0.0'
}
 ```
This will add all the subproject of cidaas SDK, if you only need a specific subproject for your use case, just add it's name before the version. E.g.:

Android Gradle plugin 3.4.0 and higher, R8 is the default compiler. If you have R8 activated (minifyEnabled:true in your app build.gradle file) for your App, please update consumer-rules.pro in respective SDK modules to use SDK features.


 ```java
dependencies {
    implementation 'com.github.Cidaas:cidaas-android-sdk:cidaasnative:3.0.0'
}
 ```

This will then add only the features available under cidaasnative.

For now we need to implement only cidaas core Functions

So please Add

 ```java
dependencies {
    implementation 'com.github.Cidaas:cidaas-android-sdk:cidaas:3.0.0'
}
 ```

> ##### Note:- This Library is developed in AndroidX so AndroidX migration is necessary

To migrate your project into Androidx in android studio goto

Refactor-> Migrate to AndroidX


## Getting started
 
Create an asset folder by right clicking the app module. Go to new -> folder -> Assets folder and click the finish button

Create an xml file named **cidaas.xml** inside the asset folder and fill all the inputs for the key value pairs. The inputs are below mentioned.

> ##### Note:- The File name must be cidaas.xml 

A sample XML file would look like this :

``` 
<?xml version="1.0" encoding="utf-8"?>
<resources>
	<item name="DomainURL" type="string">DomainURL</item>
	<item name="ClientId" type="string">ClientId</item>
	<item name="RedirectURL">RedirectURL</item>
</resources> 

```

## Migrating to Cidaas V3

Cidaas V3 has response handling adjustment on some of cidaas service call. To migrate to cidaas V3, you need to do the following:

* ensure that you use at least cidaas version: 3.97.0. You can find out the cidaas version from cidaas service portal, and ask our customer service if it need to be updated.
* ensure that you use at least cidaas-android-sdk version: 3.1.26
* add CidaasVersion  to **cidaas.xml**

A sample XML file would look like this :

``` 
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="DomainURL" type="string">DomainURL</item>
    <item name="ClientId" type="string">ClientId</item>
    <item name="RedirectURL">RedirectURL</item>
    <item name="CidaasVersion">3</item>
</resources> 

```

Without Providing CidaasVersion, your application will use response handling of Cidaas V2 by default.

### Getting client Id and urls

You can get this by creating your app in the app settings section of the cidaas admin portal. Once you have selected the right scope and application type, and have filled in all of the mandatory fields, you can use the generated client id and re-direct URLs.


#### Initialisation

The first step of integrating the cidaas sdk is the initialisation process:
```java

Cidaas cidaas = Cidaas.getInstance(yourActivityContext);

    or

Cidaas cidaas =new Cidaas(yourActivityContext);

```

### Usage

> ##### Note:- If you are going to use the native browser login, you must use the getInstance() method to create a new instance of the cidaas SDK


#### Native Browser Login 
#### Classic Login
You can login using your native browser and redirect back to the App once you have successfully logged in. To login with your native browser call ****loginWithBrowser()****.

```java
cidaas.loginWithBrowser(yourActivityContext, "NullableColorParameterInColorCode", new EventResult<AccessTokenEntity>() {
     @Override
     public void success(AccessTokenEntity result) {
    	//Your Success Code
     }

     @Override
     public void failure(WebAuthError error) {
		//Your Failure Code
     }
});
```
Add a deep link or app link (#add-custom-scheme)

You can get the url for the login by calling getLoginURL() and load your favorite browser like this:

```java
Cidaas.getInstance(yourContext).getLoginURL(new EventResult<String>() {
    @Override
    public void success(String result) {
        //Your Success code
    }

    @Override
    public void failure(WebAuthError error) {
        //Your Failure code
    }
});
```

#### Social Login
You can also perform social login using your native browser and redirect back to the app once the login was successful. To perform a social login call ****loginWithSocial()****.

```java
cidaas.loginWithSocial(yourActivityContext, "requestID", your_Social_Provider, "Nullable_Color_Parameter_In_Color_Code", new EventResult<AccessTokenEntity>() {
    @Override
    public void success(AccessTokenEntity result) {
        //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
		//Your Failure Code
    }
 });
```
where social provider may be either "facebook", "google", "linkedin" or any other providers which are configured for your app via the cidaas admin dashboard.

To get the url for the social login and load it in your favorite browser you can call

```java
Cidaas.getInstance(yourContext).getSocialLoginURL(yourRequestid,your_social_provider,new EventResult<String>() {
    @Override
    public void success(String result) {
       //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
       	//Your Failure Code
    }
});

```
where the social provider may be either "facebook", "google", "linkedin" or any other providers

#### Register in Browser

To register with the browser, you can call the following method

```java
Cidaas.getInstance(yourContext).RegisterWithBrowser(youraAtivityContext, "Nullable_Color_Parameter_In_Color_Code", new EventResult<AccessTokenEntity>() {
    @Override
    public void success(AccessTokenEntity result) {
         //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
    	//Your Failure Code
    }
});
```        

To get the url for the registration and load it in your favorite browser you can call

```java
Cidaas.getInstance(yourContext).getRegistrationURL(new EventResult<String>() {
    @Override
    public void success(String result) {
         //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
          //Your Failure Code
    }
});

```
#### Get Request Id

To get request id, You can call following method 

```java
    CidaasNative.getInstance(yourContext).getRequestId(new EventResult<AuthRequestResponseEntity>() {
    @Override
    public void success(AuthRequestResponseEntity result) {
        //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
        //Your Failure Code
    }
});

```

#### Logout

To use logout, You can call following functions 

```java
    CidaasNative.getInstance(this).logout("AccessToken", new EventResult<Boolean>() {
            @Override
            public void success(Boolean result) {
                 //Your Success Code
            }

            @Override
            public void failure(WebAuthError error) {
                //Your Failure Code
            }
        });
```

#### Add a custom scheme

Use [customScheme](https://developer.android.com/training/app-links/deep-linking) or [App Link](https://developer.android.com/studio/write/app-link-indexing) to return the control back from the browser to the app.

    Note: Don't forget to add the custom scheme url in your App's redirect url section



If you use a deep link or custom scheme, After adding the intent-filter in the manifest file, add  **android:launchMode="singleTop"** in the activity you redirected. For example if you redirect your link from browser to mainactivity

```java
 <activity android:name=".MainActivity" android:launchMode="singleTop">
 ```
 and resume the SDK from your activity
```java

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String token = intent.getDataString();
        if (token != null) {
            cidaas.handleToken(token);
            
        } else {
           //your else part
        }
    }

}
```

If you use app links, configure your Domain setup and resume the SDK from your activity


```java
  @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String token = intent.getDataString();
        if (token != null) {
            cidaas.handleToken(token);
            
        } else {
           //your else part
        }
    }
}
```

If you are using app links , you can able to open an app by click the link associated with domain URL


#### Common Methods
##### Getting AccessToken

You can get the access token for the current user using the following call

```Java
cidaas.getAccessToken(yourSub, new EventResult<AccessTokenEntity>() {
 @Override
 public void success(AccessTokenEntity result) {
      //Your Success Code
  }

  @Override
  public void failure(WebAuthError error) {
       //Your Failure Code
   }
  }); 
```

By default the access token is renewed when you call the getAccessTokenMethod, if you want to renew the token manually you need to call the following method

```Java
cidaas.getAccessTokenFromRefreshToken(refreshToken, new EventResult<AccessTokenEntity>() {
   @Override
   public void success(AccessTokenEntity result) {
      // Your Success Code                
   }

   @Override
   public void failure(WebAuthError error) {
      // Your Failure Code
    }
  });
        
```

##### Enable PKCE flow

  By default the PKCE flow is enabled. If you want to disable the PKCE flow, you can call the following method
  
  ```Java
Cidaas.getInstance(yourContext).setENABLE_PKCE(false);
```
> ##### Note:- If you disable the PKCE flow, you must use add the 'ClientSecret' variable in your cidaas.xml:
```
<item name="ClientId" type="string">ClientId</item>
```

To know whether the PKCE is enabled or not, use the following method
 ```Java
 Cidaas.getInstance(yourContext).isENABLE_PKCE();
 ```
it will return a boolean value which is true if PKCE is enabled and false if it is disabled.

##### Enable Log

  By default the logger is disabled. If you want to enable the logger, you can call the following method
  
  ```Java
  Cidaas.getInstance(yourContext).enableLog();
        
```
To disable the Log

  ```Java
Cidaas.getInstance(yourContext).disableLog();
        
```

To know whether the Log is enabled or not, use the following method
 ```Java
Cidaas.getInstance(yourContext).isLogEnable();
 ```
It will return a boolean value, which is true if the logger is enabled for the SDK and false if it is disabled.

##### User information

```Java
Cidaas.getInstance(yourContext).getUserInfo("yourSub", new EventResult<UserinfoEntity>() {
 @Override
   public void success(UserinfoEntity result) {
      //Your Success Code
     }

 @Override
  public void failure(WebAuthError error) {
      //Your Failure code
     }
});
```        


### Embedded Browser Login

You can use the embedded browser to login with cidaas. For this do the following steps:
1. You have to add '< uses-permission  android:name="android.permission.ACCESS_NETWORK_STATE"/>' in your manifest file.
2. Create an instance of the CidaasSDKLayout using the activity context.


```Java
  CidaasSDKLayout cidaasSDKLayout = CidaasSDKLayout.getInstance(this);
  
     or
  
  CidaasSDKLayout cidaasSDKLayout = new CidaasSDKLayout(this);

```
3.Call ****loginWithEmbeddedBrowser()**** .

4.Pass the relative layout as argument

```Java
 RelativeLayout relativeLayout=findViewById(R.id.relative_layout_for_webView);
 
 cidaasSDKLayout.loginWithEmbeddedBrowser(relativeLayout, new EventResult<AccessTokenEntity>() {
 @Override
 public void success(AccessTokenEntity result) {
      //Your Success Code
  }

  @Override
  public void failure(WebAuthError error) {
       //Your Failure Code
   }
  }); 
```

##### Get Access Token From social
If your app has received an access token from google or facebook, then you can use following method to get the access token of cidaas.


```Java
SocialAccessTokenEntity socialAccessTokenEntity = new SocialAccessTokenEntity();
socialAccessTokenEntity.setToken(yourtoken);
socialAccessTokenEntity.setDomainURL(yourDomainURL);
socialAccessTokenEntity.setProvider(yourprovider);
socialAccessTokenEntity.setViewType(yourviewType);

Cidaas.getInstance(yourActivityContext).getAccessTokenBySocial(socialAccessTokenEntity, new EventResult<AccessTokenEntity>() {
    @Override
    public void success(AccessTokenEntity result) {
        //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
       //Your Failure code
    }
 });

```
here yourviewType is "login" and provider is "google" or "facebook" 