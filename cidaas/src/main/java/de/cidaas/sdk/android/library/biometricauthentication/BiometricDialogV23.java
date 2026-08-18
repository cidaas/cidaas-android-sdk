package de.cidaas.sdk.android.library.biometricauthentication;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import de.cidaas.sdk.android.R;


public class BiometricDialogV23 extends BottomSheetDialog implements View.OnClickListener {

    private Context context;

    private Button btnCancel;
    // private ImageView imgLogo;
    private TextView itemTitle, itemDescription, itemSubtitle, itemStatus;

    private BiometricCallback biometricCallback;

    public BiometricDialogV23(@NonNull Context context) {
        super(context);
        this.context = context.getApplicationContext();
        //setDialogView();

        setAlertDialog();
    }

    private void setAlertDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.view_bottom_sheet_2, null);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);

        itemTitle = findViewById(R.id.item_title);
        itemStatus = findViewById(R.id.item_status);
        itemSubtitle = findViewById(R.id.item_subtitle);
        itemDescription = findViewById(R.id.item_description);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        AlertDialog mAlertDialog = dialogBuilder.create();
        if (mAlertDialog.getWindow() != null) {
            mAlertDialog.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
        mAlertDialog.show();
    }

    public BiometricDialogV23(@NonNull Context context, BiometricCallback biometricCallback) {
        super(context);
        this.context = context.getApplicationContext();
        this.biometricCallback = biometricCallback;
        setDialogView();
    }

    public BiometricDialogV23(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected BiometricDialogV23(@NonNull Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void setDialogView() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.view_bottom_sheet_2, null);
        setContentView(bottomSheetView);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);

        itemTitle = findViewById(R.id.item_title);
        itemStatus = findViewById(R.id.item_status);
        itemSubtitle = findViewById(R.id.item_subtitle);
        itemDescription = findViewById(R.id.item_description);

        //  updateLogo();
    }

    public void setTitle(String title) {
        itemTitle.setText(title);
    }

    public void updateStatus(String status) {
        itemStatus.setText(status);
    }

    public void setSubtitle(String subtitle) {
        itemSubtitle.setText(subtitle);
    }

    public void setDescription(String description) {
        itemDescription.setText(description);
    }

    public void setButtonText(String negativeButtonText) {
        btnCancel.setText(negativeButtonText);
    }

   /* private void updateLogo() {
        try {
            Drawable drawable = getContext().getPackageManager().getApplicationIcon(context.getPackageName());
            imgLogo.setImageDrawable(drawable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void onClick(View view) {
        dismiss();
        biometricCallback.onAuthenticationCancelled();
    }
}
