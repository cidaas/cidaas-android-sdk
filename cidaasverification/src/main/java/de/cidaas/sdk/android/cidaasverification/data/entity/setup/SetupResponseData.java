package de.cidaas.sdk.android.cidaasverification.data.entity.setup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import de.cidaas.sdk.android.cidaasverification.data.entity.enroll.BackupCodeData;
import de.cidaas.sdk.android.cidaasverification.data.entity.excangeid.ExchangeIDEntity;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SetupResponseData implements Serializable {
    private ExchangeIDEntity exchange_id;
    private String sub = "";
    private String status_id = "";
    private String authenticator_client_id;
    private String push_selected_number = "";
    private String totp_secret = "";

    private Fido2Entity fido2_entity;

    BackupCodeData backup_codes;

    public BackupCodeData getBackup_codes() {
        return backup_codes;
    }

    public void setBackup_codes(BackupCodeData backup_codes) {
        this.backup_codes = backup_codes;
    }

    public ExchangeIDEntity getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(ExchangeIDEntity exchange_id) {
        this.exchange_id = exchange_id;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getAuthenticator_client_id() {
        return authenticator_client_id;
    }

    public void setAuthenticator_client_id(String authenticator_client_id) {
        this.authenticator_client_id = authenticator_client_id;
    }

    public String getPush_selected_number() {
        return push_selected_number;
    }

    public void setPush_selected_number(String push_selected_number) {
        this.push_selected_number = push_selected_number;
    }

    public String getTotp_secret() {
        return totp_secret;
    }

    public void setTotp_secret(String totp_secret) {
        this.totp_secret = totp_secret;
    }

    public Fido2Entity getFido2_entity() {
        return fido2_entity;
    }

    public void setFido2_entity(Fido2Entity fido2_entity) {
        this.fido2_entity = fido2_entity;
    }
}
