package de.cidaas.sdk.android.device;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Device-scoped verification APIs from {@link Cidaas#devices()}.
 *
 * <pre>{@code
 * cidaas.devices().verifications().fetch(sub, callback);
 * cidaas.devices().verifications().remove(sub, verificationType, callback);
 * }</pre>
 *
 * <p>On success, the callback receives
 * {@code de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification.DeviceConfiguredVerificationsListResponseEntity}.</p>
 */
public final class Devices {

    private static final String NATIVE_CIDAAS_NATIVE =
            "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    private static final String DELETE_ENTITY =
            "de.cidaas.sdk.android.cidaasverification.data.entity.delete.DeleteEntity";

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

        /**
         * Deletes one configured verification method for the current device: fills device id, push id, and client id
         * then calls the verification delete endpoint (same contract as {@code CidaasVerification#delete} /
         * {@code DeleteController#deleteVerification}). Requires {@code cidaasverification} on the classpath.
         *
         * <p>On success, {@code callback.success(...)} receives
         * {@code de.cidaas.sdk.android.cidaasverification.data.entity.delete.DeleteResponse}.</p>
         *
         * @param verificationType server verification type (for example values aligned with
         *                         {@linkplain de.cidaas.sdk.android.helper.AuthenticationType AuthenticationType})
         */
        public void remove(@NonNull String sub, @NonNull String verificationType,
                @NonNull EventResult<?> callback) {
            if (sub == null || sub.isEmpty()) {
                callback.failure(WebAuthError.getShared(cidaas.context).propertyMissingException(
                        "Sub must not be null or empty", "Devices.verifications().remove"));
                return;
            }
            if (verificationType == null || verificationType.isEmpty()) {
                callback.failure(WebAuthError.getShared(cidaas.context).propertyMissingException(
                        "Verification type must not be null or empty", "Devices.verifications().remove"));
                return;
            }
            invokeDeleteVerification(sub, verificationType, callback);
        }

        private void invokeDeleteVerification(@NonNull String sub, @NonNull String verificationType,
                @NonNull EventResult<?> callback) {
            Context context = cidaas.context;
            try {
                Class<?> deleteEntityClass = Class.forName(DELETE_ENTITY);
                Constructor<?> deleteEntityCtor = deleteEntityClass.getConstructor(String.class, String.class);
                Object deleteEntity = deleteEntityCtor.newInstance(sub, verificationType);

                Class<?> verificationClazz = Class.forName(CIDAAS_VERIFICATION);
                Object verificationInstance =
                        verificationClazz.getMethod("getInstance", Context.class).invoke(null, context);
                Method deleteMethod =
                        verificationClazz.getMethod("delete", deleteEntityClass, EventResult.class);
                deleteMethod.invoke(verificationInstance, deleteEntity, callback);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "cidaasverification is required for devices().verifications().remove(...). Add "
                                + "project(':cidaasverification') (or your published cidaasverification artifact) "
                                + "to the consuming module.",
                        e);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw new IllegalStateException("devices().verifications().remove delegation failed.", cause);
            }
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
