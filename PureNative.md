# Native UI Integration

The steps here will guide you through setting up and managing authentication and authorization in your apps using cidaas SDK.

## Table of Contents

<!--ts-->

- [Getting RequestId](#getting-request-id)
- [Getting Tenant Information](#getting-tenant-info)
- [Getting Client Information](#get-client-info)
- [Login](#login)
    <!--ts-->
  - [Login with credentials](#login-with-credentials)
  <!--te-->
- [Registration](#registration)
    <!--ts-->
  - [Getting Registration Fields](#getting-registration-fields)
  - [Register user](#register-user)
  - [Update user info](#update-user-info)
  <!--te-->
- [De-duplication](#de-duplication)
    <!--ts-->
  - [Get Deduplication Details](#get-deduplication-details)
  - [Register user](#register-user-1)
  - [Login With Deduplication](#login-with-deduplication)
  <!--te-->
- [Account Verification](#account-verification)
    <!--ts-->
  - [Initiate account verification](#initiate-account-verification)
  - [Verify Account](#verify-account)
  <!--te-->
- [Forgot Password](#forgot-password)
    <!--ts-->
  - [Initiate Reset Password](#initiate-reset-password)
  - [Handle Reset Password](#handle-reset-password)
  - [Reset Password](#reset-password)
  <!--te-->
- [ChangePassword](#change-password-after-login)
     <!--ts-->
  - [Change password](#change-password-after-login)
  <!--te-->
- [Passwordless and Multifactor Authentication](/PasswordlessAndMFA.md)
<!--te-->

#### Initialisation

The first step of integrating the cidaas sdk using the native UI feature is the initialisation process.

```java

CidaasNative cidaasNative = CidaasNative.getInstance(your Activity Context);

or

CidaasNative cidaasNative =new CidaasNative(your Activity Context);

```

#### Getting Request Id

First, you will have to get the requestId and use this in your subsequent calls. The backend system provides a unique id based on urls configured for your application. Henceforth, in all requests like login, registration, you have to pass this requestId, which is utilized to identify your client between two consecutive independent calls. To get the requestId, call

\***\*getRequestId()\*\***.

```java

cidaasNative.getRequestId(new Result < AuthRequestResponseEntity > () {
  @Override
  public void success(AuthRequestResponseEntity result) {

    // Your success code here

  }

  @Override
  public void failure(WebAuthError error) {

    // Your failure code here

  }
});

```

**Response:**

```json
{
    "success":true,
    "status":200,
    "data": {
        "groupname":"default",
        "lang":"en-US,en;q=0.9,de-DE;q=0.8,de;q=0.7",
        "view_type":"login",
        "requestId":"45a921cf-ee26-46b0-9bf4-58636dced99f”
    }
}
```

#### Getting Tenant Info

Sometimes you may want to lookup which different types of login are available ('Email', 'Mobile', 'Username') for a particular tenant. To get the tenant information, call \***\*getTenantInfo()\*\***.

```java

cidaasNative.getTenantInfo(new Result < TenantInfoEntity > () {

  @Override
  public void success(TenantInfoEntity result) {

    // Your success code here

  }

  @Override
  public void failure(WebAuthError error) {

    // Your failure code here

  }
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "tenant_name": "Cidaas Management",
    "allowLoginWith": ["EMAIL", "MOBILE", "USER_NAME"]
  }
}
```

#### Get Client Info

Once you get the tenant information, if you want to find the client information you can call the following method. It contains the client name and the logo url specified for the client in the admin's apps section and details of what all social providers are configured for the app. To get the client information, call \***\*getClientInfo()\*\***.

```java
cidaasNative.getClientInfo("your RequestId", new Result < ClientInfoEntity > () {
  @Override
  public void success(ClientInfoEntity result) {

    // Your success code here

  }

  @Override
  public void failure(WebAuthError error) {

    // Your failure code here

  }
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "passwordless_enabled": true,
    "logo_uri": "https://www.cidaas.com/wp-content/uploads/2018/02/logo-black.png",
    "login_providers": ["facebook", "linkedin"],
    "policy_uri": "",
    "tos_uri": "",
    "client_name": "demo-app"
  }
}
```

#### Registration

#### Getting Registration Fields

Before registration, you may want to know what the fields are that you must show to your user to register. For getting these fields, call \***\*getRegistrationFields()\*\***.

```java
cidaasNative.getRegistrationFields("Your_RequestId","Your_locale" ,new Result < RegistrationSetupResponseEntity > () {
  @Override
  public void success(RegistrationSetupResponseEntity result) {
    // your success code here

  }

  @Override
  public void failure(WebAuthError error) {
    // your failure code here

  }
```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "dataType": "EMAIL",
      "fieldGroup": "DEFAULT",
      "isGroupTitle": false,
      "fieldKey": "email",
      "fieldType": "SYSTEM",
      "order": 1,
      "readOnly": false,
      "required": true,
      "fieldDefinition": {},
      "localeText": {
        "locale": "en-us",
        "language": "en",
        "name": "Email",
        "verificationRequired": "Given Email is not verified.",
        "required": "Email is Required"
      }
    },
    {
      "dataType": "TEXT",
      "fieldGroup": "DEFAULT",
      "isGroupTitle": false,
      "fieldKey": "given_name",
      "fieldType": "SYSTEM",
      "order": 2,
      "readOnly": false,
      "required": true,
      "fieldDefinition": {
        "maxLength": 150
      },
      "localeText": {
        "maxLength": "Givenname cannot be more than 150 chars",
        "required": "Given Name is Required",
        "name": "Given Name",
        "language": "en",
        "locale": "en-us"
      }
    }
  ]
}
```

#### Register user

To register a new user, call \***\*registerUser()\*\***.

```java

RegistrationEntity registrationEntity = new RegistrationEntity();

registrationEntity.setUsername("davidjhonson");
registrationEntity.setEmail("davidjhonson@gmail.com");
registrationEntity.setGiven_name("David");
registrationEntity.setFamily_name("Jhonson");
registrationEntity.setPassword("123456");
registrationEntity.setPassword_echo("123456");
registrationEntity.setMobile_number("+919876553230");
Date date = new Date();
date.setDate(27 / 12 / 1994);
registrationEntity.setBirthdate(date);
registrationEntity.setGender("Male");
registrationEntity.setWebsite("http://google.com");


RegistrationCustomFieldEntity registrationCustomFieldEntity = new RegistrationCustomFieldEntity();
registrationCustomFieldEntity.setKey("Pincode");
registrationCustomFieldEntity.setValue("123456");
registrationCustomFieldEntity.setDataType("String");
registrationCustomFieldEntity.setId("pincode");
registrationCustomFieldEntity.setInternal(true);

Dictionary < String, RegistrationCustomFieldEntity > customFileds = new Hashtable < > ();
customFileds.put(registrationCustomFieldEntity.getKey(), registrationCustomFieldEntity);
registrationEntity.setCustomFields(customFileds);

cidaasNative.registerUser("Your_requestId", registrationEntity, new Result < RegisterNewUserResponseEntity > () {

    @Override
    public void success(RegisterNewUserResponseEntity result) {
        //Your success code here
    }

    @Override
    public void failure(WebAuthError error) {
      //Your failure code here
    }
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "track_id": "45a921cf-ee26-46b0-9bf4-58636dced99f",
    "sub": "7dfb2122-fa5e-4f7a-8494-dadac9b43f9d",
    "userStatus": "VERIFIED",
    "email_verified": false,
    "suggested_action": "DEDUPLICATION"
  }
}
```

#### Update user info

To update info about existing user, call \***\*updateUser()\*\***.

```java

 RegistrationEntity registrationEntity=new RegistrationEntity();
 registrationEntity.setSub(sub);
 registrationEntity.setGiven_name("updated Name");
 registrationEntity.setProvider("self");// either self or facebook or google or other login providers

 CidaasNative.getInstance(getContext()).updateUser("YouraccessToken", registrationEntity, new EventResult<UpdateUserResponseEntity>() {
    @Override
    public void success(UpdateUserResponseEntity result) {
        //Your Success Code
    }

    @Override
    public void failure(WebAuthError error) {
         // Your Failure code
    }
});
```

**Response:**

```java
{
    "success": true,
    "status": 200,
    "data": {
        "updated":true
    }
}
```

After you get the response from the \***\*registerUser()\*\***, You may get a suggested_action like \***\*"DEDUPLICATION"\*\*** in the data of success respone. At that time, you have to follow the following steps

#### De-duplication

User de-duplication is a process that eliminates redundant copies of users, thus reducing storage overhead as well as other inefficiencies. This process can be triggered during registration itself by following next steps.

When a user is being registered, the system does a de-duplication check to verify if the user is already existing. The system then shows the list of potential duplicate users whose data seems to match most of the information entered during this registration.  
It then gives the option to the user to use one of the found duplicate record or reject all of them and register the new values as a fresh user.

In order to implement above functionality, the methods mentioned below have to be called.

#### Get Deduplication Details

To get the list of similar users, call \***\*getDeduplicationDetails()\*\*** . If this method is used, system uses some heuristic algorithms and finds out if any similar user exists in the system and returns them.

> #### Note :- You can get the track id from the data of the success respone of registerUser().

```java
 cidaasNative.getDeduplicationDetails("your_track_id", new Result < DeduplicationResponseEntity > () {
  @Override
  public void success(DeduplicationResponseEntity result) {
   //Your success code here.
  }

  @Override
  public void failure(WebAuthError error) {
   //Your failure code here
  }
 });
```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "email": "xxx@gmail.com",
    "deduplicationList": [
      {
        "provider": "SELF",
        "sub": "39363935-4d04-4411-8606-6805c4e673b4",
        "email": "xxx********n2716@g***l.com",
        "emailName": "xxx********n2716",
        "firstname": "xxx",
        "lastname": "yyy",
        "displayName": "xxx yyy",
        "currentLocale": "IN",
        "country": "India",
        "region": "Delhi",
        "city": "Delhi",
        "zipcode": "110008"
      },
      {
        "provider": "SELF",
        "sub": "488b8128-5584-4c25-9776-6ed34c6e7017",
        "email": "xx****n21@g***l.com",
        "emailName": "xx****n21",
        "firstname": "xxx",
        "lastname": "yyy",
        "displayName": "xxx yyy",
        "currentLocale": "IN",
        "country": "India",
        "region": "Delhi",
        "city": "Delhi",
        "zipcode": "110008"
      }
    ]
  }
}
```

#### Register User

While registering the user, if the system has found any similar users who have already registered, this list is shown to the user. He can then decide whether to use one of the existing logins, or choose to ignore all shown details. \***\*registerUser()\*\*** method can be called to ignore the shown result and register details in registration form as a new user.

```java
cidaasNative.registerUser("your track id", new Result < RegisterDeduplicationEntity > () {
 @Override
 public void success(RegisterDeduplicationEntity result) {
  //Your success code here
 }

 @Override
 public void failure(WebAuthError error) {
  //Your failure code here
 }
});
```

**Response:**

```java
{
    "success": true,
    "status": 200,
    "data": {
        "sub": "51701ec8-f2d7-4361-a727-f8df476a711a",
        "userStatus": "VERIFIED",
        "email_verified": false,
        "suggested_action": "LOGIN"
    }
}
```

#### Login With Deduplication

While registering user, if the system found any similar users who have already registered, this list is shown to user. He can decide whether to use one of the existing logins, or choose to ignore all shown details. The \***\*loginWithDeduplication()\*\*** method can be called to use one of those existing logins shown by the system. Note that the system will still use the secure authentication and verifications that were setup for earlier user, before login.

```java
cidaasNative.loginWithDeduplication("your_requestId","your_sub", "your_password", new Result < LoginCredentialsResponseEntity > () {
 @Override
 public void success(LoginCredentialsResponseEntity result) {
  //Your success code here
 }

 @Override
 public void failure(WebAuthError error) {
  //Your failure code here
 }
});
```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "token_type": "Bearer",
    "expires_in": 86400,
    "access_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjUxNWYxMGE5LTVmNDktNGZlYS04MGNlLTZmYTkzMzk2YjI4NyJ9*****",
    "session_state": "CNT7GGALeoKyTF6Og-cZHAuHUJBQ20M0jLL35oh3UGk.vcNxCNq4Y68",
    "viewtype": "login",
    "grant_type": "login"
  }
}
```

#### Account Verification

In order to avoid misuse of user registration functions, it is a good practise to include account verification along with it. Once the registration is done, you can verify your account either by email, SMS or IVR verification call. To do this, first you have to initiate the account verification. You can invoke any of the following as it suits your use case.

#### Initiate Account verification

This method has to be used when you want to receive a verification code or Link via email/sms/ivr:

```java

 InitiateAccountVerificationRequestEntity initiateAccountVerificationRequestEntity=new InitiateAccountVerificationRequestEntity();
 initiateAccountVerificationRequestEntity.setProcessingType("LINK");// You can set CODE or LINK as processingType
 initiateAccountVerificationRequestEntity.setRequestId("Your request id") ;
 initiateAccountVerificationRequestEntity.setVerificationMedium("email"); //You can set email,sms,ivr
 initiateAccountVerificationRequestEntity.setEmail("CXXXXXXX@YYYY.com"); // if your verificationMedium is email you must set the email
 initiateAccountVerificationRequestEntity.setMobile("+91XXXXXXXXXX"); //If your VerificationMedium is SMS or IVR, you must to send the mobile number

CidaasNative.getInstance(yourContext).initiateAccountVerification(initiateAccountVerificationRequestEntity, new EventResult<InitiateAccountVerificationResponseEntity>() {
                    @Override
                    public void success(InitiateAccountVerificationResponseEntity result) {
                        //Your Success Code
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        // Your Failure Code
                    }
                });
cidaasNative.initiateEmailVerification("your_sub", "your_requestId", new Result < RegisterUserAccountInitiateResponseEntity > () {
@Override
public void success(RegisterUserAccountInitiateResponseEntity result) {
//your Success Code
}

@Override
public void failure(WebAuthError error) {
 //your failure code
}
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "accvid": "353446"
  }
}
```

#### Verify Account

Once you have received your verification code via any of the mediums like email, SMS or IVR, you need to verify the code. For that verification, call **verifyAccount()**. You get get the accvid in the initate account verification call

```java

cidaasNative.verifyAccount("your code", "your accvid",new Result < RegisterUserAccountVerifyResponseEntity > () {

@Override
public void success(RegisterUserAccountVerifyResponseEntity result) {

//your success code here

}

@Override
public void failure(WebAuthError error) {

//your failure code here

}
});

```

**Response:**

```json
{
  "success": true,
  "status": 200
}
```

#### Login

#### Login with credentials

To login with your username and password, call \***\*loginWithCredentials()\*\***.

```java

LoginEntity loginEntity = new LogintEntity();

loginEntity.setUsername("davidjhonson@gmail.com");
loginEntity.setPassword("123456");
loginEntity.setUsername_type("email");

cidaasNative.loginWithCredentials("Your RequestId", loginEntity, new Result < LoginCredentialsResponseEntity > () {

@Override
public void success(LoginCredentialsResponseEntity result) {
  // Your success code here
}

@Override
public void failure(WebAuthError error) {
  // Your failure code here
}

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "token_type": "Bearer",
    "sub": "51701ec8-f2d7-4361-a727-f8df476a711a",
    "expires_in": 86400,
    "access_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjUxNWYxMGE5LTV",
    "session_state": "CNT7TF6Og-cCNq4Y68",
    "viewtype": "login",
    "grant_type": "login"
  }
}
```

#### MFA List

To get the list of physical verifications which are configured by the user, call \***\*getMFAList()\*\***.

```java
 cidaasNative.getMFAList("your_sub", new Result<MFAListResponseEntity>() {
         @Override
         public void success(MFAListResponseEntity result) {
             //Your Success Code here
         }

         @Override
         public void failure(WebAuthError error) {
 	    //Your Failure Code here
         }
     });

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "_id": "0fd78d48-f825-487f-823b-c71f05ced944",
      "verification_type": "PATTERN"
    },
    {
      "_id": "184ec81d-5bb3-466f-b10a-351f36b31fc4",
      "verification_type": "PUSH"
    }
  ]
}
```

#### Set Remote Message

For device verification, the backend system sends one push notification to you. For that you need to register your FCM Token(Firebase cloud Messaging) in your app inside the admin UI.  
You need to call \***\*setFCMToken()\*\***.

```java
 cidaasVerification.setFCMToken(refreshedToken);
```

Then in the FirebaseMessagingService extended class, in the onMessageReceived() method, you will need to set the remote message received here using \***\*setRemoteMessage()\*\***.

```java
cidaasVerification.setFCMToken(refreshedToken);
```

#### Forgot Password

There is the option to reset the password if the user forgot his password.

#### Initiate Reset Password

For resetting the password, the app will get a verification code either via email or SMS. For email you need to call

\***\*initiateResetPasswordByEMail()\*\***.

```java
cidaasNative.initiateResetPasswordByEMail("Your_requestId", "your_email_id","LINK", new EventResult < ResetPasswordResponseEntity > () {
@Override
public void success(ResetPasswordResponseEntity result) {
   //Your success code here
}

@Override
public void failure(WebAuthError error) {
    //Your failure code here.
}
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "rprq": "f595edfb-754e-444c-ba01-6b69b89fb42a",
    "reset_initiated": true
  }
}
```

To reset the password via SMS, you need to call

\***\*initiateResetPasswordBySMS()\*\***.

```java
cidaasNative.initiateResetPasswordBySMS("Your_requestId", "your_mobile_number", new Result < ResetPasswordResponseEntity > () {
@Override
public void success(ResetPasswordResponseEntity result) {
   //Your success code here
}

@Override
public void failure(WebAuthError error) {
    //Your failure code here.
}
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "rprq": "f595edfb-754e-444c-ba01-6b69b89fb42a",
    "reset_initiated": true
  }
}
```

#### Handle Reset Password

Once the verification code is received, verify that code by calling \***\*handleResetPassword()\*\***.

```java

cidaasNative.handleResetPassword("your verificaton code","your_rprq", new Result < ResetPasswordValidateCodeResponseEntity > () {

@Override
public void success(ResetPasswordValidateCodeResponseEntity result) {
    //Your suucees code here.
}

@Override
public void failure(WebAuthError error) {
    //your failure code here.
}
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "exchangeId": "1c4176bd-12b0-4672-b20c-9616e93457ed",
    "resetRequestId": "f595edfb-754e-444c-ba01-6b69b89fb42a"
  }
}
```

#### Reset Password

Once the code is verified, reset your password with your new password. To reset your password, call \***\*resetPassword()\*\***.

````java

ResetPasswordEntity resetPasswordEntity = new ResetPasswordEntity();
resetPasswordEntity.setPassword("yournewPassword");
resetPasswordEntity.setConfirmPassword("yourconfirmPassword");
resetPasswordEntity.setExchangeId("yourexchangeId");
resetPasswordEntity.setResetRequestId("yourresetRequestId");

cidaasNative.resetPassword(ResetPasswordEntity resetPasswordEntity, new Result < ResetNewPasswordResponseEntity > () {
@Override
public void success(ResetNewPasswordResponseEntity result) {
    //Your success code here.
}

@Override
public void failure(WebAuthError error) {
    //Your failure code here.
}
});

``
**Response:**

```json
{
    "success": true,
    "status": 200,
    "data": {
        "reseted":true
    }
}
````

#### Change Password After Login

You can change your password by calling **changePassword().**

```java
ChangePasswordRequestEntity changePasswordRequestEntity=new ChangePasswordRequestEntity();

    changePasswordRequestEntity.setIdentityId("YourIdentityId");
    changePasswordRequestEntity.setNew_password("123456");
    changePasswordRequestEntity.setConfirm_password("123456");
    changePasswordRequestEntity.setOld_password("yourOldPassword");

cidaasNative.changePassword("YourAccessToken",changePasswordRequestEntity, new EventResult<ChangePasswordResponseEntity>() {
@Override
public void success(ChangePasswordResponseEntity EventResult) {
    //Your success code here.
}

@Override
public void failure(WebAuthError error) {
    //Your failure code here.
 }
});

```

**Response:**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "changed": true
  }
}
```
