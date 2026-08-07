package de.cidaas.sdk.android.Library.LocationLibrary;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLocationManager;

import de.cidaas.sdk.android.library.common.Privacy;
import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;

import static org.robolectric.Shadows.shadowOf;

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
    public void whenLocationAccessDisabled_ignoresLastKnownLocation() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);

        Location lastKnown = new Location(LocationManager.NETWORK_PROVIDER);
        lastKnown.setLatitude(10.0);
        lastKnown.setLongitude(20.0);
        shadowLocationManager.setLastKnownLocation(LocationManager.NETWORK_PROVIDER, lastKnown);

        Privacy.setLocationEnabled(false);
        LocationDetails details = new LocationDetails(context);

        Assert.assertNull(details.getLocation());
        Assert.assertEquals("", details.getLatitude());
        Assert.assertEquals("", details.getLongitude());
    }

    @Test
    public void whenLocationAccessEnabled_getLatitudeDoesNotThrowWithoutPermission() {
        Privacy.setLocationEnabled(true);

        LocationDetails details = new LocationDetails(context);

        // No location permission in Robolectric by default — returns empty, does not crash
        Assert.assertEquals("", details.getLatitude());
        Assert.assertEquals("", details.getLongitude());
    }

    @Test
    public void getShared_whenLocationDisabled_stillReturnsSharedInstance() {
        Privacy.setLocationEnabled(false);

        LocationDetails first = LocationDetails.getShared(context);
        LocationDetails second = LocationDetails.getShared(context);

        Assert.assertSame(first, second);
        Assert.assertNull(first.getLocation());
    }
}
