package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentActivity;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.ui.face.FaceEnrollmentWizardActivity;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Face login after initiate: {@code push_acknowledge/face} → {@code allow/face}, then one camera capture (same
 * full-screen UI as enrollment, but a single step) → {@code authenticate/face} with multipart {@code photo}.
 */
public final class FaceLoginController {

    private static FaceLoginController shared;
    private final Context context;

    private FaceLoginController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized FaceLoginController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new FaceLoginController(context);
        }
        return shared;
    }

    public void runPushAcknowledgeAllowForFace(
            @NonNull final String initiateExchangeId,
            @NonNull final EventResult<String> onFinalExchangeId) {
        AuthenticatePushAcknowledgeAllowHelper.run(
                context, AuthenticationType.FACE, initiateExchangeId, onFinalExchangeId);
    }

    /**
     * Runs push acknowledge / allow for face, then the same camera wizard as enrollment with a single capture step.
     *
     * @param initiateExchangeId exchange id from {@code authenticate/initiate/face} (before acknowledge/allow)
     * @param dialogTitle          toolbar title (non-null)
     * @param dialogMessage        optional body under the step label; null treated as empty
     * @param initialFaceAttempt   {@code face_attempt} sent with the photo (often {@code 0})
     */
    public void verifyWithSingleFaceCaptureAfterPush(
            @NonNull final FragmentActivity activity,
            @NonNull final String initiateExchangeId,
            @NonNull final String dialogTitle,
            @Nullable final String dialogMessage,
            @StyleRes final int dialogThemeResId,
            final int initialFaceAttempt,
            @NonNull final EventResult<AuthenticateResponse> callback) {
        final String methodName = "FaceLoginController:verifyWithSingleFaceCaptureAfterPush()";
        if (initiateExchangeId.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "dialogTitle must not be null",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        final String message = dialogMessage != null ? dialogMessage : "";

        runPushAcknowledgeAllowForFace(initiateExchangeId, new EventResult<String>() {
            @Override
            public void success(String finalExchangeId) {
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing()) {
                        callback.failure(WebAuthError.getShared(context).methodException(
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                                WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                                "Activity is finishing; cannot start face login"));
                        return;
                    }
                    FaceEnrollmentWizardActivity.startForLogin(
                            activity,
                            finalExchangeId,
                            dialogTitle,
                            message,
                            dialogThemeResId,
                            initialFaceAttempt,
                            callback,
                            methodName);
                });
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }
}
