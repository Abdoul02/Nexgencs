package com.fgtit.fingermap.erd;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.adapter.CustomSpinnerAdapter;
import com.fgtit.device.BluetoothReaderService;
import com.fgtit.fingermap.BTScale;
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.User;
import com.fgtit.service.ClockService;
import com.fgtit.utils.ExtApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class ERDClock extends AppCompatActivity {

    private static final String TAG = "ERDClock";

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



    String trade_prompt = "--Trade--";
    String status_prompt = "--Status--";

    DBHandler userDB = new DBHandler(this);
    JobDB jobDB = new JobDB(this);

    //Spinners
    Spinner spn_trade,spn_status;
    //TextView
    TextView txt_job_name,txt_job_code,txt_address,txt_supervisor,txt_description;
    int job_id;
    HashMap<String,Integer> tradeMap;
    List<ERDSubTask>subTaskList;
    List<String> trades = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erdclock);
        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String value = extras.getString("job_id");
            job_id = Integer.parseInt(value);

            if (value != null) {

                Cursor cursor = jobDB.getERDJobById(Integer.parseInt(value));
                cursor.moveToFirst();
                final String job_name,address,job_code,description,supervisor;
                int supervisor_id;

                job_name = cursor.getString(cursor.getColumnIndex("name"));
                address  = cursor.getString(cursor.getColumnIndex("address"));
                job_code = cursor.getString(cursor.getColumnIndex("job_no"));
                description =cursor.getString(cursor.getColumnIndex("description"));
                supervisor_id = cursor.getInt(cursor.getColumnIndex("supervisor_id"));
                supervisor = userDB.getUserName(supervisor_id);

                txt_job_name.setText(job_name);
                txt_job_code.setText(job_code);
                txt_description.setText(description);
                txt_address.setText(address);
                txt_supervisor.setText(supervisor);

                subTaskList = jobDB.getSubTasks(job_id);
                for(ERDSubTask subTask : subTaskList){
                    trades.add(subTask.getName());
                    tradeMap.put(subTask.getName(),subTask.getId());
                }

                //Trade
                initAllSpinner(this,trade_prompt,trades,spn_trade);
            }

        }else{
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            workExit();
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void clockUser(View v){

       /* String trade_id = spn_trade.getSelectedItem().toString();
        int key = tradeMap.get(trade_id);
        Toast.makeText(this,"Trade: "+key,Toast.LENGTH_SHORT).show();*/
        Toast.makeText(this,"Please clock",Toast.LENGTH_SHORT).show();
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();
    }
    private void initViews(){

        empList = userDB.getAllUsers();
        spn_status = findViewById(R.id.status_spinner);
        spn_trade = findViewById(R.id.trade_spinner);

        txt_address = findViewById(R.id.txt_address);
        txt_description= findViewById(R.id.txt_description);
        txt_job_code =  findViewById(R.id.txt_code);
        txt_job_name = findViewById(R.id.txt_name);
        txt_supervisor= findViewById(R.id.txt_supervisor);

        tvFpStatus = findViewById(R.id.textView1);
        tvFpStatus.setText("");

        fpImage = findViewById(R.id.imageView1);


        subTaskList = new ArrayList<>();
        tradeMap = new HashMap();

        List<String> statuses = new ArrayList<>();
        statuses.add("IN");
        statuses.add("OUT");

        //status
        initAllSpinner(this,status_prompt,statuses,spn_status);
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();
    }
    private void initAllSpinner(Context context,String prom,List<String> dbValue,Spinner spinner){

        List<String> loadList = new ArrayList<>();
        loadList.add(prom);
        loadList.addAll(dbValue);

        CustomSpinnerAdapter customSpinnerAdapter=new CustomSpinnerAdapter(context,loadList);
        spinner.setAdapter(customSpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //val = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //Fingerprint methods
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

                String trade = spn_trade.getSelectedItem().toString();
                String status = spn_status.getSelectedItem().toString();

                if(trade.equals(trade_prompt) || status.equals(status_prompt)){
                    showToast("Please select a trade and a status");
                }else{
                    List<User> user = empList;
                    int count = 0;
                    if (empList.size() > 0) {

                        for (User us : user) {

                            if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                                byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                                if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                    //Clock
                                    Log.d(TAG, "onUpCharSuccess: "+us.getuName());
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                }
                            }

                            if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                                byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                                if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                    //Clock
                                    Log.d(TAG, "onUpCharSuccess: "+us.getuName());
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                }
                            }

                            count++;
                        }

                        if (count == empList.size()) {
                            showToast("fingerprint not found");

                        }
                    } else {
                        showToast("Please download employee information");

                    }
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
}
