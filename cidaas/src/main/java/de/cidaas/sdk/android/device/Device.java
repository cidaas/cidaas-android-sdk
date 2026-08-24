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
     * Device registration with {@code useAppAttestation = false} (biometric + DPoP only).
     */
    public void startRegistration(@NonNull FragmentActivity activity,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        startRegistration(activity, false, null, null, callback);
    }

    /**
     * Device registration. {@code useAppAttestation} defaults to {@code false}.
     *
     * @param activity hosting activity for biometric key usage during verification
     * @param pushId   ignored unless {@code useAppAttestation} is {@code true}
     */
    public void startRegistration(@NonNull FragmentActivity activity,
                                  @Nullable String pushId,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        startRegistration(activity, false, pushId, null, callback);
    }

    /**
     * Device registration.
     *
     * <p>Default is {@code useAppAttestation = false}: no FCM {@code push_id}, no Play Integrity /
     * Firebase App Check; verification is biometric + DPoP only.
     *
     * <p>When {@code true}, the SDK sends {@code push_id}, runs Play Integrity or Firebase App Check
     * from initiation {@code provider}, and puts that token in the verification DPoP proof.
     *
     * @param activity            hosting activity
     * @param useAppAttestation   {@code true} to use app attestation; omit or {@code false} to skip
     * @param pushId              FCM id; required when {@code useAppAttestation} is {@code true}
     */
    public void startRegistration(@NonNull FragmentActivity activity,
                                  boolean useAppAttestation,
                                  @Nullable String pushId,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        startRegistration(activity, useAppAttestation, pushId, null, callback);
    }

    /**
     * Same as {@link #startRegistration(FragmentActivity, boolean, String, EventResult)} with an explicit
     * Play Integrity cloud project number (used only when {@code useAppAttestation} is {@code true} and
     * initiation {@code provider} is {@code google}).
     */
    public void startRegistration(@NonNull FragmentActivity activity,
                                  boolean useAppAttestation,
                                  @Nullable String pushId,
                                  @Nullable Long playIntegrityCloudProjectNumber,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        DeviceRegistrationService.getShared(cidaas.context).startRegistration(
                activity, useAppAttestation, pushId, playIntegrityCloudProjectNumber, callback);
    }

    /**
     * @deprecated Prefer {@link #startRegistration(FragmentActivity, boolean, String, Long, EventResult)}.
     */
    @Deprecated
    public void startRegistration(@NonNull FragmentActivity activity,
                                  @NonNull String pushId,
                                  @Nullable Long playIntegrityCloudProjectNumber,
                                  @NonNull EventResult<DeviceRegistrationResponseEntity> callback) {
        startRegistration(activity, false, pushId, playIntegrityCloudProjectNumber, callback);
    }
}
