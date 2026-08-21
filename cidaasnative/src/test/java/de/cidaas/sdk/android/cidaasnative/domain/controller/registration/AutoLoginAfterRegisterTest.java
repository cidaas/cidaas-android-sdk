package de.cidaas.sdk.android.cidaasnative.domain.controller.registration;

import org.junit.Assert;
import org.junit.Test;

import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserDataEntity;
import de.cidaas.sdk.android.cidaasnative.data.entity.register.registeruser.RegisterNewUserResponseEntity;

public class AutoLoginAfterRegisterTest {

    @Test
    public void shouldAttempt_whenActionAutoLoginAndTrackId() {
        Assert.assertTrue(AutoLoginAfterRegister.shouldAttempt(response("AUTO_LOGIN", null, "track-1")));
    }

    @Test
    public void shouldAttempt_whenSuggestedActionAutoLoginAndTrackId() {
        Assert.assertTrue(AutoLoginAfterRegister.shouldAttempt(response(null, "AUTO_LOGIN", "track-1")));
    }

    @Test
    public void shouldAttempt_ignoresCase() {
        Assert.assertTrue(AutoLoginAfterRegister.shouldAttempt(response("auto_login", null, "track-1")));
    }

    @Test
    public void shouldNotAttempt_whenSuggestedActionIsLoginOnly() {
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(response(null, "LOGIN", "track-1")));
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(response("LOGIN", "LOGIN", "track-1")));
    }

    @Test
    public void shouldNotAttempt_whenVerification() {
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(response("VERIFICATION", "VERIFICATION", "track-1")));
    }

    @Test
    public void shouldNotAttempt_whenTrackIdMissing() {
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(response("AUTO_LOGIN", null, "")));
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(response("AUTO_LOGIN", null, "  ")));
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(response("AUTO_LOGIN", null, null)));
    }

    @Test
    public void shouldNotAttempt_whenNotSuccessfulOrNull() {
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(null));
        RegisterNewUserResponseEntity unsuccessful = response("AUTO_LOGIN", null, "track-1");
        unsuccessful.setSuccess(false);
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(unsuccessful));
        RegisterNewUserResponseEntity noData = new RegisterNewUserResponseEntity();
        noData.setSuccess(true);
        Assert.assertFalse(AutoLoginAfterRegister.shouldAttempt(noData));
    }

    @Test
    public void trackId_trimsValue() {
        Assert.assertEquals("track-1", AutoLoginAfterRegister.trackId(response("AUTO_LOGIN", null, "  track-1  ")));
        Assert.assertEquals("", AutoLoginAfterRegister.trackId(null));
        Assert.assertEquals("", AutoLoginAfterRegister.trackId(new RegisterNewUserResponseEntity()));
    }

    private static RegisterNewUserResponseEntity response(String action, String suggestedAction, String trackId) {
        RegisterNewUserDataEntity data = new RegisterNewUserDataEntity();
        data.setAction(action);
        data.setSuggested_action(suggestedAction);
        data.setTrackId(trackId);
        RegisterNewUserResponseEntity entity = new RegisterNewUserResponseEntity();
        entity.setSuccess(true);
        entity.setStatus(200);
        entity.setData(data);
        return entity;
    }
}
