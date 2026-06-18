package de.cidaas.sdk.android.cidaasverification.data.entity.authenticate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

import de.cidaas.sdk.android.entities.FingerPrintEntity;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateEntity implements Serializable {

    private String exchange_id = "";
    private String device_id = "";
    private String push_id = "";
    private String client_id = "";
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String pass_code = "";
    /** Password factor authenticate body field (not {@code pass_code}); see cidaas PASSWORD method matrix. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String password = "";
    private String verificationType = "";

    /**
     * Biometric proof JWT for touchid / fingerprint authenticate (same format as enrollment {@code attestation}).
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String attestation = "";

    private String sub = "";

    @JsonIgnore
    private FingerPrintEntity fingerPrintEntity;

    //For face and voice
    private File fileToSend;
    private int face_attempt = 0;




    // OTP / pattern / push pass_code flows (JSON field {@code pass_code}, not {@code password})
    public AuthenticateEntity(String exchange_id, String pass_code, String verificationType) {
        this.exchange_id = exchange_id;
        setPass_code(pass_code);
        this.verificationType = verificationType;
    }

    /** Password login authenticate: JSON field {@code password} only (not {@code pass_code}). */
    @NonNull
    public static AuthenticateEntity forPassword(
            @NonNull String exchangeId, @NonNull String password, @NonNull String verificationType) {
        AuthenticateEntity entity = new AuthenticateEntity();
        entity.setExchange_id(exchangeId);
        entity.setPassword(password);
        entity.setVerificationType(verificationType);
        return entity;
    }


    //For FingerPrint

    public AuthenticateEntity(String exchange_id, String verificationType, FingerPrintEntity fingerPrintEntity) {
        this.exchange_id = exchange_id;
        this.verificationType = verificationType;
        this.fingerPrintEntity = fingerPrintEntity;
    }

    //For Face And voice

    public AuthenticateEntity(String exchange_id, String verificationType, File fileToSend, int face_attempt, String sub) {
        this.exchange_id = exchange_id;
        this.verificationType = verificationType;
        this.fileToSend = fileToSend;
        this.face_attempt = face_attempt;
        this.sub=sub;
    }
    public AuthenticateEntity(String exchange_id, String verificationType, File fileToSend, int face_attempt) {
        this.exchange_id = exchange_id;
        this.verificationType = verificationType;
        this.fileToSend = fileToSend;
        this.face_attempt = face_attempt;

    }

    public AuthenticateEntity() {

    }

    @JsonIgnore
    public FingerPrintEntity getFingerPrintEntity() {
        return fingerPrintEntity;
    }

    @JsonIgnore

    public void setFingerPrintEntity(FingerPrintEntity fingerPrintEntity) {
        this.fingerPrintEntity = fingerPrintEntity;
    }
    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
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

    public String getPass_code() {
        return pass_code;
    }

    public void setPass_code(String pass_code) {
        this.pass_code = pass_code;
        if (pass_code != null && !pass_code.isEmpty()) {
            this.password = "";
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        if (password != null && !password.isEmpty()) {
            this.pass_code = "";
        }
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(String attestation) {
        this.attestation = attestation;
    }

    public int getFace_attempt() {
        return face_attempt;
    }

    public void setFace_attempt(int face_attempt) {
        this.face_attempt = face_attempt;
    }

    public File getFileToSend() {
        return fileToSend;
    }

    public void setFileToSend(File fileToSend) {
        this.fileToSend = fileToSend;
    }

}
