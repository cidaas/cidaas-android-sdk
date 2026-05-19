package de.cidaas.sdk.android.service.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceRegistrationVerificationRequestEntity implements Serializable {

    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("attestation")
    private String attestation;
    @JsonProperty("dpop_jwk_thumbprint")
    private String dpopJwkThumbprint;
    @JsonProperty("biometric_jwk_thumbprint")
    private String biometricJwkThumbprint;
    @JsonProperty("app_version")
    private String appVersion;
    @JsonProperty("platform")
    private String platform;

    public DeviceRegistrationVerificationRequestEntity() {
    }

    public DeviceRegistrationVerificationRequestEntity(String sessionId,
                                                       String attestation,
                                                       String dpopJwkThumbprint,
                                                       String biometricJwkThumbprint,
                                                       String appVersion,
                                                       String platform) {
        this.sessionId = sessionId;
        this.attestation = attestation;
        this.dpopJwkThumbprint = dpopJwkThumbprint;
        this.biometricJwkThumbprint = biometricJwkThumbprint;
        this.appVersion = appVersion;
        this.platform = platform;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(String attestation) {
        this.attestation = attestation;
    }

    public String getDpopJwkThumbprint() {
        return dpopJwkThumbprint;
    }

    public void setDpopJwkThumbprint(String dpopJwkThumbprint) {
        this.dpopJwkThumbprint = dpopJwkThumbprint;
    }

    public String getBiometricJwkThumbprint() {
        return biometricJwkThumbprint;
    }

    public void setBiometricJwkThumbprint(String biometricJwkThumbprint) {
        this.biometricJwkThumbprint = biometricJwkThumbprint;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
