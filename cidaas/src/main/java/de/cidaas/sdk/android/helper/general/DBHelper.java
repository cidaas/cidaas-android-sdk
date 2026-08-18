package de.cidaas.sdk.android.helper.general;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.crypthelper.DpopConfig;
import de.cidaas.sdk.android.helper.storage.SecureStorage;
import de.cidaas.sdk.android.models.dbmodel.AccessTokenModel;
import de.cidaas.sdk.android.models.dbmodel.UserInfoModel;
import timber.log.Timber;

/**
 * Local persistence for SDK state via {@link SharedPreferences} (not a SQL database).
 * String values may be encrypted when {@link SecureStorage} is enabled (Keystore + Tink).
 */
public class DBHelper {


    // Set Enable Log
    public static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static final String LOGIN_PROPERTIES = "Login_Properties";
    private static final String CHALLENGE_PROPERTIES = " Challenge_Properties";
    private static final String DEVICE_INFO = "Device_info";
    private static final String FCMTOKEN_INFO = "FCMTOKEN_info";
    private static final String SECRET_INFO = "Secret_info";
    private static final String USER_AGENT = "UserAgent";
    private static final String USER_DEVICE_INFO = "User_device_Info";
    private static final String PKCE_ENABLE_STATUS = "OAuthEnablePkce";
    private static final String ENCRYPTED_STRING = "EncryptedString";
    private static final String LOG_ENABLE_STATUS = "OAuthEnableLog";
    private static String user_storage_key = "cidaas_user_details_";
    private static String user_storage_info = "cidaas_user_info_";
    private static ObjectMapper shared_objectMapper = new ObjectMapper();

    public static DBHelper dbHelper = null;


    public static DBHelper getShared() {
        if (dbHelper == null) {
            dbHelper = new DBHelper();
        }
        return dbHelper;
    }


    //setEnablePKCE

    public void setEnablePKCE(boolean enablePKCE) {
        try {
            editor.putBoolean(PKCE_ENABLE_STATUS, enablePKCE);
            editor.commit();
        } catch (Exception e) {
            //Exception enabling PKCE
        }
    }


    //Get EnablePKCE
    public boolean getEnablePKCE() {
        boolean result = false;
        try {
            result = preferences.getBoolean(PKCE_ENABLE_STATUS, true);
        } catch (Exception e) {
            result = false;
        }

        return result;
    }
//SET ENCRYPTED DATA
    public void setEncryptedData(String encryptedString) {
        try {
            putSecureString(ENCRYPTED_STRING, encryptedString);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: setEncryptedData failed");
        }
    }


    //Get ENCRYPTED DATA
    public String getEncryptedData() {
        String result = "";
        try {
            result = getSecureString(ENCRYPTED_STRING, "");
        } catch (Exception e) {
            result = "";
        }

        return result;
    }
    //Set Enable Log
    public void setEnableLog(boolean enableLog) {
        try {
            editor.putBoolean(LOG_ENABLE_STATUS, enableLog);
            editor.commit();
        } catch (Exception e) {
            //Exception enabling Log
        }
    }

    //Get EnableLog
    public boolean getEnableLog() {
        boolean result = false;
        try {
            result = preferences.getBoolean(LOG_ENABLE_STATUS, true);
        } catch (Exception e) {
            result = false;
        }

        return result;
    }


