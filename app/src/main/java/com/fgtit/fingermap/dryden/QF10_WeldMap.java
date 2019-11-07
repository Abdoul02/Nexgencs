package com.fgtit.fingermap.dryden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.R;
import com.fgtit.service.DownloadService;
import com.fgtit.service.NetworkService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.fgtit.data.MyConstants.DRYDEN_UPLOAD;
import static com.fgtit.data.MyConstants.WELD_MAP;

public class QF10_WeldMap extends AppCompatActivity {

    @BindView(R.id.drydenJobNo)
    TextView txt_job_no;
    /*    @BindView(R.id.drawingNo)
        EditText edt_drawing_no;*/
    @BindView(R.id.workDescription)
    EditText edt_work_description;
    @BindView(R.id.consAWS)
    EditText edt_cons_aws;
    @BindView(R.id.stampNo)
    EditText edt_stamp_no;
    String job_id, localId, jobCode, drawingNo, workDescription, consAws, welderStampNo;
    JobDB jobDB = new JobDB(this);
    CommonFunction commonFunction = new CommonFunction(this);

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            handleResponse(bundle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qf_weld_map);
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                NetworkService._SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void initViews() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            job_id = extras.getString("job_id");
            Cursor cursor = jobDB.getDrydenJobById(Integer.parseInt(job_id));
            cursor.moveToFirst();
            jobCode = cursor.getString(cursor.getColumnIndex("job_no"));
            localId = cursor.getString(cursor.getColumnIndex("local_id"));
            workDescription = cursor.getString(cursor.getColumnIndex("description"));
            txt_job_no.setText(getString(R.string.job_no, jobCode));
            edt_work_description.setText(workDescription);
            edt_work_description.setClickable(false);

        } else finish();
    }

    public void uploadInfo(View view) {
        //drawingNo = edt_drawing_no.getText().toString();
        consAws = edt_cons_aws.getText().toString();
        welderStampNo = edt_stamp_no.getText().toString();
        if (!commonFunction.checkTextLength(consAws) || !commonFunction.checkTextLength(welderStampNo)) {
            commonFunction.showToast("Please provide both input");
        } else {

            JSONObject postDataParams = new JSONObject();

            try {
                postDataParams.accumulate("date", commonFunction.getDateAndTime());
                postDataParams.accumulate("job_id", job_id);
                postDataParams.accumulate("consAws", consAws);
                postDataParams.accumulate("welderStampNo", welderStampNo);
                //  postDataParams.accumulate("task_id", task_id);

                commonFunction.setDialog(true);
                Intent client_intent = new Intent(this, NetworkService.class);
                client_intent.putExtra(DownloadService.POST_JSON, "weldMap");
                client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
                client_intent.putExtra(DownloadService.FILTER, WELD_MAP);
                client_intent.putExtra(DownloadService.URL, DRYDEN_UPLOAD);
                startService(client_intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && filter.equals(WELD_MAP)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            try {
                JSONArray arr = new JSONArray(response);
                if (arr.length() != 0) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = (JSONObject) arr.get(i);
                        int success = obj.getInt("success");
                        String msg = obj.getString("message");
                        commonFunction.showToast(msg);
                        if (success == 1) {
                            gotoQFReport();
                        }
                    }
                }
                commonFunction.cancelDialog();
                commonFunction.showToast("An Error has occurred, Please check internet connection");
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
                commonFunction.showToast("An Error has occurred, Please check internet connection");
            }
        }
    }
    private void gotoQFReport() {
        Intent intent = new Intent(this, DrydenJobList.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent(this, DrydenJobList.class);
            startActivity(intent);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
