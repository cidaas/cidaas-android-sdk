package de.cidaas.sdk.android.library.common;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrivacyTest {

    @Before
    public void setUp() {
        Privacy.setLocationEnabled(true);
    }

    @After
    public void tearDown() {
        Privacy.setLocationEnabled(true);
    }

    @Test
    public void defaultLocationAccessIsEnabled() {
        Assert.assertTrue(Privacy.isLocationEnabled());
    }

    @Test
    public void setLocationEnabled_roundTrip() {
        Privacy.setLocationEnabled(false);
        Assert.assertFalse(Privacy.isLocationEnabled());

        Privacy.setLocationEnabled(true);
        Assert.assertTrue(Privacy.isLocationEnabled());
    }
}
