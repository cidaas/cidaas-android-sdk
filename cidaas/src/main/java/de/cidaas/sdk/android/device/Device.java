package de.cidaas.sdk.android.device;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.service.entity.device.DeviceRegistrationResponseEntity;
import de.cidaas.sdk.android.service.repository.device.DeviceRegistrationService;

/**
 * Device verification / registration entry point from {@link Cidaas#device()}.
 */
public final class Device {

    private final Cidaas cidaas;

    public Device(@NonNull Cidaas cidaas) {
        this.cidaas = cidaas;
    }

    /**
     * Initiates device registration via {@code POST .../devices/registration/initiation}. The initiation response
     * must include {@code nonce}, {@code session_id} (or {@code sessionId}), and {@code provider} ({@code google} or
     * {@code firebase}). The SDK then runs Play Integrity or Firebase App Check and completes registration via
     * {@code POST .../devices/registration/verification}.
     * Device id comes from stored {@linkplain de.cidaas.sdk.android.entities.DeviceInfoEntity device info};
     * {@code client_id} comes from saved login properties (e.g. {@code cidaas.xml}).
     *
     * @param activity hosting activity for biometric proof signing during verification
     * @param pushId   FCM push notification id
     */
    public void startRegistration(@NonNull FragmentActivity activity,
                                  @NonNull String pushId,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        startRegistration(activity, pushId, null, callback);
    }

    /**
     * Same as {@link #startRegistration(FragmentActivity, String, EventResult)} with an explicit Play Integrity
     * cloud project number (used when initiation {@code provider} is {@code google}; must match the GCP project
     * linked under Play Console → App integrity).
     */
    public void startRegistration(@NonNull FragmentActivity activity,
                                  @NonNull String pushId,
                                  @Nullable Long playIntegrityCloudProjectNumber,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        DeviceRegistrationService.getShared(cidaas.context).startRegistration(
                activity, pushId, playIntegrityCloudProjectNumber, callback);
    }
}
