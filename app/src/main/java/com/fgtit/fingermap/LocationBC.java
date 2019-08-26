package com.fgtit.fingermap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

/**
 * Created by Abdoul on 14-12-2016.
 */
public class LocationBC extends BroadcastReceiver {

    GPSTracker gps;
    static int noOfTimes = 0;

    // Method gets called when Broad Case is issued from MainActivity for every 10 seconds
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub

        gps = new GPSTracker(context);
        noOfTimes++;
        Toast.makeText(context, "BC Service Running for " + noOfTimes + " times", Toast.LENGTH_SHORT).show();
        String lat,lon;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        String provider = locationManager.getBestProvider(criteria, false);

        Location loc = locationManager.getLastKnownLocation(provider);



        MainActivity.MyLocation mylocalListener = new MainActivity.MyLocation();

        if (loc != null) {

            mylocalListener.onLocationChanged(loc);

            lat = String.valueOf(loc.getLatitude());
            lon = String.valueOf(loc.getLongitude());
        }
        else{

            lat = String.valueOf(gps.getLatitude());
            lon = String.valueOf(gps.getLongitude());
        }

        final String identifier;
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null)
            identifier = telephonyManager.getDeviceId();
        else{
            identifier = "Not available";
        }

        sendLocation(lon,lat,identifier,context);
        locationManager.requestLocationUpdates(provider, 300*1000, 5, mylocalListener);
    }


    public void sendLocation (String longitude, String latitude, String imei, final Context context){

        try{


            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("lat", latitude);
            jsonObject.accumulate("lon", longitude);
            jsonObject.accumulate("imei", imei);
            //  convert JSONObject to JSON to String
            json = jsonObject.toString();
            params.put("locate", json);

            client.post("http://www.nexgencs.co.za/api/location.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                    Toast.makeText(context, "Coordinates sent successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    // TODO Auto-generated method stub
                    if (statusCode == 404) {
                        Toast.makeText(context, "Error code: 404", Toast.LENGTH_SHORT).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(context, "Error code: 500", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error occured!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e) {

            Log.d("InputStream", e.getLocalizedMessage());
        }


    }
}
