package de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfiguredVerificationEntryEntity implements Serializable {

    private String _id = "";
    private String verification_type = "";
    private String configured_at = "";
    private String totp_secret = "";

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getVerification_type() {
        return verification_type;
    }

    public void setVerification_type(String verification_type) {
        this.verification_type = verification_type;
    }

    public String getConfigured_at() {
        return configured_at;
    }

    public void setConfigured_at(String configured_at) {
        this.configured_at = configured_at;
    }

    public String getTotp_secret() {
        return totp_secret;
    }

    public void setTotp_secret(String totp_secret) {
        this.totp_secret = totp_secret;
    }
}
