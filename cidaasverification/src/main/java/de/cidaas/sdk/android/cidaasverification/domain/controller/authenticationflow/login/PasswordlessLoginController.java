package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

import android.content.Context;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest.LoginRequest;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.initiate.InitiateResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinue;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.authenticate.AuthenticateController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.initiate.InitiateController;
import de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.verificationcontinue.VerificationContinueController;
import de.cidaas.sdk.android.cidaasverification.domain.helper.totpgenerator.GoogleAuthenticator;
import de.cidaas.sdk.android.entities.LoginCredentialsResponseEntity;
import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.UsageType;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;

public class PasswordlessLoginController {
    // Local Variables
    private Context context;

    public static PasswordlessLoginController shared;

    public PasswordlessLoginController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static PasswordlessLoginController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new PasswordlessLoginController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas)
                    .addFailureLog("PasswordlessLoginController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }

    // --------------------------------------------Login--------------------------------------------------------------
    public void loginVerification(final LoginRequest loginRequest, final String verificationType,
            final EventResult<LoginCredentialsResponseEntity> loginCredentialsResult) {
        initiateLogin(loginRequest, verificationType, loginCredentialsResult);
    }

    /**
     * @param loginRequest
     * @param verificationType
     * @param loginCredentialsResult 1.Initiate the authentication Flow
     *                               2.Pass the value to Handle authentication
     *                               entity based on Type
     *                               3.Handle Error and Exception
     */

    // --------------------------------------------Initiate
    // Login--------------------------------------------------------------
    private void initiateLogin(final LoginRequest loginRequest, final String verificationType,
            final EventResult<LoginCredentialsResponseEntity> loginCredentialsResult) {
        String methodName = "PasswordlessLoginController:-initiateLogin()";
        try {

            if (loginRequest.getRequestId() != null && !loginRequest.getRequestId().equals("")) {
                final String requestId = loginRequest.getRequestId();
                InitiateEntity initiateEntity = new InitiateEntity(loginRequest.getIdentifier(), requestId,
                        loginRequest.getUsageType(),
                        verificationType);

                initiate(initiateEntity, new EventResult<InitiateResponse>() {
                    @Override
                    public void success(InitiateResponse initiateResult) {
                        // handle Authenticate entity and call authenticate
                        handleTypesForAuthentication(initiateResult, loginRequest, requestId, verificationType,
                                loginCredentialsResult);
                    }

                    @Override
                    public void failure(WebAuthError error) {
                        loginCredentialsResult.failure(error);
                    }
                });
            } else {
                loginCredentialsResult.failure(WebAuthError.getShared(context)
                        .propertyMissingException("requestId must not be null", methodName));
            }

        } catch (Exception e) {
            loginCredentialsResult.failure(WebAuthError.getShared(context).methodException(methodName,
                    WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * @param initiateResult
     * @param loginRequest
     * @param requestId
     * @param verificationType
     * @param loginCredentialsResult 1.Create authenticationEntity base on
     *                               Verification Type
     *                               2.Pass the value to trigger authentication call
     *                               authenticateVerification
     *                               3.Handle Error and Exception
     */

    public void handleTypesForAuthentication(InitiateResponse initiateResult, final LoginRequest loginRequest,
            String requestId,
            final String verificationType, final EventResult<LoginCredentialsResponseEntity> loginCredentialsResult) {
        String methodName = "PasswordlessLoginController:handleTypesForAuthentication()";
        try {
            AuthenticateEntity authenticateEntity;

            switch (verificationType) {
                case AuthenticationType.FACE:
                    authenticateEntity = new AuthenticateEntity(
                            initiateResult.getData().getExchange_id().getExchange_id(), verificationType,
                            loginRequest.getFileToSend(), loginRequest.getAttempt());
                    break;

                case AuthenticationType.VOICE:
                    authenticateEntity = new AuthenticateEntity(
                            initiateResult.getData().getExchange_id().getExchange_id(), verificationType,
                            loginRequest.getFileToSend(), loginRequest.getAttempt());
                    break;

                case AuthenticationType.FINGERPRINT:
                    if (loginRequest.getFingerPrintEntity() == null) {
                        loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                                "fingerPrintEntity must not be null for fingerprint verification", methodName));
                        return;
                    }
                    final String fpInitExchange = initiateResult.getData().getExchange_id().getExchange_id();
                    AuthenticatePushAcknowledgeAllowHelper.run(context, verificationType, fpInitExchange,
                            new EventResult<String>() {
                                @Override
                                public void success(String finalExchangeId) {
                                    AuthenticateEntity authenticateEntity = new AuthenticateEntity(
                                            finalExchangeId, verificationType, loginRequest.getFingerPrintEntity());
                                    authenticateVerification(authenticateEntity, verificationType, requestId, loginRequest,
                                            loginCredentialsResult);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    loginCredentialsResult.failure(error);
                                }
                            });
                    return;

                case AuthenticationType.SMARTPUSH:
                    authenticateEntity = new AuthenticateEntity(
                            initiateResult.getData().getExchange_id().getExchange_id(), loginRequest.getPass_code(),
                            verificationType);
                    break;

                case AuthenticationType.SMS:
                case AuthenticationType.EMAIL:
                case AuthenticationType.IVR:
                case AuthenticationType.CHAT:
                    if (loginRequest.getPass_code() == null || loginRequest.getPass_code().equals("")) {
                        loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                                "pass_code must not be null or empty for OTP verification", methodName));
                        return;
                    }
                    authenticateEntity = new AuthenticateEntity(
                            initiateResult.getData().getExchange_id().getExchange_id(),
                            loginRequest.getPass_code(), verificationType);
                    break;

                case AuthenticationType.PATTERN:
                    if (loginRequest.getPass_code() == null || loginRequest.getPass_code().equals("")) {
                        loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                                "pass_code must not be null or empty for pattern verification", methodName));
                        return;
                    }
                    final String patternInitExchange = initiateResult.getData().getExchange_id().getExchange_id();
                    PatternLoginController.getShared(context).runPushAcknowledgeAllowForPattern(
                            patternInitExchange,
                            new EventResult<String>() {
                                @Override
                                public void success(String finalExchangeId) {
                                    AuthenticateEntity authenticateEntity = new AuthenticateEntity(
                                            finalExchangeId, loginRequest.getPass_code(), verificationType);
                                    authenticateVerification(authenticateEntity, verificationType, requestId, loginRequest,
                                            loginCredentialsResult);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    loginCredentialsResult.failure(error);
                                }
                            });
                    return;

                case AuthenticationType.TOTP:
                    String totpCode;
                    if (loginRequest.getPass_code() != null && !loginRequest.getPass_code().equals("")) {
                        totpCode = loginRequest.getPass_code();
                    } else {
                        String secret = DBHelper.getShared().getSecret(loginRequest.getIdentifier());
                        if (secret == null || secret.equals("")) {
                            loginCredentialsResult.failure(WebAuthError.getShared(context).invalidPropertiesException(
                                    "TOTP is not configured for this user: invalid or empty secret", methodName));
                            return;
                        }
                        totpCode = GoogleAuthenticator.getTOTPCode(secret);
                    }
                    authenticateEntity = new AuthenticateEntity(
                            initiateResult.getData().getExchange_id().getExchange_id(),
                            totpCode, verificationType);
                    break;

                default:
                    loginCredentialsResult.failure(
                            WebAuthError.getShared(context).invalidPropertiesException("Invalid Verification Type:- " +
                                    verificationType, methodName));
                    return;
            }

            authenticateVerification(authenticateEntity, verificationType, requestId, loginRequest,
                    loginCredentialsResult);
        } catch (Exception e) {
            loginCredentialsResult.failure(WebAuthError.getShared(context).methodException(methodName,
                    WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * @param authenticateEntity
     * @param verificationType
     * @param requestId
     * @param loginRequest
     * @param loginCredentialsResult 1.Call authentication
     *                               2.After Successful authentication call resume
     *                               login
     *                               3.Handle Error and Exception
     */

    public void authenticateVerification(final AuthenticateEntity authenticateEntity, final String verificationType,
            final String requestId,
            final LoginRequest loginRequest, final EventResult<LoginCredentialsResponseEntity> loginCredentialsResult) {
        final String methodName = "PasswordlessLoginController:authenticateVerification()";
        try {
            AuthenticateController.getShared(context).authenticateVerification(authenticateEntity,
                    new EventResult<AuthenticateResponse>() {
                        @Override
                        public void success(AuthenticateResponse result) {
                            VerificationContinue verificationContinueEntity;
                            if (loginRequest.getUsageType().equals(UsageType.MFA)) {
                                verificationContinueEntity = VerificationContinue.getVerificationContinueEntity(
                                        requestId, result.getData().getSub(), loginRequest.getTrackId(),
                                        result.getData().getStatus_id(), verificationType);
                            } else if (loginRequest.getUsageType().equals(UsageType.PASSWORDLESS)) {
                                verificationContinueEntity = VerificationContinue
                                        .getVerificationContinuePasswordlessEntity(requestId,
                                                result.getData().getSub(), result.getData().getStatus_id(),
                                                verificationType);
                            } else {
                                loginCredentialsResult.failure(WebAuthError.getShared(context)
                                        .invalidPropertiesException("Invalid UsageType:- " +
                                                loginRequest.getUsageType(), methodName));
                                return;
                            }

                            verificationContinue(verificationContinueEntity, loginCredentialsResult);
                        }

                        @Override
                        public void failure(WebAuthError error) {
                            loginCredentialsResult.failure(error);
                        }
                    });
        } catch (Exception e) {
            loginCredentialsResult.failure(WebAuthError.getShared(context).methodException(methodName,
                    WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * Login step: v2 authenticate only (no login continue / token exchange). Used
     * by OTP login {@code verify} split flow.
     */
    public void authenticateVerificationOnly(
            final AuthenticateEntity authenticateEntity,
            final EventResult<AuthenticateResponse> authenticateResult) {
        final String methodName = "PasswordlessLoginController:authenticateVerificationOnly()";
        try {
            AuthenticateController.getShared(context).authenticateVerification(authenticateEntity, authenticateResult);
        } catch (Exception e) {
            authenticateResult.failure(WebAuthError.getShared(context).methodException(methodName,
                    WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * After successful OTP {@linkplain #authenticateVerificationOnly authenticate},
     * POST {@code /login-srv/verification/login},
     * resolve {@code code}, exchange for tokens.
     */
    public void loginOtpContinueAfterAuthenticate(
            final String requestId,
            final LoginRequest loginRequest,
            final String verificationType,
            final AuthenticateResponse authenticateResponse,
            final EventResult<LoginCredentialsResponseEntity> loginCredentialsResult) {
        final String methodName = "PasswordlessLoginController:loginOtpContinueAfterAuthenticate()";
        try {
            if (requestId == null || requestId.isEmpty()) {
                loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "requestId must not be null or empty", methodName));
                return;
            }
            if (loginRequest == null) {
                loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "loginRequest must not be null", methodName));
                return;
            }
            if (authenticateResponse == null || authenticateResponse.getData() == null) {
                loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "authenticateResponse must not be null", methodName));
                return;
            }
            String sub = authenticateResponse.getData().getSub();
            String statusId = authenticateResponse.getData().getStatus_id();
            if (sub == null || sub.isEmpty() || statusId == null || statusId.isEmpty()) {
                loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                        "Authenticate response missing sub or status_id", methodName));
                return;
            }
            VerificationContinue verificationContinueEntity;
            if (UsageType.MFA.equals(loginRequest.getUsageType())) {
                if (loginRequest.getTrackId() == null || loginRequest.getTrackId().isEmpty()) {
                    loginCredentialsResult.failure(WebAuthError.getShared(context).propertyMissingException(
                            "trackId must not be null or empty for MFA login continue", methodName));
                    return;
                }
                verificationContinueEntity = VerificationContinue.getVerificationContinueEntity(
                        requestId, sub, loginRequest.getTrackId(), statusId, verificationType);
            } else if (UsageType.PASSWORDLESS.equals(loginRequest.getUsageType())) {
                verificationContinueEntity = VerificationContinue.getVerificationContinuePasswordlessEntity(
                        requestId, sub, statusId, verificationType);
            } else {
                loginCredentialsResult.failure(WebAuthError.getShared(context).invalidPropertiesException(
                        "Invalid UsageType:- " + loginRequest.getUsageType(), methodName));
                return;
            }
            verificationContinueEntity.setUseVerificationLoginPath(true);
            verificationContinue(verificationContinueEntity, loginCredentialsResult);
        } catch (Exception e) {
            loginCredentialsResult.failure(WebAuthError.getShared(context).methodException(methodName,
                    WebAuthErrorCode.PASSWORDLESS_LOGIN_FAILURE,
                    e.getMessage()));
        }
    }

    /**
     * @param verificationContinueEntity
     * @param loginCredentialsResponseEntityResult 1.Call Resume call to get Access
     *                                             Token
     */
    private void verificationContinue(VerificationContinue verificationContinueEntity,
            EventResult<LoginCredentialsResponseEntity> loginCredentialsResponseEntityResult) {
        VerificationContinueController.getShared(context).verificationContinue(verificationContinueEntity,
                loginCredentialsResponseEntityResult);
    }

    /**
     * @param initiateEntity-Entity  to Initiate call
     * @param initiateResponseResult 1. Call Initiate to start the authentication
     *                               Flow
     */
    // -------------------------------------------------------INITIATE
    // CALL--------------------------------------------------------------
    private void initiate(InitiateEntity initiateEntity, EventResult<InitiateResponse> initiateResponseResult) {
        InitiateController.getShared(context).initiateVerification(initiateEntity, initiateResponseResult);
    }

}
