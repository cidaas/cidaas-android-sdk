package de.cidaas.sdk.android.cidaasverification.view;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentActivity;

import java.util.Dictionary;

import de.cidaas.sdk.android.cidaasverification.R;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryResponseNew;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.UserAuthenticatedHistoryDataEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.UserAuthenticatedHistoryResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.delete.DeleteEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.delete.DeleteResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.deviceslist.DevicesListEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enduser.configurerequest.ConfigurationRequest;
import de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushacknowledge.PushAcknowledgeEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushacknowledge.PushAcknowledgeResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushallow.PushAllowEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushallow.PushAllowResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushreject.PushRejectEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.push.pushreject.PushRejectResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DeviceListResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DeviceMfaDataEntitiy;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DevicesMfaResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.ScannedResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.SetUpCancelEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.SetUpCancelResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.configuredmfalist.ConfiguredMFAList;
import de.cidaas.sdk.android.cidaasverification.data.entity.settings.pendingnotification.PendingNotificationResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticatehistory.AuthenticatedHistoryController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.authenticate.AuthenticateController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.initiate.InitiateController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.FaceLoginController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.FingerprintLoginController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.PatternLoginController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.PushLoginController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login.PasswordlessLoginController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.push.pushacknowledge.PushAcknowledgeController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.push.pushallow.PushAllowController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.push.pushreject.PushRejectController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.configuration.ConfigurationController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.FaceEnrollmentController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.FingerprintAttestationEnrollmentController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.PasskeyEnrollmentController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.PatternEnrollmentController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.PushEnrollmentController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll.EnrollController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.scanned.ScannedController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.delete.DeleteController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.pendingnotification.PendingNotificationController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.settings.SettingsController;
import de.cidaas.sdk.android.controller.LoginController;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.helper.general.DBHelper;

public class CidaasVerification {

    private Context context;
    public static CidaasVerification cidaasverificationInstance;

    public static CidaasVerification getInstance(Context YourActivitycontext) {
        if (cidaasverificationInstance == null) {
            cidaasverificationInstance = new CidaasVerification(YourActivitycontext);
        }

        return cidaasverificationInstance;
    }

    public CidaasVerification(Context yourActivityContext) {
        this.context = yourActivityContext;
        CidaasHelper.getShared(yourActivityContext).initialiseObject();

        // 1.Initialise DB ie shared preference
        // 2.Enable log
        // 3.add devi0ce info and FCM token
        // 4.Set Base url

    }

    // ------------------------------------------CALL FOR AUTHENTICATOR AND WEB TO
    // MOBILE FLOW
    // ONLY----------------------------------------------------------------------

    // -------------------------------------------------------SCANNED CALL
    // COMMON--------------------------------------------------------------
    public void scanned(ScannedEntity scannedEntity, EventResult<ScannedResponse> scannedResult) {
        ScannedController.getShared(context).scannedVerification(scannedEntity, scannedResult);
    }

    // -------------------------------------------------------ENROLL CALL
    // COMMON--------------------------------------------------------------

    public void enroll(@NonNull final EnrollEntity enrollEntity,
            final EventResult<EnrollResponse> enrollResponseResult) {
        EnrollController.getShared(context).enrollVerification(enrollEntity, enrollResponseResult);
    }

    // -------------------------------------------------------SMARTPUSH CALL
    // COMMON--------------------------------------------------------------

    // ---------------------Acknowledge-------------------------
    public void pushAcknowledge(PushAcknowledgeEntity pushAcknowledgeEntity,
            EventResult<PushAcknowledgeResponse> pushAcknowledgeResult) {
        PushAcknowledgeController.getShared(context).pushAcknowledgeVerification(pushAcknowledgeEntity,
                pushAcknowledgeResult);
    }

    // ---------------------Allow-------------------------
    public void pushAllow(PushAllowEntity pushAllowEntity, EventResult<PushAllowResponse> pushAllowResponseResult) {
        PushAllowController.getShared(context).pushAllowVerification(pushAllowEntity, pushAllowResponseResult);
    }

    // ---------------------Reject-------------------------
    public void pushReject(PushRejectEntity pushRejectEntity,
            EventResult<PushRejectResponse> pushRejectResponseResult) {
        PushRejectController.getShared(context).pushRejectVerification(pushRejectEntity, pushRejectResponseResult);
    }

    // -------------------------------------------------------AUTHENTICATE CALL
    // COMMON--------------------------------------------------------------
    public void authenticate(AuthenticateEntity authenticateEntity,
            EventResult<AuthenticateResponse> authenticateResponseResult) {
        AuthenticateController.getShared(context).authenticateVerification(authenticateEntity,
                authenticateResponseResult);
    }

    // -------------------------------------------------------PENDING NOTIFICATION
    // LIST CALL --------------------------------------------------------------
    public void getPendingNotificationList(String sub,
            EventResult<PendingNotificationResponse> pendingNotificationResponse) {
        PendingNotificationController.getShared(context).getPendingNotification(sub, pendingNotificationResponse);
    }

    // -------------------------------------------------------SETURL CALL
    // COMMON--------------------------------------------------------------

    // FOR MULTIPLE TENANT AND MULTIPLE USER(ONLY FOR AUTHENTICATOR)
    public void setURL(@NonNull final Dictionary<String, String> loginproperties, EventResult<String> result,
            String methodName) {
        LoginController.getShared(context).setURL(loginproperties, result, methodName);
    }

    // ------------------------------------------CALL FOR BOTH SDK AND
    // AUTHENTICATOR----------------------------------------------------------------------

    // -------------------------------------------------------DELETE CALL
    // COMMON--------------------------------------------------------------
    // warning IF TOTP IS DELETED IT MUST BE DELETED FROM BY WEBPAGE ONLY
    public void delete(DeleteEntity deleteEntity, EventResult<DeleteResponse> deleteResponseResult) {
        DeleteController.getShared(context).deleteVerification(deleteEntity, deleteResponseResult);
    }

