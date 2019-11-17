package com.fgtit.fingermap.strucmac;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;

public class DeliveryDetail extends AppCompatActivity {

    @BindView(R.id.txtDeliveryNote)
    TextView txtDeliveryNumber;
    @BindView(R.id.txt_driver)
    TextView txt_driver;
    @BindView(R.id.txt_vehicle)
    TextView txt_vehicle;
    @BindView(R.id.txtDeliveryDate)
    TextView txtDeliveryDate;
    @BindView(R.id.txtDeliveryTime)
    TextView txtDeliveryTime;
    @BindView(R.id.txt_customer)
    TextView txt_customer;
    @BindView(R.id.lvAddresses)
    ListView lvAddresses;
    int job_id;
    JobDB jobDB = new JobDB(this);
    DBHandler userDB = new DBHandler(this);
    CommonFunction commonFunction = new CommonFunction(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);
        initViews();
    }

    private void initViews() {
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("job_id")) {
            String value = extras.getString("job_id");
            job_id = Integer.parseInt(value);
            Cursor cursor = jobDB.getDataById(job_id, JobDB.DELIVERY_TABLE);
            cursor.moveToFirst();
            int vehicleId, driverId;
            String customer, driver, vehicle, deliveryNote, deliveryDAte, deliveryTime;

            customer = cursor.getString(cursor.getColumnIndex("customer"));
            deliveryNote = cursor.getString(cursor.getColumnIndex("delivery_note"));
            deliveryDAte = cursor.getString(cursor.getColumnIndex("delivery_date"));
            deliveryTime = cursor.getString(cursor.getColumnIndex("delivery_time"));
            vehicleId = cursor.getInt(cursor.getColumnIndex("vehicle_id"));
            driverId = cursor.getInt(cursor.getColumnIndex("driver_id"));
            driver = userDB.getUserName(driverId);
            vehicle = jobDB.getVehicleById(vehicleId);
            txt_customer.setText(getString(R.string.struc_mac_customer, customer));
            txt_driver.setText(getString(R.string.struc_mac_driver, driver));
            txtDeliveryDate.setText(getString(R.string.delivery_date, deliveryDAte));
            txtDeliveryTime.setText(getString(R.string.delivery_time, deliveryTime));
            txtDeliveryNumber.setText(getString(R.string.delivery_no, deliveryNote));
            txt_vehicle.setText(getString(R.string.struc_mac_vehicle, vehicle));
            ArrayList<HashMap<String, String>> addressList = jobDB.getAddressByDeliveryId(job_id);
            displayAddresses(addressList);
            lvAddresses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView txtAddress = view.findViewById(R.id.txt_addresses);
                    String address = txtAddress.getText().toString();
                    gotoMap(address);
                }
            });

        }
    }

    private void displayAddresses(ArrayList<HashMap<String, String>> addressList) {
        if (addressList.size() > 0) {
            ListAdapter adapter = new SimpleAdapter(DeliveryDetail.this, addressList, R.layout.address_entry, new String[]{"sequence", "address", "latitude", "longitude"}, new int[]{R.id.txt_sequence, R.id.txt_addresses, R.id.txt_latitude, R.id.txt_longitude});
            lvAddresses.setAdapter(adapter);
        } else {
            commonFunction.showToast("No addresses available for this delivery Note");
        }
    }

    public boolean isAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public void gotoMap(String address) {

        if (isAppInstalled("com.google.android.apps.maps")) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=" + address));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Please Install Google Maps for navigation", Toast.LENGTH_SHORT).show();

        }
    }
}
