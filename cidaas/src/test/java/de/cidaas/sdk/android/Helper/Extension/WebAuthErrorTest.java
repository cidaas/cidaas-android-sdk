package de.cidaas.sdk.android.Helper.Extension;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import de.cidaas.sdk.android.helper.extension.WebAuthError;

@RunWith(RobolectricTestRunner.class)
@Ignore
public class WebAuthErrorTest {

    Context context;
    @Mock
    Object error;

    WebAuthError shared;

    WebAuthError webAuthError;

    @Before
    public void setUp() {

        context = RuntimeEnvironment.application;
        webAuthError = WebAuthError.getShared(context);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetShared() throws Exception {
        WebAuthError result = WebAuthError.getShared(context);
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testFileNotFoundException() throws Exception {
        WebAuthError result = webAuthError.fileNotFoundException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testNoContentInFileException() throws Exception {
        WebAuthError result = webAuthError.noContentInFileException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testPropertyMissingException() throws Exception {
        WebAuthError result = webAuthError.propertyMissingException("WebError Testing", "");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testServiceFailureException() throws Exception {
        WebAuthError result = webAuthError.serviceCallFailureException(0, "errorMessage", "error");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testLoginURLMissingException() throws Exception {
        WebAuthError result = webAuthError.loginURLMissingException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testRedirectURLMissingException() throws Exception {
        WebAuthError result = webAuthError.redirectURLMissingException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testUserCancelledException() throws Exception {
        WebAuthError result = webAuthError.userCancelledException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testCodeNotFoundException() throws Exception {
        WebAuthError result = webAuthError.codeNotFoundException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testEmptyCallbackException() throws Exception {
        WebAuthError result = webAuthError.emptyCallbackException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testNoUserFoundException() throws Exception {
        WebAuthError result = webAuthError.noUserFoundException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testDeviceVerificationFailureException() throws Exception {
        WebAuthError result = webAuthError.deviceVerificationFailureException("");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testCustomException() throws Exception {
        WebAuthError result = webAuthError.customException(0, "errorMessage", "");
        Assert.assertTrue(result instanceof WebAuthError);
    }

    @Test
    public void testErrorCode() throws Exception {
        WebAuthError.getShared(context).setError(error);
        WebAuthError.getShared(context).setErrorCode(27);
        WebAuthError.getShared(context).setErrorMessage("Message");
        WebAuthError.getShared(context).setStatusCode(17);

        Assert.assertEquals("Message", WebAuthError.getShared(context).getErrorMessage());
        Assert.assertEquals(27, WebAuthError.getShared(context).getErrorCode());
        Assert.assertEquals(17, WebAuthError.getShared(context).getStatusCode());
        Assert.assertEquals(error, WebAuthError.getShared(context).getError());

    }
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme