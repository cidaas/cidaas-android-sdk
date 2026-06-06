package de.cidaas.sdk.android.cidaasnative.data.entity.deviceconfiguredverification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfiguredVerificationsListDataEntity implements Serializable {

    private String tenant_name = "";
    private String tenant_key = "";
    private List<DeviceConfiguredVerificationEntryEntity> configured_list;

    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public String getTenant_key() {
        return tenant_key;
    }

    public void setTenant_key(String tenant_key) {
        this.tenant_key = tenant_key;
    }

    public List<DeviceConfiguredVerificationEntryEntity> getConfigured_list() {
        return configured_list;
    }

    public void setConfigured_list(List<DeviceConfiguredVerificationEntryEntity> configured_list) {
        this.configured_list = configured_list;
    }
}
