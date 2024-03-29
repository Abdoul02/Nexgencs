package com.fgtit.fingermap.dryden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fgtit.adapter.ClockActivity;
import com.fgtit.adapter.DrawingActivity;
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

import static com.fgtit.data.MyConstants.CONSUMABLE;
import static com.fgtit.data.MyConstants.CONSUMABLE_REQUEST_CLOCK;
import static com.fgtit.data.MyConstants.CONSUMABLE_REQUEST_SIGN;
import static com.fgtit.data.MyConstants.DRYDEN_UPLOAD;
import static com.fgtit.data.MyConstants.IMAGE;
import static com.fgtit.data.MyConstants.IMAGE_NAME;
import static com.fgtit.data.MyConstants.IMAGE_PATH;
import static com.fgtit.data.MyConstants.JOB_DETAIL;
import static com.fgtit.data.MyConstants.USER_ID;
import static com.fgtit.data.MyConstants.VERIFICATION_REQUEST_CLOCK;

public class QF10_Report extends AppCompatActivity {

    //General Info
    @BindView(R.id.job_no)
    TextView txt_job_no;
    @BindView(R.id.description)
    TextView txt_description;
    @BindView(R.id.qcNo)
    TextView txt_qc_no;
    @BindView(R.id.job_date)
    TextView txt_job_date;

    //Verification
    @BindView(R.id.dateOfWeld)
    EditText edt_date_of_weld;
    @BindView(R.id.weldMachineNo)
    EditText edt_weld_machine;
    @BindView(R.id.actualVoltage)
    EditText edt_actual_voltage;
    @BindView(R.id.actualAmps)
    EditText edt_actual_amps;
    @BindView(R.id.actualPreheat)
    EditText edt_actual_preheat;
    @BindView(R.id.actualInterPass)
    EditText edt_actual_interpass;

    //Consumable
    @BindView(R.id.issueDate)
    EditText edt_issue_date;
    @BindView(R.id.welderNo)
    EditText edt_weld_no;
    @BindView(R.id.consSize)
    EditText edt_consSize;
    @BindView(R.id.consBatch)
    EditText edt_cons_batch;
    @BindView(R.id.receivedKg)
    EditText edt_received_kg;
    @BindView(R.id.returnedKg)
    EditText edt_returned_kg;
    @BindView(R.id.type)
    EditText edt_type;

    JobDB jobDB = new JobDB(this);
    CommonFunction commonFunction = new CommonFunction(this);
    String job_id, localId, jobCode, qcpNo, dateOfIssue, description;
    String dateOfWeld, machineNo, actualVolt, actualAmps, actualPreheat, actualInterpass;
    String issueDate, welderNo, consSize, consBatch, receivedKg, returnedKg, type, imagePath, imageName, image;

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
        setContentView(R.layout.activity_qf__report);
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
            qcpNo = cursor.getString(cursor.getColumnIndex("qc_no"));
            dateOfIssue = cursor.getString(cursor.getColumnIndex("issue_date"));
            description = cursor.getString(cursor.getColumnIndex("description"));

