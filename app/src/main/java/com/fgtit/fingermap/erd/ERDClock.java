package com.fgtit.fingermap.erd;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.adapter.CustomSpinnerAdapter;
import com.fgtit.fingermap.BTScale;
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.R;
import com.fgtit.models.ERDSubTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ERDClock extends AppCompatActivity {

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

    public void clockUser(View v){

        String trade_id = spn_trade.getSelectedItem().toString();
        int key = tradeMap.get(trade_id);
        Toast.makeText(this,"Trade: "+key,Toast.LENGTH_SHORT).show();
    }
    private void initViews(){
        spn_status = findViewById(R.id.status_spinner);
        spn_trade = findViewById(R.id.trade_spinner);

        txt_address = findViewById(R.id.txt_address);
        txt_description= findViewById(R.id.txt_description);
        txt_job_code =  findViewById(R.id.txt_code);
        txt_job_name = findViewById(R.id.txt_name);
        txt_supervisor= findViewById(R.id.txt_supervisor);

        subTaskList = new ArrayList<>();
        tradeMap = new HashMap();

        List<String> statuses = new ArrayList<>();
        statuses.add("IN");
        statuses.add("OUT");

        //status
        initAllSpinner(this,status_prompt,statuses,spn_status);
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
}
