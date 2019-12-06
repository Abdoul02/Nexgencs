package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.models.User;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class PineCapture extends AppCompatActivity {

    private Spinner loadTypeSpinner, vehicleLoadSpinner;
    String selectLoad = "Select load type";
    String selectVehicleLoad = "Select load vehicle type";
    EditText edtTallyId,edtBill,edtSupervisor,edtDateReceived,edtCrosscut,edtDrDeliveryN,edtSupplier,
    edtSupplierDN,edtPlantation,edtVehicleReg,edtCompartment,edtPlacardNo;
    String serverURL = "http://nexgencs.co.za/api/createPine.php";
    String userId,dRDeliveryNote,supplierDn,currentDateandTime;
    int responseCode;
    ProgressDialog prgDialog;
    Calendar dateR;

    //Fingerprint
    private AsyncFingerprint vFingerprint;
    private Dialog fpDialog;
    private TextView tvFpStatus;
    private boolean bcheck=false;
    private boolean	bIsUpImage=true;
    private boolean	bIsCancel=false;
    private boolean	bfpWork=false;
    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private int	iFinger=0;
    private int count;
    private ArrayList<User> empList;
    private ImageView camBtn,pImage,fpImage;
    DBHandler mydb = new DBHandler(this);
    JobDB jDB = new JobDB(this);

    LayoutInflater inflater;
    View vl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pine_capture);
        initViews();
        initSpinner();

    }

    private void initSpinner(){

        List<String> loadList = new ArrayList<String>();
        loadList.add(selectLoad);
        loadList.add("B&F");
        loadList.add("SAW");

        List<String> vehicleLoadList = new ArrayList<String>();
        vehicleLoadList.add(selectVehicleLoad);
        vehicleLoadList.add("SD");
        vehicleLoadList.add("DD");
        vehicleLoadList.add("TR");
        vehicleLoadList.add("TT");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,loadList);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        loadTypeSpinner.setAdapter(dataAdapter);

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,vehicleLoadList);
        vehicleAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        vehicleLoadSpinner.setAdapter(vehicleAdapter);
    }

    private void initViews(){


        inflater = LayoutInflater.from(PineCapture.this);
        vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = (ImageView) vl.findViewById(R.id.imageView1);
        tvFpStatus= (TextView) vl.findViewById(R.id.textview1);

        loadTypeSpinner = (Spinner) findViewById(R.id.spnLoadType);
        vehicleLoadSpinner = (Spinner) findViewById(R.id.spnVehicleLoad);
        edtTallyId = (EditText) findViewById(R.id.edtTallyID);
        edtBill = (EditText) findViewById(R.id.edtBill);
        edtSupervisor = (EditText) findViewById(R.id.edtSupervisor);
        edtDateReceived = (EditText) findViewById(R.id.edtDateReceived);
        edtCrosscut = (EditText) findViewById(R.id.edtCrosscut);
        edtDrDeliveryN = (EditText) findViewById(R.id.edtDrDeliveryNote);
        edtSupplier = (EditText) findViewById(R.id.edtSupplier);
        edtSupplierDN = (EditText) findViewById(R.id.edtSupplierDN);
        edtPlantation = (EditText) findViewById(R.id.edtPlantation);
        edtVehicleReg = (EditText) findViewById(R.id.edtVehicleReg);
        edtCompartment = (EditText) findViewById(R.id.edtCompartment);
        edtPlacardNo = (EditText) findViewById(R.id.edtPlacardNo);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Creating delivery note...");
        prgDialog.setCancelable(false);

        empList = mydb.getAllUsers();

        dateR =  Calendar.getInstance(); //dateReceived
        final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                dateR.set(Calendar.YEAR, year);
                dateR.set(Calendar.MONTH, monthOfYear);
                dateR.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateRequired();
            }

        };

        edtDateReceived.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(PineCapture.this, datePicker, dateR
                        .get(Calendar.YEAR), dateR.get(Calendar.MONTH),
                        dateR.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();
    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
            this.finish();
        }
    }

    private void updateRequired() {

        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        edtDateReceived.setText(sdf.format(dateR.getTime()));
    }

    public void createPine(View view){

        if(submitForm()){

            //Create the delivery note
            FPDialog(1);
        }
    }

    public void showToast(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }


    private boolean submitForm() {
        if (!validateTallyId()) {
            return false;
        }

        if (!validateBill()) {
            return false;
        }

        if (!validateSupervisor()) {
            return false;
        }

        if (!validateDate()) {
            return false;
        }

        if (!validateCrossCut()) {
            return false;
        }

        if(!validateDRDeliveryN()){
            return false;
        }

        if(!validateSupplier()){
            return false;
        }

        if(!validateSupplierDN()){
            return false;
        }

        if(!validatePlantation()){
            return false;
        }

        if(!validateVehicleReg()){
            return false;
        }

        if(!validateCompartment()){
            return false;
        }

        if(!validatePlacardNo()){
            return false;
        }

        if(!validateLoadType()){
            return false;
        }

        if(!validateVehicleLoad()){
            return false;
        }
        return true;
    }
    private boolean validateTallyId() {
        if (edtTallyId.getText().toString().trim().isEmpty()) {
            showToast("Please enter ID");
            requestFocus(edtTallyId);
            return false;
        }
        return true;
    }
    private boolean validateBill() {
        String bill = edtBill.getText().toString().trim();

        if (bill.isEmpty() )  {
            showToast("Please enter Bill Month");
            requestFocus(edtBill);
            return false;
        }

        return true;
    }
    private boolean validateSupervisor() {
        if (edtSupervisor.getText().toString().trim().isEmpty()) {
            showToast("Please enter Supervisor name");
            requestFocus(edtSupervisor);
            return false;
        }

        return true;
    }
    private boolean validateDate(){
        if (edtDateReceived.getText().toString().trim().isEmpty()) {
            showToast("Please Select date received");
            requestFocus(edtDateReceived);
            return false;
        }
        return true;
    }
    private boolean validateCrossCut(){

        if (edtCrosscut.getText().toString().trim().isEmpty()) {
            showToast("Please enter crosscut");
            requestFocus(edtCrosscut);
            return false;
        }

        return true;
    }
    private boolean validateDRDeliveryN(){

        if (edtDrDeliveryN.getText().toString().trim().isEmpty()) {
            showToast("Please enter DR delivery note");
            requestFocus(edtDrDeliveryN);
            return false;
        }
        return true;
    }
    private boolean validateSupplier(){

        if (edtSupplier.getText().toString().trim().isEmpty()) {
            showToast("Please enter Supplier");
            requestFocus(edtSupplier);
            return false;
        }
        return true;
    }
    private boolean validateSupplierDN(){

        if (edtSupplierDN.getText().toString().trim().isEmpty()) {
            showToast("Please enter supplier delivery note");
            requestFocus(edtSupplierDN);
            return false;
        }
        return true;
    }
    private boolean validatePlantation(){

        if (edtPlantation.getText().toString().trim().isEmpty()) {
            showToast("Please enter Plantation");
            requestFocus(edtPlantation);
            return false;
        }
        return true;
    }
    private boolean validateVehicleReg(){

        if (edtVehicleReg.getText().toString().trim().isEmpty()) {
            showToast("Please enter Vehicle registration");
            requestFocus(edtVehicleReg);
            return false;
        }
        return true;
    }
    private boolean validateCompartment(){

        if (edtCompartment.getText().toString().trim().isEmpty()) {
            showToast("Please enter compartment");
            requestFocus(edtCompartment);
            return false;
        }
        return true;
    }
    private boolean validatePlacardNo(){

        if (edtPlacardNo.getText().toString().trim().isEmpty()) {
            showToast("Please enter placard number");
            requestFocus(edtPlacardNo);
            return false;
        }
        return true;
    }
    private boolean validateLoadType(){
        if(loadTypeSpinner.equals(selectLoad)){
            showToast("Please Select a load type");
            return false;
        }
        return true;
    }
    private boolean validateVehicleLoad(){
        if(vehicleLoadSpinner.equals(selectVehicleLoad)){
            showToast("Select load vehicle type");
            return false;
        }
        return true;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            exitApplication();
            workExit();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApplication(){
        Intent intent = new Intent(getApplicationContext(), PinneReport.class);
        startActivity(intent);
    }

    //Fingerprint related
    private void FPDialog(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(PineCapture.this);
        builder.setTitle("fingerprint Reader ");
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

       // FPProcess();
    }

    private void FPProcess(){

        if(!bfpWork){
            tvFpStatus.setText(getString(R.string.txt_fpplace));
            try {
                Thread.currentThread();
                Thread.sleep(500);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            //imgFeed.setImageResource(R.drawable.green_trans);

            vFingerprint.FP_GetImage();
            bfpWork=true;
        }
    }

    private void FPInit(){
        //ָ�ƴ���
        vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener()  {
            @Override
            public void onGetImageSuccess() {
                if(bIsUpImage){
                    vFingerprint.FP_UpImage();
                    tvFpStatus.setText(getString(R.string.txt_fpdisplay));
                }else{
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
                    Toast.makeText(PineCapture.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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

        //Send Information after clocking

        vFingerprint.setOnUpCharListener(new AsyncFingerprint.OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {

                String tallyId,billMonth,supervisor,dateReceived,crosscut,
                        supplier,plantation,vehicleReg,compartment,placardNo,
                        loadType,vehicleLoad;

                tallyId = edtTallyId.getText().toString();
                billMonth = edtBill.getText().toString();
                supervisor = edtSupervisor.getText().toString();
                dateReceived = edtDateReceived.getText().toString();
                crosscut = edtCrosscut.getText().toString();
                dRDeliveryNote = edtDrDeliveryN.getText().toString();
                supplier = edtSupplier.getText().toString();
                supplierDn = edtSupplierDN.getText().toString();
                plantation = edtPlantation.getText().toString();
                vehicleReg = edtVehicleReg.getText().toString();
                compartment = edtCompartment.getText().toString();
                placardNo = edtPlacardNo.getText().toString();
                loadType = loadTypeSpinner.getSelectedItem().toString();
                vehicleLoad = vehicleLoadSpinner.getSelectedItem().toString();

                //Check the fingerprints here
                List<User> user = empList;
                int count = 0;
                int pineUserCount = 0;
                if (empList.size() > 0) {
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                //Upload here
                                userId = String.valueOf(us.getuId());
                                List <String>authorisedList = mydb.getPineUsers();
                                for(int x=0; x<authorisedList.size();x++){

                                    if(userId.equals(authorisedList.get(x))){
                                        fpDialog.cancel();
                                        new createDeliveryNote().execute(tallyId,billMonth,supervisor,dateReceived,crosscut,dRDeliveryNote,supplier,
                                                supplierDn,plantation,vehicleReg,compartment,placardNo,loadType,vehicleLoad,userId);
                                        break;
                                    }

                                    pineUserCount++;
                                }
                                if (pineUserCount == authorisedList.size()) {
                                    Toast.makeText(getApplicationContext(), "You are not on the authorized list", Toast.LENGTH_SHORT).show();
                                }

                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;

                            }
                        }


                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                //Upload here
                                userId = String.valueOf(us.getuId());
                                List <String>authorisedList = mydb.getPineUsers();
                                for(int x=0; x<authorisedList.size();x++){

                                    if(userId.equals(authorisedList.get(x))){
                                        fpDialog.cancel();
                                        new createDeliveryNote().execute(tallyId,billMonth,supervisor,dateReceived,crosscut,dRDeliveryNote,supplier,
                                                supplierDn,plantation,vehicleReg,compartment,placardNo,loadType,vehicleLoad,userId);
                                        break;
                                    }

                                    pineUserCount++;
                                }
                                if (pineUserCount == authorisedList.size()) {
                                    Toast.makeText(getApplicationContext(), "You are not on the authorized list", Toast.LENGTH_SHORT).show();
                                }

                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;
                            }
                        }

                        count++;
                    }

                    if (count == empList.size()) {
                        Toast.makeText(getApplicationContext(), "fingerprint not found", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getApplicationContext(), "Please download employee information", Toast.LENGTH_SHORT).show();
                bfpWork=false;
                TimerStart();

            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText("Failed to read fingerprint");
                bfpWork=false;
                TimerStart();
            }
        });

    }

    public void TimerStart(){
        if(startTimer==null){
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

    public void TimeStop(){
        if (startTimer!=null)
        {
            startTimer.cancel();
            startTimer = null;
            startTask.cancel();
            startTask=null;
        }
    }

    public class createDeliveryNote extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){

            prgDialog.show();

        }

        protected String doInBackground(String... args) {


            try{

                URL url = new URL(serverURL);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                currentDateandTime = sdf.format(new Date());
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("tallyId", args[0]);
                postDataParams.put("billMonth", args[1]);
                postDataParams.put("supervisor", args[2]);
                postDataParams.put("dateReceived", args[3]);
                postDataParams.put("crosscut", args[4]);
                postDataParams.put("DrDeliveryNote", args[5]);
                postDataParams.put("supplier", args[6]);
                postDataParams.put("supplierDN", args[7]);
                postDataParams.put("plantation", args[8]);
                postDataParams.put("vehicleReg", args[9]);
                postDataParams.put("compartment", args[10]);
                postDataParams.put("placardNo", args[11]);
                postDataParams.put("loadType", args[12]);
                postDataParams.put("vehicleLoad", args[13]);
                postDataParams.put("userId", args[14]);
                postDataParams.put("dateProcessed", currentDateandTime);
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept","application/json");
                //conn.setRequestProperty("Content-Type","application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();
                responseCode=conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();
                    //return new String("false : "+responseCode);
                }


            }catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }


        @Override
        protected void onPostExecute(String result) {

            if (prgDialog != null && prgDialog.isShowing()) {
                prgDialog.dismiss();
            }

            if(result != null){
                Log.e("RESULT+++++++++:",result);

                if(result.equals("success")){

                    showToast("Delivery note successfully created");
                    jDB.insertPine(userId,supplierDn,dRDeliveryNote,currentDateandTime);

                }else{

                    showToast("Error delivery note could not be created");
                }


            }else{
                showToast("Error, please ensure you have data connection");
            }

            if (responseCode == 404) {
                showToast("Requested resource not found");
            } else if (responseCode == 500) {
                showToast("Something went wrong at server end");
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

}
