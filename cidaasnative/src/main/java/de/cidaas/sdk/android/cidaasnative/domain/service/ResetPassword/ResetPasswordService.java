package de.cidaas.sdk.android.cidaasnative.domain.service.ResetPassword;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Hashtable;
import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.ResetPasswordRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.ResetPasswordResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetnewpassword.ResetNewPasswordResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetnewpassword.ResetPasswordEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetpasswordvalidatecode.ResetPasswordValidateCodeRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetpasswordvalidatecode.ResetPasswordValidateCodeResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.resetpasswordvalidatecode.ResetPasswordValidateCodeDataEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ResetPasswordService {
    //Reset Password
    CidaasNativeService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    //Local variables
    private String statusId;
    private String authenticationType;
    private String sub;
    private String verificationType;
    private Context context;

    public static ResetPasswordService shared;

    public ResetPasswordService(Context contextFromCidaas) {
        sub = "";
        statusId = "";
        verificationType = "";
        context = contextFromCidaas;
        authenticationType = "";
        if (service == null) {
            service = new CidaasNativeService(context);
        }

    }


    public static ResetPasswordService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new ResetPasswordService(contextFromCidaas);
            }

        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void initiateresetPassword(ResetPasswordRequestEntity resetPasswordRequestEntity, String baseurl, DeviceInfoEntity deviceInfoEntityFromParam,String locale,
                                      final EventResult<ResetPasswordResponseEntity> callback) {
        //Local Variables
        String resetpasswordUrl = "";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                resetpasswordUrl = baseurl + NativeURLHelper.getShared().getResetPassword() + "?action=initiatereset";
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE)
                        , NativeConstants.ERROR_RESET_PASSWORD_INITIATE));
                return;
            }

            //Construct Body Parameter for Reset Password

            Map<String, String> headers = new Hashtable<>();
            // Get Device Information
            DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
            //This is only for testing purpose
            if (deviceInfoEntityFromParam == null) {
                deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
            } else {
                deviceInfoEntity = deviceInfoEntityFromParam;
            }
            //check Construct Headers pending,Null Checking Pending
            //Add headers
            headers.put(NativeConstants.CONTENT_TYPE, NativeURLHelper.contentTypeJson);
            headers.put(NativeConstants.USER_AGENT, "de.cidaas-android");
            headers.put(NativeConstants.DEVICE_ID, deviceInfoEntity.getDeviceId());
            headers.put(NativeConstants.DEVICE_MAKE, deviceInfoEntity.getDeviceMake());
            headers.put(NativeConstants.DEVICE_MODEL, deviceInfoEntity.getDeviceModel());
            headers.put(NativeConstants.DEVICE_VERSION, deviceInfoEntity.getDeviceVersion());
            headers.put(NativeConstants.ACCEPT_LANGUAGE,locale);
            headers.put(NativeConstants.DEVICE_LATTITUDE, LocationDetails.getShared(context).getLatitude());
            headers.put(NativeConstants.DEVICE_LONGITUDE, LocationDetails.getShared(context).getLongitude());

            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.initiateresetPassword(resetpasswordUrl, headers, resetPasswordRequestEntity).enqueue(new Callback<ResetPasswordResponseEntity>() {
                @Override
                public void onResponse(Call<ResetPasswordResponseEntity> call, Response<ResetPasswordResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            callback.success(response.body());
                        } else {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.INITIATE_RESET_PASSWORD_FAILURE,
                                    "Service failure but successful response", NativeConstants.ERROR_RESET_PASSWORD_INITIATE));
                        }
                    } else {
                        if(response.errorBody() != null){
                            callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.INITIATE_RESET_PASSWORD_FAILURE,
                                    response, NativeConstants.ERROR_RESET_PASSWORD_INITIATE));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResetPasswordResponseEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.INITIATE_RESET_PASSWORD_FAILURE,
                            t.getMessage(), NativeConstants.ERROR_RESET_PASSWORD_INITIATE));

                }
            });

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException("Exception :ResetPasswordService :initiateresetPassword()",
                    WebAuthErrorCode.INITIATE_RESET_PASSWORD_FAILURE, e.getMessage()));
        }
    }


    //Reset Password Validate Code
    public void resetPasswordValidateCode(ResetPasswordValidateCodeRequestEntity resetPasswordValidateCodeRequestEntity,
                                          String baseurl, final EventResult<ResetPasswordValidateCodeResponseEntity> callback) {
        //Local Variables
        String resetpasswordValidateCodeUrl = "";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                resetpasswordValidateCodeUrl = baseurl + NativeURLHelper.getShared().getResetPassword() + "?action=validatecode";
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                return;
            }

            //Construct Body Parameter for Reset Password

            Map<String, String> headers = new Hashtable<>();
            // Get Device Information
            DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();

            // - check Construct Headers pending,Null Checking Pending
            //Add headers
            headers.put(NativeConstants.CONTENT_TYPE, NativeURLHelper.contentTypeJson);
            headers.put(NativeConstants.USER_AGENT, "de.cidaas-android");
            headers.put(NativeConstants.DEVICE_ID, deviceInfoEntity.getDeviceId());
            headers.put(NativeConstants.DEVICE_MAKE, deviceInfoEntity.getDeviceMake());
            headers.put(NativeConstants.DEVICE_MODEL, deviceInfoEntity.getDeviceModel());
            headers.put(NativeConstants.DEVICE_VERSION, deviceInfoEntity.getDeviceVersion());
            headers.put(NativeConstants.DEVICE_LATTITUDE, LocationDetails.getShared(context).getLatitude());
            headers.put(NativeConstants.DEVICE_LONGITUDE, LocationDetails.getShared(context).getLongitude());
            serviceForResetPasswordValidateCode(resetpasswordValidateCodeUrl, resetPasswordValidateCodeRequestEntity, headers, callback);

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException("Exception :ResetPasswordService :resetPasswordValidateCode()",
                    WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE, e.getMessage()));
        }
    }

    public void serviceForResetPasswordValidateCode(String resetpasswordValidateCodeUrl,
            ResetPasswordValidateCodeRequestEntity resetPasswordValidateCodeRequestEntity, Map<String, String> headers,
            final EventResult<ResetPasswordValidateCodeResponseEntity> callback) {
        OkHttpClient noRedirectClient = service.getOKHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(resetPasswordValidateCodeRequestEntity);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    "Exception :ResetPasswordService :serviceForResetPasswordValidateCode()",
                    WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE, e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse(NativeURLHelper.contentTypeJson));
        Request.Builder requestBuilder = new Request.Builder().url(resetpasswordValidateCodeUrl).post(body);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        Request request = requestBuilder.build();

        noRedirectClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                        WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE, e.getMessage(),
                        NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try (okhttp3.Response r = response) {
                    int code = r.code();
                    if (isHttpRedirect(code)) {
                        ResetPasswordValidateCodeResponseEntity entity = buildValidateCodeResponseFromRedirect(r);
                        if (entity != null) {
                            callback.success(entity);
                        } else {
                            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                    "Redirect without Location or without exchangeId/rprq query parameters",
                                    NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                        }
                        return;
                    }
                    if (r.isSuccessful() && code == 200) {
                        ResponseBody responseBody = r.body();
                        if (responseBody != null) {
                            String payload = responseBody.string();
                            if (payload != null && !payload.trim().isEmpty()) {
                                try {
                                    ResetPasswordValidateCodeResponseEntity parsed = objectMapper.readValue(payload,
                                            ResetPasswordValidateCodeResponseEntity.class);
                                    callback.success(parsed);
                                    return;
                                } catch (Exception parseEx) {
                                    callback.failure(WebAuthError.getShared(context).methodException(
                                            NativeConstants.EXCEPTION_RESET_PASSWORD_NEW,
                                            WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE, parseEx.getMessage()));
                                    return;
                                }
                            }
                        }
                        callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE,
                                "Empty 200 response for validate code", NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                        return;
                    }
                    String err = r.message();
                    ResponseBody errBody = r.body();
                    if (errBody != null) {
                        try {
                            err = errBody.string();
                        } catch (IOException ignored) {
                            // keep r.message()
                        }
                    }
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                            WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE,
                            "HTTP " + code + ": " + err, NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            NativeConstants.EXCEPTION_RESET_PASSWORD_NEW,
                            WebAuthErrorCode.RESET_PASSWORD_VALIDATE_CODE_FAILURE, e.getMessage()));
                }
            }
        });
    }

    private static boolean isHttpRedirect(int code) {
        return code == 301 || code == 302 || code == 303 || code == 307 || code == 308;
    }

    /**
     * Builds {@link ResetPasswordValidateCodeResponseEntity} from a redirect's {@code Location} URL query
     * ({@code exchangeId}, {@code rprq}).
     */
    private ResetPasswordValidateCodeResponseEntity buildValidateCodeResponseFromRedirect(okhttp3.Response response) {
        String location = response.header("Location");
        if (location == null || location.isEmpty()) {
            return null;
        }
        HttpUrl requestUrl = response.request().url();
        HttpUrl resolved = requestUrl.resolve(location);
        if (resolved == null) {
            try {
                resolved = HttpUrl.get(location);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        String exchangeId = resolved.queryParameter("exchangeId");
        String rprq = resolved.queryParameter("rprq");
        if ((exchangeId == null || exchangeId.isEmpty() || rprq == null || rprq.isEmpty())
                && resolved.encodedFragment() != null && !resolved.encodedFragment().isEmpty()) {
            HttpUrl fragmentAsQuery = parseFragmentAsQueryUrl(resolved.encodedFragment());
            if (fragmentAsQuery != null) {
                if (exchangeId == null || exchangeId.isEmpty()) {
                    exchangeId = fragmentAsQuery.queryParameter("exchangeId");
                }
                if (rprq == null || rprq.isEmpty()) {
                    rprq = fragmentAsQuery.queryParameter("rprq");
                }
            }
        }
        if (exchangeId == null || exchangeId.isEmpty() || rprq == null || rprq.isEmpty()) {
            return null;
        }
        ResetPasswordValidateCodeDataEntity data = new ResetPasswordValidateCodeDataEntity();
        data.setExchangeId(exchangeId);
        data.setResetRequestId(rprq);
        ResetPasswordValidateCodeResponseEntity out = new ResetPasswordValidateCodeResponseEntity();
        out.setSuccess(true);
        out.setStatus(response.code());
        out.setData(data);
        return out;
    }

    private static HttpUrl parseFragmentAsQueryUrl(String encodedFragment) {
        if (encodedFragment == null) {
            return null;
        }
        String frag = encodedFragment.startsWith("?") ? encodedFragment.substring(1) : encodedFragment;
        if (frag.isEmpty()) {
            return null;
        }
        try {
            return HttpUrl.get("https://cidaas.local/placeholder?" + frag);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    //Reset Password Validate Code
    public void resetNewPassword(ResetPasswordEntity resetPasswordEntity,
                                 String baseurl, final EventResult<ResetNewPasswordResponseEntity> callback) {
        //Local Variables
        String ResetNewPasswordUrl = "";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For Change Password
                ResetNewPasswordUrl = baseurl + NativeURLHelper.getShared().getResetPassword() + "?action=acceptreset";
            } else {
                callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.PROPERTY_MISSING,
                        context.getString(R.string.PROPERTY_MISSING), NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                return;
            }

            //Construct Body Parameter for Reset Password

            Map<String, String> headers = new Hashtable<>();
            // Get Device Information
            DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();

            //- check Construct Headers pending,Null Checking Pending
            //Add headers
            headers.put(NativeConstants.CONTENT_TYPE, NativeURLHelper.contentTypeJson);
            headers.put(NativeConstants.DEVICE_ID, deviceInfoEntity.getDeviceId());
            headers.put(NativeConstants.DEVICE_MAKE, deviceInfoEntity.getDeviceMake());
            headers.put(NativeConstants.DEVICE_MODEL, deviceInfoEntity.getDeviceModel());
            headers.put(NativeConstants.DEVICE_VERSION, deviceInfoEntity.getDeviceVersion());
            headers.put(NativeConstants.DEVICE_LATTITUDE, LocationDetails.getShared(context).getLatitude());
            headers.put(NativeConstants.DEVICE_LONGITUDE, LocationDetails.getShared(context).getLongitude());
            serviceCallForResetNewPassword(ResetNewPasswordUrl, resetPasswordEntity, headers, callback);

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_RESET_PASSWORD_NEW,
                    WebAuthErrorCode.RESET_NEWPASSWORD_FAILURE, e.getMessage()));
        }
    }

    public void serviceCallForResetNewPassword(String resetNewPasswordUrl, ResetPasswordEntity resetPasswordEntity, Map<String, String> headers, final EventResult<ResetNewPasswordResponseEntity> callback) {
        //Call Service-getRequestId
        ICidaasNativeService cidaasNativeService = service.getInstance();
        cidaasNativeService.ResetNewPassword(resetNewPasswordUrl, headers, resetPasswordEntity)
                .enqueue(new Callback<ResetNewPasswordResponseEntity>() {
                    @Override
                    public void onResponse(Call<ResetNewPasswordResponseEntity> call, Response<ResetNewPasswordResponseEntity> response) {
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                callback.success(response.body());
                            } else {
                                callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.RESET_NEWPASSWORD_FAILURE,
                                        "Service failure but successful response", NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                            }
                        } else {
                            if( response.errorBody() != null){
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.RESET_NEWPASSWORD_FAILURE, response,
                                        NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResetNewPasswordResponseEntity> call, Throwable t) {
                        callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                WebAuthErrorCode.RESET_NEWPASSWORD_FAILURE, t.getMessage(), NativeConstants.EXCEPTION_RESET_PASSWORD_NEW));

                    }
                });
    }

}
