package de.cidaas.sdk.android.Service.HelperForService.Headers;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Map;

import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.library.common.Privacy;
import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;
import de.cidaas.sdk.android.service.helperforservice.Headers.Headers;

@RunWith(RobolectricTestRunner.class)
public class HeadersLocationAccessTest {

    private Headers headers;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        DBHelper.setConfig(context);
        LocationDetails.shared = null;
        Privacy.setLocationEnabled(true);
        headers = new Headers(context);
    }

    @After
    public void tearDown() {
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;
    }

    @Test
    public void getHeaders_whenLocationDisabled_omitsLatAndLon() {
        Privacy.setLocationEnabled(false);

        Map<String, String> result = headers.getHeaders(null, false, null);

        Assert.assertFalse(result.containsKey("lat"));
        Assert.assertFalse(result.containsKey("lon"));
    }

    @Test
    public void getHeaders_whenLocationEnabled_includesLatAndLon() {
        Privacy.setLocationEnabled(true);

        Map<String, String> result = headers.getHeaders(null, false, null);

        Assert.assertTrue(result.containsKey("lat"));
        Assert.assertTrue(result.containsKey("lon"));
    }
}
