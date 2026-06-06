package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentActivity;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.scanned.ScannedController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.setup.SetupController;
import de.cidaas.sdk.android.cidaasverification.ui.face.FaceEnrollmentWizardActivity;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Face MFA enrollment: {@code setup/initiate/face/} → {@code setup/scan/face/} → full-screen wizard (up to three
 * captures) → {@code setup/enroll/face/} with multipart {@code photo}. The wizard continues while the API
 * indicates more images are needed and finishes on the first fully successful response.
 */
public final class FaceEnrollmentController {

    private static FaceEnrollmentController shared;
    private final Context context;

    private FaceEnrollmentController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized FaceEnrollmentController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new FaceEnrollmentController(context);
        }
        return shared;
    }

    /**
     * @param dialogMessage optional body text; null is treated as empty
     * @param faceAttempt sent as {@code face_attempt} on enroll (often {@code 0} for first enrollment)
     * @param dialogThemeResId {@code 0} for default dialog theme
     */
    public void enrollWithCameraCapture(
            @NonNull final FragmentActivity activity,
            @NonNull final String sub,
            @NonNull final String dialogTitle,
            @Nullable final String dialogMessage,
            final int faceAttempt,
            @StyleRes final int dialogThemeResId,
            @NonNull final EventResult<EnrollResponse> callback) {
        final String methodName = "FaceEnrollmentController:enrollWithCameraCapture()";
        if (sub.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Dialog title must not be null", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        final String message = dialogMessage != null ? dialogMessage : "";

        final String verificationType = AuthenticationType.FACE;
        SetupEntity setupEntity = new SetupEntity(sub, verificationType);
        SetupController.getShared(context).setupVerification(setupEntity, new EventResult<SetupResponse>() {
            @Override
            public void success(SetupResponse setupResult) {
                try {
                    if (setupResult == null || setupResult.getData() == null
                            || setupResult.getData().getExchange_id() == null) {
                        callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                WebAuthErrorCode.SETUP_VERIFICATION_FAILURE, 0,
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                    String setupExchangeId = setupResult.getData().getExchange_id().getExchange_id();
                    if (setupExchangeId == null || setupExchangeId.isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "exchange_id missing from setup response",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }

                    ScannedEntity scannedEntity = new ScannedEntity(sub, setupExchangeId, verificationType);
                    ScannedController.getShared(context).scannedVerification(scannedEntity, new EventResult<ScannedResponse>() {
                        @Override
                        public void success(ScannedResponse scannedResult) {
                            try {
                                if (scannedResult == null || scannedResult.getData() == null
                                        || scannedResult.getData().getExchange_id() == null) {
                                    callback.failure(WebAuthError.getShared(context).emptyResponseException(
                                            WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE, 0,
                                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                    return;
                                }
                                String scannedExchangeId = scannedResult.getData().getExchange_id().getExchange_id();
                                if (scannedExchangeId == null || scannedExchangeId.isEmpty()) {
                                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                            "exchange_id missing from scan response",
                                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                                    return;
                                }

                                startFaceEnrollmentWizard(activity, dialogTitle, message, scannedExchangeId, faceAttempt,
                                        dialogThemeResId, callback, methodName);
                            } catch (Exception e) {
                                callback.failure(WebAuthError.getShared(context).methodException(
                                        VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                                        WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE, e.getMessage()));
                            }
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            callback.failure(error);
                        }
                    });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.SETUP_VERIFICATION_FAILURE, e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private void startFaceEnrollmentWizard(
            @NonNull FragmentActivity activity,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @NonNull String scannedExchangeId,
            int faceAttempt,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<EnrollResponse> callback,
            @NonNull String methodName) {
        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) {
                callback.failure(WebAuthError.getShared(context).methodException(
                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                        WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                        "Activity is finishing; cannot start face enrollment"));
                return;
            }
            FaceEnrollmentWizardActivity.start(
                    activity,
                    scannedExchangeId,
                    dialogTitle,
                    dialogMessage,
                    dialogThemeResId,
                    faceAttempt,
                    callback,
                    methodName);
        });
    }
}
