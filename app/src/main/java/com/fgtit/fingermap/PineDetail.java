package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.models.User;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//URL Connection
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class PineDetail extends AppCompatActivity {

    EditText delNote,tag,diameter;
    Spinner pineStatus;
    HashMap<String, String> queryValues;
    DBHandler mydb = new DBHandler(this);
    JobDB jobDB = new JobDB(this);


    private ProgressDialog pDialog;
    //http response code
    int responseCode;


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
    private ImageView fpImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pine_detail);

        delNote = (EditText) findViewById(R.id.edtDelivery);
        tag = (EditText) findViewById(R.id.edtTag);
        diameter = (EditText) findViewById(R.id.edtDiameter);
        //Get All the users for fingerprint
        empList = mydb.getAllUsers();

        pineStatus = (Spinner) findViewById(R.id.spnStatus);
        List<String> statusList = new ArrayList<String>();
        statusList.add("Good");
        statusList.add("No Tags");
        statusList.add("Reject Under-size");
        statusList.add("Reject Quality");
        statusList.add("No Tag Reject");
        statusList.add("No Tag Quality");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,statusList);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        pineStatus.setAdapter(dataAdapter);
        Bundle extras = getIntent().getExtras();

        if(extras !=null){

            String value = extras.getString("delivery");

            delNote.setText(value);
            delNote.setClickable(false);
            delNote.setEnabled(false);

        }


        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();


    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.pine_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.upload) {

            if(delNote.length() > 0){

                if(mydb.checkRemainingTag(delNote.getText().toString()) > 0){

                    //Call Fingerprint for uploading.
                    FPDialog(1);
                }else
                    Toast.makeText(this,"This Delivery note has 0 tag saved",Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(this,"Please enter a delivery note",Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            workExit();
            exitApplication();
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
    
    /*
    * Everything Fingerprint related
    * */
    private void FPDialog(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(PineDetail.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(PineDetail.this);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = (ImageView) vl.findViewById(R.id.imageView1);
        tvFpStatus= (TextView) vl.findViewById(R.id.textview1);
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
                    Toast.makeText(PineDetail.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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

                //Check the fingerprints here
                List<User> user = empList;
                int count = 0;
                if (empList.size() > 0) {
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if(us.getuId() == jobDB.getPineUser(delNote.getText().toString())){

                                    //Upload here
                                    fpDialog.cancel();
                                    uploadPine(us.getuId());
                                }else{

                                    Toast.makeText(getApplicationContext(), "Only the creator of the delivery note can upload it", Toast.LENGTH_SHORT).show();
                                }

                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;

                            }
                        }


                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if(us.getuId() == jobDB.getPineUser(delNote.getText().toString())){

                                    //Upload here
                                    fpDialog.cancel();
                                    uploadPine(us.getuId());
                                }else{

                                    Toast.makeText(getApplicationContext(), "Only the creator of the delivery note can upload it", Toast.LENGTH_SHORT).show();
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
                tvFpStatus.setText("Registration Failed");
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

    public void addPine(View v){

        if(delNote.length() >0 && diameter.length() > 0){

            String deliveryNote, pineTag, pineDiameter, status;
            deliveryNote = delNote.getText().toString();
            pineDiameter = diameter.getText().toString();
            status = String.valueOf(pineStatus.getSelectedItem());
            queryValues = new HashMap<String, String>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

            if(tag.length() > 0 ){

                if(status.equals("No Tag Reject") || status.equals("No Tags")){

                    Toast.makeText(this,"This status cannot be selected because you have a tag",Toast.LENGTH_SHORT).show();
                }else{

                    pineTag = tag.getText().toString();
                    if(mydb.checkTag(pineTag,deliveryNote) > 0){ //Check if Tag Exist in DB already
                        Toast.makeText(this,"Sorry this tag already exist",Toast.LENGTH_SHORT).show();
                    }else{

                        //Add to MySQLite
                        queryValues.put("deliveryN", deliveryNote);
                        queryValues.put("tag", pineTag);
                        queryValues.put("status", status);
                        queryValues.put("diameter", pineDiameter);
                        queryValues.put("date", currentDateandTime);
                        mydb.insertPine(queryValues);
                        Toast.makeText(this,"Tag added successfully",Toast.LENGTH_SHORT).show();
                        refresh();
                    }
                }

            }

            else{
                pineTag = "No Tag";
                //Add to MySQLite

                //Add to MySQLite
                queryValues.put("deliveryN", deliveryNote);
                queryValues.put("tag", pineTag);
                queryValues.put("status", status);
                queryValues.put("diameter", pineDiameter);
                queryValues.put("date", currentDateandTime);
                mydb.insertPine(queryValues);
                Toast.makeText(this,"Tag added successfully",Toast.LENGTH_SHORT).show();
                refresh();
            }

        }else{

            Toast.makeText(this,"Please provide delivery Note and Diameter",Toast.LENGTH_SHORT).show();
        }


    }

    public void refresh(){

        delNote.setClickable(false);
        delNote.setEnabled(false);

        diameter.setText("");
        tag.setText("");
        pineStatus.setSelection(0);


    }

    public void uploadPine(int id){


         String delivery =  delNote.getText().toString();
         new SendTags().execute(mydb.pineJSON(delivery),String.valueOf(id));

    }

    public class SendTags extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){

            pDialog = new ProgressDialog(PineDetail.this);
            pDialog.setMessage("Uploading data please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        protected String doInBackground(String... args) {


            try{

                URL url = new URL("http://nexgencs.co.za/api/insertPine.php");
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("pineJSON", args[0]);
                postDataParams.put("id", args[1]);
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
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
                    return new String("false : "+responseCode);
                }


            }catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }


        @Override
        protected void onPostExecute(String result) {

            int pineId;
            String status;

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if(result != null){
                try{
                    JSONArray arr = new JSONArray(result);

                    if(arr.length() != 0) {

                        for (int i = 0; i < arr.length(); i++) {

                            JSONObject obj = (JSONObject) arr.get(i);
                            pineId = obj.getInt("pineId");
                            status = obj.getString("inserted");
                            // response = obj.getString("answer");

                            if(status.equals("true")){

                                //delete one by one here
                                mydb.deletePine(pineId);
                            }
                            //Toast.makeText(getApplicationContext(),status +" "+String.valueOf(questionId),Toast.LENGTH_SHORT).show();

                        }
                        int remaining = mydb.checkRemainingTag(delNote.getText().toString());
                        if(remaining > 0){
                            Toast.makeText(getApplicationContext(),String.valueOf(remaining)+" Tags remaining, please upload again",Toast.LENGTH_SHORT).show();
                            Log.e("RESULT++++++:",result);
                        }else{
                        Toast.makeText(getApplicationContext(),"All tags successfully uploaded",Toast.LENGTH_SHORT).show();
                        Log.e("RESULT++++++:",result);
                        jobDB.deletePine(delNote.getText().toString());
                        reloadActivity();
                        }
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                    //  Log.e("RESULT++++++:",result);

                    if (responseCode == 404) {
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    } else if (responseCode == 500) {
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), result,
                                Toast.LENGTH_LONG).show();
                    }
                    reloadActivity();
                }
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

    public void reloadActivity(){

        Intent objIntent = new Intent(getApplicationContext(), PinneReport.class);
        startActivity(objIntent);
    }
}
