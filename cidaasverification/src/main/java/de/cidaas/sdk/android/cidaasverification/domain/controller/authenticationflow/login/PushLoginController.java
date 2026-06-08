package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import java.util.concurrent.atomic.AtomicBoolean;

import de.cidaas.sdk.android.cidaasverification.R;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Push login after initiate: {@code push_acknowledge/push} → {@code allow/push}, then an accept-only card modal;
 * on accept POSTs {@code authenticate/push} with {@code pass_code} from the initiate response
 * ({@code push_selected_number}).
 */
public final class PushLoginController {

    private static PushLoginController shared;
    private final Context context;

    private PushLoginController(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public static synchronized PushLoginController getShared(@NonNull Context context) {
        if (shared == null) {
            shared = new PushLoginController(context);
        }
        return shared;
    }

    /**
     * After push initiate: {@code POST .../push_acknowledge/push} then {@code POST .../allow/push}.
     */
    public void runPushAcknowledgeAllowForPush(
            @NonNull final String initiateExchangeId,
            @NonNull final EventResult<String> onFinalExchangeId) {
        AuthenticatePushAcknowledgeAllowHelper.run(
                context, AuthenticationType.SMARTPUSH, initiateExchangeId, onFinalExchangeId);
    }

    /**
     * Runs push acknowledge / allow, then shows the same push card modal as enrollment with only the Accept action
     * visible; on accept POSTs {@code authenticate/push} with {@code passCode} (authenticate only — call login continue
     * next).
     *
     * @param dialogTitle   non-null display title
     * @param dialogMessage non-null body text
     * @param acceptButtonText when null or blank, {@code "Accept"} is used
     */
    public void verifyWithAcceptDialogAfterPush(
            @NonNull final FragmentActivity activity,
            @NonNull final String initiateExchangeId,
            @NonNull final String passCode,
            @NonNull final String dialogTitle,
            @NonNull final String dialogMessage,
            @Nullable final String acceptButtonText,
            @StyleRes final int dialogThemeResId,
            @NonNull final EventResult<AuthenticateResponse> callback) {
        final String methodName = "PushLoginController:verifyWithAcceptDialogAfterPush()";
        if (initiateExchangeId == null || initiateExchangeId.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (passCode == null || passCode.trim().isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "passCode (push_selected_number) must not be null or empty",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }
        if (dialogTitle == null || dialogMessage == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Dialog title and message must not be null",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
            return;
        }

        final String verificationType = AuthenticationType.SMARTPUSH;
        final String trimmedPass = passCode.trim();

        runPushAcknowledgeAllowForPush(initiateExchangeId, new EventResult<String>() {
            @Override
            public void success(String finalExchangeId) {
                showAcceptDialogForLogin(
                        activity,
                        dialogTitle,
                        dialogMessage,
                        acceptButtonText,
                        dialogThemeResId,
                        finalExchangeId,
                        trimmedPass,
                        verificationType,
                        callback,
                        methodName);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private void showAcceptDialogForLogin(
            @NonNull final FragmentActivity activity,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull final String exchangeAfterAllow,
            @NonNull final String passCode,
            @NonNull final String verificationType,
            @NonNull final EventResult<AuthenticateResponse> callback,
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
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            "Activity is finishing; cannot show push login dialog"));
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
            cancelBtn.setVisibility(View.GONE);

            final AlertDialog dialog = builder
                    .setView(content)
                    .setCancelable(true)
                    .setOnCancelListener(d -> cancelPushLogin(completed, callback, methodName))
                    .create();

            acceptBtn.setOnClickListener(v -> {
                if (!completed.compareAndSet(false, true)) {
                    return;
                }
                dialog.dismiss();
                AuthenticateEntity authenticateEntity =
                        new AuthenticateEntity(exchangeAfterAllow, passCode, verificationType);
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

    private void cancelPushLogin(
            @NonNull AtomicBoolean completed,
            @NonNull EventResult<AuthenticateResponse> callback,
            @NonNull String methodName) {
        if (completed.compareAndSet(false, true)) {
            callback.failure(WebAuthError.getShared(context).customException(
                    WebAuthErrorCode.USER_CANCELLED_LOGIN,
                    "Push login cancelled",
                    VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
        }
    }

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
