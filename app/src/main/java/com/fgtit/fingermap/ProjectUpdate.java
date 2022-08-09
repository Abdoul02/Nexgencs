package com.fgtit.fingermap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.adapter.DrawingActivity;
import com.fgtit.data.CommonFunction;
import com.fgtit.data.MyConstants;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.models.SessionManager;
import com.fgtit.models.User;
import com.fgtit.service.DownloadService;
import com.fgtit.service.NetworkService;
import com.fgtit.service.SingleUploadBroadcastReceiver;
import com.fgtit.utils.ExtApi;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

import static com.fgtit.data.MyConstants.BASE_URL;
import static com.fgtit.data.MyConstants.IMAGE;
import static com.fgtit.data.MyConstants.IMAGE_NAME;
import static com.fgtit.data.MyConstants.IMAGE_PATH;
import static com.fgtit.data.MyConstants.PROJECT_SIGNATURE;
import static com.fgtit.data.MyConstants.PROJECT_SIGNATURE_URL;
import static com.fgtit.service.NetworkService.PROJECT_URL;

public class ProjectUpdate extends AppCompatActivity implements SingleUploadBroadcastReceiver.Delegate {

    EditText location, asset, requestedBy, criticalAsset, dateRequired, workRequired, site, trade, date, dt, progress, comment;
    LinearLayout llImageView;
    private ImageView camBtn, pImage, fpImage;
    private Spinner statusSpinner;
    ProgressDialog prgDialog;
    Calendar myCal, mycal2;
    String pictName;
    String ba1;
    TabHost mTabHost;
    DBHandler mydb = new DBHandler(this);
    JobDB jobDB = new JobDB(this);
    CommonFunction common = new CommonFunction(this);
    SessionManager session;
    HashMap<String, String> queryValues;
    private byte[] jpgbytes = null;
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
    private boolean bcheck = false;
    private boolean bIsUpImage = true;
    private boolean bIsCancel = false;
    private boolean bfpWork = false;
    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private int iFinger = 0;
    private int count;
    private ArrayList<User> empList;

    String currentPhotoPath, fileID;
    static final int REQUEST_TAKE_PHOTO = 1;
    private List<String> listOfImagesPath;
    Dialog dialog;
    ProgressDialog pDialog;
    String imgPath, signatureImg, signatureImgPath, signatureImgName;
    private final SingleUploadBroadcastReceiver uploadReceiver =
            new SingleUploadBroadcastReceiver();

    private BroadcastReceiver signatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            handleSignatureResponse(bundle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_project_detail);


        listOfImagesPath = new ArrayList<>();
        //EditTexts
        location = findViewById(R.id.edtPlocation);
        asset = findViewById(R.id.edtAsset);
        requestedBy = findViewById(R.id.edtPrequest);
        criticalAsset = findViewById(R.id.edtCAsset);
        dateRequired = findViewById(R.id.edtDateReq);
        workRequired = findViewById(R.id.edtWorkReq);
        site = findViewById(R.id.edtPsite);
        trade = findViewById(R.id.edtPTrade);
        //date = (EditText) findViewById(R.id.edtPdate);
        //nt = (EditText) findViewById(R.id.edtNT);
        //ot1 = (EditText) findViewById(R.id.edtOT1);
        //ot2 = (EditText) findViewById(R.id.edtOT2);
        dt = findViewById(R.id.edtDT);
        comment = findViewById(R.id.edtPComment);
        progress = findViewById(R.id.edtPprogress);
        llImageView = findViewById(R.id.lPict);

