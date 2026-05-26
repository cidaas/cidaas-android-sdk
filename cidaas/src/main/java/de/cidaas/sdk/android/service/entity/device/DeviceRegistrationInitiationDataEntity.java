package de.cidaas.sdk.android.service.entity.device;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceRegistrationInitiationDataEntity implements Serializable {

    /** Server session id for the verification call (JSON may use {@code session_id} or {@code sessionId}). */
    @JsonProperty("session_id")
    @JsonAlias("sessionId")
    private String sessionId;
    @JsonProperty("nonce")
    private String nonce;
    /** When set, selects attestation after initiation: {@code google} or {@code firebase}. */
    @JsonProperty("provider")
    private String provider;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
