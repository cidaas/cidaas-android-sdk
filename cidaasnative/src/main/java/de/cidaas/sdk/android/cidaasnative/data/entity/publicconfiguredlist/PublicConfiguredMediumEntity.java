package de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicConfiguredMediumEntity implements Serializable {

    private String id = "";
    private String key_name = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }
}
