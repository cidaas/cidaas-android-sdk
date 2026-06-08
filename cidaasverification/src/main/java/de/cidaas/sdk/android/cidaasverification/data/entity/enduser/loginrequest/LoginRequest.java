package de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.io.Serializable;

import de.cidaas.sdk.android.entities.FingerPrintEntity;
import de.cidaas.sdk.android.helper.enums.UsageType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest implements Serializable {

    private String pass_code = "";
    /**
     * User identifier for login (e.g. username, email, or OIDC subject). Serialized as {@code identifier};
     * {@code sub} is accepted on deserialize for backward compatibility.
     */
    @JsonAlias("sub")
    private String identifier = "";

    // For face and voice
    private File fileToSend;
    private int attempt = 0;

    // For Fingerprint
    @JsonIgnore
    private FingerPrintEntity fingerPrintEntity;

    private String usageType = "";

    private String trackId = "";

    private String requestId = "";

    /**
     * Optional medium id for verification initiate (e.g. pattern login); copied to the initiate API body as
     * {@code medium_id}. Not serialized on this entity.
     */
    @JsonIgnore
    private transient String mediumId = "";

    /**
     * Host for the pattern-login modal when using {@code cidaas.verifications().login().pattern(loginRequest, callback)}.
     * Not serialized. When null, the SDK uses the {@code Context} passed to {@code Cidaas.getInstance(...)} if it is a
     * {@link FragmentActivity}.
     */
    @JsonIgnore
    private transient FragmentActivity patternLoginHostActivity;

    /** Optional dialog title for pattern login; not serialized. When null or blank, a default string resource is used. */
    @JsonIgnore
    private transient String patternLoginDialogTitle;

    /** Optional dialog message; not serialized. */
    @JsonIgnore
    private transient String patternLoginDialogMessage;

    /**
     * Optional prefix for pattern encoding before hashing (e.g. {@code RED}); not serialized.
     * When null, {@link de.cidaas.sdk.android.cidaasverification.view.pattern.PatternPasscodeFormatter} uses {@code RED}.
     */
    @JsonIgnore
    private transient String patternLoginCodePrefix;

    /** Optional {@code AlertDialog} theme resource id; not serialized. {@code 0} means default. */
    @JsonIgnore
    private transient int patternLoginDialogThemeResId;

    public LoginRequest() {
    }

    @Nullable
    public FragmentActivity getPatternLoginHostActivity() {
        return patternLoginHostActivity;
    }

    public void setPatternLoginHostActivity(@Nullable FragmentActivity patternLoginHostActivity) {
        this.patternLoginHostActivity = patternLoginHostActivity;
    }

    @Nullable
    public String getPatternLoginDialogTitle() {
        return patternLoginDialogTitle;
    }

    public void setPatternLoginDialogTitle(@Nullable String patternLoginDialogTitle) {
        this.patternLoginDialogTitle = patternLoginDialogTitle;
    }

    @Nullable
    public String getPatternLoginDialogMessage() {
        return patternLoginDialogMessage;
    }

    public void setPatternLoginDialogMessage(@Nullable String patternLoginDialogMessage) {
        this.patternLoginDialogMessage = patternLoginDialogMessage;
    }

    @Nullable
    public String getPatternLoginCodePrefix() {
        return patternLoginCodePrefix;
    }

    public void setPatternLoginCodePrefix(@Nullable String patternLoginCodePrefix) {
        this.patternLoginCodePrefix = patternLoginCodePrefix;
    }

    public int getPatternLoginDialogThemeResId() {
        return patternLoginDialogThemeResId;
    }

    public void setPatternLoginDialogThemeResId(int patternLoginDialogThemeResId) {
        this.patternLoginDialogThemeResId = patternLoginDialogThemeResId;
    }

    /**
     * Host for biometric proof when using {@code cidaas.verifications().login().fingerprint(loginRequest, callback)}.
     * Not serialized. When null, the SDK uses the {@code Cidaas} context if it is a {@link FragmentActivity}.
     */
    @JsonIgnore
    private transient FragmentActivity fingerprintLoginHostActivity;

    @Nullable
    public FragmentActivity getFingerprintLoginHostActivity() {
        return fingerprintLoginHostActivity;
    }

    public void setFingerprintLoginHostActivity(@Nullable FragmentActivity fingerprintLoginHostActivity) {
        this.fingerprintLoginHostActivity = fingerprintLoginHostActivity;
    }

    /**
     * Host for the push-login accept modal when using {@code cidaas.verifications().login().push(loginRequest, callback)}.
     * Not serialized. When null, the SDK uses the {@code Cidaas} context if it is a {@link FragmentActivity}.
     */
    @JsonIgnore
    private transient FragmentActivity pushLoginHostActivity;

    /** Optional dialog title for push login; not serialized. When null or blank, a default string resource is used. */
    @JsonIgnore
    private transient String pushLoginDialogTitle;

    /** Optional dialog message; not serialized. When null or blank, a default string resource is used. */
    @JsonIgnore
    private transient String pushLoginDialogMessage;

    /** Optional accept button label; not serialized. When null or blank, {@code Accept} is used. */
    @JsonIgnore
    private transient String pushLoginAcceptButtonText;

    /** Optional {@code AlertDialog} theme resource id for push login; not serialized. {@code 0} means default. */
    @JsonIgnore
    private transient int pushLoginDialogThemeResId;

    @Nullable
    public FragmentActivity getPushLoginHostActivity() {
        return pushLoginHostActivity;
    }

    public void setPushLoginHostActivity(@Nullable FragmentActivity pushLoginHostActivity) {
        this.pushLoginHostActivity = pushLoginHostActivity;
    }

    @Nullable
    public String getPushLoginDialogTitle() {
        return pushLoginDialogTitle;
    }

    public void setPushLoginDialogTitle(@Nullable String pushLoginDialogTitle) {
        this.pushLoginDialogTitle = pushLoginDialogTitle;
    }

    @Nullable
    public String getPushLoginDialogMessage() {
        return pushLoginDialogMessage;
    }

    public void setPushLoginDialogMessage(@Nullable String pushLoginDialogMessage) {
        this.pushLoginDialogMessage = pushLoginDialogMessage;
    }

    @Nullable
    public String getPushLoginAcceptButtonText() {
        return pushLoginAcceptButtonText;
    }

    public void setPushLoginAcceptButtonText(@Nullable String pushLoginAcceptButtonText) {
        this.pushLoginAcceptButtonText = pushLoginAcceptButtonText;
    }

    public int getPushLoginDialogThemeResId() {
        return pushLoginDialogThemeResId;
    }

    public void setPushLoginDialogThemeResId(int pushLoginDialogThemeResId) {
        this.pushLoginDialogThemeResId = pushLoginDialogThemeResId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPass_code() {
        return pass_code;
    }

    public void setPass_code(String pass_code) {
        this.pass_code = pass_code;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @deprecated Use {@link #getIdentifier()} — same value; login flows use "identifier" as the API concept.
     */
    @Deprecated
    @JsonIgnore
    public String getSub() {
        return identifier;
    }

    /**
     * @deprecated Use {@link #setIdentifier(String)}.
     */
    @Deprecated
    @JsonIgnore
    public void setSub(String sub) {
        this.identifier = sub;
    }

    public File getFileToSend() {
        return fileToSend;
    }

    public void setFileToSend(File fileToSend) {
        this.fileToSend = fileToSend;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public FingerPrintEntity getFingerPrintEntity() {
        return fingerPrintEntity;
    }

    public void setFingerPrintEntity(FingerPrintEntity fingerPrintEntity) {
        this.fingerPrintEntity = fingerPrintEntity;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    @Nullable
    public String getMediumId() {
        return mediumId;
    }

    /**
     * Sets the medium id sent on authenticate initiate (e.g. pattern). Use the id from the configured-methods list.
     */
    public void setMediumId(@Nullable String mediumId) {
        this.mediumId = mediumId;
    }

    // --- Static factories (passwordless / MFA login) — use identifier, not separate "sub" ---

    @NonNull
    public static LoginRequest getPasswordlessRequestEntity(@NonNull String identifier, @NonNull String requestId) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier(identifier);
        loginRequest.setRequestId(requestId);
        loginRequest.setUsageType(UsageType.PASSWORDLESS);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getPasswordlessEmailRequestEntity(@NonNull String identifier, @NonNull String requestId) {
        return getPasswordlessRequestEntity(identifier, requestId);
    }

    @NonNull
    public static LoginRequest getPasswordlessSMSRequestEntity(@NonNull String identifier, @NonNull String requestId) {
        return getPasswordlessRequestEntity(identifier, requestId);
    }

    @NonNull
    public static LoginRequest getPasswordlessIVRRequestEntity(@NonNull String identifier, @NonNull String requestId) {
        return getPasswordlessRequestEntity(identifier, requestId);
    }

    @NonNull
    public static LoginRequest getPasswordlessPatternLoginRequestEntity(
            @NonNull String pass_code, @NonNull String identifier, @NonNull String requestId) {
        LoginRequest loginRequest = getPasswordlessRequestEntity(identifier, requestId);
        loginRequest.setPass_code(pass_code);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getPasswordlessSmartPushLoginRequestEntity(
            @NonNull String identifier, @NonNull String requestId) {
        return getPasswordlessRequestEntity(identifier, requestId);
    }

    @NonNull
    public static LoginRequest getPasswordlessTOTPRequestEntity(@NonNull String identifier, @NonNull String requestId) {
        return getPasswordlessRequestEntity(identifier, requestId);
    }

    @NonNull
    public static LoginRequest getPasswordlessFaceLoginRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull File fileToSend) {
        LoginRequest loginRequest = getPasswordlessRequestEntity(identifier, requestId);
        loginRequest.setFileToSend(fileToSend);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getPasswordlessVoiceLoginRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull File fileToSend) {
        return getPasswordlessFaceLoginRequestEntity(identifier, requestId, fileToSend);
    }

    @NonNull
    public static LoginRequest getPasswordlessFingerprintLoginRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull FingerPrintEntity fingerPrintEntity) {
        LoginRequest loginRequest = getPasswordlessRequestEntity(identifier, requestId);
        loginRequest.setFingerPrintEntity(fingerPrintEntity);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getMFAEmailRequestEntity(@NonNull String identifier, @NonNull String requestId) {
        return getPasswordlessRequestEntity(identifier, requestId);
    }

    @NonNull
    public static LoginRequest getMFASMSRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull String trackId) {
        LoginRequest loginRequest = getPasswordlessRequestEntity(identifier, requestId);
        loginRequest.setUsageType(UsageType.MFA);
        loginRequest.setTrackId(trackId);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getMFAIVRRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull String trackId) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier(identifier);
        loginRequest.setRequestId(requestId);
        loginRequest.setUsageType(UsageType.MFA);
        loginRequest.setTrackId(trackId);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getMFAPatternLoginRequestEntity(
            @NonNull String pass_code,
            @NonNull String identifier,
            @NonNull String requestId,
            @NonNull String trackId) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPass_code(pass_code);
        loginRequest.setIdentifier(identifier);
        loginRequest.setRequestId(requestId);
        loginRequest.setUsageType(UsageType.MFA);
        loginRequest.setTrackId(trackId);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getMFASmartPushLoginRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull String trackId) {
        return getMFAIVRRequestEntity(identifier, requestId, trackId);
    }

    @NonNull
    public static LoginRequest getMFATOTPRequestEntity(
            @NonNull String identifier, @NonNull String requestId, @NonNull String trackId) {
        return getMFASmartPushLoginRequestEntity(identifier, requestId, trackId);
    }

    @NonNull
    public static LoginRequest getMFAFaceLoginRequestEntity(
            @NonNull String identifier,
            @NonNull String requestId,
            @NonNull File fileToSend,
            @NonNull String trackId) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier(identifier);
        loginRequest.setFileToSend(fileToSend);
        loginRequest.setRequestId(requestId);
        loginRequest.setUsageType(UsageType.MFA);
        loginRequest.setTrackId(trackId);
        return loginRequest;
    }

    @NonNull
    public static LoginRequest getMFAVoiceLoginRequestEntity(
            @NonNull String identifier,
            @NonNull String requestId,
            @NonNull File fileToSend,
            @NonNull String trackId) {
        return getMFAFaceLoginRequestEntity(identifier, requestId, fileToSend, trackId);
    }

    @NonNull
    public static LoginRequest getMFAFingerprintLoginRequestEntity(
            @NonNull String identifier,
            @NonNull String requestId,
            @NonNull FingerPrintEntity fingerPrintEntity,
            @NonNull String trackId) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier(identifier);
        loginRequest.setRequestId(requestId);
        loginRequest.setFingerPrintEntity(fingerPrintEntity);
        loginRequest.setUsageType(UsageType.MFA);
        loginRequest.setTrackId(trackId);
        return loginRequest;
    }
}
