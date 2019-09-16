package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.models.User;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class PinneReport extends AppCompatActivity {


    String delivery;
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
    DBHandler db = new DBHandler(this);
    JobDB jDB = new JobDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinne_report);

        //ArrayList<HashMap<String, String>> pineList = jDB.getAllPine();
        List <String> myPineList = new ArrayList<String>();
        empList = db.getAllUsers();
        myPineList.addAll(jDB.getPine());

        ArrayList<HashMap<String, String>> pineList = new ArrayList<>();

        final ListView deliveryList = (ListView)findViewById(R.id.pineList);

        if(myPineList.size() != 0){

            for(int i = 0; i< myPineList.size(); i++){

                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("deliveryNote",myPineList.get(i));
                hashMap.put("number",String.valueOf(db.checkRemainingTag(myPineList.get(i))) + " Tag(s) recorded");
                pineList.add(hashMap);
            }

            String [] from = {"deliveryNote","number"};
            int [] to = {R.id.deliveryNote,R.id.numberOfTag};

            ListAdapter adapter = new SimpleAdapter( PinneReport.this,pineList, R.layout.pine_entry, from,to);
            deliveryList.setAdapter(adapter);
        }


        deliveryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView c = (TextView) view.findViewById(R.id.deliveryNote);
                delivery = c.getText().toString();
                FPDialog(1);

                //String find = criticalA;

            }
        });

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

    //Fingerprint related
    private void FPDialog(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(PinneReport.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(PinneReport.this);
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
                //reloadActivity();
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
                    Toast.makeText(PinneReport.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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
        //Open Tag capture after clocking
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

                                if(us.getuId() == jDB.getPineUser(delivery)){
                                    fpDialog.cancel();
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putString("delivery", delivery);
                                    Intent intent = new Intent(getApplicationContext(), PineDetail.class);
                                    intent.putExtras(dataBundle);
                                    startActivity(intent);
                                }else{
                                    showToast("Only the creator of the delivery note can access it.");
                                    //reloadActivity();
                                }


                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;

                            }
                        }

                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if(us.getuId() == jDB.getPineUser(delivery)){
                                    fpDialog.cancel();
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putString("delivery", delivery);
                                    Intent intent = new Intent(getApplicationContext(), PineDetail.class);
                                    intent.putExtras(dataBundle);
                                    startActivity(intent);
                                }else{
                                    showToast("Only the creator of the delivery note can access it.");
                                    //reloadActivity();
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
                //fpDialog.cancel();
                bfpWork = false;
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

    public void showToast(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            exitApplication();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApplication(){
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pine, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {

            List <String> pineUserList = db.getPineUsers();
            if(pineUserList.size() !=0){
            Intent intent = new Intent(PinneReport.this, PineCapture.class);
            startActivity(intent);
            }
            else{
               showToast("Please go to data Upload/download to get pine users");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void reloadActivity(){

        Intent objIntent = new Intent(getApplicationContext(), PinneReport.class);
        startActivity(objIntent);
    }


}