            txt_job_no.setText(getString(R.string.job_no, jobCode));
            txt_description.setText(getString(R.string.job_description, description));
            txt_qc_no.setText(getString(R.string.qcp_no, qcpNo));
            txt_job_date.setText(getString(R.string.date_of_jb, dateOfIssue));
            edt_date_of_weld.setText(commonFunction.getDate());
            edt_date_of_weld.setClickable(false);
            edt_issue_date.setText(commonFunction.getDate());
            edt_issue_date.setClickable(false);
        } else finish();
    }

    public void captureVerification(View view) {
        dateOfWeld = edt_date_of_weld.getText().toString();
        machineNo = edt_weld_machine.getText().toString();
        actualVolt = edt_actual_voltage.getText().toString();
        actualAmps = edt_actual_amps.getText().toString();
        actualPreheat = edt_actual_preheat.getText().toString();
        actualInterpass = edt_actual_interpass.getText().toString();

        if (commonFunction.checkTextLength(dateOfWeld) && commonFunction.checkTextLength(machineNo) &&
                commonFunction.checkTextLength(actualVolt) && commonFunction.checkTextLength(actualAmps) &&
                commonFunction.checkTextLength(actualPreheat) && commonFunction.checkTextLength(actualInterpass)) {
            Intent clockIntent = new Intent(this, ClockActivity.class);
            startActivityForResult(clockIntent, VERIFICATION_REQUEST_CLOCK);
        } else commonFunction.showToast("Please provide all Verification and Validation data");
    }

    private void uploadVerification(String user_id) {
        JSONObject postDataParams = new JSONObject();
        try {
            postDataParams.accumulate("dateOfWeld", dateOfWeld);
            postDataParams.accumulate("job_id", job_id);
            postDataParams.accumulate("machineNo", machineNo);
            postDataParams.accumulate("actualVolt", actualVolt);
            postDataParams.accumulate("actualAmps", actualAmps);
            postDataParams.accumulate("actualPreheat", actualPreheat);
            postDataParams.accumulate("actualInterpass", actualInterpass);
            postDataParams.accumulate("user_id", user_id);

            commonFunction.setDialog(true);
            Intent client_intent = new Intent(this, NetworkService.class);
            client_intent.putExtra(DownloadService.POST_JSON, "job_detail");
            client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
            client_intent.putExtra(DownloadService.FILTER, JOB_DETAIL);
            client_intent.putExtra(DownloadService.URL, DRYDEN_UPLOAD);
            startService(client_intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void uploadConsumable(String user_id) {
        JSONObject postDataParams = new JSONObject();
        try {
            postDataParams.accumulate("issueDate", issueDate);
            postDataParams.accumulate("job_id", job_id);
            postDataParams.accumulate("welderNo", welderNo);
            postDataParams.accumulate("consSize", consSize);
            postDataParams.accumulate("consBatch", consBatch);
            postDataParams.accumulate("receivedKg", receivedKg);
            postDataParams.accumulate("returnedKg", returnedKg);
            postDataParams.accumulate("image", image);
            postDataParams.accumulate("imageName", imageName);
            postDataParams.accumulate("imagePath", imagePath);
            postDataParams.accumulate("type", type);
            postDataParams.accumulate("user_id", user_id);

            commonFunction.setDialog(true);
            Intent client_intent = new Intent(this, NetworkService.class);
            client_intent.putExtra(DownloadService.POST_JSON, "consumable");
            client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
            client_intent.putExtra(DownloadService.FILTER, CONSUMABLE);
            client_intent.putExtra(DownloadService.URL, DRYDEN_UPLOAD);
            startService(client_intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void captureConsumable(View view) {
        issueDate = edt_issue_date.getText().toString();
        welderNo = edt_weld_no.getText().toString();
        consSize = edt_consSize.getText().toString();
        consBatch = edt_cons_batch.getText().toString();
        receivedKg = edt_received_kg.getText().toString();
        returnedKg = edt_returned_kg.getText().toString();
        type = edt_type.getText().toString();

        if (consumableProvided()) {
            if (image != null && imageName != null) {
                Intent clockIntent = new Intent(this, ClockActivity.class);
                startActivityForResult(clockIntent, CONSUMABLE_REQUEST_CLOCK);
            } else commonFunction.showToast("Please allow store controller to sign first");

        } else commonFunction.showToast("Please provide all Consumable control data");

    }

    public void storeControllerSign(View view) {
        Intent signatureIntent = new Intent(this, DrawingActivity.class);
        startActivityForResult(signatureIntent, CONSUMABLE_REQUEST_SIGN);
    }

    public boolean consumableProvided() {
        if (commonFunction.checkTextLength(issueDate) && commonFunction.checkTextLength(welderNo) &&
                commonFunction.checkTextLength(consSize) && commonFunction.checkTextLength(consBatch) &&
                commonFunction.checkTextLength(receivedKg) && commonFunction.checkTextLength(returnedKg) &&
                commonFunction.checkTextLength(type)) {
            return true;
        }

        return false;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERIFICATION_REQUEST_CLOCK && resultCode == RESULT_OK) {
            if (data != null) {
                String user_id = data.getStringExtra(USER_ID);
                uploadVerification(user_id);
            }
        } else if (requestCode == CONSUMABLE_REQUEST_SIGN && resultCode == RESULT_OK) {
            if (data != null) {
                image = data.getStringExtra(IMAGE);
                imagePath = data.getStringExtra(IMAGE_PATH);
                imageName = data.getStringExtra(IMAGE_NAME);
            }
        } else if (requestCode == CONSUMABLE_REQUEST_CLOCK && resultCode == RESULT_OK) {
            if (data != null) {
                String user_id = data.getStringExtra(USER_ID);
                uploadConsumable(user_id);
            }
        }
    }

    private void handleResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && filter.equals(JOB_DETAIL)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            try {
                JSONArray arr = new JSONArray(response);
                commonFunction.cancelDialog();
                if (arr.length() != 0) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = (JSONObject) arr.get(i);
                        int success = obj.getInt("success");
                        String msg = obj.getString("message");
                        commonFunction.showToast(msg);
                        if (success == 1) {
                            clearInfo(edt_date_of_weld);
                            clearInfo(edt_weld_machine);
                            clearInfo(edt_actual_voltage);
                            clearInfo(edt_actual_amps);
                            clearInfo(edt_actual_preheat);
                            clearInfo(edt_actual_interpass);
                        }
                    }
                } else {
                    commonFunction.showToast("An Error has occurred, Please check internet connection");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
                commonFunction.showToast("An Error has occurred, Please check internet connection");
            }
        } else if (resultCode == RESULT_OK && filter.equals(CONSUMABLE)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            try {
                JSONArray arr = new JSONArray(response);
                commonFunction.cancelDialog();
                if (arr.length() != 0) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = (JSONObject) arr.get(i);
                        int success = obj.getInt("success");
                        String msg = obj.getString("message");
                        if (success == 1) {
                            if (commonFunction.deleteFile(imagePath)) {
                                commonFunction.showToast(msg);
                            } else {
                                commonFunction.showToast("Image could not be deleted locally");
                            }
                            clearInfo(edt_issue_date);
                            clearInfo(edt_weld_no);
                            clearInfo(edt_consSize);
                            clearInfo(edt_cons_batch);
                            clearInfo(edt_received_kg);
                            clearInfo(edt_returned_kg);
                            clearInfo(edt_type);
                        } else {
                            commonFunction.showToast(msg);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
            }
        } else {
            commonFunction.cancelDialog();
            commonFunction.showToast("Something went wrong");
        }
    }

    private void clearInfo(EditText edt) {
        edt.setText("");
    }
}
