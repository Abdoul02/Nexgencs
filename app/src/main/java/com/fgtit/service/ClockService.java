package com.fgtit.service;

import android.app.Activity;
import android.app.Dialog;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.R;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.models.User;
import com.fgtit.utils.ExtApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class ClockService extends IntentService {

    private static final String TAG = "ClockService";
    private int response = Activity.RESULT_CANCELED;
    public static final String SERVICE_RESPONSE = "response";
    public static final String RESULT = "result";
    public static final String FILTER = "filter";
    public static final String USER_ID = "user_id";
    public static final String USER = "user";
    public static final String SUPERVISOR = "supervisor";
    public static final String START_ACTION = "package com.fgtit.service.clock";

    String filter_value;
    int user_id;

    //Fingerprint
    private AsyncFingerprint vFingerprint;
    private boolean bIsCancel = false;
    private boolean bfpWork = false;
    private Dialog fpDialog;
    private TextView tvFpStatus;
    private boolean bcheck = false;
    private boolean bIsUpImage = true;
    private int iFinger = 0;
    private int count;
    private ArrayList<User> empList;

    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private ImageView fpImage;

    Context mContext;
    DBHandler userDB = new DBHandler(this);

    public ClockService() {
        super("ClockService");
    }

    final Handler responseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            showToast("Something went wrong: ");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: Called");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.activity_erdclock,null);
                tvFpStatus = layout.findViewById(R.id.textView1);
                tvFpStatus.setText("");

                fpImage = layout.findViewById(R.id.imageView1);

            }
        });


    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "onHandleIntent: Called");
        // final String filter = intent.getStringExtra(FILTER);
        filter_value = intent.getStringExtra(FILTER);
        user_id = Integer.parseInt(intent.getStringExtra(USER_ID));
        empList = userDB.getAllUsers();


        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();
    }


    private void FPProcess() {
        if (!bfpWork) {
            try {
                Thread.currentThread();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tvFpStatus.setText(getString(R.string.txt_fpplace));
            vFingerprint.FP_GetImage();
            bfpWork = true;
        }
    }

    private void FPInit() {

        vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener() {
            @Override
            public void onGetImageSuccess() {
                if (bIsUpImage) {
                    vFingerprint.FP_UpImage();
                    tvFpStatus.setText(getString(R.string.txt_fpdisplay));
                } else {
                    tvFpStatus.setText(getString(R.string.txt_fpprocess));
                    vFingerprint.FP_GenChar(1);
                }
            }

            @Override
            public void onGetImageFail() {
                if (!bIsCancel) {
                    vFingerprint.FP_GetImage();
                    //SignLocalActivity.this.AddStatus("Error");
                } else {
                    // Toast.makeText(BTScale.this, "Cancel OK", Toast.LENGTH_SHORT).show();
                }
            }
        });

        vFingerprint.setOnUpImageListener(new AsyncFingerprint.OnUpImageListener() {
            @Override
            public void onUpImageSuccess(byte[] data) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                fpImage.setImageBitmap(image);
                //fpImage.setBackgroundDrawable(new BitmapDrawable(image));
                tvFpStatus.setText(getString(R.string.txt_fpprocess));
                vFingerprint.FP_GenChar(1);
            }

            @Override
            public void onUpImageFail() {
                bfpWork = false;
                TimerStart();
            }
        });


        vFingerprint.setOnGenCharListener(new AsyncFingerprint.OnGenCharListener() {
            @Override
            public void onGenCharSuccess(int bufferId) {
                tvFpStatus.setText(getString(R.string.txt_fpidentify));
                vFingerprint.FP_UpChar();
            }

            @Override
            public void onGenCharFail() {
                tvFpStatus.setText(getString(R.string.txt_fpfail));
            }
        });

        vFingerprint.setOnUpCharListener(new AsyncFingerprint.OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {

                List<User> user = empList;
                int count = 0;
                if (empList.size() > 0) {

                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {


                                if (filter_value.equals(SUPERVISOR)) {
                                    if (us.getuId() == user_id) {
                                        response = Activity.RESULT_OK;
                                        publishResults(SUPERVISOR, us.getuId(), response, true);
                                    } else {
                                        publishResults(SUPERVISOR, us.getuId(), response, false);
                                    }

                                } else if (filter_value.equals(USER)) {
                                    response = Activity.RESULT_OK;
                                    publishResults(USER, us.getuId(), response, true);
                                }
                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;
                            }
                        }

                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if (filter_value.equals(SUPERVISOR)) {
                                    if (us.getuId() == user_id) {
                                        response = Activity.RESULT_OK;
                                        publishResults(SUPERVISOR, us.getuId(), response, true);
                                    } else {
                                        publishResults(SUPERVISOR, us.getuId(), response, false);
                                    }

                                } else if (filter_value.equals(USER)) {
                                    response = Activity.RESULT_OK;
                                    publishResults(USER, us.getuId(), response, true);
                                }
                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;
                            }
                        }

                        count++;
                    }

                    if (count == empList.size()) {
                        showToast("fingerprint not found");
                        publishResults(USER, 0, response, false);
                    }
                } else {
                    showToast("Please download employee information");
                    publishResults(USER, 0, response, false);
                }


                bfpWork = false;
                TimerStart();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText(getString(R.string.txt_fpmatchfail) + ":-1");
                bfpWork = false;
                TimerStart();
            }
        });

    }

    public void TimerStart() {
        if (startTimer == null) {
            startTimer = new Timer();
            startHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    TimeStop();
                    FPProcess();
                }
            };
            startTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    startHandler.sendMessage(message);
                }
            };
            startTimer.schedule(startTask, 1000, 1000);
        }
    }

    public void TimeStop() {
        if (startTimer != null) {
            startTimer.cancel();
            startTimer = null;
            startTask.cancel();
            startTask = null;
        }
    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        workExit();
    }

    private void publishResults(String filter, int userId, int response, boolean result) {
        Intent intent = new Intent(START_ACTION);
        intent.putExtra(FILTER, filter);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(RESULT, result);
        intent.putExtra(SERVICE_RESPONSE, response);
        sendBroadcast(intent);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
