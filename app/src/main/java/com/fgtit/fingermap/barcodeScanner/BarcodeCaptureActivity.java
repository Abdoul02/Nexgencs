package com.fgtit.fingermap.barcodeScanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;

import com.fgtit.fingermap.R;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class BarcodeCaptureActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener{
    BarcodeReader barcodeReader;
    public static final String BarcodeObject = "Barcode";
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_capture);

        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
    }

    @Override
    public void onScanned(Barcode barcode) {
        barcodeReader.playBeep();
        if(barcode.displayValue != null){
            Intent data = new Intent();
            data.putExtra(BarcodeObject, barcode);
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
        }
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }
}
