package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import de.cidaas.sdk.android.cidaasverification.R;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.scanned.ScannedController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.setup.SetupController;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Smart push MFA enrollment: {@code setup/initiate/push/} → {@code setup/scan/push/} → card-style modal →
 * {@code setup/enroll/push/} with {@code pass_code} set to {@code push_selected_number} from the setup response.
 */
public final class PushEnrollmentController {

    private static PushEnrollmentController shared;
    private final Context context;

    private PushEnrollmentController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized PushEnrollmentController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new PushEnrollmentController(context);
        }
        return shared;
    }

    /**
     * Same chain as fingerprint enrollment up to scan; then shows a card-style modal (Material card, accent bar,
     * primary/secondary actions). On accept, enrolls using
     * {@link de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponseData#getPush_selected_number()}.
     *
     * @param acceptButtonText label for the confirm button; when null or blank, {@code "Accept"} is used
     * @param dialogThemeResId optional {@link androidx.appcompat.app.AlertDialog} theme (e.g. Material3 overlay) for
     *                         window decor and {@link com.google.android.material.button.MaterialButton} styling;
     *                         use {@code 0} for the default dialog theme from the activity context
     */
    public void enrollWithAcceptDialog(
            @NonNull final FragmentActivity activity,
            @NonNull final String sub,
            @NonNull final String dialogTitle,
            @NonNull final String dialogMessage,
            @Nullable final String acceptButtonText,
            @StyleRes final int dialogThemeResId,
            @NonNull final EventResult<EnrollResponse> callback) {
        final String methodName = "PushEnrollmentController:enrollWithAcceptDialog()";
        if (sub.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (dialogTitle == null || dialogMessage == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Dialog title and message must not be null", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        final String verificationType = AuthenticationType.SMARTPUSH;
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
                    String pushSelectedNumber = setupResult.getData().getPush_selected_number();
                    if (pushSelectedNumber == null || pushSelectedNumber.trim().isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "push_selected_number missing from setup response (required for push enroll)",
                                VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                        return;
                    }
                    final String passCode = pushSelectedNumber.trim();

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

                                showAcceptDialog(activity, dialogTitle, dialogMessage, acceptButtonText,
                                        dialogThemeResId, scannedExchangeId, passCode, verificationType, callback,
                                        methodName);
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

    private void showAcceptDialog(
            @NonNull final FragmentActivity activity,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull final String scannedExchangeId,
            @NonNull final String passCode,
            @NonNull final String verificationType,
            @NonNull final EventResult<EnrollResponse> callback,
            @NonNull final String methodName) {
        final String positiveLabel = (acceptButtonText != null && !acceptButtonText.trim().isEmpty())
                ? acceptButtonText.trim()
                : "Accept";
        final AtomicBoolean completed = new AtomicBoolean(false);

        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) {
                if (completed.compareAndSet(false, true)) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                            "Activity is finishing; cannot show push enrollment dialog"));
                }
                return;
            }
            AlertDialog.Builder builder = dialogThemeResId != 0
                    ? new AlertDialog.Builder(activity, dialogThemeResId)
                    : new AlertDialog.Builder(activity);

            View content = LayoutInflater.from(activity).inflate(R.layout.cidaasverification_push_enroll_modal, null);
            TextView titleView = content.findViewById(R.id.cidaasverification_push_modal_title);
            TextView messageView = content.findViewById(R.id.cidaasverification_push_modal_message);
            MaterialButton cancelBtn = content.findViewById(R.id.cidaasverification_push_modal_cancel);
            MaterialButton acceptBtn = content.findViewById(R.id.cidaasverification_push_modal_accept);

            applyPushModalIconTheming(activity, content);

            titleView.setText(dialogTitle);
            messageView.setText(dialogMessage);
            acceptBtn.setText(positiveLabel);

            final AlertDialog dialog = builder
                    .setView(content)
                    .setCancelable(true)
                    .setOnCancelListener(d -> cancelPushEnrollment(completed, callback, methodName))
                    .create();

            cancelBtn.setOnClickListener(v -> {
                cancelPushEnrollment(completed, callback, methodName);
                dialog.dismiss();
            });
            acceptBtn.setOnClickListener(v -> {
                if (!completed.compareAndSet(false, true)) {
                    return;
                }
                dialog.dismiss();
                EnrollEntity enrollEntity = new EnrollEntity(scannedExchangeId, passCode, verificationType);
                EnrollController.getShared(context).enrollVerification(enrollEntity, callback);
            });

            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                DisplayMetrics dm = activity.getResources().getDisplayMetrics();
                int maxCardPx = (int) activity.getResources().getDimension(R.dimen.cidaasverification_push_modal_max_width);
                int screenMarginPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 32f, dm);
                int targetWidth = Math.min(maxCardPx, dm.widthPixels - screenMarginPx);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = Math.max(targetWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280f, dm));
                lp.gravity = Gravity.CENTER;
                window.setAttributes(lp);
            }
        });
    }

    private void cancelPushEnrollment(
            @NonNull AtomicBoolean completed,
            @NonNull EventResult<EnrollResponse> callback,
            @NonNull String methodName) {
        if (completed.compareAndSet(false, true)) {
            callback.failure(WebAuthError.getShared(context).customException(
                    WebAuthErrorCode.USER_CANCELLED_LOGIN,
                    "Push enrollment cancelled",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
        }
    }

    /**
     * Tints the push modal icon circle and glyph from the activity theme ({@code colorPrimary} /
     * {@code colorOnPrimary} when present, with sensible fallbacks).
     */
    private static void applyPushModalIconTheming(@NonNull FragmentActivity activity, @NonNull View content) {
        View iconContainer = content.findViewById(R.id.cidaasverification_push_icon_container);
        AppCompatImageView iconImg = content.findViewById(R.id.cidaasverification_push_modal_icon);
        if (iconContainer == null || iconImg == null) {
            return;
        }
        int primary = MaterialColors.getColor(
                activity,
                com.google.android.material.R.attr.colorPrimary,
                ContextCompat.getColor(activity, R.color.cidaasverification_push_modal_accent));
        int onPrimary = MaterialColors.getColor(
                activity,
                com.google.android.material.R.attr.colorOnPrimary,
                Color.WHITE);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(primary);
        ViewCompat.setBackground(iconContainer, circle);

        iconImg.setImageResource(R.drawable.cidaasverification_ic_push_notifications);
        ImageViewCompat.setImageTintList(iconImg, ColorStateList.valueOf(onPrimary));
    }
}
