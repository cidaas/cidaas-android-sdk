package de.cidaas.sdk.android.controller;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLocationManager;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.library.common.Privacy;
import de.cidaas.sdk.android.library.locationlibrary.LocationDetails;

import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class CidaasLocationAccessTest {

    private Context context;
    private Cidaas cidaas;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        DBHelper.setConfig(context);
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;
        cidaas = new Cidaas(context);
    }

    @After
    public void tearDown() {
        Privacy.setLocationEnabled(true);
        LocationDetails.shared = null;
    }

    @Test
    public void setEnableLocationAccess_updatesPrivacy() {
        cidaas.setEnableLocationAccess(false);
        Assert.assertFalse(cidaas.isEnableLocationAccess());
        Assert.assertFalse(Privacy.isLocationEnabled());

        cidaas.setEnableLocationAccess(true);
        Assert.assertTrue(cidaas.isEnableLocationAccess());
        Assert.assertTrue(Privacy.isLocationEnabled());
    }

    @Test
    public void isEnableLocationAccess_reflectsPrivacyState() {
        Privacy.setLocationEnabled(false);
        Assert.assertFalse(cidaas.isEnableLocationAccess());

        Privacy.setLocationEnabled(true);
        Assert.assertTrue(cidaas.isEnableLocationAccess());
    }

    @Test
    public void setEnableLocationAccess_false_stopsRegisteredLocationListener() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);

        LocationDetails.getShared(context);
        Assert.assertNotNull(LocationDetails.shared);
        Assert.assertFalse(shadowLocationManager.getRequestLocationUpdateListeners().isEmpty());

        Cidaas.setEnableLocationAccess(false);

        Assert.assertFalse(Privacy.isLocationEnabled());
        Assert.assertTrue(shadowLocationManager.getRequestLocationUpdateListeners().isEmpty());
    }
}
