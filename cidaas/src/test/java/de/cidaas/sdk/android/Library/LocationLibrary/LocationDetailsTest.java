package de.cidaas.sdk.android.Library.LocationLibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class LocationDetailsTest {
    @Mock
    Context mContext;
    @Mock
    LocationDetails shared;
    @Mock
    Location location;
    @Mock
    LocationManager locationManager;
    @InjectMocks
    LocationDetails locationDetails;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetShared() throws Exception {
        when(shared.getLocation()).thenReturn(null);

        Context context = mock(Context.class);

        when(context.checkPermission(eq(Manifest.permission.WRITE_EXTERNAL_STORAGE), anyInt(), anyInt())).thenReturn(
                PackageManager.PERMISSION_GRANTED);

        LocationDetails result = LocationDetails.getShared(null);
        Assert.assertEquals(new LocationDetails(null, "string"), result);
    }

    @Test
    public void testGetLocation() throws Exception {
        Location result = locationDetails.getLocation();
        Assert.assertEquals(null, result);
    }

    @Test
    public void testStopUsingGPS() throws Exception {
        locationDetails.stopUsingGPS();
    }

    @Test
    public void testGetLatitude() throws Exception {
        when(shared.getLocation()).thenReturn(null);

        String result = locationDetails.getLatitude();
        Assert.assertEquals("replaceMeWithExpectedResult", result);
    }

    @Test
    public void testGetLongitude() throws Exception {
        when(shared.getLocation()).thenReturn(null);

        String result = locationDetails.getLongitude();
        Assert.assertEquals("replaceMeWithExpectedResult", result);
    }

    @Test
    public void testGetBearing() throws Exception {
        float result = locationDetails.getBearing();
        Assert.assertEquals(0f, result);
    }

    @Test
    public void testCanGetLocation() throws Exception {
        boolean result = locationDetails.canGetLocation();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testShowSettingsAlert() throws Exception {
        locationDetails.showSettingsAlert();
    }

    @Test
    public void testOnLocationChanged() throws Exception {
        locationDetails.onLocationChanged((Location) null);
    }

    @Test
    public void testOnProviderDisabled() throws Exception {
        locationDetails.onProviderDisabled("provider");
    }

    @Test
    public void testOnProviderEnabled() throws Exception {
        locationDetails.onProviderEnabled("provider");
    }

    @Test
    public void testOnStatusChanged() throws Exception {
        locationDetails.onStatusChanged("provider", 0, null);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme