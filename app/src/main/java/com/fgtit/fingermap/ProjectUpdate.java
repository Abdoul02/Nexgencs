package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class ProjectUpdate extends AppCompatActivity {

    EditText location,asset,requestedBy,criticalAsset,dateRequired,workRequired,site,trade,date,dt,progress,comment;
    private ImageView camBtn,pImage,fpImage;
    private Spinner statusSpinner;
    ProgressDialog prgDialog;
    Calendar myCal,mycal2;
    String pictName;
    String ba1;
    TabHost mTabHost;
    DBHandler mydb = new DBHandler(this);
    SessionManager session;
    HashMap<String, String> queryValues;
    private byte[] jpgbytes=null;
    String selectStatus = "Select status";

    //Progress Circle
    private TextView txtProgress;
    private ProgressBar progressBar;
    private int pStatus = 0;
    private Handler handler = new Handler();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_project_detail);

        //EditTexts
        location = (EditText) findViewById(R.id.edtPlocation);
        asset = (EditText) findViewById(R.id.edtAsset);
        requestedBy = (EditText) findViewById(R.id.edtPrequest);
        criticalAsset = (EditText) findViewById(R.id.edtCAsset);
        dateRequired = (EditText) findViewById(R.id.edtDateReq);
        workRequired = (EditText) findViewById(R.id.edtWorkReq);
        site = (EditText) findViewById(R.id.edtPsite);
        trade = (EditText) findViewById(R.id.edtPTrade);
        //date = (EditText) findViewById(R.id.edtPdate);
        //nt = (EditText) findViewById(R.id.edtNT);
        //ot1 = (EditText) findViewById(R.id.edtOT1);
        //ot2 = (EditText) findViewById(R.id.edtOT2);
        dt = (EditText) findViewById(R.id.edtDT);
        comment = (EditText) findViewById(R.id.edtPComment);
        progress = (EditText) findViewById(R.id.edtPprogress);

        statusSpinner = (Spinner) findViewById(R.id.statusSpinner);
        List<String>statusList = new ArrayList<String>();
        statusList.add(selectStatus);
        statusList.add("Start");
        statusList.add("Finish");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,statusList);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        statusSpinner.setAdapter(dataAdapter);

        txtProgress = (TextView) findViewById(R.id.txtProgress);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);

        mTabHost = (TabHost)findViewById(R.id.tabHost);
        mTabHost.setup();


        //Lets add the first Tab
        TabHost.TabSpec mSpec = mTabHost.newTabSpec("Detail");
        mSpec.setContent(R.id.first_Tab);
        mSpec.setIndicator("Details");
        mTabHost.addTab(mSpec);

        //Lets add the second Tab
        mSpec = mTabHost.newTabSpec("work");
        mSpec.setContent(R.id.second_Tab);
        mSpec.setIndicator("Work & Site");
        mTabHost.addTab(mSpec);

        //Lets add the third Tab
        mSpec = mTabHost.newTabSpec("Signature");
        mSpec.setContent(R.id.third_Tab);
        mSpec.setIndicator("Progress");
        mTabHost.addTab(mSpec);

        for(int i =0; i<mTabHost.getTabWidget().getChildCount();i++){

            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.WHITE);
            //tv.setPadding(10,10,10,15);
            tv.setTextSize((float) 10.0);
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
            //tv.setBackgroundResource(R.mipmap.email);
        }

        //ImageView
        camBtn = (ImageView) findViewById(R.id.imgPCam);
        pImage = (ImageView) findViewById(R.id.imgPPict);

        //Progress Dialog
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Creating Project. Please wait...");
        prgDialog.setCancelable(false);

        //Get All the users for fingerprint
        empList = mydb.getAllUsers();

        //Calenders
        myCal = Calendar.getInstance(); //dateRequired
        mycal2 = Calendar.getInstance();//Date


        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String value = extras.getString("criticalA");
            if(value != null){

                Cursor rs = mydb.getProject(value);
                rs.moveToFirst();

                final String locatio,asse,requestedB,criticalAsse,dateRequire,workRequire,sit,pro;
                locatio = rs.getString(rs.getColumnIndex("location"));
                asse = rs.getString(rs.getColumnIndex("asset"));
                requestedB = rs.getString(rs.getColumnIndex("requestedBy"));
                criticalAsse = rs.getString(rs.getColumnIndex("criticalAsset"));
                dateRequire = rs.getString(rs.getColumnIndex("dateReq"));
                workRequire = rs.getString(rs.getColumnIndex("workReq"));
                sit = rs.getString(rs.getColumnIndex("site"));
                pro = rs.getString(rs.getColumnIndex("progress"));

                location.setText(locatio);
                asset.setText(asse);
                requestedBy.setText(requestedB);
                criticalAsset.setText(criticalAsse);
                dateRequired.setText(dateRequire);
                workRequired.setText(workRequire);
                site.setText(sit);
                progress.setText(pro);

                criticalAsset.setClickable(false);
                criticalAsset.setEnabled(false);

                requestedBy.setClickable(false);
                requestedBy.setEnabled(false);

                asset.setClickable(false);
                asset.setEnabled(false);

                location.setClickable(false);
                location.setEnabled(false);

                dateRequired.setClickable(false);
                dateRequired.setEnabled(false);

                workRequired.setClickable(false);
                workRequired.setEnabled(false);

                site.setClickable(false);
                site.setEnabled(false);
                progressBar.setProgress(Integer.parseInt(pro));
                txtProgress.setText("Progress: "+pro + " %");

                //updateProBar(Integer.parseInt(pro));
            }
        }


        //dateRequired
        final DatePickerDialog.OnDateSetListener Rdate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCal.set(Calendar.YEAR, year);
                myCal.set(Calendar.MONTH, monthOfYear);
                myCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateRequired();
            }

        };

        //Date
      /*  final DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                mycal2.set(Calendar.YEAR, year);
                mycal2.set(Calendar.MONTH, monthOfYear);
                mycal2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }

        };*/

        dateRequired.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(ProjectUpdate.this, Rdate, myCal
                        .get(Calendar.YEAR), myCal.get(Calendar.MONTH),
                        myCal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        /*date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(ProjectUpdate.this, date2, mycal2
                        .get(Calendar.YEAR), mycal2.get(Calendar.MONTH),
                        mycal2.get(Calendar.DAY_OF_MONTH)).show();
            }
        });*/

        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random r = new Random();
                int random = r.nextInt(100-1)+1;
                if(criticalAsset.length() > 0){
                    pictName = criticalAsset.getText().toString() +"_"+String.valueOf(random);
                    Intent intent = new Intent(ProjectUpdate.this, CameraExActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", pictName);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 0);

                }else
                    Toast.makeText(getApplicationContext(), "Please Provide the Critical Asset first", Toast.LENGTH_SHORT).show();
            }
        });



        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(resultCode){
            case 1:{
                Bundle bl= data.getExtras();
            }
            break;
            case 2:
                break;
            case 3:{
                Bundle bl= data.getExtras();
                String id=bl.getString("id");
                Toast.makeText(ProjectUpdate.this, "Pictures Finish", Toast.LENGTH_SHORT).show();
                byte[] photo=bl.getByteArray("photo");
                if(photo!=null){
                    try{
                        Matrix matrix = new Matrix();
                        Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);
                        matrix.preRotate(90);
                        Bitmap nbm=Bitmap.createBitmap(bm ,0,0, bm .getWidth(), bm .getHeight(),matrix,true);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        nbm.compress(Bitmap.CompressFormat.JPEG, 80, out);
                        jpgbytes= out.toByteArray();

                        Bitmap bitmap =BitmapFactory.decodeByteArray(jpgbytes, 0, jpgbytes.length);
                        pImage.setImageBitmap(bitmap);

                    }catch(Exception e){
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void upload(String path,String imgName,String loc ,String asset,String rb,String ca,String dr,String wr,String sit,String nam,String trad,
                       String dat,String dt, String com,int pro,String status){

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        ///Upload image to server
        createProject(imgName, loc, asset, rb, ca, dr, wr, sit, nam, trad, dat,dt, com, pro,status);

    }

    public void updateProBar(final int progress){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (pStatus <= progress) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(pStatus);
                            txtProgress.setText("Progress: "+pStatus + " %");
                        }
                    });
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pStatus++;
                }
            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.project_detail, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){

            if (SerialPortManager.getInstance().isOpen()) {
                bIsCancel = true;
                SerialPortManager.getInstance().closeSerialPort();
            }
            exitApplication();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApplication(){
        Intent intent = new Intent(getApplicationContext(), Project.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.upload) {
        /*
         * Upload information here.
          * */

            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());


            String locatio,asse,requestedB,criticalAsse,dateRequire,workRequire,sit,nam,trad,dat,ntim,otim1,otim2,dtim,coment;
            int progres;
            locatio = location.getText().toString();
            asse = asset.getText().toString();
            requestedB = requestedBy.getText().toString();
            criticalAsse = criticalAsset.getText().toString();
            dateRequire = dateRequired.getText().toString();
            workRequire = workRequired.getText().toString();
            sit = site.getText().toString();
            nam = String.valueOf(userID);
            trad = trade.getText().toString();
            dat = currentDateandTime;
            dtim = dt.getText().toString();

            coment = comment.getText().toString();
            String status = String.valueOf(statusSpinner.getSelectedItem());
            progres = Integer.parseInt(progress.getText().toString());
            //String trad = trade.getText().toString();


            // int loc,asset,rb,ca,wr,sit,pro,dr;
            if(location.length()>0 && asset.length()>0 && requestedBy.length()>0 && criticalAsset.length()>0 && workRequired.length()>0
                    && site.length()>0 && progress.length()>0 &&dateRequired.length()>0){

                if(progres == 0){
                    Toast.makeText(getApplicationContext(), "Please update the progress", Toast.LENGTH_SHORT).show();
                }
                if(progres > 100){

                    Toast.makeText(getApplicationContext(), "Progress can not be over 100%", Toast.LENGTH_SHORT).show();
                }

                if(progres == 100){
                    //Supervisor clocks to complete project.
                    //FPDialog(1);
                    createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment, progres,status);
                }
                else if(progres < 100 && trad.length() > 0 && status != selectStatus){
                    //FPDialog(1);
                    createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment, progres,status);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please double check status or Trade", Toast.LENGTH_SHORT).show();
                }
            }

            else{

                Toast.makeText(getApplicationContext(), "Please fill in fields with *", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FPDialog(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectUpdate.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(ProjectUpdate.this);
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
                    Toast.makeText(ProjectUpdate.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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

                session = new SessionManager(getApplicationContext());
                HashMap<String, String> manager = session.getUserDetails();
                int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));
                String status;

                String locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment;
                int progres;
                locatio = location.getText().toString();
                asse = asset.getText().toString();
                requestedB = requestedBy.getText().toString();
                criticalAsse = criticalAsset.getText().toString();
                dateRequire = dateRequired.getText().toString();
                workRequire = workRequired.getText().toString();
                sit = site.getText().toString();
                status = String.valueOf(statusSpinner.getSelectedItem());
                trad = trade.getText().toString();
                dtim = dt.getText().toString();
                progres = Integer.parseInt(progress.getText().toString());
                coment = comment.getText().toString();
                File extf = Environment.getExternalStorageDirectory();
                File myFile = new File(extf.getAbsolutePath() + "/fgtit/" + pictName + ".jpg");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                dat = currentDateandTime;

                //Check the fingerprints here
                List<User> user = empList;
                int count = 0;
                if (empList.size() > 0) {
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                fpDialog.cancel();
                                nam = String.valueOf(us.getuId());
                                if(progres == 100 && us.getuId() != userID){
                                    //Call Dialog here
                                    myDialog();
                                }

                                else if(progres == 100 && us.getuId() == userID){

                                    createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment, progres,status);
                                   // Toast.makeText(getApplicationContext(), "Project Should be completed", Toast.LENGTH_SHORT).show();
                                }
                                else{

                                    if (myFile.exists()) {

                                        String path = "/sdcard/fgtit/" + pictName + ".jpg";
                                        String name = pictName + ".jpg";
                                        upload(path, name, locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat, dtim, coment, progres,status);

                                    } else {
                                        //No Picture
                                        createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment, progres,status);
                                        //Toast.makeText(getApplicationContext(), "No Picture", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                    //Toast.makeText(getApplicationContext(), "Job Done " + us.getuName(), Toast.LENGTH_SHORT).show();
                                    tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                    break;

                            }
                        }


                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                fpDialog.cancel();
                                nam = String.valueOf(us.getuId());
                                if(progres == 100 && us.getuId() != userID){
                                    //Call Dialog here
                                    myDialog();
                                }
                                else if(progres == 100 && us.getuId() == userID){
                                    //Toast.makeText(getApplicationContext(), "Project Should be completed", Toast.LENGTH_SHORT).show();
                                    createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat, dtim, coment, progres, status);
                                }
                                else {
                                    if (myFile.exists()) {
                                        String path = "/sdcard/fgtit/" + pictName + ".jpg";
                                        String name = pictName + ".jpg";
                                        upload(path, name, locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat, dtim, coment, progres, status);
                                    } else {
                                        //No Picture
                                        createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat, dtim, coment, progres, status);
                                    }
                                }
                                    //Toast.makeText(getApplicationContext(), "Job Done " + us.getuName(), Toast.LENGTH_SHORT).show();
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
                bfpWork = false;
                TimerStart();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText("Registration Failed");
                bfpWork = false;
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

    private void updateRequired() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        dateRequired.setText(sdf.format(myCal.getTime()));
    }

    private void updateDate() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

       // date.setText(sdf.format(mycal2.getTime()));
    }

    public void myDialog(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(ProjectUpdate.this);
        dialog.setTitle("Supervisor Required");
        dialog.setMessage("Only the supervisor can set a 100% progress. Please reduce the progress and clock" +
                " then set 100% for supervisor to clock. ");
        //dialog.setMessage("Project was updated to "+progress+"%, please press OK for supervisor to complete project");
        dialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //FPDialog(1);
            }
        });
        dialog.show();
    }

    public void createProject(String imgName,final String loc ,final String asset,final String rb,final String ca,final String dr,final String wr,final String sit,String nam,String trad,
                              String dat,String dt, String com,final int pro,final String status){

        try{

            //Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String json = "";
            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String currentDateandTime = sdf.format(new Date());


            //  build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("base64", ba1);
            jsonObject.accumulate("ImageName", imgName);
            jsonObject.accumulate("location", loc);
            jsonObject.accumulate("asset", asset);
            jsonObject.accumulate("rb", rb);
            jsonObject.accumulate("ca", ca);
            jsonObject.accumulate("dr", dr);
            jsonObject.accumulate("wr", wr);
            jsonObject.accumulate("site", sit);
            jsonObject.accumulate("nam", nam);
            jsonObject.accumulate("trade", trad);
            jsonObject.accumulate("dat", dat);
            jsonObject.accumulate("dt", dt);
            jsonObject.accumulate("comment", com);
            jsonObject.accumulate("progress",pro);
            jsonObject.accumulate("status",status);
            jsonObject.accumulate("id", userID);
            jsonObject.accumulate("dateDone", currentDateandTime);
            json = jsonObject.toString();
            prgDialog.show();
            params.put("projectJSON", json);
            client.setTimeout(7000);
            client.post("http://www.nexgencs.co.za/api/project/update.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    /*System.out.println("+++++++++++++++++++++++++");
                    System.out.println(response);
                    System.out.println("+++++++++++++++++++++++++");*/
                    prgDialog.hide();
                    mydb.updateProDate(pro,currentDateandTime,rb,sit,loc,asset,dr,ca);
                    Toast.makeText(getApplicationContext(), "Project Updated", Toast.LENGTH_LONG).show();
                    if(Integer.parseInt(response) == 100){
                        mydb.deleteProject(ca);
                        reloadActivity();
                    }else
                        //updateProBar(pro);
                    reloadActivity();

                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    // TODO Auto-generated method stub
                    prgDialog.hide();
                    //controller.insertRecord(idN, name, dat, lat, lon, id, "IN");
                    //jdb.insertJInfo(comment, time, dat, job_code,strt,fnsh);
                    //jdb.updateJob(job_code, progres);
                    Toast.makeText(getApplicationContext(), "Network error could not update project please check data connection", Toast.LENGTH_LONG).show();

                }
            });


        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void createNopict(final String loc ,final String asset,final String rb,final String ca,final String dr,final String wr,final String sit,String nam,String trad,
                             String dat,String dt, String com,final int pro,final String status){

        try{

            //Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String json = "";
            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String currentDateandTime = sdf.format(new Date());

            //  build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("location", loc);
            jsonObject.accumulate("asset", asset);
            jsonObject.accumulate("rb", rb);
            jsonObject.accumulate("ca", ca);
            jsonObject.accumulate("dr", dr);
            jsonObject.accumulate("wr", wr);
            jsonObject.accumulate("site", sit);
            jsonObject.accumulate("nam", nam);
            jsonObject.accumulate("trade", trad);
            jsonObject.accumulate("dat", dat);
            //jsonObject.accumulate("nt", nt);
            //jsonObject.accumulate("ot1", ot1);
            //jsonObject.accumulate("ot2", ot2);
            jsonObject.accumulate("dt", dt);
            jsonObject.accumulate("comment", com);
            jsonObject.accumulate("progress",pro);
            jsonObject.accumulate("status",status);
            jsonObject.accumulate("id", userID);
            jsonObject.accumulate("dateDone", currentDateandTime);

            json = jsonObject.toString();
            prgDialog.show();
            params.put("projectJSON", json);
            client.setTimeout(7000);
            client.post("http://www.nexgencs.co.za/api/project/update.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                   /* System.out.println("+++++++++++++++++++++++++");
                    System.out.println(response);
                    System.out.println("+++++++++++++++++++++++++");*/
                    prgDialog.hide();
                    mydb.updateProDate(pro,currentDateandTime,rb,sit,loc,asset,dr,ca);
                    Toast.makeText(getApplicationContext(), "Project Updated", Toast.LENGTH_LONG).show();
                    if(response.equals("100")){
                        mydb.deleteProject(ca);
                        reloadActivity();
                    }else{
                        //updateProBar(pro);
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        reloadActivity();
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    // TODO Auto-generated method stub
                    prgDialog.hide();
                    //controller.insertRecord(idN, name, dat, lat, lon, id, "IN");
                    //jdb.insertJInfo(comment, time, dat, job_code,strt,fnsh);
                    //jdb.updateJob(job_code, progres);
                    Toast.makeText(getApplicationContext(), "Network error could not update project please check data connection", Toast.LENGTH_LONG).show();

                }
            });


        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    // Reload ProjectActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), Project.class);
        startActivity(objIntent);
    }
}
