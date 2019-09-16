package com.fgtit.fingermap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.fgtit.models.AdminData;


import java.util.HashMap;

public class AppSettings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    EditText edtOldPass,edtNewPass;
    AdminData data;
    String savedPassword,oldPassword,newPassword;
    CheckBox fingerLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));*/


        data = new AdminData(this);
        HashMap<String, String> manager = data.getDataDetails();
        savedPassword = manager.get(AdminData.PASSWORD);

        edtOldPass = (EditText) findViewById(R.id.edtOldPass);
        edtNewPass = (EditText) findViewById(R.id.edtNewPass);
        fingerLock = (CheckBox) findViewById(R.id.chFingerLock);

        if(savedPassword == null){
            toast("First time setting up password, ignore Old password Field");

        }

        fingerLock.setChecked(data.isLocked());
        fingerLock.setOnCheckedChangeListener(this);



      /*  CheckBox chSync,chLocation;
        chLocation = (CheckBox)findViewById(R.id.chLocation);
        chSync = (CheckBox)findViewById(R.id.chSync);

        chSync.setChecked(getFromSP("chSync"));

        chLocation.setChecked(getFromSP("chLocation"));

        chSync.setOnCheckedChangeListener(this);
        chLocation.setOnCheckedChangeListener(this);
        chLocation.setVisibility(View.GONE);

        if(chSync.isChecked()){

            // BroadCase Receiver Intent Object
            Intent alarmIntent = new Intent(getApplicationContext(), SampleBC.class);
            // Pending Intent Object
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Alarm Manager Object
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            // Alarm Manager calls BroadCast for every Ten seconds (10 * 1000), BroadCase further calls service to check if new records are inserted in
            // Remote MySQL DB
            //1hr
            //600000 * 6 = 1h
            //60*60*1000
            //2min = 120000
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 600000 * 12, pendingIntent);

        }*/

    }

    public void updatePass(View v){


        oldPassword = edtOldPass.getText().toString();
        newPassword = edtNewPass.getText().toString();

            //toast(savedPassword);

            if(savedPassword != null){

                if(oldPassword.length() < 1 && newPassword.length() < 1){

                    toast("Please Provide both fields");
                }else{

                    if(!oldPassword.equals(savedPassword)){
                        toast("Incorrect old Password");
                    }else{

                        data.storePassword(newPassword);
                        toast("New Password saved!");
                        Intent intent = new Intent(AppSettings.this, MenuActivity.class);
                        startActivity(intent);
                    }
                }


            }else{

                if( newPassword.length() < 1){
                    toast("Please Provide new Password");
                }else{
                    data.storePassword(newPassword);
                    toast("New Password saved!");
                    Intent intent = new Intent(AppSettings.this, MenuActivity.class);
                    startActivity(intent);
                }
            }




    }

public void toast(String message){

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
}




    /*private boolean getFromSP(String key){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("NEXGENCS", android.content.Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }
    private void saveInSp(String key,boolean value){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("NEXGENCS", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }*/

    @Override
    public void onCheckedChanged(CompoundButton buttonView,
                                 boolean isChecked) {
        // TODO Auto-generated method stub

        switch(buttonView.getId()){

            case R.id.chFingerLock:
                //saveInSp("chSync",isChecked);
                data.lockFingerReg(isChecked);
                Toast.makeText(getApplicationContext(),String.valueOf(isChecked),Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
