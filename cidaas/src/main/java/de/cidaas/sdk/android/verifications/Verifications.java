package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;

/**
 * Tenant verification configuration on {@link de.cidaas.sdk.android.Cidaas}. Delegates to {@code CidaasNative}
 * at runtime; add {@code cidaasnative} to the app module.
 *
 * <pre>{@code
 * cidaas.verifications().fetch(sub, callback);
 * }</pre>
 *
 * <p>On success, the callback receives
 * {@code de.cidaas.sdk.android.cidaasnative.data.entity.verificationconfig.VerificationConfigsResponseEntity}.</p>
 */
public final class Verifications {

    private static final String NATIVE_CIDAAS_NATIVE =
            "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";

    private final Context context;

    public Verifications(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Loads the access token for {@code sub}, then calls GET {@code verification-actions-srv/config} with that token
     * in headers.
     */
    public void fetch(@NonNull String sub, @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException("Sub must not be null or empty",
                    "Verifications.fetch"));
            return;
        }
        AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
            @Override
            public void success(AccessTokenEntity accessTokenEntity) {
                String token = accessTokenEntity.getAccess_token();
                if (token == null || token.isEmpty()) {
                    callback.failure(WebAuthError.getShared(context).propertyMissingException(
                            "Access Token must not be empty", "Verifications.fetch"));
                    return;
                }
                invokeGetVerificationConfigs(token, callback);
            }

            @Override
            public void failure(WebAuthError error) {
                callback.failure(error);
            }
        });
    }

    private void invokeGetVerificationConfigs(@NonNull String accessToken, @NonNull EventResult<?> callback) {
        try {
            Class<?> nativeClazz = Class.forName(NATIVE_CIDAAS_NATIVE);
            Object nativeInstance =
                    nativeClazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = nativeClazz.getMethod("getVerificationConfigs", String.class, EventResult.class);
            m.invoke(nativeInstance, accessToken, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasnative is required for verifications().fetch(...). Add project(':cidaasnative') (or your "
                            + "published cidaasnative artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().fetch delegation failed.", cause);
        }
    }
}
