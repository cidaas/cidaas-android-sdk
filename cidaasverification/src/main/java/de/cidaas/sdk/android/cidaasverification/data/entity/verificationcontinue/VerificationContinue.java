package de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import de.cidaas.sdk.android.helper.enums.UsageType;


@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationContinue implements Serializable {
    private String requestId;
    private String sub;
    private String status_id;
    private String verificationType;
    private String trackId;
    private String usageType;

    private String device_id = "";
    private String push_id = "";

    /** When true, {@link de.cidaas.sdk.android.cidaasverification.domain.controller.authenticationflow.verificationcontinue.VerificationContinueController} uses {@code /login-srv/verification/login}. */
    @JsonIgnore
    private boolean useVerificationLoginPath;

    public VerificationContinue() {
    }

    @JsonIgnore
    public boolean isUseVerificationLoginPath() {
        return useVerificationLoginPath;
    }

    @JsonIgnore
    public void setUseVerificationLoginPath(boolean useVerificationLoginPath) {
        this.useVerificationLoginPath = useVerificationLoginPath;
    }


    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }


    public static VerificationContinue getVerificationContinueEntity(String requestId, String sub, String trackId, String status_id, String verificationType) {
        VerificationContinue verificationContinue = new VerificationContinue();
        verificationContinue.setRequestId(requestId);
        verificationContinue.setSub(sub);
        verificationContinue.setStatus_id(status_id);
        verificationContinue.setTrackId(trackId);
        verificationContinue.setVerificationType(verificationType);
        verificationContinue.setUsageType(UsageType.MFA);
        return verificationContinue;

    }

    public static VerificationContinue getVerificationContinuePasswordlessEntity(String requestId, String sub, String status_id, String verificationType) {
        VerificationContinue verificationContinue = new VerificationContinue();
        verificationContinue.setRequestId(requestId);
        verificationContinue.setSub(sub);
        verificationContinue.setStatus_id(status_id);
        verificationContinue.setVerificationType(verificationType);
        verificationContinue.setUsageType(UsageType.PASSWORDLESS);
        return verificationContinue;

    }

}
