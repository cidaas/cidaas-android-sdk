package de.cidaas.sdk.android.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Dictionary;

import de.cidaas.sdk.android.helper.enums.EventResult;
import de.cidaas.sdk.android.helper.enums.WebAuthErrorCode;
import de.cidaas.sdk.android.helper.extension.WebAuthError;
import de.cidaas.sdk.android.helper.general.CidaasConstants;
import de.cidaas.sdk.android.properties.CidaasProperties;
import de.cidaas.sdk.android.service.entity.accesstoken.AccessTokenEntity;
import de.cidaas.sdk.android.service.entity.documentscanner.DocumentScannerServiceResultEntity;
import de.cidaas.sdk.android.service.repository.DocumemtScannerService.DocumentScannerService;
import timber.log.Timber;

public class DocumentScannnerController {


    File imageFile = null;
    Context context;


    FileOutputStream out = null;

    static DocumentScannnerController shared;

    public DocumentScannnerController(Context contextFromCidaas) {
        this.context = contextFromCidaas;
    }
    //Create File


    public static DocumentScannnerController getShared(Context contextFromCidaas) {
        try {

            if (shared == null) {
                shared = new DocumentScannnerController(contextFromCidaas);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        }
        return shared;
    }

    @NonNull
    private File file() throws IOException {
        File sdDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File pictureFileDir = new File(sdDir, "Cidaas-Faces-docs");
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Toast.makeText(context, "File Storage Permission Denied.Please enable it",
                    Toast.LENGTH_LONG).show();
        }
        imageFile = new File(pictureFileDir, "cidaasDocs.jpg");
        if (imageFile.exists()) {
            imageFile.delete();
        }
        imageFile.createNewFile();
        return imageFile;
    }

    public File convertImageJpeg(Bitmap bitmap) {

        Bitmap picture = bitmap;
        try {
            imageFile = file();

            //Timber.d("Error");
            // Toast.makeText(FaceSetupActivity.this, imageFile.toString(), Toast.LENGTH_SHORT).show();
            if (imageFile == null) {
                // Toast.makeText(FaceSetupActivity.this, "NUll Image", Toast.LENGTH_SHORT).show();
                File sdDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File pictureFileDir = new File(sdDir, "Cidaas-Faces-docs");
                if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                    Toast.makeText(context, "File Storage Permission Denied.Please enable it",
                            Toast.LENGTH_LONG).show();
                }
                imageFile = new File(pictureFileDir, "cidaasDocs.jpg");
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                imageFile.createNewFile();

            }


            out = new FileOutputStream(imageFile);
            picture.compress(Bitmap.CompressFormat.JPEG, 95, out);
            return imageFile;

        } catch (Exception e) {
            Timber.e(e, "Failed to save document scanner image file");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                Timber.e(e, "Failed to close document scanner image output stream");
            }
        }
        return imageFile;
    }

    public void sendtoServicecall(final File photo, final String sub, final EventResult<DocumentScannerServiceResultEntity> resultEntityResult) {

        final String methodName = "DocumentScannnerController :sendtoServicecall()";
        try {

            if (photo != null && !sub.equals("") && sub != null) {

                CidaasProperties.getShared(context).checkCidaasProperties(new EventResult<Dictionary<String, String>>() {
                    @Override
                    public void success(Dictionary<String, String> result) {
                        final String baseurl = result.get("DomainURL");

                        if (baseurl != null && !baseurl.equals("")) {

                            AccessTokenController.getShared(context).getAccessToken(sub, new EventResult<AccessTokenEntity>() {
                                @Override
                                public void success(AccessTokenEntity result) {
                                    sendtoServicecall(baseurl, photo, result.getAccess_token(), resultEntityResult);
                                }

                                @Override
                                public void failure(WebAuthError error) {
                                    resultEntityResult.failure(error);
                                }
                            });


                        } else {
                            resultEntityResult.failure(WebAuthError.getShared(context).propertyMissingException("BaseURL must not be null", "Error" + methodName));
                        }
                    }

                    @Override
                    public void failure(WebAuthError error) {

                        resultEntityResult.failure(error);
                    }
                });
            } else {
                resultEntityResult.failure(WebAuthError.getShared(context).propertyMissingException("Photo or sub must not be null", "Error" + methodName));
            }
        } catch (Exception e) {
            resultEntityResult.failure(WebAuthError.getShared(context).methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DOCUMENT_VERIFICATION_FAILURE, e.getMessage()));

        }
    }


    public void sendtoServicecall(String baseurl, File photo, String accessToken, EventResult<DocumentScannerServiceResultEntity> result) {
        String methodName = "DocumentScannnerController :sendtoServicecall()";
        try {

            if (baseurl != null && !baseurl.equals("") && accessToken != null && !accessToken.equals("")) {
                DocumentScannerService.getShared(context).sendDocumentToService(baseurl, photo, accessToken, null, result);
            } else {
                result.failure(WebAuthError.getShared(context).propertyMissingException("Base URL or AccessToken must not be null", "Error" + methodName));
            }
        } catch (Exception e) {
            result.failure(WebAuthError.getShared(context).methodException(CidaasConstants.EXCEPTION_LOGGING_PREFIX + methodName, WebAuthErrorCode.DOCUMENT_VERIFICATION_FAILURE, e.getMessage()));

        }
    }
}
