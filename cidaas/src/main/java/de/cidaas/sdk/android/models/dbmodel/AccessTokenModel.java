package de.cidaas.sdk.android.models.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by widasrnarayanan on 16/1/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenModel implements Serializable {
    //Shared instance
    public static AccessTokenModel sharedinstance;

    public static AccessTokenModel getShared() {
        if (sharedinstance == null) {
            sharedinstance = new AccessTokenModel();
        }
        return sharedinstance;
    }

    //Properties
    private String access_token;
    private String userState;
    private String refresh_token;
    private String id_token;
    private String scope;
    private int expires_in = 0;
    private String key;
    private String salt;
    private String userId;
    private long seconds = 0;
    private String plainToken;
    private boolean isEncrypted = true;
    private String token_type = "";
    private String id_token_expires_in = "";
    //Getters and Setters


    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getUserState() {
        return userState;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }


    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getId_token_expires_in() {
        return id_token_expires_in;
    }

    public void setId_token_expires_in(String id_token_expires_in) {
        this.id_token_expires_in = id_token_expires_in;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public String getPlainToken() {
        return plainToken;
    }

    public void setPlainToken(String plainToken) {
        this.plainToken = plainToken;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

}


