package de.cidaas.sdk.android.publicapi;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * Unauthenticated (public) tenant APIs from {@link Cidaas#getPublic()}.
 *
 * <p>In Java the entry point is {@link Cidaas#getPublic()} because {@code public} is a reserved keyword.</p>
 *
 * <pre>{@code
 * cidaas.getPublic().verifications().fetch(requestId, identifier, callback);
 * }</pre>
 *
 * <p>On success, {@code callback.success(...)} receives
 * {@code de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist.PublicConfiguredListResponseEntity}
 * (requires {@code cidaasnative} on the classpath).</p>
 */
public final class CidaasPublic {

    private static final String NATIVE_CIDAAS_NATIVE =
            "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";

    private final Cidaas cidaas;

    public CidaasPublic(@NonNull Cidaas cidaas) {
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
     * Public verification setup list (no access token).
     */
    public static final class Verifications {

        private final Cidaas cidaas;

        Verifications(@NonNull Cidaas cidaas) {
            this.cidaas = cidaas;
        }

        /**
         * POST {@code /verification-srv/v2/setup/public/configured/list}. Sends {@code identifier}, {@code request_id},
         * and {@code client_id}; {@code push_id} is the FCM token from {@link Cidaas#registerFCM(String)}.
         */
        public void fetch(@NonNull String requestId, @NonNull String identifier, @NonNull EventResult<?> callback) {
            if (requestId == null || requestId.isEmpty()) {
                callback.failure(WebAuthError.getShared(cidaas.context).propertyMissingException(
                        "requestId must not be null or empty", "CidaasPublic.Verifications.fetch"));
                return;
            }
            if (identifier == null || identifier.isEmpty()) {
                callback.failure(WebAuthError.getShared(cidaas.context).propertyMissingException(
                        "identifier must not be null or empty", "CidaasPublic.Verifications.fetch"));
                return;
            }
            invokeGetPublicConfiguredList(requestId, identifier, callback);
        }

        private void invokeGetPublicConfiguredList(@NonNull String requestId, @NonNull String identifier,
                @NonNull EventResult<?> callback) {
            Context context = cidaas.context;
            try {
                Class<?> nativeClazz = Class.forName(NATIVE_CIDAAS_NATIVE);
                Object nativeInstance =
                        nativeClazz.getMethod("getInstance", Context.class).invoke(null, context);
                Method m = nativeClazz.getMethod("getPublicConfiguredVerificationsList", String.class, String.class,
                        EventResult.class);
                m.invoke(nativeInstance, requestId, identifier, callback);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "cidaasnative is required for getPublic().verifications().fetch(...). Add project(':cidaasnative') "
                                + "(or your published cidaasnative artifact) to the consuming module.",
                        e);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                throw new IllegalStateException("getPublic().verifications().fetch delegation failed.", cause);
            }
        }
    }
}
