package de.cidaas.sdk.android.cidaasnative;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginCredentialsErrorDataEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.login.LoginCredentialsResponseErrorEntity;

public class LoginCredentialsResponseErrorEntityTest {

    LoginCredentialsErrorDataEntity error;

    LoginCredentialsResponseErrorEntity loginCredentialsResponseErrorEntity;

    @Before
    public void setUp() {
        loginCredentialsResponseErrorEntity = new LoginCredentialsResponseErrorEntity();
    }

    @Test
    public void setSuccess() {
        loginCredentialsResponseErrorEntity.setSuccess(true);
        Assert.assertTrue(loginCredentialsResponseErrorEntity.isSuccess());

    }

    @Test
    public void setStatus() {
        loginCredentialsResponseErrorEntity.setStatus(27);
        Assert.assertEquals(27, loginCredentialsResponseErrorEntity.getStatus());

    }

    @Test
    public void setData() {
        error = new LoginCredentialsErrorDataEntity();

        error.setError("Error");
        loginCredentialsResponseErrorEntity.setError(error);
        Assert.assertEquals("Error", loginCredentialsResponseErrorEntity.getError().getError());

    }
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme