package de.cidaas.sdk.android.controller;

import android.content.Context;

import java.util.Dictionary;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.entity.userlogininfo.UserLoginInfoEntity;
import de.cidaas.sdk.android.service.entity.userlogininfo.UserLoginInfoResponseEntity;
import de.cidaas.sdk.android.service.repository.LocationHistory.UserLoginInfoService;
import timber.log.Timber;

public class UserLoginInfoController {

    private Context context;
    private String TrackId;

    public static UserLoginInfoController shared;

    public UserLoginInfoController(Context contextFromCidaas) {

        context = contextFromCidaas;

    }

    public static UserLoginInfoController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new UserLoginInfoController(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    public void getUserLoginInfo(final UserLoginInfoEntity userLoginInfoEntity,
            final EventResult<UserLoginInfoResponseEntity> result) {
        final String methodName = "UserLoginInfoController :getUserLoginInfo()";
        try {
            if (userLoginInfoEntity.getSub() != null && !userLoginInfoEntity.getSub().equals("")) {
                CidaasProperties.getShared(context)
                        .checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                            @Override
                            public void success(final Dictionary<String, String> lpresult) {
                                final String baseurl = lpresult.get("DomainURL");

                                // Get AccessToken From Sub
                                AccessTokenController.getShared(context).getAccessToken(userLoginInfoEntity.getSub(),
                                        new EventResult<AccessTokenEntity>() {
                                            @Override
                                            public void success(AccessTokenEntity accessTokenresult) {
                                                getUserLoginInfo(baseurl, accessTokenresult.getAccess_token(),
                                                        userLoginInfoEntity, result);
                                            }

                                            @Override
                                            public void failure(WebAuthError error) {
                                                result.failure(error);
                                            }
                                        });

                            }

                            @Override
                            public void failure(WebAuthError error) {
                                result.failure(WebAuthError.getShared(context).cidaasPropertyMissingException(
                                        error.getErrorMessage(), CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
                            }
                        });
            } else {
                // handle Faliure
                result.failure(WebAuthError.getShared(context).propertyMissingException("Sub must not be empty",
                        CidaasConstants.ERROR_LOGGING_PREFIX + methodName));
            }
        } catch (Exception e) {
            // handle Faliure Exception
            result.failure(WebAuthError.getShared(context).methodException(
                    CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName,
                    WebAuthErrorCode.USER_LOGIN_INFO_SERVICE_FAILURE, e.getMessage()));
        }
    }

    public void getUserLoginInfo(String baseurl, String accessToken, UserLoginInfoEntity locationHistoryEntity,
            EventResult<UserLoginInfoResponseEntity> locationHistoryResponseEntityResult) {
        String methodName = "UserLoginInfoController :getUserLoginInfo()";
        if (baseurl != null && !baseurl.equals("") && accessToken != null && !accessToken.equals("")) {
            UserLoginInfoService.getShared(context).getUserLoginInfoService(baseurl, accessToken, locationHistoryEntity,
                    locationHistoryResponseEntityResult);
        } else {
            locationHistoryResponseEntityResult.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Base URL and Access Token must not be empty",
                    "Error" + methodName));
        }

    }
}
