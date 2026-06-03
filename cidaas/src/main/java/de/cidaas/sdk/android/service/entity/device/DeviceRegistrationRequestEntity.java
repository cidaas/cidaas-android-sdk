package de.cidaas.sdk.android.service.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceRegistrationRequestEntity implements Serializable {

    @JsonProperty("device_id")
    private String deviceId;
    @JsonProperty("push_id")
    private String pushId;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("platform")
    private String platform;

    public DeviceRegistrationRequestEntity() {
    }

    public DeviceRegistrationRequestEntity(String deviceId, String pushId, String clientId) {
        this.deviceId = deviceId;
        this.pushId = pushId;
        this.clientId = clientId;
        this.platform = "android";
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
