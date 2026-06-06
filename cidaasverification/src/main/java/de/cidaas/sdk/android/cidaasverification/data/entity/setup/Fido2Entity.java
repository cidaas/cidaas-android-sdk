package de.cidaas.sdk.android.cidaasverification.data.entity.setup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

/**
 * FIDO2 / passkey block returned on setup initiation for {@code FIDO2}
 * verification type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fido2Entity implements Serializable {

    private String type = "";
    private String fidoRequestId = "";
    private JsonNode server_challenge;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type != null ? type : "";
    }

    public String getFidoRequestId() {
        return fidoRequestId;
    }

    public void setFidoRequestId(String fidoRequestId) {
        this.fidoRequestId = fidoRequestId != null ? fidoRequestId : "";
    }

    public JsonNode getServer_challenge() {
        return server_challenge;
    }

    public void setServer_challenge(JsonNode server_challenge) {
        this.server_challenge = server_challenge;
    }
}
