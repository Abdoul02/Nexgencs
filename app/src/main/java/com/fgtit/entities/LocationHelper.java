package com.fgtit.entities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationHelper implements PermissionUtils.PermissionResultCallback{

    private Context context;
    private Activity current_activity;
    private boolean isPermissionGranted;
    private Location mLastLocation;

    // list of permissions
    private ArrayList<String> permissions = new ArrayList<>();
    private PermissionUtils permissionUtils;

    private final static int REQUEST_CHECK_SETTINGS = 2000;

    public LocationHelper(Context context) {

        this.context=context;
        this.current_activity= (Activity) context;

        permissionUtils=new PermissionUtils(context,this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.CAMERA);

    }

/**
 * Method to check the availability of location permissions
 * */

public void checkpermission()
        {
        permissionUtils.check_permission(permissions,"Need GPS permission for getting your location",1);
        }

private boolean isPermissionGranted() {
        return isPermissionGranted;
        }

/**
 * Handles the permission results
 */
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
        {
        permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }

/**
 * Handles the activity results
 */
public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CHECK_SETTINGS:
        switch (resultCode) {
        case Activity.RESULT_OK:
        // All required changes were successfully made
        //mLastLocation=getLocation();
        break;
        case Activity.RESULT_CANCELED:
        // The user was asked to change settings, but chose not to
        break;
default:
        break;
        }
        break;
        }
        }

@Override
public void PermissionGranted(int request_code) {
        Log.i("PERMISSION","GRANTED");
        isPermissionGranted=true;
        }

@Override
public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY","GRANTED");
        }

@Override
public void PermissionDenied(int request_code) {
        Log.i("PERMISSION","DENIED");
        }

@Override
public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION","NEVER ASK AGAIN");
        }
}
