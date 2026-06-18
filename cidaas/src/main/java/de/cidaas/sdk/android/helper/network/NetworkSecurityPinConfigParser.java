package de.cidaas.sdk.android.helper.network;

import android.content.Context;
import android.content.res.XmlResourceParser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.CertificatePinner;

/**
 * Builds an OkHttp {@link CertificatePinner} from a {@code res/xml/network_security_config.xml}
 * resource (same schema as Android's Network Security Config {@code pin-set} / {@code pin}).
 * <p>
 * OkHttp does not consume {@code android:networkSecurityConfig} automatically; this parser
 * mirrors the declarative pins so Retrofit/OkHttp clients can enforce the same SPKI hashes.
 */
public final class NetworkSecurityPinConfigParser {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private NetworkSecurityPinConfigParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static CertificatePinner parseFromResource(@NonNull Context context, int xmlResId) {
        if (xmlResId == 0) {
            return null;
        }
        try {
            XmlResourceParser parser = context.getApplicationContext().getResources().getXml(xmlResId);
            return parse(parser);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    public static CertificatePinner parse(@NonNull XmlPullParser parser) {
        try {
            CertificatePinner.Builder builder = new CertificatePinner.Builder();
            boolean[] anyPattern = new boolean[] { false };

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "domain-config".equals(parser.getName())) {
                    List<String> domainHosts = new ArrayList<>();
                    List<Boolean> domainIncludeSubdomains = new ArrayList<>();
                    List<String> pinHashesOkHttp = new ArrayList<>();
                    readDomainConfigBlock(parser, domainHosts, domainIncludeSubdomains, pinHashesOkHttp);
                    applyDomainConfig(builder, anyPattern, domainHosts, domainIncludeSubdomains, pinHashesOkHttp);
                }
                eventType = parser.next();
            }
            if (!anyPattern[0]) {
                return null;
            }
            return builder.build();
        } catch (XmlPullParserException | IOException e) {
            return null;
        }
    }

    private static void readDomainConfigBlock(
            @NonNull XmlPullParser parser,
            @NonNull List<String> domainHosts,
            @NonNull List<Boolean> domainIncludeSubdomains,
            @NonNull List<String> pinHashesOkHttp
    ) throws XmlPullParserException, IOException {
        int startDepth = parser.getDepth();
        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.END_TAG
                    && "domain-config".equals(parser.getName())
                    && parser.getDepth() == startDepth) {
                break;
            }
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("domain".equals(tag)) {
                    boolean includeSub = readBooleanAttr(parser, "includeSubdomains");
                    String host = readSimpleTextElement(parser).trim();
                    if (!host.isEmpty()) {
                        domainHosts.add(host);
                        domainIncludeSubdomains.add(includeSub);
                    }
                } else if ("pin".equals(tag)) {
                    String digest = firstNonNull(
                            parser.getAttributeValue(ANDROID_NS, "digest"),
                            parser.getAttributeValue(null, "digest"));
                    String body = readSimpleTextElement(parser).trim();
                    if ("SHA-256".equalsIgnoreCase(digest) && !body.isEmpty()) {
                        pinHashesOkHttp.add("sha256/" + body);
                    }
                }
            }
            eventType = parser.next();
        }
    }

    private static void applyDomainConfig(
            @NonNull CertificatePinner.Builder builder,
            @NonNull boolean[] anyPattern,
            @NonNull List<String> domainHosts,
            @NonNull List<Boolean> domainIncludeSubdomains,
            @NonNull List<String> pinHashesOkHttp
    ) {
        if (pinHashesOkHttp.isEmpty() || domainHosts.isEmpty()) {
            return;
        }
        String[] pins = pinHashesOkHttp.toArray(new String[0]);
        for (int i = 0; i < domainHosts.size(); i++) {
            String host = domainHosts.get(i);
            boolean includeSub = domainIncludeSubdomains.get(i);
            builder.add(host, pins);
            anyPattern[0] = true;
            if (includeSub) {
                builder.add("*." + host, pins);
                anyPattern[0] = true;
            }
        }
    }

    /**
     * Reads text for a simple element like {@code <domain>host</domain>} or {@code <pin>...</pin>}.
     */
    @NonNull
    private static String readSimpleTextElement(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        StringBuilder sb = new StringBuilder();
        int innerDepth = parser.getDepth();
        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.TEXT) {
                String t = parser.getText();
                if (t != null) {
                    sb.append(t);
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getDepth() == innerDepth) {
                break;
            }
            eventType = parser.next();
        }
        return sb.toString();
    }

    private static boolean readBooleanAttr(@NonNull XmlPullParser parser, @NonNull String localName) {
        String v = firstNonNull(
                parser.getAttributeValue(ANDROID_NS, localName),
                parser.getAttributeValue(null, localName));
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }

    @Nullable
    private static String firstNonNull(@Nullable String a, @Nullable String b) {
        return a != null ? a : b;
    }
}
