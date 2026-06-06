package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.helper.AuthenticationType;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;

/**
 * OTP-based MFA enrollment (SMS, email, IVR, chat): two API steps only — no scan step.
 * <ol>
 *   <li>{@link #initiate(String, AcceptMethod, EventResult)} — POST
 *   {@code /verification-actions-srv/setup/&lt;channel&gt;/initiation} (sends OTP).</li>
 *   <li>{@link #verify(String, String, String, AcceptMethod, EventResult)} — POST
 *   {@code /verification-actions-srv/setup/&lt;channel&gt;/verification} with the OTP as {@code pass_code}.</li>
 * </ol>
 *
 * <p>On {@link #initiate} success, {@code callback.success(...)} receives
 * {@code de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse} (use {@code data.exchange_id} for
 * step 2). On {@link #verify} success, the callback receives
 * {@code de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse}.</p>
 */
public final class VerificationEnrolmentOtp {

    private static final String CIDAAS_VERIFICATION =
            "de.cidaas.sdk.android.cidaasverification.view.CidaasVerification";

    /**
     * Channel for OTP enrollment; maps to {@link AuthenticationType} and the setup URL segment
     * ({@code sms}, {@code email}, {@code ivr}, {@code chat}).
     */
    public enum AcceptMethod {
        SMS(AuthenticationType.SMS),
        EMAIL(AuthenticationType.EMAIL),
        IVR(AuthenticationType.IVR),
        CHAT(AuthenticationType.CHAT);

        private final String verificationType;

        AcceptMethod(@NonNull String verificationType) {
            this.verificationType = verificationType;
        }

        @NonNull
        public String verificationType() {
            return verificationType;
        }
    }

    private final Context context;

    VerificationEnrolmentOtp(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * Sends the enrollment OTP for the chosen channel.
     */
    public void initiate(
            @NonNull String sub,
            @NonNull AcceptMethod accept,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolmentOtp.initiate"));
            return;
        }
        if (accept == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "accept must not be null", "VerificationEnrolmentOtp.initiate"));
            return;
        }
        invokeEnrollOtpInitiate(sub, accept.verificationType(), callback);
    }

    /**
     * Completes enrollment by verifying the OTP from step 1.
     */
    public void verify(
            @NonNull String otp,
            @NonNull String sub,
            @NonNull String exchangeId,
            @NonNull AcceptMethod accept,
            @NonNull EventResult<?> callback) {
        if (sub == null || sub.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "Sub must not be null or empty", "VerificationEnrolmentOtp.verify"));
            return;
        }
        if (exchangeId == null || exchangeId.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "exchangeId must not be null or empty", "VerificationEnrolmentOtp.verify"));
            return;
        }
        if (otp == null || otp.isEmpty()) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "otp must not be null or empty", "VerificationEnrolmentOtp.verify"));
            return;
        }
        if (accept == null) {
            callback.failure(WebAuthError.getShared(context).propertyMissingException(
                    "accept must not be null", "VerificationEnrolmentOtp.verify"));
            return;
        }
        invokeEnrollOtpVerify(otp, sub, exchangeId, accept.verificationType(), callback);
    }

    private void invokeEnrollOtpInitiate(
            @NonNull String sub,
            @NonNull String verificationType,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrollOtpInitiate",
                    String.class,
                    String.class,
                    EventResult.class);
            m.invoke(inst, sub, verificationType, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().otp().initiate(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().otp().initiate delegation failed.", cause);
        }
    }

    private void invokeEnrollOtpVerify(
            @NonNull String otp,
            @NonNull String sub,
            @NonNull String exchangeId,
            @NonNull String verificationType,
            @NonNull EventResult<?> callback) {
        try {
            Class<?> clazz = Class.forName(CIDAAS_VERIFICATION);
            Object inst = clazz.getMethod("getInstance", Context.class).invoke(null, context);
            Method m = clazz.getMethod(
                    "enrollOtpVerify",
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    EventResult.class);
            m.invoke(inst, otp, sub, exchangeId, verificationType, callback);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "cidaasverification is required for verifications().enrolment().otp().verify(...). Add "
                            + "project(':cidaasverification') (or your published cidaasverification artifact) to the consuming module.",
                    e);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("verifications().enrolment().otp().verify delegation failed.", cause);
        }
    }
}
