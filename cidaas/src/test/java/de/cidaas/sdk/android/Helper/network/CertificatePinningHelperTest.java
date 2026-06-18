package de.cidaas.sdk.android.helper.network;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import de.cidaas.sdk.android.helper.general.CidaasHelper;
import okhttp3.CertificatePinner;

public class CertificatePinningHelperTest {

  private static final String PIN =
      "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

  @After
  public void tearDown() {
    CidaasHelper.certificatePinningConfig = null;
    CidaasHelper.networkSecurityPinningXmlResId = 0;
    CidaasHelper.baseurl = "";
  }

  @Test
  public void extractHost_fromHttpsBaseUrl() {
    Assert.assertEquals(
        "tenant.example.com", CertificatePinningHelper.extractHost("https://tenant.example.com/"));
  }

  @Test
  public void buildCertificatePinner_usesExplicitHost() {
    CidaasHelper.certificatePinningConfig =
        new CertificatePinningConfig("pinned.example.com", PIN);

    CertificatePinner pinner = CertificatePinningHelper.buildCertificatePinner();

    Assert.assertNotNull(pinner);
    Assert.assertEquals(1, pinner.findMatchingPins("pinned.example.com").size());
    Assert.assertEquals(
        "pinned.example.com",
        pinner.findMatchingPins("pinned.example.com").get(0).getPattern());
  }

  @Test
  public void buildCertificatePinner_usesBaseUrlHostWhenHostNotSet() {
    CidaasHelper.baseurl = "https://tenant.cidaas.de/";
    CidaasHelper.certificatePinningConfig = new CertificatePinningConfig(null, PIN);

    CertificatePinner pinner = CertificatePinningHelper.buildCertificatePinner();

    Assert.assertNotNull(pinner);
    Assert.assertEquals(1, pinner.findMatchingPins("tenant.cidaas.de").size());
  }

  @Test
  public void buildCertificatePinner_returnsNullWhenNotConfigured() {
    Assert.assertNull(CertificatePinningHelper.buildCertificatePinner());
  }

  @Test(expected = IllegalArgumentException.class)
  public void config_rejectsInvalidPinFormat() {
    new CertificatePinningConfig("example.com", "not-a-pin");
  }
}
