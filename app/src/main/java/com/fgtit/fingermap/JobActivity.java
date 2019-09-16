package com.fgtit.fingermap;

import com.fgtit.models.JobCard;
import com.fgtit.models.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class JobActivity extends AppCompatActivity {

    JobDB db = new JobDB(this);
    ProgressDialog prgDialog;
    HashMap<String, String> queryValues;
    // Session Manager Class
    SessionManager session;

    private MyAppAdapter myAppAdapter;
    ArrayList<JobCard> list_of_jobs;
    ListView myList;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

        //ArrayList<HashMap<String, String>> jobList = db.getAlljobcard();
        list_of_jobs = db.getJobList();

        myList = findViewById(R.id.jobList);

        if (list_of_jobs.size() != 0) {
            myAppAdapter = new MyAppAdapter(list_of_jobs, this);
            myList.setAdapter(myAppAdapter);
        }

        //Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Retrieving Job Card(s). Please wait...");
        prgDialog.setCancelable(false);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView c = view.findViewById(R.id.jobID);
                String jobID = c.getText().toString();

                String find = jobID;
                Bundle dataBundle = new Bundle();
                dataBundle.putString("jobCode", find);
                Intent intent = new Intent(getApplicationContext(), JobDetail.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.records, menu);

        MenuItem searchItem = menu.findItem(R.id.job_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //*** setOnQueryTextFocusChangeListener ***
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {

                if(list_of_jobs.isEmpty()){
                    Toast.makeText(JobActivity.this, "Download jobs first", Toast.LENGTH_SHORT).show();
                }else{
                    myAppAdapter.filter(searchQuery.trim());
                    myList.invalidate();
                }

                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //this.finish();
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                return true;

            case R.id.refresh:
                syncSQLiteMySQLDB();
                return true;

            case R.id.job_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class MyAppAdapter extends BaseAdapter {

        public class ViewHolder {
            TextView txt_job_name, txt_jobID;


        }

        public List<JobCard> JobList;

        public Context context;
        ArrayList<JobCard> arraylist;

        private MyAppAdapter(List<JobCard> apps, Context context) {
            this.JobList = apps;
            this.context = context;
            arraylist = new ArrayList<>();
            arraylist.addAll(JobList);

        }

        @Override
        public int getCount() {
            return JobList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            View rowView = convertView;
            JobActivity.MyAppAdapter.ViewHolder viewHolder;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.job_entry, null);
                // configure view holder
                viewHolder = new JobActivity.MyAppAdapter.ViewHolder();
                viewHolder.txt_job_name = rowView.findViewById(R.id.jbName);
                viewHolder.txt_jobID = rowView.findViewById(R.id.jobID);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (JobActivity.MyAppAdapter.ViewHolder) convertView.getTag();
            }

            viewHolder.txt_job_name.setText(JobList.get(position).getName() + "");
            viewHolder.txt_jobID.setText(JobList.get(position).getJob_id() + "");
            //Toast.makeText(getApplicationContext(),"Position error",Toast.LENGTH_SHORT).show();
            return rowView;


        }

        public void filter(String charText) {

            charText = charText.toLowerCase(Locale.getDefault());

            JobList.clear();
            if (charText.length() == 0) {
                JobList.addAll(arraylist);

            } else {
                for (JobCard jobCard : arraylist) {
                    if (charText.length() != 0 && jobCard.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }


    // Method to Sync MySQL to SQLite DB
    public void syncSQLiteMySQLDB() {

        try {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();

            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));


            String json = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("dat", "dummy");
            jsonObject.accumulate("userID", userID);
            //  convert JSONObject to JSON to String
            json = jsonObject.toString();

            prgDialog.show();

            params.put("jobJSON", json);

            // Make Http call to getJobs.php
            client.post("http://www.nexgencs.co.za/api/getJobs.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                    // Hide ProgressBar
                    prgDialog.hide();
                    // Update SQLite DB with response sent by getusers.php
                    updateSQLite(response);
                }

                // When error occured
                @Override
                public void onFailure(int statusCode, Throwable error, String content) {
                    // TODO Auto-generated method stub
                    // Hide ProgressBar
                    prgDialog.hide();
                    if (statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception e) {

            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void updateSQLite(String response) {

        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            System.out.println(arr.length());

            // If no of array elements is not zero
            if (arr.length() != 0) {
                db.deleteAll();
                // Loop through each array element, get JSON object which has userid and username
                for (int i = 0; i < arr.length(); i++) {
                    // Get JSON object
                    JSONObject obj = (JSONObject) arr.get(i);
                    System.out.println(obj.get("jobCardId"));
                    System.out.println(obj.get("jobName"));
                    // DB QueryValues Object to insert into SQLite
                    queryValues = new HashMap<String, String>();

                    queryValues.put("jobID", obj.get("jobCardId").toString());
                    queryValues.put("name", obj.get("jobName").toString());
                    queryValues.put("description", obj.get("description").toString());
                    queryValues.put("location", obj.get("location").toString());
                    queryValues.put("assignee", obj.get("assignee").toString());
                    queryValues.put("progress", obj.get("progress").toString());
                    queryValues.put("approvedBy", obj.get("approvedBy").toString());
                    queryValues.put("customer", obj.get("customer").toString());
                    queryValues.put("start", obj.get("jobStart").toString());
                    queryValues.put("end", obj.get("jobEnd").toString());
                    queryValues.put("jobCode", obj.get("jobCode").toString());
                    queryValues.put("attachment", obj.get("attachment").toString());
                    queryValues.put("office", obj.get("office").toString());


                    // Insert User into SQLite DB
                    db.insertjobcard(queryValues);
                }
                // Reload the Main Activity
                reloadActivity();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), JobActivity.class);
        startActivity(objIntent);
    }
}
