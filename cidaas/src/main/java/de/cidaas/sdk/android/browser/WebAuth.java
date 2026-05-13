package de.cidaas.sdk.android.browser;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;

/**
 * Browser-based auth flows via {@link Cidaas#webAuth(Context)}. Uses the {@link Context} you pass into
 * {@code webAuth(...)} for launching custom tabs (not {@link Cidaas#context}).
 *
 * <pre>{@code
 * cidaas.webAuth(this).extraParams(map).signIn(callback);
 * cidaas.webAuth(this).signOut(sub, callback);
 * cidaas.webAuth(this).signOut(sub, postLogoutRedirectUri, callback);
 * cidaas.webAuth(this).social(requestId, provider).signIn(callback);
 * }</pre>
 */
public final class WebAuth {

    private final Cidaas cidaas;
    private final Context activityContext;
    private String color;
    private Map<String, String> extraParams;

    public WebAuth(@NonNull Cidaas cidaas, @NonNull Context activityContext) {
        if (activityContext == null) {
            throw new IllegalArgumentException("activityContext must not be null");
        }
        this.cidaas = cidaas;
        this.activityContext = activityContext;
    }

    @NonNull
    public WebAuth color(@Nullable String color) {
        this.color = color;
        return this;
    }

    @NonNull
    public WebAuth extraParams(@Nullable Map<String, String> extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    /**
     * Hosted login in a custom tab ({@link Cidaas#loginWithBrowser}).
     */
    public void signIn(@NonNull EventResult<AccessTokenEntity> callback) {
        cidaas.loginWithBrowser(activityContext, color, extraParams, callback);
    }

    /**
     * Browser sign-out ({@link Cidaas#logoutWithBrowser}) for the given subject.
     * Equivalent to {@link #signOut(String, String, EventResult)} with no post-logout redirect URI.
     */
    public void signOut(@NonNull String sub, @NonNull EventResult<Boolean> callback) {
        signOut(sub, null, callback);
    }

    /**
     * Browser sign-out ({@link Cidaas#logoutWithBrowser}) for the given subject.
     *
     * @param postLogoutRedirectUri optional post-logout redirect URI ({@code post_logout_redirect_uri})
     */
    public void signOut(@NonNull String sub, @Nullable String postLogoutRedirectUri,
                        @NonNull EventResult<Boolean> callback) {
        cidaas.logoutWithBrowser(activityContext, sub, postLogoutRedirectUri, null, callback);
    }

    /**
     * Social login in a custom tab ({@link Cidaas#loginWithSocial}).
     */
    @NonNull
    public Social social(@NonNull String requestId, @NonNull String provider) {
        return new Social(cidaas, activityContext, requestId, provider);
    }

    /**
     * Optional toolbar color for {@link WebAuth#social(String, String)}, completed with {@link #signIn(EventResult)}.
     */
    public static final class Social {

        private final Cidaas cidaas;
        private final Context activityContext;
        private final String requestId;
        private final String provider;
        private String color;

        Social(@NonNull Cidaas cidaas, @NonNull Context activityContext,
               @NonNull String requestId, @NonNull String provider) {
            this.cidaas = cidaas;
            this.activityContext = activityContext;
            this.requestId = requestId;
            this.provider = provider;
        }

        @NonNull
        public Social color(@Nullable String color) {
            this.color = color;
            return this;
        }

        public void signIn(@NonNull EventResult<AccessTokenEntity> callback) {
            cidaas.loginWithSocial(activityContext, requestId, provider, color, callback);
        }
    }
}
