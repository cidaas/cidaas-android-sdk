package de.cidaas.sdk.android.helper.encryption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Selects an encryption public key from a JWKS document.
 * Any key with {@code use=enc}, {@code kty=EC}, {@code alg=ECDH-ES} (and preferably {@code crv=P-256}) is valid.
 * When several keys match, one is chosen at random.
 */
public final class JwksEncKeySelector {

    private static Random random = new SecureRandom();

    private JwksEncKeySelector() {
    }

    @VisibleForTesting
    static void setRandom(@NonNull Random randomOverride) {
        random = randomOverride;
    }

    @VisibleForTesting
    static void resetRandom() {
        random = new SecureRandom();
    }

    /**
     * Picks a suitable encryption key from JWKS JSON.
     * If multiple {@code use:enc} ECDH-ES P-256 keys are present, one is selected at random.
     */
    @Nullable
    public static ECKey selectEncryptionKey(@NonNull String jwksJson) throws Exception {
        JSONObject root = new JSONObject(jwksJson);
        JSONArray keys = root.optJSONArray("keys");
        if (keys == null || keys.length() == 0) {
            return null;
        }

        List<ECKey> candidates = new ArrayList<>();
        for (int i = 0; i < keys.length(); i++) {
            ECKey candidate = parseCandidate(keys.getJSONObject(i));
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    @Nullable
    private static ECKey parseCandidate(@NonNull JSONObject key) throws Exception {
        if (!"enc".equals(key.optString("use"))) {
            return null;
        }
        if (!"EC".equals(key.optString("kty"))) {
            return null;
        }
        String alg = key.optString("alg");
        if (alg != null && !alg.isEmpty() && !"ECDH-ES".equals(alg)) {
            return null;
        }
        String crv = key.optString("crv", "P-256");
        if (!"P-256".equals(crv)) {
            return null;
        }
        String kid = key.optString("kid");
        String x = key.optString("x");
        String y = key.optString("y");
        if (x.isEmpty() || y.isEmpty()) {
            return null;
        }
        ECKey.Builder builder = new ECKey.Builder(Curve.P_256, new Base64URL(x), new Base64URL(y))
                .keyUse(KeyUse.ENCRYPTION);
        if (kid != null && !kid.isEmpty()) {
            builder.keyID(kid);
        }
        if (alg != null && !alg.isEmpty()) {
            builder.algorithm(com.nimbusds.jose.Algorithm.parse(alg));
        }
        return builder.build();
    }
}