    // ---------------------DELETE ALL CALL ------------------------------
    public void deleteAll(String baseURL, String clientId, EventResult<DeleteResponse> deleteResponseResult) {
        DeleteController.getShared(context).deleteAllVerification(baseURL, clientId, deleteResponseResult);
    }

    // --------------------------------------------CONFIGURED MFA LIST CALL
    // --------------------------------------------------------------
    public void getConfiguredMFAList(String sub, EventResult<ConfiguredMFAList> configuredMFAListResult) {
        SettingsController.getShared(context).getConfiguredMFAList(sub, configuredMFAListResult);
    }

    // -------------------------------------------------------AUTHENTICATED HISTORY
    // CALL--------------------------------------------------------------
    public void getAuthenticatedHistory(AuthenticatedHistoryEntity authenticatedHistoryEntity,
            EventResult<AuthenticatedHistoryResponse> authenticatedHistoryResult) {
        AuthenticatedHistoryController.getShared(context).getauthenticatedHistoryList(authenticatedHistoryEntity,
                authenticatedHistoryResult);
    }

    public void getAuthenticatedHistoryNew(AuthenticatedHistoryEntity authenticatedHistoryEntity,
            EventResult<AuthenticatedHistoryResponseNew> authenticatedHistoryResult) {
        AuthenticatedHistoryController.getShared(context).getauthenticatedHistoryListNew(authenticatedHistoryEntity,
                authenticatedHistoryResult);
    }

    public void getAuthenticatedHistoryDetail(UserAuthenticatedHistoryDataEntity userAuthenticatedHistoryDataEntity,
            EventResult<UserAuthenticatedHistoryResponse> userAuthenticatedHistoryResponseEventResult) {
        AuthenticatedHistoryController.getShared(context).getauthenticatedHistoryListDetail(
                userAuthenticatedHistoryDataEntity, userAuthenticatedHistoryResponseEventResult);
    }

    // -------------------------------------------------------UPDATE FCMTOKEN
    // CALL--------------------------------------------------------------
    public void updateFCMToken(String FCMToken) {
        SettingsController.getShared(context).updateFCMToken(FCMToken);
    }
    // ------------------------------------------CALL FOR SDK
    // ONLY----------------------------------------------------------------------

    // ------------------------------------------SETUP
    // CALL--------------------------------------------------------------

    // EMAIL
    public void setupEmail(String sub, EventResult<SetupResponse> setupResponseResult) {
        SetupEntity setupEntity = new SetupEntity(sub, AuthenticationType.EMAIL);
        ConfigurationController.getShared(context).setup(setupEntity, setupResponseResult);
    }

    // SMS
    public void setupSMS(String sub, EventResult<SetupResponse> setupResponseResult) {
        SetupEntity setupEntity = new SetupEntity(sub, AuthenticationType.SMS);
        ConfigurationController.getShared(context).setup(setupEntity, setupResponseResult);
    }

    // IVR
    public void setupIVR(String sub, EventResult<SetupResponse> setupResponseResult) {
        SetupEntity setupEntity = new SetupEntity(sub, AuthenticationType.IVR);
        ConfigurationController.getShared(context).setup(setupEntity, setupResponseResult);
    }

    /**
     * OTP enrollment step 1: POST
     * {@code /verification-actions-srv/setup/&lt;channel&gt;/initiation} for
     * {@link AuthenticationType#SMS}, {@link AuthenticationType#EMAIL},
     * {@link AuthenticationType#IVR}, or
     * {@link AuthenticationType#CHAT} — sends the OTP.
     */
    public void enrollOtpInitiate(
            @NonNull String sub,
            @NonNull String verificationType,
            @NonNull EventResult<SetupResponse> setupResponseResult) {
        SetupEntity setupEntity = new SetupEntity(sub, verificationType);
        ConfigurationController.getShared(context).setup(setupEntity, setupResponseResult);
    }

    /**
     * OTP enrollment step 2: POST
     * {@code /verification-actions-srv/setup/&lt;channel&gt;/verification} with
     * {@code pass_code} set to the user-entered OTP.
     */
    public void enrollOtpVerify(
            @NonNull String verificationCode,
            @NonNull String sub,
            @NonNull String exchange_id,
            @NonNull String verificationType,
            @NonNull EventResult<EnrollResponse> enrollResponseResult) {
        EnrollEntity enrollEntity = new EnrollEntity();
        enrollEntity.setExchange_id(exchange_id);
        enrollEntity.setSub(sub);
        enrollEntity.setVerificationType(verificationType);
        enrollEntity.setPass_code(verificationCode);
        enroll(enrollEntity, enrollResponseResult);
    }

    // BackupCode
    public void setupBackupCode(String sub, EventResult<SetupResponse> setupResponseResult) {
        SetupEntity setupEntity = new SetupEntity(sub, AuthenticationType.BACKUPCODE);
        ConfigurationController.getShared(context).setup(setupEntity, setupResponseResult);
    }

    // Enroll Email
    public void enrollEmail(String verificationCode, String sub, String exchange_id,
            final EventResult<EnrollResponse> enrollResponseResult) {
        EnrollEntity enrollEntity = new EnrollEntity();
        enrollEntity.setExchange_id(exchange_id);
        enrollEntity.setSub(sub);
        enrollEntity.setVerificationType(AuthenticationType.EMAIL);
        enrollEntity.setPass_code(verificationCode);

        enroll(enrollEntity, enrollResponseResult);
    }

    // Enroll SMS
    public void enrollSMS(String verificationCode, String sub, String exchange_id,
            final EventResult<EnrollResponse> enrollResponseResult) {
        EnrollEntity enrollEntity = new EnrollEntity();
        enrollEntity.setExchange_id(exchange_id);
        enrollEntity.setSub(sub);
        enrollEntity.setVerificationType(AuthenticationType.SMS);
        enrollEntity.setPass_code(verificationCode);

        enroll(enrollEntity, enrollResponseResult);
    }

