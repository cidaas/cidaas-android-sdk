package de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicConfiguredListResponseEntity implements Serializable {

    private boolean success;
    private int status;
    private PublicConfiguredListDataEntity data;

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

    public PublicConfiguredListDataEntity getData() {
        return data;
    }

    public void setData(PublicConfiguredListDataEntity data) {
        this.data = data;
    }
}
