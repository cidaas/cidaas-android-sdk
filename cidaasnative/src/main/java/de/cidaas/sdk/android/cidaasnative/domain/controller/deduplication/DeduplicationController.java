package de.cidaas.sdk.android.cidaasnative.domain.controller.deduplication;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasnative.data.entity.deduplication.DeduplicationResponseEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.deduplication.registerdeduplication.RegisterDeduplicationEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginCredentialsRequestEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginEntity;
import de.cidaas.sdk.android.cidaasnative.domain.service.Deduplication.DeduplicationService;
import de.cidaas.sdk.android.cidaasnative.domain.service.Login.NativeLoginService;
import de.cidaas.sdk.android.cidaasnative.util.NativeConstants;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.properties.CidaasProperties;
import timber.log.Timber;

public class DeduplicationController {
    private Context context;
    private String TrackId;

    public static DeduplicationController shared;

    public DeduplicationController(Context contextFromCidaas) {

        context = contextFromCidaas;
    }

    public static DeduplicationController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new DeduplicationController(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    // -------------------------------------------------getDeduplicationList-------------------------------------------------------------

    public void getDeduplicationList(final String trackId,
            final EventResult<DeduplicationResponseEntity> deduplicaionResult) {
        String methodName = "DeduplicationController:getDeduplicationList()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {
                    String baseurl = result.get(NativeConstants.DOMAIN_URL);
                    DeduplicationService.getShared(context).getDeduplicationList(baseurl, trackId, deduplicaionResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    deduplicaionResult.failure(error);
                }
            });
        } catch (Exception e) {
            deduplicaionResult.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DEDUPLICATION_LIST_FAILURE,
                    e.getMessage()));
        }
    }

    // -------------------------------------------------registerDeduplication-------------------------------------------------------------
    public void registerDeduplication(@NonNull String baseurl, @NonNull final String trackId,
            final EventResult<RegisterDeduplicationEntity> deduplicaionResult) {
        final String methodName = "DeduplicationController:registerDeduplication()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {
                    String baseurl = result.get(NativeConstants.DOMAIN_URL);
                    String clientId = result.get(NativeConstants.CLIENT_ID);
                    if (trackId != null && !trackId.equals("")) {
                        DeduplicationService.getShared(context).registerDeduplication(baseurl, trackId,
                                deduplicaionResult);
                    } else {
                        String errorMessage = "TrackId Must not be null";
                        deduplicaionResult.failure(
                                WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));

                    }
                }

                @Override
                public void failure(WebAuthError error) {
                    deduplicaionResult
                            .failure(WebAuthError.getShared(context).cidaasPropertyMissingException("", methodName));
                }
            });
        } catch (Exception e) {
            deduplicaionResult.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DEDUPLICATION_LIST_FAILURE,
                    e.getMessage()));
        }

    }

    // -------------------------------------------------registerDeduplication-------------------------------------------------------------

    public void loginDeduplication(@NonNull final String requestId, @NonNull final String sub,
            @NonNull final String password,
            final EventResult<LoginCredentialsResponseEntity> deduplicaionResult) {
        final String methodName = "DeduplicationController:loginDeduplication()";
        try {
            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> result) {

                    LoginEntity loginEntity = new LoginEntity();
                    loginEntity.setUsername(sub);
                    loginEntity.setUsername_type("sub");
                    loginEntity.setPassword(password);

                    loginCredenditalsWithSub(result, loginEntity, requestId, deduplicaionResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    deduplicaionResult
                            .failure(WebAuthError.getShared(context).cidaasPropertyMissingException("", methodName));
                }
            });
        } catch (Exception e) {
            deduplicaionResult.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.DEDUPLICATION_LIST_FAILURE, e.getMessage()));
        }
    }

    // -------------------------------------------------registerDeduplication-------------------------------------------------------------

    private void loginCredenditalsWithSub(Dictionary<String, String> result, LoginEntity loginEntity, String requestId,
            final EventResult<LoginCredentialsResponseEntity> deduplicaionResult) {
        String methodName = "DeduplicationController :loginCredenditalsWithSub()";
        try {
            String baseurl = result.get(NativeConstants.DOMAIN_URL);
            String clientId = result.get(NativeConstants.CLIENT_ID);
            if (loginEntity.getUsername() != null && !loginEntity.getUsername().isEmpty()
                    && loginEntity.getPassword() != null &&
                    !loginEntity.getPassword().isEmpty()) {

                LoginCredentialsRequestEntity loginCredentialsRequestEntity = new LoginCredentialsRequestEntity();
                loginCredentialsRequestEntity.setUsername(loginEntity.getUsername());
                loginCredentialsRequestEntity.setPassword(loginEntity.getPassword());
                loginCredentialsRequestEntity.setUsername_type("sub");
                loginCredentialsRequestEntity.setRequestId(requestId);

                NativeLoginService.getShared(context).loginWithCredentials(baseurl, loginCredentialsRequestEntity,
                        deduplicaionResult);

            } else {
                String errorMessage = "Sub or requestId or Password Must not be null";
                deduplicaionResult
                        .failure(WebAuthError.getShared(context).propertyMissingException(errorMessage, methodName));
            }
        } catch (Exception e) {
            deduplicaionResult.failure(WebAuthError.getShared(context).methodException(
                    NativeConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DEDUPLICATION_LIST_FAILURE,
                    e.getMessage()));
        }
    }
}