    // Enroll IVR
    public void enrollIVR(String verificationCode, String sub, String exchange_id,
            final EventResult<EnrollResponse> enrollResponseResult) {
        EnrollEntity enrollEntity = new EnrollEntity();
        enrollEntity.setExchange_id(exchange_id);
        enrollEntity.setSub(sub);
        enrollEntity.setVerificationType(AuthenticationType.IVR);
        enrollEntity.setPass_code(verificationCode);

        enroll(enrollEntity, enrollResponseResult);
    }

    public void configurePattern(final ConfigurationRequest configurationRequest,
            final EventResult<EnrollResponse> enrollResponseResult) {
        configure(configurationRequest, AuthenticationType.PATTERN, enrollResponseResult);
    }

    public void configureSmartPush(final ConfigurationRequest configurationRequest,
            final EventResult<EnrollResponse> enrollResponseResult) {
        configure(configurationRequest, AuthenticationType.SMARTPUSH, enrollResponseResult);
    }

    public void configureFaceRecognition(final ConfigurationRequest configurationRequest,
            final EventResult<EnrollResponse> enrollResponseResult) {
        configure(configurationRequest, AuthenticationType.FACE, enrollResponseResult);
    }

    public void configureVoiceRecognition(final ConfigurationRequest configurationRequest,
            final EventResult<EnrollResponse> enrollResponseResult) {
        configure(configurationRequest, AuthenticationType.VOICE, enrollResponseResult);
    }

    public void configureTOTP(final ConfigurationRequest configurationRequest,
            final EventResult<EnrollResponse> enrollResponseResult) {
        configure(configurationRequest, AuthenticationType.TOTP, enrollResponseResult);
    }

    public void configureFingerprint(final ConfigurationRequest configurationRequest,
            final EventResult<EnrollResponse> enrollResponseResult) {
        configure(configurationRequest, AuthenticationType.FINGERPRINT, enrollResponseResult);
    }

    /**
     * Fingerprint MFA enrollment using verification v2 setup APIs: initiate → scan
     * → Keystore biometric proof JWT
     * in {@code attestation} on enroll. Prefer
     * {@code cidaas.verifications().enrolment().fingerprint(activity, sub, callback)}
     * from the main SDK module.
     */
    public void enrolFingerprintWithAttestation(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull EventResult<EnrollResponse> callback) {
        FingerprintAttestationEnrollmentController.getShared(context).enrollWithBiometricAttestation(activity, sub,
                callback);
    }

    /**
     * Passkey (FIDO2) MFA enrollment: setup initiation only (no scan) →
     * {@code fido2_entity.server_challenge} via
     * Credential Manager → enroll with WebAuthn {@code registrationResponseJson} as
     * {@code attestation}.
     * Prefer
     * {@code cidaas.verifications().enrolment().passkey(activity, sub, callback)}
     * from the main SDK module.
     */
    public void enrolPasskeyWithCredentialManager(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull EventResult<EnrollResponse> callback) {
        PasskeyEnrollmentController.getShared(context).enrollWithPasskey(activity, sub, callback);
    }

