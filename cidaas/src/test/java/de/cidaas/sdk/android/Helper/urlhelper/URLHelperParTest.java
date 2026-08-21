package de.cidaas.sdk.android.Helper.urlhelper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.cidaas.sdk.android.helper.urlhelper.URLHelper;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class URLHelperParTest {

    @Test
    public void constructParLoginURL_appendsClientIdAndRequestUri() {
        String authz = "https://demo.cidaas.de/authz-srv/authz";
        String clientId = "client-123";
        String requestUri = "urn:ietf:params:oauth:request_uri:abc";

        String result = URLHelper.getShared().constructParLoginURL(authz, clientId, requestUri);

        Assert.assertNotNull(result);
        Assert.assertFalse("expected non-empty URL, got: " + result, result.isEmpty());
        Assert.assertTrue("result=" + result, result.startsWith(authz));
        Assert.assertTrue("result=" + result, result.contains("client_id=" + clientId));
        Assert.assertTrue("result=" + result, result.contains("request_uri="));
    }

    @Test
    public void constructParLoginURL_returnsEmptyOnMissingArgs() {
        Assert.assertEquals("", URLHelper.getShared().constructParLoginURL(null, "c", "r"));
        Assert.assertEquals("", URLHelper.getShared().constructParLoginURL("https://x", null, "r"));
        Assert.assertEquals("", URLHelper.getShared().constructParLoginURL("https://x", "c", ""));
    }

    @Test
    public void getParURL_isAuthzSrvPar() {
        Assert.assertEquals("/authz-srv/par", URLHelper.getShared().getParURL());
    }
}
