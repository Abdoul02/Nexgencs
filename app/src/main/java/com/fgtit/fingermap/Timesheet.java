package com.fgtit.fingermap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Timesheet extends AppCompatActivity {

    EditText customer,jobN,serialN,hours,spare,details,idNumber;
    Button sign;
    //ProgressDialog prgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timesheet);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

        customer = (EditText) findViewById(R.id.edtCustomer);
        jobN = (EditText) findViewById(R.id.edtJob);
        serialN = (EditText) findViewById(R.id.edtSN);
        hours = (EditText) findViewById(R.id.edtHours);
        spare = (EditText) findViewById(R.id.edtPart);
        details = (EditText) findViewById(R.id.edtDetail);
        idNumber = (EditText) findViewById(R.id.edtIdnum);
        sign = (Button) findViewById(R.id.btnSignature);


        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(idNumber.length() >0 && jobN.length() > 0 && customer.length() > 0 && serialN.length() >0 && details.length() >0 && hours.length() >0 && spare.length() >0){

                    String cust,job,serial,hour,spar,detail,idn;
                    cust = customer.getText().toString();
                    job = jobN.getText().toString();
                    serial = serialN.getText().toString();
                    hour = hours.getText().toString();
                    spar = spare.getText().toString();
                    detail = details.getText().toString();
                    idn = idNumber.getText().toString();

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("cust", cust);
                    dataBundle.putString("job", job);
                    dataBundle.putString("serial", serial);
                    dataBundle.putString("hour", hour);
                    dataBundle.putString("spar", spar);
                    dataBundle.putString("detail", detail);
                    dataBundle.putString("idNum", idn);
                    Intent intent = new Intent(getApplicationContext(), Tsign.class);
                    intent.putExtras(dataBundle);
                    startActivity(intent);

                }else{

                    Toast.makeText(getApplicationContext(),"Please Fill in every information",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
