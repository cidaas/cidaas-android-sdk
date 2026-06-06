package de.cidaas.sdk.android.cidaasverification.domain.controller.configrationflow.enroll;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponseDataEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.FaceMetadataEntity;
import de.cidaas.sdk.android.cidaasverification.data.entity.excangeid.ExchangeIDEntity;
import de.cidaas.sdk.android.helper.enums.HttpStatusCode;

/**
 * Interprets face enroll API payloads so the wizard can stop on full success or continue when more images are required.
 */
public final class FaceEnrollInterpreter {

    private FaceEnrollInterpreter() {
    }

    /**
     * Enrollment is finished: {@code enrolled} is true, or uploaded image count meets {@code number_images_needed},
     * or a minimal success payload with no further meta.
     */
    public static boolean isEnrollmentComplete(@Nullable EnrollResponse response) {
        if (response == null) {
            return false;
        }
        EnrollResponseDataEntity data = response.getData();
        if (data != null && Boolean.TRUE.equals(data.getEnrolled())) {
            return true;
        }
        FaceMetadataEntity meta = data != null ? data.getMeta() : null;
        if (meta != null) {
            int needed = meta.getNumber_images_needed();
            int uploaded = meta.getNumber_images_uploaded();
            if (needed > 0 && uploaded >= needed) {
                return true;
            }
        }
        if (response.isSuccess() && data == null) {
            return true;
        }
        if (response.isSuccess() && data != null && meta == null) {
            return !Boolean.FALSE.equals(data.getEnrolled());
        }
        return false;
    }

    /**
     * Backend indicates another capture is needed before enrollment completes.
     */
    public static boolean needsMoreImages(@Nullable EnrollResponse response) {
        if (response == null || isEnrollmentComplete(response)) {
            return false;
        }
        EnrollResponseDataEntity data = response.getData();
        FaceMetadataEntity meta = data != null ? data.getMeta() : null;
        if (meta != null) {
            int needed = meta.getNumber_images_needed();
            int uploaded = meta.getNumber_images_uploaded();
            if (needed > 0 && uploaded < needed) {
                return true;
            }
            if (textSuggestsMoreImages(meta)) {
                return true;
            }
        }
        return false;
    }

    /**
     * After a capture, the wizard should advance to the next step (or stay within max steps), not as terminal failure.
     * Includes HTTP 417 (Expectation Failed) from face enroll, or {@link #needsMoreImages(EnrollResponse)} from API metadata.
     */
    public static boolean shouldContinueFaceWizard(@Nullable EnrollResponse response) {
        if (response == null || isEnrollmentComplete(response)) {
            return false;
        }
        if (response.getFaceEnrollmentRawHttpCode() == HttpStatusCode.EXPECTATION_FAILED) {
            return true;
        }
        return needsMoreImages(response);
    }

    @Nullable
    public static String nextExchangeIdOrNull(@Nullable EnrollResponse response, @NonNull String currentExchangeId) {
        if (response == null || response.getData() == null) {
            return null;
        }
        ExchangeIDEntity ex = response.getData().getExchange_id();
        if (ex == null) {
            return null;
        }
        String next = ex.getExchange_id();
        if (next == null || next.isEmpty() || next.equals(currentExchangeId)) {
            return null;
        }
        return next;
    }

    @Nullable
    public static String userVisibleHint(@Nullable EnrollResponse response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        FaceMetadataEntity meta = response.getData().getMeta();
        if (meta == null) {
            return null;
        }
        String c = meta.getComment();
        if (c != null && !c.trim().isEmpty()) {
            return c.trim();
        }
        String e = meta.getError();
        if (e != null && !e.trim().isEmpty()) {
            return e.trim();
        }
        return null;
    }

    private static boolean textSuggestsMoreImages(@NonNull FaceMetadataEntity meta) {
        String t = (safe(meta.getComment()) + " " + safe(meta.getError())).toLowerCase();
        if (t.isEmpty()) {
            return false;
        }
        if (t.contains("more") && (t.contains("image") || t.contains("photo") || t.contains("picture"))) {
            return true;
        }
        if (t.contains("need") && (t.contains("image") || t.contains("photo") || t.contains("picture"))) {
            return true;
        }
        if (t.contains("add") && (t.contains("image") || t.contains("photo"))) {
            return true;
        }
        return false;
    }

    @NonNull
    private static String safe(@Nullable String s) {
        return s != null ? s : "";
    }
}
