package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

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

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import de.cidaas.sdk.android.cidaasverification.R;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.util.Sha256Hex;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.cidaasverification.view.pattern.PatternLockView;
import de.cidaas.sdk.android.cidaasverification.view.pattern.PatternPasscodeFormatter;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Pattern login after initiate: {@code push_acknowledge/pattern} → {@code allow/pattern} → pattern UI (optional) →
 * {@code authenticate/pattern} with SHA-256 (lowercase hex, UTF-8) of {@code PREFIX[d1,d2,...]}.
 */
public final class PatternLoginController {

    private static PatternLoginController shared;
    private final Context context;

    private PatternLoginController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized PatternLoginController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new PatternLoginController(context);
        }
        return shared;
    }

    /**
     * After pattern initiate: {@code POST .../push_acknowledge/pattern} then {@code POST .../allow/pattern}.
     * On success, {@code onFinalExchangeId} receives the exchange id to use for {@code authenticate/pattern}.
     */
    public void runPushAcknowledgeAllowForPattern(
            @NonNull final String initiateExchangeId,
            @NonNull final EventResult<String> onFinalExchangeId) {
        AuthenticatePushAcknowledgeAllowHelper.run(
                context, AuthenticationType.PATTERN, initiateExchangeId, onFinalExchangeId);
    }

    /**
     * Runs {@link #runPushAcknowledgeAllowForPattern}, then v2 {@code authenticate/pattern} with the given
     * {@code pass_code} (authenticate only).
     */
    public void authenticatePassCodeAfterPushAcknowledgeAllow(
            @NonNull final String initiateExchangeId,
            @NonNull final String passCodeSha256Hex,
            @NonNull final EventResult<AuthenticateResponse> callback) {
        if (passCodeSha256Hex == null || passCodeSha256Hex.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "passCodeSha256Hex must not be null or empty",
                    "PatternLoginController:authenticatePassCodeAfterPushAcknowledgeAllow()"));
            return;
        }
        runPushAcknowledgeAllowForPattern(initiateExchangeId, new EventResult<String>() {
            @Override
            public void success(String finalExchangeId) {
                AuthenticateEntity authenticateEntity =
                        new AuthenticateEntity(finalExchangeId, passCodeSha256Hex, AuthenticationType.PATTERN);
                PasswordlessLoginController.getShared(context).authenticateVerificationOnly(authenticateEntity, callback);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    /**
     * Runs {@link #runPushAcknowledgeAllowForPattern}, then shows the same pattern lock modal as enrollment; on confirm
     * POSTs {@code authenticate/pattern} (authenticate only — call login continue next).
     *
     * @param patternCodePrefix optional prefix before hashing (default {@code RED})
     */
    public void verifyWithPatternLockDialog(
            @NonNull final FragmentActivity activity,
            @NonNull final String initiateExchangeId,
            @NonNull final String dialogTitle,
            @Nullable final String dialogMessage,
            @Nullable final String patternCodePrefix,
            @StyleRes final int dialogThemeResId,
            @NonNull final EventResult<AuthenticateResponse> callback) {
        final String methodName = "PatternLoginController:verifyWithPatternLockDialog()";
        if (initiateExchangeId == null || initiateExchangeId.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (dialogTitle == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Dialog title must not be null", VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        runPushAcknowledgeAllowForPattern(initiateExchangeId, new EventResult<String>() {
            @Override
            public void success(String finalExchangeId) {
                showPatternLockDialogAfterPushSteps(
                        activity, finalExchangeId, dialogTitle, dialogMessage, patternCodePrefix, dialogThemeResId, callback, methodName);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private void showPatternLockDialogAfterPushSteps(
            @NonNull final FragmentActivity activity,
            @NonNull final String exchangeIdForAuthenticate,
            @NonNull final String dialogTitle,
            @Nullable final String dialogMessage,
            @Nullable final String patternCodePrefix,
            @StyleRes final int dialogThemeResId,
            @NonNull final EventResult<AuthenticateResponse> callback,
            @NonNull final String methodName) {
        final String message = dialogMessage != null ? dialogMessage : "";
        final String verificationType = AuthenticationType.PATTERN;
        final AtomicBoolean completed = new AtomicBoolean(false);

        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) {
                if (completed.compareAndSet(false, true)) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName,
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            "Activity is finishing; cannot show pattern login dialog"));
                }
                return;
            }

            View content = LayoutInflater.from(activity).inflate(R.layout.cidaasverification_pattern_enroll_dialog, null);
            TextView titleView = content.findViewById(R.id.cidaasverification_pattern_modal_title);
            TextView msg = content.findViewById(R.id.cidaasverification_pattern_message);
            titleView.setText(dialogTitle);
            if (message.isEmpty()) {
                msg.setVisibility(View.GONE);
            } else {
                msg.setVisibility(View.VISIBLE);
                msg.setText(message);
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
                    .setOnCancelListener(d -> cancelPatternLogin(completed, callback, methodName));

            final AlertDialog dialog = builder.create();

            cancel.setOnClickListener(v -> {
                cancelPatternLogin(completed, callback, methodName);
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
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            "SHA-256 not available: " + e.getMessage()));
                    return;
                }
                if (!completed.compareAndSet(false, true)) {
                    return;
                }
                dialog.dismiss();
                AuthenticateEntity authenticateEntity =
                        new AuthenticateEntity(exchangeIdForAuthenticate, passCode, verificationType);
                PasswordlessLoginController.getShared(context).authenticateVerificationOnly(authenticateEntity, callback);
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

    private void cancelPatternLogin(
            @NonNull AtomicBoolean completed,
            @NonNull EventResult<AuthenticateResponse> callback,
            @NonNull String methodName) {
        if (completed.compareAndSet(false, true)) {
            callback.failure(WebAuthError.getShared(context).customException(
                    WebAuthErrorCode.USER_CANCELLED_LOGIN,
                    "Pattern login cancelled",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
        }
    }
}
