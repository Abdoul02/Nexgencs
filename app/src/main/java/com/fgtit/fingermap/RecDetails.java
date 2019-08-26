package com.fgtit.fingermap;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RecDetails extends Activity {

    JobDB mydb = new JobDB(this);
    TextView name,userID,date,lats,lngs;
    Button btnSave;
    int id_To_Update = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_details);

        name  = (TextView) findViewById(R.id.edtName);
        userID = (TextView) findViewById(R.id.edtId);
        date  = (TextView) findViewById(R.id.edtdate);
        lats   = (TextView) findViewById(R.id.edtlat);
        lngs   = (TextView) findViewById(R.id.edtlong);



        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int Value = extras.getInt("id");

            if (Value > 0) {
                //means this is the view part not the add contact part.
                Cursor rs = mydb.getRec(Value);
                id_To_Update = Value;
                rs.moveToFirst();
                String useId,dat,lat,lng;
                useId = rs.getString(rs.getColumnIndex("userId"));
                int  nam = rs.getInt(rs.getColumnIndex("id"));
                 dat = rs.getString(rs.getColumnIndex("dat"));
                 lat = rs.getString(rs.getColumnIndex("lat"));
                 lng = rs.getString(rs.getColumnIndex("lng"));

                if (!rs.isClosed()) {
                    rs.close();
                }

                name.setText(String.valueOf(nam));
                userID.setText(useId);
                date.setText(dat);
                lats.setText(lat);
                lngs.setText(lng);

            }

        }

    }
}
