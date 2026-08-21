package de.cidaas.sdk.android.cidaasnative.data.entity.accountverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyAccountResponseDataEntity implements Serializable {
    String trackId;
    String suggested_action;

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getSuggested_action() {
        return suggested_action;
    }

    public void setSuggested_action(String suggested_action) {
        this.suggested_action = suggested_action;
    }
}
