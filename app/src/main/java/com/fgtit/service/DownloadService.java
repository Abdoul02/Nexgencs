package com.fgtit.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String TAG = "DownloadService";
    public static final String NOTIFICATION = "package com.fgtit.service";
    public static final String POST_JSON = "json";
    public static final String EC_DATA_URL = "url";//"http://nexgencs.co.za/alos/get_ec_data.php";
    public static final String CUSTOMER = "customer";
    public static final String PRODUCTS = "product";
    public static final String RESULT = "result";
    public static final String FILTER = "filter";
    public static final String CALL_RESPONSE = "response";

    public DownloadService() {
        super("DownloadService");
    }


    final Handler responseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            showToast("Something went wrong: " +  msg.obj);
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        final String json = intent.getStringExtra(POST_JSON);
        final String url = intent.getStringExtra(EC_DATA_URL);
        final String filter = intent.getStringExtra(FILTER);

        try {
            post(url,json, new Callback() {
                Handler handler = new Handler(DownloadService.this.getMainLooper());

                @Override
                public void onFailure(final Call call, final IOException e) {
                    call.cancel();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Please check internet connection");
                        }
                    });

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        response.body().close();
                        result = Activity.RESULT_OK;
                        publishResults(filter,result,responseStr);

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


    Call post(String url,String filter, Callback callback) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Call call;

        RequestBody body = new FormBody.Builder()
                .add(filter,"")
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

    private void publishResults(String filter, int result,String response) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILTER, filter);
        intent.putExtra(RESULT, result);
        intent.putExtra(CALL_RESPONSE,response);
        sendBroadcast(intent);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}