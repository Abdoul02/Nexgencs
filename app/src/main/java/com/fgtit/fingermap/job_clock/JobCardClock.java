package com.fgtit.fingermap.job_clock;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fgtit.adapter.CustomSpinnerAdapter;
import com.fgtit.data.CommonFunction;
import com.fgtit.data.ImageSimpleAdapter;
import com.fgtit.data.MyConstants;
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.User;
import com.fgtit.service.DownloadService;
import com.fgtit.utils.ExtApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

import static com.fgtit.service.DownloadService.ERD_CLOCK_URL;
import static com.fgtit.service.DownloadService.JOB_CLOCK;

public class JobCardClock extends AppCompatActivity {

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


    Dialog dialog;
    String trade_prompt = "--Trade--";
    String status_prompt = "--Status--";
    String stage_prompt = "--Stage--";

    DBHandler userDB = new DBHandler(this);
    JobDB jobDB = new JobDB(this);
    CommonFunction cf = new CommonFunction(this);

    //Spinners
    Spinner spn_trade, spn_status;
    //TextView
    TextView txt_job_name, txt_job_code, txt_address, txt_supervisor,
            txt_description, txt_start_date, txt_end_date, txt_notify;
    int job_id;
    int companyId;
    HashMap<String, Integer> tradeMap;
    List<ERDSubTask> subTaskList;
    List<String> trades = new ArrayList<>();

    //users who clocked
    private ListView listView1;
    private ArrayList<HashMap<String, Object>> mData1;
    private SimpleAdapter adapter1;
    private Boolean isSubTaskEmpty;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            String filter = bundle.getString(DownloadService.FILTER);
            int resultCode = bundle.getInt(DownloadService.RESULT);
            clearInfo();

