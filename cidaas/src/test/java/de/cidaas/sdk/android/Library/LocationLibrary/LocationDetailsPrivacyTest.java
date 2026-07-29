package de.cidaas.sdk.android.Library.LocationLibrary;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import de.cidaas.sdk.android.library.common.Privacy;
import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;

@RunWith(RobolectricTestRunner.class)
public class LocationDetailsPrivacyTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;
    }

    @After
    public void tearDown() {
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;
    }

    @Test
    public void whenLocationAccessDisabled_returnsEmptyCoordinatesAndCannotGetLocation() {
        Privacy.setLocationEnabled(false);

        LocationDetails details = new LocationDetails(context);

        Assert.assertNull(details.getLocation());
        Assert.assertEquals("", details.getLatitude());
        Assert.assertEquals("", details.getLongitude());
        Assert.assertEquals(0f, details.getBearing(), 0f);
        Assert.assertFalse(details.canGetLocation());
    }

    @Test
    public void whenLocationAccessEnabled_getLatitudeDoesNotThrowWithoutPermission() {
        Privacy.setLocationEnabled(true);

        LocationDetails details = new LocationDetails(context);

        // No location permission in Robolectric by default — returns empty, does not crash
        Assert.assertEquals("", details.getLatitude());
        Assert.assertEquals("", details.getLongitude());
    }
}
