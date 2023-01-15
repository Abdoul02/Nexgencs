package com.fgtit.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.data.MyConstants.BASE_URL;
import static com.fgtit.data.MyConstants.DOWNLOAD_EMP;

import com.fgtit.data.MyConstants;
import com.fgtit.fingermap.SaveCustomDataInDb;
import com.fgtit.models.SaveDataResponse;

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String TAG = "DownloadService";
    public static final String NOTIFICATION = "package com.fgtit.service";
    public static final String POST_JSON = "json";
    public static final String JSON_VAL = "json_value";

    public static final String EC_DATA_URL = BASE_URL + "/alos/get_ec_data.php";
    public static final String ERD_DATA_URL = BASE_URL + "/alos/get_erd_job.php";
    public static final String ERD_CLOCK_URL = BASE_URL + "/alos/erd_job_clock.php";

    public static final String CUSTOMER = "customer";
    public static final String PRODUCTS = "product";
    public static final String ERD = "erd_job";
    public static final String ERROR = "error";
    public static final String JOB_CLOCK = "job_clock";

    public static final String URL = "url";
    public static final String RESULT = "result";
    public static final String FILTER = "filter";
    public static final String CALL_RESPONSE = "response";

    public DownloadService() {
        super("DownloadService");
    }


    final Handler responseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            publishResults(ERROR, Activity.RESULT_CANCELED, msg.obj.toString());
            //showToast("Something went wrong: ");
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        final String json = intent.getStringExtra(POST_JSON);
        final String url = intent.getStringExtra(URL);
        final String filter = intent.getStringExtra(FILTER);

        Log.d(TAG, json);
        Log.d(TAG, filter);

        String jsonValue = "";
        if (filter.equals(JOB_CLOCK) || filter.equals(DOWNLOAD_EMP)) {
            jsonValue = intent.getStringExtra(JSON_VAL);
        }

        try {
            post(url, json, jsonValue, new Callback() {
                Handler handler = new Handler(DownloadService.this.getMainLooper());

                @Override
                public void onFailure(final Call call, final IOException e) {
                    call.cancel();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            publishResults(filter, Activity.RESULT_CANCELED, "Please check internet connection");
                        }
                    });

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = Objects.requireNonNull(response.body()).string();
                        Objects.requireNonNull(response.body()).close();
                        result = Activity.RESULT_OK;
                        Log.d(TAG, "Result =>" + result);
                        Log.d(TAG, "Response =>" + responseStr);
                        publishResults(filter, result, responseStr);

                    } else {
                        // Request not successful
                        Message msg = new Message();
                        msg.obj = response.message();
                        responseHandler.sendMessage(msg);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    Call post(String url, String filter, String jsonVal, Callback callback) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Call call;

        RequestBody body = new FormBody.Builder()
                .add(filter, jsonVal)
                .build();
        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        call = client.newCall(request);
        call.enqueue(callback);


        return call;
    }

    private void publishResults(String filter, int result, String response) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILTER, filter);
        intent.putExtra(RESULT, result);
        if (result == Activity.RESULT_OK && (filter.equals(ERD) || filter.equals(MyConstants.TURNMILL_GET_JOB)
                || filter.equals(MyConstants.DRYDEN_GET_JOB) || filter.equals(MyConstants.MECHFIT_GET_JOB))) {
            SaveDataResponse saveDataResponse = saveJobs(response, filter);
            intent.putExtra(CALL_RESPONSE, saveDataResponse.getMessage());
        } else if (result == Activity.RESULT_OK && filter.equals(DOWNLOAD_EMP)) {
            SaveDataResponse saveDataResponse = saveEmployInfo(response);
            intent.putExtra(CALL_RESPONSE, saveDataResponse.getMessage());
        } else {
            intent.putExtra(CALL_RESPONSE, response);
        }
        sendBroadcast(intent);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private SaveDataResponse saveJobs(String jsonResponse, String filter) {
        SaveCustomDataInDb saveJob = new SaveCustomDataInDb(this.getApplicationContext());
        return saveJob.saveJobs(jsonResponse, filter);
    }

    private SaveDataResponse saveEmployInfo(String employeeData) {
        SaveCustomDataInDb saveData = new SaveCustomDataInDb(this.getApplicationContext());
        return saveData.saveEmployeeInfo(employeeData);
    }
}
