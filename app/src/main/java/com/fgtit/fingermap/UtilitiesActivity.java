package com.fgtit.fingermap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fgtit.models.SessionManager;
import com.fgtit.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.data.MyConstants.BASE_URL;

public class UtilitiesActivity extends AppCompatActivity {

    // User DB Class to perform DB related operations
    DBHandler db = new DBHandler(this);


    // Session Manager Class
    SessionManager session;
    int compID;
    int responseCode;
    //record db
    JobDB mydb = new JobDB(this);
    //progress Dialog
    ProgressDialog prgDialog, prgDialog1, prgDialogRecord, prgDialogPine;
    Dialog dialog;

    HashMap<String, String> queryValues;
    private ListView listView;
    private List<Map<String, Object>> mData;
    String serverURL = BASE_URL + "/api/getPineUsers.php";
    String scaleURL = BASE_URL + "/alos/scale_upload.php";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utilities);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> manager = session.getUserDetails();
        compID = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));

        // Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Transferring Data from Server. Please wait...");
        prgDialog.setCancelable(false);

        prgDialog1 = new ProgressDialog(this);
        prgDialog1.setMessage("Syncing Data with Remote server. Please wait...");
        prgDialog1.setCancelable(false);

        prgDialogRecord = new ProgressDialog(this);
        prgDialogRecord.setMessage("Transferring Record to server. please wait...");
        prgDialogRecord.setCancelable(false);

        prgDialogPine = new ProgressDialog(this);
        prgDialogPine.setMessage("Downloading authorized personnel");
        prgDialogPine.setCancelable(false);


        listView = (ListView) findViewById(R.id.listView1);
        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.listview_menuitem,
                new String[]{"title", "info", "img"},
                new int[]{R.id.title, R.id.info, R.id.img});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                //Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);
                switch (pos) {
                    case 0: {
                        //Update Employee information on server
                        syncData();
                    }
                    break;
                    case 1: {
                        if (db.dbSyncCount() > 0) {

                            Toast.makeText(getApplicationContext(), "Please Upload employee information first", Toast.LENGTH_LONG).show();
                        } else {
                            //Download employee record from server DB to SQLite DB

							/*Intent serviceIntent = new Intent(getApplicationContext(), MyService.class);
							serviceIntent.putExtra("inputExtra", "test");
							ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);*/

                            syncSQLiteMySQLDB();
                        }
                    }
                    break;
                    case 2: {
                        //Upload employee record to online server
                        syncRecord();
                    }
                    break;

                    case 3: {
                        //Download Authorised pine users
                        new getPineUsers().execute();

                    }
                    break;


                    case 4:

                        //Upload Job Clocking
                        if (mydb.getScaleCount() > 0) {

                            try {
                                postRequest(mydb.bt_scale_JSON());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {

                            Toast.makeText(getApplicationContext(), "Nothing to upload", Toast.LENGTH_LONG).show();
                        }
                        break;


                }
            }
        });
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_netup1));
        map.put("info", "You Have " + db.dbSyncCount() + " users info to upload.");
        map.put("img", R.drawable.upload);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_netdn1));
        map.put("info", getString(R.string.txt_netdn2));
        map.put("img", R.drawable.download_b);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_netup3));
        map.put("info", "You Have " + mydb.dbSyncCoun() + " clock to upload.");
        map.put("img", R.drawable.upload);
        list.add(map);

        if (compID == 3 || compID == 8 || compID == 31) {

            map = new HashMap<String, Object>();
            map.put("title", "Download Pine users");
            map.put("info", "Download authorised users for pine report");
            map.put("img", R.drawable.authorise);
            list.add(map);
        }

        if (compID == 3 || compID == 8 || compID == 29) {

            map = new HashMap<String, Object>();
            map.put("title", "Upload Scale clocking");
            map.put("info", "You Have " + mydb.getScaleCount() + " scale clock to upload.");
            map.put("img", R.drawable.upload);
            list.add(map);
        }

        mData = list;
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.utilities, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_settings:
                //Intent intent = new Intent(this, UserList.class);
                //startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDialog(boolean show) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(UtilitiesActivity.this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if (show) {
            dialog.show();
        } else {
            dialog.cancel();
        }

    }

    // Method to Sync MySQL to SQLite DB
    public void syncSQLiteMySQLDB() {


        try {
            // Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();


            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("dat", "dummy");
            jsonObject.accumulate("compID", compID);
            //  convert JSONObject to JSON to String
            json = jsonObject.toString();
            //prgDialog.show();
            setDialog(true);
            params.put("userJSON", json);
            client.post(BASE_URL + "/alos/getUsers.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                    // Update SQLite DB with response sent by getusers.php
                    updateSQLite(response);
                    //prgDialog.hide();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                // When error occured
                @Override
                public void onFailure(int statusCode, Throwable error, String content) {
                    // TODO Auto-generated method stub
                    // Hide ProgressBar
                    //prgDialog.hide();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {

            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void updateSQLite(String response) {
        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            System.out.println(arr.length());
            // If no of array elements is not zero
            if (arr.length() != 0) {

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

                    user.setCostCenterId(Integer.parseInt(obj.get("costCenterId").toString()));
                    user.setShifts_id(Integer.parseInt(obj.get("shifts_id").toString()));
                    user.setShift_type(Integer.parseInt(obj.get("shift_type").toString()));
                    user.setCard(obj.get("card").toString());

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
                //updateMySQLSyncSts(gson.toJson(usersynclist),arr.length());
                // Reload the Main Activity
                gotoUserList();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Move to employeeList
    public void gotoUserList() {

        Intent objIntent = new Intent(getApplicationContext(), UserList.class);
        startActivity(objIntent);
    }

    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), UtilitiesActivity.class);
        startActivity(objIntent);

    }

    public void syncData() {
        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<User> userList = db.getAllUsers();
        if (userList.size() != 0) {
            if (db.dbSyncCount() != 0) {
                prgDialog1.show();
                params.put("usersJSON", db.composeJSONfromSQLite());
                client.post(BASE_URL + "/alos/updateUser.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println("=======" + response);
                        prgDialog1.hide();
                        try {
                            JSONArray arr = new JSONArray(response);
                            System.out.println(arr.length());
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                System.out.println(obj.get("user_id"));
                                System.out.println(obj.get("status"));
                                db.updateSyncStatus(obj.get("user_id").toString(), obj.get("status").toString());
                            }
                            Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub
                        prgDialog1.hide();
                        Toast.makeText(getApplicationContext(), "Network Error please make sure you have data connection", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "All users have been uploaded already", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No data in SQLite DB, please download users to perform Sync action", Toast.LENGTH_LONG).show();
        }
    }

    //Still pointing to production
    public void syncRecord() {

        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList = mydb.getAllrecord();
        if (userList.size() != 0) {
            if (mydb.dbSyncCoun() != 0) {
                prgDialogRecord.show();
                params.put("recordJSON", mydb.composeJSONfromSQLite());
                client.setTimeout(50 * 1000);
                client.post(BASE_URL + "/alos/syncRecord.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println(response);
                        prgDialogRecord.hide();
                        try {
                            JSONArray arr = new JSONArray(response);
                            //System.out.println(arr.length());
                            int count = 0;
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                //System.out.println(obj.get("recId"));
                                //System.out.println(obj.get("status"));
                                String status = obj.get("status").toString();
                                if (status.equals("yes")) {
                                    mydb.updateSyncStatus(obj.get("recId").toString(), obj.get("status").toString());
                                    mydb.deleteRecord(obj.get("recId").toString());
                                    count++;
                                } else {

                                    mydb.updateSyncStatus(obj.get("recId").toString(), obj.get("status").toString());
                                }
                                //mydb.deleteRecord(obj.get("id").toString());
                                //Toast.makeText(getApplicationContext(), obj.get("id").toString() + " "+ obj.get("status").toString(), Toast.LENGTH_LONG).show();
                            }
                            if (count == arr.length()) {
                                Toast.makeText(getApplicationContext(), "Records uploaded", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(getApplicationContext(), "Server Error Occurred", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub
                        prgDialogRecord.hide();
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                        } else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unexpected Error occcured! " + String.valueOf(statusCode), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "All record have already been uploaded", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No data in SQLite DB, please enter record to perform Sync action", Toast.LENGTH_LONG).show();
        }

    }

    public class getPineUsers extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

            prgDialogPine.show();

        }

        protected String doInBackground(String... args) {


            try {

                URL url = new URL(serverURL);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("diggersRest", "diggersRest");
                //Log.e("params",postDataParams.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                //conn.setRequestProperty("Content-Type","application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();
                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();
                    //return new String("false : "+responseCode);
                }


            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }


        @Override
        protected void onPostExecute(String result) {

            if (prgDialogPine != null && prgDialogPine.isShowing()) {
                prgDialogPine.dismiss();
            }

            int arrLength = 0;
            if (result != null) {
                Log.e("RESULT+++++++++:", result);


                try {
                    JSONArray arr = new JSONArray(result);
                    if (arr.length() != 0) {
                        db.deletePineUsers();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            int success = obj.getInt("success");
                            if (success == 1) {

                                int userId = obj.getInt("userId");
                                db.insertPineUsers(userId);
                                arrLength++;
                            } else {
                                String msg = obj.getString("msg");
                                showToast(msg);
                            }


                        }

                        if (arrLength == arr.length()) {
                            showToast("Authorized users successfully downloaded");
                        }

                    } else {
                        showToast("Error, please ensure you have data connection");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("Error, please ensure you have data connection");
                }

            } else {
                showToast("Error, please ensure you have data connection");
            }

            if (responseCode == 404) {
                showToast("Requested resource not found");
            } else if (responseCode == 500) {
                showToast("Something went wrong at server end");
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    //Upload Job Clocking
    public Object postRequest(String param) throws IOException {

        setDialog(true);

        OkHttpClient client = new OkHttpClient();
        //RequestBody body = RequestBody.create(JSON, param);
        RequestBody body = new FormBody.Builder()
                .add("scale_JSON", param)
                .build();
        Request request = new Request.Builder()
                .url(scaleURL)
                .post(body)
                .build();
        //Log.d("PARAM:+++",param[0]+ " "+param[1]);

        client.newCall(request).enqueue(new Callback() {

            Handler handler = new Handler(UtilitiesActivity.this.getMainLooper());

            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error: " + e.getMessage());
                    }
                });


            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (!response.isSuccessful()) {

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showToast("Unexpected error: " + response.message());


                        } else {

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            try {
                                displayResponse(response.body().string());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }


                    }
                });
            }
        });

        return null;
    }

    public void displayResponse(String response) {

        try {


            int count = 0;
            JSONArray arr = new JSONArray(response);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = (JSONObject) arr.get(i);
                int success = obj.getInt("success");
                String scale_id = obj.getString("scale_id");
                if (success == 1) {
                    mydb.delete_scale_rec(scale_id);
                    count++;
                }

            }

            if (count == arr.length()) {
                Toast.makeText(getApplicationContext(), "All Scale data have been uploaded", Toast.LENGTH_LONG).show();
                reloadActivity();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
