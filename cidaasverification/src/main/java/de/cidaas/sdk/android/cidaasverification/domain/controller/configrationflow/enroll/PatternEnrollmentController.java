package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import android.content.Context;
import android.content.res.ColorStateList;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import de.cidaas.sdk.android.cidaasverification.util.Sha256Hex;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.cidaasverification.view.pattern.PatternLockView;
import de.cidaas.sdk.android.cidaasverification.view.pattern.PatternPasscodeFormatter;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pattern MFA enrollment: {@code setup/initiate/pattern/} → {@code setup/scan/pattern/} → pattern lock dialog →
 * {@code setup/enroll/pattern/} with {@code pass_code} set to the SHA-256 (lowercase hex, UTF-8) of the pattern string
 * {@code PREFIX[d1,d2,...]} (1-based indices), same encoding as {@link PatternPasscodeFormatter}.
 */
public final class PatternEnrollmentController {

    private static PatternEnrollmentController shared;
    private final Context context;

    private PatternEnrollmentController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized PatternEnrollmentController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new PatternEnrollmentController(context);
        }
        return shared;
    }

    /**
     * @param dialogMessage optional instructions under the title; null is treated as empty
     * @param patternCodePrefix optional prefix for the pattern string before hashing (e.g. {@code RED}); null/blank defaults to {@code RED}
     * @param dialogThemeResId {@code 0} for default dialog theme
     */
    public void enrollWithPatternLockDialog(
            @NonNull final FragmentActivity activity,
            @NonNull final String sub,
            @NonNull final String dialogTitle,
            @Nullable final String dialogMessage,
            @Nullable final String patternCodePrefix,
            @StyleRes final int dialogThemeResId,
            @NonNull final EventResult<EnrollResponse> callback) {
        final String methodName = "PatternEnrollmentController:enrollWithPatternLockDialog()";
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

        final String verificationType = AuthenticationType.PATTERN;
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

                                showPatternLockDialog(activity, dialogTitle, message, patternCodePrefix, dialogThemeResId,
                                        scannedExchangeId, verificationType, callback, methodName);
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

    private void showPatternLockDialog(
            @NonNull final FragmentActivity activity,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull final String scannedExchangeId,
            @NonNull final String verificationType,
            @NonNull final EventResult<EnrollResponse> callback,
            @NonNull final String methodName) {
        final AtomicBoolean completed = new AtomicBoolean(false);

        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) {
                if (completed.compareAndSet(false, true)) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                            "Activity is finishing; cannot show pattern enrollment dialog"));
                }
                return;
            }

            View content = LayoutInflater.from(activity).inflate(R.layout.cidaasverification_pattern_enroll_dialog, null);
            TextView titleView = content.findViewById(R.id.cidaasverification_pattern_modal_title);
            TextView msg = content.findViewById(R.id.cidaasverification_pattern_message);
            titleView.setText(dialogTitle);
            if (dialogMessage.isEmpty()) {
                msg.setVisibility(View.GONE);
            } else {
                msg.setVisibility(View.VISIBLE);
                msg.setText(dialogMessage);
            }
            PatternLockView lockView = content.findViewById(R.id.cidaasverification_pattern_lock);
            lockView.applyThemedColors(activity);
            MaterialButton clear = content.findViewById(R.id.cidaasverification_pattern_clear);
            MaterialButton confirm = content.findViewById(R.id.cidaasverification_pattern_confirm);
            MaterialButton cancel = content.findViewById(R.id.cidaasverification_pattern_cancel);

            applyPatternModalCardTheming(activity, content);

            AlertDialog.Builder builder = dialogThemeResId != 0
                    ? new AlertDialog.Builder(activity, dialogThemeResId)
                    : new AlertDialog.Builder(activity);
            builder.setView(content)
                    .setCancelable(true)
                    .setOnCancelListener(d -> cancelPatternEnrollment(completed, callback, methodName));

            final AlertDialog dialog = builder.create();

            cancel.setOnClickListener(v -> {
                cancelPatternEnrollment(completed, callback, methodName);
                dialog.dismiss();
            });
            clear.setOnClickListener(v -> lockView.clearPattern());
            confirm.setOnClickListener(v -> {
                if (!lockView.isPatternLongEnough()) {
                    Toast.makeText(activity, R.string.cidaasverification_pattern_too_short, Toast.LENGTH_SHORT).show();
                    return;
                }
                String patternString = PatternPasscodeFormatter.format(patternCodePrefix, lockView.getSelectedCells());
                final String passCode;
                try {
                    passCode = Sha256Hex.sha256HexUtf8(patternString);
                } catch (NoSuchAlgorithmException e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                            "SHA-256 not available: " + e.getMessage()));
                    return;
                }
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

    /**
     * Card surface, stroke, and divider from the activity theme so the pattern modal matches the host app.
     */
    private static void applyPatternModalCardTheming(@NonNull FragmentActivity activity, @NonNull View content) {
        MaterialCardView card = content.findViewById(R.id.cidaasverification_pattern_modal_card);
        View divider = content.findViewById(R.id.cidaasverification_pattern_modal_divider);
        if (card == null) {
            return;
        }
        int surface = MaterialColors.getColor(
                activity,
                com.google.android.material.R.attr.colorSurface,
                Color.parseColor("#FFFAFAFA"));
        int outline = MaterialColors.getColor(
                activity,
                com.google.android.material.R.attr.colorOutline,
                Color.parseColor("#FF79747E"));
        card.setCardBackgroundColor(surface);
        card.setStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(outline, (int) (0.22f * 255))));
        if (divider != null) {
            divider.setBackgroundColor(ColorUtils.setAlphaComponent(outline, (int) (0.18f * 255)));
        }
    }

    private void cancelPatternEnrollment(
            @NonNull AtomicBoolean completed,
            @NonNull EventResult<EnrollResponse> callback,
            @NonNull String methodName) {
        if (completed.compareAndSet(false, true)) {
            callback.failure(WebAuthError.getShared(context).customException(
                    WebAuthErrorCode.USER_CANCELLED_LOGIN,
                    "Pattern enrollment cancelled",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
        }
    }
}
