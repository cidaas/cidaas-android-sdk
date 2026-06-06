package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import de.cidaas.sdk.android.controller.AccessTokenController;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;

/**
 * Tenant verification configuration and enrollment on {@link de.cidaas.sdk.android.Cidaas}.
 * {@code fetch} delegates to {@code cidaasnative}; {@code enrolment().fingerprint} delegates to {@code cidaasverification}.
 *
 * <pre>{@code
 * cidaas.verifications().fetch(sub, callback);
 * cidaas.verifications().enrolment().fingerprint(activity, sub, callback);
 * cidaas.verifications().enrolment().push(activity, sub, dialogTitle, dialogMessage, R.style.MyPushDialog, callback);
 * cidaas.verifications().enrolment().pattern(activity, sub, dialogTitle, dialogMessage, R.style.MyPatternDialog, callback);
 * cidaas.verifications().enrolment().face(activity, sub, dialogTitle, dialogMessage, R.style.MyFaceDialog, callback);
 * cidaas.verifications().enrolment().otp().initiate(sub, VerificationEnrolmentOtp.AcceptMethod.SMS, initiateCb);
 * cidaas.verifications().enrolment().otp().verify(otp, sub, exchangeId, VerificationEnrolmentOtp.AcceptMethod.SMS, verifyCb);
 * }</pre>
 *
 * <p>For {@code fetch}, on success the callback receives
 * {@code de.cidaas.sdk.android.cidaasnative.data.entity.verificationconfig.VerificationConfigsResponseEntity}.
 * For {@code enrolment().fingerprint}, {@code enrolment().push}, {@code enrolment().pattern}, or {@code enrolment().face},
 * on success the callback receives
 * {@code de.cidaas.sdk.android.cidaasverification.data.entity.enroll.EnrollResponse}.
 * For {@code enrolment().otp().initiate}, on success the callback receives
 * {@code de.cidaas.sdk.android.cidaasverification.data.entity.setup.SetupResponse};
 * for {@code enrolment().otp().verify}, {@code EnrollResponse}.</p>
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
     * Verification MFA enrollment (fingerprint, push, …); delegates to {@code cidaasverification} at runtime.
     */
    @NonNull
    public VerificationEnrolment enrolment() {
        return new VerificationEnrolment(context);
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
