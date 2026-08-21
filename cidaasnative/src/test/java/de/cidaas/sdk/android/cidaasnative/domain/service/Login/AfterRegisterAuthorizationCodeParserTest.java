package de.cidaas.sdk.android.cidaasnative.domain.service.Login;

import org.junit.Assert;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AfterRegisterAuthorizationCodeParserTest {

    @Test
    public void findCodeInPlainText_fromRedirectMessage() {
        String body = "Found. Redirecting to com.rahul.cidaasproject://oauth/callback?code=abc123&expires_in=30";
        Assert.assertEquals("abc123", AfterRegisterAuthorizationCodeParser.findCodeInPlainText(body));
    }

    @Test
    public void findCodeInPlainText_missing() {
        Assert.assertNull(AfterRegisterAuthorizationCodeParser.findCodeInPlainText(null));
        Assert.assertNull(AfterRegisterAuthorizationCodeParser.findCodeInPlainText("Found. Redirecting to /login"));
    }

    @Test
    public void findAuthorizationCode_fromJsonData() {
        String json = "{\"success\":true,\"data\":{\"code\":\"json-code\"}}";
        Assert.assertEquals("json-code", AfterRegisterAuthorizationCodeParser.findAuthorizationCode(null, json));
    }

    @Test
    public void findAuthorizationCode_fromLocationHeader() {
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://tenant.example/login-srv/login/handle/afterregister/t1").build())
                .protocol(Protocol.HTTP_1_1)
                .code(302)
                .message("Found")
                .header("Location", "com.app://callback?code=loc-code&state=s")
                .body(ResponseBody.create("", MediaType.parse("text/plain")))
                .build();
        Assert.assertEquals("loc-code", AfterRegisterAuthorizationCodeParser.findAuthorizationCode(response, null));
    }
}
