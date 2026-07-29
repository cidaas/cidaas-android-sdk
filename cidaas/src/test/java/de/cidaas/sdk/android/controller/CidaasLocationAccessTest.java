package de.cidaas.sdk.android.controller;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.library.common.Privacy;

@RunWith(RobolectricTestRunner.class)
public class CidaasLocationAccessTest {

    private Cidaas cidaas;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        DBHelper.setConfig(context);
        Privacy.setLocationEnabled(true);
        cidaas = new Cidaas(context);
    }

    @After
    public void tearDown() {
        Privacy.setLocationEnabled(true);
    }

    @Test
    public void defaultEnableLocationAccessIsTrue() {
        Assert.assertTrue(cidaas.isEnableLocationAccess());
        Assert.assertTrue(Privacy.isLocationEnabled());
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
}
