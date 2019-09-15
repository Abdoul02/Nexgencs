package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.service.DownloadService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import static com.fgtit.fingermap.CreateEffectiveJob.JOBURL;
import static com.fgtit.service.DownloadService.PRODUCTS;

public class EffectiveCooling extends AppCompatActivity {

    private LinearLayout parentLinearLayout;
    TextView txt_time_in, txt_time_out;
    EditText edt_work,edt_km,edt_travel_time;
    public static final String TAG = "EffectiveCooling";
    Dialog dialog;
    String job_id,ec_id;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String URL = "http://www.nexgencs.co.za/alos/capture_ec_job_info.php";
    String currentDateTime,status;
    JobDB jobDB = new JobDB(this);
    HashMap<String, String> queryValues;



    //Receiving Downloaded info
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

                String filter = bundle.getString(DownloadService.FILTER);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                //String latest_rate = bundle.getDouble(DownloadService.LATEST_RATE);
                setDialog(false);
                if (resultCode == RESULT_OK && filter == PRODUCTS) {
                   String response = bundle.getString(DownloadService.CALL_RESPONSE);
                    Log.d(TAG, "onReceive: "+response);

                    try {
                        JSONArray arr = new JSONArray(response);
                        if(arr.length() != 0){

                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }


                } else {
                    showToast("");
                }


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effective_cooling);
        setTitle("Job Card Info");
        parentLinearLayout = findViewById(R.id.linearLayoutMaterial);
        txt_time_in = findViewById(R.id.txt_time_in);
        txt_time_out = findViewById(R.id.txt_time_out);
        edt_km = findViewById(R.id.edt_km);
        edt_travel_time = findViewById(R.id.edt_travel_time);
        edt_work = findViewById(R.id.edt_work);

        queryValues = new HashMap<String, String>();

