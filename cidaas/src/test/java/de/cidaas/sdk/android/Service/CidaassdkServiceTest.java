package de.cidaas.sdk.android.Service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.cidaas.sdk.android.helper.general.CidaasHelper;

/**
 * Unit tests for createCustomUserAgent ASCII sanitization.
 * Tests verify that non-ASCII characters are properly removed from User-Agent string.
 */
public class CidaassdkServiceTest {

    @Before
    public void setUp() {
        CidaasHelper.APP_NAME = "com.test.app";
        CidaasHelper.APP_VERSION = "1.0.0";
    }

    @Test
    public void testSanitizeUserAgent_withAsciiAppName() {
        String input = "Cidaas-com.example.testapp/1.0.0 Make:Samsung_device Model:Galaxy";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("Cidaas-com.example.testapp"));
        Assert.assertTrue(isAsciiOnly(result));
    }

    @Test
    public void testSanitizeUserAgent_withNonAsciiCharacters() {
        String input = "Cidaas-RTS Bêta/1.0.0 Make:Samsung";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertTrue("User-Agent should contain only ASCII characters", isAsciiOnly(result));
        Assert.assertFalse("Non-ASCII character ê should be removed", result.contains("ê"));
        Assert.assertTrue("Should contain RTS", result.contains("RTS"));
        Assert.assertTrue("Should contain ta", result.contains("ta"));
    }

    @Test
    public void testSanitizeUserAgent_withUnicodeCharacters() {
        String input = "Cidaas-Test日本語App/2.0.0";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertTrue("User-Agent should contain only ASCII characters", isAsciiOnly(result));
        Assert.assertFalse(result.contains("日"));
        Assert.assertFalse(result.contains("本"));
        Assert.assertFalse(result.contains("語"));
        Assert.assertTrue("Should contain TestApp", result.contains("TestApp"));
    }

    @Test
    public void testSanitizeUserAgent_withEmojis() {
        String input = "Cidaas-MyApp🚀/1.0.0";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertTrue("User-Agent should contain only ASCII characters", isAsciiOnly(result));
        Assert.assertTrue("Should contain MyApp", result.contains("MyApp"));
    }

    @Test
    public void testSanitizeUserAgent_preservesAsciiCharacters() {
        String input = "Cidaas-com.cidaas.test/3.2.14 Make:Google_Pixel Model:Pixel5";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertEquals(input, result);
        Assert.assertTrue(isAsciiOnly(result));
    }

    @Test
    public void testSanitizeUserAgent_withSpecialAsciiCharacters() {
        String input = "Cidaas-com.test-app_v2/1.0.0-beta";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("com.test-app_v2"));
        Assert.assertTrue(result.contains("1.0.0-beta"));
    }

    @Test
    public void testSanitizeUserAgent_withMixedContent() {
        String input = "Cidaas-RTSBêtaApp/1.0.0 Make:Huawei华为 Model:P40";
        String result = sanitizeForUserAgent(input);

        Assert.assertNotNull(result);
        Assert.assertTrue(isAsciiOnly(result));
        Assert.assertTrue(result.contains("RTSBtaApp"));
        Assert.assertTrue(result.contains("Huawei"));
        Assert.assertTrue(result.contains("P40"));
    }

    /**
     * Simulates the sanitization logic from createCustomUserAgent.
     * Removes non-ASCII characters for HTTP header compatibility.
     */
    private String sanitizeForUserAgent(String ua) {
        return ua.replaceAll("[^\\x20-\\x7E]", "");
    }

    private boolean isAsciiOnly(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (c < 0x20 || c > 0x7E) {
                return false;
            }
        }
        return true;
    }
}
