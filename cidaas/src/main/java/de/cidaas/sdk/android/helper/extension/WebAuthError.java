package de.cidaas.sdk.android.helper.extension;

import android.content.Context;

import de.cidaas.sdk.android.R;
import de.cidaas.sdk.android.entities.ErrorEntity;
import de.cidaas.sdk.android.helper.enums.HttpStatusCode;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.interfaces.IOAuthExcepiton;
import timber.log.Timber;

/**
 * Created by widasrnarayanan on 16/1/18.
 */

public class WebAuthError extends Error implements IOAuthExcepiton {

    private int errorCode = WebAuthErrorCode.DEFAULT;
    private int statusCode = 400;
    private Context context;
    private String ErrorMessage = "";
    private String DetailedErrorMessage = "";
    private ErrorEntity errorEntity;
    private String jsonMessage = "";

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setDetailedErrorMessage(String detailedErrorMessage) {
        DetailedErrorMessage = detailedErrorMessage;
    }

    public String getJsonMessage() {
        return jsonMessage;
    }

    public void setJsonMessage(String jsonMessage) {
        this.jsonMessage = jsonMessage;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public String getDetailedErrorMessage() {
        return DetailedErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    public ErrorEntity getErrorEntity() {
        return errorEntity;
    }

    public void setErrorEntity(ErrorEntity errorEntity) {
        this.errorEntity = errorEntity;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public Object error;
    private static WebAuthError shared;

    public static WebAuthError getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new WebAuthError(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public WebAuthError(Context contextFromCidaas) {

        context = contextFromCidaas;
    }

    // File NotFound Exception
    @Override
    public WebAuthError fileNotFoundException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.FILE_NOT_FOUND);
        WebAuthError.shared.setStatusCode(HttpStatusCode.NOT_FOUND);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.FILE_NOT_FOUND));

        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(statusCode);
        errorEntity.setError(context.getString(R.string.FILE_NOT_FOUND));

        String loggerMessage = "File not found Error :- " + methodName + CidaasConstants.ERROR_CODE
                + WebAuthError.shared.errorCode + CidaasConstants.ERROR_MESSAGE
                + context.getString(R.string.FILE_NOT_FOUND);
        LogFile.getShared(context).addFailureLog(loggerMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);
        return WebAuthError.shared;
    }

