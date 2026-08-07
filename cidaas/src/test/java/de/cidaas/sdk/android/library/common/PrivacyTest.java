package de.cidaas.sdk.android.library.common;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class PrivacyTest {

    @After
    public void tearDown() {
        // Isolate tests; does not claim to verify the field initializer default
        Privacy.setLocationEnabled(true);
    }

    @Test
    public void setLocationEnabled_roundTrip() {
        Privacy.setLocationEnabled(false);
        Assert.assertFalse(Privacy.isLocationEnabled());

        Privacy.setLocationEnabled(true);
        Assert.assertTrue(Privacy.isLocationEnabled());
    }
}
