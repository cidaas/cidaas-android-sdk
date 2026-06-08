package de.cidaas.sdk.android.cidaasverification.ui.face;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import de.cidaas.sdk.android.cidaasverification.R;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.FaceLoginResultBridge;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.PasswordlessLoginController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.EnrollController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.FaceEnrollInterpreter;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.FaceEnrollmentResultBridge;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Full-screen face capture: <strong>enrollment</strong> runs up to three steps while the API asks for more images;
 * <strong>login</strong> uses a single capture then {@code authenticate/face}.
 */
public final class FaceEnrollmentWizardActivity extends AppCompatActivity {

    public static final String EXTRA_EXCHANGE_ID = "cidaasverification_face_wizard_exchange_id";
    public static final String EXTRA_TITLE = "cidaasverification_face_wizard_title";
    public static final String EXTRA_MESSAGE = "cidaasverification_face_wizard_message";
    public static final String EXTRA_THEME = "cidaasverification_face_wizard_theme";
    public static final String EXTRA_INITIAL_FACE_ATTEMPT = "cidaasverification_face_wizard_initial_attempt";
    public static final String EXTRA_LOG_PREFIX = "cidaasverification_face_wizard_log_prefix";
    /** {@link #MODE_ENROLL} (default) or {@link #MODE_LOGIN_SINGLE}. */
    public static final String EXTRA_MODE = "cidaasverification_face_wizard_mode";

    public static final int MODE_ENROLL = 0;
    /** One capture → authenticate only (after push acknowledge / allow outside this activity). */
    public static final int MODE_LOGIN_SINGLE = 1;

    private static final int REQ_CAMERA_PERMISSION = 44021;

    private int mode = MODE_ENROLL;
    private boolean loginMode;
    private int maxCaptureSteps = 3;

    private String currentExchangeId;
    private int initialFaceAttempt;
    private String introMessage;
    private String logPrefix;

    private int currentStepIndex;

    private MaterialToolbar toolbar;
    private TextView stepLabel;
    private TextView messageView;
    private PreviewView previewView;
    private MaterialButton captureButton;
    private ProgressBar progressBar;
    private final View[] stepDots = new View[3];

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private ImageCapture imageCapture;

    private final AtomicBoolean enrolling = new AtomicBoolean(false);
    private final AtomicBoolean terminalOutcome = new AtomicBoolean(false);

    public static void start(
            @NonNull FragmentActivity activity,
            @NonNull String exchangeId,
            @NonNull String title,
            @NonNull String message,
            int themeResId,
            int initialFaceAttempt,
            @NonNull EventResult<EnrollResponse> callback,
            @NonNull String logPrefixForCancel) {
        FaceEnrollmentResultBridge.setPending(callback);
        Intent i = new Intent(activity, FaceEnrollmentWizardActivity.class);
        i.putExtra(EXTRA_MODE, MODE_ENROLL);
        i.putExtra(EXTRA_EXCHANGE_ID, exchangeId);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_MESSAGE, message);
        i.putExtra(EXTRA_THEME, themeResId);
        i.putExtra(EXTRA_INITIAL_FACE_ATTEMPT, initialFaceAttempt);
        i.putExtra(EXTRA_LOG_PREFIX, logPrefixForCancel);
        activity.startActivity(i);
    }

    /**
     * Face login: single capture with the same camera UI as enrollment (no multi-step enroll loop).
     *
     * @param exchangeId exchange id after {@code push_acknowledge/face} and {@code allow/face} (authenticate exchange)
     */
    public static void startForLogin(
            @NonNull FragmentActivity activity,
            @NonNull String exchangeId,
            @NonNull String title,
            @NonNull String message,
            int themeResId,
            int initialFaceAttempt,
            @NonNull EventResult<AuthenticateResponse> callback,
            @NonNull String logPrefixForCancel) {
        FaceLoginResultBridge.setPending(callback);
        Intent i = new Intent(activity, FaceEnrollmentWizardActivity.class);
        i.putExtra(EXTRA_MODE, MODE_LOGIN_SINGLE);
        i.putExtra(EXTRA_EXCHANGE_ID, exchangeId);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_MESSAGE, message);
        i.putExtra(EXTRA_THEME, themeResId);
        i.putExtra(EXTRA_INITIAL_FACE_ATTEMPT, initialFaceAttempt);
        i.putExtra(EXTRA_LOG_PREFIX, logPrefixForCancel);
        activity.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        int theme = getIntent().getIntExtra(EXTRA_THEME, 0);
        if (theme != 0) {
            setTheme(theme);
        }
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mode = intent.getIntExtra(EXTRA_MODE, MODE_ENROLL);
        loginMode = (mode == MODE_LOGIN_SINGLE);
        maxCaptureSteps = loginMode ? 1 : 3;

        currentExchangeId = intent.getStringExtra(EXTRA_EXCHANGE_ID);
        if (currentExchangeId == null || currentExchangeId.isEmpty()) {
            WebAuthError err = WebAuthError.getShared(getApplicationContext()).propertyMissingException(
                    "exchange_id missing", VerificationConstants.ERROR_LOGGING_PREFIX + "FaceEnrollmentWizardActivity");
            if (loginMode) {
                finishWithLoginFailure(err);
            } else {
                finishWithFailure(err);
            }
            return;
        }
        initialFaceAttempt = intent.getIntExtra(EXTRA_INITIAL_FACE_ATTEMPT, 0);
        introMessage = intent.getStringExtra(EXTRA_MESSAGE);
        if (introMessage == null) {
            introMessage = "";
        }
        logPrefix = intent.getStringExtra(EXTRA_LOG_PREFIX);
        if (logPrefix == null) {
            logPrefix = "FaceEnrollmentWizardActivity";
        }

        if (savedInstanceState != null) {
            currentExchangeId = savedInstanceState.getString("saved_exchange", currentExchangeId);
            currentStepIndex = savedInstanceState.getInt("saved_step", 0);
            mode = savedInstanceState.getInt("saved_mode", mode);
            loginMode = (mode == MODE_LOGIN_SINGLE);
            maxCaptureSteps = loginMode ? 1 : 3;
        } else {
            currentStepIndex = 0;
        }

        setContentView(R.layout.cidaasverification_activity_face_enrollment_wizard);

        toolbar = findViewById(R.id.cidaasverification_face_wizard_toolbar);
        stepLabel = findViewById(R.id.cidaasverification_face_wizard_step);
        messageView = findViewById(R.id.cidaasverification_face_wizard_message);
        previewView = findViewById(R.id.cidaasverification_face_wizard_preview);
        captureButton = findViewById(R.id.cidaasverification_face_wizard_capture);
        progressBar = findViewById(R.id.cidaasverification_face_wizard_progress);
        stepDots[0] = findViewById(R.id.cidaasverification_face_wizard_dot0);
        stepDots[1] = findViewById(R.id.cidaasverification_face_wizard_dot1);
        stepDots[2] = findViewById(R.id.cidaasverification_face_wizard_dot2);

        View dotsRow = findViewById(R.id.cidaasverification_face_wizard_dots);
        if (loginMode && dotsRow != null) {
            dotsRow.setVisibility(View.GONE);
        }

        toolbar.setTitle(intent.getStringExtra(EXTRA_TITLE));
        toolbar.setNavigationOnClickListener(v -> handleNavigateUp());

        if (!introMessage.isEmpty()) {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(introMessage);
        } else {
            messageView.setVisibility(View.GONE);
        }

        captureButton.setOnClickListener(v -> onCaptureClicked());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleNavigateUp();
            }
        });

        updateStepUi();
        ensureCameraPermissionAndBind();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("saved_exchange", currentExchangeId);
        outState.putInt("saved_step", currentStepIndex);
        outState.putInt("saved_mode", mode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onDestroy() {
        if (!terminalOutcome.get()) {
            if (loginMode) {
                EventResult<AuthenticateResponse> cbLogin = FaceLoginResultBridge.consumePending();
                if (cbLogin != null) {
                    cbLogin.failure(WebAuthError.getShared(getApplicationContext()).customException(
                            WebAuthErrorCode.USER_CANCELLED_LOGIN,
                            "Face login cancelled",
                            VerificationConstants.ERROR_LOGGING_PREFIX + logPrefix));
                }
            } else {
                EventResult<EnrollResponse> cb = FaceEnrollmentResultBridge.consumePending();
                if (cb != null) {
                    cb.failure(WebAuthError.getShared(getApplicationContext()).customException(
                            WebAuthErrorCode.USER_CANCELLED_LOGIN,
                            "Face enrollment cancelled",
                            VerificationConstants.ERROR_LOGGING_PREFIX + logPrefix));
                }
            }
        }
        super.onDestroy();
    }

    private void handleNavigateUp() {
        if (!terminalOutcome.compareAndSet(false, true)) {
            finish();
            return;
        }
        if (loginMode) {
            EventResult<AuthenticateResponse> cbLogin = FaceLoginResultBridge.consumePending();
            if (cbLogin != null) {
                cbLogin.failure(WebAuthError.getShared(getApplicationContext()).customException(
                        WebAuthErrorCode.USER_CANCELLED_LOGIN,
                        "Face login cancelled",
                        VerificationConstants.ERROR_LOGGING_PREFIX + logPrefix));
            }
        } else {
            EventResult<EnrollResponse> cb = FaceEnrollmentResultBridge.consumePending();
            if (cb != null) {
                cb.failure(WebAuthError.getShared(getApplicationContext()).customException(
                        WebAuthErrorCode.USER_CANCELLED_LOGIN,
                        "Face enrollment cancelled",
                        VerificationConstants.ERROR_LOGGING_PREFIX + logPrefix));
            }
        }
        finish();
    }

    private void ensureCameraPermissionAndBind() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bindCameraUseCases(null);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindCameraUseCases(null);
            } else {
                Toast.makeText(this, R.string.cidaasverification_face_camera_denied, Toast.LENGTH_LONG).show();
                WebAuthError err = WebAuthError.getShared(getApplicationContext()).customException(
                        loginMode ? WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE : WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                        "Camera permission denied",
                        VerificationConstants.ERROR_LOGGING_PREFIX + logPrefix);
                if (loginMode) {
                    finishWithLoginFailure(err);
                } else {
                    finishWithFailure(err);
                }
            }
        }
    }

    /** Stops preview and capture while the enroll request is in flight. */
    private void pauseCameraForUpload() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        imageCapture = null;
    }

    private void bindCameraUseCases(@Nullable Runnable onCameraReady) {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                if (cameraProvider == null) {
                    if (onCameraReady != null) {
                        onCameraReady.run();
                    }
                    return;
                }
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                try {
                    cameraProvider.bindToLifecycle(
                            this,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageCapture);
                } catch (Exception e) {
                    Toast.makeText(FaceEnrollmentWizardActivity.this, R.string.cidaasverification_face_wizard_camera_bind_error,
                            Toast.LENGTH_LONG).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(FaceEnrollmentWizardActivity.this, R.string.cidaasverification_face_wizard_camera_bind_error,
                        Toast.LENGTH_LONG).show();
            } finally {
                if (onCameraReady != null) {
                    onCameraReady.run();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void onCaptureClicked() {
        if (enrolling.get()) {
            return;
        }
        if (imageCapture == null) {
            Toast.makeText(this, R.string.cidaasverification_face_wizard_camera_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        File out = new File(getCacheDir(), "cidaasverification_face_wizard_" + currentStepIndex + "_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions opts = new ImageCapture.OutputFileOptions.Builder(out).build();
        enrolling.set(true);
        progressBar.setVisibility(View.VISIBLE);
        captureButton.setEnabled(false);

        Executor executor = ContextCompat.getMainExecutor(this);
        imageCapture.takePicture(opts, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> {
                    pauseCameraForUpload();
                    if (loginMode) {
                        submitAuthenticationFile(out);
                    } else {
                        submitEnrollmentFile(out);
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> {
                    enrolling.set(false);
                    progressBar.setVisibility(View.GONE);
                    captureButton.setEnabled(true);
                    Toast.makeText(FaceEnrollmentWizardActivity.this, R.string.cidaasverification_face_capture_failed,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void submitEnrollmentFile(@NonNull File file) {
        int faceAttempt = initialFaceAttempt + currentStepIndex;
        EnrollEntity entity = new EnrollEntity(currentExchangeId, AuthenticationType.FACE, file, faceAttempt);
        EnrollController.getShared(getApplicationContext()).enrollVerification(entity, new EventResult<EnrollResponse>() {
            @Override
            public void success(EnrollResponse enrollResponse) {
                String nextEx = FaceEnrollInterpreter.nextExchangeIdOrNull(enrollResponse, currentExchangeId);
                if (nextEx != null) {
                    currentExchangeId = nextEx;
                }

                if (FaceEnrollInterpreter.isEnrollmentComplete(enrollResponse)) {
                    enrolling.set(false);
                    progressBar.setVisibility(View.GONE);
                    captureButton.setEnabled(false);
                    completeWithSuccessAndFinish(enrollResponse);
                    return;
                }
                if (FaceEnrollInterpreter.shouldContinueFaceWizard(enrollResponse)) {
                    if (currentStepIndex < maxCaptureSteps - 1) {
                        currentStepIndex++;
                        updateStepUi();
                        String hint = FaceEnrollInterpreter.userVisibleHint(enrollResponse);
                        if (hint != null && !hint.isEmpty()) {
                            Toast.makeText(FaceEnrollmentWizardActivity.this, hint, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FaceEnrollmentWizardActivity.this,
                                    R.string.cidaasverification_face_wizard_need_more, Toast.LENGTH_SHORT).show();
                        }
                        bindCameraUseCases(() -> {
                            enrolling.set(false);
                            progressBar.setVisibility(View.GONE);
                            captureButton.setEnabled(true);
                        });
                        return;
                    }
                    enrolling.set(false);
                    progressBar.setVisibility(View.GONE);
                    finishWithFailure(WebAuthError.getShared(getApplicationContext()).customException(
                            WebAuthErrorCode.ENROLL_VERIFICATION_FAILURE,
                            getString(R.string.cidaasverification_face_wizard_max_steps),
                            VerificationConstants.ERROR_LOGGING_PREFIX + logPrefix));
                    return;
                }

                String hint = FaceEnrollInterpreter.userVisibleHint(enrollResponse);
                Toast.makeText(
                        FaceEnrollmentWizardActivity.this,
                        hint != null && !hint.isEmpty() ? hint : getString(R.string.cidaasverification_face_wizard_unknown_response),
                        Toast.LENGTH_LONG).show();
                bindCameraUseCases(() -> {
                    enrolling.set(false);
                    progressBar.setVisibility(View.GONE);
                    captureButton.setEnabled(true);
                });
            }

            @Override
            public void failure(WebAuthError error) {
                enrolling.set(false);
                progressBar.setVisibility(View.GONE);
                captureButton.setEnabled(true);
                finishWithFailure(error);
            }
        });
    }

    private void submitAuthenticationFile(@NonNull File file) {
        int faceAttempt = initialFaceAttempt + currentStepIndex;
        AuthenticateEntity entity =
                new AuthenticateEntity(currentExchangeId, AuthenticationType.FACE, file, faceAttempt);
        PasswordlessLoginController.getShared(getApplicationContext()).authenticateVerificationOnly(
                entity,
                new EventResult<AuthenticateResponse>() {
                    @Override
                    public void success(AuthenticateResponse authenticateResponse) {
                        enrolling.set(false);
                        progressBar.setVisibility(View.GONE);
                        captureButton.setEnabled(false);
                        completeLoginSuccessAndFinish(authenticateResponse);
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        enrolling.set(false);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(FaceEnrollmentWizardActivity.this, R.string.cidaasverification_face_wizard_unknown_response,
                                Toast.LENGTH_LONG).show();
                        bindCameraUseCases(() -> captureButton.setEnabled(true));
                    }
                });
    }

    private void completeWithSuccessAndFinish(@NonNull EnrollResponse body) {
        if (!terminalOutcome.compareAndSet(false, true)) {
            finish();
            return;
        }
        EventResult<EnrollResponse> cb = FaceEnrollmentResultBridge.consumePending();
        if (cb != null) {
            cb.success(body);
        }
        finish();
    }

    private void completeLoginSuccessAndFinish(@NonNull AuthenticateResponse body) {
        if (!terminalOutcome.compareAndSet(false, true)) {
            finish();
            return;
        }
        EventResult<AuthenticateResponse> cb = FaceLoginResultBridge.consumePending();
        if (cb != null) {
            cb.success(body);
        }
        finish();
    }

    private void finishWithFailure(@NonNull WebAuthError error) {
        if (!terminalOutcome.compareAndSet(false, true)) {
            finish();
            return;
        }
        EventResult<EnrollResponse> cb = FaceEnrollmentResultBridge.consumePending();
        if (cb != null) {
            cb.failure(error);
        }
        finish();
    }

    private void finishWithLoginFailure(@NonNull WebAuthError error) {
        if (!terminalOutcome.compareAndSet(false, true)) {
            finish();
            return;
        }
        EventResult<AuthenticateResponse> cb = FaceLoginResultBridge.consumePending();
        if (cb != null) {
            cb.failure(error);
        }
        finish();
    }

    private void updateStepUi() {
        if (loginMode) {
            stepLabel.setText(getString(R.string.cidaasverification_face_login_step_title));
        } else {
            stepLabel.setText(String.format(Locale.US, getString(R.string.cidaasverification_face_wizard_step_format),
                    currentStepIndex + 1, maxCaptureSteps));
        }
        int primary = MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorPrimary,
                Color.parseColor("#1976D2"));
        int inactive = Color.argb(120, 0, 0, 0);
        for (int i = 0; i < stepDots.length; i++) {
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(i <= currentStepIndex ? primary : inactive);
            stepDots[i].setBackground(d);
        }
    }
}
