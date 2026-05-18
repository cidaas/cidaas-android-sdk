package de.cidaas.sdk.android.service;

import java.util.Map;

import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.service.entity.UserInfo.UserInfoEntity;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.entity.documentscanner.DocumentScannerServiceResultEntity;
import de.cidaas.sdk.android.service.entity.notificationentity.getpendingnotification.NotificationEntity;
import de.cidaas.sdk.android.service.entity.socialprovider.SocialProviderEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationRequestEntity;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationResponseEntity;
import de.cidaas.sdk.android.service.entity.userlogininfo.UserLoginInfoEntity;
import de.cidaas.sdk.android.service.entity.userlogininfo.UserLoginInfoResponseEntity;
import de.cidaas.sdk.android.service.entity.userprofile.UserprofileResponseEntity;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by widasrnarayanan on 17/1/18.
 */

public interface ICidaasSDKService {
    // -----------------------------------------------------------POST
    // Call--------------------------------------------------------------
    // get Access token by Code
    @FormUrlEncoded
    @POST
    Call<AccessTokenEntity> getAccessTokenByCode(@Url String url,
            @HeaderMap Map<String, String> headers,
            @FieldMap(encoded = true) Map<String, String> params);

    // get Access token by refreshtoken
    @FormUrlEncoded
    @POST
    Call<AccessTokenEntity> getAccessTokenByRefreshToken(@Url String url,
            @HeaderMap Map<String, String> headers,
            @FieldMap(encoded = true) Map<String, String> params);

    @GET
    Call<SocialProviderEntity> getAccessTokenBySocial(@Url String url, @HeaderMap Map<String, String> headers);

    // Add FieldMap Pending
    // @FormUrlEncoded
    @POST
    Call<String> getLoginUrl(@Url String url, @HeaderMap Map<String, String> headers);

    // Enroll Face MFA
    @POST
    @Multipart
    Call<DocumentScannerServiceResultEntity> enrollDocument(@Url String url, @HeaderMap Map<String, String> headers,
            @Part MultipartBody.Part face);

    // Deny Notification
    @POST
    Call<de.cidaas.sdk.android.Service.Entity.NotificationEntity.DenyNotification.DenyNotificationResponseEntity> denyNotificationService(
            @Url String url, @Header("Content-Type") String content_type,
            @Header("access_token") String access_token, @HeaderMap Map<String, String> headers,
            @Body de.cidaas.sdk.android.Service.Entity.NotificationEntity.DenyNotification.DenyNotificationRequestEntity denyRequest);

    // Pending Notification
    @POST
    Call<NotificationEntity> getPendingNotification(@Url String url, @Header("Content-Type") String content_type,
            @Header("access_token") String access_token, @HeaderMap Map<String, String> headers);

    @POST
    Call<Object> updateFCMToken(@Url String url,
            @Header("access_token") String access_token,
            @HeaderMap Map<String, String> headers,
            @Body DeviceInfoEntity deviceInfoEntity);

    // Location History Service
    @POST
    Call<UserLoginInfoResponseEntity> getUserLoginInfoService(@Url String url, @HeaderMap Map<String, String> headers,
            @Body UserLoginInfoEntity userLoginInfoEntity);

    @POST
    Call<DeviceRegistrationResponseEntity> initiateDeviceRegistration(@Url String url,
            @HeaderMap Map<String, String> headers,
            @Body DeviceRegistrationRequestEntity body);

    // -----------------------------------------------------GetCall-----------------------------------------------------------------

    // Get Registration Setup
    // get userinfo
    @GET
    Call<UserInfoEntity> getUserInfo(@Url String url, @HeaderMap Map<String, String> headers);

    // Get TenantInfo
    @GET
    Call<UserprofileResponseEntity> getInternalUserProfileInfo(@Url String url, @HeaderMap Map<String, String> headers);

    // Construct URL
    @GET
    Call<Object> getUrlList(@Url String url, @HeaderMap Map<String, String> headers);

    // ---------
}
