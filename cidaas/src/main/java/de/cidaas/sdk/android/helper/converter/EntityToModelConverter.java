package de.cidaas.sdk.android.helper.converter;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.UUID;

import javax.crypto.Cipher;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.DBHelper;
import de.cidaas.sdk.android.helper.logger.LogFile;
import de.cidaas.sdk.android.models.dbmodel.AccessTokenModel;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.helper.crypthelper.AESCrypt;

/**
 * Created by widasrnarayanan on 13/2/18.
 */

public class EntityToModelConverter {
    // Convert AccessTokenEntity to Model
    Context context;
    private String KEY_ALIAS = "Salt";
    public static EntityToModelConverter sharedinstance;

    public EntityToModelConverter(Context contextFromCidaas) {
        context = contextFromCidaas;
    }

    public static EntityToModelConverter getShared(Context contextFromCidaas) {
        if (sharedinstance == null) {
            sharedinstance = new EntityToModelConverter(contextFromCidaas);
        }
        return sharedinstance;
    }

    // Entity to Model Conversion

    // convert accessTokenEntity To AccessTokenModel
    public void accessTokenEntityToAccessTokenModel(AccessTokenEntity accessTokenEntity, String userId,
            EventResult<AccessTokenModel> callback) {
        String methodName = "accessTokenEntityToAccessTokenModel";
        try {
            String EncryptedToken, EncryptedRefreshToken = "";

            AccessTokenModel.getShared().setExpires_in(accessTokenEntity.getExpires_in());
            AccessTokenModel.getShared().setId_token(accessTokenEntity.getId_token());
            AccessTokenModel.getShared().setRefresh_token(accessTokenEntity.getRefresh_token());
            AccessTokenModel.getShared().setScope(accessTokenEntity.getScope());
            AccessTokenModel.getShared().setUserState(accessTokenEntity.getUserstate());

            // Additional Details to store token in Local DB
            AccessTokenModel.getShared().setUserId(userId);
            // AccessTokenModel.getShared().setSalt();
            String accessToken_salt = UUID.randomUUID().toString();
            String refreshToken_salt = UUID.randomUUID().toString();

            byte[] encData = (accessToken_salt + "," + refreshToken_salt).getBytes("UTF-8");

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Delete if already exist
            if (keyStore.containsAlias(KEY_ALIAS)) {

                keyStore.deleteEntry(KEY_ALIAS);
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setKeySize(2048)
                    .build();

            keyPairGenerator.initialize(keyGenParameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = null;
            Certificate certificate = keyStore.getCertificate(KEY_ALIAS);
            if (certificate != null) {
                publicKey = certificate.getPublicKey();
            } else {
                callback.failure(WebAuthError.getShared(context).accessTokenException("Access token Conversion failed",
                        "accessTokenEntityToAccessTokenModel"));
                LogFile.getShared(context)
                        .addFailureLog("accessTokenEntityToAccessTokenModel \"Certificate not found in the KeyStore.\""
                                + WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
            }
            // null check
            if (publicKey != null) {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                // Store the encrypted data securely in SharedPreferences
                byte[] encryptedData = cipher.doFinal(encData);

                String encryptedString = Base64.encodeToString(encryptedData, Base64.DEFAULT);

                DBHelper.getShared().setEncryptedData(encryptedString);
            } else {
                callback.failure(WebAuthError.getShared(context).accessTokenException("Access token Conversion failed",
                        "accessTokenEntityToAccessTokenModel"));
                LogFile.getShared(context).addFailureLog("accessTokenEntityToAccessTokenModel public key is Null"
                        + WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
            }

            // AccessTokenModel.getShared().setKey(UUID.randomUUID().toString());
            // Convert Milliseconds into seconds
            AccessTokenModel.getShared().setSeconds(System.currentTimeMillis() / 1000);

            // Encrypt the AccessToken
            try {
                EncryptedToken = AESCrypt.encrypt(accessToken_salt, accessTokenEntity.getAccess_token());
            } catch (Exception e) {
                EncryptedToken = "";
            }
            try {
                EncryptedRefreshToken = AESCrypt.encrypt(refreshToken_salt, accessTokenEntity.getRefresh_token());
            } catch (Exception e) {
                EncryptedRefreshToken = "";
            }
            if (EncryptedToken != "") {
                AccessTokenModel.getShared().setAccess_token(EncryptedToken);
                AccessTokenModel.getShared().setRefresh_token(EncryptedRefreshToken);
                AccessTokenModel.getShared().setEncrypted(true);
            } else {
                // AccessTokenModel.getShared().setAccess_token(accessTokenEntity.getAccess_token());
                AccessTokenModel.getShared().setEncrypted(false);
                // AccessTokenModel.getShared().setPlainToken(accessTokenEntity.getAccess_token());
            }
            DBHelper.getShared().setAccessToken(AccessTokenModel.getShared());
            callback.success(AccessTokenModel.getShared());

        } catch (Exception e) {
            // Handle Error
            callback.failure(WebAuthError.getShared(context).accessTokenException(e.getMessage(), methodName));
            LogFile.getShared(context).addFailureLog(e.getMessage() + WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
        }
    }

    // Model to Entity Conversion

    // convert accessTokenModel to AccessTokenEntity
    public void accessTokenModelToAccessTokenEntity(AccessTokenModel accessTokenModel, String userId,
            EventResult<AccessTokenEntity> callback) {
        try {
            AccessTokenEntity accessTokenEntity = new AccessTokenEntity();

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Retrieve the private key
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);

            if (privateKey != null) {
                // Decrypt the stored data using the private key
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                // Retrieve the encrypted data from SharedPreferences
                String encString = DBHelper.getShared().getEncryptedData();
                byte[] encryData = Base64.decode(encString, Base64.DEFAULT);

                byte[] decryptedData = cipher.doFinal(encryData);

                // Convert the decrypted data to a string and return it
                String decryptedString = new String(decryptedData, "UTF-8");

                String accessToken_salt = decryptedString.split(",")[0];
                String refreshToken_salt = decryptedString.split(",")[1];

                if (accessTokenModel.getAccess_token() != null && !accessTokenModel.getAccess_token().equals("")) {
                    accessTokenEntity.setAccess_token(accessTokenModel.getAccess_token());
                }

                if (accessTokenModel.getId_token() != null && !accessTokenModel.getId_token().equals("")) {
                    accessTokenEntity.setId_token(accessTokenModel.getId_token());
                }

                if (accessTokenModel.getRefresh_token() != null && !accessTokenModel.getRefresh_token().equals("")) {
                    accessTokenEntity.setRefresh_token(accessTokenModel.getRefresh_token());
                }
                if (accessTokenModel.getScope() != null && !accessTokenModel.getScope().equals("")) {
                    accessTokenEntity.setScope(accessTokenModel.getScope());
                }
                if (accessTokenModel.getUserState() != null && !accessTokenModel.getUserState().equals("")) {
                    accessTokenEntity.setUserstate(accessTokenModel.getUserState());
                }

                accessTokenEntity.setExpires_in(accessTokenModel.getExpires_in());

                // Decrypt the AccessToken
                if (accessTokenModel.isEncrypted()) {
                    if (accessTokenEntity.getAccess_token() != null
                            && !accessTokenEntity.getAccess_token().equals("")) {
                        accessTokenEntity
                                .setAccess_token(
                                        AESCrypt.decrypt(accessToken_salt, accessTokenEntity.getAccess_token()));
                    }
                    if (accessTokenEntity.getRefresh_token() != null
                            && !accessTokenEntity.getRefresh_token().equals("")) {
                        accessTokenEntity.setRefresh_token(
                                AESCrypt.decrypt(refreshToken_salt, accessTokenEntity.getRefresh_token()));
                    }

                } else {
                    /*
                     * if(accessTokenModel.getPlainToken()==null ||
                     * (accessTokenModel.getPlainToken().equals("")))
                     * {
                     * accessTokenModel.setPlainToken(accessTokenEntity.getAccess_token());
                     * }
                     * accessTokenEntity.setAccess_token(accessTokenModel.getPlainToken());
                     */
                }
                callback.success(accessTokenEntity);
            } else {
                callback.failure(WebAuthError.getShared(context).accessTokenException("Access token Conversion failed",
                        "accessTokenModelToAccessTokenEntity"));
                LogFile.getShared(context)
                        .addFailureLog("Private key is Null" + WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
            }
        } catch (Exception e) {
            // Handle Error
            callback.failure(WebAuthError.getShared(context).accessTokenException(e.getMessage(),
                    "accessTokenModelToAccessTokenEntity"));
            LogFile.getShared(context).addFailureLog(e.getMessage() + WebAuthErrorCode.ACCESS_TOKEN_CONVERSION_FAILURE);
        }
    }

}
