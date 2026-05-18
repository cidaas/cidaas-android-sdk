package de.cidaas.sdk.android.device;

import androidx.annotation.NonNull;

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
     * Initiates device registration via {@code POST /verification-actions-srv/devices/registration}.
     * Device id comes from stored {@linkplain de.cidaas.sdk.android.entities.DeviceInfoEntity device info};
     * {@code client_id} comes from saved login properties (e.g. {@code cidaas.xml}).
     */
    public void startRegistration(@NonNull String pushId,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        DeviceRegistrationService.getShared(cidaas.context).startRegistration(pushId, callback);
    }
}
