package com.fgtit.models;

/**
 * Created by Abdoul on 19-09-2016.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.GPSTracker;
import com.fgtit.fingermap.JobDB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SampleBC extends BroadcastReceiver {
    static int noOfTimes = 0;
    GPSTracker gps;

    // User DB Class to perform DB related operations



    // Method gets called when Broad Case is issued from MainActivity for every 10 seconds
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub

        final DBHandler db = new DBHandler(context);
        //record db
        final JobDB mydb = new JobDB(context);

        if(mydb.dbSyncCoun() > 0 ){

            Intent background = new Intent(context,MyService.class);
            context.startService(background);
        }

     /*   if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            Intent alarmIntent = new Intent(context, SampleBC.class);
            boolean alarmRunning = (PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null);

            if(alarmRunning == false) {

                // Pending Intent Object
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
                // Alarm Manager Object
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                // Alarm Manager calls BroadCast for every Ten seconds (10 * 1000), BroadCase further calls service to check if new records are inserted in
                // Remote MySQL DB
                //2hours
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 600000*12, pendingIntent);
            }else
                Toast.makeText(context, "Alarm is already running", Toast.LENGTH_SHORT).show();

        }*/


    }


    public void uploadRecord(final JobDB mydb,final Context context){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  mydb.getAllrecord();
        if(userList.size()!=0){
            if(mydb.dbSyncCoun() != 0) {

                params.put("recordJSON", mydb.composeJSONfromSQLite());
                client.setTimeout(50*1000);
                client.post("http://www.nexgencs.co.za/api/recInsert.php",params ,new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println(arr.length());
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                System.out.println(obj.get("id"));
                                System.out.println(obj.get("status"));
                                String status = obj.get("status").toString();
                                if(status.equals("yes")){
                                    mydb.updateSyncStatus(obj.get("id").toString(), obj.get("status").toString());
                                    mydb.deleteRecord(obj.get("id").toString());
                                }else{

                                    mydb.updateSyncStatus(obj.get("id").toString(), obj.get("status").toString());
                                }

                                //Updating users fingerprints on the server
                                //syncData(db,context);
                                //Toast.makeText(getApplicationContext(), obj.get("id").toString() + " "+ obj.get("status").toString(), Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(context, "Sync completed!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(context, "Server Error Occurred", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub
                        Toast.makeText(context, "Network Error please make sure you have data connection", Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                Toast.makeText(context, "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
                //even if the DB's are in sync, try to update users fingerprints
                // syncData(db, context);
            }
        }else{
            Toast.makeText(context, "No records to perform Sync action", Toast.LENGTH_LONG).show();
            //even if there are no records, try to update users fingerprints
            //syncData(db,context);
        }

    }


    public void syncData(final DBHandler db, final Context context){
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<User> userList =  db.getAllUsers();
        if(userList.size()!=0){
            if(db.dbSyncCount() != 0){
                //Updating users fingerprints to the server
                params.put("usersJSON", db.composeJSONfromSQLite());
                client.post("http://www.nexgencs.co.za/api/updateUser.php",params ,new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);

                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println(arr.length());
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                                System.out.println(obj.get("idNum"));
                                System.out.println(obj.get("status"));
                                db.updateSyncStatus(obj.get("idNum").toString(), obj.get("status").toString());

                            }
                            //download and send the users info to SQLite
                            //syncSQLiteMySQLDB(context, db);
                            sendLocation(context);
                            Toast.makeText(context, "DB Sync completed!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub

                        Toast.makeText(context, "Network Error please make sure you have data connection", Toast.LENGTH_LONG).show();
                        sendLocation(context);
                    }
                });
            }else{
                Toast.makeText(context, "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
               // syncSQLiteMySQLDB(context, db);
                sendLocation(context);
            }
        }else{
            Toast.makeText(context, "No data to perform Sync action", Toast.LENGTH_LONG).show();
            //download and send the users info to SQLite
            //syncSQLiteMySQLDB(context,db);
            sendLocation(context);
        }
    }

    // Method to download employee information and insert in to SQLite DB
    public void syncSQLiteMySQLDB(final Context context, final DBHandler db) {


        try{
            // Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            SessionManager session =  new SessionManager(context);

           // session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int compID = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));

            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("dat", "dummy");
            jsonObject.accumulate("compID",compID);
            //  convert JSONObject to JSON to String
            json = jsonObject.toString();


            params.put("userJSON",json);
            client.post("http://www.nexgencs.co.za/api/getusers.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    // Hide ProgressBar

                    // Update SQLite DB with response sent by getusers.php
                    updateSQLite(response,db,context);
                }
                // When error occured
                @Override
                public void onFailure(int statusCode, Throwable error, String content) {
                    // TODO Auto-generated method stub
                    // Hide ProgressBar

                    Toast.makeText(context, "Network Error please make sure you have data connection", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){

            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void updateSQLite(String response, DBHandler db, Context context){
        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            System.out.println(arr.length());
            // If no of array elements is not zero
            if(arr.length() != 0){

                db.deleteAll();
                // Loop through each array element, get JSON object which has userid and username
                for (int i = 0; i < arr.length(); i++) {
                    // Get JSON object
                    JSONObject obj = (JSONObject) arr.get(i);
					/*System.out.println(obj.get("userId"));
					System.out.println(obj.get("idNum"));
					System.out.println(obj.get("name"));  */

                    User user = new User();

                    // Add userID extracted from Object
                    user.setuId(Integer.parseInt(obj.get("userId").toString()));

                    //Add ID Number from Object
                    user.setIdNum(obj.get("idNum").toString());

                    // Add name extracted from Object
                    user.setuName(obj.get("name").toString());

                    // Add finger1 extracted from Object
                    user.setFinger1(obj.get("finger1").toString());

                    // Add finger2 extracted from Object
                    user.setFinger2(obj.get("finger2").toString());

                    //delete duplicate before entering them
                    //db.delete(obj.get("idNum").toString());
                    // Insert User into SQLite DB
                    db.insertUser(user);
                    HashMap<String, String> map = new HashMap<String, String>();
                    // Add status for each User in Hashmap
                    map.put("Id", obj.get("userId").toString());
                    map.put("status", "1");
                    usersynclist.add(map);
                }


                // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
                updateMySQLSyncSts(gson.toJson(usersynclist),arr.length(),context);
                // Reload the Main Activity
               // reloadActivity();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Method to inform remote MySQL DB about completion of Sync activity
    public void updateMySQLSyncSts(String json,final int c, final Context context) {
        System.out.println(json);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("syncsts", json);
        // Make Http call to updatesyncsts.php with JSON parameter which has Sync statuses of Users
        client.post("http://www.nexgencs.co.za/api/updatesyncsts.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(context, String.valueOf(c)+" employees imported", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                Toast.makeText(context, "Error Occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendLocation (final Context context){


        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        criteria.setCostAllowed(false);
        // get the best provider depending on the criteria
        String provider = locationManager.getBestProvider(criteria, false);
        Location loc = locationManager.getLastKnownLocation(provider);
        String lat,lon;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        MyLocation mylocalListener = new MyLocation();

        if (loc != null) {

            mylocalListener.onLocationChanged(loc);

            lat = String.valueOf(loc.getLatitude());
            lon = String.valueOf(loc.getLongitude());
        }
        else{

            lat = String.valueOf(gps.getLatitude());
            lon = String.valueOf(gps.getLongitude());
        }
        final String imei;
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null)
            imei = telephonyManager.getDeviceId();
        else{
            imei = "Not available";
        }

        try{


            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("lat", lat);
            jsonObject.accumulate("lon", lon);
            jsonObject.accumulate("imei", imei);
            jsonObject.accumulate("dat", currentDateandTime);
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

        locationManager.requestLocationUpdates(provider, 300*1000, 5, mylocalListener);
    }

    static class MyLocation implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // Initialize the location fields

          /*  Loc loc = new Loc();
            loc.setLat(location.getLatitude());
            loc.setLon(location.getLongitude());  */

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Toast.makeText(MainActivity.this, provider + "'s status changed to "+status +"!",
            //	Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
            //	Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            //Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
            //		Toast.LENGTH_SHORT).show();
        }
    }
}
