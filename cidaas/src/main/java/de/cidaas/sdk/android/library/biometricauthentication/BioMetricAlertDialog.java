package de.cidaas.sdk.android.library.biometricauthentication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import de.cidaas.sdk.android.R;


public class BioMetricAlertDialog extends AlertDialog implements View.OnClickListener {

    Context context;

    private BiometricCallback biometricCallback;


    AlertDialog mAlertDialog;

    private Button btnCancel;
    // private ImageView imgLogo;
    private TextView itemTitle, itemDescription, itemSubtitle, itemStatus;

    public BioMetricAlertDialog(@NonNull Context context, BiometricCallback biometricCallback) {
        super(context, R.style.BottomSheetDialogTheme);

        this.context = context.getApplicationContext();
        this.biometricCallback = biometricCallback;
        setDialogView();
    }

    private void setDialogView() {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.view_bottom_sheet_2, null);

        setContentView(dialogView);

        btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);

        itemTitle = dialogView.findViewById(R.id.item_title);
        itemStatus = dialogView.findViewById(R.id.item_status);
        itemSubtitle = dialogView.findViewById(R.id.item_subtitle);
        itemDescription = dialogView.findViewById(R.id.item_description);


        // AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        //  dialogBuilder.setView(dialogView);
        //  dialogBuilder.setCancelable(false);

        //  mAlertDialog = dialogBuilder.create();
        //mAlertDialog.show();

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
