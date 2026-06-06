package de.cidaas.sdk.android.device;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Device-scoped verification APIs from {@link Cidaas#devices()}.
 *
 * <pre>{@code
 * cidaas.devices().verifications().fetch(sub, callback);
 * }</pre>
 *
 * <p>On success, the callback receives
 * {@code de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification.DeviceConfiguredVerificationsListResponseEntity}.</p>
 */
public final class Devices {

    private static final String NATIVE_CIDAAS_NATIVE =
            "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";

    private final Cidaas cidaas;

    public Devices(@NonNull Cidaas cidaas) {
        if (cidaas == null) {
            throw new IllegalArgumentException("cidaas must not be null");
        }
        this.cidaas = cidaas;
    }

    @NonNull
    public Verifications verifications() {
        return new Verifications(cidaas);
    }

    /**
     * Configured verification methods for the current device (stored device id / push id).
     */
    public static final class Verifications {

        private final Cidaas cidaas;

        Verifications(@NonNull Cidaas cidaas) {
            this.cidaas = cidaas;
        }

        /**
         * POST {@code verification-srv/v2/setup/device/configured/list} with device id, push id, client id, and
         * {@code sub} in the JSON body (same contract as {@code cidaasverification} configured MFA list).
         */
        public void fetch(@NonNull String sub, @NonNull EventResult<?> callback) {
            if (sub == null || sub.isEmpty()) {
                callback.failure(WebAuthError.getShared(cidaas.context).propertyMissingException(
                        "Sub must not be null or empty", "Devices.verifications().fetch"));
                return;
            }
            invokeGetDeviceConfiguredVerificationsList(sub, callback);
        }

        private void invokeGetDeviceConfiguredVerificationsList(@NonNull String sub,
                @NonNull EventResult<?> callback) {
            Context context = cidaas.context;
            try {
                Class<?> nativeClazz = Class.forName(NATIVE_CIDAAS_NATIVE);
                Object nativeInstance =
                        nativeClazz.getMethod("getInstance", Context.class).invoke(null, context);
                Method m = nativeClazz.getMethod("getDeviceConfiguredVerificationsList", String.class,
                        EventResult.class);
                m.invoke(nativeInstance, sub, callback);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "cidaasnative is required for devices().verifications().fetch(...). Add project(':cidaasnative') "
                                + "(or your published cidaasnative artifact) to the consuming module.",
                        e);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw new IllegalStateException("devices().verifications().fetch delegation failed.", cause);
            }
        }
    }
}
