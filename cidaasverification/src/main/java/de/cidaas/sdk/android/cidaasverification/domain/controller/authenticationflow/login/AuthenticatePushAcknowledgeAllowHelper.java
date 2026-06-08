package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushacknowledge.PushAcknowledgeEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushacknowledge.PushAcknowledgeResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushallow.PushAllowEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushallow.PushAllowResponse;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.push.pushacknowledge.PushAcknowledgeController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.push.pushallow.PushAllowController;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Shared v2 step after initiate for some verification types: {@code push_acknowledge/&lt;type&gt;} then
 * {@code allow/&lt;type&gt;}, yielding the exchange id to use on {@code authenticate/&lt;type&gt;}.
 */
public final class AuthenticatePushAcknowledgeAllowHelper {

    private AuthenticatePushAcknowledgeAllowHelper() {
    }

    /**
     * @param verificationType e.g. {@link de.cidaas.sdk.android.helper.AuthenticationType#PATTERN} or {@code TOUCHID}
     */
    public static void run(
            @NonNull Context context,
            @NonNull final String verificationType,
            @NonNull final String initiateExchangeId,
            @NonNull final EventResult<String> onFinalExchangeId) {
        final String methodName = "AuthenticatePushAcknowledgeAllowHelper.run()";
        final String segment = verificationType.toLowerCase();
        if (initiateExchangeId.trim().isEmpty()) {
            onFinalExchangeId.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        PushAcknowledgeEntity ackEntity = new PushAcknowledgeEntity(initiateExchangeId, verificationType);
        PushAcknowledgeController.getShared(context).pushAcknowledgeVerification(ackEntity,
                new EventResult<PushAcknowledgeResponse>() {
                    @Override
                    public void success(PushAcknowledgeResponse ackResponse) {
                        String afterAck = exchangeIdAfterPushAcknowledge(ackResponse, initiateExchangeId);
                        if (afterAck == null || afterAck.isEmpty()) {
                            onFinalExchangeId.failure(WebAuthError.getShared(context).propertyMissingException(
                                    "push_acknowledge/" + segment + " response missing usable exchange_id",
                                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                            return;
                        }
                        PushAllowEntity allowEntity = new PushAllowEntity(afterAck, verificationType);
                        PushAllowController.getShared(context).pushAllowVerification(allowEntity,
                                new EventResult<PushAllowResponse>() {
                                    @Override
                                    public void success(PushAllowResponse allowResponse) {
                                        String afterAllow = exchangeIdAfterPushAllow(allowResponse, afterAck);
                                        if (afterAllow == null || afterAllow.isEmpty()) {
                                            onFinalExchangeId.failure(WebAuthError.getShared(context).propertyMissingException(
                                                    "push_allow/" + segment + " response missing usable exchange_id",
                                                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                            return;
                                        }
                                        onFinalExchangeId.success(afterAllow);
                                    }

                                    @Override
                                    public void failure(WebAuthError error) {
                                        onFinalExchangeId.failure(error);
                                    }
                                });
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        onFinalExchangeId.failure(error);
                    }
                });
    }

    @Nullable
    private static String exchangeIdAfterPushAcknowledge(
            @Nullable PushAcknowledgeResponse response,
            @NonNull String fallbackExchangeId) {
        if (response == null || response.getData() == null || response.getData().getExchange_id() == null) {
            return nonEmptyOrNull(fallbackExchangeId);
        }
        String id = response.getData().getExchange_id().getExchange_id();
        return id != null && !id.isEmpty() ? id : nonEmptyOrNull(fallbackExchangeId);
    }

    @Nullable
    private static String exchangeIdAfterPushAllow(
            @Nullable PushAllowResponse response,
            @NonNull String fallbackExchangeId) {
        if (response == null || response.getData() == null || response.getData().getExchange_id() == null) {
            return nonEmptyOrNull(fallbackExchangeId);
        }
        String id = response.getData().getExchange_id().getExchange_id();
        return id != null && !id.isEmpty() ? id : nonEmptyOrNull(fallbackExchangeId);
    }

    @Nullable
    private static String nonEmptyOrNull(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return s;
    }
}
