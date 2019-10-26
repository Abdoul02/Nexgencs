package com.fgtit.fingermap.dryden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.fgtit.adapter.ClockActivity;
import com.fgtit.adapter.DrawingActivity;
import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.service.DownloadService;
import com.fgtit.service.UploadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.fgtit.data.MyConstants.CHECKLIST;
import static com.fgtit.data.MyConstants.CHECKLIST_REQUEST_CLOCK;
import static com.fgtit.data.MyConstants.CHECKLIST_REQUEST_SIGN;
import static com.fgtit.data.MyConstants.DRYDEN_UPLOAD;
import static com.fgtit.data.MyConstants.ID_NUMBER;
import static com.fgtit.data.MyConstants.IMAGE;
import static com.fgtit.data.MyConstants.IMAGE_NAME;
import static com.fgtit.data.MyConstants.IMAGE_PATH;
import static com.fgtit.data.MyConstants.USERNAME;
import static com.fgtit.data.MyConstants.USER_ID;

public class CheckList extends AppCompatActivity {

    int checkCount = 0;
    LinearLayout linearLayout;
    String[] checklist;
    CheckBox checkBox;
    CommonFunction commonFunction = new CommonFunction(this);
    JobDB jobDB = new JobDB(this);
    String job_id,job_code,local_id,user_id,image_path,image,imageName;

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
        setContentView(R.layout.activity_check_list);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                UploadService._SERVICE));
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
            job_code = cursor.getString(cursor.getColumnIndex("job_no"));
            local_id = cursor.getString(cursor.getColumnIndex("local_id"));
            setTitle(getString(R.string.job_no,job_code));
            linearLayout = findViewById(R.id.mainLinearLayout);
            checkBox = findViewById(R.id.cb_dryden);
            Resources res = getResources();
            checklist = res.getStringArray(R.array.dryden_checkList);
            for (int i = 0; i < checklist.length; i++) {
                CheckBox cb = new CheckBox(this);
                cb.setText(checklist[i]);
                cb.setLayoutParams(checkBox.getLayoutParams());
                cb.setId(i + 1);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            checkCount++;
                        } else {
                            if (checkCount > 0) {
                                checkCount--;
                            }
                        }
                    }
                });
                linearLayout.addView(cb);
            }
        }else finish();

    }

    private boolean checkIfAllIsChecked() {
        if (checkCount == checklist.length) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.project_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.upload) {
            if (checkIfAllIsChecked()) {
                if(image != null){
                    Intent clockIntent = new Intent(this, ClockActivity.class);
                    startActivityForResult(clockIntent,CHECKLIST_REQUEST_CLOCK);
                }else commonFunction.showToast("Please allow RWC to sign");
            } else {
                commonFunction.showToast("Check All checklist please");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHECKLIST_REQUEST_CLOCK && resultCode == RESULT_OK){
            String userName = data.getStringExtra(USERNAME);
            user_id = data.getStringExtra(USER_ID);
            String idNumber = data.getStringExtra(ID_NUMBER);
            if(image != null && imageName != null){
                uploadInfo();
            }else commonFunction.showToast("Please allow RWC to sign");
        }else if(requestCode == CHECKLIST_REQUEST_SIGN && resultCode == RESULT_OK){
            image = data.getStringExtra(IMAGE);
            image_path = data.getStringExtra(IMAGE_PATH);
            imageName = data.getStringExtra(IMAGE_NAME);
            commonFunction.showToast(imageName);
        }else{
            commonFunction.showToast("Operation Cancelled");
        }
    }

    public void uploadInfo(){
        JSONObject postDataParams = new JSONObject();

        try {
            postDataParams.accumulate("date", commonFunction.getDate());
            postDataParams.accumulate("user_id", user_id);
            postDataParams.accumulate("job_id", job_id);
            postDataParams.accumulate("image", image);
            postDataParams.accumulate("image_name", imageName);
            //  postDataParams.accumulate("task_id", task_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        commonFunction.setDialog(true);
        Intent client_intent = new Intent(this, UploadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "checkList");
        client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
        client_intent.putExtra(DownloadService.FILTER, CHECKLIST);
        client_intent.putExtra(DownloadService.URL, DRYDEN_UPLOAD);
        startService(client_intent);
    }

    public void sign(View view){
        if (checkIfAllIsChecked()) {
            Intent signatureIntent = new Intent(this, DrawingActivity.class);
            startActivityForResult(signatureIntent,CHECKLIST_REQUEST_SIGN);
        }else  commonFunction.showToast("Check All checklist please");
    }
    private void handleResponse(Bundle bundle){
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && filter.equals(CHECKLIST)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            try {
                JSONArray arr = new JSONArray(response);
                if (arr.length() != 0) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = (JSONObject) arr.get(i);
                        int success = obj.getInt("success");
                        String msg = obj.getString("message");
                        if (success == 1) {
                            if(commonFunction.deleteFile(image_path)){
                                commonFunction.showToast(msg);
                                jobDB.updateCheckStatus(local_id);
                                gotoQFReport();
                            }else{
                                commonFunction.showToast("Image could not be deleted locally");
                            }
                        } else {
                            commonFunction.showToast(msg);
                        }
                    }
                }
                commonFunction.cancelDialog();
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
            }
        }else {
            commonFunction.cancelDialog();
            commonFunction.showToast("Something went wrong");
        }
    }

    private void gotoQFReport(){
        Intent intent = new Intent(this, QF10_Report.class);
        startActivity(intent);
    }
}
