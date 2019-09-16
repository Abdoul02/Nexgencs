package com.fgtit.fingermap;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.models.SessionManager;
import com.fgtit.models.User;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class JobDetail extends AppCompatActivity {

    JobDB jdb = new JobDB(this);
    DBHandler db = new DBHandler(this);
    TextView jobName,location,description,customer,approved,start,end,jCode,down,ope,company,office;
    LinearLayout lloffice,llSatis,llTime;
    EditText comment,progress,startkm,endkm,edtTimeOnSite;
    Button update,signature;
    ProgressDialog prgDialog;
    private int count;
    private ArrayList<User> empList;
    String ba1;
    SessionManager session;
    HashMap<String, String> manager;
    int compId;


    private AsyncFingerprint vFingerprint;
    private Dialog fpDialog;
    ProgressDialog saveDialog;
    private int	iFinger=0;
    private ImageView fpImage,pict,camera,imgSmile,imgNeutral,imgSad;
    private TextView  tvFpStatus;
    private boolean bcheck=false;
    private boolean	bIsUpImage=true;
    private boolean	bIsCancel=false;
    private boolean	bfpWork=false;
    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private byte[] jpgbytes=null;
    int clientSatisfaction=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

        session = new SessionManager(getApplicationContext());
        manager = session.getUserDetails();
        compId = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));
        jobName  = (TextView) findViewById(R.id.job_name);
        location = (TextView) findViewById(R.id.location);
        description = (TextView) findViewById(R.id.description);
        customer = (TextView) findViewById(R.id.customer);
        approved = (TextView) findViewById(R.id.approvedBy);
        start = (TextView) findViewById(R.id.start);
        end = (TextView) findViewById(R.id.end_date);
        jCode = (TextView) findViewById(R.id.Job_code);
        down = (TextView) findViewById(R.id.txtDownl);
        ope =(TextView) findViewById(R.id.txtOpen);
        comment = (EditText)findViewById(R.id.edtComment);
        progress = (EditText)findViewById(R.id.edtProgress);
        startkm = (EditText)findViewById(R.id.edtStart);
        edtTimeOnSite = (EditText)findViewById(R.id.edtTime);
        endkm = (EditText)findViewById(R.id.edtFinish);
        update = (Button) findViewById(R.id.btnUpd);
        signature = (Button)findViewById(R.id.btnSign);
        pict = (ImageView) findViewById(R.id.imgPict);
        camera = (ImageView) findViewById(R.id.imgCam);
        company = (TextView) findViewById(R.id.cust);
        lloffice = (LinearLayout) findViewById(R.id.llOffice);
        llTime = (LinearLayout) findViewById(R.id.llTimeOnSite);
        llSatis = (LinearLayout) findViewById(R.id.llClientSat);
        office = (TextView) findViewById(R.id.office);
        imgSmile = (ImageView) findViewById(R.id.imgSmile);
        imgNeutral = (ImageView) findViewById(R.id.imgNeutral);
        imgSad = (ImageView) findViewById(R.id.imgSad);

       // signature.setClickable(false);
       // signature.setVisibility(View.GONE);

        if(compId == 63 || compId ==3){

            signature.setClickable(true);
            signature.setVisibility(View.VISIBLE);
            lloffice.setVisibility(View.VISIBLE);
            llSatis.setVisibility(View.VISIBLE);
            llTime.setVisibility(View.VISIBLE);
            company.setText("Company");

        }

        empList = db.getAllUsers();

        //Progress Dialog
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Updating Job Card. Please wait...");
        prgDialog.setCancelable(false);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String value = extras.getString("jobCode");
            if (value != null) {

                Cursor rs = jdb.getData(value);
                rs.moveToFirst();

                final String jname,jloc,jdesc,jcust,japp,jstart,jend,code,prog,link,sOffice;

                jname   = rs.getString(rs.getColumnIndex("name"));
                jloc    = rs.getString(rs.getColumnIndex("location"));
                jdesc   = rs.getString(rs.getColumnIndex("description"));
                jcust   = rs.getString(rs.getColumnIndex("customer"));
                japp    = rs.getString(rs.getColumnIndex("approvedBy"));
                jstart  = rs.getString(rs.getColumnIndex("start"));
                jend    = rs.getString(rs.getColumnIndex("end"));
                code    = rs.getString(rs.getColumnIndex("jobCode"));
                prog    = rs.getString(rs.getColumnIndex("progress"));
                link    = rs.getString(rs.getColumnIndex("attachment"));
                sOffice  = rs.getString(rs.getColumnIndex("office"));

                jobName.setText(jname);
                location.setText(jloc);
                description.setText(jdesc);
                customer.setText(jcust);
                approved.setText(japp);
                start.setText(jstart);
                end.setText(jend);
                jCode.setText(code);
                office.setText(sOffice);
                progress.setText(String.valueOf(prog));


                down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (link.length() > 1) {

                            int indexOfString = link.indexOf("Attachments");
                            int nxtSla = link.indexOf("/", indexOfString) + 1;
                            String value = link.substring(nxtSla);
                            File test = Environment.getExternalStorageDirectory();
                            File myFile = new File(test.getAbsolutePath() + "/attach/" + value);

                            if(myFile.exists()){

                                open(value);
                            }else{
                                //Toast.makeText(getApplicationContext(), "Downloading, please wait...", Toast.LENGTH_LONG).show();
                                download("http://nexgencs.co.za/nexgen"+link, value);
                            }

                        } else {

                            Toast.makeText(getApplicationContext(), "No attachment provided", Toast.LENGTH_SHORT).show();
                        }
                    }

                });

                ope.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (link.length() > 1) {
                            int indexOfString = link.indexOf("Attachments");
                            int nxtSla = link.indexOf("/", indexOfString) + 1;
                            String value = link.substring(nxtSla);

                            File test = Environment.getExternalStorageDirectory();
                            File myFile = new File(test.getAbsolutePath() + "/attach/" + value);

                            if(myFile.exists()){

                                open(value);
                            }

                            else{

                                Toast.makeText(getApplicationContext(),value + "  not found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String pictName = jCode.getText().toString() + "_" + progress.getText().toString();
                        Intent intent = new Intent(JobDetail.this, CameraExActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("id", pictName);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 0);
                    }
                });

                imgSmile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientSatisfaction = 1;
                        imgSmile.setImageResource(R.drawable.happy_green);
                        imgNeutral.setImageResource(R.drawable.neutral_grey);
                        imgSad.setImageResource(R.drawable.sad_grey);
                        Toast.makeText(JobDetail.this,"Satisfied",Toast.LENGTH_SHORT).show();
                    }
                });

                imgNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientSatisfaction = 2;
                        imgSmile.setImageResource(R.drawable.happy_grey);
                        imgNeutral.setImageResource(R.drawable.neutral_green);
                        imgSad.setImageResource(R.drawable.sad_grey);
                        Toast.makeText(JobDetail.this,"Neutral",Toast.LENGTH_SHORT).show();
                    }
                });

                imgSad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientSatisfaction = 3;
                        imgSmile.setImageResource(R.drawable.happy_grey);
                        imgNeutral.setImageResource(R.drawable.neutral_grey);
                        imgSad.setImageResource(R.drawable.sad_green);
                        Toast.makeText(JobDetail.this,"Not satisfied",Toast.LENGTH_SHORT).show();
                    }
                });



                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateandTime = sdf.format(new Date());
                        String com = comment.getText().toString().trim();
                        String pro = progress.getText().toString();
                        String startK = startkm.getText().toString();
                        String endK = endkm.getText().toString();
                        String timeOnSite = edtTimeOnSite.getText().toString();


                        if (pro.equals("100")) {

                            if(clientSatisfaction == 0 && compId == 63){
                                Toast.makeText(getApplicationContext(), "Please select client satisfaction and press finish", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "Please press finish", Toast.LENGTH_SHORT).show();
                            }


                        }

                        else {

                            if(Integer.parseInt(pro) > 100){

                                Toast.makeText(getApplicationContext(), "Progress cannot be over 100%", Toast.LENGTH_SHORT).show();
                            }

                            if (com.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Please provide a comment", Toast.LENGTH_SHORT).show();

                            } else {

                                File extf = Environment.getExternalStorageDirectory();
                                File myFile = new File(extf.getAbsolutePath()+ "/fgtit/"+ code +"_"+ pro + ".jpg");
                                if(myFile.exists()){

                                    String path ="/sdcard/fgtit/"+ code +"_"+ pro + ".jpg";
                                    String name = code +"_"+ pro + ".jpg";

                                    upload(path, name, com, currentDateandTime, Integer.parseInt(pro), code,startK,endK,clientSatisfaction,timeOnSite);
                                }

                                else{
                                    nopict(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code,startK,endK,clientSatisfaction,timeOnSite);
                                    //Toast.makeText(getApplicationContext(), "No Picture", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }

                    }
                });

                signature.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String code = jCode.getText().toString();
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("code", code);
                        dataBundle.putString("url", "http://www.nexgencs.co.za/api/signatures/sign.php");
                        Intent intent = new Intent(getApplicationContext(), Signature.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);


                    }
                });

            }
        }

        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        //FPProcess();
    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(resultCode){
            case 1:{
                Bundle bl= data.getExtras();
                String barcode=bl.getString("barcode");
            }
            break;
            case 2:
                break;
            case 3:{
                Bundle bl= data.getExtras();
                String id=bl.getString("id");
                Toast.makeText(JobDetail.this, "Pictures Finish", Toast.LENGTH_SHORT).show();
                byte[] photo=bl.getByteArray("photo");
                if(photo!=null){
                    try{
                        Matrix matrix = new Matrix();
                        Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);
                        matrix.preRotate(270);
                        Bitmap nbm=Bitmap.createBitmap(bm ,0,0, bm .getWidth(), bm .getHeight(),matrix,true);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        nbm.compress(Bitmap.CompressFormat.JPEG, 80, out);
                        jpgbytes= out.toByteArray();

                        Bitmap bitmap =BitmapFactory.decodeByteArray(jpgbytes, 0, jpgbytes.length);
                        pict.setImageBitmap(bitmap);

                    }catch(Exception e){
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void gotoMap(View v)
    {

        if(isAppInstalled("com.google.android.apps.maps")) {

            String address = location.getText().toString();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q="+address));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
        }
        else{

            Toast.makeText(getApplicationContext(),"You do not have google maps installed",Toast.LENGTH_SHORT).show();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.job_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                this.setResult(1);
                this.finish();
                workExit();
                return true;
            case R.id.finish:

                if(progress.getText().toString().equals("100")){

                    if(clientSatisfaction == 0 && compId == 63){
                        Toast.makeText(getApplicationContext(), "Please select client satisfaction", Toast.LENGTH_SHORT).show();
                    }else{

                        FPDialog(1);
                    }


                }
                else{
                    Toast.makeText(getApplicationContext(), "Progress must be 100%", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FPDialog(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(JobDetail.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(JobDetail.this);
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

    //Finger Print Registration
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
                    Toast.makeText(JobDetail.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                String com = comment.getText().toString().trim();
                String pro = progress.getText().toString();
                String code = jCode.getText().toString();
                String strt = startkm.getText().toString();
                String fnsh = endkm.getText().toString();
                String timeOnSite = edtTimeOnSite.getText().toString();
                File extf = Environment.getExternalStorageDirectory();
                File myFile = new File(extf.getAbsolutePath() + "/fgtit/" + code + "_" + pro + ".jpg");


                int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));


                //Check the fingerprints here
                List<User> user = empList;
                int count = 0;
                if (empList.size() > 0) {

                    //for(int i=0; i<emplist.size(); i++)
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if (us.getuId() == userID) {

                                    fpDialog.cancel();

                                    if (myFile.exists()) {
                                        String path = "/sdcard/fgtit/" + code + "_" + pro + ".jpg";
                                        String name = code + "_" + pro + ".jpg";
                                        upload(path, name, com, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh,clientSatisfaction,timeOnSite);

                                    } else {
                                        nopict(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, strt,fnsh,clientSatisfaction,timeOnSite);
                                        //Toast.makeText(getApplicationContext(), "No Picture", Toast.LENGTH_SHORT).show();
                                    }

                                    //Toast.makeText(getApplicationContext(), "Job Done " + us.getuName(), Toast.LENGTH_SHORT).show();
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                } else
                                    Toast.makeText(getApplicationContext(), "Wrong user", Toast.LENGTH_SHORT).show();
                            }
                        }


                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                if (us.getuId() == userID) {
                                    fpDialog.cancel();
                                    if (myFile.exists()) {
                                        String path = "/sdcard/fgtit/" + code + "_" + pro + ".jpg";
                                        String name = code + "_" + pro + ".jpg";
                                        upload(path, name, com, currentDateandTime, Integer.parseInt(pro), code, strt,fnsh,clientSatisfaction,timeOnSite);

                                    } else {
                                        nopict(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh,clientSatisfaction,timeOnSite);

                                        //Toast.makeText(getApplicationContext(), "No Picture", Toast.LENGTH_SHORT).show();
                                    }
                                    //Toast.makeText(getApplicationContext(), "Job Done " + us.getuName(), Toast.LENGTH_SHORT).show();
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;
                                } else
                                    Toast.makeText(getApplicationContext(), "Wrong user", Toast.LENGTH_SHORT).show();
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
                tvFpStatus.setText("Reading Failed");
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

    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), JobActivity.class);
        startActivity(objIntent);
    }


    public void upload(String path,String name,String com,String currentDateandTime,int pro,String code,String strt,String finish,int cs,String timeOnSite ){

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        // Upload image to server
        updateJob(name, com, currentDateandTime, currentDateandTime, pro, code, strt, finish,cs,timeOnSite);
       // new uploadToServer().execute(name);
    }

    public void updateJob(final String name,final String comment,final String dat,final String time,final int progres, final String job_code,final String strt,final String fnsh,final int cs,final String timeOnSite){

        try{

            //Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String json = "";

            //  build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("base64", ba1);
            jsonObject.accumulate("ImageName", name);
            jsonObject.accumulate("dat", dat);
            jsonObject.accumulate("jobCode", job_code);
            jsonObject.accumulate("comment", comment);
            jsonObject.accumulate("tim", time);
            jsonObject.accumulate("progress", progres);
            jsonObject.accumulate("start", strt);
            jsonObject.accumulate("finish", fnsh);
            jsonObject.accumulate("cs",cs);
            jsonObject.accumulate("timeOnSite",timeOnSite);


            json = jsonObject.toString();
            prgDialog.show();
            params.put("jCardJSON", json);
            client.setTimeout(7000);
            client.post("http://www.nexgencs.co.za/api/signatures/job.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                    System.out.println("+++++++++++++++++++++++++");
                    System.out.println(response);
                    System.out.println("+++++++++++++++++++++++++");
                    prgDialog.hide();
                    Toast.makeText(getApplicationContext(), "Job card updated", Toast.LENGTH_LONG).show();
                    if(Integer.parseInt(response) == 100){
                        jdb.deletejobcard(job_code);
                        jdb.deleteJinfo(job_code);
                        reloadActivity();
                    }else {
                        jdb.updateJob(job_code, progres);
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    // TODO Auto-generated method stub
                    System.out.println(content);
                    prgDialog.hide();
                    //controller.insertRecord(idN, name, dat, lat, lon, id, "IN");
                    jdb.insertJInfo(comment, time, dat, job_code,strt,fnsh);
                    //jdb.updateJob(job_code, progres);
                    Toast.makeText(getApplicationContext(), "Network error "+ String.valueOf(statusCode), Toast.LENGTH_LONG).show();

                }
            });


        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void nopict(final String comment,final String dat,final String time,final int progres, final String job_code,final String strt,final String fnsh,final int cs,final String timeOnSite){

        try{

            //Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String json = "";

            //  build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("date", dat);
            jsonObject.accumulate("jobCode", job_code);
            jsonObject.accumulate("comment", comment);
            jsonObject.accumulate("time", time);
            jsonObject.accumulate("progress", progres);
            jsonObject.accumulate("start", strt);
            jsonObject.accumulate("finish", fnsh);
            jsonObject.accumulate("cs",cs);
            jsonObject.accumulate("timeOnSite",timeOnSite);

            json = jsonObject.toString();
            prgDialog.show();

            params.put("jCardJSON", json);
            client.setTimeout(5000);
            client.post("http://www.nexgencs.co.za/api/insertJob.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    /*System.out.println("+++++++++++++++++++++++++");
                    System.out.println(response);
                    System.out.println("+++++++++++++++++++++++++");*/
                    prgDialog.hide();
                    Toast.makeText(getApplicationContext(), "Job card updated", Toast.LENGTH_LONG).show();
                    if(Integer.parseInt(response) == 100100){
                        jdb.deletejobcard(job_code);
                        jdb.deleteJinfo(job_code);
                        reloadActivity();
                    }else {
                        jdb.updateJob(job_code, progres);
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    // TODO Auto-generated method stub
                    System.out.println(content);
                    prgDialog.hide();
                    //controller.insertRecord(idN, name, dat, lat, lon, id, "IN");
                    jdb.insertJInfo(comment,time,dat,job_code,strt,fnsh);
                    Toast.makeText(getApplicationContext(), "Network error, please check your internet connection.", Toast.LENGTH_LONG).show();

                }
            });


        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }


    public void open(String nam)
    {
        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/attach/" + nam);  // -> filename = maven.pdf
        Uri path = Uri.fromFile(pdfFile);
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try{
            startActivity(pdfIntent);
        }catch(ActivityNotFoundException e){
            Toast.makeText(JobDetail.this, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }

    public void download(String url, String nam)
    {
        new DownloadFile().execute(url);
    }
    private class DownloadFile extends AsyncTask<String, Integer, String> {
         ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(JobDetail.this);
            pDialog.setMessage("Downloading Attachment...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(pDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... Url) {
            try {


                URL url = new URL(Url[0]);
                // String fileName = Url[1];
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // URLConnection connection = url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                String val = String.valueOf(url);
                int indexOfString = val.indexOf("Attachments");
                int nxtSla = val.indexOf("/", indexOfString) + 1;
                String value = val.substring(nxtSla);
                String fileName = value;

                // Detect the file lenghth
                int fileLength = connection.getContentLength();

                // Locate storage location

                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                File folder = new File(extStorageDirectory, "attach");
                folder.mkdir();

                /*String filepath = Environment.getExternalStorageDirectory()
                        .getPath();*/

                // Download the file
                InputStream input = new BufferedInputStream(url.openStream());

                // Save the downloaded file
                OutputStream output = new FileOutputStream(folder + "/"
                        + fileName);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                // Close connection
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                // Error Log
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // super.onProgressUpdate(progress);
            // Update the progress dialog
            pDialog.setProgress(progress[0]);
            // Dismiss the progress dialog
            //mProgressDialog.dismiss();
        }

        protected void onPostExecute(String file_url) {

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
                Toast.makeText(JobDetail.this, "Download complete, Please tap open to view", Toast.LENGTH_SHORT).show();
            }
        }


    }


}
