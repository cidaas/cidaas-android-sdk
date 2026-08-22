package de.cidaas.sdk.android.service.repository.Login;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * Resolves OAuth {@code code} from {@code POST /login-srv/login/handle/afterregister/:track_id}
 * (typically {@code 302 Location}, JSON {@code data.code}, or a "Redirecting to …?code=" body).
 */
public final class AfterRegisterAuthorizationCodeParser {

    private static final Pattern CODE_IN_QUERY = Pattern.compile("(?:[?&])code=([^&\\s\"'<>]+)");

    private AfterRegisterAuthorizationCodeParser() {
    }

    @Nullable
    public static String findAuthorizationCode(@Nullable Response rawOkHttp, @Nullable String responseBodyText) {
        String code = findCodeInOkHttp(rawOkHttp);
        if (code != null && !code.isEmpty()) {
            return code;
        }
        if (responseBodyText == null || responseBodyText.isEmpty()) {
            return null;
        }
        String trimmed = responseBodyText.trim();
        if (trimmed.startsWith("{")) {
            String fromJson = parseJsonCode(trimmed);
            if (fromJson != null && !fromJson.isEmpty()) {
                return fromJson;
            }
        }
        return findCodeInPlainText(trimmed);
    }

    @Nullable
    public static String findCodeInPlainText(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Matcher m = CODE_IN_QUERY.matcher(text);
        if (m.find()) {
            String raw = m.group(1).trim();
            return raw.isEmpty() ? null : raw;
        }
        return null;
    }

    @Nullable
    private static String parseJsonCode(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            com.fasterxml.jackson.databind.JsonNode data = root.path("data");
            String code = data.path("code").asText(null);
            if (code != null && !code.trim().isEmpty()) {
                return code.trim();
            }
            String top = root.path("code").asText(null);
            if (top != null && !top.trim().isEmpty()) {
                return top.trim();
            }
        } catch (Exception ignored) {
            // not JSON
        }
        return null;
    }

    @Nullable
    private static String findCodeInOkHttp(@Nullable Response raw) {
        if (raw == null) {
            return null;
        }
        for (Response r = raw; r != null; r = r.priorResponse()) {
            if (r.code() >= 300 && r.code() < 400) {
                String location = r.header("Location");
                String fromLocation = codeFromLocation(location, r.request() != null ? r.request().url() : null);
                if (fromLocation != null) {
                    return fromLocation;
                }
            }
            if (r.request() != null) {
                String fromUrl = queryCode(r.request().url());
                if (fromUrl != null) {
                    return fromUrl;
                }
            }
        }
        return null;
    }

    @Nullable
    private static String codeFromLocation(@Nullable String location, @Nullable HttpUrl requestUrl) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        if (requestUrl != null) {
            HttpUrl resolved = requestUrl.resolve(location);
            String fromResolved = queryCode(resolved);
            if (fromResolved != null) {
                return fromResolved;
            }
        }
        try {
            String fromAbsolute = queryCode(HttpUrl.get(location));
            if (fromAbsolute != null) {
                return fromAbsolute;
            }
        } catch (IllegalArgumentException ignored) {
            // custom scheme
        }
        return findCodeInPlainText(location);
    }

    @Nullable
    private static String queryCode(@Nullable HttpUrl url) {
        if (url == null) {
            return null;
        }
        String code = url.queryParameter("code");
        if (code != null && !code.trim().isEmpty()) {
            return code.trim();
        }
        return null;
    }
}
