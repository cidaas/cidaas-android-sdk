package de.cidaas.sdk.android.cidaasnative.domain.service.SetPassword;

import android.content.Context;

import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.resetpassword.changepassword.ChangePasswordResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.setpassword.SetPasswordRequestEntity;
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

public class SetPasswordService {

    CidaasNativeService service;
    private Context context;

    public static SetPasswordService shared;

    public SetPasswordService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasNativeService(context);
        }
    }

    public static SetPasswordService getShared(Context contextFromCidaas) {
        if (shared == null) {
            shared = new SetPasswordService(contextFromCidaas);
        }
        return shared;
    }

    public void setPassword(
            SetPasswordRequestEntity setPasswordRequestEntity,
            String baseurl,
            DeviceInfoEntity deviceInfoEntityFromParam,
            final EventResult<ChangePasswordResponseEntity> callback) {
        final String methodName = "SetPassword Service :setPassword()";
        try {
            if (baseurl != null && !baseurl.equals("")) {
                String setPasswordUrl = baseurl + NativeURLHelper.getShared().getSetPasswordURL();
                Map<String, String> headers = Headers.getShared(context).getHeaders(
                        setPasswordRequestEntity.getAccess_token(), false, NativeURLHelper.contentTypeJson);
                serviceForSetPassword(setPasswordRequestEntity, setPasswordUrl, headers, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.SET_PASSWORD_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForSetPassword(
            SetPasswordRequestEntity setPasswordRequestEntity,
            String setPasswordUrl,
            Map<String, String> headers,
            final EventResult<ChangePasswordResponseEntity> callback) {
        final String methodName = "SetPassword Service :serviceForSetPassword()";
        try {
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.setPassword(setPasswordUrl, headers, setPasswordRequestEntity)
                    .enqueue(new Callback<ChangePasswordResponseEntity>() {
                        @Override
                        public void onResponse(
                                Call<ChangePasswordResponseEntity> call,
                                Response<ChangePasswordResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.SET_PASSWORD_FAILURE,
                                            response.code(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                        WebAuthErrorCode.SET_PASSWORD_FAILURE,
                                        response,
                                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        }

                        @Override
                        public void onFailure(Call<ChangePasswordResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.SET_PASSWORD_FAILURE,
                                    t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.SET_PASSWORD_FAILURE,
                    e.getMessage()));
        }
    }
}
