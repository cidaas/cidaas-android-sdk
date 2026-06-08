package de.cidaas.sdk.android.cidaasverification.data.entity.enduser.loginrequest;

import androidx.annotation.NonNull;

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

    public LoginRequest() {
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
