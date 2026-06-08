package de.cidaas.sdk.android.cidaasnative.data.entity.publicconfiguredlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Request body for POST {@code /verification-srv/v2/setup/public/configured/list}.
 * {@code identifier} is the user handle (e.g. email); {@code push_id} is the FCM registration token from
 * {@link de.cidaas.sdk.android.Cidaas#registerFCM(String)} / {@link de.cidaas.sdk.android.helper.general.DBHelper}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicConfiguredListRequestEntity implements Serializable {

    private String request_id = "";
    private String identifier = "";
    private String push_id = "";
    private String client_id = "";

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }
}
