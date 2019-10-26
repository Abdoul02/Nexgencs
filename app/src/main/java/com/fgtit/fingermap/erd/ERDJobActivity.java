package com.fgtit.fingermap.erd;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.ERDjobCard;
import com.fgtit.models.User;
import com.fgtit.service.DownloadService;
import com.fgtit.utils.ExtApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

import static com.fgtit.service.DownloadService.ERD;
import static com.fgtit.service.DownloadService.ERD_DATA_URL;

public class ERDJobActivity extends AppCompatActivity {
    Dialog dialog;
    private static final String TAG = "ERDJobActivity";
    JobDB jobDB = new JobDB(this);
    DBHandler userDB = new DBHandler(this);
    private MyAppAdapter myAppAdapter;
    ArrayList<ERDjobCard> list_of_jobs;
    ListView myList;
    int supervisor_id;
    String job_id;


    //Fingerprint
    //Fingerprint
    private AsyncFingerprint vFingerprint;
    private boolean bIsCancel = false;
    private boolean bfpWork = false;
    private Dialog fpDialog;
    private TextView tvFpStatus;
    private boolean bcheck = false;
    private boolean bIsUpImage = true;
    private int iFinger = 0;
    private int count;
    private ArrayList<User> empList;


    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private ImageView fpImage;



    //Receiving Downloaded info
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            String filter = bundle.getString(DownloadService.FILTER);
            int resultCode = bundle.getInt(DownloadService.RESULT);

