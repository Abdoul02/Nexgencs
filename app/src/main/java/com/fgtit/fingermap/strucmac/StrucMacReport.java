package com.fgtit.fingermap.strucmac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.R;
import com.fgtit.service.DownloadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.fgtit.data.MyConstants.STRUCMAC_CHECKLIST;
import static com.fgtit.data.MyConstants.STRUCMAC_DATA_URL;
import static com.fgtit.data.MyConstants.STRUCMAC_VEHICLE;

public class StrucMacReport extends AppCompatActivity {

    @BindView(R.id.edtPlantNo)
    EditText edtPlantNo;
    @BindView(R.id.edtRegNo)
    AutoCompleteTextView actRegNo;
    @BindView(R.id.edtWorkCondition)
    EditText edtWorkCondition;
    @BindView(R.id.edtFault)
    EditText edtFault;
    @BindView(R.id.edtLicence)
    EditText edtLicence;
    @BindView(R.id.edtDailyKm)
    EditText edtKm;

    HashMap<String, String> vehicleQueryValues;
    ArrayList<HashMap<String, String>> vehicleList;
    JobDB jobDB = new JobDB(this);
    CommonFunction commonFunction = new CommonFunction(this);
    String[] vehicleArray;
    String vehicle_id;

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
        setContentView(R.layout.activity_struc_mac_report);
        ButterKnife.bind(this);
        setUp();
    }

    private void setUp() {
        edtLicence.setClickable(false);
        List<String> allVehicles = new ArrayList<>();
        vehicleList = jobDB.getVehicleInfo();
        if (vehicleList.size() > 0) {
            for (int i = 0; i < vehicleList.size(); i++) {
                HashMap<String, String> hashMap = vehicleList.get(i);
                String registrationNo = hashMap.get("reg_no");
                allVehicles.add(registrationNo);
            }
            vehicleArray = allVehicles.toArray(new String[0]);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, vehicleArray);
            actRegNo.setThreshold(1);
            actRegNo.setAdapter(adapter);
            actRegNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = adapter.getItem(position);
                    Cursor cursor = jobDB.getVehicleByRegNo(item);
                    cursor.moveToFirst();
                    if (cursor.getCount() > 0) {
                        String licence_disc = cursor.getString(cursor.getColumnIndex("licence_disc"));
                        vehicle_id = cursor.getString(cursor.getColumnIndex("id"));
                        edtLicence.setText(licence_disc);
                    } else commonFunction.showToast("This Vehicle does not any data");
                }
            });
        }else commonFunction.showToast("Please download vehicle information by clicking cloud icon");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_only,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch(id){
            case R.id.download:
                downloadVehicleList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void downloadVehicleList() {
        commonFunction.setDialog(true);
        Intent client_intent = new Intent(this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "getCarList");
        client_intent.putExtra(DownloadService.URL, STRUCMAC_DATA_URL);
        client_intent.putExtra(DownloadService.FILTER, STRUCMAC_VEHICLE);
        startService(client_intent);
    }

    private void handleResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && (filter != null && filter.equals(STRUCMAC_VEHICLE))) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            int success;
            String message;
            try {
                JSONObject result = new JSONObject(response);
                success = result.getInt("success");
                message = result.getString("message");
                if (success == 1) {
                    JSONArray vehicleArray = result.getJSONArray("data");
                    if (vehicleArray.length() > 0) {
                        jobDB.deleteTable(JobDB.VEHICLE_TABLE);
                        for (int i = 0; i < vehicleArray.length(); i++) {
                            JSONObject obj = (JSONObject) vehicleArray.get(i);
                            vehicleQueryValues = new HashMap<>();
                            vehicleQueryValues.put("id", obj.getString("id"));
                            vehicleQueryValues.put("reg_no", obj.getString("reg_no"));
                            vehicleQueryValues.put("licence_disc", obj.getString("licence_disc"));
                            vehicleQueryValues.put("km", obj.getString("kilometers"));
                            jobDB.insertVehicle(vehicleQueryValues);
                        }
                    }
                }
                commonFunction.cancelDialog();
                refresh(message);
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
            }
        } else {
            commonFunction.cancelDialog();
            commonFunction.showToast("Something went wrong");
        }

    }

    public void gotoStrucMacChecklist(View view) {
        if (commonFunction.checkTextLength(edtPlantNo.getText().toString()) && commonFunction.checkTextLength(edtWorkCondition.getText().toString())
                && commonFunction.checkTextLength(edtFault.getText().toString()) && commonFunction.checkTextLength(edtKm.getText().toString())
                && commonFunction.checkTextLength(actRegNo.getText().toString())) {
            Bundle dataBundle = new Bundle();
            dataBundle.putString("vehicle_id", vehicle_id);
            dataBundle.putString("work_condition", edtWorkCondition.getText().toString());
            dataBundle.putString("fault", edtFault.getText().toString());
            dataBundle.putString("km", edtKm.getText().toString());
            dataBundle.putString("plant_no", edtPlantNo.getText().toString());
            Intent intent = new Intent(getApplicationContext(), StrucMacCheckList.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
        } else commonFunction.showToast("Please provide all fields");
    }

    public void refresh(String message) {
        commonFunction.showToast(message);
        Intent intent = new Intent(this, StrucMacReport.class);
        startActivity(intent);
    }
}
