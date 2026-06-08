package de.cidaas.sdk.android.cidaasverification.data.entity.initiate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import de.cidaas.sdk.android.cidaasverification.data.entity.excangeid.ExchangeIDEntity;

/**
 * Payload under {@link InitiateResponse#getData()} for
 * {@code POST /verification-srv/v2/authenticate/initiate/{method}}.
 *
 * <p>Example JSON:</p>
 * <pre>{@code
 * {
 *   "exchange_id": { "exchange_id": "...", "expires_at": "...", "id": "...", ... },
 *   "medium_text": "gan******mar@w***s.in",
 *   "status_id": "...",
 *   "sub": "3cf86734-99d3-4da3-922b-eb1064bdb476",
 *   "push_selected_number": "42"
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateResponseDataEntity implements Serializable {

    private ExchangeIDEntity exchange_id;
    /** Masked destination (e.g. email) for the chosen channel. */
    private String medium_text;
    /** User subject (OIDC {@code sub}) after initiate; use with verify / continue login flows. */
    private String sub;
    private String status_id;
    /**
     * For push login initiate; server-selected value used as {@code pass_code} on authenticate (same concept as
     * enrollment setup {@code push_selected_number}).
     */
    private String push_selected_number;

    public ExchangeIDEntity getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(ExchangeIDEntity exchange_id) {
        this.exchange_id = exchange_id;
    }

    public String getMedium_text() {
        return medium_text;
    }

    public void setMedium_text(String medium_text) {
        this.medium_text = medium_text;
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

    public String getPush_selected_number() {
        return push_selected_number;
    }

    public void setPush_selected_number(String push_selected_number) {
        this.push_selected_number = push_selected_number;
    }
}
