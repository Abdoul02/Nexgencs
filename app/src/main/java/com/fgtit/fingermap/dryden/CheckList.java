package com.fgtit.fingermap.dryden;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.fgtit.adapter.ClockActivity;
import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.R;

import static com.fgtit.data.MyConstants.ID_NUMBER;
import static com.fgtit.data.MyConstants.USERNAME;
import static com.fgtit.data.MyConstants.USER_ID;

public class CheckList extends AppCompatActivity {

    int checkCount = 0;
    LinearLayout linearLayout;
    String[] checklist;
    CheckBox checkBox;
    CommonFunction commonFunction = new CommonFunction(this);
    JobDB jobDB = new JobDB(this);
    private static final int REQUEST_CLOCK = 101;
    String job_id,job_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);
        initViews();
    }

    private void initViews() {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            job_id = extras.getString("job_id");
            Cursor cursor = jobDB.getDrydenJobById(Integer.parseInt(job_id));
            cursor.moveToFirst();
            job_code = cursor.getString(cursor.getColumnIndex("job_no"));
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
                Intent alcoholIntent = new Intent(this, ClockActivity.class);
                startActivityForResult(alcoholIntent,REQUEST_CLOCK);
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
        if(requestCode == REQUEST_CLOCK && resultCode == RESULT_OK){
            String userName = data.getStringExtra(USERNAME);
            String userId = data.getStringExtra(USER_ID);
            String idNumber = data.getStringExtra(ID_NUMBER);
            commonFunction.showToast(userId+"-"+" "+userName+"=> "+idNumber);
        }else{
            commonFunction.showToast("Operation Cancelled");
        }
    }
}
