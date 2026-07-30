package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticatehistory;

import android.content.Context;

import java.util.Dictionary;
import java.util.Map;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.AuthenticatedHistoryResponseNew;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.UserAuthenticatedHistoryDataEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.authenticatedhistory.UserAuthenticatedHistoryResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.deviceslist.DevicesListEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DeviceListResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DeviceMfaDataEntitiy;
import de.cidaas.sdk.android.cidaasverification.data.entity.scanned.DevicesMfaResponse;
import de.cidaas.sdk.android.cidaasverification.data.service.helper.VerificationURLHelper;
import de.cidaas.sdk.android.cidaasverification.domain.service.authenticatedhistory.AuthenticatedHistoryService;
import de.cidaas.sdk.android.cidaasverification.util.VerificationConstants;
import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.helper.urlhelper.URLHelper;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;


public class AuthenticatedHistoryController {
    //Local Variables
    private Context context;


    public static AuthenticatedHistoryController shared;

    public AuthenticatedHistoryController(Context contextFromCidaas) {
        context = contextFromCidaas;
    }


    public static AuthenticatedHistoryController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new AuthenticatedHistoryController(contextFromCidaas);
            }
        } catch (Exception e) {
            LogFile.getShared(contextFromCidaas).addFailureLog("AuthenticatedHistoryController instance Creation Exception:-" + e.getMessage());
        }
        return shared;
    }

    //--------------------------------------------AuthenticatedHistory--------------------------------------------------------------
    public void getauthenticatedHistoryList(final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponse> authenticatedHistoryResult) {
        checkAuthenticatedHistoryEntity(authenticatedHistoryEntity, authenticatedHistoryResult);
    }
    public void getauthenticatedHistoryListNew(final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponseNew> authenticatedHistoryResult) {
        checkAuthenticatedHistoryEntityNew(authenticatedHistoryEntity, authenticatedHistoryResult);
    }

    public void getauthenticatedHistoryListDetail(UserAuthenticatedHistoryDataEntity userAuthenticatedHistoryDataEntity,
                                                  EventResult<UserAuthenticatedHistoryResponse> userAuthenticatedHistoryResponseEventResult) {
        checkAuthenticatedHistoryEntityDetail(userAuthenticatedHistoryDataEntity, userAuthenticatedHistoryResponseEventResult);
    }
    //-------------------------------------checkAuthenticatedHistoryEntity-----------------------------------------------------------
    private void checkAuthenticatedHistoryEntityDetail(final UserAuthenticatedHistoryDataEntity authenticatedHistoryEntity, final EventResult<UserAuthenticatedHistoryResponse> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_AUTHENTICATED_HISTORY_ENTITY;
        try {


            addPropertiesDetail(authenticatedHistoryEntity, authenticatedHistoryResult);


        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void checkAuthenticatedHistoryEntityNew(final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponseNew> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_AUTHENTICATED_HISTORY_ENTITY;
        try {
            if (authenticatedHistoryEntity.getVerification_type() != null && !authenticatedHistoryEntity.getVerification_type().equals("") &&
                    authenticatedHistoryEntity.getSub() != null && !authenticatedHistoryEntity.getSub().equals("")
            ) {
                if (authenticatedHistoryEntity.getStart_time() != null && !authenticatedHistoryEntity.getStart_time().equals("") &&
                        authenticatedHistoryEntity.getEnd_time() != null && !authenticatedHistoryEntity.getEnd_time().equals("")) {

                    LogFile.getShared(context).addInfoLog(methodName, "Verification Type:" + authenticatedHistoryEntity.getVerification_type() +
                            "Sub:" + authenticatedHistoryEntity.getSub());
                    addPropertiesNew(authenticatedHistoryEntity, authenticatedHistoryResult);
                } else {
                    authenticatedHistoryResult.failure(WebAuthError.getShared(context).propertyMissingException("StartDate or EndDate  must not be null",
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
            } else {
                authenticatedHistoryResult.failure(WebAuthError.getShared(context).propertyMissingException("VerificationType or Sub  must not be null",
                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    //-------------------------------------checkAuthenticatedHistoryEntity-----------------------------------------------------------
    private void checkAuthenticatedHistoryEntity(final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponse> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_AUTHENTICATED_HISTORY_ENTITY;
        try {
            if (authenticatedHistoryEntity.getVerification_type() != null && !authenticatedHistoryEntity.getVerification_type().equals("") &&
                    authenticatedHistoryEntity.getSub() != null && !authenticatedHistoryEntity.getSub().equals("")
            ) {
                if (authenticatedHistoryEntity.getStart_time() != null && !authenticatedHistoryEntity.getStart_time().equals("") &&
                        authenticatedHistoryEntity.getEnd_time() != null && !authenticatedHistoryEntity.getEnd_time().equals("")) {

                    LogFile.getShared(context).addInfoLog(methodName, "Verification Type:" + authenticatedHistoryEntity.getVerification_type() +
                            "Sub:" + authenticatedHistoryEntity.getSub());
                    addProperties(authenticatedHistoryEntity, authenticatedHistoryResult);
                } else {
                    authenticatedHistoryResult.failure(WebAuthError.getShared(context).propertyMissingException("StartDate or EndDate  must not be null",
                            VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                    return;
                }
            } else {
                authenticatedHistoryResult.failure(WebAuthError.getShared(context).propertyMissingException("VerificationType or Sub  must not be null",
                        VerificationConstants.ERROR_LOGGING_PREFIX + methodName));
                return;
            }
        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void addPropertiesDetail(UserAuthenticatedHistoryDataEntity authenticatedHistoryEntity, EventResult<UserAuthenticatedHistoryResponse> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_ADD_PROPERTIES;
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                    final String clientId = loginPropertiesResult.get(VerificationConstants.CLIENT_ID);


                    //App properties
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();

                    authenticatedHistoryEntity.setPush_id(deviceInfoEntity.getPushNotificationId());
                    authenticatedHistoryEntity.setClient_id(clientId);

                    //call authenticatedHistory call
                    callAuthenticatedHistoryDetail(baseurl, authenticatedHistoryEntity, authenticatedHistoryResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    authenticatedHistoryResult.failure(error);
                }
            });

        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    //-------------------------------------Add Device info and pushnotificationId-------------------------------------------------------
    private void addProperties(final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponse> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_ADD_PROPERTIES;
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                    final String clientId = loginPropertiesResult.get(VerificationConstants.CLIENT_ID);


                    //App properties
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
                    authenticatedHistoryEntity.setDevice_id(deviceInfoEntity.getDeviceId());
                    authenticatedHistoryEntity.setPush_id(deviceInfoEntity.getPushNotificationId());
                    authenticatedHistoryEntity.setClient_id(clientId);

                    //call authenticatedHistory call
                    callAuthenticatedHistory(baseurl, authenticatedHistoryEntity, authenticatedHistoryResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    authenticatedHistoryResult.failure(error);
                }
            });

        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    private void addPropertiesNew(final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponseNew> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_ADD_PROPERTIES;
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get(VerificationConstants.DOMAIN_URL);
                    final String clientId = loginPropertiesResult.get(VerificationConstants.CLIENT_ID);


                    //App properties
                    DeviceInfoEntity deviceInfoEntity = DBHelper.getShared().getDeviceInfo();
                    authenticatedHistoryEntity.setDevice_id(deviceInfoEntity.getDeviceId());
                    authenticatedHistoryEntity.setPush_id(deviceInfoEntity.getPushNotificationId());
                    authenticatedHistoryEntity.setClient_id(clientId);

                    //call authenticatedHistory call
                    callAuthenticatedHistoryNew(baseurl, authenticatedHistoryEntity, authenticatedHistoryResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    authenticatedHistoryResult.failure(error);
                }
            });

        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    //-------------------------------------------Call authenticatedHistory Service-----------------------------------------------------------
    private void callAuthenticatedHistoryDetail(String baseurl, final UserAuthenticatedHistoryDataEntity authenticatedHistoryEntity, final EventResult<UserAuthenticatedHistoryResponse> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_AUTHENTICATED_HISTORY;
        try {
            String authenticatedHistoryUrl = VerificationURLHelper.getShared().getAuthentictedHistoryDetailURL(baseurl);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //AuthenticatedHistory Service call
            AuthenticatedHistoryService.getShared(context).callAuthenticatedHistoryServiceDetail(authenticatedHistoryUrl, headers, authenticatedHistoryEntity, authenticatedHistoryResult);
        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    //-------------------------------------------Call authenticatedHistory Service-----------------------------------------------------------
    private void callAuthenticatedHistory(String baseurl, final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponse> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_AUTHENTICATED_HISTORY;
        try {
            String authenticatedHistoryUrl = VerificationURLHelper.getShared().getAuthentictedHistoryURL(baseurl);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //AuthenticatedHistory Service call
            AuthenticatedHistoryService.getShared(context).callAuthenticatedHistoryService(authenticatedHistoryUrl, headers, authenticatedHistoryEntity, authenticatedHistoryResult);
        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    private void callAuthenticatedHistoryNew(String baseurl, final AuthenticatedHistoryEntity authenticatedHistoryEntity, final EventResult<AuthenticatedHistoryResponseNew> authenticatedHistoryResult) {
        String methodName = VerificationConstants.METHOD_AUTHENTICATED_HISTORY;
        try {
            String authenticatedHistoryUrl = VerificationURLHelper.getShared().getAuthentictedHistoryURLNew(baseurl);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //AuthenticatedHistory Service call
            AuthenticatedHistoryService.getShared(context).callAuthenticatedHistoryServiceNew(authenticatedHistoryUrl, headers, authenticatedHistoryEntity, authenticatedHistoryResult);
        } catch (Exception e) {
            authenticatedHistoryResult.failure(WebAuthError.getShared(context).methodException(VerificationConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    public void getDevicesList(DevicesListEntity devicesListEntity, EventResult<DeviceListResponse> deviceListResponseEventResult) {

        checkGetDevicesListEntity(devicesListEntity, deviceListResponseEventResult);
    }

    private void checkGetDevicesListEntity(DevicesListEntity devicesListEntity, EventResult<DeviceListResponse> deviceListResponseEventResult) {
        String methodName = "AuthenticatedHistoryController:-checkGetDevicesListEntity()";
        try {

            addPropertiesGetDevices(devicesListEntity, deviceListResponseEventResult);

        } catch (Exception e) {
            deviceListResponseEventResult.failure(WebAuthError.getShared(context).methodException("Exception:-" + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    private void addPropertiesGetDevices(DevicesListEntity devicesListEntity, EventResult<DeviceListResponse> deviceListResponseEventResult) {

        String methodName = "AuthenticatedHistoryController:-addPropertiesGetDevices()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get("DomainURL");
                    final String clientId = loginPropertiesResult.get("ClientId");

                    //call authenticatedHistory call
                    callGetDevices(baseurl, devicesListEntity, deviceListResponseEventResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    deviceListResponseEventResult.failure(error);
                }
            });

        } catch (Exception e) {
            deviceListResponseEventResult.failure(WebAuthError.getShared(context).methodException("Exception:-" + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }

    }

    private void callGetDevices(String baseurl, DevicesListEntity devicesListEntity, EventResult<DeviceListResponse> deviceListResponseEventResult) {

        String methodName = "AuthenticatedHistoryController:-GetDevices()";
        try {
            String authenticatedHistoryUrl = VerificationURLHelper.getShared().getDevicesList(baseurl);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //AuthenticatedHistory Service call
            AuthenticatedHistoryService.getShared(context).callGetDevicesService(authenticatedHistoryUrl, headers, devicesListEntity, deviceListResponseEventResult);
            LogFile.getShared(context).addInfoLog(methodName, "Requesting devices list. URL:-" + authenticatedHistoryUrl);
            LogFile.getShared(context).addAPILog("Multidevices api url: " + authenticatedHistoryUrl);

        } catch (Exception e) {
            LogFile.getShared(context).addFailureLog(methodName + " Exception:-" + e.getMessage());
            deviceListResponseEventResult.failure(WebAuthError.getShared(context).methodException("Exception:-" + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }

    public void getDevicesRemove(DeviceMfaDataEntitiy deviceMfaDataEntitiy, EventResult<DevicesMfaResponse> devicesMfaResponseEventResult) {
        checkGetDevicesRemoveEntity(deviceMfaDataEntitiy, devicesMfaResponseEventResult);

    }

    private void checkGetDevicesRemoveEntity(DeviceMfaDataEntitiy deviceMfaDataEntitiy, EventResult<DevicesMfaResponse> devicesMfaResponseEventResult) {
        String methodName = "AuthenticatedHistoryController:-checkGetDevicesRemoveEntity()";
        try {

            addPropertiesGetDevicesRemove(deviceMfaDataEntitiy, devicesMfaResponseEventResult);

        } catch (Exception e) {
            devicesMfaResponseEventResult.failure(WebAuthError.getShared(context).methodException("Exception:-" + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    private void callGetDevicesRemove(String baseurl,DeviceMfaDataEntitiy deviceMfaDataEntitiy, EventResult<DevicesMfaResponse> devicesMfaResponseEventResult) {

        String methodName = "AuthenticatedHistoryController:-GetDevices()";
        try {
            String authenticatedHistoryUrl = VerificationURLHelper.getShared().getDevicesRemove(baseurl);

            //headers Generation
            Map<String, String> headers = Headers.getShared(context).getHeaders(null, false, URLHelper.contentTypeJson);

            //AuthenticatedHistory Service call
            AuthenticatedHistoryService.getShared(context).callGetDevicesRemoveService(authenticatedHistoryUrl, headers, deviceMfaDataEntitiy, devicesMfaResponseEventResult);
        } catch (Exception e) {
            devicesMfaResponseEventResult.failure(WebAuthError.getShared(context).methodException("Exception:-" + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }
    private void addPropertiesGetDevicesRemove(DeviceMfaDataEntitiy deviceMfaDataEntitiy, EventResult<DevicesMfaResponse> devicesMfaResponseEventResult) {


        String methodName = "AuthenticatedHistoryController:-addPropertiesGetDevicesRemove()";
        try {

            CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                @Override
                public void success(Dictionary<String, String> loginPropertiesResult) {
                    final String baseurl = loginPropertiesResult.get("DomainURL");
                    final String clientId = loginPropertiesResult.get("ClientId");

                    //call authenticatedHistory call
                    callGetDevicesRemove(baseurl, deviceMfaDataEntitiy, devicesMfaResponseEventResult);
                }

                @Override
                public void failure(WebAuthError error) {
                    devicesMfaResponseEventResult.failure(error);
                }
            });

        } catch (Exception e) {
            devicesMfaResponseEventResult.failure(WebAuthError.getShared(context).methodException("Exception:-" + methodName, WebAuthErrorCode.SCANNED_VERIFICATION_FAILURE,
                    e.getMessage()));
        }
    }


}
