package com.fgtit.fingermap.strucmac;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.DBHandler;
import com.fgtit.fingermap.DisplayLocalPictures;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.R;
import com.fgtit.service.SingleUploadBroadcastReceiver;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fgtit.data.MyConstants.STRUCMAC_IMAGE_UPLOAD;
import static com.fgtit.data.MyConstants.STRUCMAC_UPLOAD_URL;

public class DeliveryDetail extends AppCompatActivity implements SingleUploadBroadcastReceiver.Delegate {

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

    //Multiple pictures
    String currentPhotoPath, fileID, imgPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private List<String> listOfImagesPath;

    JobDB jobDB = new JobDB(this);
    DBHandler userDB = new DBHandler(this);
    CommonFunction commonFunction = new CommonFunction(this);
    private final SingleUploadBroadcastReceiver uploadReceiver =
            new SingleUploadBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.job_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.finish) {
            listOfImagesPath = jobDB.getPictures(String.valueOf(job_id));
            if(listOfImagesPath.size() > 0){
                commonFunction.showToast("Please upload pictures first");
            }else{
                completeDelivery();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("job_id")) {
            listOfImagesPath = new ArrayList<>();
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

    //Capture and Show Images

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            jobDB.insertPictPath(0, currentPhotoPath, String.valueOf(job_id));
            anotherPicture();
        } else {
            commonFunction.showToast("Something went wrong, could not save picture");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void takePicture(View view) {
        dispatchTakePictureIntent();
    }

    public void showPictures(View view) {
        listOfImagesPath = jobDB.getPictures(String.valueOf(job_id));
        if (listOfImagesPath.size() > 0) {
            Bundle dataBundle = new Bundle();
            dataBundle.putString("asset", String.valueOf(job_id));
            Intent intent = new Intent(getApplicationContext(), DisplayLocalPictures.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
        } else {
            commonFunction.showToast("No pictures to display");
        }
    }

    private void anotherPicture() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Picture Saved!");
        dialog.setMessage("Would you like to take another picture?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dispatchTakePictureIntent();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void completeDelivery() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Complete Delivery note!");
        dialog.setMessage("Pressing finish will mark this delivery note complete. Continue?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                JSONObject postDataParams = new JSONObject();
                try {
                    postDataParams.accumulate("job_id", String.valueOf(job_id));
                    postRequest(postDataParams.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "co.za.nexgencs.clocking.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SM_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Picture Network related
    public void uploadImages(View view) {
        listOfImagesPath = jobDB.getPictures(String.valueOf(job_id));
        if (listOfImagesPath.size() > 0) {
            commonFunction.showProgressDialog();
            uploadFunction();
        } else {
            commonFunction.showToast("No picture to upload");
        }
    }

    public void uploadFunction() {
        fileID = UUID.randomUUID().toString();
        uploadReceiver.setDelegate(this);
        uploadReceiver.setUploadID(fileID);

        imgPath = listOfImagesPath.get(0).substring(0, listOfImagesPath.get(0).lastIndexOf("/") + 1);
        try {
            MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(this, fileID, STRUCMAC_IMAGE_UPLOAD);
            multipartUploadRequest.addHeader("Accept", "application/json");
            for (int i = 0; i < listOfImagesPath.size(); i++) {
                String imageName = listOfImagesPath.get(i).substring(listOfImagesPath.get(i).lastIndexOf("/") + 1);
                multipartUploadRequest.addParameter("name" + i, imageName);
                multipartUploadRequest.addFileToUpload(listOfImagesPath.get(i), "document" + i);
            }
            multipartUploadRequest.addParameter("numberOfPict", String.valueOf(listOfImagesPath.size()));
            multipartUploadRequest.addParameter("deliveryNoteId", String.valueOf(job_id));
            multipartUploadRequest.setNotificationConfig(new UploadNotificationConfig());
            multipartUploadRequest.setMaxRetries(3);
            multipartUploadRequest.startUpload();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean deleteFile(String path) {
        File fileToDelete = new File(path);
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void uploadResponse(String response) {
        try {
            JSONObject result = new JSONObject(response);
            int count = 0;
            JSONArray pictures = result.getJSONArray("pictures");
            if (pictures.length() > 0) {
                for (int x = 0; x < pictures.length(); x++) {
                    JSONObject obj = (JSONObject) pictures.get(x);
                    String imageName = obj.getString("file_name");
                    int imgSuccess = obj.getInt("success");
                    if (imgSuccess == 1) {
                        if (deleteFile(imgPath + imageName)) {
                            jobDB.deletePicturesByPath(imgPath + imageName);
                            count++;
                        }
                    }
                }
                commonFunction.showToast(count + " images uploaded");
            } else {
                commonFunction.showToast("No images were uploaded.");
            }
        } catch (JSONException e) {
            Log.e("JobDetail", e.getMessage());
            commonFunction.showToast("An error has occurred");
        }
    }

    @Override
    public void onProgress(int progress) {
        commonFunction.showProgress(progress);
    }

    @Override
    public void onProgress(long uploadedBytes, long totalBytes) {

    }

    @Override
    public void onError(Exception exception) {

    }

    @Override
    public void onCompleted(int serverResponseCode, byte[] serverResponseBody) {
        commonFunction.dismissProgressDialog();
        try {
            String response = new String(serverResponseBody, "UTF-8");
            uploadResponse(response);
        } catch (UnsupportedEncodingException e) {
            Log.e("DeliveryDetail", "UnsupportedEncodingException");
        }
    }

    @Override
    public void onCancelled() {

    }

    //Complete
    public void postRequest(String param) throws IOException {

        commonFunction.setDialog(true);

        OkHttpClient client = new OkHttpClient();
        //RequestBody body = RequestBody.create(JSON, param);
        RequestBody body = new FormBody.Builder()
                .add("deliveryDetail", param)
                .build();
        Request request = new Request.Builder()
                .url(STRUCMAC_UPLOAD_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            Handler handler = new Handler(DeliveryDetail.this.getMainLooper());

            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();
                commonFunction.cancelDialog();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        commonFunction.showToast("Error: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isSuccessful()) {
                            commonFunction.cancelDialog();
                            commonFunction.showToast("Unexpected error: " + response.message());
                        } else {
                            commonFunction.cancelDialog();
                            try {
                                displayResponse(response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

    }

    public void displayResponse(String response) {
        int success;
        String message;
        String jobId;
        try {
            JSONObject result = new JSONObject(response);
            success = result.getInt("success");
            message = result.getString("message");
            commonFunction.cancelDialog();
            if (success == 1) {
                jobId = result.getString("job_id");
                jobDB.deleteDeliveryById(jobId);
                refresh(message, DeliveryList.class);
            } else {
                commonFunction.showToast(message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            commonFunction.showToast("Error Occurred: "+e.getMessage());
        }
    }
    public void refresh(String message, Class destination) {
        commonFunction.showToast(message);
        Intent intent = new Intent(this, destination);
        startActivity(intent);
    }
}
