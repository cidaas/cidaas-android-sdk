package de.cidaas.sdk.android.cidaasverification.data.service;


import java.util.HashMap;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryResponseNew;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.UserAuthenticatedHistoryDataEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.UserAuthenticatedHistoryResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.delete.DeleteEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.delete.DeleteResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.deviceslist.DevicesListEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushacknowledge.PushAcknowledgeEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushacknowledge.PushAcknowledgeResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushallow.PushAllowEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushallow.PushAllowResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushreject.PushRejectEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushreject.PushRejectResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DeviceListResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DeviceMfaDataEntitiy;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DevicesMfaResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.SetUpCancelEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.SetUpCancelResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.configuredmfalist.ConfiguredMFAList;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.configuredmfalist.GetMFAListEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.pendingnotification.PendingNotificationEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.pendingnotification.PendingNotificationResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.updatefcmtoken.UpdateFCMTokenEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.updatefcmtoken.UpdateFCMTokenResponseEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.userlist.ConfiguredMFAListEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinue;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface ICidaasSDK_V2_Services {

    //Setup
    @POST
    Call<SetupResponse> setup(@Url String url, @HeaderMap Map<String, String> headers, @Body SetupEntity setupEntity);

    //Scanned
    @POST
    Call<ScannedResponse> scanned(@Url String url, @HeaderMap Map<String, String> headers, @Body ScannedEntity scannedEntity);

    //Enroll
    @POST
    Call<EnrollResponse> enroll(@Url String url, @HeaderMap Map<String, String> headers, @Body EnrollEntity enrollEntity);

    //Enroll with multipart
    @Multipart
    @POST
    Call<EnrollResponse> enrollWithMultipart(@Url String url, @HeaderMap Map<String, String> headers, @Part MultipartBody.Part face,
                                             @PartMap() HashMap<String, RequestBody> map);

    //Initiate
    @POST
    Call<InitiateResponse> initiate(@Url String url, @HeaderMap Map<String, String> headers, @Body InitiateEntity initiateEntity);

    //PushAcknowledge
    @POST
    Call<PushAcknowledgeResponse> pushAcknowledge(@Url String url, @HeaderMap Map<String, String> headers, @Body PushAcknowledgeEntity pushAcknowledgeEntity);

    //PushAllow
    @POST
    Call<PushAllowResponse> pushAllow(@Url String url, @HeaderMap Map<String, String> headers, @Body PushAllowEntity pushAllowEntity);

    //PushReject
    @POST
    Call<PushRejectResponse> pushReject(@Url String url, @HeaderMap Map<String, String> headers, @Body PushRejectEntity pushRejectEntity);

    //Authenticate
    @POST
    Call<AuthenticateResponse> authenticate(@Url String url, @HeaderMap Map<String, String> headers, @Body AuthenticateEntity authenticateEntity);


    //Authenticate Face MFA
    @POST
    @Multipart
    Call<AuthenticateResponse> authenticateWithMultipart(@Url String url, @HeaderMap Map<String, String> headers, @Part MultipartBody.Part face,
                                                         @PartMap() HashMap<String, RequestBody> map);

    //Delete MFA
    @HTTP(method = "DELETE", hasBody = true)
    Call<DeleteResponse> delete(@Url String url, @HeaderMap Map<String, String> headers, @Body DeleteEntity deleteEntity);

    //ConfiguredMFAList
    @POST
    Call<ConfiguredMFAList> getConfiguredMFAList(@Url String url, @HeaderMap Map<String, String> headers, @Body GetMFAListEntity getMFAListEntity);

    //For Authenticator app
    @GET
    Call<ConfiguredMFAListEntity> getConfiguredMFAList(@Url String url, @HeaderMap Map<String, String> headers, @Query("sub") String sub, @Query("userDeviceId") String userDeviceId);

    //PendingNotification List
    @POST
    Call<PendingNotificationResponse> getPendingNotification(@Url String url, @HeaderMap Map<String, String> headers, @Body PendingNotificationEntity pendingNotificationEntity);

    //AuthenticatedHistory List
    @POST
    Call<AuthenticatedHistoryResponse> getAuthenticatedHistory(@Url String url, @HeaderMap Map<String, String> headers, @Body AuthenticatedHistoryEntity authenticatedHistoryEntity);

    @POST
    Call<AuthenticatedHistoryResponseNew> getAuthenticatedHistoryNew(@Url String url, @HeaderMap Map<String, String> headers, @Body AuthenticatedHistoryEntity authenticatedHistoryEntity);

    //AuthenticatedHistory List
    @POST
    Call<UserAuthenticatedHistoryResponse> getAuthenticatedHistoryDetail(@Url String url, @HeaderMap Map<String, String> headers, @Body UserAuthenticatedHistoryDataEntity authenticatedHistoryEntity);



    //Update FCM List
    @POST
    Call<UpdateFCMTokenResponseEntity> updateFCMToken(@Url String url, @HeaderMap Map<String, String> headers, @Body UpdateFCMTokenEntity updateFCMTokenEntity);

    //Resume Login (body may be JSON, 302 Location, or plain text e.g. "Found. Redirecting to ...?code=...")
    @POST
    Call<ResponseBody> verificationContinue(@Url String url, @HeaderMap Map<String, String> headers, @Body VerificationContinue verificationContinueEntity);

    @POST
    Call<DeviceListResponse> getDevicesList(@Url String url, @HeaderMap Map<String, String> headers, @Body DevicesListEntity authenticatedHistoryEntity);

    @POST
    Call<DevicesMfaResponse> getDevicesRemove(@Url String url, @HeaderMap Map<String, String> headers, @Body DeviceMfaDataEntitiy authenticatedHistoryEntity);

    @POST
    Call<SetUpCancelResponse> setUpCancel(@Url String url, @HeaderMap Map<String, String> headers, @Body SetUpCancelEntity setUpCancelEntity);

}
