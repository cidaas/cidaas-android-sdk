package de.cidaas.sdk.android.cidaasnative.data.entity.accountverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyAccountResponseEntity implements Serializable {
    boolean success = false;
    int status = 0;
    VerifyAccountResponseDataEntity data;

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

    public VerifyAccountResponseDataEntity getData() {
        return data;
    }

    public void setData(VerifyAccountResponseDataEntity data) {
        this.data = data;
    }
}
