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

import static com.fgtit.service.DownloadService.CALL_RESPONSE;
import static com.fgtit.service.DownloadService.FILTER;
import static com.fgtit.service.DownloadService.RESULT;
import static com.fgtit.service.DownloadService.URL;

public class UploadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String TAG = "UploadService";
    public static final String _SERVICE = "package com.fgtit.service.uploadService";
    public static final String POST_JSON = "json";
    public static final String JSON_VAL = "json_value";
    public static final String PROJECT = "Project";
    public static final String PROJECT_URL  ="http://www.nexgencs.co.za/alos/upload.php";
    public static final String JOBCARD_URL  ="http://www.nexgencs.co.za/alos/jobCardPictures.php";

    public UploadService() {
        super( "UploadService");
    }

    final Handler responseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            showToast("Something went wrong: ");
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String json = intent.getStringExtra(POST_JSON);
        final String url = intent.getStringExtra(URL);
        final String filter = intent.getStringExtra(FILTER);
        final String jsonValue = intent.getStringExtra(JSON_VAL);

        try {
            post(url,json,jsonValue, new Callback() {
                Handler handler = new Handler(UploadService.this.getMainLooper());

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
                        Log.d(TAG, "Result =>"+result);
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

    Call post(String url, String filter, String jsonVal, Callback callback) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        Call call;

        RequestBody body = new FormBody.Builder()
                .add(filter,jsonVal)
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

    private void publishResults(String filter,int result,String response) {
        Intent intent = new Intent(_SERVICE);
        intent.putExtra(FILTER, filter);
        intent.putExtra(RESULT, result);
        intent.putExtra(CALL_RESPONSE,response);
        sendBroadcast(intent);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
