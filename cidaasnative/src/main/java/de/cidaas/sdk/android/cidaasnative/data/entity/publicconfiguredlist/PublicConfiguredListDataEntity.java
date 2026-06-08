package de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicConfiguredListDataEntity implements Serializable {

    private List<PublicConfiguredListEntryEntity> configured_list;

    public List<PublicConfiguredListEntryEntity> getConfigured_list() {
        return configured_list;
    }

    public void setConfigured_list(List<PublicConfiguredListEntryEntity> configured_list) {
        this.configured_list = configured_list;
    }
}
