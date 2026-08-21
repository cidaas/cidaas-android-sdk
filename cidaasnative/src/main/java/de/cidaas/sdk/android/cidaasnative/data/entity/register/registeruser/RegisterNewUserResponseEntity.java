package de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterNewUserResponseEntity implements Serializable {
    boolean success;
    int status;
    RegisterNewUserDataEntity data;
    /**
     * Set by the SDK when tenant auto-login after register succeeds ({@code action=AUTO_LOGIN}).
     * Not part of the register JSON payload.
     */
    @JsonIgnore
    AccessTokenEntity accessToken;

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

    public AccessTokenEntity getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessTokenEntity accessToken) {
        this.accessToken = accessToken;
    }
}
