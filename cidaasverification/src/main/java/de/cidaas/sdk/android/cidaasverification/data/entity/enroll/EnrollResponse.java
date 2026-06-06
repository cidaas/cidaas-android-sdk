package de.cidaas.sdk.android.cidaasverification.data.entity.enroll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrollResponse implements Serializable {
    boolean success;
    int status;
    EnrollResponseDataEntity data;

    /**
     * Set only client-side when mapping HTTP 417 from face enroll to a non-error continuation; not sent on the wire.
     */
    @JsonIgnore
    private transient int faceEnrollmentRawHttpCode;

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

    public EnrollResponseDataEntity getData() {
        return data;
    }

    public void setData(EnrollResponseDataEntity data) {
        this.data = data;
    }


    EnrollResponseDataErrorEntity errordata;

    public EnrollResponseDataErrorEntity getErrordata() {
        return errordata;
    }

    public void setErrordata(EnrollResponseDataErrorEntity errordata) {
        this.errordata = errordata;
    }

    public int getFaceEnrollmentRawHttpCode() {
        return faceEnrollmentRawHttpCode;
    }

    public void setFaceEnrollmentRawHttpCode(int faceEnrollmentRawHttpCode) {
        this.faceEnrollmentRawHttpCode = faceEnrollmentRawHttpCode;
    }
}
