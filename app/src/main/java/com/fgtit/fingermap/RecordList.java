package com.fgtit.fingermap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordList extends Activity {


    //DB Class to perform DB related operations
    JobDB mydb = new JobDB(this);
    //Progress Dialog Object
    ProgressDialog prgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);


        //Get User records from SQLite DB
        ArrayList<HashMap<String, String>> recordList =  mydb.getAllrecord();
        final ListView myList=(ListView)findViewById(R.id.rList);

        if(recordList.size()!=0){
            //Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter( RecordList.this,recordList, R.layout.view_user_entry, new String[] { "date","userName"}, new int[] {R.id.txtTime, R.id.userName});

            myList.setAdapter(adapter);
            //Display Sync status of SQLite DB
            Toast.makeText(getApplicationContext(), mydb.getSyncStatus()+" "+String.valueOf(recordList.size()) + " Records", Toast.LENGTH_LONG).show();
        }
        //Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Syncing SQLite Data with Remote MySQL DB. Please wait...");
        prgDialog.setCancelable(false);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int search = position + 1;

                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", search);
               /* Intent intent = new Intent(getApplicationContext(), RecDetails.class);
                intent.putExtras(dataBundle);
                startActivity(intent); */
               Toast.makeText(getApplicationContext(), String.valueOf(search), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
