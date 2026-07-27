package de.cidaas.sdk.android.cidaasverification.domain.service.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.settings.configuredmfalist.ConfiguredMFAList;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.configuredmfalist.GetMFAListEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.updatefcmtoken.UpdateFCMTokenEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.updatefcmtoken.UpdateFCMTokenResponseEntity;
import de.cidaas.sdk.android.cidaasverification.data.service.CidaasSDK_V2_Service;
import de.cidaas.sdk.android.cidaasverification.data.service.ICidaasSDK_V2_Services;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.logger.LogFile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsService {
    private Context context;

    public static SettingsService shared;

    CidaasSDK_V2_Service service;


    public SettingsService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasSDK_V2_Service();
        }

    }

    public static SettingsService getShared(@NonNull Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new SettingsService(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("SettingsService instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }


    //call Settings Service
    public void getConfigurationList(@NonNull String settingsURL, Map<String, String> headers, final GetMFAListEntity getMFAListEntity,
                                     final EventResult<ConfiguredMFAList> configuredMFAListResult) {
        final String methodName = "SettingsService:-getConfigurationList()";
        try {

            LogFile.getShared(context).addInfoLog(methodName, getMFAListEntity.getSub());
            //call service
            ICidaasSDK_V2_Services cidaasSDK_v2_services = service.getInstance();
            cidaasSDK_v2_services.getConfiguredMFAList(settingsURL, headers, getMFAListEntity).enqueue(new Callback<ConfiguredMFAList>() {
                @Override
                public void onResponse(Call<ConfiguredMFAList> call, Response<ConfiguredMFAList> response) {
                    if (response.isSuccessful()) {

                        LogFile.getShared(context).addSuccessLog(methodName, "Configuration list retrieved successfully. Sub:-" + getMFAListEntity.getSub()
                        );
                        if (response.code() == 200) {
                            configuredMFAListResult.success(response.body());
                        } else {

                            ConfiguredMFAList configuredMFAList = new ConfiguredMFAList();
                            configuredMFAList.setStatus(response.code());
                            configuredMFAList.setSuccess(true);
                            configuredMFAListResult.success(configuredMFAList);
                        }
                    } else {
                        configuredMFAListResult.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.MFA_LIST_VERIFICATION_FAILURE,
                                response, VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<ConfiguredMFAList> call, Throwable t) {
                    configuredMFAListResult.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.MFA_LIST_VERIFICATION_FAILURE,
                            t.getMessage(), VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            configuredMFAListResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.MFA_LIST_VERIFICATION_FAILURE, e.getMessage()));
        }
    }

    //call update FCM Token
    public void updateFCMToken(@NonNull String updateFCMTOkenURL, Map<String, String> headers, UpdateFCMTokenEntity updateFCMTokenEntity,
                               final EventResult<UpdateFCMTokenResponseEntity> result) {
        final String methodName = "SettingsService:-updateFCMToken()";
        try {
            //call service
            ICidaasSDK_V2_Services cidaasSDK_v2_services = service.getInstance();
            cidaasSDK_v2_services.updateFCMToken(updateFCMTOkenURL, headers, updateFCMTokenEntity).enqueue(new Callback<UpdateFCMTokenResponseEntity>() {
                @Override
                public void onResponse(Call<UpdateFCMTokenResponseEntity> call, Response<UpdateFCMTokenResponseEntity> response) {
                    if (response.isSuccessful()) {
                        result.success(response.body());
                    } else {
                        result.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.UPDATE_FCM_TOKEN,
                                response, VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<UpdateFCMTokenResponseEntity> call, Throwable t) {
                    result.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.UPDATE_FCM_TOKEN,
                            t.getMessage(), VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(VerificationConstants.ERROR_LOGGING_PREFIX + methodName, WebAuthErrorCode.UPDATE_FCM_TOKEN, e.getMessage()));
        }
    }

    public void getConfigurationListThirdParty(String configuredListURL, Map<String, String> headers, GetMFAListEntity getMFAListEntity, EventResult<ConfiguredMFAList> configuredMFAListResult) {


        final String methodName = "SettingsService:-getConfigurationList()";
        try {

            LogFile.getShared(context).addInfoLog(methodName, getMFAListEntity.getSub());
            LogFile.getShared(context).addAPILog("other_devices configuration list API url: "+configuredListURL);
            //call service
            ICidaasSDK_V2_Services cidaasSDK_v2_services = service.getInstance();
            cidaasSDK_v2_services.getConfiguredMFAList(configuredListURL, headers, getMFAListEntity).enqueue(new Callback<ConfiguredMFAList>() {
                @Override
                public void onResponse(Call<ConfiguredMFAList> call, Response<ConfiguredMFAList> response) {
                    if (response.isSuccessful()) {

                        LogFile.getShared(context).addSuccessLog(methodName, "Configuration list retrieved successfully. Sub:-" + getMFAListEntity.getSub()
                        );
                        if (response.code() == 200) {
                            configuredMFAListResult.success(response.body());
                            LogFile.getShared(context).addAPILog("other_devices configuration list api completed successfully\n");
                        } else {

                            ConfiguredMFAList configuredMFAList = new ConfiguredMFAList();
                            configuredMFAList.setStatus(response.code());
                            configuredMFAList.setSuccess(true);
                            configuredMFAListResult.success(configuredMFAList);
                        }
                    } else {
                        configuredMFAListResult.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.MFA_LIST_VERIFICATION_FAILURE,
                                response, "Error:- " + methodName));
                    }
                }

                @Override
                public void onFailure(Call<ConfiguredMFAList> call, Throwable t) {
                    configuredMFAListResult.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.MFA_LIST_VERIFICATION_FAILURE,
                            t.getMessage(), "Error:- " + methodName));
                }
            });
        } catch (Exception e) {
            configuredMFAListResult.failure(WebAuthError.getShared(context).methodException("Exception :" + methodName,
                    WebAuthErrorCode.MFA_LIST_VERIFICATION_FAILURE, e.getMessage()));
        }
    }


}
