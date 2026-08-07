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
public class LocationDetailsTest {

    private Context context;
    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
    }

    @After
    public void tearDown() {
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;
    }

    @Test
    public void getShared_returnsSameInstanceOnRepeatedCalls() {
        LocationDetails first = LocationDetails.getShared(context);
        LocationDetails second = LocationDetails.getShared(context);

        Assert.assertSame(first, second);
        Assert.assertSame(first, LocationDetails.shared);
    }

    @Test
    public void getLocation_withoutLastKnown_returnsNullWithoutBlocking() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        LocationDetails details = new LocationDetails(context);

        Assert.assertNull(details.getLocation());
        Assert.assertEquals("", details.getLatitude());
        Assert.assertEquals("", details.getLongitude());
    }

    @Test
    public void getLocation_withLastKnown_returnsCoordinates() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        Location lastKnown = new Location(LocationManager.NETWORK_PROVIDER);
        lastKnown.setLatitude(48.137154);
        lastKnown.setLongitude(11.576124);
        shadowLocationManager.setLastKnownLocation(LocationManager.NETWORK_PROVIDER, lastKnown);

        LocationDetails details = new LocationDetails(context);
        Location result = details.getLocation();

        Assert.assertNotNull(result);
        Assert.assertEquals(48.137154, result.getLatitude(), 0.000001);
        Assert.assertEquals(11.576124, result.getLongitude(), 0.000001);
        Assert.assertEquals("48.137154", details.getLatitude());
        Assert.assertEquals("11.576124", details.getLongitude());
        Assert.assertTrue(details.canGetLocation());
    }

    @Test
    public void onLocationChanged_doesNotUpdateCachedLocation() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        LocationDetails details = new LocationDetails(context);
        Assert.assertNull(details.getLocation());

        Location update = new Location(LocationManager.GPS_PROVIDER);
        update.setLatitude(1.0);
        update.setLongitude(2.0);
        details.onLocationChanged(update);

        Assert.assertNull(details.getLocation());
        Assert.assertEquals("", details.getLatitude());
        Assert.assertEquals("", details.getLongitude());
    }

    @Test
    public void stopUsingGPS_doesNotThrow() {
        LocationDetails details = new LocationDetails(context);
        details.stopUsingGPS();
    }
}
