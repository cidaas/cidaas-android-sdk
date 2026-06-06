package de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfiguredVerificationsListResponseEntity implements Serializable {

    private boolean success;
    private int status;
    private DeviceConfiguredVerificationsListDataEntity data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DeviceConfiguredVerificationsListDataEntity getData() {
        return data;
    }

    public void setData(DeviceConfiguredVerificationsListDataEntity data) {
        this.data = data;
    }
}
