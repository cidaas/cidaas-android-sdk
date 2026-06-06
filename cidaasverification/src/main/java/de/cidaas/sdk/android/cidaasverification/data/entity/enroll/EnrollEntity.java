package de.cidaas.sdk.android.cidaasverification.data.entity.enroll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.Serializable;

import de.cidaas.sdk.android.entities.FingerPrintEntity;


@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrollEntity implements Serializable {

    private String exchange_id = "";
    private String device_id = "";
    private String client_id = "";
    private String push_id = "";
    private String pass_code = "";
    private String verificationType = "";
    private String sub = "";

    /** Echo of setup initiation {@code status_id} when required by the verification API (e.g. FIDO2 passkey enroll). */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String status_id = "";


    //For face and voice
    private File fileToSend;
    private int attempt = 0;

    //For Fingerprint
    @JsonIgnore
    private FingerPrintEntity fingerPrintEntity;

    /** Biometric proof JWT for fingerprint enrollment (e.g. {@code biometric+jwt} from Keystore EC P-256). */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String attestation = "";

    /** Optional; echoed from setup initiation for FIDO2 / passkey enroll. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String authenticator_client_id = "";

    /** Optional; echoed from {@link de.cidaas.sdk.android.cidaasverification.data.entity.setup.Fido2Entity#getFidoRequestId()}. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("fidoRequestId")
    private String fidoRequestId = "";

    //EmptyConstructors
    public EnrollEntity() {
    }


    //For Pattern and push
    public EnrollEntity(String exchange_id, String pass_code, String verificationType) {
        this.exchange_id = exchange_id;
        this.pass_code = pass_code;
        this.verificationType = verificationType;
    }

    //For FingerPrint
    public EnrollEntity(String exchange_id, String verificationType, FingerPrintEntity fingerPrintEntity) {
        this.exchange_id = exchange_id;
        this.verificationType = verificationType;
        this.fingerPrintEntity = fingerPrintEntity;
    }

    //For Face and Voice
    public EnrollEntity(String exchange_id, String verificationType, File fileToSend, int face_attempt) {
        this.exchange_id = exchange_id;
        this.verificationType = verificationType;
        this.fileToSend = fileToSend;
        this.attempt = face_attempt;
    }

    public File getFileToSend() {
        return fileToSend;
    }

    public void setFileToSend(File fileToSend) {
        this.fileToSend = fileToSend;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    @JsonIgnore
    public FingerPrintEntity getFingerPrintEntity() {
        return fingerPrintEntity;
    }

    @JsonIgnore
    public void setFingerPrintEntity(FingerPrintEntity fingerPrintEntity) {
        this.fingerPrintEntity = fingerPrintEntity;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
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
        this.status_id = status_id != null ? status_id : "";
    }

    public String getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(String exchange_id) {
        this.exchange_id = exchange_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    public String getPass_code() {
        return pass_code;
    }

    public void setPass_code(String pass_code) {
        this.pass_code = pass_code;
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(String attestation) {
        this.attestation = attestation != null ? attestation : "";
    }

    public String getAuthenticator_client_id() {
        return authenticator_client_id;
    }

    public void setAuthenticator_client_id(String authenticator_client_id) {
        this.authenticator_client_id = authenticator_client_id != null ? authenticator_client_id : "";
    }

    public String getFidoRequestId() {
        return fidoRequestId;
    }

    public void setFidoRequestId(String fidoRequestId) {
        this.fidoRequestId = fidoRequestId != null ? fidoRequestId : "";
    }
}
