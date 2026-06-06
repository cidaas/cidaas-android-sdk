package de.cidaas.sdk.android.cidaasnative.data.entity.userconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserConfiguredVerificationsListDataEntity implements Serializable {

    private List<UserConfiguredVerificationGroupEntity> configured_list;

    public List<UserConfiguredVerificationGroupEntity> getConfigured_list() {
        return configured_list;
    }

    public void setConfigured_list(List<UserConfiguredVerificationGroupEntity> configured_list) {
        this.configured_list = configured_list;
    }
}
