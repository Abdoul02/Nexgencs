package com.fgtit.fingermap.dryden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.models.DrydenJobCard;
import com.fgtit.service.DownloadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.fgtit.data.MyConstants.DRYDEN;
import static com.fgtit.data.MyConstants.DRYDEN_GET_JOB_URL;

public class DrydenJobList extends AppCompatActivity {

    private static final String TAG = "DrydenJobList";
    JobDB jobDB = new JobDB(this);
    CommonFunction commonFunction = new CommonFunction(this);
    private MyAppAdapter myAppAdapter;
    ArrayList<DrydenJobCard> list_of_jobs;
    ListView myList;
    String job_id, local_id;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            handleResponse(bundle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dryden_job_list);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                DownloadService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void initViews() {
        list_of_jobs = jobDB.getDrydenJobList();
        myList = findViewById(R.id.drydenJobList);
        if (list_of_jobs.size() != 0) {
            myAppAdapter = new MyAppAdapter(list_of_jobs, this);
            myList.setAdapter(myAppAdapter);
            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView txt_job_id = view.findViewById(R.id.txt_job_id);
                    TextView txt_local_id = view.findViewById(R.id.txt_local_id);
                    job_id = txt_job_id.getText().toString();
                    local_id = txt_local_id.getText().toString();
                    if(!jobDB.isChecked(local_id)){
                        gotoCheckList(job_id);
                    }
                }
            });
        } else {
            commonFunction.showToast("Click icon in top right corner to download jobs");
        }
    }

    private void gotoCheckList(String id){
        Bundle dataBundle = new Bundle();
        dataBundle.putString("job_id", id);
        Intent intent = new Intent(this, CheckList.class);
        intent.putExtras(dataBundle);
        startActivity(intent);
    }

    private void handleResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && filter.equals(DRYDEN)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            int success;
            String message = "";

            try {
                JSONObject result = new JSONObject(response);
                success = result.getInt("success");
                message = result.getString("message");
                if (success == 1) {
                    JSONArray arr = result.getJSONArray("data");
                    if (arr.length() > 0) {
                        jobDB.deleteAllDryden();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            DrydenJobCard jobCard = new DrydenJobCard();
                            jobCard.setId(obj.getInt("id"));
                            jobCard.setSupervisorId(obj.getInt("supervisor_id"));
                            jobCard.setJobNo(obj.getString("job_no"));
                            jobCard.setDescription(obj.getString("description"));
                            jobCard.setJobName(obj.getString("name"));
                            jobCard.setDrawingNo(obj.getString("drawing_no"));
                            jobCard.setIssueDate(obj.getString("from_date"));
                            jobCard.setQcNo(obj.getString("qcp_no"));
                            jobCard.setChecklistDone(0);
                            jobDB.insertDryden(jobCard);
                        }
                    }
                }
               commonFunction.cancelDialog();
                refresh(message);
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
            }
        }else{
            commonFunction.cancelDialog();
            commonFunction.showToast("Something went wrong");
        }
    }

    public void refresh(String message) {
        commonFunction.showToast(message);
        Intent intent = new Intent(this, DrydenJobList.class);
        startActivity(intent);
    }

    private void downloadDrydenJob() {
        commonFunction.setDialog(true);
        Intent client_intent = new Intent(this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "dryden_job");
        client_intent.putExtra(DownloadService.URL, DRYDEN_GET_JOB_URL);
        client_intent.putExtra(DownloadService.FILTER, DRYDEN);
        startService(client_intent);
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
                if (list_of_jobs.isEmpty()) {
                    commonFunction.showToast("Download jobs first");
                } else {
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
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                downloadDrydenJob();
                return true;
            case R.id.job_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyAppAdapter extends BaseAdapter {

        public class ViewHolder {
            TextView txt_job_name, txt_local_id, txt_supervisor, txt_job_code, txt_job_id, txt_supervisor_id;
        }

        public List<DrydenJobCard> JobList;

        public Context context;
        ArrayList<DrydenJobCard> arraylist;

        private MyAppAdapter(List<DrydenJobCard> apps, Context context) {
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
            DrydenJobList.MyAppAdapter.ViewHolder viewHolder;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.erd_entry, null);
                // configure view holder
                viewHolder = new DrydenJobList.MyAppAdapter.ViewHolder();
                viewHolder.txt_job_name = rowView.findViewById(R.id.jbName);
                viewHolder.txt_local_id = rowView.findViewById(R.id.txt_local_id);
                viewHolder.txt_job_code = rowView.findViewById(R.id.erd_job_no);
                viewHolder.txt_supervisor = rowView.findViewById(R.id.txt_supervisor);
                viewHolder.txt_job_id = rowView.findViewById(R.id.txt_job_id);
                viewHolder.txt_supervisor_id = rowView.findViewById(R.id.txt_supervisor_id);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (DrydenJobList.MyAppAdapter.ViewHolder) convertView.getTag();
            }

            viewHolder.txt_job_name.setText(JobList.get(position).getJobName() + "");
            viewHolder.txt_supervisor.setText(getString(R.string.job_no, JobList.get(position).getJobNo()));
            viewHolder.txt_job_code.setText(getString(R.string.qcp_no, JobList.get(position).getQcNo()));
            viewHolder.txt_local_id.setText(JobList.get(position).getLocal_id() + "");
            viewHolder.txt_job_id.setText(JobList.get(position).getId() + "");
            viewHolder.txt_supervisor_id.setText(JobList.get(position).getSupervisorId() + "");

            return rowView;


        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            JobList.clear();
            if (charText.length() == 0) {
                JobList.addAll(arraylist);
            } else {
                for (DrydenJobCard jobCard : arraylist) {
                    if (charText.length() != 0 && jobCard.getJobName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    } else if (charText.length() != 0 && jobCard.getJobNo().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}