        statusSpinner = findViewById(R.id.statusSpinner);
        List<String> statusList = new ArrayList<String>();
        statusList.add(selectStatus);
        statusList.add("Start");
        statusList.add("Finish");

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Uploading information...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(pDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, statusList);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        statusSpinner.setAdapter(dataAdapter);

        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);
        //progressBar.setProgress(0);
        progressBar.setSecondaryProgress(100);
        progressBar.setMax(100);

        mTabHost = (TabHost) findViewById(R.id.tabHost);
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

        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {

            TextView tv = mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.WHITE);
            //tv.setPadding(10,10,10,15);
            tv.setTextSize((float) 10.0);
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
            //tv.setBackgroundResource(R.mipmap.email);
        }

        //ImageView
        camBtn = findViewById(R.id.imgPCam);
        pImage = findViewById(R.id.imgPPict);

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
            if (value != null) {

                Cursor rs = mydb.getProject(value);
                rs.moveToFirst();

                final String locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, pro;
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
                txtProgress.setText("Progress: " + pro + " %");

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

        dateRequired.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(ProjectUpdate.this, Rdate, myCal
                        .get(Calendar.YEAR), myCal.get(Calendar.MONTH),
                        myCal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random r = new Random();
                int random = r.nextInt(100 - 1) + 1;
                if (criticalAsset.length() > 0) {
/*                    pictName = criticalAsset.getText().toString() +"_"+String.valueOf(random);
                    Intent intent = new Intent(ProjectUpdate.this, CameraExActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", pictName);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 0);*/
                    dispatchTakePictureIntent();

                } else
                    Toast.makeText(getApplicationContext(), "Please Provide the Critical Asset first", Toast.LENGTH_SHORT).show();
            }
        });


        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                setPic();
                jobDB.insertPictPath(0, currentPhotoPath, criticalAsset.getText().toString());
                anotherPicture();
            }

            if (requestCode == MyConstants.PROJECT_SIGN) {
                if (data != null) {
                    signatureImg = data.getStringExtra(IMAGE);
                    signatureImgPath = data.getStringExtra(IMAGE_PATH);
                    signatureImgName = data.getStringExtra(IMAGE_NAME);
                    signatureDialog();
                }
            }
        } else {
            Toast.makeText(this, "Something went wrong, could not save picture", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void signatureDialog() {
        common.showCustomDialog(
                "Signature",
                "Please upload or delete signature",
                signatureImgPath,
                onSign(),
                deleteSignature(),
                "upload",
                "delete"
        );
    }

    public DialogInterface.OnClickListener onSign() {
        return (dialog, which) -> uploadSignature();
    }

    private void uploadSignature() {
        JSONObject postDataParams = new JSONObject();
        String cAsset = criticalAsset.getText().toString();
        String jobNumber = asset.getText().toString();
        try {
            postDataParams.accumulate("job_number", jobNumber);
            postDataParams.accumulate("critical_asset", cAsset);
            postDataParams.accumulate("image_name", signatureImgName);
            postDataParams.accumulate("image", signatureImg);

            common.setDialog(true);
            Intent client_intent = new Intent(this, NetworkService.class);
            client_intent.putExtra(DownloadService.POST_JSON, "signature");
            client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
            client_intent.putExtra(DownloadService.FILTER, PROJECT_SIGNATURE);
            client_intent.putExtra(DownloadService.URL, PROJECT_SIGNATURE_URL);
            startService(client_intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public DialogInterface.OnClickListener deleteSignature() {
        return (dialog, which) -> {
            if (common.deleteFile(signatureImgPath)) {
                common.showToast("Signature deleted");
            }
        };
    }

    public void showGrid(View v) {
        listOfImagesPath = jobDB.getPictures(criticalAsset.getText().toString());
        if (listOfImagesPath.size() > 0) {
            Bundle dataBundle = new Bundle();
            dataBundle.putString("asset", criticalAsset.getText().toString());
            Intent intent = new Intent(getApplicationContext(), DisplayLocalPictures.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No pictures to display", Toast.LENGTH_SHORT).show();
        }
    }

    public void fileUploadFunction(String userId, final String loc, final String asset, final String rb, final String ca, final String dr, final String wr, final String sit, String nam, String trad,
                                   String dat, String dt, String com, final int pro, final String status) {
        fileID = UUID.randomUUID().toString();
        uploadReceiver.setDelegate(this);
        uploadReceiver.setUploadID(fileID);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String currentDateTime = sdf.format(new Date());
        imgPath = listOfImagesPath.get(0).substring(0, listOfImagesPath.get(0).lastIndexOf("/") + 1);
        try {
            MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(this, fileID, PROJECT_URL);
            multipartUploadRequest.addHeader("Accept", "application/json");

            for (int i = 0; i < listOfImagesPath.size(); i++) {
                String imageName = listOfImagesPath.get(i).substring(listOfImagesPath.get(i).lastIndexOf("/") + 1);
/*                String extension = imageName.substring(listOfImagesPath.lastIndexOf(".")+1);
                String actualName = listOfImagesPath.get(i).substring(listOfImagesPath.lastIndexOf("/")+1,listOfImagesPath.lastIndexOf("."));
                String newName = actualName + "_"+i + "."+extension;*/
                multipartUploadRequest.addParameter("name" + i, imageName);
                multipartUploadRequest.addFileToUpload(listOfImagesPath.get(i), "document" + i);
            }
            multipartUploadRequest.addParameter("location", loc);
            multipartUploadRequest.addParameter("asset", asset);
            multipartUploadRequest.addParameter("rb", rb);
            multipartUploadRequest.addParameter("ca", ca);
            multipartUploadRequest.addParameter("dr", dr);
            multipartUploadRequest.addParameter("wr", wr);
            multipartUploadRequest.addParameter("site", sit);
            multipartUploadRequest.addParameter("nam", nam);
            multipartUploadRequest.addParameter("trade", trad);
            multipartUploadRequest.addParameter("dat", dat);
            multipartUploadRequest.addParameter("dt", dt);
            multipartUploadRequest.addParameter("comment", com);
            multipartUploadRequest.addParameter("progress", String.valueOf(pro));
            multipartUploadRequest.addParameter("status", status);
            multipartUploadRequest.addParameter("id", userId);
            multipartUploadRequest.addParameter("numberOfPict", String.valueOf(listOfImagesPath.size()));
            multipartUploadRequest.addParameter("dateDone", currentDateTime);
            multipartUploadRequest.setNotificationConfig(new UploadNotificationConfig());
            multipartUploadRequest.setMaxRetries(3);
            multipartUploadRequest.startUpload();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void updateProBar(final int progress) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (pStatus <= progress) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(pStatus);
                            txtProgress.setText("Progress: " + pStatus + " %");
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
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (SerialPortManager.getInstance().isOpen()) {
                bIsCancel = true;
                SerialPortManager.getInstance().closeSerialPort();
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.upload) {

            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());


            String locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat, ntim, otim1, otim2, dtim, coment;
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
            if (location.length() > 0 && asset.length() > 0 && requestedBy.length() > 0 && criticalAsset.length() > 0 && workRequired.length() > 0
                    && site.length() > 0 && progress.length() > 0 && dateRequired.length() > 0) {

                if (progres == 0) {
                    Toast.makeText(getApplicationContext(), "Please update the progress", Toast.LENGTH_SHORT).show();
                }
                if (progres > 100) {
                    Toast.makeText(getApplicationContext(), "Progress can not be over 100%", Toast.LENGTH_SHORT).show();
                }

                if (progres == 100) {
                    //Supervisor clocks to complete project.
                    FPDialog(1);
                    //createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment, progres,status);

                } else if (progres < 100 && trad.length() > 0 && !status.equals(selectStatus)) {
                    FPDialog(1);
                    //createNopict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, nam, trad, dat,dtim, coment, progres,status);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select check status and Trade", Toast.LENGTH_SHORT).show();
                }
            } else {

                Toast.makeText(getApplicationContext(), "Please fill in fields with *", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        if (id == R.id.sign) {
            Intent signatureIntent = new Intent(this, DrawingActivity.class);
            startActivityForResult(signatureIntent, MyConstants.PROJECT_SIGN);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
        registerReceiver(signatureReceiver, new IntentFilter(
                NetworkService._SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
        unregisterReceiver(signatureReceiver);
    }

    private void FPDialog(int i) {
        iFinger = i;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectUpdate.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(ProjectUpdate.this);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = vl.findViewById(R.id.imageView1);
        tvFpStatus = vl.findViewById(R.id.textview1);
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

    private void FPProcess() {
        if (!bfpWork) {
            tvFpStatus.setText(getString(R.string.txt_fpplace));
            try {
                Thread.currentThread();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //imgFeed.setImageResource(R.drawable.green_trans);

            vFingerprint.FP_GetImage();
            bfpWork = true;
        }
    }

    private void FPInit() {
        vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener() {
            @Override
            public void onGetImageSuccess() {
                if (bIsUpImage) {
                    vFingerprint.FP_UpImage();
                    tvFpStatus.setText(getString(R.string.txt_fpdisplay));
                } else {
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
                listOfImagesPath = jobDB.getPictures(criticalAsset.getText().toString());
                String locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, user_id, trad, dat, dtim, coment;
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
                //File myFile = new File(extf.getAbsolutePath() + "/fgtit/" + pictName + ".jpg");
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
                                user_id = String.valueOf(us.getuId());
                                if (listOfImagesPath.size() > 0) {
                                    String path = "/sdcard/fgtit/" + pictName + ".jpg";
                                    String name = pictName + ".jpg";
                                    pDialog.show();
                                    fileUploadFunction(user_id, locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, user_id, trad, dat, dtim, coment, progres, status);

                                } else {
                                    //No Picture
                                    createNoPict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, user_id, trad, dat, dtim, coment, progres, status);
                                }
                                tvFpStatus.setText(getString(R.string.txt_fpmatchok));
                                break;
                            }
                        }

                        if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
                                fpDialog.cancel();
                                user_id = String.valueOf(us.getuId());
                                if (listOfImagesPath.size() > 0) {
                                    pDialog.show();
                                    fileUploadFunction(user_id, locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, user_id, trad, dat, dtim, coment, progres, status);
                                } else {
                                    //No Picture
                                    createNoPict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, user_id, trad, dat, dtim, coment, progres, status);
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

    @SuppressLint("HandlerLeak")
    public void TimerStart() {
        if (startTimer == null) {
            startTimer = new Timer();
            startHandler = new Handler() {
                @SuppressLint("HandlerLeak")
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

    public void TimeStop() {
        if (startTimer != null) {
            startTimer.cancel();
            startTimer = null;
            startTask.cancel();
            startTask = null;
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

    public void myDialog() {
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

    private void anotherPicture() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ProjectUpdate.this);
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

    public void createNoPict(final String loc, final String asset, final String rb, final String ca, final String dr, final String wr, final String sit, String nam, String trad,
                             String dat, String dt, String com, final int pro, final String status) {

        try {

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
            jsonObject.accumulate("dt", dt);
            jsonObject.accumulate("comment", com);
            jsonObject.accumulate("progress", pro);
            jsonObject.accumulate("status", status);
            jsonObject.accumulate("id", userID);
            jsonObject.accumulate("dateDone", currentDateandTime);

            json = jsonObject.toString();
            prgDialog.show();
            params.put("projectJSON", json);
            client.setTimeout(7000);
            client.post(BASE_URL + "/api/project/update.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                   /* System.out.println("+++++++++++++++++++++++++");
                    System.out.println(response);
                    System.out.println("+++++++++++++++++++++++++");*/
                    prgDialog.hide();
                    mydb.updateProDate(pro, currentDateandTime, rb, sit, loc, asset, dr, ca);
                    Toast.makeText(getApplicationContext(), "Project Updated", Toast.LENGTH_LONG).show();
                    if (response.equals("100")) {
                        mydb.deleteProject(ca);
                    } else {
                        //updateProBar(pro);
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                    }
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


        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
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
                Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID +
                                ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JC_" + timeStamp;
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

    private void setPic() {
        // Get the dimensions of the View
        int targetW = pImage.getWidth();
        int targetH = pImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        pImage.setImageBitmap(bitmap);
    }

    // Reload ProjectActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), Project.class);
        startActivity(objIntent);
    }

    @Override
    public void onProgress(int progress) {
        pDialog.setProgress(progress);
    }

    @Override
    public void onProgress(long uploadedBytes, long totalBytes) {
    }

    @Override
    public void onError(Exception exception) {
    }

    @Override
    public void onCompleted(int serverResponseCode, byte[] serverResponseBody) {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
        try {
            String response = new String(serverResponseBody, "UTF-8");
            handleResponse(response);
        } catch (UnsupportedEncodingException e) {
            Log.e(ProjectUpdate.class.getSimpleName(), "UnsupportedEncodingException");
        }
    }

    @Override
    public void onCancelled() {
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean deleteFile(String path) {
        File fileToDelete = new File(path);
        if (fileToDelete.exists()) {
            return fileToDelete.delete();
        }
        return false;
    }

    public void handleResponse(String response) {
        try {
            String databaseRes, message;
            JSONObject result = new JSONObject(response);
            databaseRes = result.getString("database");
            JSONObject database = new JSONObject(databaseRes);
            int count = 0;
            int success = database.getInt("success");
            String progress = database.getString("progress");
            message = database.getString("message");
            JSONArray pictures = result.getJSONArray("pictures");
            if (pictures.length() > 0) {
                for (int x = 0; x < pictures.length(); x++) {

                    JSONObject obj = (JSONObject) pictures.get(x);
                    String imageName = obj.getString("file_name");
                    int imgSuccess = obj.getInt("success");
                    if (imgSuccess == 1) {
                        Log.d("ImagePath", imgPath + imageName);
                        if (deleteFile(imgPath + imageName)) {
                            jobDB.deletePicturesByPath(imgPath + imageName);
                            count++;
                        }
                    }
                }
            }
            //        Log.d("ImagePath", "Critical Asset "+ criticalAsset.getText().toString() + " Progress: "+ progress);
            if (success == 1) {
                if (progress.equals("100")) {
                    mydb.deleteProject(criticalAsset.getText().toString());
                } else {
                    mydb.updateProgress(criticalAsset.getText().toString(), progress);
                }
            }
            showToast(message + ", with " + count + " images uploaded");
            reloadActivity();
        } catch (JSONException e) {
            Log.e(ProjectUpdate.class.getSimpleName(), "UnsupportedEncodingException");
        }
    }

    private void handleSignatureResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        common.cancelDialog();
        if (resultCode == RESULT_OK && filter.equals(PROJECT_SIGNATURE)) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            try {
                JSONObject result = new JSONObject(response);
                int success = result.getInt("success");
                String message = result.getString("message");

                if (success == 1) {
                    common.deleteFile(signatureImgPath);
                }
                common.showToast(message);

            } catch (JSONException e) {
                Log.e(ProjectUpdate.class.getSimpleName(), "jsonError: " + e.getMessage());
            }
        }
    }
}
