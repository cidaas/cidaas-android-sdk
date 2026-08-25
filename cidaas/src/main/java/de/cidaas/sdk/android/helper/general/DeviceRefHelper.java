package de.cidaas.sdk.android.helper.general;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import de.cidaas.sdk.android.entities.DeviceInfoEntity;

/**
 * Device reference used by web as cookie {@code cidaas_dr}.
 * After device registration, the device id from {@link DBHelper#getDeviceInfo()} is applied as
 * {@code Cookie: cidaas_dr=&lt;deviceId&gt;} on requestId, initiate, and authenticate calls.
 */
public final class DeviceRefHelper {

    public static final String COOKIE_NAME = "cidaas_dr";

    private DeviceRefHelper() {
    }

    /**
     * Device id from shared device info when the device is registered; otherwise {@code null}.
     */
    @Nullable
    public static String resolveRegisteredDeviceId() {
        try {
            if (!DBHelper.getShared().isDeviceRegistered()) {
                return null;
            }
            DeviceInfoEntity deviceInfo = DBHelper.getShared().getDeviceInfo();
            if (deviceInfo == null) {
                return null;
            }
            String deviceId = deviceInfo.getDeviceId();
            if (deviceId == null || deviceId.trim().isEmpty()) {
                return null;
            }
            return deviceId.trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Overlays headers with registered device id as {@code Cookie: cidaas_dr} when available.
     * Call from requestId / initiate / authenticate — not from shared {@code Headers.getHeaders}.
     */
    public static void applyToHeaders(@NonNull Map<String, String> headers) {
        String deviceId = resolveRegisteredDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }
        headers.put("Cookie", cookieHeaderValue(deviceId));
    }

    @NonNull
    public static String cookieHeaderValue(@NonNull String deviceId) {
        return COOKIE_NAME + "=" + deviceId;
    }
}
