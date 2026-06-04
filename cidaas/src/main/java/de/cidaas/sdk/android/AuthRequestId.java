package de.cidaas.sdk.android;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.cidaas.sdk.android.helper.enums.EventResult;

/**
 * Fetches an auth {@code requestId} via {@code CidaasNative.getRequestId} (reflection). Obtain from
 * {@link Cidaas#requestId()} or {@link de.cidaas.sdk.android.browser.WebAuth#requestId()}; complete with
 * {@link #fetch(EventResult)}.
 *
 * <p>Callback success receives
 * {@code de.cidaas.sdk.android.cidaasnative.data.entity.authrequest.AuthRequestResponseEntity} at runtime; add
 * {@code cidaasnative} to your app module.</p>
 */
public final class AuthRequestId {

    private static final String NATIVE_CIDAAS_NATIVE =
            "de.cidaas.sdk.android.cidaasnative.view.CidaasNative";

    private final Cidaas cidaas;
    private Map<String, String> extraParams;

    public AuthRequestId(@NonNull Cidaas cidaas, @Nullable Map<String, String> extraParams) {
        this.cidaas = cidaas;
        this.extraParams = extraParams;
    }

    @NonNull
    public AuthRequestId extraParams(@Nullable Map<String, String> extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    /**
     * Calls {@code CidaasNative.getRequestId(EventResult, HashMap...)} using {@link Cidaas#context}.
     */
    @SuppressWarnings("unchecked")
    public void fetch(@NonNull EventResult<?> callback) {
        try {
            Class<?> nativeClazz = Class.forName(NATIVE_CIDAAS_NATIVE);
            Object nativeInstance =
                    nativeClazz.getMethod("getInstance", Context.class).invoke(null, cidaas.context);
            Method m = nativeClazz.getMethod("getRequestId", EventResult.class, HashMap[].class);
            HashMap<String, String>[] varargs = toRequestIdVarargs(extraParams);
            m.invoke(nativeInstance, callback, varargs);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasnative is required for requestId().fetch(...). Add project(':cidaasnative') (or your "
                            + "published cidaasnative artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("requestId().fetch delegation failed.", cause);
        }
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, String>[] toRequestIdVarargs(@Nullable Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap[0];
        }
        return new HashMap[] { new HashMap<>(map) };
    }
}
