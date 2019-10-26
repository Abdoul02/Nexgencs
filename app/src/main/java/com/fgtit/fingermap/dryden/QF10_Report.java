package com.fgtit.fingermap.dryden;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;

public class QF10_Report extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qf__report);
    }

    public void captureVerification(View view){
        Intent intent = new Intent(this, QF10_WeldMap.class);
        startActivity(intent);
    }
    public void captureConsumable(View view){
        Intent intent = new Intent(this, CheckList.class);
        startActivity(intent);
    }
}
