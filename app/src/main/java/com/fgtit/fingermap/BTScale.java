package com.fgtit.fingermap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.data.ImageSimpleAdapter;
import com.fgtit.data.RecordItem;
import com.fgtit.device.BluetoothReaderService;
import com.fgtit.entities.SessionManager;
import com.fgtit.entities.User;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BTScale extends AppCompatActivity {

    //DB
    DBHandler mydb = new DBHandler(this);
    JobDB jobDB = new JobDB(this);
    SessionManager session;
    //Fingerprint
    private AsyncFingerprint vFingerprint;

    private boolean			 bIsCancel=false;
    private boolean			 bfpWork=false;
    private Dialog fpDialog;
    private TextView tvFpStatus;
    private boolean bcheck=false;
    private boolean	bIsUpImage=true;
    private int	iFinger=0;
    private int count;
    private ArrayList<User> empList;

    //From SignON
    private ListView listView1;
    private ListView listView2;
    private ArrayList<HashMap<String, Object>> mData1;
    private SimpleAdapter adapter1;
    private TextView txtWeight;

    private ArrayAdapter<String> mListArrayAdapter2;

    private ImageView fpImage;

    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;

    //Bluetooth
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothReaderService mChatService = null;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private BluetoothSocket socket;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String DEVICE_UUID = "device uuid";
    private static final String TAG = "BTScale";
    private BluetoothDevice device;
    private boolean mIsWork=false;
    ProgressDialog prgDialog;

    Button btnWeigt;

    int responseCode;
    String serverURL = "http://www.nexgencs.co.za/alos/bt_scale.php";
    String infoURL = "http://nexgencs.co.za/alos/get_info.php";

    Spinner spn_cost_center,spn_product;
    String cost_center_prompt = "--Cost center--";
    String product_prompt = "--Product--";
    Dialog dialog;
    int company_id;
    String db_user_id,db_status,db_weight,db_date,db_time,db_imei;
    int db_cost_center_id,db_product_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btscale);

        empList = mydb.getAllUsers();
        mChatService = new BluetoothReaderService(this, mHandler);	// Initialize the BluetoothChatService to perform bluetooth connections


        txtWeight = (TextView) findViewById(R.id.txtWeight);
        listView1=(ListView) findViewById(R.id.listView1);
        mData1 = new ArrayList<HashMap<String, Object>>();
        adapter1 = new ImageSimpleAdapter(this,mData1,R.layout.listview_signitem,
                new String[]{"title","info","dts","img"},
                new int[]{R.id.title,R.id.info,R.id.dts,R.id.img});
        listView1.setAdapter(adapter1);


        mListArrayAdapter2 = new ArrayAdapter<String>(this, R.layout.list_item);
        listView2 = (ListView) findViewById(R.id.listView2);
        listView2.setAdapter(mListArrayAdapter2);

        tvFpStatus = (TextView)findViewById(R.id.textView1);
        tvFpStatus.setText("");

        btnWeigt = (Button) findViewById(R.id.btnGetWeight);

        fpImage = (ImageView)findViewById(R.id.imageView1);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Uploading scale information...");
        prgDialog.setCancelable(false);

        spn_cost_center = (Spinner) findViewById(R.id.costCenterSpinner);
        spn_product = (Spinner) findViewById(R.id.productSpinner);

        //cost center
        initAllSpinner(this,cost_center_prompt,mydb.getAllCostCenter(),spn_cost_center);
        //Product
        initAllSpinner(this,product_prompt,mydb.getAllProduct(),spn_product);

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> manager = session.getUserDetails();
        company_id = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));

        //jobDB.insert_bt_scale("12793","13.45","2018-11-07 09:04:23","2018-11-07 09:04:23","868646030046123","ST,GS",2,311);


        //Fingerprint
        AddStatus("Disconnected");
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
            if (mChatService != null) mChatService.stop();
            //this.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null)Toast.makeText(this, "Starting session", Toast.LENGTH_SHORT).show(); //setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();

        try{
            unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bt_scale, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){

            case R.id.download:
                try {
                    postRequest("Whatever",String.valueOf(company_id));
                }catch (IOException e){
                    e.printStackTrace();
                }
                return true;
            case R.id.action_screen:
                mData1.clear();
                adapter1.notifyDataSetChanged();
                mListArrayAdapter2.clear();
                break;
            case R.id.action_view:
                Toast.makeText(getApplicationContext(),"Record",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), RecordList.class);
                startActivity(intent);
                return true;

            case R.id.action_scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            //bIsCancel=true;
            //CloseReadCard();
            //SerialPortManager.getInstance().closeSerialPort();
            workExit();
            Intent objIntent = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(objIntent);
            //this.finish();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void ensureDiscoverable() {
        //if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void initAllSpinner(Context context,String prom,List<String> dbValue,Spinner spinner){

        List<String> loadList = new ArrayList<>();
        loadList.add(prom);
        loadList.addAll(dbValue);

        CustomSpinnerAdapter customSpinnerAdapter=new CustomSpinnerAdapter(context,loadList);
        spinner.setAdapter(customSpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //val = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public Object postRequest(String param,String company_id) throws IOException{

        setDialog(true);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("BT_SCALE",param)
                .add("company_id",company_id)
                .build();
        Request request = new Request.Builder()
                .url(infoURL)
                .post(body)
                .build();
        //Log.d("PARAM:+++",param[0]+ " "+param[1]);

        client.newCall(request).enqueue(new Callback() {

            Handler handler = new Handler(BTScale.this.getMainLooper());
            @Override
            public void onFailure(Call call, final IOException e) {
                call.cancel();
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error: "+e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(!response.isSuccessful()){

                            if(dialog != null && dialog.isShowing()){
                                dialog.dismiss();
                            }
                            showToast("Unexpected error: "+response.message());

                        }else{

                            try {

                                updateSQLite(response.body().string());
                                if(dialog != null && dialog.isShowing()){
                                    dialog.dismiss();
                                }
                            }catch (IOException e){
                                e.printStackTrace();
                            }

                        }


                    }
                });
            }
        });

        return null;
    }

    public void updateSQLite(String response){


        Log.e("RESULT+++++++++:",response);
        int success = 0;
        String message = "";
        try {
            JSONObject object = new JSONObject(response);

            success = object.getInt("success");
            message = object.getString("message");

            if(success == 1){

                JSONArray productArray = object.getJSONArray("product");
                JSONArray costCenterArray = object.getJSONArray("cost_center");

                if(productArray.length() > 0 ){
                    mydb.deleteProduct();
                    for(int i=0; i<productArray.length(); i++){

                        JSONObject obj = (JSONObject) productArray.get(i);
                        String code = obj.getString("code");
                        int product_id = obj.getInt("product_id");
                        String product_name = obj.getString("product_name");
                        mydb.insertProduct(product_id,product_name,code);
                    }

                }


                if(costCenterArray.length() > 0 ){
                    mydb.deleteCostCenter();
                    for(int x=0; x<costCenterArray.length(); x++){

                        JSONObject obj = (JSONObject) costCenterArray.get(x);
                        String name = obj.getString("cost_center_name");
                        int cost_center_id = obj.getInt("cost_center_id");
                        mydb.insertCostCenter(cost_center_id,name);
                    }

                }

                showToast(message);
                reloadActivity();
            }else{

                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                showToast(message);
            }


        }catch (JSONException e){
            e.printStackTrace();
        }


    }
    private void setDialog(boolean show){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(BTScale.this);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if(show) {
            dialog.show();
        }else{
            dialog.cancel();
        }

    }

    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), BTScale.class);
        startActivity(objIntent);
    }



   /* public void getInfo(View view){

        String status,weight,fullText;
        fullText = txtWeight.getText().toString();
        weight = fullText.substring(fullText.lastIndexOf(" ")+1);
        status = fullText.substring(0,fullText.indexOf(" "));

        Toast.makeText(this,"Status: "+status +" Weight: "+weight,Toast.LENGTH_SHORT).show();
    }*/

   private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

       public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
        if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equalsIgnoreCase(action)){

            mListArrayAdapter2.clear();
            listView2.setBackgroundColor(getResources().getColor(R.color.red));
            AddStatus("Connection lost...");
            btnWeigt.setClickable(false);
            btnWeigt.setEnabled(false);
            if (mChatService != null) mChatService.stop();
        }
        if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equalsIgnoreCase(action)){

            mListArrayAdapter2.clear();
            listView2.setBackgroundColor(getResources().getColor(R.color.red));
            AddStatus("Disconnecting...");
            btnWeigt.setClickable(false);
            btnWeigt.setEnabled(false);
            if (mChatService != null) mChatService.stop();
        }
       }
   };

    public void getWeight(View view){

        txtWeight.setText("00.00KG");
        sendCom("R");
    }

    private void ReceiveCommand(byte[] databuf,int datasize) {

        try {
            String res = new String(databuf,"UTF-8");
            Log.i("RECEIVED++++++++++++", "VALUE: " + res);
            txtWeight.setText(res);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }


    }


        // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    //if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothReaderService.STATE_CONNECTED:
                            //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            mListArrayAdapter2.clear();
                            AddStatus("Connected");
                            listView2.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                            btnWeigt.setClickable(true);
                            btnWeigt.setEnabled(true);
                            IntentFilter connection = new IntentFilter();
                            connection.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                            connection.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                            getApplicationContext().registerReceiver(mBroadcastReceiver1,connection);
                            showToast("Receiver started");
                            break;
                        case BluetoothReaderService.STATE_CONNECTING:
                            //   mTitle.setText(R.string.title_connecting);
                            Toast.makeText(getApplicationContext(),"Connecting...",Toast.LENGTH_SHORT).show();
                            mListArrayAdapter2.clear();
                            AddStatus("Connecting...");
                            break;
                        case BluetoothReaderService.STATE_LISTEN:
                        case BluetoothReaderService.STATE_NONE:
                           showToast("State None");
                            break;
                        case BluetoothReaderService.STATE_LOSTCONNECT:
                            showToast("Lost Connection");
                            mListArrayAdapter2.clear();
                            listView2.setBackgroundColor(getResources().getColor(R.color.red));
                            AddStatus("Connection lost...");
                            btnWeigt.setClickable(false);
                            btnWeigt.setEnabled(false);
                            break;

                        case BluetoothReaderService.STATE_UNCONNECT:
                            showToast("Unable to connect, please turn off and turn on the scale");
                            mListArrayAdapter2.clear();
                            AddStatus("Please restart scale...");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                   // ReceiveCommand(readBuf,msg.arg1);

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    /*Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();*/
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT);
                    break;
            }
        }
    };

    private void FPProcess(){
        if(!bfpWork){
            try {
                Thread.currentThread();
                Thread.sleep(500);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            tvFpStatus.setText(getString(R.string.txt_fpplace));
            vFingerprint.FP_GetImage();
            bfpWork=true;
        }
    }

    private void FPInit(){

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
                    Toast.makeText(BTScale.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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

                List<User> user = empList;
                String costCenter = spn_cost_center.getSelectedItem().toString();
                String product = spn_product.getSelectedItem().toString();
                if(costCenter.equals(cost_center_prompt) || product.equals(product_prompt)){

                    showToast("Please select cost center or product");

                }else{

                    int count =0;
                    if (empList.size() > 0) {

                        //for(int i=0; i<emplist.size(); i++)
                        for (User us : user)
                        {

                            if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                                byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                                if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                    String string = "R";
                                    sendCom(string);
                                    AddPersonItem(us);
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    TimeOutStart();
                                    break;
                                }
                            }


                            if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                                byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                                if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                    String string = "R";
                                    sendCom(string);
                                    AddPersonItem(us);
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    TimeOutStart();
                                    break;
                                }
                            }

                            count ++;
                        }

                        if(count == empList.size()){
                            Toast.makeText(getApplicationContext(),"fingerprint not found",Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(getApplicationContext(),"Please download employee information",Toast.LENGTH_SHORT).show();
                }

                bfpWork=false;
                TimerStart();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText(getString(R.string.txt_fpmatchfail)+":-1");
                bfpWork=false;
                TimerStart();
            }
        });

    }

    public void TimeOutStart() {

        final Handler tOut = new Handler();
        tOut.postDelayed(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(getApplicationContext(), "Time out", Toast.LENGTH_SHORT).show();
                //sendCom("R");
            }
        }, 2000);
    }

    public void sendCom(String string){

        //TimeOutStart();

        mChatService.write(string.getBytes());

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

    private void AddPersonItem(User person){

        RecordItem rs = new RecordItem();


        String status,weight,fullText;
        fullText = txtWeight.getText().toString().trim();
        weight = fullText.substring(fullText.lastIndexOf(" ")+1);
        status = fullText.substring(0,fullText.indexOf(" "));


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        rs.id=person.getIdNum();
        rs.name=person.getuName();
        rs.datetime=currentDateandTime;
        rs.scaleStatus = status;
        rs.weight = weight.trim();

        String costCenter,product;
        costCenter = spn_cost_center.getSelectedItem().toString();
        product = spn_product.getSelectedItem().toString();

        String costCenter_id = String.valueOf(mydb.getCostCenterId(costCenter));
        String product_id = String.valueOf(mydb.getProductId(product));

        db_user_id = String.valueOf(person.getuId());
        db_cost_center_id = Integer.parseInt(costCenter_id);
        db_date = rs.datetime;
        db_time = rs.datetime;
        db_weight = rs.weight;
        db_status = rs.scaleStatus;
        db_product_id = Integer.parseInt(product_id);
           new uploadScale().execute(String.valueOf(person.getuId()),rs.scaleStatus,rs.weight,rs.datetime,costCenter_id,product_id);

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("title", rs.name);
        map.put("info", txtWeight.getText().toString().trim());
        map.put("dts", rs.datetime);
        map.put("img", ExtApi.LoadBitmap(getResources(), R.drawable.guest));
        mData1.add(map);
        adapter1.notifyDataSetChanged();
        ScrollListViewToBottom();
    }

    private void ScrollListViewToBottom() {
        listView1.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView1.setSelection(adapter1.getCount() - 1);
            }
        });
    }

    private void AddStatus(String text){
        mListArrayAdapter2.add(text);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    Toast.makeText(this, "Starting session", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occured
                    //Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public class uploadScale extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){

            prgDialog.show();

        }

        protected String doInBackground(String... args) {


            try{


                final String identifier;
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null){ identifier = telephonyManager.getDeviceId();}
                else{ identifier = "Not available";}

                db_imei = identifier;

                URL url = new URL(serverURL);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("user_id", args[0]);
                postDataParams.put("status", args[1]);
                postDataParams.put("weight", args[2]);
                postDataParams.put("date", args[3]);
                postDataParams.put("time", args[3]);
                postDataParams.put("imei", identifier);
                postDataParams.put("cost_center_id", args[4]);
                postDataParams.put("product_id", args[5]);
               /* postDataParams.put("phone_on_leave", args[5]);
                postDataParams.put("email_on_leave", args[6]);
                postDataParams.put("user", args[7]);
                postDataParams.put("specific_leave_type", args[8]);*/
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept","application/json");
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

            int success = 0;
            String message = "";

            if(result != null){

                try{
                    JSONObject object = new JSONObject(result);

                success = object.getInt("success");
                message = object.getString("message");

                if(success == 1){

                    txtWeight.setText("00.00KG");
                    showToast(message);
                }else{

                    showToast(message);
                    jobDB.insert_bt_scale(db_user_id,db_weight,db_date,db_time,db_imei,db_status,db_product_id,db_cost_center_id);
                }

                }catch (JSONException e){
                    e.printStackTrace();
                    jobDB.insert_bt_scale(db_user_id,db_weight,db_date,db_time,db_imei,db_status,db_product_id,db_cost_center_id);

                }
                Log.e("RESULT+++++++++:",result);
                clearInfo();
            }else{
                showToast("Error, please ensure you have data connection");
                jobDB.insert_bt_scale(db_user_id,db_weight,db_date,db_time,db_imei,db_status,db_product_id,db_cost_center_id);

            }

            if (responseCode == 404) {
                showToast("Requested resource not found");

            } else if (responseCode == 500) {
                showToast("Something went wrong at server end");

            }

            clearInfo();
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

    public void showToast(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }


    public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        private final Context activity;
        private List<String> asr;

        public CustomSpinnerAdapter(Context context,List<String> asr) {
            this.asr=asr;
            activity = context;
        }



        public int getCount()
        {
            return asr.size();
        }

        public Object getItem(int i)
        {
            return asr.get(i);
        }

        public long getItemId(int i)
        {
            return (long)i;
        }



        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(BTScale.this);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(20);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setText(asr.get(position));
            txt.setTextColor(Color.parseColor("#000000"));
            return  txt;
        }

        public View getView(int i, View view, ViewGroup viewgroup) {
            TextView txt = new TextView(BTScale.this);
            txt.setGravity(Gravity.CENTER);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(20);
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.drop_down, 0);
            txt.setText(asr.get(i));
            txt.setTextColor(Color.parseColor("#000000"));
            return  txt;
        }

    }

    public void clearInfo(){

        final Handler tOut = new Handler();
        tOut.postDelayed(new Runnable() {
            @Override
            public void run() {

                mData1.clear();
                adapter1.notifyDataSetChanged();
               // mListArrayAdapter2.clear();

            }
        },2000);
    }
}
