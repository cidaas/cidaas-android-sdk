package de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterNewUserResponseEntity implements Serializable {
    boolean success;
    int status;
    RegisterNewUserDataEntity data;

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

    public RegisterNewUserDataEntity getData() {
        return data;
    }

    public void setData(RegisterNewUserDataEntity data) {
        this.data = data;
    }
}
