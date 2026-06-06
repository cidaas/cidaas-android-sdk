package de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Request body for POST {@code verification-srv/v2/setup/device/configured/list} (same shape as
 * {@code GetMFAListEntity} in {@code cidaasverification}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfiguredVerificationsListRequestEntity implements Serializable {

    private String device_id = "";
    private String push_id = "";
    private String client_id = "";
    private String sub = "";
    private String linked_device_id = "";

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

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getLinked_device_id() {
        return linked_device_id;
    }

    public void setLinked_device_id(String linked_device_id) {
        this.linked_device_id = linked_device_id;
    }
}
