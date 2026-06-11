package de.cidaas.sdk.android.cidaasnative.domain.service.RequestId;

import android.content.Context;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.authrequest.AuthRequestResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class RequestIdService {

    private Context context;

    public static RequestIdService shared;

    CidaasNativeService service;
    public ObjectMapper objectMapper = new ObjectMapper();

    public RequestIdService(Context contextFromCidaas) {
        context = contextFromCidaas;

        if (service == null) {
            service = new CidaasNativeService(context);
        }
    }

    public static RequestIdService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new RequestIdService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    //-------------------------------------------------------Get Request ID From Service--------------------------------------------------------------
    public void getRequestID(Dictionary<String, String> loginProperties, final EventResult<AuthRequestResponseEntity> callback,
                             @Nullable HashMap<String, String>... extraParams) {
        String methodName = "RequestIdService :getRequestID()";
        try {

            if (loginProperties.get(NativeConstants.DOMAIN_URL) != null && !loginProperties.get(NativeConstants.DOMAIN_URL).equals("")) {

                //Header Generation
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentType);

                // Get Device Information
                Dictionary<String, String> challengeProperties = DBHelper.getShared().getChallengeProperties();

                //Construct URL For RequestId
                String requestidURL = loginProperties.get(NativeConstants.DOMAIN_URL) + NativeURLHelper.getShared().getRequest_id_url();

                String codeChallenge = "";
                String clientSecret = " ";

                if (challengeProperties.get("Challenge") != null) {
                    codeChallenge = challengeProperties.get("Challenge");
                }
                if (challengeProperties.get("ClientSecret") != null) {
                    clientSecret = challengeProperties.get("ClientSecret");
                }


                Map<String, String> authRequestEntityMap = new HashMap<>();

                authRequestEntityMap.put("client_id", URLEncoder.encode(loginProperties.get(NativeConstants.CLIENT_ID), "utf-8"));
                authRequestEntityMap.put("redirect_uri", URLEncoder.encode(loginProperties.get("RedirectURL"), "utf-8"));
                authRequestEntityMap.put("response_type", "code");
                authRequestEntityMap.put("nonce", UUID.randomUUID().toString());
                authRequestEntityMap.put("client_secret", clientSecret);
                authRequestEntityMap.put("code_challenge", codeChallenge);
                authRequestEntityMap.put("code_challenge_method", "S256");

                for (Map.Entry<String, String> entry : Cidaas.extraParams.entrySet()) {
                    authRequestEntityMap.put(entry.getKey(), URLEncoder.encode(entry.getValue(), "utf-8"));
                }
                if (extraParams != null) {
                    for (HashMap<String, String> extraMap : extraParams) {
                        if (extraMap == null) {
                            continue;
                        }
                        for (Map.Entry<String, String> e : extraMap.entrySet()) {
                            if (e.getKey() != null && e.getValue() != null) {
                                authRequestEntityMap.put(e.getKey(), URLEncoder.encode(e.getValue(), "utf-8"));
                            }
                        }
                    }
                }
                serviceForGetRequestId(requestidURL, authRequestEntityMap, headers, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE),
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE, e.getMessage()));
        }
    }

    public void serviceForGetRequestId(String requestidURL, Map<String, String> authRequestEntityMap, Map<String, String> headers,
                                       final EventResult<AuthRequestResponseEntity> callback) {
        final String methodName = "RequestIdService :getRequestID()";
        try {
            //add codeChallenge and codeChallengeMethod and clientSecret
            //Call Service-getRequestId
            ICidaasNativeService CidaasNativeService = service.getInstance();

            //Service call
            CidaasNativeService.getRequestId(requestidURL, headers, authRequestEntityMap).enqueue(new Callback<AuthRequestResponseEntity>() {
                @Override
                public void onResponse(Call<AuthRequestResponseEntity> call, Response<AuthRequestResponseEntity> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            callback.success(response.body());
                        } else {
                            callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE,
                                    response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    } else {
                        assert response.errorBody() != null;
                        callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE, response
                                , NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                    }
                }

                @Override
                public void onFailure(Call<AuthRequestResponseEntity> call, Throwable t) {
                    callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE, t.getMessage(),
                            NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName));
                }
            });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.REQUEST_ID_SERVICE_FAILURE, e.getMessage()));
        }
    }

}
