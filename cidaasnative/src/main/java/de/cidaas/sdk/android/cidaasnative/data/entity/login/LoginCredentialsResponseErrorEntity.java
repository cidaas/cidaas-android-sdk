package de.cidaas.sdk.android.cidaasnative.data.entity.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginCredentialsResponseErrorEntity implements Serializable {
    private boolean success;
    private int status;
    private LoginCredentialsErrorDataEntity error;

    public LoginCredentialsErrorDataEntity getError() {
        return error;
    }

    public void setError(LoginCredentialsErrorDataEntity error) {
        this.error = error;
    }

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

}
