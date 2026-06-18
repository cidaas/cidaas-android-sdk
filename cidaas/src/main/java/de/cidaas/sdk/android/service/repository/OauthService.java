package de.cidaas.sdk.android.service.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;
import de.cidaas.sdk.android.service.CidaassdkService;
import de.cidaas.sdk.android.service.ICidaasSDKService;
import de.cidaas.sdk.android.service.entity.UserInfo.UserInfoEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by widasrnarayanan on 16/1/18.
 */

public class OauthService {

    CidaassdkService service;

    public Context context;
    public static OauthService shared;
    public WebAuthError webAuthError;
    URLHelper urlHelper = new URLHelper();
    public ObjectMapper objectMapper = new ObjectMapper();

    // Create Shared instances
    public static OauthService getShared(Context contextfromcidaas) {
        if (shared == null) {
            shared = new OauthService(contextfromcidaas);
        }
        return shared;
    }

    // Constructor
    OauthService(Context contextfromcidaas) {
        this.context = contextfromcidaas;
        if (service == null) {
            service = new CidaassdkService(context);
        }
        webAuthError = new WebAuthError(context);
    }

    // Get Login URL
    public void getLoginUrl(String requestId, @NonNull String DomainURL, final EventResult<String> callback) {
        try {
            // Local Variables
            String finalUrl = "";
            String baseUrl = "";

            URLHelper urlComponents;

            // Get Properties From DB
            Dictionary<String, String> loginProperties = DBHelper.getShared().getLoginProperties(DomainURL);

            if (loginProperties == null) {
                callback.failure(webAuthError.loginURLMissingException("getLoginUrl"));
            }

            // Construct URl Checking,Add Parameter(FieldMap) pending
            callback.success(finalUrl);
        } catch (Exception e) {
            Timber.d(e.getMessage());
            callback.failure(webAuthError);
        }
    }

    public void getUserinfo(String AccessToken, String DomainURL, final EventResult<UserInfoEntity> callback) {
        final String methodName = "OauthService:-getUserinfo()";
        try {
            // Local Variables
            String url = "";
            String baseUrl = "";

            Map<String, String> headers = new Hashtable<>();

            // get Device Information
            DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();

            // check Construct Headers pending,Null Checking Pending
            // Add headers
            headers.put("Content-Type", URLHelper.contentType);
            headers.put("access_token", AccessToken);
            headers.put("device-id", deviceInfoEntity.getDeviceId());
            headers.put("device-make", deviceInfoEntity.getDeviceMake());
            headers.put("device-model", deviceInfoEntity.getDeviceModel());
            headers.put("device-version", deviceInfoEntity.getDeviceVersion());
            headers.put("lat", LocationDetails.getShared(context).getLatitude());
            headers.put("long", LocationDetails.getShared(context).getLongitude());

            // Get Properties From DB

            Dictionary<String, String> loginProperties = DBHelper.getShared().getLoginProperties(DomainURL);
            if (loginProperties == null) {
                callback.failure(WebAuthError.getShared(context).loginURLMissingException(methodName));
            }

            // querymap.put("UserInfoURL",loginProperties.get("UserInfoURL"));

            // Assign Url
            // Perform Null Check
            if (null != loginProperties) {
                url = loginProperties.get("DomainURL") + URLHelper.getShared().getUserInfoURL();
            }

            // call Service
            ICidaasSDKService cidaassdkService = service.getInstance();
            cidaassdkService.getUserInfo(url, headers).enqueue(new Callback<UserInfoEntity>() {
                @Override
                public void onResponse(Call<UserInfoEntity> call, Response<UserInfoEntity> response) {
                    if (response.isSuccessful()) {

                        if (response.code() == 200) {
                            callback.success(response.body());
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                    WebAuthErrorCode.USER_INFO_SERVICE_FAILURE,
                                    response.code(), methodName));
                        }
                    } else {
                        // callback requires
                        assert response.errorBody() != null;
                        CommonError.getShared(context).generateCommonErrorEntity(
                                WebAuthErrorCode.USER_INFO_SERVICE_FAILURE, response, methodName);
                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                WebAuthErrorCode.USER_INFO_SERVICE_FAILURE, response, methodName));
                    }
                }

                @Override
                public void onFailure(Call<UserInfoEntity> call, Throwable t) {
                    Timber.e("Faliure in getAccessTokenByCode id call" + t.getMessage());
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE, t.getMessage(), methodName));

                }
            });
        } catch (Exception e) {
            Timber.d(e.getMessage());
            callback.failure(webAuthError);
        }
    }

}
