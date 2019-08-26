package com.fgtit.fingermap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class EmpUpdate extends AppCompatActivity {

    TextView updName,userID,finger1,finger2;
    DBHandler myHandler = new DBHandler(this);
    Button btnSave;
    //int id_To_Update = 0;

    //For finger registration
    private ImageView fpImage;
    private int	iFinger=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_update);

        updName  = (TextView) findViewById(R.id.edtUPname);
        userID = (TextView) findViewById(R.id.edtUPid);
        finger1 = (TextView) findViewById(R.id.edtFinger1);
        finger2 = (TextView) findViewById(R.id.edtFinger2);
        updName.setClickable(false);

        Bundle extras = getIntent().getExtras();

       if (extras != null) {
            String Value = extras.getString("test");

            if (Value != null) {

                //means this is the view part not the add contact part.
                Cursor rs = myHandler.getUser(Value);
               // id_To_Update = Value;
                rs.moveToFirst();

                int test = rs.getCount();
               // updName.setText(String.valueOf(test));
              //  userID.setText(String.valueOf(Value));

                if(test >=1 ) {
                    String nam, useId, dat, lat, lng;
                    useId = rs.getString(rs.getColumnIndex("idNum"));
                    nam = rs.getString(rs.getColumnIndex("name"));
                    // dat = rs.getString(rs.getColumnIndex("dat"));
                    // lat = rs.getString(rs.getColumnIndex("lat"));

                    if (!rs.isClosed()) {
                        rs.close();
                    }

                    updName.setText(nam);
                    userID.setText(useId);
                    finger1.setText("not registered");
                    finger2.setText("not registered");
                }

                else{

                    updName.setText(String.valueOf(Value));
                    userID.setText(String.valueOf(test));
                }


            }

        }


    }
}
