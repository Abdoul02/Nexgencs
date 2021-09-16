package com.fgtit.models;

/**
 * Created by Abdoul on 19-09-2016.
 */
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
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MainActivity;
import com.fgtit.fingermap.R;
import com.fgtit.fingermap.UserList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.data.MyConstants.BASE_URL;
import static com.fgtit.models.App.CHANNEL_ID;

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


    @Override
    public void onDestroy() {

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

}