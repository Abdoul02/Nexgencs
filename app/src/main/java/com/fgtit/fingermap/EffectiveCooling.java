package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.models.EcCustomer;
import com.fgtit.models.EcProduct;
import com.fgtit.service.DownloadService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.fingermap.CreateEffectiveJob.JOBURL;
import static com.fgtit.service.DownloadService.PRODUCTS;

public class EffectiveCooling extends AppCompatActivity {

    private LinearLayout parentLinearLayout;
    TextView txt_time_in, txt_time_out;
    EditText edt_work, edt_price, edt_quatity;
    AutoCompleteTextView edt_product;
    public static final String TAG = "EffectiveCooling_Test";
    Dialog dialog;
    String job_id, ec_id;
    String[] productsArray;
    private ArrayList<EcProduct> productList;
    List<String> allProducts = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String URL = "http://www.nexgencs.co.za/alos/capture_ec_job_info.php";
    String currentDateTime, status;
    JobDB jobDB = new JobDB(this);
    HashMap<String, String> queryValues;
    HashMap<String, String> productqueryValues;
    ArrayAdapter<String> adapter;
    //Receiving Downloaded info
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            Bundle bundle = intent.getExtras();
            String filter = bundle.getString(DownloadService.FILTER);
            int resultCode = bundle.getInt(DownloadService.RESULT);
            //String latest_rate = bundle.getDouble(DownloadService.LATEST_RATE);
            setDialog(false);
            if (resultCode == RESULT_OK && filter.equals(PRODUCTS)) {
                String response = bundle.getString(DownloadService.CALL_RESPONSE);
                Log.d(TAG, "onReceive: " + response);

                try {
                    JSONArray arr = new JSONArray(response);
                    if (arr.length() != 0) {
                        jobDB.deleteAllProduct();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            EcProduct product = new EcProduct();
                            product.setId(obj.getInt("id"));
                            product.setName(obj.getString("name"));
                            product.setPrice(obj.getString("price"));
                            jobDB.insertProduct(product);
                        }
                        refresh("Products saved successfully");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                showToast("Something went wrong");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effective_cooling);
        setTitle("Job Card Info");

        parentLinearLayout = findViewById(R.id.linearLayoutMaterial);
        txt_time_in = findViewById(R.id.txt_time_in);
        txt_time_out = findViewById(R.id.txt_time_out);
//        edt_travel_time = findViewById(R.id.edt_travel_time);
        edt_work = findViewById(R.id.edt_work);

        edt_product = findViewById(R.id.edt_material);
        edt_quatity = findViewById(R.id.edt_qty);
        edt_price = findViewById(R.id.edt_unit_price);

        queryValues = new HashMap<>();
        productqueryValues = new HashMap<>();

        txt_time_in.setVisibility(View.INVISIBLE);
        txt_time_out.setVisibility(View.INVISIBLE);


        productList = jobDB.getAllProducts();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            job_id = extras.getString("id");
            ec_id = extras.getString("db_job_id");

            if (productList.isEmpty()) {
                showToast("Please Download Products by clicking the cloud icon");
            } else {

                List<EcProduct> products = productList;
                for (EcProduct product : products) {
                    allProducts.add(product.getName());
                }
                productsArray = allProducts.toArray(new String[0]);
                adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, productsArray);
                edt_product.setThreshold(1);
                edt_product.setAdapter(adapter);
                edt_product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selected = (String) parent.getItemAtPosition(position);
                        edt_price.setText(jobDB.getProductPrice(selected));
                    }
                });
            }

        } else {
            finish();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.effective_cooling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.sign) {

            Bundle dataBundle = new Bundle();
            dataBundle.putString("code", job_id);
            dataBundle.putString("url", "http://www.nexgencs.co.za/alos/ec_job_card/sign.php");
            Intent intent = new Intent(getApplicationContext(), Signature.class);
            intent.putExtras(dataBundle);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
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
        Intent intent = new Intent(getApplicationContext(), Project.class);
        startActivity(intent);
    }

    public void viewProducts(View v) {
        ArrayList<HashMap<String, String>> productList = getSavedProducts();
        final ListView myList = findViewById(R.id.product_list);

        if (productList.size() != 0) {

            ListAdapter adapter = new SimpleAdapter(this, productList, R.layout.product_entry, new String[]{"material_id", "name", "quantity"}, new int[]{R.id.product_id, R.id.Product_name, R.id.db_quantity});
            myList.setAdapter(adapter);

            myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageView imageView = view.findViewById(R.id.imgDelete);
                    TextView txt_id = view.findViewById(R.id.product_id);
                    final String product_id = txt_id.getText().toString();
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            jobDB.delete_ec_material(product_id);
                            refresh("Material deleted");
                        }
                    });
                }
            });
        } else {
            showToast("No Products have been captured");
        }
    }

    public ArrayList<HashMap<String, String>> getSavedProducts() {
        ArrayList<HashMap<String, String>> productList;
        productList = new ArrayList<>();

        JSONArray arr;
        try {
            arr = new JSONArray(jobDB.ec_material_JSON(job_id));
            if (arr.length() != 0) {

                for (int i = 0; i < arr.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject obj = (JSONObject) arr.get(i);
                    map.put("name", obj.getString("material_used"));
                    map.put("quantity", obj.getString("quantity"));
                    map.put("id", obj.getString("id"));
                    map.put("material_id", obj.getString("material_id"));
                    // map.put("price", obj.getString("unit_price"));
                    productList.add(map);
                    Log.d(TAG, obj.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        return productList;
    }

    public void addField(View v) {

     /*   LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.field, null);

        LinearLayout kid = (LinearLayout) v.getParent();

        TextInputLayout txt_quantity = (TextInputLayout) kid.getChildAt(0);
        EditText quantity = txt_quantity.getEditText();

        TextInputLayout txt_material = (TextInputLayout) kid.getChildAt(1);
        AutoCompleteTextView material = (AutoCompleteTextView)txt_material.getEditText();

        TextInputLayout txt_price = (TextInputLayout) kid.getChildAt(2);
        EditText price = txt_price.getEditText();



        if (quantity.getText().length() > 0 && material.getText().length() > 0 && price.getText().length() > 0) {
            // Add the new row.
            parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount());
            setAdapter(adapter,material);
        } else {
            showToast("Please fill all details be adding a new material");
        }*/
        String quantity = edt_quatity.getText().toString();
        String product = edt_product.getText().toString();
        String price = edt_price.getText().toString();

        if (quantity.length() > 0 && product.length() > 0 && price.length() > 0) {

            if (jobDB.getProductId(product) > 0) {
                productqueryValues.put("job_id", job_id);
                productqueryValues.put("id", String.valueOf(jobDB.getProductId(product)));
                productqueryValues.put("quantity", quantity);
                productqueryValues.put("material_used", product);
                productqueryValues.put("unit_price", price);
                jobDB.insert_ec_material(productqueryValues);
                showToast("Product successfully captured");
                edt_product.setText("");
                edt_quatity.setText("");
                edt_price.setText("");
            } else {
                showToast("This product is not in the device");
            }

        }


    }

    public void removeField(View v) {

        parentLinearLayout.removeView((View) v.getParent());
    }

    public void timeIn(View v) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        currentDateTime = sdf.format(new Date());
        status = "IN";
        if (jobDB.checkSignIn(status, date, ec_id) > 0) {
            showToast("You already Have a Time IN for today");
        } else {

         //   if (edt_km.getText().length() > 0 && edt_travel_time.getText().length() > 0) {
                txt_time_in.setText(currentDateTime);
                txt_time_in.setVisibility(View.VISIBLE);
                uploadInfo(status);
        }


    }

    public void timeOut(View v) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        currentDateTime = sdf.format(new Date());
        status = "OUT";


        if (parentLinearLayout.getChildCount() > 1) {
            showToast("Please upload first");
        }

        if (jobDB.checkSignIn(status, date, ec_id) > 0) {
            showToast("You already Have a Time OUT for today");
        } else {
            txt_time_out.setText(currentDateTime);
            txt_time_out.setVisibility(View.VISIBLE);
            uploadInfo(status);
        }

    }

    public void upload(View v) {

        String currentDateTime = sdf.format(new Date());
        uploadInfo("");

        // Log.d(TAG, "Material: "+materialJSON());

    }

    public void uploadInfo(String status) {

        String work_undertaken;// travel_time;
        work_undertaken = edt_work.getText().toString();
        //travel_time = edt_travel_time.getText().toString();

        String currentDateTime = sdf.format(new Date());

        JSONObject postDataParams = new JSONObject();
        try {

            postDataParams.accumulate("job_id", job_id);
            postDataParams.accumulate("work_undertaken", work_undertaken);
            postDataParams.accumulate("date_in", currentDateTime);
            postDataParams.accumulate("status", status);
            //postDataParams.accumulate("travelling_time", travel_time);
            postDataParams.accumulate("materials", jobDB.ec_material_JSON(job_id));
            postRequest(postDataParams.toString());
            Log.d(TAG, "JSONObject: " + postDataParams.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String materialJSON() {

        ArrayList<HashMap<String, String>> materialList;
        materialList = new ArrayList<>();


        for (int i = 0; i < parentLinearLayout.getChildCount(); i++) {

            HashMap<String, String> map = new HashMap<String, String>();

            LinearLayout kid = (LinearLayout) parentLinearLayout.getChildAt(i);

            TextInputLayout txt_quantity = (TextInputLayout) kid.getChildAt(0);
            EditText quantity = txt_quantity.getEditText();

            TextInputLayout txt_material = (TextInputLayout) kid.getChildAt(1);
            AutoCompleteTextView material = (AutoCompleteTextView) txt_material.getEditText();


            TextInputLayout txt_price = (TextInputLayout) kid.getChildAt(2);
            EditText price = txt_price.getEditText();

            map.put("quantity", quantity.getText().toString());
            map.put("material_used", material.getText().toString());
            map.put("unit_price", price.getText().toString());
            materialList.add(map);
        }

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(materialList);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setDialog(boolean show) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(EffectiveCooling.this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if (show) {
            dialog.show();
        } else {
            dialog.cancel();
        }

    }

    public void downloadProduct(View v) {

        setDialog(true);
        Intent product_intent = new Intent(this, DownloadService.class);
        product_intent.putExtra(DownloadService.POST_JSON, "ec_products");
        product_intent.putExtra(DownloadService.FILTER, PRODUCTS);
        startService(product_intent);

    }

    public Object postRequest(String param) throws IOException {

        //System.out.println("PARAM==="+param);
        setDialog(true);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ec_job_json", param)
                .build();
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();
        //Log.d("PARAM:+++",param[0]+ " "+param[1]);
        Log.d(TAG, "ec_job_json: " + param);
        client.newCall(request).enqueue(new Callback() {
            Handler handler = new Handler(EffectiveCooling.this.getMainLooper());
            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error, Please check network");
                    }
                });
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (!response.isSuccessful()) {

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showToast("Unexpected error: " + response.message());

                        } else {

                            try {

                                apiFeedback(response.body().string());
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                                showToast("Something went wrong contact admin");

                            }

                        }


                    }
                });
            }
        });

        return null;
    }

    public void apiFeedback(String response) {

        try {
            JSONObject object = new JSONObject(response);
            int success = object.getInt("success");
            String message = object.getString("message");
            if (success == 1) {

                queryValues.put("job_id", job_id);
                queryValues.put("id", ec_id);
                queryValues.put("work_undertaken", "");
                queryValues.put("km", "");
                queryValues.put("clock_time", currentDateTime);
                queryValues.put("status", status);
                queryValues.put("travelling_time", "");
                jobDB.insert_job_info(queryValues);
                jobDB.deleteAllMaterials(job_id);
                refresh(message);
            } else {
                showToast(message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reload(String message) {
        showToast(message);
        edt_work.setText("");
        txt_time_out.setText("");
        txt_time_in.setText("");

        LinearLayout kid = (LinearLayout) parentLinearLayout.getChildAt(0);

        TextInputLayout txt_quantity = (TextInputLayout) kid.getChildAt(0);
        txt_quantity.getEditText().setText("");

        TextInputLayout txt_material = (TextInputLayout) kid.getChildAt(1);
        txt_material.getEditText().setText("");

        TextInputLayout txt_price = (TextInputLayout) kid.getChildAt(2);
        txt_price.getEditText().setText("");


        if (parentLinearLayout.getChildCount() > 1) {

            int x = parentLinearLayout.getChildCount() - 1;
            do {
                parentLinearLayout.removeView(parentLinearLayout.getChildAt(x));
                x--;
            } while (x > 0);

        }

        Intent intent = new Intent(this, EffectiveCooling.class);
        startActivity(intent);
    }

    public void refresh(String message) {
        showToast(message);
       /* Intent intent = new Intent(this, EffectiveCooling.class);
        startActivity(intent);*/
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }


}
