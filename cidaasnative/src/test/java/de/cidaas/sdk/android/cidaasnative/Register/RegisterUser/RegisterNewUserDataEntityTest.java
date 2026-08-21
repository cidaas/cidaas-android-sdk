package de.cidaas.sdk.android.cidaasnative.Register.RegisterUser;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserDataEntity;

public class RegisterNewUserDataEntityTest {


    RegisterNewUserDataEntity registerNewUserDataEntity = new RegisterNewUserDataEntity();

    @Before
    public void setup() {

    }

    @Test
    public void setEmail() {
        registerNewUserDataEntity.setSuggested_action("TestData");
        Assert.assertEquals("TestData", registerNewUserDataEntity.getSuggested_action());
    }

    @Test
    public void setPhoneNumber() {
        registerNewUserDataEntity.setTrackId("TestData");
        Assert.assertEquals("TestData", registerNewUserDataEntity.getTrackId());
    }

    @Test
    public void setProcessingType() {
        registerNewUserDataEntity.setSub("TestData");
        Assert.assertEquals("TestData", registerNewUserDataEntity.getSub());
    }

    @Test
    public void setRequestId() {
        registerNewUserDataEntity.setUserStatus("TestData");
        Assert.assertEquals("TestData", registerNewUserDataEntity.getUserStatus());
    }

    @Test
    public void setResetMedium() {
        registerNewUserDataEntity.setEmail_verified("TestData");
        Assert.assertEquals("TestData", registerNewUserDataEntity.getEmail_verified());
    }

    @Test
    public void setNext_token() {
        registerNewUserDataEntity.setNext_token("TestData");
        Assert.assertEquals("TestData", registerNewUserDataEntity.getNext_token());
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme