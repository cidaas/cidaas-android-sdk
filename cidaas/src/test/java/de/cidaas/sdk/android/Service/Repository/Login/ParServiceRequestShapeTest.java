package de.cidaas.sdk.android.Service.Repository.Login;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import de.cidaas.sdk.android.Cidaas;
import de.cidaas.sdk.android.helper.general.GenralHelper;
import de.cidaas.sdk.android.service.repository.Login.ParService;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ParServiceRequestShapeTest {

    @Before
    public void setUp() {
        Cidaas.extraParams = null;
    }

    @Test
    public void buildParFormBody_containsRequiredOauthFields() {
        Map<String, String> form = ParService.buildParFormBody(
                "client-id",
                "https://app.example/callback",
                "challenge-value",
                "state123",
                null);

        Assert.assertEquals("client-id", form.get("client_id"));
        Assert.assertEquals("code", form.get("response_type"));
        Assert.assertEquals("https://app.example/callback", form.get("redirect_uri"));
        Assert.assertEquals("challenge-value", form.get("code_challenge"));
        Assert.assertEquals(GenralHelper.codeChallengeMethod, form.get("code_challenge_method"));
        Assert.assertEquals("S256", form.get("code_challenge_method"));
        Assert.assertEquals("state123", form.get("state"));
        Assert.assertEquals("openid profile", form.get("scope"));
        Assert.assertNull(form.get("nonce"));
    }

    @Test
    public void buildParFormBody_mergesExtraParamsAndCustomScope() {
        Map<String, String> extra = new HashMap<>();
        extra.put("scope", "openid email");
        extra.put("prompt", "login");

        Map<String, String> form = ParService.buildParFormBody(
                "c", "https://r", "ch", "st", extra);

        Assert.assertEquals("openid email", form.get("scope"));
        Assert.assertEquals("login", form.get("prompt"));
    }

    @Test
    public void buildParFormBody_doesNotAllowExtraParamsToOverwritePkceFields() {
        Map<String, String> extra = new HashMap<>();
        extra.put("code_challenge", "attacker-challenge");
        extra.put("client_id", "attacker-client");
        extra.put("prompt", "consent");

        Map<String, String> form = ParService.buildParFormBody(
                "client-id",
                "https://app.example/callback",
                "challenge-value",
                "state123",
                extra);

        Assert.assertEquals("challenge-value", form.get("code_challenge"));
        Assert.assertEquals("client-id", form.get("client_id"));
        Assert.assertEquals("consent", form.get("prompt"));
    }
}
