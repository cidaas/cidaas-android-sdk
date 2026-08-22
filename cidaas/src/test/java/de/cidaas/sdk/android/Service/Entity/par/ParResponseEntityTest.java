package de.cidaas.sdk.android.Service.Entity.par;

import org.junit.Assert;
import org.junit.Test;

import de.cidaas.sdk.android.service.entity.par.ParResponseEntity;

public class ParResponseEntityTest {

    @Test
    public void gettersAndSetters() {
        ParResponseEntity entity = new ParResponseEntity();
        entity.setRequestUri("urn:ietf:params:oauth:request_uri:test");
        entity.setExpiresIn(600);

        Assert.assertEquals("urn:ietf:params:oauth:request_uri:test", entity.getRequestUri());
        Assert.assertEquals(600, entity.getExpiresIn());
    }
}