    // NoContentinFileException
    @Override
    public WebAuthError noContentInFileException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.NO_CONTENT_IN_FILE);
        WebAuthError.shared.setStatusCode(HttpStatusCode.NO_CONTENT);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.NO_CONTENT_IN_FILE));

        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.NO_CONTENT_IN_FILE);
        errorEntity.setStatus(HttpStatusCode.NO_CONTENT);
        errorEntity.setError(context.getString(R.string.NO_CONTENT_IN_FILE));

        String loggerMessage = "No Content in Files Exception :- " + methodName + "ErrorCode :- "
                + WebAuthError.shared.errorCode + CidaasConstants.ERROR_MESSAGE +
                context.getString(R.string.NO_CONTENT_IN_FILE);
        LogFile.getShared(context).addFailureLog(loggerMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);
        return WebAuthError.shared;
    }

    // PropertyMissingException
    @Override
    public WebAuthError propertyMissingException(String errorDetails, String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.PROPERTY_MISSING);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.PROPERTY_MISSING));
        WebAuthError.shared.setDetailedErrorMessage(errorDetails);

        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.PROPERTY_MISSING);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.PROPERTY_MISSING));
        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = "Property Missing Error :-" + methodName + "ErrorCode :- "
                + WebAuthError.shared.errorCode + CidaasConstants.ERROR_MESSAGE + errorDetails;
        LogFile.getShared(context).addFailureLog(loggerMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    public WebAuthError cidaasPropertyMissingException(String errorDetails, String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.CIDAAS_PROPERTY_MISSING);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.CIDAAS_PROPERTY_MISSING));
        WebAuthError.shared.setDetailedErrorMessage(errorDetails);

        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.CIDAAS_PROPERTY_MISSING);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.CIDAAS_PROPERTY_MISSING));

        String loggerMessage = "Cidaas Property Missing Error :-" + methodName + "ErrorCode :- "
                + WebAuthError.shared.errorCode + CidaasConstants.ERROR_MESSAGE + errorDetails;
        LogFile.getShared(context).addFailureLog(loggerMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // Access Token Exception
    @Override
    public WebAuthError accessTokenException(String errorDetails, String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
        WebAuthError.shared.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.ACCESS_TOKEN_SERVICE_FAILURE));
        WebAuthError.shared.setDetailedErrorMessage(errorDetails);

        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
        errorEntity.setStatus(HttpStatusCode.INTERNAL_SERVER_ERROR);
        errorEntity.setError(context.getString(R.string.ACCESS_TOKEN_SERVICE_FAILURE));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // Service FailureException
    @Override
    public WebAuthError serviceCallFailureException(int errorCode, String errorMessage, String methodName) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(HttpStatusCode.BAD_REQUEST);
        WebAuthError.shared.setErrorMessage(errorMessage);

        ErrorEntity errorEntityServiceCallFailure = new ErrorEntity();

        if (errorMessage.contains("timeout")) {
            errorEntityServiceCallFailure.setCode(WebAuthErrorCode.TIME_OUT_ERROR);
        } else {
            errorEntityServiceCallFailure.setCode(errorCode);
        }
        errorEntityServiceCallFailure.setStatus(HttpStatusCode.BAD_REQUEST);
        errorEntityServiceCallFailure.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntityServiceCallFailure);

        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + CidaasConstants.ERROR_MESSAGE + errorMessage + CidaasConstants.STATUS_CODE + statusCode;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;

    }

    // Service FailureException

    public WebAuthError serviceCallException(int errorCode, String errorMessage, int statusCode,
            ErrorEntity errorEntity, String errorResponse, String methodName) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(statusCode);
        WebAuthError.shared.setErrorMessage(errorMessage);
        WebAuthError.shared.jsonMessage = errorResponse;

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + CidaasConstants.ERROR_MESSAGE + errorMessage + CidaasConstants.STATUS_CODE + statusCode +
                "ErrorResponse From Server:" + errorResponse;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;

    }

    public WebAuthError loginFailureException(int errorCode, String errorMessage, int statusCode, Object error,
            String methodName) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(statusCode);
        WebAuthError.shared.setErrorMessage(errorMessage);
        WebAuthError.shared.error = error;

        errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(statusCode);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + CidaasConstants.ERROR_MESSAGE + errorMessage + CidaasConstants.STATUS_CODE + statusCode;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;

    }

    // FCM TOKEN FAILURE
    public WebAuthError FCMTokenFailure(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.UPDATE_FCM_TOKEN);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.UPDATE_FCM_TOKEN_FAILURE));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.UPDATE_FCM_TOKEN);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.UPDATE_FCM_TOKEN_FAILURE));
        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + "Error Message - FCM TOKEN FAILURE " + " Empty response" + CidaasConstants.STATUS_CODE + statusCode;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;
    }

    public WebAuthError emptyResponseException(int errorCode, int statusCode, String methodName) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(statusCode);
        WebAuthError.shared.setErrorMessage("Empty response");

        errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(statusCode);
        errorEntity.setError("Empty response");

        WebAuthError.shared.setErrorEntity(errorEntity);
        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + CidaasConstants.ERROR_MESSAGE + " Empty response" + CidaasConstants.STATUS_CODE + statusCode;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;

    }

    public WebAuthError loginWithBrowserFailureException(int errorCode, String errorMessage, String methodName) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(HttpStatusCode.BAD_REQUEST);
        WebAuthError.shared.setErrorMessage(errorMessage);

        errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(HttpStatusCode.BAD_REQUEST);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + CidaasConstants.ERROR_MESSAGE + errorMessage + CidaasConstants.STATUS_CODE + statusCode;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;

    }

    public WebAuthError methodException(String methodName, int errorCode, String errorMessage) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(HttpStatusCode.BAD_REQUEST);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.SERVICE_EXCEPTION));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(HttpStatusCode.BAD_REQUEST);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = methodName + " :- " + CidaasConstants.ERROR_CODE + errorCode + " "
                + CidaasConstants.ERROR_MESSAGE + errorMessage;
        LogFile.getShared(context).addFailureLog(loggerMessage);

        Timber.d(loggerMessage);

        return WebAuthError.shared;

    }

    // Location History Failure Exception
    @Override
    public WebAuthError locationHistoryException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.USER_LOGIN_INFO_SERVICE_FAILURE);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.LOCATION_HISTORY_FAILURE));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.USER_LOGIN_INFO_SERVICE_FAILURE);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.LOCATION_HISTORY_FAILURE));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // LoginUrl MissingException
    @Override
    public WebAuthError loginURLMissingException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.EMPTY_LOGIN_URL);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.EMPTY_LOGIN_URL));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.EMPTY_LOGIN_URL);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.EMPTY_LOGIN_URL));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // RedirectUrl Missing Exception
    @Override
    public WebAuthError redirectURLMissingException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.EMPTY_REDIRECT_URL);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.EMPTY_REDIRECT_URL));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.EMPTY_REDIRECT_URL);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.EMPTY_REDIRECT_URL));

        WebAuthError.shared.setErrorEntity(errorEntity);
        return WebAuthError.shared;
    }

    // Usercancelled Exception
    @Override
    public WebAuthError userCancelledException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.USER_CANCELLED_LOGIN);
        WebAuthError.shared.setStatusCode(HttpStatusCode.CANCEL_REQUEST);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.USER_CANCELLED_LOGIN));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.USER_CANCELLED_LOGIN);
        errorEntity.setStatus(HttpStatusCode.CANCEL_REQUEST);
        errorEntity.setError(context.getString(R.string.USER_CANCELLED_LOGIN));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // CodenotFoundException
    @Override
    public WebAuthError codeNotFoundException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.CODE_NOT_FOUND);
        WebAuthError.shared.setStatusCode(HttpStatusCode.NO_CONTENT);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.CODE_NOT_FOUND));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.CODE_NOT_FOUND);
        errorEntity.setStatus(HttpStatusCode.NO_CONTENT);
        errorEntity.setError(context.getString(R.string.CODE_NOT_FOUND));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // EmptyCallbackException
    @Override
    public WebAuthError emptyCallbackException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.EMPTY_CALLBACK);
        WebAuthError.shared.setStatusCode(HttpStatusCode.BAD_REQUEST);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.EMPTY_CALLBACK));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.EMPTY_CALLBACK);
        errorEntity.setStatus(HttpStatusCode.BAD_REQUEST);
        errorEntity.setError(context.getString(R.string.EMPTY_CALLBACK));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // NouserFoundException
    @Override
    public WebAuthError noUserFoundException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.NO_USER_FOUND);
        WebAuthError.shared.setStatusCode(HttpStatusCode.NOT_FOUND);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.NO_USER_FOUND));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.NO_USER_FOUND);
        errorEntity.setStatus(HttpStatusCode.NOT_FOUND);
        errorEntity.setError(context.getString(R.string.NO_USER_FOUND));

        WebAuthError.shared.setErrorEntity(errorEntity);

        return WebAuthError.shared;
    }

    // Device Failed to Verify
    public WebAuthError deviceVerificationFailureException(String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.DEVICE_VERIFICATION_FAILURE));

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.DEVICE_VERIFICATION_FAILURE);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.DEVICE_VERIFICATION_FAILURE));

        String loggerMessage = "Device Verification Failure Exception:-" + methodName + CidaasConstants.ERROR_CODE
                + WebAuthError.shared.errorCode
                + CidaasConstants.ERROR_MESSAGE + errorEntity.getError();
        LogFile.getShared(context).addFailureLog(loggerMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);
        return WebAuthError.shared;
    }

    public WebAuthError invalidPropertiesException(String errorMessage, String methodName) {
        WebAuthError.shared.setErrorCode(WebAuthErrorCode.INVALID_PROPERTIES);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(context.getString(R.string.INVALID_PROPERTIES));
        WebAuthError.shared.setDetailedErrorMessage(errorMessage);

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.INVALID_PROPERTIES);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(context.getString(R.string.INVALID_PROPERTIES));

        String loggerMessage = "Device Verification Failure Exception:-" + methodName + CidaasConstants.ERROR_CODE
                + WebAuthError.shared.errorCode
                + CidaasConstants.ERROR_MESSAGE + errorEntity.getError();
        LogFile.getShared(context).addFailureLog(loggerMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);
        return WebAuthError.shared;
    }

    // Custom Exception
    public WebAuthError customException(int errorCode, String errorMessage, String methodName) {

        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(HttpStatusCode.BAD_REQUEST);
        WebAuthError.shared.setErrorMessage(errorMessage);

        errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(HttpStatusCode.BAD_REQUEST);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = "CustomException:-" + methodName + CidaasConstants.ERROR_CODE + errorCode + " "
                + "Error Message:- " + errorMessage;
        LogFile.getShared(context).addFailureLog(loggerMessage);

        return WebAuthError.shared;
    }

    // Custom Exception
    public WebAuthError fingerPrintException(String errorMessage, String methodName) {

        WebAuthError.shared.setErrorCode(WebAuthErrorCode.FINGERPRINT_AUTHENTICATION_FAILED);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(errorMessage);

        errorEntity = new ErrorEntity();
        errorEntity.setCode(WebAuthErrorCode.FINGERPRINT_AUTHENTICATION_FAILED);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = "Finger print Exception:-" + methodName + CidaasConstants.ERROR_CODE
                + WebAuthError.shared.errorCode + CidaasConstants.ERROR_MESSAGE + errorMessage;
        LogFile.getShared(context).addFailureLog(loggerMessage);

        return WebAuthError.shared;
    }

    // Custom Exception
    public WebAuthError fingerPrintError(int ErrorCode, String errorMessage, String methodName) {

        WebAuthError.shared.setErrorCode(ErrorCode);
        WebAuthError.shared.setStatusCode(HttpStatusCode.EXPECTATION_FAILED);
        WebAuthError.shared.setErrorMessage(errorMessage);

        errorEntity = new ErrorEntity();
        errorEntity.setCode(ErrorCode);
        errorEntity.setStatus(HttpStatusCode.EXPECTATION_FAILED);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = "Finger print Error:-" + methodName + CidaasConstants.ERROR_CODE + ErrorCode + " "
                + "Error Message:- " + errorMessage;
        LogFile.getShared(context).addFailureLog(loggerMessage);

        return WebAuthError.shared;
    }

    // unauthorized Access
    public WebAuthError unAuthorizedAccess(int errorCode, String errorMessage, String methodName) {
        WebAuthError.shared.setErrorCode(errorCode);
        WebAuthError.shared.setStatusCode(HttpStatusCode.UNAUTHORIZED);
        WebAuthError.shared.setErrorMessage(errorMessage);

        errorEntity = new ErrorEntity();
        errorEntity.setCode(errorCode);
        errorEntity.setStatus(HttpStatusCode.UNAUTHORIZED);
        errorEntity.setError(errorMessage);

        WebAuthError.shared.setErrorEntity(errorEntity);

        String loggerMessage = methodName + " :- " + " ErrorCode : " + errorCode + " " + "Error Response From Json"
                + errorMessage + " Empty response" + CidaasConstants.STATUS_CODE + statusCode;
        LogFile.getShared(context).addFailureLog(loggerMessage);
        Timber.d(loggerMessage);

        return WebAuthError.shared;
    }

}
