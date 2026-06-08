package de.cidaas.sdk.android.cidaasverification.data.entity.initiate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateEntity implements Serializable {

    /**
     * Login / user identifier (e.g. email). Serialized as {@code identifier}; {@code sub} is accepted when
     * deserializing for backward compatibility.
     */
    @JsonProperty("identifier")
    @JsonAlias("sub")
    private String identifier = "";
    private String request_id = "";
    private String usage_type = "";
    private String device_id = "";
    private String push_id = "";
    private String verificationType = "";

    public InitiateEntity(String identifier, String request_id, String usage_type, String verificationType) {
        this.identifier = identifier;
        this.request_id = request_id;
        this.usage_type = usage_type;
        this.verificationType = verificationType;
    }

    public InitiateEntity(String identifier, String request_id, String usage_type, String verificationType,
            String device_id, String push_id) {
        this.identifier = identifier;
        this.request_id = request_id;
        this.verificationType = verificationType;
        this.usage_type = usage_type;
        this.device_id = device_id;
        this.push_id = push_id;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @deprecated Use {@link #getIdentifier()}.
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

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getUsage_type() {
        return usage_type;
    }

    public void setUsage_type(String usage_type) {
        this.usage_type = usage_type;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }
}
