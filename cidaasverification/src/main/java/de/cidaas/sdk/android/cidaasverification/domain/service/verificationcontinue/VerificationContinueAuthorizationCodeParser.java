package de.cidaas.sdk.android.cidaasverification.domain.service.verificationcontinue;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cidaas.sdk.android.cidaasverification.data.entity.verificationcontinue.VerificationContinueResponseEntity;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Resolves OAuth authorization {@code code} from a login-continue call: JSON body, {@code 302 Location},
 * redirect URL on the OkHttp chain, or plain text such as
 * {@code Found. Redirecting to com.example.app://callback?code=...&expires_in=...}.
 */
final class VerificationContinueAuthorizationCodeParser {

    private static final Pattern CODE_IN_QUERY = Pattern.compile("(?:[?&])code=([^&\\s\"'<>]+)");

    private VerificationContinueAuthorizationCodeParser() {
    }

    /**
     * @param rawOkHttp raw OkHttp response (for Location / redirect chain)
     * @param responseBodyText optional body text (success or error); may be JSON or plain redirect message
     * @return non-null trimmed code, or null if not found
     */
    @Nullable
    static String findAuthorizationCode(@Nullable Response rawOkHttp, @Nullable String responseBodyText) {
        String code = findCodeInOkHttpChain(rawOkHttp);
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
        return findCodeInPlainText(responseBodyText);
    }

    @Nullable
    private static String parseJsonCode(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                    false);
            VerificationContinueResponseEntity entity = mapper.readValue(json, VerificationContinueResponseEntity.class);
            if (entity.getData() != null && entity.getData().getCode() != null && !entity.getData().getCode().trim().isEmpty()) {
                return entity.getData().getCode().trim();
            }
        } catch (Exception ignored) {
            // not JSON or wrong shape
        }
        return null;
    }

    /**
     * Extracts {@code code} query value from any substring (e.g. custom scheme URLs after "Redirecting to").
     */
    @Nullable
    static String findCodeInPlainText(@Nullable String text) {
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
    static String findCodeInOkHttpChain(@Nullable Response raw) {
        if (raw == null) {
            return null;
        }
        for (Response r = raw; r != null; r = r.priorResponse()) {
            Request req = r.request();
            if (req != null) {
                String fromRequestUrl = queryCode(req.url());
                if (fromRequestUrl != null) {
                    return fromRequestUrl;
                }
            }
            if (r.code() >= 300 && r.code() < 400) {
                String location = r.header("Location");
                if (location != null && !location.isEmpty() && req != null) {
                    HttpUrl resolved = req.url().resolve(location);
                    String fromLocation = queryCode(resolved);
                    if (fromLocation != null) {
                        return fromLocation;
                    }
                    try {
                        HttpUrl absolute = HttpUrl.get(location);
                        String fromAbsolute = queryCode(absolute);
                        if (fromAbsolute != null) {
                            return fromAbsolute;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // not a valid absolute URL
                    }
                    String fromLocationPlain = findCodeInPlainText(location);
                    if (fromLocationPlain != null) {
                        return fromLocationPlain;
                    }
                }
            }
        }
        return null;
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
