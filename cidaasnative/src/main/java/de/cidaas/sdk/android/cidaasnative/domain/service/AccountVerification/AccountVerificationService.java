package de.cidaas.sdk.android.cidaasnative.domain.service.AccountVerification;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import de.cidaas.sdk.android.cidaasnative.R;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.AccountVerificationListResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.InitiateAccountVerificationRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.InitiateAccountVerificationResponseDataEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.InitiateAccountVerificationResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.VerifyAccountRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.accountverification.VerifyAccountResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.service.CidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.ICidaasNativeService;
import de.cidaas.sdk.android.cidaasnative.data.service.helper.NativeURLHelper;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.helper.commonerror.CommonError;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;
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

public class AccountVerificationService {
    CidaasNativeService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    //Local variables
    private String statusId;
    private String authenticationType;
    private String sub;
    private String verificationType;
    private Context context;

    public static AccountVerificationService shared;

    public AccountVerificationService(Context contextFromCidaas) {
        sub = "";
        statusId = "";
        verificationType = "";
        context = contextFromCidaas;
        authenticationType = "";


        if (service == null) {
            service = new CidaasNativeService(context);
        }

    }

    public static AccountVerificationService getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new AccountVerificationService(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    //----------------------------------------------------Register New User initiate  Account Verification--------------------------------------------------
    public void initiateAccountVerification(String baseurl, final InitiateAccountVerificationRequestEntity initiateAccountVerificationRequestEntity,
                                            final EventResult<InitiateAccountVerificationResponseEntity> callback) {
        //Local Variables

        String methodName = "AccountVerificationService :initiateAccountVerification()";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                String initiateAccountVerificationUrl = baseurl + NativeURLHelper.getShared().getRegisterUserAccountInitiate();

                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);

                serviceForInitiateAccountVerification(initiateAccountVerificationUrl, initiateAccountVerificationRequestEntity, headers, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForInitiateAccountVerification(String initiateAccountVerificationUrl,
            InitiateAccountVerificationRequestEntity initiateAccountVerificationRequestEntity,
            Map<String, String> headers, final EventResult<InitiateAccountVerificationResponseEntity> callback) {
        final String methodName = NativeConstants.METHOD_VERIFY_ACCOUNT_VERFICATION;
        OkHttpClient noRedirectClient = service.getOKHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(initiateAccountVerificationRequestEntity);
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.ERROR_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE, e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse(NativeURLHelper.contentTypeJson));
        Request.Builder requestBuilder = new Request.Builder().url(initiateAccountVerificationUrl).post(body);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        Request request = requestBuilder.build();

        noRedirectClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                        WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE, e.getMessage(),
                        NativeConstants.ERROR_LOGGING_PREFIX + methodName));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try (okhttp3.Response r = response) {
                    int code = r.code();
                    if (isHttpRedirectForAccountVerification(code)) {
                        InitiateAccountVerificationResponseEntity entity =
                                buildInitiateAccountVerificationFromRedirect(r);
                        if (entity != null) {
                            callback.success(entity);
                        } else {
                            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                    "Redirect without Location or without accvid query parameter",
                                    NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                        return;
                    }
                    if (r.isSuccessful() && code == 200) {
                        ResponseBody responseBody = r.body();
                        if (responseBody != null) {
                            String payload = responseBody.string();
                            if (payload != null && !payload.trim().isEmpty()) {
                                try {
                                    InitiateAccountVerificationResponseEntity parsed = objectMapper.readValue(payload,
                                            InitiateAccountVerificationResponseEntity.class);
                                    callback.success(parsed);
                                    return;
                                } catch (Exception parseEx) {
                                    callback.failure(WebAuthError.getShared(context).methodException(
                                            NativeConstants.ERROR_LOGGING_PREFIX + methodName,
                                            WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE,
                                            parseEx.getMessage()));
                                    return;
                                }
                            }
                        }
                        callback.failure(WebAuthError.getShared(context).serviceCallFailureException(
                                WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE,
                                "Empty 200 response for initiate account verification",
                                NativeConstants.ERROR_LOGGING_PREFIX + methodName));
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
                            WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE,
                            "HTTP " + code + ": " + err, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            NativeConstants.ERROR_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.INITIATE_ACCOUNT_VERIFICATION_FAILURE, e.getMessage()));
                }
            }
        });
    }

    private static boolean isHttpRedirectForAccountVerification(int code) {
        return code == 301 || code == 302 || code == 303 || code == 307 || code == 308;
    }

    /**
     * Builds {@link InitiateAccountVerificationResponseEntity} from a redirect {@code Location} URL query
     * ({@code accvid}).
     */
    private InitiateAccountVerificationResponseEntity buildInitiateAccountVerificationFromRedirect(
            okhttp3.Response response) {
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
        String accvid = resolved.queryParameter("accvid");
        if ((accvid == null || accvid.isEmpty()) && resolved.encodedFragment() != null
                && !resolved.encodedFragment().isEmpty()) {
            HttpUrl fragmentAsQuery = parseInitiateAccountVerificationFragmentAsQuery(resolved.encodedFragment());
            if (fragmentAsQuery != null) {
                if (accvid == null || accvid.isEmpty()) {
                    accvid = fragmentAsQuery.queryParameter("accvid");
                }
            }
        }
        if (accvid == null || accvid.isEmpty()) {
            return null;
        }
        InitiateAccountVerificationResponseDataEntity data = new InitiateAccountVerificationResponseDataEntity();
        data.setAccvid(accvid);
        InitiateAccountVerificationResponseEntity out = new InitiateAccountVerificationResponseEntity();
        out.setSuccess(true);
        out.setStatus(response.code());
        out.setData(data);
        return out;
    }

    private static HttpUrl parseInitiateAccountVerificationFragmentAsQuery(String encodedFragment) {
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

    //-----------------------------------------------------Register New User verify  Account Verification----------------------------------------------------
    public void verifyAccountVerification(String baseurl, final VerifyAccountRequestEntity verifyAccountRequestEntity,
                                          final EventResult<VerifyAccountResponseEntity> callback) {
        //Local Variables

        String methodName = NativeConstants.METHOD_VERIFY_ACCOUNT_VERFICATION;
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                String verifyAccountVerificationUrl = baseurl + NativeURLHelper.getShared().getRegisterUserAccountVerify();

                //Header
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);

                //Service
                serviceForVerifyAccountVerification(verifyAccountVerificationUrl, verifyAccountRequestEntity, headers, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForVerifyAccountVerification(String verifyAccountVerificationUrl, VerifyAccountRequestEntity
            verifyAccountRequestEntity, Map<String, String> headers, final EventResult<VerifyAccountResponseEntity> callback) {
        final String methodName = NativeConstants.METHOD_VERIFY_ACCOUNT_VERFICATION;
        try {
            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();

            cidaasNativeService.verifyAccountVerification(verifyAccountVerificationUrl, headers, verifyAccountRequestEntity)
                    .enqueue(new Callback<VerifyAccountResponseEntity>() {
                        @Override
                        public void onResponse(Call<VerifyAccountResponseEntity> call, Response<VerifyAccountResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                            response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if(null != response.errorBody()){
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                            response, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VerifyAccountResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                    t.getMessage(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-----------------------------------------------------Register New User  Account Verification List ----------------------------------------------------
    public void getAccountVerificationList(String baseurl, final String sub,
                                           final EventResult<AccountVerificationListResponseEntity> callback) {
        //Local Variables

        String methodName = "AccountVerificationService :getAccountVerificationList()";
        try {

            if (baseurl != null && !baseurl.equals("")) {
                //Construct URL For RequestId
                String verifyAccountListUrl = baseurl + NativeURLHelper.getShared().getAccountVerificationList(sub);

                //Header
                Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, NativeURLHelper.contentTypeJson);

                //Service
                serviceForGetAccountVerificationList(verifyAccountListUrl, headers, callback);
            } else {
                callback.failure(WebAuthError.getShared(context).propertyMissingException(context.getString(R.string.EMPTY_BASE_URL_SERVICE), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }

        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void serviceForGetAccountVerificationList(String verifyAccountVerificationUrl, Map<String, String> headers, final EventResult<AccountVerificationListResponseEntity> callback) {
        final String methodName = NativeConstants.METHOD_VERIFY_ACCOUNT_VERFICATION;
        try {
            //Call Service-getRequestId
            ICidaasNativeService cidaasNativeService = service.getInstance();

            cidaasNativeService.getAccountVerificationList(verifyAccountVerificationUrl, headers)
                    .enqueue(new Callback<AccountVerificationListResponseEntity>() {
                        @Override
                        public void onResponse(Call<AccountVerificationListResponseEntity> call, Response<AccountVerificationListResponseEntity> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    callback.success(response.body());
                                } else {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                            response.code(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }
                            } else {
                                if (null != response.errorBody()){
                                    callback.failure(CommonError.getShared(context).generateCommonErrorEntity(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                            response, NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<AccountVerificationListResponseEntity> call, Throwable t) {
                            callback.failure(WebAuthError.getShared(context).serviceCallFailureException(WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                                    t.getMessage(), NativeConstants.ERROR_LOGGING_PREFIX + methodName));
                        }
                    });
        } catch (Exception e) {
            callback.failure(WebAuthError.getShared(context).methodException(NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.VERIFY_ACCOUNT_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
}
