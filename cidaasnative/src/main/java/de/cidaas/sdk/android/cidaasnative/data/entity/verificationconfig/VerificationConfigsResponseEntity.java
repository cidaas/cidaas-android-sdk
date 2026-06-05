package de.cidaas.sdk.android.cidaasnative.data.entity.verificationconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationConfigsResponseEntity implements Serializable {

    private boolean success;
    private int status;
    private List<VerificationConfigEntity> data;

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

    public List<VerificationConfigEntity> getData() {
        return data;
    }

    public void setData(List<VerificationConfigEntity> data) {
        this.data = data;
    }
}
