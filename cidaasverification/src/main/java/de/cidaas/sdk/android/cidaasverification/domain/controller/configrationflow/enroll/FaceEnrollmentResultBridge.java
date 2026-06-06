package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import androidx.annotation.Nullable;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.helper.enums.EventResult;

/**
 * Holds the {@link EventResult} for an in-flight face enrollment wizard (single active flow).
 */
public final class FaceEnrollmentResultBridge {

    @Nullable
    private static volatile EventResult<EnrollResponse> pending;

    private FaceEnrollmentResultBridge() {
    }

    public static void setPending(@Nullable EventResult<EnrollResponse> callback) {
        pending = callback;
    }

    @Nullable
    public static EventResult<EnrollResponse> consumePending() {
        EventResult<EnrollResponse> c = pending;
        pending = null;
        return c;
    }

    @Nullable
    public static EventResult<EnrollResponse> peekPending() {
        return pending;
    }
}