    /**
     * Smart push MFA enrollment: initiate → scan → dialog (custom title/message) →
     * enroll with
     * {@code pass_code} from setup {@code push_selected_number}. Prefer
     * {@code cidaas.verifications().enrolment().push(...)} from the main SDK
     * module.
     *
     * @param acceptButtonText optional label for the confirm button; when null or
     *                         blank, {@code "Accept"} is used
     */
    public void enrolPushWithAcceptDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @NonNull String dialogMessage, @Nullable String acceptButtonText,
            @NonNull EventResult<EnrollResponse> callback) {
        enrolPushWithAcceptDialog(activity, sub, dialogTitle, dialogMessage, acceptButtonText, 0, callback);
    }

    /**
     * Same as
     * {@link #enrolPushWithAcceptDialog(FragmentActivity, String, String, String, String, EventResult)}
     * with an
     * optional {@link androidx.appcompat.app.AlertDialog} theme (e.g. Material3
     * overlay) so the dialog matches your app.
     *
     * @param dialogThemeResId {@code 0} for the default; otherwise a style resource
     *                         such as
     *                         {@code com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog}
     */
    public void enrolPushWithAcceptDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @NonNull String dialogMessage, @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<EnrollResponse> callback) {
        PushEnrollmentController.getShared(context).enrollWithAcceptDialog(
                activity, sub, dialogTitle, dialogMessage, acceptButtonText, dialogThemeResId, callback);
    }

    /**
     * Pattern MFA enrollment: initiate → scan → modal with 9-dot pattern UI →
     * enroll with {@code pass_code} as
     * SHA-256 lowercase hex (UTF-8) of the pattern string (default prefix
     * {@code RED}, e.g. hash of {@code RED[1,2,3,4]}).
     * Prefer {@code cidaas.verifications().enrolment().pattern(...)} from the main
     * SDK module.
     *
     * @param patternCodePrefix optional prefix before hashing (default {@code RED})
     */
    public void enrolPatternWithLockDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage,
            @NonNull EventResult<EnrollResponse> callback) {
        enrolPatternWithLockDialog(activity, sub, dialogTitle, dialogMessage, null, 0, callback);
    }

    public void enrolPatternWithLockDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage, @StyleRes int dialogThemeResId,
            @NonNull EventResult<EnrollResponse> callback) {
        enrolPatternWithLockDialog(activity, sub, dialogTitle, dialogMessage, null, dialogThemeResId, callback);
    }

    public void enrolPatternWithLockDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage, @Nullable String patternCodePrefix,
            @NonNull EventResult<EnrollResponse> callback) {
        enrolPatternWithLockDialog(activity, sub, dialogTitle, dialogMessage, patternCodePrefix, 0, callback);
    }

    public void enrolPatternWithLockDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage, @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<EnrollResponse> callback) {
        PatternEnrollmentController.getShared(context).enrollWithPatternLockDialog(
                activity, sub, dialogTitle, dialogMessage, patternCodePrefix, dialogThemeResId, callback);
    }

    /**
     * Face MFA enrollment: initiate → scan → full-screen camera wizard (up to three
     * captures) → enroll with
     * multipart {@code photo}. Prefer
     * {@code cidaas.verifications().enrolment().face(...)} from the main SDK
     * module.
     *
     * <p>
     * Requires {@code CAMERA} permission at runtime. The host app merges
     * {@code FileProvider} with authority
     * {@code your.application.id.cidaasverification.fileprovider} from this module.
     * </p>
     *
     * @param faceAttempt sent as {@code face_attempt} on enroll (typically
     *                    {@code 0})
     */
    public void enrolFaceWithCameraDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage,
            @NonNull EventResult<EnrollResponse> callback) {
        enrolFaceWithCameraDialog(activity, sub, dialogTitle, dialogMessage, 0, 0, callback);
    }

    public void enrolFaceWithCameraDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage, @StyleRes int dialogThemeResId,
            @NonNull EventResult<EnrollResponse> callback) {
        enrolFaceWithCameraDialog(activity, sub, dialogTitle, dialogMessage, 0, dialogThemeResId, callback);
    }

    public void enrolFaceWithCameraDialog(@NonNull FragmentActivity activity, @NonNull String sub,
            @NonNull String dialogTitle, @Nullable String dialogMessage, int faceAttempt,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<EnrollResponse> callback) {
        FaceEnrollmentController.getShared(context).enrollWithCameraCapture(
                activity, sub, dialogTitle, dialogMessage, faceAttempt, dialogThemeResId, callback);
    }

    // -------------------------------------------------------SCANNED CALL
    // COMMON--------------------------------------------------------------
    private void configure(final ConfigurationRequest configurationRequest, final String verificationType,
            final EventResult<EnrollResponse> enrollResponseResult) {
        ConfigurationController.getShared(context).configureVerification(configurationRequest, verificationType,
                enrollResponseResult);
    }

    public void initiateIVR(LoginRequest loginRequest, EventResult<InitiateResponse> initiateResult) {
        InitiateEntity initiateEntity = new InitiateEntity(loginRequest.getIdentifier(), loginRequest.getRequestId(),
                loginRequest.getUsageType(),
                AuthenticationType.IVR);
        InitiateController.getShared(context).initiateVerification(initiateEntity, initiateResult);
    }

    public void initiateEmail(LoginRequest loginRequest, EventResult<InitiateResponse> initiateResult) {
        InitiateEntity initiateEntity = new InitiateEntity(loginRequest.getIdentifier(), loginRequest.getRequestId(),
                loginRequest.getUsageType(),
                AuthenticationType.EMAIL);
        InitiateController.getShared(context).initiateVerification(initiateEntity, initiateResult);
    }

    public void initiateSMS(LoginRequest loginRequest, EventResult<InitiateResponse> initiateResult) {
        InitiateEntity initiateEntity = new InitiateEntity(loginRequest.getIdentifier(), loginRequest.getRequestId(),
                loginRequest.getUsageType(),
                AuthenticationType.SMS);
        InitiateController.getShared(context).initiateVerification(initiateEntity, initiateResult);
    }

    public void initiateCHAT(LoginRequest loginRequest, EventResult<InitiateResponse> initiateResult) {
        InitiateEntity initiateEntity = new InitiateEntity(loginRequest.getIdentifier(), loginRequest.getRequestId(),
                loginRequest.getUsageType(),
                AuthenticationType.CHAT);
        InitiateController.getShared(context).initiateVerification(initiateEntity, initiateResult);
    }

    /**
     * OTP login step 1: POST
     * {@code /verification-srv/v2/authenticate/initiate/&lt;method&gt;} (e.g.
     * {@code sms},
     * {@code email}). Use {@code LoginRequest} with {@code identifier}, {@code requestId},
     * {@code usageType}; for MFA set
     * {@code trackId}. Optional {@link LoginRequest#setMediumId(String)} is sent as {@code medium_id} on initiate
     * (e.g. pattern login).
     */
    public void loginOtpInitiate(
            @NonNull LoginRequest loginRequest,
            @NonNull String verificationType,
            @NonNull EventResult<InitiateResponse> callback) {
        InitiateEntity initiateEntity = new InitiateEntity(
                loginRequest.getIdentifier(),
                loginRequest.getRequestId(),
                loginRequest.getUsageType(),
                verificationType);
        String mediumId = loginRequest.getMediumId();
        if (mediumId != null && !mediumId.isEmpty()) {
            initiateEntity.setMedium_id(mediumId);
        }
        InitiateController.getShared(context).initiateVerification(initiateEntity, callback);
    }

    /**
     * OTP login step 2: POST
     * {@code /verification-srv/v2/authenticate/authenticate/&lt;method&gt;} with
     * {@code pass_code}
     * only (no login continue). On success, {@code callback} receives
     * {@link AuthenticateResponse}.
     * Call {@link #loginOtpContinueLogin} next to POST
     * {@code /login-srv/verification/login} and obtain tokens.
     */
    public void loginOtpVerify(
            @NonNull String otp,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String verificationType,
            @NonNull EventResult<AuthenticateResponse> callback) {
        AuthenticateEntity authenticateEntity = new AuthenticateEntity(exchangeId, otp, verificationType);
        PasswordlessLoginController.getShared(context).authenticateVerificationOnly(authenticateEntity, callback);
    }

    /**
     * OTP login step 3 (after {@link #loginOtpVerify}): POST
     * {@code /login-srv/verification/login}. Response may be JSON
     * with {@code data.code} or HTTP {@code 302} with {@code code} in
     * {@code Location}; the code is exchanged for tokens.
     * On success, {@code callback} receives {@link LoginCredentialsResponseEntity}
     * with
     * {@link de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity} in
     * {@code getData()}.
     * Prefer {@code cidaas.verifications().login().otp().continueLogin(...)} from
     * the main SDK module.
     */
    public void loginOtpContinueLogin(
            @NonNull LoginRequest loginRequest,
            @NonNull String verificationType,
            @NonNull AuthenticateResponse authenticateResponse,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        PasswordlessLoginController.getShared(context).loginOtpContinueAfterAuthenticate(
                loginRequest.getRequestId(), loginRequest, verificationType, authenticateResponse, callback);
    }

    /**
     * Pattern login step 1: same as OTP initiate with verification type {@code pattern}
     * ({@code /verification-srv/v2/authenticate/initiate/pattern}). Optional {@link LoginRequest#setMediumId(String)}
     * is sent as {@code medium_id} in the initiate body.
     * Prefer {@code cidaas.verifications().login().pattern(loginRequest, callback)} from the main SDK module.
     */
    public void loginPatternInitiate(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<InitiateResponse> callback) {
        loginOtpInitiate(loginRequest, AuthenticationType.PATTERN, callback);
    }

    /**
     * Pattern login step 2 (no UI): {@code push_acknowledge/pattern} → {@code allow/pattern}, then v2
     * {@code authenticate/pattern} with {@code passCodeSha256Hex} as {@code pass_code} (SHA-256 lowercase hex of
     * {@code PREFIX[d1,d2,...]}, same as enrollment). {@code exchangeId} must be the id from
     * {@link #loginPatternInitiate(LoginRequest, EventResult)}.
     */
    public void loginPatternVerifyPassCode(
            @NonNull String passCodeSha256Hex,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        PatternLoginController.getShared(context).authenticatePassCodeAfterPushAcknowledgeAllow(
                exchangeId, passCodeSha256Hex, callback);
    }

    /**
     * Pattern login step 2: {@code push_acknowledge/pattern} → {@code allow/pattern}, then the same 9-dot modal as
     * enrollment; on confirm POSTs {@code authenticate/pattern}.
     */
    public void loginPatternVerifyWithLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginPatternVerifyWithLockDialog(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, null, 0, callback);
    }

    public void loginPatternVerifyWithLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginPatternVerifyWithLockDialog(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, null, dialogThemeResId, callback);
    }

    public void loginPatternVerifyWithLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginPatternVerifyWithLockDialog(
                activity, loginRequest, exchangeId, dialogTitle, dialogMessage, patternCodePrefix, 0, callback);
    }

    /**
     * @param patternCodePrefix optional prefix before hashing (default {@code RED} in formatter when null)
     */
    public void loginPatternVerifyWithLockDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @Nullable String patternCodePrefix,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        PatternLoginController.getShared(context).verifyWithPatternLockDialog(
                activity, exchangeId, dialogTitle, dialogMessage, patternCodePrefix, dialogThemeResId, callback);
    }

    /**
     * Pattern login step 3: POST {@code /login-srv/verification/login} and exchange code for tokens.
     * Prefer {@code cidaas.verifications().login().pattern(loginRequest, callback)} from the main SDK module.
     */
    public void loginPatternContinueLogin(
            @NonNull LoginRequest loginRequest,
            @NonNull AuthenticateResponse authenticateResponse,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        loginOtpContinueLogin(loginRequest, AuthenticationType.PATTERN, authenticateResponse, callback);
    }

    /**
     * Fingerprint login step 1: v2 authenticate initiate for {@code touchid}
     * ({@code /verification-srv/v2/authenticate/initiate/touchid}).
     */
    public void loginFingerprintInitiate(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<InitiateResponse> callback) {
        loginOtpInitiate(loginRequest, AuthenticationType.FINGERPRINT, callback);
    }

    /**
     * Fingerprint login step 2: {@code push_acknowledge/touchid} → {@code allow/touchid}, then biometric proof JWT as
     * {@code attestation} on {@code authenticate/touchid} (authenticate only).
     */
    public void loginFingerprintVerifyWithBiometricAttestation(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        FingerprintLoginController.getShared(context).authenticateWithBiometricAttestationAfterPush(
                activity, exchangeId, callback);
    }

    /**
     * Fingerprint login step 3: POST {@code /login-srv/verification/login} and exchange code for tokens.
     */
    public void loginFingerprintContinueLogin(
            @NonNull LoginRequest loginRequest,
            @NonNull AuthenticateResponse authenticateResponse,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        loginOtpContinueLogin(loginRequest, AuthenticationType.FINGERPRINT, authenticateResponse, callback);
    }

    /**
     * One-shot fingerprint login: initiate → push acknowledge / allow → biometric attestation → login continue.
     */
    public void loginFingerprintOneShot(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        final String methodName = "CidaasVerification.loginFingerprintOneShot()";
        FragmentActivity activity = loginRequest.getFingerprintLoginHostActivity();
        if (activity == null && context instanceof FragmentActivity) {
            activity = (FragmentActivity) context;
        }
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Fingerprint login requires a FragmentActivity: call loginRequest.setFingerprintLoginHostActivity(activity), "
                            + "or initialize Cidaas with a FragmentActivity context.",
                    methodName));
            return;
        }
        final FragmentActivity hostActivity = activity;
        loginFingerprintInitiate(loginRequest, new EventResult<InitiateResponse>() {
            @Override
            public void success(InitiateResponse initiateResponse) {
                try {
                    if (initiateResponse == null || initiateResponse.getData() == null
                            || initiateResponse.getData().getExchange_id() == null) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "Initiate response missing data or exchange_id", methodName));
                        return;
                    }
                    String exchangeId = initiateResponse.getData().getExchange_id().getExchange_id();
                    if (exchangeId == null || exchangeId.isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "exchange_id missing from initiate response", methodName));
                        return;
                    }
                    FingerprintLoginController.getShared(context).authenticateWithBiometricAttestationAfterPush(
                            hostActivity,
                            exchangeId,
                            new EventResult<AuthenticateResponse>() {
                                @Override
                                public void success(AuthenticateResponse result) {
                                    loginFingerprintContinueLogin(loginRequest, result, callback);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    callback.failure(error);
                                }
                            });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            methodName,
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    /**
     * One-shot pattern login: initiate → same pattern lock modal as enrollment → verification login continue.
     * On success, {@code callback} receives {@link LoginCredentialsResponseEntity} with
     * {@link de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity} in {@code getData()}.
     *
     * <p>Provide a UI host via {@link LoginRequest#setPatternLoginHostActivity} when {@code Cidaas} was not created
     * with a {@link FragmentActivity}. Optional: {@link LoginRequest#setPatternLoginDialogTitle},
     * {@link LoginRequest#setPatternLoginDialogMessage}, {@link LoginRequest#setPatternLoginCodePrefix},
     * {@link LoginRequest#setPatternLoginDialogThemeResId}.</p>
     *
     * <p>Prefer {@code cidaas.verifications().login().pattern(loginRequest, callback)} from the main SDK module.</p>
     */
    public void loginPatternOneShot(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        final String methodName = "CidaasVerification.loginPatternOneShot()";
        FragmentActivity activity = loginRequest.getPatternLoginHostActivity();
        if (activity == null && context instanceof FragmentActivity) {
            activity = (FragmentActivity) context;
        }
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Pattern login requires a FragmentActivity: call loginRequest.setPatternLoginHostActivity(activity), "
                            + "or initialize Cidaas with a FragmentActivity context.",
                    methodName));
            return;
        }
        final FragmentActivity hostActivity = activity;
        loginPatternInitiate(loginRequest, new EventResult<InitiateResponse>() {
            @Override
            public void success(InitiateResponse initiateResponse) {
                try {
                    if (initiateResponse == null || initiateResponse.getData() == null
                            || initiateResponse.getData().getExchange_id() == null) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "Initiate response missing data or exchange_id", methodName));
                        return;
                    }
                    String exchangeId = initiateResponse.getData().getExchange_id().getExchange_id();
                    if (exchangeId == null || exchangeId.isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "exchange_id missing from initiate response", methodName));
                        return;
                    }
                    String title = loginRequest.getPatternLoginDialogTitle();
                    if (title == null || title.trim().isEmpty()) {
                        title = context.getString(R.string.cidaasverification_pattern_login_title);
                    }
                    PatternLoginController.getShared(context).verifyWithPatternLockDialog(
                            hostActivity,
                            exchangeId,
                            title,
                            loginRequest.getPatternLoginDialogMessage(),
                            loginRequest.getPatternLoginCodePrefix(),
                            loginRequest.getPatternLoginDialogThemeResId(),
                            new EventResult<AuthenticateResponse>() {
                                @Override
                                public void success(AuthenticateResponse result) {
                                    loginPatternContinueLogin(loginRequest, result, callback);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    callback.failure(error);
                                }
                            });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            methodName,
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    /**
     * Push login step 1: v2 authenticate initiate for {@code push}
     * ({@code /verification-srv/v2/authenticate/initiate/push}). Response data may include
     * {@code push_selected_number} for use as {@code pass_code} on authenticate.
     */
    public void loginPushInitiate(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<InitiateResponse> callback) {
        loginOtpInitiate(loginRequest, AuthenticationType.SMARTPUSH, callback);
    }

    /**
     * Push login step 2: {@code push_acknowledge/push} → {@code allow/push}, then accept-only card modal; on accept
     * POSTs {@code authenticate/push} with {@code passCode} (typically {@code push_selected_number} from initiate).
     */
    public void loginPushVerifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull EventResult<AuthenticateResponse> callback) {
        String title = loginRequest.getPushLoginDialogTitle();
        if (title == null || title.trim().isEmpty()) {
            title = context.getString(R.string.cidaasverification_push_login_title);
        }
        String message = loginRequest.getPushLoginDialogMessage();
        if (message == null || message.trim().isEmpty()) {
            message = context.getString(R.string.cidaasverification_push_login_message);
        }
        loginPushVerifyWithAcceptDialog(
                activity,
                loginRequest,
                exchangeId,
                passCode,
                title,
                message,
                loginRequest.getPushLoginAcceptButtonText(),
                loginRequest.getPushLoginDialogThemeResId(),
                callback);
    }

    public void loginPushVerifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginPushVerifyWithAcceptDialog(
                activity,
                loginRequest,
                exchangeId,
                passCode,
                dialogTitle,
                dialogMessage,
                loginRequest.getPushLoginAcceptButtonText(),
                loginRequest.getPushLoginDialogThemeResId(),
                callback);
    }

    public void loginPushVerifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginPushVerifyWithAcceptDialog(
                activity,
                loginRequest,
                exchangeId,
                passCode,
                dialogTitle,
                dialogMessage,
                loginRequest.getPushLoginAcceptButtonText(),
                dialogThemeResId,
                callback);
    }

    /**
     * @param acceptButtonText when null or blank, {@code Accept} is used in the dialog
     */
    public void loginPushVerifyWithAcceptDialog(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String exchangeId,
            @NonNull String passCode,
            @NonNull String dialogTitle,
            @NonNull String dialogMessage,
            @Nullable String acceptButtonText,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        PushLoginController.getShared(context).verifyWithAcceptDialogAfterPush(
                activity,
                exchangeId,
                passCode,
                dialogTitle,
                dialogMessage,
                acceptButtonText,
                dialogThemeResId,
                callback);
    }

    /**
     * Push login step 3: POST {@code /login-srv/verification/login} and exchange code for tokens.
     * Prefer {@code cidaas.verifications().login().push(loginRequest, callback)} from the main SDK module.
     */
    public void loginPushContinueLogin(
            @NonNull LoginRequest loginRequest,
            @NonNull AuthenticateResponse authenticateResponse,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        loginOtpContinueLogin(loginRequest, AuthenticationType.SMARTPUSH, authenticateResponse, callback);
    }

    /**
     * One-shot push login: initiate → push acknowledge / allow → accept-only modal → login continue to tokens.
     * Uses {@link de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateResponseDataEntity#getPush_selected_number()}
     * from the initiate response as {@code pass_code}; set {@link LoginRequest#setPushLoginHostActivity} when
     * {@code Cidaas} was not created with a {@link FragmentActivity}.
     */
    public void loginPushOneShot(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        final String methodName = "CidaasVerification.loginPushOneShot()";
        FragmentActivity activity = loginRequest.getPushLoginHostActivity();
        if (activity == null && context instanceof FragmentActivity) {
            activity = (FragmentActivity) context;
        }
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Push login requires a FragmentActivity: call loginRequest.setPushLoginHostActivity(activity), "
                            + "or initialize Cidaas with a FragmentActivity context.",
                    methodName));
            return;
        }
        final FragmentActivity hostActivity = activity;
        loginPushInitiate(loginRequest, new EventResult<InitiateResponse>() {
            @Override
            public void success(InitiateResponse initiateResponse) {
                try {
                    if (initiateResponse == null || initiateResponse.getData() == null
                            || initiateResponse.getData().getExchange_id() == null) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "Initiate response missing data or exchange_id", methodName));
                        return;
                    }
                    String exchangeId = initiateResponse.getData().getExchange_id().getExchange_id();
                    if (exchangeId == null || exchangeId.isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "exchange_id missing from initiate response", methodName));
                        return;
                    }
                    String pushNumber = initiateResponse.getData().getPush_selected_number();
                    if (pushNumber == null || pushNumber.trim().isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "push_selected_number missing from initiate response (required for push login)",
                                methodName));
                        return;
                    }
                    String title = loginRequest.getPushLoginDialogTitle();
                    if (title == null || title.trim().isEmpty()) {
                        title = context.getString(R.string.cidaasverification_push_login_title);
                    }
                    String message = loginRequest.getPushLoginDialogMessage();
                    if (message == null || message.trim().isEmpty()) {
                        message = context.getString(R.string.cidaasverification_push_login_message);
                    }
                    PushLoginController.getShared(context).verifyWithAcceptDialogAfterPush(
                            hostActivity,
                            exchangeId,
                            pushNumber.trim(),
                            title,
                            message,
                            loginRequest.getPushLoginAcceptButtonText(),
                            loginRequest.getPushLoginDialogThemeResId(),
                            new EventResult<AuthenticateResponse>() {
                                @Override
                                public void success(AuthenticateResponse result) {
                                    loginPushContinueLogin(loginRequest, result, callback);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    callback.failure(error);
                                }
                            });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            methodName,
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    /**
     * Face login step 1: v2 authenticate initiate for {@code face}
     * ({@code /verification-srv/v2/authenticate/initiate/face}).
     */
    public void loginFaceInitiate(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<InitiateResponse> callback) {
        loginOtpInitiate(loginRequest, AuthenticationType.FACE, callback);
    }

    /**
     * Face login step 2: {@code push_acknowledge/face} → {@code allow/face}, then one camera capture (same UI as
     * enrollment, single step) → {@code authenticate/face}. {@code initiateExchangeId} is the exchange from
     * {@link #loginFaceInitiate(LoginRequest, EventResult)}.
     */
    public void loginFaceVerifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String initiateExchangeId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        String title = loginRequest.getFaceLoginDialogTitle();
        if (title == null || title.trim().isEmpty()) {
            title = context.getString(R.string.cidaasverification_face_login_dialog_title);
        }
        String message = loginRequest.getFaceLoginDialogMessage();
        if (message == null || message.trim().isEmpty()) {
            message = context.getString(R.string.cidaasverification_face_login_dialog_message);
        }
        loginFaceVerifyWithCameraWizard(
                activity,
                loginRequest,
                initiateExchangeId,
                title,
                message,
                loginRequest.getFaceLoginDialogThemeResId(),
                loginRequest.getFaceLoginInitialFaceAttempt(),
                callback);
    }

    public void loginFaceVerifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String initiateExchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginFaceVerifyWithCameraWizard(
                activity,
                loginRequest,
                initiateExchangeId,
                dialogTitle,
                dialogMessage,
                loginRequest.getFaceLoginDialogThemeResId(),
                loginRequest.getFaceLoginInitialFaceAttempt(),
                callback);
    }

    public void loginFaceVerifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String initiateExchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            @NonNull EventResult<AuthenticateResponse> callback) {
        loginFaceVerifyWithCameraWizard(
                activity,
                loginRequest,
                initiateExchangeId,
                dialogTitle,
                dialogMessage,
                dialogThemeResId,
                loginRequest.getFaceLoginInitialFaceAttempt(),
                callback);
    }

    /**
     * @param initialFaceAttempt {@code face_attempt} sent with the captured photo (often {@code 0})
     */
    public void loginFaceVerifyWithCameraWizard(
            @NonNull FragmentActivity activity,
            @NonNull LoginRequest loginRequest,
            @NonNull String initiateExchangeId,
            @NonNull String dialogTitle,
            @Nullable String dialogMessage,
            @StyleRes int dialogThemeResId,
            int initialFaceAttempt,
            @NonNull EventResult<AuthenticateResponse> callback) {
        FaceLoginController.getShared(context).verifyWithSingleFaceCaptureAfterPush(
                activity,
                initiateExchangeId,
                dialogTitle,
                dialogMessage,
                dialogThemeResId,
                initialFaceAttempt,
                callback);
    }

    /**
     * Face login step 3: POST {@code /login-srv/verification/login} and exchange code for tokens.
     * Prefer {@code cidaas.verifications().login().face(loginRequest, callback)} from the main SDK module.
     */
    public void loginFaceContinueLogin(
            @NonNull LoginRequest loginRequest,
            @NonNull AuthenticateResponse authenticateResponse,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        loginOtpContinueLogin(loginRequest, AuthenticationType.FACE, authenticateResponse, callback);
    }

    /**
     * One-shot face login: initiate → push acknowledge / allow → single camera capture → login continue to tokens.
     * Set {@link LoginRequest#setFaceLoginHostActivity} when {@code Cidaas} was not created with a
     * {@link FragmentActivity}. Optional dialog copy: {@link LoginRequest#setFaceLoginDialogTitle},
     * {@link LoginRequest#setFaceLoginDialogMessage}, {@link LoginRequest#setFaceLoginDialogThemeResId},
     * {@link LoginRequest#setFaceLoginInitialFaceAttempt}.
     */
    public void loginFaceOneShot(
            @NonNull LoginRequest loginRequest,
            @NonNull EventResult<LoginCredentialsResponseEntity> callback) {
        final String methodName = "CidaasVerification.loginFaceOneShot()";
        FragmentActivity activity = loginRequest.getFaceLoginHostActivity();
        if (activity == null && context instanceof FragmentActivity) {
            activity = (FragmentActivity) context;
        }
        if (activity == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Face login requires a FragmentActivity: call loginRequest.setFaceLoginHostActivity(activity), "
                            + "or initialize Cidaas with a FragmentActivity context.",
                    methodName));
            return;
        }
        final FragmentActivity hostActivity = activity;
        loginFaceInitiate(loginRequest, new EventResult<InitiateResponse>() {
            @Override
            public void success(InitiateResponse initiateResponse) {
                try {
                    if (initiateResponse == null || initiateResponse.getData() == null
                            || initiateResponse.getData().getExchange_id() == null) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "Initiate response missing data or exchange_id", methodName));
                        return;
                    }
                    String exchangeId = initiateResponse.getData().getExchange_id().getExchange_id();
                    if (exchangeId == null || exchangeId.isEmpty()) {
                        callback.failure(WebAuthError.getShared(context).propertyMissingException(
                                "exchange_id missing from initiate response", methodName));
                        return;
                    }
                    String title = loginRequest.getFaceLoginDialogTitle();
                    if (title == null || title.trim().isEmpty()) {
                        title = context.getString(R.string.cidaasverification_face_login_dialog_title);
                    }
                    String message = loginRequest.getFaceLoginDialogMessage();
                    if (message == null || message.trim().isEmpty()) {
                        message = context.getString(R.string.cidaasverification_face_login_dialog_message);
                    }
                    FaceLoginController.getShared(context).verifyWithSingleFaceCaptureAfterPush(
                            hostActivity,
                            exchangeId,
                            title,
                            message,
                            loginRequest.getFaceLoginDialogThemeResId(),
                            loginRequest.getFaceLoginInitialFaceAttempt(),
                            new EventResult<AuthenticateResponse>() {
                                @Override
                                public void success(AuthenticateResponse result) {
                                    loginFaceContinueLogin(loginRequest, result, callback);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    callback.failure(error);
                                }
                            });
                } catch (Exception e) {
                    callback.failure(WebAuthError.getShared(context).methodException(
                            methodName,
                            WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                            e.getMessage()));
                }
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    // Onlu For Native ... Can we
    public void verifyCode(String code, String exchange_id, String verificationType, String requestId, String usageType,
            EventResult<LoginCredentialsResponseEntity> loginResult) {
        AuthenticateEntity authenticateEntity = new AuthenticateEntity(exchange_id, code, verificationType);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsageType(usageType);
        PasswordlessLoginController.getShared(context).authenticateVerification(authenticateEntity, verificationType,
                requestId, loginRequest, loginResult);
    }

    public void loginWithPattern(final LoginRequest loginRequest,
            final EventResult<LoginCredentialsResponseEntity> authenticateResponseResult) {
        login(loginRequest, AuthenticationType.PATTERN, authenticateResponseResult);
    }

    public void loginWithSmartPush(final LoginRequest loginRequest,
            final EventResult<LoginCredentialsResponseEntity> authenticateResponseResult) {
        login(loginRequest, AuthenticationType.SMARTPUSH, authenticateResponseResult);
    }

    public void loginWithFaceRecognition(final LoginRequest loginRequest,
            final EventResult<LoginCredentialsResponseEntity> authenticateResponseResult) {
        login(loginRequest, AuthenticationType.FACE, authenticateResponseResult);
    }

    public void loginWithVoice(final LoginRequest loginRequest,
            final EventResult<LoginCredentialsResponseEntity> authenticateResponseResult) {
        login(loginRequest, AuthenticationType.VOICE, authenticateResponseResult);
    }

    // To Handle TOTP
    public void loginWithTOTP(final LoginRequest loginRequest,
            final EventResult<LoginCredentialsResponseEntity> authenticateResponseResult) {
        login(loginRequest, AuthenticationType.TOTP, authenticateResponseResult);
    }

    public void loginWithFingerprint(final LoginRequest loginRequest,
            final EventResult<LoginCredentialsResponseEntity> authenticateResponseResult) {
        login(loginRequest, AuthenticationType.FINGERPRINT, authenticateResponseResult);
    }
    // -------------------------------------------------------LOGIN CALL
    // COMMON--------------------------------------------------------------

    private void login(final LoginRequest loginRequest, final String verificationType,
            final EventResult<LoginCredentialsResponseEntity> loginCredentialsResult) {
        PasswordlessLoginController.getShared(context).loginVerification(loginRequest, verificationType,
                loginCredentialsResult);
    }

    // Set FCM Token For Update
    public void setFCMToken(String FCMToken) {
        // Store Device info for Later Purposes
        DBHelper.getShared().setFCMToken(FCMToken);
    }

    public void getDevicesList(DevicesListEntity devicesListEntity,
            EventResult<DeviceListResponse> deviceListResponseEventResult) {
        // AuthenticatedHistoryController.getShared(context).getauthenticatedHistoryListNew(authenticatedHistoryEntity,
        // authenticatedHistoryResult);
        AuthenticatedHistoryController.getShared(context).getDevicesList(devicesListEntity,
                deviceListResponseEventResult);
    }

    public void getDevicesRemove(DeviceMfaDataEntitiy deviceMfaDataEntitiy,
            EventResult<DevicesMfaResponse> devicesMfaResponseEventResult) {
        AuthenticatedHistoryController.getShared(context).getDevicesRemove(deviceMfaDataEntitiy,
                devicesMfaResponseEventResult);

    }

    public void getConfiguredMFAListThirdParty(String baseurl, String sub, String linkeddeviceid, String clientid,
            EventResult<ConfiguredMFAList> configuredMFAListResult) {
        SettingsController.getShared(context).getConfiguredMFAListThirdParty(baseurl, sub, linkeddeviceid, clientid,
                configuredMFAListResult);
    }

    public void setUpCancel(SetUpCancelEntity setUpCancelEntity,
            EventResult<SetUpCancelResponse> setUpCancelResponseResult) {
        ScannedController.getShared(context).setUpCancel(setUpCancelEntity, setUpCancelResponseResult);
    }

}
