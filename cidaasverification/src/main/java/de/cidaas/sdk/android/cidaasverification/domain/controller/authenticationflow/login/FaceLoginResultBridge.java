package de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.login;

import androidx.annotation.Nullable;

import de.cidaas.sdk.android.cidaasverification.data.entity.authenticate.AuthenticateResponse;
import de.cidaas.sdk.android.helper.enums.EventResult;

/**
 * Holds the {@link EventResult} for an in-flight face login capture (single active flow).
 */
public final class FaceLoginResultBridge {

    @Nullable
    private static volatile EventResult<AuthenticateResponse> pending;

    private FaceLoginResultBridge() {
    }

    public static void setPending(@Nullable EventResult<AuthenticateResponse> callback) {
        pending = callback;
    }

    @Nullable
    public static EventResult<AuthenticateResponse> consumePending() {
        EventResult<AuthenticateResponse> c = pending;
        pending = null;
        return c;
    }

    @Nullable
    public static EventResult<AuthenticateResponse> peekPending() {
        return pending;
    }
}
