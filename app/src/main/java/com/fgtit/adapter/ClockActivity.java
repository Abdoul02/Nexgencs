package com.fgtit.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.fgtit.data.CommonFunction;
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

import static com.fgtit.data.MyConstants.ID_NUMBER;
import static com.fgtit.data.MyConstants.USERNAME;
import static com.fgtit.data.MyConstants.USER_ID;

public class ClockActivity extends Activity {

    //Fingerprint
    private AsyncFingerprint vFingerprint;
    private boolean bIsCancel = false;
    private boolean bfpWork = false;
    private TextView tvFpStatus;
    private boolean bIsUpImage = true;
    private ArrayList<User> empList;

    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private ImageView fpImage;

    DBHandler userDB = new DBHandler(this);
    CommonFunction commonFunction = new CommonFunction(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        initViews();
    }

    private void initViews() {
        empList = userDB.getAllUsers();
        tvFpStatus = findViewById(R.id.textView1);
        tvFpStatus.setText("");
        fpImage = findViewById(R.id.imageView1);
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
                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                goBack(us);
                                break;
                            }
                        }
                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                goBack(us);
                                break;
                            }
                        }

                        count++;
                    }

                    if (count == empList.size()) {
                        commonFunction.showToast("fingerprint not found");

                    }
                } else {
                    commonFunction.showToast("Please download employee information");

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

    private void goBack(User user) {
        workExit();
        Intent data = new Intent();
        data.putExtra(USERNAME, user.getuName());
        data.putExtra(USER_ID, String.valueOf(user.getuId()));
        data.putExtra(ID_NUMBER, user.getIdNum());
        setResult(RESULT_OK, data);
        finish();
    }
}