            if (resultCode == RESULT_OK && filter.equals(ERD)) {
                String response = bundle.getString(DownloadService.CALL_RESPONSE);
                Log.d(TAG, "onReceive: " + response);

                int success = 0;
                String message = "";

                try {
                    JSONObject result = new JSONObject(response);
                    success = result.getInt("success");
                    message = result.getString("message");

                    if(success == 1){
                        JSONArray arr = result.getJSONArray("data");
                        if (arr.length() != 0) {
                            jobDB.deleteAllERD();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                ERDjobCard jobCard = new ERDjobCard();
                                jobCard.setId(obj.getInt("id"));
                                jobCard.setSupervisorId(obj.getInt("supervisor_id"));
                                jobCard.setJobNo(obj.getString("job_no"));
                                jobCard.setAddress(obj.getString("address"));
                                jobCard.setDescription(obj.getString("description"));
                                jobCard.setProgress(obj.getString("progress"));
                                jobCard.setName(obj.getString("name"));
                                jobCard.setFromDate(obj.getString("from_date"));
                                jobCard.setToDate(obj.getString("to_date"));
                                String tasks = obj.getString("sub_task");
                                JSONArray taskArray = new JSONArray(tasks);

                                if(taskArray.length() !=0){
                                    Log.d(TAG, "Tasks: "+taskArray);
                                    for (int x = 0; x < taskArray.length(); x++) {
                                        JSONObject taskObject = (JSONObject) taskArray.get(x);
                                            Log.d(TAG, "Name: "+taskObject.getString("name"));
                                            Log.d(TAG, "job_card_id: "+taskObject.getString("job_card_id"));
                                            ERDSubTask subTask = new ERDSubTask();
                                            subTask.setName(taskObject.getString("name"));
                                            subTask.setJobCardId(taskObject.getInt("job_card_id"));
                                            subTask.setId(taskObject.getInt("id"));
                                            jobDB.insertERDSubTask(subTask);
                                    }
                                }
                                jobDB.insertERDJob(jobCard);
                            }

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            refresh(message);
                        }
                    }else{

                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        showToast(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "JSON: "+e.getMessage());
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
               // showToast("Something went wrong");
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erdjob);

        list_of_jobs = jobDB.getERDJobList();
        myList = findViewById(R.id.erd_job_list);

        empList = userDB.getAllUsers();

        if (list_of_jobs.size() != 0) {
            myAppAdapter = new MyAppAdapter(list_of_jobs, this);
            myList.setAdapter(myAppAdapter);

            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    TextView txt_job_id =  view.findViewById(R.id.txt_job_id);
                    TextView txt_supervisor_id = view.findViewById(R.id.txt_supervisor_id);
                    job_id = txt_job_id.getText().toString();
                    supervisor_id = Integer.parseInt(txt_supervisor_id.getText().toString());
                    FPDialog(1);

                }
            });
        }else{
            showToast("No jobs");
        }

        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            workExit();
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
                    Toast.makeText(ERDJobActivity.this, "Download jobs first", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                downloadERDJobs();
                return true;
            case R.id.job_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDialog(boolean show) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if (show) {
            dialog.show();
        } else {
            dialog.cancel();
        }
    }
    private void downloadERDJobs(){
        setDialog(true);
        Intent client_intent = new Intent(this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "erd_data");
        client_intent.putExtra(DownloadService.URL,ERD_DATA_URL);
        client_intent.putExtra(DownloadService.FILTER, ERD);
        startService(client_intent);
    }

    public void refresh(String message) {
        showToast(message);
        Intent intent = new Intent(this, ERDJobActivity.class);
        startActivity(intent);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
            this.finish();
        }
    }
    private void FPDialog(int i) {
        iFinger = i;
        AlertDialog.Builder builder = new AlertDialog.Builder(ERDJobActivity.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(ERDJobActivity.this);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = vl.findViewById(R.id.imageView1);
        tvFpStatus = vl.findViewById(R.id.textview1);
        builder.setView(vl);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });

        fpDialog = builder.create();
        fpDialog.setCanceledOnTouchOutside(false);
        fpDialog.show();
        FPProcess();
    }

    private void FPProcess() {

        if (!bfpWork) {
            tvFpStatus.setText(getString(R.string.txt_fpplace));
            try {
                Thread.currentThread();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //imgFeed.setImageResource(R.drawable.green_trans);

            vFingerprint.FP_GetImage();
            bfpWork = true;
        }
    }

    //Finger Print Registration
    private void FPInit() {
        vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener() {
            @Override
            public void onGetImageSuccess() {
                if (bIsUpImage) {
                    vFingerprint.FP_UpImage();
                    tvFpStatus.setText(getString(R.string.txt_fpdisplay));
                } else {
                    tvFpStatus.setText(getString(R.string.txt_fpprocess));
                    vFingerprint.FP_GenChar(1);
                }
            }

            @Override
            public void onGetImageFail() {
                if (!bIsCancel) {
                    vFingerprint.FP_GetImage();
                    //SignLocalActivity.this.AddStatus("Error");
                } else {
                   showToast("Cancel OK");
                }
            }
        });

        vFingerprint.setOnUpImageListener(new AsyncFingerprint.OnUpImageListener() {
            @Override
            public void onUpImageSuccess(byte[] data) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                fpImage.setImageBitmap(image);
                //fpImage.setBackgroundDrawable(new BitmapDrawable(image));
                tvFpStatus.setText(getString(R.string.txt_fpprocess));
                vFingerprint.FP_GenChar(1);
            }

            @Override
            public void onUpImageFail() {
                bfpWork = false;
                TimerStart();
            }
        });

        vFingerprint.setOnGenCharListener(new AsyncFingerprint.OnGenCharListener() {
            @Override
            public void onGenCharSuccess(int bufferId) {
                tvFpStatus.setText(getString(R.string.txt_fpidentify));
                vFingerprint.FP_UpChar();
            }

            @Override
            public void onGenCharFail() {
                tvFpStatus.setText(getString(R.string.txt_fpfail));
            }
        });

        vFingerprint.setOnUpCharListener(new AsyncFingerprint.OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {


                //Check the fingerprints here
                List<User> user = empList;
                int count = 0;
                if (empList.size() > 0) {

                    //for(int i=0; i<emplist.size(); i++)
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if (us.getuId() == supervisor_id) {
                                    fpDialog.cancel();
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putString("job_id", job_id);
                                    Intent intent = new Intent(getApplicationContext(), ERDClock.class);
                                    intent.putExtras(dataBundle);
                                    workExit();
                                    startActivity(intent);
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                } else
                                showToast("Wrong user");
                            }
                        }


                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                if (us.getuId() == supervisor_id) {
                                    fpDialog.cancel();
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putString("job_id", job_id);
                                    Intent intent = new Intent(getApplicationContext(), ERDClock.class);
                                    intent.putExtras(dataBundle);
                                    workExit();
                                    startActivity(intent);
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                } else
                                    showToast("Wrong user");
                            }
                        }

                        count++;
                    }

                    if (count == empList.size()) {
                        Toast.makeText(getApplicationContext(), "fingerprint not found", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getApplicationContext(), "Please download employee information", Toast.LENGTH_SHORT).show();

                bfpWork = false;
                TimerStart();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText("Reading Failed");
                bfpWork = false;
                TimerStart();
            }
        });

    }

    @SuppressLint("HandlerLeak")
    public void TimerStart() {
        if (startTimer == null) {
            startTimer = new Timer();
            startHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    TimeStop();
                    FPProcess();
                }
            };
            startTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    startHandler.sendMessage(message);
                }
            };
            startTimer.schedule(startTask, 1000, 1000);
        }
    }

    public void TimeStop() {
        if (startTimer != null) {
            startTimer.cancel();
            startTimer = null;
            startTask.cancel();
            startTask = null;
        }
    }


    //Adapter
    public class MyAppAdapter extends BaseAdapter  {

        public class ViewHolder {
            TextView txt_job_name, txt_local_id,txt_supervisor,txt_job_code,txt_job_id,txt_supervisor_id;
        }

        public List<ERDjobCard> JobList;

        public Context context;
        ArrayList<ERDjobCard> arraylist;

        private MyAppAdapter(List<ERDjobCard> apps, Context context) {
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
            ERDJobActivity.MyAppAdapter.ViewHolder viewHolder;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.erd_entry, null);
                // configure view holder
                viewHolder = new ERDJobActivity.MyAppAdapter.ViewHolder();
                viewHolder.txt_job_name = rowView.findViewById(R.id.jbName);
                viewHolder.txt_local_id = rowView.findViewById(R.id.txt_local_id);
                viewHolder.txt_job_code = rowView.findViewById(R.id.erd_job_no);
                viewHolder.txt_supervisor = rowView.findViewById(R.id.txt_supervisor);
                viewHolder.txt_job_id = rowView.findViewById(R.id.txt_job_id);
                viewHolder.txt_supervisor_id = rowView.findViewById(R.id.txt_supervisor_id);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ERDJobActivity.MyAppAdapter.ViewHolder) convertView.getTag();
            }

            viewHolder.txt_job_name.setText(JobList.get(position).getName() + "");
            viewHolder.txt_supervisor.setText(userDB.getUserName(JobList.get(position).getSupervisorId())+"");
            viewHolder.txt_job_code.setText(JobList.get(position).getJobNo()+"");
            viewHolder.txt_local_id.setText(JobList.get(position).getLocal_id()+ "");
            viewHolder.txt_job_id.setText(JobList.get(position).getId() + "");
            viewHolder.txt_supervisor_id.setText(JobList.get(position).getSupervisorId()+"");

            return rowView;


        }

        public void filter(String charText) {

            charText = charText.toLowerCase(Locale.getDefault());

            JobList.clear();
            if (charText.length() == 0) {
                JobList.addAll(arraylist);

            } else {
                for (ERDjobCard jobCard : arraylist) {
                    if (charText.length() != 0 && jobCard.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    }
                    else if (charText.length() != 0 && jobCard.getJobNo().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}
