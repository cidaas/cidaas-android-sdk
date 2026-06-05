package de.cidaas.sdk.android.cidaasnative.data.entity.verificationconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationConfigEntity implements Serializable {

    private String _id;
    private String verificationType;
    private boolean active;
    private String owner;
    private String createdTime;
    private String updatedTime;
    private String icon;
    private String category;
    private String factor;
    private Map<String, Integer> allowedMFAOptions;
    private boolean requiresAuthenticatorApp;
    private String name;
    private String description;
    private VerificationCodeSettingsEntity code;
    private String url;
    private String auth_type;
    private String prerequisite;
    private CidaasAuthDetailsEntity cidaasAuthDetails;
    private String algorithmTypeId;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public Map<String, Integer> getAllowedMFAOptions() {
        return allowedMFAOptions;
    }

    public void setAllowedMFAOptions(Map<String, Integer> allowedMFAOptions) {
        this.allowedMFAOptions = allowedMFAOptions;
    }

    public boolean isRequiresAuthenticatorApp() {
        return requiresAuthenticatorApp;
    }

    public void setRequiresAuthenticatorApp(boolean requiresAuthenticatorApp) {
        this.requiresAuthenticatorApp = requiresAuthenticatorApp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VerificationCodeSettingsEntity getCode() {
        return code;
    }

    public void setCode(VerificationCodeSettingsEntity code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuth_type() {
        return auth_type;
    }

    public void setAuth_type(String auth_type) {
        this.auth_type = auth_type;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public void setPrerequisite(String prerequisite) {
        this.prerequisite = prerequisite;
    }

    public CidaasAuthDetailsEntity getCidaasAuthDetails() {
        return cidaasAuthDetails;
    }

    public void setCidaasAuthDetails(CidaasAuthDetailsEntity cidaasAuthDetails) {
        this.cidaasAuthDetails = cidaasAuthDetails;
    }

    public String getAlgorithmTypeId() {
        return algorithmTypeId;
    }

    public void setAlgorithmTypeId(String algorithmTypeId) {
        this.algorithmTypeId = algorithmTypeId;
    }
}
