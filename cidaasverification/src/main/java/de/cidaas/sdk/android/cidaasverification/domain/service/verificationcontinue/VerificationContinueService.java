package de.cidaas.sdk.android.cidaasverification.domain.service.verificationcontinue;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinue;
import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinueResponseDataEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinueResponseEntity;
import de.cidaas.sdk.android.cidaasverification.data.service.CidaasSDK_V2_Service;
import de.cidaas.sdk.android.cidaasverification.data.service.ICidaasSDK_V2_Services;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.logger.LogFile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationContinueService {

    private Context context;

    public static VerificationContinueService shared;

    CidaasSDK_V2_Service service;


    public VerificationContinueService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasSDK_V2_Service();
        }
    }

    public static VerificationContinueService getShared(@NonNull Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new VerificationContinueService(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("VerificationContinueService instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }


    //call VerificationContinue Service
    public void callVerificationContinueService(@NonNull String verificationContinueURL, Map<String, String> headers, VerificationContinue verificationContinueEntity,
                                                final EventResult<VerificationContinueResponseEntity> verificationContinueCallback) {
        final String methodName = "VerificationContinueService:-callVerificationContinueService()";
        try {
            //call service — ResponseBody so non-JSON bodies (e.g. "Found. Redirecting to …?code=…") do not break Jackson
            ICidaasSDK_V2_Services cidaasSDK_v2_services = service.getInstance();
            cidaasSDK_v2_services.verificationContinue(verificationContinueURL, headers, verificationContinueEntity).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    String successBodyText = null;
                    if (response.isSuccessful() && response.body() != null) {
                        successBodyText = readBodySafely(response.body());
                    }
                    String code = VerificationContinueAuthorizationCodeParser.findAuthorizationCode(response.raw(), successBodyText);
                    if (code != null && !code.isEmpty()) {
                        VerificationContinueResponseEntity wrapped = new VerificationContinueResponseEntity();
                        wrapped.setSuccess(response.isSuccessful());
                        wrapped.setStatus(response.code());
                        VerificationContinueResponseDataEntity data = new VerificationContinueResponseDataEntity();
                        data.setCode(code);
                        wrapped.setData(data);
                        verificationContinueCallback.success(wrapped);
                        return;
                    }
                    if (!response.isSuccessful()) {
                        verificationContinueCallback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                                response, VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    } else {
                        verificationContinueCallback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                                "No authorization code in login continue response",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    verificationContinueCallback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                            t.getMessage(), VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            verificationContinueCallback.failure(WebAuthError.getShared(context).methodException(VerificationConstants.ERROR_LOGGING_PREFIX + methodName, WebAuthErrorCode.RESUME_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    @Nullable
    private static String readBodySafely(@Nullable ResponseBody body) {
        if (body == null) {
            return null;
        }
        try {
            return body.string();
        } catch (IOException e) {
            return null;
        }
    }
}