        txt_time_in.setVisibility(View.INVISIBLE);
        txt_time_out.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            job_id = extras.getString("id");
            ec_id = extras.getString("db_job_id");

        }else{

            finish();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                DownloadService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.effective_cooling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.sign) {

            Bundle dataBundle = new Bundle();
            dataBundle.putString("code", job_id);
            dataBundle.putString("url", "http://www.nexgencs.co.za/alos/ec_job_card/sign.php");
            Intent intent = new Intent(getApplicationContext(), Signature.class);
            intent.putExtras(dataBundle);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            exitApplication();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApplication() {
        Intent intent = new Intent(getApplicationContext(), Project.class);
        startActivity(intent);
    }


    public void addField(View v) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.field, null);

        LinearLayout kid = (LinearLayout) v.getParent();

        TextInputLayout txt_quantity = (TextInputLayout) kid.getChildAt(0);
        EditText quantity = txt_quantity.getEditText();

        TextInputLayout txt_material = (TextInputLayout) kid.getChildAt(1);
        EditText material = txt_material.getEditText();

        TextInputLayout txt_price = (TextInputLayout) kid.getChildAt(2);
        EditText price = txt_price.getEditText();

        if(quantity.getText().length() > 0 && material.getText().length() > 0 && price.getText().length() > 0){
            // Add the new row.
            parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount());
        }else{
            showToast("Please fill all details be adding a new material");
        }



    }

    public void removeField(View v) {

        parentLinearLayout.removeView((View) v.getParent());
    }

    public void timeIn(View v){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        currentDateTime = sdf.format(new Date());
        status = "IN";

        if(parentLinearLayout.getChildCount() > 1){
            showToast("Please upload first");
        }

        if(jobDB.checkSignIn(status,date,ec_id) > 0){
            showToast("You already Have a Time IN for today");
        }else{

            if(edt_km.getText().length() > 0 && edt_travel_time.getText().length() > 0){
                txt_time_in.setText(currentDateTime);
                txt_time_in.setVisibility(View.VISIBLE);
                uploadInfo(status);

            }else{
                showToast("Please provide KM and Travel Time.");
            }
        }




    }
    public void timeOut(View v){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        currentDateTime = sdf.format(new Date());
        status = "OUT";


        if(parentLinearLayout.getChildCount() > 1){
            showToast("Please upload first");
        }

        if(jobDB.checkSignIn(status,date,ec_id) > 0){
            showToast("You already Have a Time OUT for today");
        }else{
                txt_time_out.setText(currentDateTime);
                txt_time_out.setVisibility(View.VISIBLE);
                uploadInfo(status);
        }

    }

    public void upload(View v){

        String currentDateTime = sdf.format(new Date());
        uploadInfo("");

       // Log.d(TAG, "Material: "+materialJSON());

    }

    public void uploadInfo(String status){

        String work_undertaken,km,travel_time;
        work_undertaken = edt_work.getText().toString();
        km = edt_km.getText().toString();
        travel_time = edt_travel_time.getText().toString();

        String currentDateTime = sdf.format(new Date());

        JSONObject postDataParams = new JSONObject();
        try{

            postDataParams.accumulate("job_id", job_id);
            postDataParams.accumulate("work_undertaken", work_undertaken);
            postDataParams.accumulate("date_in", currentDateTime);
            postDataParams.accumulate("status", status);
            postDataParams.accumulate("km", km);
            postDataParams.accumulate("travelling_time", travel_time);
            postDataParams.accumulate("materials", materialJSON());
            postRequest(postDataParams.toString());

            Log.d(TAG, "JSONObject: "+ postDataParams.toString());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String materialJSON(){

        ArrayList<HashMap<String, String>> materialList;
        materialList = new ArrayList<>();

        for (int i = 0; i < parentLinearLayout.getChildCount() ; i++) {

            HashMap<String, String> map = new HashMap<String, String>();

            LinearLayout kid = (LinearLayout) parentLinearLayout.getChildAt(i);

            TextInputLayout txt_quantity = (TextInputLayout) kid.getChildAt(0);
            EditText quantity = txt_quantity.getEditText();

            TextInputLayout txt_material = (TextInputLayout) kid.getChildAt(1);
            EditText material = txt_material.getEditText();

            TextInputLayout txt_price = (TextInputLayout) kid.getChildAt(2);
            EditText price = txt_price.getEditText();

            map.put("quantity",quantity.getText().toString());
            map.put("material_used",material.getText().toString());
            map.put("unit_price",price.getText().toString());
            materialList.add(map);
        }

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(materialList);
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setDialog(boolean show){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(EffectiveCooling.this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if(show) {
            dialog.show();
        }else{
            dialog.cancel();
        }

    }

    public void downloadProduct(View v){

        setDialog(true);
        Intent product_intent = new Intent(this, DownloadService.class);
        product_intent.putExtra(DownloadService.POST_JSON, "ec_products");
        product_intent.putExtra(DownloadService.FILTER, PRODUCTS);
        startService(product_intent);

    }

    public Object postRequest(String param) throws IOException {

        //System.out.println("PARAM==="+param);
        setDialog(true);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ec_job_json",param)
                .build();
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();
        //Log.d("PARAM:+++",param[0]+ " "+param[1]);
        Log.d(TAG, "ec_job_json: "+param);


        client.newCall(request).enqueue(new Callback() {

            Handler handler = new Handler(EffectiveCooling.this.getMainLooper());
            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();

                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error, Please check network");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(!response.isSuccessful()){

                            if(dialog != null && dialog.isShowing()){
                                dialog.dismiss();
                            }
                            showToast("Unexpected error: "+response.message());

                        }else{

                            try {

                                apiFeedback(response.body().string());
                                if(dialog != null && dialog.isShowing()){
                                    dialog.dismiss();
                                }

                            }catch (IOException e){
                                e.printStackTrace();
                                showToast("Something went wrong contact admin");

                            }

                        }


                    }
                });
            }
        });

        return null;
    }

    public void apiFeedback(String response){

        try {
            JSONObject object = new JSONObject(response);
            int success = object.getInt("success");
            String message = object.getString("message");
            if(success == 1){

                queryValues.put("job_id", job_id);
                queryValues.put("id", ec_id);
                queryValues.put("work_undertaken", "");
                queryValues.put("clock_time", currentDateTime);
                queryValues.put("status", status);
                queryValues.put("km", edt_km.getText().toString());
                queryValues.put("travelling_time", edt_travel_time.getText().toString());
                jobDB.insert_job_info(queryValues);


                reload(message);
            }else{
                showToast(message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reload(String message){
        showToast(message);
        edt_travel_time.setText("");
        edt_km.setText("");
        edt_work.setText("");
        txt_time_out.setText("");
        txt_time_in.setText("");

        LinearLayout kid = (LinearLayout) parentLinearLayout.getChildAt(0);

        TextInputLayout txt_quantity = (TextInputLayout) kid.getChildAt(0);
        txt_quantity.getEditText().setText("");

        TextInputLayout txt_material = (TextInputLayout) kid.getChildAt(1);
        txt_material.getEditText().setText("");

        TextInputLayout txt_price = (TextInputLayout) kid.getChildAt(2);
        txt_price.getEditText().setText("");


        if(parentLinearLayout.getChildCount() > 1){

            int x = parentLinearLayout.getChildCount() -1;
            do{
                parentLinearLayout.removeView(parentLinearLayout.getChildAt(x));
                x--;
            }while(x > 0);

        }

        Intent intent = new Intent(this, EffectiveCooling.class);
        startActivity(intent);
    }


}