            if (resultCode == RESULT_OK && filter.equals(JOB_CLOCK)) {
                String response = bundle.getString(DownloadService.CALL_RESPONSE);
                Log.d(TAG, "onReceive: " + response);
                try {
                    JSONArray arr = new JSONArray(response);
                    if (arr.length() != 0) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            int success = obj.getInt("success");
                            String msg = obj.getString("message");
                            showToast(msg);
                        }
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
        setContentView(R.layout.activity_erdclock);
        companyId = cf.companyId();
        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String value = extras.getString("job_id");
            job_id = Integer.parseInt(value);

            Cursor cursor = jobDB.getERDJobById(Integer.parseInt(value));
            cursor.moveToFirst();
            final String job_name, address, job_code, description, supervisor, startDate, endDate;
            int supervisor_id;

            job_name = cursor.getString(cursor.getColumnIndex("name"));
            address = cursor.getString(cursor.getColumnIndex("address"));
            job_code = cursor.getString(cursor.getColumnIndex("job_no"));
            description = cursor.getString(cursor.getColumnIndex("description"));
            startDate = cursor.getString(cursor.getColumnIndex("from_date"));
            endDate = cursor.getString(cursor.getColumnIndex("to_date"));
            supervisor_id = cursor.getInt(cursor.getColumnIndex("supervisor_id"));
            supervisor = userDB.getUserName(supervisor_id);

            hideViews(job_name, txt_job_name);
            hideViews(address, txt_address);
            hideViews(description, txt_description);

            if (companyId == MyConstants.COMPANY_DRYDEN) {
                txt_notify.setVisibility(View.VISIBLE);
                txt_job_name.setText(getString(R.string.client_name, job_name));
            } else if (companyId == MyConstants.COMPANY_MECHFIT) {
                txt_job_name.setText(getString(R.string.client_name, job_name));
                txt_address.setText(getString(R.string.drawing_no, address));
            } else {
                txt_job_name.setText(getString(R.string.job_name, job_name));
                txt_address.setText(getString(R.string.address, address));
            }
            txt_job_code.setText(getString(R.string.job_no, job_code));
            txt_description.setText(getString(R.string.job_description, description));
            txt_supervisor.setText(getString(R.string.supervisor, supervisor));
            txt_start_date.setText(getString(R.string.start_date, startDate));
            txt_end_date.setText(getString(R.string.end_date, endDate));

            subTaskList = jobDB.getSubTasks(job_id);
            isSubTaskEmpty = subTaskList.isEmpty();
            if (!isSubTaskEmpty) {
                for (ERDSubTask subTask : subTaskList) {
                    trades.add(subTask.getName());
                    tradeMap.put(subTask.getName(), subTask.getId());
                }
            }

            //SubTAsks
            if (companyId == MyConstants.COMPANY_MECHFIT) {
                if (!isSubTaskEmpty) {
                    spn_trade.setVisibility(View.VISIBLE);
                    initAllSpinner(this, stage_prompt, trades, spn_trade);
                } else {
                    cf.showToast("This job has no stage(s)");
                }
            }

        } else {
            finish();
        }
    }

    private void hideViews(String text, View view) {
        if (text == null || text.isEmpty() || text.equalsIgnoreCase("null")) {
            view.setVisibility(View.GONE);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            workExit();
           if(companyId == MyConstants.COMPANY_MECHFIT){
               Intent intent = new Intent(this, JobClockActivity.class);
               startActivity(intent);
           }else{
               Intent intent = new Intent(this, MenuActivity.class);
               startActivity(intent);
           }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {

        empList = userDB.getAllUsers();
        spn_status = findViewById(R.id.status_spinner);
        spn_trade = findViewById(R.id.trade_spinner);

        txt_address = findViewById(R.id.txt_address);
        txt_description = findViewById(R.id.txt_description);
        txt_job_code = findViewById(R.id.txt_code);
        txt_job_name = findViewById(R.id.txt_name);
        txt_supervisor = findViewById(R.id.txt_supervisor);
        txt_start_date = findViewById(R.id.txt_start_date);
        txt_end_date = findViewById(R.id.txt_end_date);
        txt_notify = findViewById(R.id.txtNotify);

        tvFpStatus = findViewById(R.id.textView1);
        tvFpStatus.setText("");

        fpImage = findViewById(R.id.imageView1);


        subTaskList = new ArrayList<>();
        tradeMap = new HashMap();

        List<String> statuses = new ArrayList<>();
        statuses.add("IN");
        statuses.add("OUT");


        listView1 = findViewById(R.id.listView1);
        mData1 = new ArrayList<>();
        adapter1 = new ImageSimpleAdapter(this, mData1, R.layout.listview_signitem,
                new String[]{"title", "info", "dts", "img"},
                new int[]{R.id.title, R.id.info, R.id.dts, R.id.img});
        listView1.setAdapter(adapter1);

        //status
        initAllSpinner(this, status_prompt, statuses, spn_status);

        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();
    }

    private void initAllSpinner(Context context, String prom, List<String> dbValue, Spinner spinner) {

        List<String> loadList = new ArrayList<>();
        loadList.add(prom);
        loadList.addAll(dbValue);

        CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(context, loadList);
        spinner.setAdapter(customSpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void AddPersonItem(User user) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        upload(user.getuId(), currentTime);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("title", user.getuName());
        map.put("info", user.getIdNum());
        map.put("dts", currentTime);
        mData1.add(map);
        adapter1.notifyDataSetChanged();
        ScrollListViewToBottom();
    }

    private void ScrollListViewToBottom() {
        listView1.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView1.setSelection(adapter1.getCount() - 1);
            }
        });
    }

    public void clearInfo() {

        final Handler tOut = new Handler();
        tOut.postDelayed(new Runnable() {
            @Override
            public void run() {
                mData1.clear();
                adapter1.notifyDataSetChanged();
            }
        }, 2000);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setDialog(boolean show) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if (show) {
            dialog.show();
        } else {
            dialog.cancel();
        }

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

                String status = spn_status.getSelectedItem().toString();
                if (companyId == MyConstants.COMPANY_MECHFIT && !isSubTaskEmpty) {
                    String trade = spn_trade.getSelectedItem().toString();
                    if (status.equals(status_prompt) || trade.equals(trade_prompt)) {
                        showToast("Please select both Status a Stage");
                    } else {
                        acceptClock(model);
                    }
                } else {
                    if (status.equals(status_prompt)) {
                        showToast("Please select a status");
                    } else {
                        acceptClock(model);
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

    private void acceptClock(byte[] model) {
        List<User> user = empList;
        int count = 0;
        if (empList.size() > 0) {

            for (User us : user) {

                if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                    byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                    if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                        AddPersonItem(us);
                        tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                        break;
                    }
                }

                if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                    byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                    if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                        AddPersonItem(us);
                        Log.d(TAG, "onUpCharSuccess: " + us.getuName());
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

    public void upload(int user_id, String date) {
        JSONObject postDataParams = new JSONObject();

        try {
            postDataParams.accumulate("clock_date", date);
            postDataParams.accumulate("status", spn_status.getSelectedItem().toString());
            postDataParams.accumulate("user_id", user_id);
            postDataParams.accumulate("job_id", job_id);

            //For MechFit add tradeId
            if (companyId == MyConstants.COMPANY_MECHFIT && !isSubTaskEmpty) {
                String stage = spn_trade.getSelectedItem().toString();
                if (tradeMap.get(stage) != null) {
                    int stageId = tradeMap.get(stage);
                    postDataParams.accumulate("stage_id", stageId);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "upload: " + postDataParams);

        setDialog(true);
        Intent client_intent = new Intent(JobCardClock.this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "erd_clock");
        switch (companyId) {
            case MyConstants.COMPANY_TURN_MILL:
                client_intent.putExtra(DownloadService.URL, MyConstants.TURNMILL_CLOCK_URL);
                break;
            case MyConstants.COMPANY_ERD:
                client_intent.putExtra(DownloadService.URL, ERD_CLOCK_URL);
                break;
            case MyConstants.COMPANY_DRYDEN:
                client_intent.putExtra(DownloadService.URL, MyConstants.DRYDEN_CLOCK_URL);
                break;
            case MyConstants.COMPANY_MECHFIT:
                client_intent.putExtra(DownloadService.URL, MyConstants.MECHFIT_JOB_CLOCK_URL);
                break;
        }
        client_intent.putExtra(DownloadService.FILTER, JOB_CLOCK);
        client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
        startService(client_intent);
    }

    @SuppressLint("HandlerLeak")
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
