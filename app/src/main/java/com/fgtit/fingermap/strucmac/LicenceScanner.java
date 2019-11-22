package com.fgtit.fingermap.strucmac;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.R;
import com.fgtit.fingermap.barcodeScanner.BarcodeCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.nio.charset.StandardCharsets;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.co.exid.exidlicense.ExIdLicense;
import za.co.exid.exidlicense.ExIdLicenseStartupActivity;
import za.co.exid.exidlicense.SADLCardInfo;

public class LicenceScanner extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.txt_driverName)
    TextView driverName;
    @BindView(R.id.txt_driverSurname)
    TextView driverSurname;
    @BindView(R.id.txt_driverId)
    TextView driverId;
    @BindView(R.id.imv_driver)
    ImageView driverPhoto;
    @BindView(R.id.read_barcode)
    Button btnScan;
    @BindView(R.id.btn_saveInfo)
    Button saveInfo;
    @BindView(R.id.auto_focus)
    CompoundButton autoFocus;
    @BindView(R.id.use_flash)
    CompoundButton useFlash;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    CommonFunction commonFunction = new CommonFunction(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence_scanner);
        ButterKnife.bind(this);

        btnScan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        byte[] rawLicenseCard = barcode.rawValue.getBytes(StandardCharsets.ISO_8859_1);
                        exidInit(rawLicenseCard);
                    } else {
                        exidInit(barcode.displayValue.getBytes());
                    }
                } else {
                    commonFunction.showToast("No licence information captured");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Decoding
    public void exidInit(byte[] byteArray) {
        //exidInitSimple();
        exidInitAdvanced(byteArray);
    }

    private void exidInitAdvanced(final byte[] byteArray) {
        ExIdLicense lic = ExIdLicense.getInstance();
        lic.registerUser = "michael@nexgencs.co.za";
        lic.registerPass = "N3x84mpldL0Oo\\";
        lic.registerAccount = "NexGen01";
        Log.d("Password", lic.registerPass);


        ExIdLicense.ExIdLicenseInitCallbacks exidCallbacks = new ExIdLicense.ExIdLicenseInitCallbacks() {
            @Override
            public void onInitCompleted(ExIdLicense inst) {
                licenseStatusCallback(inst.IsAuthenticated(), byteArray);
            }
        };

        ExIdLicense.ExIdLicenseCallbacks exidLicenseCallbacks = new ExIdLicense.ExIdLicenseCallbacks() {
            @Override
            public void onLicenseFailed(String s) {
                licenseStatusCallback(false, byteArray);
            }

            @Override
            public void onLicenseSuccess(String s) {
                licenseStatusCallback(true, byteArray);
            }
        };

        ExIdLicense.getInstance().init(exidLicenseCallbacks, this.getApplicationContext());
        ExIdLicense.getInstance().setExIdLicenseInitCallbacks(exidCallbacks);
        licenseStatusCallback(ExIdLicense.getInstance().IsAuthenticated(), byteArray);
    }

    private void licenseStatusCallback(boolean isValid, byte[] byteArray) {
        if (isValid) {
            decodeSampleLicense(byteArray);
        } else {
            //1.    show input screen here to prompt for credentials

            //2.    When credentials are entered update account information like this
            ExIdLicense lic = ExIdLicense.getInstance();
            lic.registerUser = "michael@nexgencs.co.za";
            lic.registerPass = "N3x84mpldL0Oo\\";
            lic.registerAccount = "NexGen01";
            Log.d("Password", lic.registerPass);

            //3. Fire the registration process
            Intent i = new Intent(this, ExIdLicenseStartupActivity.class);
            startActivity(i);
        }
    }

    private void decodeSampleLicense(byte[] byteArray) {
        SADLCardInfo sadl = ExIdLicense.getInstance().DecodeBarcode(byteArray);
        if (sadl != null) {
            driverName.setText(getString(R.string.driver_name, sadl.Initials()));
            driverSurname.setText(getString(R.string.driver_surname, sadl.LastName()));
            driverId.setText(getString(R.string.driver_id, sadl.IDNo()));
            Bitmap bmp = BitmapFactory.decodeByteArray(sadl.FacialImage(), 0, sadl.FacialImage().length);
            driverPhoto.setImageBitmap(bmp);
        } else {
            commonFunction.showToast("Could not retrieved driver's information");
        }
    }
}
