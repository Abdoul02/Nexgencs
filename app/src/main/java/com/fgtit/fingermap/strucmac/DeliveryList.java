package com.fgtit.fingermap.strucmac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
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
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.models.Delivery;
import com.fgtit.service.DownloadService;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.fgtit.data.MyConstants.DELIVERY;
import static com.fgtit.data.MyConstants.STRUCMAC_DATA_URL;

public class DeliveryList extends AppCompatActivity {

    JobDB jobDB = new JobDB(this);
    DBHandler userDB = new DBHandler(this);
    CommonFunction commonFunction = new CommonFunction(this);
    ArrayList<Delivery> list_of_jobs;
    private MyAppAdapter myAppAdapter;
    ListView myList;
    String job_id, local_id;
    HashMap<String, String> addressQueryValues;

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
        list_of_jobs = jobDB.getDeliveries();
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

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("job_id", job_id);
                    Intent intent = new Intent(DeliveryList.this, DeliveryDetail.class);
                    intent.putExtras(dataBundle);
                    startActivity(intent);
                }
            });
        } else {
            commonFunction.showToast("Click icon in top right corner to download jobs");
        }
    }

    public void refresh(String message) {
        commonFunction.showToast(message);
        Intent intent = new Intent(this, DeliveryList.class);
        startActivity(intent);
    }

    private void downloadDelivery() {
        commonFunction.setDialog(true);
        Intent client_intent = new Intent(this, DownloadService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "getDeliveryNotes");
        client_intent.putExtra(DownloadService.URL, STRUCMAC_DATA_URL);
        client_intent.putExtra(DownloadService.FILTER, DELIVERY);
        startService(client_intent);
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
                    myAppAdapter.filter(searchQuery.trim());
                    myList.invalidate();
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
                downloadDelivery();
                return true;
            case R.id.job_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public class MyAppAdapter extends BaseAdapter {

        public class ViewHolder {
            TextView txt_job_name, txt_local_id, txt_supervisor, txt_job_code, txt_job_id, txt_supervisor_id;
        }

        public List<Delivery> JobList;

        public Context context;
        ArrayList<Delivery> arraylist;

        private MyAppAdapter(List<Delivery> apps, Context context) {
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
            DeliveryList.MyAppAdapter.ViewHolder viewHolder;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.erd_entry, null);
                // configure view holder
                viewHolder = new DeliveryList.MyAppAdapter.ViewHolder();
                viewHolder.txt_job_name = rowView.findViewById(R.id.jbName);
                viewHolder.txt_local_id = rowView.findViewById(R.id.txt_local_id);
                viewHolder.txt_job_code = rowView.findViewById(R.id.erd_job_no);
                viewHolder.txt_supervisor = rowView.findViewById(R.id.txt_supervisor);
                viewHolder.txt_job_id = rowView.findViewById(R.id.txt_job_id);
                viewHolder.txt_supervisor_id = rowView.findViewById(R.id.txt_supervisor_id);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (DeliveryList.MyAppAdapter.ViewHolder) convertView.getTag();
            }

            viewHolder.txt_job_name.setText(JobList.get(position).getCustomer() + "");
            viewHolder.txt_supervisor.setText(userDB.getUserName(JobList.get(position).getDriverId()));
            viewHolder.txt_job_code.setText(getString(R.string.delivery_no, JobList.get(position).getDeliveryNote()));
            viewHolder.txt_local_id.setText(JobList.get(position).getLocalId() + "");
            viewHolder.txt_job_id.setText(JobList.get(position).getiD() + "");
            viewHolder.txt_supervisor_id.setText(JobList.get(position).getDriverId() + "");

            return rowView;


        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            JobList.clear();
            if (charText.length() == 0) {
                JobList.addAll(arraylist);
            } else {
                for (Delivery jobCard : arraylist) {
                    if (charText.length() != 0 && jobCard.getCustomer().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    } else if (charText.length() != 0 && jobCard.getDeliveryNote().toLowerCase(Locale.getDefault()).contains(charText)) {
                        JobList.add(jobCard);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    private void handleResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && filter.equals(DELIVERY)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            int success;
            String message = "";

            try {
                JSONObject result = new JSONObject(response);
                success = result.getInt("success");
                message = result.getString("message");
                if (success == 1) {
                    JSONArray deliveries = result.getJSONArray("data");
                    if (deliveries.length() > 0) {
                        jobDB.deleteTable(JobDB.DELIVERY_TABLE);
                        jobDB.deleteTable(JobDB.ADDRESS_TABLE);
                        for (int i = 0; i < deliveries.length(); i++) {
                            JSONObject obj = (JSONObject) deliveries.get(i);
                            Delivery delivery = new Delivery();
                            delivery.setiD(obj.getInt("id"));
                            delivery.setDeliveryDate(obj.getString("delivery_date"));
                            delivery.setDriverId(obj.getInt("driver_id"));
                            delivery.setDeliveryNote(obj.getString("delivery_note_no"));
                            delivery.setCustomer(obj.getString("customer"));
                            delivery.setDeliveryTime(obj.getString("delivery_time"));
                            delivery.setLatitude(obj.getDouble("origin_lat"));
                            delivery.setLongitude(obj.getDouble("origin_lng"));
                            delivery.setOriginAddress(obj.getString("origin_address"));
                            delivery.setVehicleId(obj.getInt("vehicle_id"));
                            jobDB.insertDelivery(delivery);
                        }

                        JSONArray addresses = result.getJSONArray("address");
                        if (addresses.length() > 0) {
                            for (int x = 0; x < addresses.length(); x++) {
                                JSONObject address = (JSONObject) addresses.get(x);
                                addressQueryValues = new HashMap<>();
                                addressQueryValues.put("id", address.getString("addressId"));
                                addressQueryValues.put("delivery_id", address.getString("delivery_note_id"));
                                addressQueryValues.put("sequence", address.getString("sequence"));
                                addressQueryValues.put("address", address.getString("address"));
                                addressQueryValues.put("latitude", address.getString("latitude"));
                                addressQueryValues.put("longitude", address.getString("longitude"));
                                jobDB.insertAddresses(addressQueryValues);
                            }
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
}
