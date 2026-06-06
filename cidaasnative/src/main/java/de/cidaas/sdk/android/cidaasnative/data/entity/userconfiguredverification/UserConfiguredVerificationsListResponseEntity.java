package de.cidaas.sdk.android.cidaasnative.data.entity.userconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserConfiguredVerificationsListResponseEntity implements Serializable {

    private boolean success;
    private int status;
    private UserConfiguredVerificationsListDataEntity data;

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

    public UserConfiguredVerificationsListDataEntity getData() {
        return data;
    }

    public void setData(UserConfiguredVerificationsListDataEntity data) {
        this.data = data;
    }
}
