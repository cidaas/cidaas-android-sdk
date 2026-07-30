package de.cidaas.sdk.android.cidaasverification.domain.service.authenticate;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.CidaasSDK_V2_Service;
import de.cidaas.sdk.android.cidaasverification.data.service.ICidaasSDK_V2_Services;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.logger.LogFile;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AuthenticateService {
    private Context context;

    public static AuthenticateService shared;

    CidaasSDK_V2_Service service;


    public AuthenticateService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasSDK_V2_Service();
        }

    }

    public static AuthenticateService getShared(@NonNull Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new AuthenticateService(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("AuthenticateService instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }

    //call authenticate Service
    public void callAuthenticateService(@NonNull String authenticateURL, Map<String, String> headers, AuthenticateEntity authenticateEntity,
                                        final EventResult<AuthenticateResponse> authenticateCallback) {
        final String methodName = "AuthenticateService:-callAuthenticateService()";
        try {

            LogFile.getShared(context).addInfoLog(methodName, " AuthenticateURL:- " + authenticateURL);

            //call service
            ICidaasSDK_V2_Services cidaasSDK_v2_services = service.getInstance();
            cidaasSDK_v2_services.authenticate(authenticateURL, headers, authenticateEntity).enqueue(new Callback<AuthenticateResponse>() {
                @Override
                public void onResponse(Call<AuthenticateResponse> call, Response<AuthenticateResponse> response) {
                    if (response.isSuccessful()) {
                        authenticateCallback.success(response.body());

                        LogFile.getShared(context).addSuccessLog(methodName,
                                "Authenticate succeeded. Status id:- " + resolveStatusId(response));
                    } else {
                        authenticateCallback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE,
                                response, VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<AuthenticateResponse> call, Throwable t) {
                    authenticateCallback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE,
                            t.getMessage(), VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            authenticateCallback.failure(WebAuthError.getShared(context).methodException(VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    //call authenticate Service
    public void callAuthenticateServiceForFaceOrVoice(MultipartBody.Part fileToSend, @NonNull String authenticateURL, Map<String, String> headers,
                                                      HashMap<String, RequestBody> authenticateHashmap, final EventResult<AuthenticateResponse> authenticateCallback) {
        final String methodName = "AuthenticateService:-callAuthenticateServiceForFaceOrVoice()";
        try {
            //call service

            LogFile.getShared(context).addInfoLog(methodName, " AuthenticateURL:- " + authenticateURL);

            ICidaasSDK_V2_Services cidaasSDK_v2_services = service.getInstance();
            cidaasSDK_v2_services.authenticateWithMultipart(authenticateURL, headers, fileToSend, authenticateHashmap).enqueue(new Callback<AuthenticateResponse>() {
                @Override
                public void onResponse(Call<AuthenticateResponse> call, Response<AuthenticateResponse> response) {
                    if (response.isSuccessful()) {

                        authenticateCallback.success(response.body());

                        LogFile.getShared(context).addSuccessLog(methodName,
                                "Authenticate succeeded. Status id:- " + resolveStatusId(response));
                    } else {
                        authenticateCallback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, response, VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        // Handle proper error message
                        String errorResponse = "NO message";
                        try {
                            errorResponse = response.errorBody().source().readByteString().utf8();
                        } catch (Exception e) {
                            Timber.d(e.getMessage() + errorResponse + e.getMessage());
                        }

                        LogFile.getShared(context).addFailureLog(errorResponse);
                    }
                }

                @Override
                public void onFailure(Call<AuthenticateResponse> call, Throwable t) {
                    authenticateCallback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE,
                            t.getMessage(), VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            authenticateCallback.failure(WebAuthError.getShared(context).methodException(VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.AUTHENTICATE_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    private static String resolveStatusId(Response<AuthenticateResponse> response) {
        if (response.body() != null && response.body().getData() != null) {
            return String.valueOf(response.body().getData().getStatus_id());
        }
        return "unknown";
    }
}
