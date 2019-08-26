package com.fgtit.entities;

/**
 * Created by Abdoul on 19-09-2016.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MainActivity;
import com.fgtit.fingermap.R;
import com.fgtit.fingermap.UserList;
import com.fgtit.fingermap.UtilitiesActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.entities.App.CHANNEL_ID;

public class MyService extends Service {
    int numMessages = 0;
    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;
    Dialog dialog;
    Handler requestHandler = new Handler(Looper.getMainLooper());

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        this.context = this;
        this.isRunning = false;
        //setDialog(true);
        Toast.makeText(this, "Downloading users...", Toast.LENGTH_LONG).show();

    }

    final Handler responseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            Toast.makeText(MyService.this,
                    "Runnable completed with result: "+(String)msg.obj,
                    Toast.LENGTH_LONG)
                    .show();
        }
    };
    private Runnable myTask = new Runnable() {
        public void run() {
            // Do something here
            try {

                JobDB db = new JobDB(context);

                //uploadRecord(db,context);
                postRequest("68");

            } catch (Exception err) {
                err.printStackTrace();
            }
            stopSelf();
        }
    };



    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {

        if(!this.isRunning){
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

          /*  Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Download Employee information")
                    .setContentText("Downloading users...")
                    .setSmallIcon(R.drawable.download)
                    .setContentIntent(pendingIntent)
                    .build();*/

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
            Notification notification = builder.setContentTitle("Download Employee information")
                    .setContentText("Downloading users...")
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.download)
                    .setContentIntent(pendingIntent)
                    .build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);

            startForeground(1, notification);
            this.isRunning = true;
            //this.backgroundThread.start();
            requestHandler.post(myTask);

        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    public void uploadRecord(final JobDB mydb, final Context context){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  mydb.getAllrecord();
        if(userList.size()!=0){
            if(mydb.dbSyncCoun() != 0) {

                params.put("recordJSON", mydb.composeJSONfromSQLite());
                client.setTimeout(50*1000);
                client.post("http://www.nexgencs.co.za/version2/syncRecord.php",params ,new AsyncHttpResponseHandler() {
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
                                    mydb.updateSyncStatus(obj.get("recId").toString(), obj.get("status").toString());
                                    mydb.deleteRecord(obj.get("recId").toString());
                                }else{

                                    mydb.updateSyncStatus(obj.get("recId").toString(), obj.get("status").toString());
                                }
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

    public Object postRequest(String param) throws IOException {


        JSONObject postDataParams = new JSONObject();
        //postDataParams.accumulate("","");

        OkHttpClient client = new OkHttpClient();
        //RequestBody body = RequestBody.create(JSON, param);
        RequestBody body = new FormBody.Builder()
                .add("userJSON","userJSON")
                .add("compID",param)
                .build();
        Request request = new Request.Builder()
                .url("http://www.nexgencs.co.za/avl_api/getUsers.php")
                .post(body)
                .build();
        //Log.d("PARAM:+++",param[0]+ " "+param[1]);

        client.newCall(request).enqueue(new Callback() {

            Handler handler = new Handler(MyService.this.getMainLooper());
            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();

                Message msg = new Message();
                msg.obj = e.getMessage();
                responseHandler.sendMessage(msg);


               /* if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error: "+e.getMessage());
                    }
                });*/


            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(!response.isSuccessful()){

                          /*  if(dialog != null && dialog.isShowing()){
                                dialog.dismiss();
                            }
                            showToast("Unexpected error: "+response.message());*/
                            Message msg = new Message();
                            msg.obj = response.message();
                            responseHandler.sendMessage(msg);


                        }else{

                         /*   if(dialog != null && dialog.isShowing()){
                                dialog.dismiss();
                            }*/
                            try {
                                displayResponse(response.body().string());

                            }catch (IOException e){
                                e.printStackTrace();
                            }

                        }


                    }
                });
            }
        });

        return null;
    }

    public void displayResponse(String response){

        DBHandler db = new DBHandler(this);

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
                    User user = new User();
                    user.setuId(Integer.parseInt(obj.get("userId").toString()));
                    user.setIdNum(obj.get("idNum").toString());
                    user.setuName(obj.get("name").toString());
                    user.setFinger1(obj.get("finger1").toString());
                    user.setFinger2(obj.get("finger2").toString());

                    user.setCostCenterId(Integer.parseInt(obj.get("costCenterId").toString()));
                    user.setShifts_id(Integer.parseInt(obj.get("shifts_id").toString()));
                    user.setShift_type(Integer.parseInt(obj.get("shift_type").toString()));
                    user.setCard(obj.get("card").toString());
                    db.insertUser(user);
                    HashMap<String, String> map = new HashMap<String, String>();
                    // Add status for each User in Hashmap
                    map.put("Id", obj.get("userId").toString());
                    map.put("status", "1");
                    usersynclist.add(map);
                }


                gotoUserList();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            Message msg = new Message();
            msg.obj = e.getMessage();
            responseHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    public void gotoUserList() {
        //Intent objIntent = new Intent(getApplicationContext(), UserList.class);
        //startActivity(objIntent);

        Intent dialogIntent = new Intent(this, UserList.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

}