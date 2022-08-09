package com.fgtit.fingermap.job_clock;

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

import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.data.CommonFunction;
import com.fgtit.data.MyConstants;
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.CustomJobCard;
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

public class JobClockActivity extends AppCompatActivity {
    Dialog dialog;
    private static final String TAG = "JobClockActivity";
    JobDB jobDB = new JobDB(this);
    DBHandler userDB = new DBHandler(this);
    CommonFunction cf = new CommonFunction(this);
    private MyAppAdapter myAppAdapter;
    ArrayList<CustomJobCard> list_of_jobs;
    ListView myList;
    int supervisor_id;
    String job_id;
    int companyId;


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
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            assert bundle != null;
            String filter = bundle.getString(DownloadService.FILTER);
            int resultCode = bundle.getInt(DownloadService.RESULT);

            assert filter != null;
            if (resultCode == RESULT_OK && (filter.equals(ERD) || filter.equals(MyConstants.TURNMILL_GET_JOB)
                    || filter.equals(MyConstants.DRYDEN_GET_JOB) || filter.equals(MyConstants.MECHFIT_GET_JOB))) {
                String response = bundle.getString(DownloadService.CALL_RESPONSE);

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                refresh(response);
            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                cf.showToast("Data error has occurred");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erdjob);
        companyId = cf.companyId();

        if (companyId == MyConstants.COMPANY_MECHFIT) {
            list_of_jobs = jobDB.getMechFitJobList();
        } else {
            list_of_jobs = jobDB.getERDJobList();
        }
        myList = findViewById(R.id.erd_job_list);

        empList = userDB.getAllUsers();

        if (list_of_jobs.size() != 0) {
            myAppAdapter = new MyAppAdapter(list_of_jobs, this);
            myList.setAdapter(myAppAdapter);

            myList.setOnItemClickListener((parent, view, position, id) -> {

                TextView txt_job_id = view.findViewById(R.id.txt_job_id);
                TextView txt_supervisor_id = view.findViewById(R.id.txt_supervisor_id);
                job_id = txt_job_id.getText().toString();
                supervisor_id = Integer.parseInt(txt_supervisor_id.getText().toString());
                if (companyId == MyConstants.COMPANY_ERD) {
                    FPDialog(1);
                } else {
                    gotoDetails();
                }
            });
        } else {
            cf.showToast("No jobs");
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

                if (list_of_jobs.isEmpty()) {
                    Toast.makeText(JobClockActivity.this, "Download jobs first", Toast.LENGTH_SHORT).show();
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
                downloadJobs();
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

    private void getJobs(String jsonName, String url, String filter) {
        setDialog(true);
        Intent client_intent = new Intent(this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, jsonName);
        client_intent.putExtra(DownloadService.URL, url);
        client_intent.putExtra(DownloadService.FILTER, filter);
        startService(client_intent);
    }

    private void downloadJobs() {
        switch (companyId) {
            case MyConstants.COMPANY_TURN_MILL:
                getJobs("turnmill_data", MyConstants.TURNMILL_GET_JOB_URL, MyConstants.TURNMILL_GET_JOB);
                break;
            case MyConstants.COMPANY_ERD:
                getJobs("erd_data", ERD_DATA_URL, ERD);
                break;
            case MyConstants.COMPANY_DRYDEN:
                getJobs("dryden_data", MyConstants.DRYDEN_GET_JOB_CARDS_URL, MyConstants.DRYDEN_GET_JOB);
                break;

            case MyConstants.COMPANY_MECHFIT:
                getJobs("mpt", MyConstants.MECHFIT_GET_JOB_URL, MyConstants.MECHFIT_GET_JOB);
                break;
        }
    }

    public void refresh(String message) {
        cf.showToast(message);
        Intent intent = new Intent(this, JobClockActivity.class);
        startActivity(intent);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(JobClockActivity.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(JobClockActivity.this);
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
                    cf.showToast("Cancel OK");
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

                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if (us.getuId() == supervisor_id) {
                                    fpDialog.cancel();
                                    gotoDetails();
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                } else
                                    cf.showToast("Wrong user");
                            }
                        }

                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                if (us.getuId() == supervisor_id) {
                                    fpDialog.cancel();
                                    gotoDetails();
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                } else
                                    cf.showToast("Wrong user");
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

    private void gotoDetails() {
        Bundle dataBundle = new Bundle();
        dataBundle.putString("job_id", job_id);
        Intent intent = new Intent(getApplicationContext(), JobCardClock.class);
        intent.putExtras(dataBundle);
        workExit();
        startActivity(intent);
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
    public class MyAppAdapter extends BaseAdapter {

        public class ViewHolder {
            TextView txt_job_name, txt_local_id, txt_supervisor, txt_job_code, txt_job_id, txt_supervisor_id;
        }

        public List<CustomJobCard> JobList;

        public Context context;
        ArrayList<CustomJobCard> arrayList;

        private MyAppAdapter(List<CustomJobCard> apps, Context context) {
            this.JobList = apps;
            this.context = context;
            arrayList = new ArrayList<>();
            arrayList.addAll(JobList);

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

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            View rowView = convertView;
            JobClockActivity.MyAppAdapter.ViewHolder viewHolder;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.erd_entry, null);
                // configure view holder
                viewHolder = new JobClockActivity.MyAppAdapter.ViewHolder();
                viewHolder.txt_job_name = rowView.findViewById(R.id.jbName);
                viewHolder.txt_local_id = rowView.findViewById(R.id.txt_local_id);
                viewHolder.txt_job_code = rowView.findViewById(R.id.erd_job_no);
                viewHolder.txt_supervisor = rowView.findViewById(R.id.txt_supervisor);
                viewHolder.txt_job_id = rowView.findViewById(R.id.txt_job_id);
                viewHolder.txt_supervisor_id = rowView.findViewById(R.id.txt_supervisor_id);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (JobClockActivity.MyAppAdapter.ViewHolder) convertView.getTag();
            }

            if (companyId == MyConstants.COMPANY_TURN_MILL) {
                viewHolder.txt_job_name.setText(JobList.get(position).getJobNo() + "");
                viewHolder.txt_job_code.setVisibility(View.INVISIBLE);
            } else if (companyId == MyConstants.COMPANY_MECHFIT) {
                viewHolder.txt_job_name.setText(JobList.get(position).getJobNo() + "");
                viewHolder.txt_job_code.setText(JobList.get(position).getCustomerName() + "");
            } else {
                viewHolder.txt_job_name.setText(JobList.get(position).getName() + "");
                viewHolder.txt_job_code.setText(JobList.get(position).getJobNo() + "");
            }

            viewHolder.txt_supervisor.setText(userDB.getUserName(JobList.get(position).getSupervisorId()) + "");
            viewHolder.txt_local_id.setText(JobList.get(position).getLocal_id() + "");
            viewHolder.txt_job_id.setText(JobList.get(position).getId() + "");
            viewHolder.txt_supervisor_id.setText(JobList.get(position).getSupervisorId() + "");

            return rowView;
        }

        public void filter(String charText) {

            charText = charText.toLowerCase(Locale.getDefault());

            JobList.clear();
            if (charText.length() == 0) {
                JobList.addAll(arrayList);

            } else {
                for (CustomJobCard jobCard : arrayList) {
                    if (charText.length() != 0 && jobCard.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
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