    public static void setConfig(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("cidaas_preference", 0);
            editor = preferences.edit();
        }
        SecureStorage.init(context);
        DpopConfig.init(context);
    }


    //Add Secret Based on Sub
    public void addSecret(String secret, String sub) {
        try {
            putSecureString(SECRET_INFO + sub, secret);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: addSecret failed for sub");
        }

    }

    //get Secret base on Sub
    public String getSecret(String sub) {
        String secret;
        try {
            secret = getSecureString(SECRET_INFO + sub, "");
        } catch (Exception e) {
            secret = "";
        }
        return secret;
    }


    //remove Secret
    public String removeSecret(String sub) {
        String secret;
        try {

            editor.remove(SECRET_INFO + sub);
            editor.commit();
            secret = "success";
        } catch (Exception e) {
            secret = "Fails" + e.getMessage();
        }
        return secret;

    }

    //Add Secret Based on Sub
    public void setUserAgent(String userAgent) {
        try {
            putSecureString(USER_AGENT, userAgent);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: setUserAgent failed");
        }

    }

    //get Device info
    public String getUserAgent() {
        String userAgent;
        try {
            userAgent = getSecureString(USER_AGENT, "");
        } catch (Exception e) {
            userAgent = "";
        }
        return userAgent;
    }

    //Add Challenge Properties
    public void addChallengeProperties(Dictionary<String, String> challengepropObj) {
        try {
            String jsonString = shared_objectMapper.writeValueAsString(challengepropObj);
            putSecureString(CHALLENGE_PROPERTIES, jsonString);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: addChallengeProperties failed");
        }

    }

    //Get Challenge Properties
    public Dictionary<String, String> getChallengeProperties() {

        Dictionary<String, String> challengeProperties = new Hashtable<>();
        try {
            String jsonString = getSecureString(CHALLENGE_PROPERTIES, (new JSONObject().toString()));
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                String value = (String) jsonObject.get(key);
                challengeProperties.put(key, value);
            }
        } catch (Exception e) {
            challengeProperties = null;
        }
        return challengeProperties;
    }


    //Add Login object in LocalDB(SharedPreferences) for Future purposes
    public boolean addLoginProperties(Dictionary<String, String> loginpropObj) {
        try {
            String jsonString = shared_objectMapper.writeValueAsString(loginpropObj);
            return putSecureString(LOGIN_PROPERTIES + loginpropObj.get("DomainURL"), jsonString);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: addLoginProperties failed");
            return false;
        }
    }


    //Get Login Object
    public Dictionary<String, String> getLoginProperties(String domainURL) {

        Dictionary<String, String> loginProperties = new Hashtable<>();
        try {
            String jsonString = getSecureString(LOGIN_PROPERTIES + domainURL, (new JSONObject().toString()));
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                String value = (String) jsonObject.get(key);
                loginProperties.put(key, value);
            }
        } catch (Exception e) {
            loginProperties = null;
        }
        return loginProperties;
    }


    //Add Device
    public void addDeviceInfo(DeviceInfoEntity deviceInfoEntity) {
        try {
            String jsonString = shared_objectMapper.writeValueAsString(deviceInfoEntity);
            putSecureString(DEVICE_INFO, jsonString);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: addDeviceInfo failed");
        }
    }

    //get Device info
    public DeviceInfoEntity getDeviceInfo() {
        String jsonString = getSecureString(DEVICE_INFO, "");
        DeviceInfoEntity deviceInfoEntity;
        try {
            // parse jsonObject instead of Jackson readValue() as Jackson also map stability from Parcelable to jsonString as of Android 14
            JSONObject jsonObject = new JSONObject(jsonString);
            deviceInfoEntity = new DeviceInfoEntity();
            if (jsonObject.has("deviceId")) {
                deviceInfoEntity.setDeviceId(jsonObject.getString("deviceId"));
            }
            if (jsonObject.has("deviceMake")) {
                deviceInfoEntity.setDeviceMake(jsonObject.getString("deviceMake"));
            }
            if (jsonObject.has("deviceModel")) {
                deviceInfoEntity.setDeviceModel(jsonObject.getString("deviceModel"));
            }
            if (jsonObject.has("deviceType")) {
                deviceInfoEntity.setDeviceType(jsonObject.getString("deviceType"));
            }
            if (jsonObject.has("deviceVersion")) {
                deviceInfoEntity.setDeviceVersion(jsonObject.getString("deviceVersion"));
            }
            if (jsonObject.has("pushNotificationId")) {
                deviceInfoEntity.setPushNotificationId(jsonObject.getString("pushNotificationId"));
            }
        } catch (Exception e) {
            deviceInfoEntity = null;
        }
        return deviceInfoEntity;
    }


    //Add Device
    public void setFCMToken(String fcmToken) {
        try {
            putSecureString(FCMTOKEN_INFO, fcmToken);

            DeviceInfoEntity deviceInfoEntity = getDeviceInfo();
            if (deviceInfoEntity != null) {
                deviceInfoEntity.setPushNotificationId(fcmToken);
                addDeviceInfo(deviceInfoEntity);
            }
        } catch (Exception e) {
            Timber.e(e, "DBHelper: setFCMToken failed");
        }
    }

    //get Device info
    public String getFCMToken() {
        String fcmtoken;
        try {
            fcmtoken = getSecureString(FCMTOKEN_INFO, "");
        } catch (Exception e) {
            fcmtoken = "";
        }
        return fcmtoken;
    }


    //Add User Device id with Domain URL
    public void setUserDeviceId(String userDeviceId, String domainURL) {
        try {
            putSecureString(USER_DEVICE_INFO + domainURL, userDeviceId);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: setUserDeviceId failed");
        }
    }

    //get Device info
    public String getUserDeviceId(String domainURL) {
        String userDeviceId;
        try {
            userDeviceId = getSecureString(USER_DEVICE_INFO + domainURL, "");
        } catch (Exception e) {
            userDeviceId = "";
        }
        return userDeviceId;
    }

    //Done Set Access Token
    public void setAccessToken(AccessTokenModel accessTokenModel) {
        try {
            String key = accessTokenModel.getUserId();
            String jsonString = shared_objectMapper.writeValueAsString(accessTokenModel);
            putSecureString(user_storage_key + key, jsonString);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: setAccessToken failed");
        }

    }

    //Done Get Access Token
    public AccessTokenModel getAccessToken(String userId) {
        String jsonString = getSecureString(user_storage_key + userId, "");
        AccessTokenModel accessTokenModel;
        try {
            accessTokenModel = shared_objectMapper.readValue(jsonString, AccessTokenModel.class);
        } catch (Exception e) {
            accessTokenModel = null;
        }
        return accessTokenModel;
    }

    //done Set UserInfo
    public void setUserInfo(UserInfoModel userInfoModel) {
        try {
            String key = userInfoModel.getUserid();
            String jsonString = shared_objectMapper.writeValueAsString(userInfoModel);
            putSecureString(user_storage_info + key, jsonString);
        } catch (Exception e) {
            Timber.e(e, "DBHelper: setUserInfo failed");
        }

    }

    //Done Get User info
    public UserInfoModel getUserInfo(String userId) {
        String jsonString = getSecureString(user_storage_info + userId, "DefaultUserinfo");
        UserInfoModel userInfoModel;
        try {
            userInfoModel = shared_objectMapper.readValue(jsonString, UserInfoModel.class);
        } catch (Exception e) {
            userInfoModel = null;
        }
        return userInfoModel;
    }


    //DeleteUserInfo
    public boolean removeUserInfo(String userId) {
        boolean result = false;
        //Remove Access Token
        String savedKey = user_storage_key + userId;
        editor.remove(savedKey);
        result = editor.commit();
        return result;
    }

    /**
     * Encrypts (when SecureStorage is on) and commits a string preference.
     *
     * @return {@code true} if {@link SharedPreferences.Editor#commit()} succeeded
     * @throws IllegalStateException if SecureStorage wrap fails (cipher missing / encrypt error);
     *                               callers must not treat that as a successful write
     */
    private static boolean putSecureString(String key, String value) {
        try {
            String wrapped = SecureStorage.wrapForPersist(key, value);
            editor.putString(key, wrapped);
            boolean committed = editor.commit();
            if (!committed) {
                Timber.e("DBHelper: SharedPreferences commit failed for key=%s", key);
            }
            return committed;
        } catch (RuntimeException e) {
            Timber.e(e, "DBHelper: failed to encrypt/persist string for key=%s", key);
            throw e;
        }
    }

    private static String getSecureString(String key, String defaultValue) {
        return SecureStorage.unwrapFromPersist(key, preferences.getString(key, defaultValue), defaultValue);
    }

}
