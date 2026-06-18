package de.cidaas.sdk.android.helper.commonerror;

import android.content.Context;
import android.util.SparseIntArray;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.cidaas.sdk.android.entities.CommonErrorEntity;
import de.cidaas.sdk.android.entities.ErrorEntity;
import de.cidaas.sdk.android.entities.ProperErrorEntity;
import de.cidaas.sdk.android.entities.StandardErrorEntity;
import de.cidaas.sdk.android.helper.enums.HttpStatusCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.logger.LogFile;
import retrofit2.Response;


public class CommonError {

    public static CommonError shared;
    private Context context;


    private ObjectMapper objectMapper = new ObjectMapper();

    public CommonError(Context contextFromCidaas) {
        context = contextFromCidaas;
    }


    public static CommonError getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new CommonError(contextFromCidaas);
            }
        } catch (Exception e) {
            // Timber.i(e.getMessage());
        }
        return shared;
    }


    //Method to get Error Entity
    public WebAuthError generateCommonErrorEntity(int webAuthErrorCode, Response response, String methodName) {
        try {
            assert response.errorBody() != null;
            String errorResponse = response.errorBody().source().readByteString().utf8();
            return generateCommonErrorEntityFromBody(webAuthErrorCode, errorResponse, methodName);
        } catch (Exception e) {
            return WebAuthError.getShared(context).methodException(
                    "Exception :CommonError :generateCommonErrorEntity()", webAuthErrorCode, e.getMessage());
        }
    }

    public WebAuthError generateCommonErrorEntityFromBody(
            int webAuthErrorCode, String errorResponse, String methodName) {
        try {
            if (errorResponse.contains("<!DOCTYPE html>")) {
                return WebAuthError.getShared(context).emptyResponseException(
                        webAuthErrorCode, HttpStatusCode.NOT_FOUND, methodName);
            }

            if (errorResponse.contains(
                    "invalid_request: given url is not allowed by the application configuration.")) {
                String error_message =
                        "Configured redirect url for your application in cidaas is not allowed by the application "
                                + "configuration. Please update your application configuration in cidaas to include "
                                + "the redirect url";
                return WebAuthError.getShared(context).customException(webAuthErrorCode, error_message, methodName);
            }

            final CommonErrorEntity commonErrorEntity =
                    objectMapper.readValue(errorResponse, CommonErrorEntity.class);

            if (commonErrorEntity.getErrorAsString() != null) {
                String errorMessage = commonErrorEntity.getErrorAsString();
                LogFile.getShared(context).addFailureLog(
                        "Error:- WebAuthErrorCode: " + webAuthErrorCode + " ErrorCode:- "
                                + commonErrorEntity.getCode() + "error message:-"
                                + commonErrorEntity.getError() + errorMessage);

                return WebAuthError.getShared(context).serviceCallException(
                        webAuthErrorCode, errorMessage, commonErrorEntity.getStatus(),
                        null, errorResponse, methodName);

            } else if (commonErrorEntity.getErrorAsObject() != null) {
                ErrorEntity errorEntity = commonErrorEntity.getErrorAsObject();
                LogFile.getShared(context).addFailureLog(
                        "Error:- WebAuthErrorCode: " + webAuthErrorCode + " ErrorCode:- "
                                + errorEntity.getCode() + "error message:-" + errorEntity.getError());

                return WebAuthError.getShared(context).serviceCallException(
                        webAuthErrorCode, errorEntity.getError(), commonErrorEntity.getStatus(),
                        errorEntity, errorResponse, methodName);

            } else {
                LogFile.getShared(context).addFailureLog(
                        "Error:- WebAuthErrorCode: " + webAuthErrorCode + " Response Message:- " + errorResponse
                                + "error is Different type than Object or String");
                return WebAuthError.getShared(context).methodException(
                        "Exception :CommonError :generateCommonErrorEntity()", webAuthErrorCode, errorResponse);
            }
        } catch (Exception e) {
            return WebAuthError.getShared(context).methodException(
                    "Exception :CommonError :generateCommonErrorEntity()", webAuthErrorCode, e.getMessage());
        }
    }

    public WebAuthError generateStandardErrorEntity(int webAuthErrorCode, Response response, String methodName) {
        try {

            assert response.errorBody() != null;

            response.message().toLowerCase();

            // Handle proper error message
            String errorResponse = response.errorBody().source().readByteString().utf8();


            if (errorResponse.contains("<!DOCTYPE html>")) {
                return WebAuthError.getShared(context).emptyResponseException(webAuthErrorCode, HttpStatusCode.NOT_FOUND, methodName);
            }

            if (errorResponse.contains("invalid_request: given url is not allowed by the application configuration.")) {
                String error_message = "Configured redirect url for your application in cidaas is not allowed by the application configuration. " +
                        "Please update your application configuration in cidaas to include the redirect url";
                return WebAuthError.getShared(context).customException(webAuthErrorCode, error_message, methodName);
            }


            final StandardErrorEntity standardErrorEntity;
            standardErrorEntity = objectMapper.readValue(errorResponse, StandardErrorEntity.class);

            LogFile.getShared(context).addFailureLog("Error:- WebAuthErrorCode: " + webAuthErrorCode + " Response Message:- " + response.message() +
                    " ErrorCode:- " + standardErrorEntity.getError() + "error message:-" + standardErrorEntity.getError_description());


            return WebAuthError.getShared(context).serviceCallException(webAuthErrorCode, standardErrorEntity.getError_description(), response.code(),
                    null, errorResponse, methodName);
        } catch (Exception e) {

            return WebAuthError.getShared(context).methodException("Exception :CommonError :generateCommonErrorEntity()", webAuthErrorCode, e.getMessage());

        }
    }

    public WebAuthError generateProperErrorEntity(int webAuthErrorCode, Response response, String methodName) {
        try {

            assert response.errorBody() != null;

            response.message().toLowerCase();

            // Handle proper error message
            String errorResponse = response.errorBody().source().readByteString().utf8();


            if (errorResponse.contains("<!DOCTYPE html>")) {
                return WebAuthError.getShared(context).emptyResponseException(webAuthErrorCode, HttpStatusCode.NOT_FOUND, methodName);
            }

            if (errorResponse.contains("invalid_request: given url is not allowed by the application configuration.")) {
                String error_message = "Configured redirect url for your application in cidaas is not allowed by the application configuration. " +
                        "Please update your application configuration in cidaas to include the redirect url";
                return WebAuthError.getShared(context).customException(webAuthErrorCode, error_message, methodName);
            }


            final ProperErrorEntity properErrorEntity;
            properErrorEntity = objectMapper.readValue(errorResponse, ProperErrorEntity.class);

            LogFile.getShared(context).addFailureLog("Error:- WebAuthErrorCode: " + webAuthErrorCode + " Response Message:- " + response.message() +
                    " ErrorCode:- " + properErrorEntity.getStatus() + "error message:-" + properErrorEntity.getError());


            return WebAuthError.getShared(context).serviceCallException(webAuthErrorCode, properErrorEntity.getError(), response.code(),
                    null, errorResponse, methodName);
        } catch (Exception e) {

            return WebAuthError.getShared(context).methodException("Exception :CommonError :generateCommonErrorEntity()", webAuthErrorCode, e.getMessage());

        }
    }
}
