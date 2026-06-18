package de.cidaas.sdk.android.helper.general;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.Dictionary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.cidaas.sdk.android.entities.DeviceInfoEntity;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.network.CertificatePinningConfig;
import de.cidaas.sdk.android.properties.CidaasProperties;

import static android.os.Build.MODEL;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CidaasHelper {

    // Shared Instances
    public static CidaasHelper shared;
    public Context context;

    public static boolean ENABLE_PKCE;
    public boolean ENABLE_LOG;

    public DeviceInfoEntity deviceInfoEntity;

    public static String baseurl = "";
    public static int cidaasVersion;

    // Comomon Varivale
    public static String APP_NAME = "de.cidaas";
    public static String APP_VERSION = "";

    public static boolean IS_SETURL_CALLED = false;

    @Nullable
    public static CertificatePinningConfig certificatePinningConfig;

    /**
     * When non-zero, {@link de.cidaas.sdk.android.helper.network.CertificatePinningHelper} builds pins
     * by parsing this {@code res/xml} resource (Network Security Config schema) instead of using
     * {@link #certificatePinningConfig}.
     */
    public static int networkSecurityPinningXmlResId;

    public static CidaasHelper getShared(Context context) {
        if (shared == null) {
            shared = new CidaasHelper(context);
        }
        return shared;
    }

    CidaasHelper(Context context) {
        this.context = context;
    }

    public void initialiseObject() {
        // Initialise Shared Preferences
        DBHelper.setConfig(context);

        // Default Value;
        ENABLE_PKCE = true;

        // Default Log Value
        ENABLE_LOG = false;

        CidaasHelper.baseurl = "";
        CidaasHelper.IS_SETURL_CALLED = false;

        // Add Device info
        deviceInfoEntity = new DeviceInfoEntity();
        deviceInfoEntity
                .setDeviceId(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        deviceInfoEntity.setDeviceModel(android.os.Build.MODEL);
        deviceInfoEntity.setDeviceVersion(String.valueOf(Build.VERSION.RELEASE));
        deviceInfoEntity.setDeviceMake(Build.MANUFACTURER + " " + Build.BRAND);
        deviceInfoEntity.setDeviceType("MOBILE");

        if (DBHelper.getShared().getFCMToken() != null && !DBHelper.getShared().getFCMToken().equals("")) {
            deviceInfoEntity.setPushNotificationId(DBHelper.getShared().getFCMToken());
        }

        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;

        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
            APP_VERSION = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName,
                    0).versionName;
        } catch (final PackageManager.NameNotFoundException e) {
        }

        if (applicationInfo != null) {
            // Use package name instead of application label for ASCII-safe User-Agent
            // header
            APP_NAME = applicationInfo.packageName;
        } else {
            APP_NAME = "UNKNOWN";
        }

        // Store Device info for Later Purposes
        DBHelper.getShared().addDeviceInfo(deviceInfoEntity);

        CidaasProperties.getShared(context).saveCidaasProperties(new EventResult<Dictionary<String, String>>() {
            @Override
            public void success(Dictionary<String, String> result) {
                CidaasHelper.baseurl = result.get("DomainURL");
                CidaasHelper.cidaasVersion = result.get("CidaasVersion") != null
                        ? Integer.parseInt(result.get("CidaasVersion"))
                        : 2;
            }

            @Override
            public void failure(WebAuthError error) {

            }
        });
    }

    public static boolean isENABLE_PKCE() {
        ENABLE_PKCE = DBHelper.getShared().getEnablePKCE();
        return ENABLE_PKCE;
    }

    public void setENABLE_PKCE(boolean ENABLE_PKCE) {
        this.ENABLE_PKCE = ENABLE_PKCE;
        DBHelper.getShared().setEnablePKCE(ENABLE_PKCE);
    }

    // enableLog

    public boolean isLogEnable() {
        ENABLE_LOG = DBHelper.getShared().getEnableLog();
        return ENABLE_LOG;
    }

    public String enableLog() {
        String messsage = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            messsage = enableLogWithPermission();
            return messsage;
        }
        // Check permission For marshmallow and above
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                messsage = enableLogWithPermission();
                return messsage;
            } else {
                messsage = "Storge permission is not given, please request storage permisson to enable log";
                return messsage;
            }
        } else {
            messsage = enableLogWithPermission();
            return messsage;
        }
    }

    private String enableLogWithPermission() {
        // Enable Log
        this.ENABLE_LOG = true;
        DBHelper.getShared().setEnableLog(ENABLE_LOG);
        return "Log Successfully Enabled";
    }

    public String disableLog() {
        this.ENABLE_LOG = false;
        DBHelper.getShared().setEnableLog(ENABLE_LOG);
        return "Log Successfully Disabled";
    }

    /**
     * Pins the cidaas instance host derived from {@link #baseurl}.
     * Call after the domain URL is known (e.g. after {@code setURL}).
     */
    public static void setCertificatePinning(@NonNull String... pinHashes) {
        networkSecurityPinningXmlResId = 0;
        certificatePinningConfig = new CertificatePinningConfig(null, pinHashes);
    }

    /**
     * Pins a specific host (use when the pin host differs from the configured base URL).
     */
    public static void setCertificatePinning(@NonNull String host, @NonNull String... pinHashes) {
        networkSecurityPinningXmlResId = 0;
        certificatePinningConfig = new CertificatePinningConfig(host, pinHashes);
    }

    public static void setCertificatePinning(@NonNull CertificatePinningConfig config) {
        networkSecurityPinningXmlResId = 0;
        certificatePinningConfig = config;
    }

    /**
     * Use certificate pins declared in {@code res/xml/network_security_config.xml} (same {@code domain-config} /
     * {@code pin-set} format as {@code android:networkSecurityConfig}). OkHttp does not read that file
     * automatically; the SDK parses it and builds an OkHttp {@code CertificatePinner}.
     * <p>
     * Mutually exclusive with {@link #setCertificatePinning(String...)} for a given build: when this id is
     * non-zero, programmatic {@link #certificatePinningConfig} is ignored until cleared.
     */
    public static void setCertificatePinningFromNetworkSecurityConfig(int xmlResId) {
        certificatePinningConfig = null;
        networkSecurityPinningXmlResId = xmlResId;
    }

    public static void clearCertificatePinning() {
        certificatePinningConfig = null;
        networkSecurityPinningXmlResId = 0;
    }

}
