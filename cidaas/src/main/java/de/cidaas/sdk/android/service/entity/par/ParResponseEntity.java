package de.cidaas.sdk.android.service.entity.par;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Response from {@code POST /authz-srv/par} (RFC 9126).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParResponseEntity implements Serializable {

    @JsonProperty("request_uri")
    private String requestUri;

    @JsonProperty("expires_in")
    private int expiresIn;

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
