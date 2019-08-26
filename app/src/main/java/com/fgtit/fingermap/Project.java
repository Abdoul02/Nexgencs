package com.fgtit.fingermap;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.entities.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Project extends AppCompatActivity {

    DBHandler db = new DBHandler(this);
    JobDB jobDB = new JobDB(this);

    SessionManager session;
    HashMap<String, String> manager;
    int companyID;

    LinearLayout linearLayout_jobCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        ArrayList<HashMap<String, String>> projectList = db.getAllProjects();
        ArrayList<HashMap<String, String>> jobList = jobDB.get_all_ec_job();

        session = new SessionManager(this);
        manager = session.getUserDetails();
        companyID = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));

        final ListView myList = findViewById(R.id.projectList);
        linearLayout_jobCode = findViewById(R.id.linearLayout_jobCode);



        if (companyID == 3 || companyID == 116) {
            //effective Cooling JC

            setTitle("List Of Jobs");
            if (jobList.size() != 0) {

                ListAdapter adapter = new SimpleAdapter(Project.this, jobList, R.layout.project_entry, new String[]{"id", "company", "job_id"}, new int[]{R.id.criticalAsset, R.id.clientName, R.id.code});
                myList.setAdapter(adapter);

                myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        TextView c = view.findViewById(R.id.code);
                        String jobId = c.getText().toString();
                        TextView txt_id = view.findViewById(R.id.criticalAsset);
                        String ec_id = txt_id.getText().toString();

                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("id", jobId);
                        dataBundle.putString("db_job_id", ec_id);
                        Intent intent = new Intent(getApplicationContext(), EffectiveCooling.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                });

                myList.setLongClickable(true);
                myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        TextView c = view.findViewById(R.id.criticalAsset);
                        String jobId = c.getText().toString();
                        deleteDialog(jobId);
                        return true;
                    }
                });
            }

        } else {

            //Normal Project
            //linearLayout_jobCode.setVisibility(View.GONE);
            if (projectList.size() != 0) {

                ListAdapter adapter = new SimpleAdapter(Project.this, projectList, R.layout.project_entry, new String[]{"criticalAsset", "requestedBy"}, new int[]{R.id.criticalAsset, R.id.clientName});
                myList.setAdapter(adapter);
            }


            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    TextView c = view.findViewById(R.id.criticalAsset);
                    String criticalA = c.getText().toString();

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("criticalA", criticalA);
                    Intent intent = new Intent(getApplicationContext(), ProjectUpdate.class);
                    intent.putExtras(dataBundle);
                    startActivity(intent);
                }
            });
        }

    }

    public void createJob(View v) {

        if (companyID == 3 || companyID == 116) {

            Intent intent = new Intent(Project.this, CreateEffectiveJob.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Project.this, ProjectDetail.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            exitApplication();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApplication() {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {

            Intent intent = new Intent(Project.this, ProjectDetail.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteDialog(final String id) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Delete Job Card");
        dialogBuilder.setMessage("Do you want to delete this job card?");

        dialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
               jobDB.delete_ec_job(id);
                reload();
            }
        });
        dialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();

    }

    public void reload(){
        Intent intent = new Intent(this, Project.class);
        startActivity(intent);
    }
}
