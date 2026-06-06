package de.cidaas.sdk.android.cidaasnative.data.entity.userconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserConfiguredVerificationGroupEntity implements Serializable {

    private String type;
    private List<UserConfiguredVerificationMediumEntity> mediums;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<UserConfiguredVerificationMediumEntity> getMediums() {
        return mediums;
    }

    public void setMediums(List<UserConfiguredVerificationMediumEntity> mediums) {
        this.mediums = mediums;
    }
}
