package de.cidaas.sdk.android.helper.network;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

import okhttp3.CertificatePinner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class NetworkSecurityPinConfigParserTest {

  private static final String VALID_PIN_BODY =
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

  @Test
  public void parse_buildsCertificatePinnerFromDomainConfig() throws Exception {
    String xml =
        "<?xml version='1.0' encoding='utf-8'?><network-security-config>"
            + "<domain-config>"
            + "<domain includeSubdomains='false'>pinned.example.com</domain>"
            + "<pin-set><pin digest='SHA-256'>"
            + VALID_PIN_BODY
            + "</pin></pin-set>"
            + "</domain-config>"
            + "</network-security-config>";
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    XmlPullParser parser = factory.newPullParser();
    parser.setInput(new StringReader(xml));

    CertificatePinner pinner = NetworkSecurityPinConfigParser.parse(parser);

    Assert.assertNotNull(pinner);
    Assert.assertEquals(1, pinner.findMatchingPins("pinned.example.com").size());
  }

  @Test
  public void parse_includeSubdomains_addsWildcardPattern() throws Exception {
    String xml =
        "<?xml version='1.0' encoding='utf-8'?><network-security-config>"
            + "<domain-config>"
            + "<domain includeSubdomains='true'>example.com</domain>"
            + "<pin-set><pin digest='SHA-256'>"
            + VALID_PIN_BODY
            + "</pin></pin-set>"
            + "</domain-config>"
            + "</network-security-config>";
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    XmlPullParser parser = factory.newPullParser();
    parser.setInput(new StringReader(xml));

    CertificatePinner pinner = NetworkSecurityPinConfigParser.parse(parser);

    Assert.assertNotNull(pinner);
    Assert.assertFalse(pinner.findMatchingPins("example.com").isEmpty());
    Assert.assertFalse(pinner.findMatchingPins("api.example.com").isEmpty());
  }
}
