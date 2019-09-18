package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.fgtit.models.EcCustomer;
import com.fgtit.service.DownloadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.service.DownloadService.CUSTOMER;

public class CreateEffectiveJob extends AppCompatActivity {

    private static final String TAG = "CreateEffectiveJob";
    EditText edt_order, edt_technician;
    AutoCompleteTextView edt_name;
    String name, order, technician, user_id;
    Dialog dialog;
    String[] customersArray;
    private ArrayList<EcCustomer> customerList;
    public static final String JOBURL = "http://www.nexgencs.co.za/alos/create_update_job.php";
    JobDB jobDB = new JobDB(this);
    HashMap<String, String> queryValues;

    DBHandler userDb = new DBHandler(this);

    //Receiving Downloaded info
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            String filter = bundle.getString(DownloadService.FILTER);
            int resultCode = bundle.getInt(DownloadService.RESULT);

            if (resultCode == RESULT_OK && filter.equals(CUSTOMER)) {
                String response = bundle.getString(DownloadService.CALL_RESPONSE);
                Log.d(TAG, "onReceive: " + response);

                try {
                    JSONArray arr = new JSONArray(response);
                    if (arr.length() != 0) {
                        jobDB.deleteAllCustomer();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            EcCustomer customer = new EcCustomer();
                            customer.setId(obj.getInt("id"));
                            customer.setName(obj.getString("name"));
                            jobDB.insertCustomers(customer);
                        }
                        refresh("Customers saved successfully");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                showToast("Something went wrong");
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_effective_job);
        initViews();
        setTitle("Create Job Card");

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

    private void initViews() {
        edt_name = findViewById(R.id.edt_name);
        edt_order = findViewById(R.id.edt_order);
        edt_technician = findViewById(R.id.edt_technician);

        List<String> allCustomers = new ArrayList<>();
        customerList = jobDB.getAllCustomers();
        if (customerList.isEmpty()) {
            showToast("Please Download customers by clicking the cloud icon");
        } else {

            List<EcCustomer> customers = customerList;
            for (EcCustomer customer : customers) {
                allCustomers.add(customer.getName());
            }
            customersArray = allCustomers.toArray(new String[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, customersArray);
            edt_name.setThreshold(1);
            edt_name.setAdapter(adapter);
        }
    }

    public void createJobCard(View v) {

        name = edt_name.getText().toString();
        order = edt_order.getText().toString();
        technician = edt_technician.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = sdf.format(new Date());
        int company_id = -1;
        if (validateInput(name) && validateInput(order) && validateInput(technician)) {

            company_id = jobDB.getCustomerId(name);
            if (company_id != -1) {

                JSONObject postDataParams = new JSONObject();
                try {

                    postDataParams.accumulate("company_id", company_id);
                    postDataParams.accumulate("order_no", order);
                    postDataParams.accumulate("date", currentDateTime);
                    postDataParams.accumulate("user_id", technician);
                    postRequest(postDataParams.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {

                showToast("This Company is not in the device");
            }


        } else {
            showToast("Please provide all fields");
        }


    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void downloadClient(View view) {
        setDialog(true);
        Intent client_intent = new Intent(this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "ec_clients");
        client_intent.putExtra(DownloadService.FILTER, CUSTOMER);
        startService(client_intent);
    }

    public Object postRequest(String param) throws IOException {

        //System.out.println("PARAM==="+param);
        setDialog(true);


        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ec_job_json", param)
                .build();
        Request request = new Request.Builder()
                .url(JOBURL)
                .post(body)
                .build();
        //Log.d("PARAM:+++",param[0]+ " "+param[1]);
        Log.d(TAG, "ec_job_json: " + param);


        client.newCall(request).enqueue(new Callback() {

            Handler handler = new Handler(CreateEffectiveJob.this.getMainLooper());

            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();

                if (dialog != null && dialog.isShowing()) {
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

                        if (!response.isSuccessful()) {

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showToast("Unexpected error: " + response.message());

                        } else {

                            try {

                                apiFeedback(response.body().string());
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }

                            } catch (IOException e) {
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

    private void setDialog(boolean show) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(CreateEffectiveJob.this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if (show) {
            dialog.show();
        } else {
            dialog.cancel();
        }

    }

    public void apiFeedback(String response) {

        try {
            JSONObject object = new JSONObject(response);
            int success = object.getInt("success");
            String message = object.getString("message");
            if (success == 1) {
                String company_id = object.getString("company");
                String jobId = object.getString("job_id");

                queryValues = new HashMap<>();

                queryValues.put("job_id", jobId);
                queryValues.put("company", jobDB.getCustomerName(company_id));
                jobDB.insert_ec_Job(queryValues);
                gotoProject(message);
            } else {
                showToast(message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void gotoProject(String message) {
        showToast(message);
        Intent intent = new Intent(CreateEffectiveJob.this, Project.class);
        startActivity(intent);
    }

    public void refresh(String message) {
        showToast(message);
        Intent intent = new Intent(this, CreateEffectiveJob.class);
        startActivity(intent);
    }

    private boolean validateInput(String input) {
        boolean status = false;

        if (input.length() > 0) {
            status = true;
        }
        return status;
    }
}
