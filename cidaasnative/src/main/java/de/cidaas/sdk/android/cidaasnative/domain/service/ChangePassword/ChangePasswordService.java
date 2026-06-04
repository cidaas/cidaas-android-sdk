package de.cidaas.sdk.android.cidaasnative.domain.service.ChangePassword;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.changepassword.ChangePasswordRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.changepassword.ChangePasswordResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordService {
    CidaasNativeService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    //Local variables
    private String statusId;
    private String authenticationType;
    private String sub;
    private String verificationType;
    private Context context;

    public static ChangePasswordService shared;

    public ChangePasswordService(Context contextFromCidaas) {
        sub = "";
        statusId = "";
        verificationType = "";
        context = contextFromCidaas;
        authenticationType = "";

        if (service == null) {
            service = new CidaasNativeService(context);
        }

    }


    public static ChangePasswordService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new ChangePasswordService(contextFromCidaas);
            }
        } catch (Exception e) {
            //Timber.i(e.getMessage());
        }
        return shared;
    }

    //------------------------------------------------------------------------------Reset Password Validate Code
    public void changePassword(ChangePasswordRequestEntity changePasswordRequestEntity,
                               String baseurl, DeviceInfoEntity deviceInfoEntityFromParam, final EventResult<ChangePasswordResponseEntity> callback) {
        //Local Variables
        final String methodName = "ChangePassword Service :changePassword()";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For Change Password
                String changePasswordUrl = baseurl + NativeURLHelper.getShared().getChangePasswordURL();


                //Construct header
                Map<String, String> headers = Headers.getShared(context).getHeaders(changePasswordRequestEntity.getAccess_token(), false,
                        NativeURLHelper.contentTypeJson);
                serviceForChangePassword(changePasswordRequestEntity, changePasswordUrl, headers, callback);


            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.CHANGE_PASSWORD_FAILURE, e.getMessage()));
        }
    }

    private void serviceForChangePassword(ChangePasswordRequestEntity changePasswordRequestEntity, String changePasswordUrl,
                                          Map<String, String> headers, final EventResult<ChangePasswordResponseEntity> callback) {
        final String methodName = "ChangePassword Service :serviceForChangePassword()";
        try {
            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.changePassword(changePasswordUrl, headers, changePasswordRequestEntity)
                    .enqueue(new Callback<ChangePasswordResponseEntity>() {
                        @Override
                        public void onResponse(Call<ChangePasswordResponseEntity> call, Response<ChangePasswordResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.CHANGE_PASSWORD_FAILURE,
                                            response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.CHANGE_PASSWORD_FAILURE, response,
                                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<ChangePasswordResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.CHANGE_PASSWORD_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));

                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.CHANGE_PASSWORD_FAILURE, e.getMessage()));
        }
    }
}
