package de.cidaas.sdk.android.verifications;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Login-time verification flows from {@link Verifications#login()}.
 */
public final class VerificationLogin {

    private final Context context;

    VerificationLogin(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
    }

    /**
     * OTP login (SMS, email, IVR, chat, TOTP): {@link VerificationLoginOtp#initiate}, {@link VerificationLoginOtp#verify},
     * then {@link VerificationLoginOtp#continueLogin}.
     */
    @NonNull
    public VerificationLoginOtp otp() {
        return new VerificationLoginOtp(context);
    }
}
