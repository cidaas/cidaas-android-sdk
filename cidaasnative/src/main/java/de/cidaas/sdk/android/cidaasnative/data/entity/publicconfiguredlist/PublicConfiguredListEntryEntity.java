package de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicConfiguredListEntryEntity implements Serializable {

    private String type = "";
    private List<PublicConfiguredMediumEntity> mediums;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PublicConfiguredMediumEntity> getMediums() {
        return mediums;
    }

    public void setMediums(List<PublicConfiguredMediumEntity> mediums) {
        this.mediums = mediums;
    }
}
