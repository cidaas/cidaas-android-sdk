package de.cidaas.sdk.android.cidaasnative.domain.service.publicconfiguredverifications;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist.PublicConfiguredListDataEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist.PublicConfiguredListRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist.PublicConfiguredListResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class PublicConfiguredVerificationsListService {

    private CidaasNativeService service;
    private Context context;

    public static PublicConfiguredVerificationsListService shared;

    public PublicConfiguredVerificationsListService(Context contextFromCidaas) {
        context = contextFromCidaas;
        if (service == null) {
            service = new CidaasNativeService(context);
        }
    }

    public static PublicConfiguredVerificationsListService getShared(Context contextFromCidaas) {
        try {
            if (shared == null) {
                shared = new PublicConfiguredVerificationsListService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void postPublicConfiguredVerificationsList(String baseurl, PublicConfiguredListRequestEntity body,
            final EventResult<PublicConfiguredListResponseEntity> callback) {
        String methodName = NativeConstants.METHOD_PUBLIC_CONFIGURED_VERIFICATIONS_LIST;
        try {
            if (baseurl == null || baseurl.equals("")) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            if (body == null) {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Request body must not be null", NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
            String url = baseurl + NativeURLHelper.getShared().getPublicConfiguredVerificationsList();
            Map<String, String> headers =
                    Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);
            executePost(url, headers, body, callback, methodName);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }

    private void executePost(String fullUrl, Map<String, String> headers, PublicConfiguredListRequestEntity body,
            final EventResult<PublicConfiguredListResponseEntity> callback, final String methodName) {
        try {
            ICidaasNativeService cidaasNativeService = service.getInstance();
            cidaasNativeService.postPublicConfiguredVerificationsList(fullUrl, headers, body)
                    .enqueue(new Callback<PublicConfiguredListResponseEntity>() {
                        @Override
                        public void onResponse(Call<PublicConfiguredListResponseEntity> call,
                                Response<PublicConfiguredListResponseEntity> response) {
                            if (response.isSuccessful()) {
                                int code = response.code();
                                if (code == 200) {
                                    callback.success(response.body());
                                } else if (code == 204) {
                                    PublicConfiguredListResponseEntity entity =
                                            new PublicConfiguredListResponseEntity();
                                    entity.setSuccess(true);
                                    entity.setStatus(204);
                                    PublicConfiguredListDataEntity data = new PublicConfiguredListDataEntity();
                                    data.setConfigured_list(new ArrayList<>());
                                    entity.setData(data);
                                    callback.success(entity);
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE,
                                            response.code(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if (response.errorBody() != null) {
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(
                                            WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE, response,
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                } else {
                                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                            WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE,
                                            "HTTP " + response.code() + ": " + response.message(),
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<PublicConfiguredListResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                    WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE, t.getMessage(),
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.PUBLIC_CONFIGURED_VERIFICATIONS_LIST_FAILURE, e.getMessage()));
        }
    }
}
