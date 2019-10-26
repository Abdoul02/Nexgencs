package com.fgtit.fingermap;


import android.annotation.SuppressLint;
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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

import static com.fgtit.service.UploadService.JOBCARD_URL;

public class JobDetail extends AppCompatActivity implements SingleUploadBroadcastReceiver.Delegate {

    JobDB jdb = new JobDB(this);
    DBHandler db = new DBHandler(this);
    TextView jobName, location, description, customer, approved, start, end, jCode, down, ope, company, office;
    LinearLayout lloffice, llSatis, llTime;
    EditText comment, progress, startkm, endkm, edtTimeOnSite;
    Button update, signature;
    ProgressDialog prgDialog;
    private int count;
    private ArrayList<User> empList;
    String ba1;
    SessionManager session;
    HashMap<String, String> manager;
    int compId;
    String currentPhotoPath, fileID, imgPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    private AsyncFingerprint vFingerprint;
    private Dialog fpDialog;
    ProgressDialog saveDialog, pDialog;
    private int iFinger = 0;
    private ImageView fpImage, pict, camera, imgSmile, imgNeutral, imgSad;
    private TextView tvFpStatus;
    private boolean bcheck = false;
    private boolean bIsUpImage = true;
    private boolean bIsCancel = false;
    private boolean bfpWork = false;
    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;
    private byte[] jpgbytes = null;
    int clientSatisfaction = 0;
    String jobCode;
    private List<String> listOfImagesPath;
    private final SingleUploadBroadcastReceiver uploadReceiver =
            new SingleUploadBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

        listOfImagesPath = new ArrayList<>();
        session = new SessionManager(getApplicationContext());
        manager = session.getUserDetails();
        compId = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));
        jobName = findViewById(R.id.job_name);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        customer = findViewById(R.id.customer);
        approved = findViewById(R.id.approvedBy);
        start = findViewById(R.id.start);
        end = findViewById(R.id.end_date);
        jCode = findViewById(R.id.Job_code);
        down = findViewById(R.id.txtDownl);
        ope = findViewById(R.id.txtOpen);
        comment = findViewById(R.id.edtComment);
        progress = findViewById(R.id.edtProgress);
        startkm = findViewById(R.id.edtStart);
        edtTimeOnSite = findViewById(R.id.edtTime);
        endkm = findViewById(R.id.edtFinish);
        update = findViewById(R.id.btnUpd);
        signature = findViewById(R.id.btnSign);
        pict = findViewById(R.id.imgPict);
        camera = findViewById(R.id.imgCam);
        company = findViewById(R.id.cust);
        lloffice = findViewById(R.id.llOffice);
        llTime = findViewById(R.id.llTimeOnSite);
        llSatis = findViewById(R.id.llClientSat);
        office = findViewById(R.id.office);
        imgSmile = findViewById(R.id.imgSmile);
        imgNeutral = findViewById(R.id.imgNeutral);
        imgSad = findViewById(R.id.imgSad);

        // signature.setClickable(false);
        // signature.setVisibility(View.GONE);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Uploading information...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(pDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);

        if (compId == 63 || compId == 3) {

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
                jobCode = value;
                final String jname, jloc, jdesc, jcust, japp, jstart, jend, code, prog, link, sOffice;

                jname = rs.getString(rs.getColumnIndex("name"));
                jloc = rs.getString(rs.getColumnIndex("location"));
                jdesc = rs.getString(rs.getColumnIndex("description"));
                jcust = rs.getString(rs.getColumnIndex("customer"));
                japp = rs.getString(rs.getColumnIndex("approvedBy"));
                jstart = rs.getString(rs.getColumnIndex("start"));
                jend = rs.getString(rs.getColumnIndex("end"));
                code = rs.getString(rs.getColumnIndex("jobCode"));
                prog = rs.getString(rs.getColumnIndex("progress"));
                link = rs.getString(rs.getColumnIndex("attachment"));
                sOffice = rs.getString(rs.getColumnIndex("office"));

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

                            if (myFile.exists()) {

                                open(value);
                            } else {
                                //Toast.makeText(getApplicationContext(), "Downloading, please wait...", Toast.LENGTH_LONG).show();
                                download("http://nexgencs.co.za/nexgen" + link, value);
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

                            if (myFile.exists()) {

                                open(value);
                            } else {

                                Toast.makeText(getApplicationContext(), value + "  not found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispatchTakePictureIntent();
                    }
                });

                imgSmile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientSatisfaction = 1;
                        imgSmile.setImageResource(R.drawable.happy_green);
                        imgNeutral.setImageResource(R.drawable.neutral_grey);
                        imgSad.setImageResource(R.drawable.sad_grey);
                        Toast.makeText(JobDetail.this, "Satisfied", Toast.LENGTH_SHORT).show();
                    }
                });

                imgNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientSatisfaction = 2;
                        imgSmile.setImageResource(R.drawable.happy_grey);
                        imgNeutral.setImageResource(R.drawable.neutral_green);
                        imgSad.setImageResource(R.drawable.sad_grey);
                        Toast.makeText(JobDetail.this, "Neutral", Toast.LENGTH_SHORT).show();
                    }
                });

                imgSad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientSatisfaction = 3;
                        imgSmile.setImageResource(R.drawable.happy_grey);
                        imgNeutral.setImageResource(R.drawable.neutral_grey);
                        imgSad.setImageResource(R.drawable.sad_green);
                        Toast.makeText(JobDetail.this, "Not satisfied", Toast.LENGTH_SHORT).show();
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

                            if (clientSatisfaction == 0 && compId == 63) {
                                Toast.makeText(getApplicationContext(), "Please select client satisfaction and press finish", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please press finish", Toast.LENGTH_SHORT).show();
                            }


                        } else {

                            if (Integer.parseInt(pro) > 100) {

                                Toast.makeText(getApplicationContext(), "Progress cannot be over 100%", Toast.LENGTH_SHORT).show();
                            }

                            if (com.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Please provide a comment", Toast.LENGTH_SHORT).show();

                            } else {

                                File extf = Environment.getExternalStorageDirectory();
                                File myFile = new File(extf.getAbsolutePath() + "/fgtit/" + code + "_" + pro + ".jpg");
                                listOfImagesPath = jdb.getPictures(jobCode);
                                if (listOfImagesPath.size() > 0) {

                                    String path = "/sdcard/fgtit/" + code + "_" + pro + ".jpg";
                                    String name = code + "_" + pro + ".jpg";
                                    pDialog.show();
                                    uploadFunction(com,currentDateandTime,currentDateandTime,Integer.parseInt(pro),code,startK,endK,clientSatisfaction,timeOnSite);
                                } else {
                                    nopict(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, startK, endK, clientSatisfaction, timeOnSite);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
            jdb.insertPictPath(0, currentPhotoPath, jobCode);
            anotherPicture();
        } else {
            showToast("Something went wrong, could not save picture");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = pict.getWidth();
        int targetH = pict.getHeight();

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
        pict.setImageBitmap(bitmap);
    }

    public void showGridView(View v) {
        listOfImagesPath = jdb.getPictures(jobCode);
        if (listOfImagesPath.size() > 0) {
            Bundle dataBundle = new Bundle();
            dataBundle.putString("asset", jobCode);
            Intent intent = new Intent(getApplicationContext(), DisplayLocalPictures.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No pictures to display", Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoMap(View v) {

        if (isAppInstalled("com.google.android.apps.maps")) {

            String address = location.getText().toString();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=" + address));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
        } else {

            Toast.makeText(getApplicationContext(), "You do not have google maps installed", Toast.LENGTH_SHORT).show();

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
        switch (id) {
            case android.R.id.home:
                this.setResult(1);
                this.finish();
                workExit();
                return true;
            case R.id.finish:

                if (progress.getText().toString().equals("100")) {

                    if (clientSatisfaction == 0 && compId == 63) {
                        Toast.makeText(getApplicationContext(), "Please select client satisfaction", Toast.LENGTH_SHORT).show();
                    } else {

                        FPDialog(1);
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "Progress must be 100%", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void FPDialog(int i) {
        iFinger = i;
        AlertDialog.Builder builder = new AlertDialog.Builder(JobDetail.this);
        builder.setTitle("fingerprint Reader ");
        final LayoutInflater inflater = LayoutInflater.from(JobDetail.this);
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

    //Finger Print Registration
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
                listOfImagesPath = jdb.getPictures(jobCode);
                if (empList.size() > 0) {

                    //for(int i=0; i<emplist.size(); i++)
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if (us.getuId() == userID) {

                                    fpDialog.cancel();

                                    if (listOfImagesPath.size() > 0) {
                                        String path = "/sdcard/fgtit/" + code + "_" + pro + ".jpg";
                                        String name = code + "_" + pro + ".jpg";
                                        //upload(path, name, com, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh, clientSatisfaction, timeOnSite);
                                        pDialog.show();
                                        uploadFunction(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh, clientSatisfaction, timeOnSite);

                                    } else {
                                        nopict(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh, clientSatisfaction, timeOnSite);
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
                                    if (listOfImagesPath.size() > 0) {
                                        String path = "/sdcard/fgtit/" + code + "_" + pro + ".jpg";
                                        String name = code + "_" + pro + ".jpg";
                                        //upload(path, name, com, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh, clientSatisfaction, timeOnSite);
                                        pDialog.show();
                                        uploadFunction(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh, clientSatisfaction, timeOnSite);


                                    } else {
                                        nopict(com, currentDateandTime, currentDateandTime, Integer.parseInt(pro), code, strt, fnsh, clientSatisfaction, timeOnSite);

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

                bfpWork = false;
                TimerStart();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText("Reading Failed");
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

    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), JobActivity.class);
        startActivity(objIntent);
    }

    public void uploadFunction(final String comment, final String dat, final String time, final int progres, final String job_code, final String strt, final String fnsh, final int cs, final String timeOnSite) {
        fileID = UUID.randomUUID().toString();
        uploadReceiver.setDelegate(this);
        uploadReceiver.setUploadID(fileID);

        imgPath = listOfImagesPath.get(0).substring(0, listOfImagesPath.get(0).lastIndexOf("/") + 1);
        try {
            MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(this, fileID, JOBCARD_URL);
            multipartUploadRequest.addHeader("Accept", "application/json");
            for (int i = 0; i < listOfImagesPath.size(); i++) {
                String imageName = listOfImagesPath.get(i).substring(listOfImagesPath.get(i).lastIndexOf("/") + 1);
                multipartUploadRequest.addParameter("name" + i, imageName);
                multipartUploadRequest.addFileToUpload(listOfImagesPath.get(i), "document" + i);
            }

            multipartUploadRequest.addParameter("comment", comment);
            multipartUploadRequest.addParameter("tim", time);
            multipartUploadRequest.addParameter("dat", dat);
            multipartUploadRequest.addParameter("jobCode", job_code);
            multipartUploadRequest.addParameter("progress", String.valueOf(progres));
            multipartUploadRequest.addParameter("start", strt);
            multipartUploadRequest.addParameter("finish", fnsh);
            multipartUploadRequest.addParameter("cs", String.valueOf(cs));
            multipartUploadRequest.addParameter("timeOnSite", timeOnSite);
            multipartUploadRequest.addParameter("numberOfPict", String.valueOf(listOfImagesPath.size()));
            multipartUploadRequest.setNotificationConfig(new UploadNotificationConfig());
            multipartUploadRequest.setMaxRetries(3);
            multipartUploadRequest.startUpload();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void upload(String path, String name, String com, String currentDateandTime, int pro, String code, String strt, String finish, int cs, String timeOnSite) {

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        // Upload image to server
        updateJob(name, com, currentDateandTime, currentDateandTime, pro, code, strt, finish, cs, timeOnSite);
        // new uploadToServer().execute(name);
    }

    public void updateJob(final String name, final String comment, final String dat, final String time, final int progres, final String job_code, final String strt, final String fnsh, final int cs, final String timeOnSite) {

        try {

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
            jsonObject.accumulate("cs", cs);
            jsonObject.accumulate("timeOnSite", timeOnSite);


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
                    if (Integer.parseInt(response) == 100) {
                        jdb.deletejobcard(job_code);
                        jdb.deleteJinfo(job_code);
                        reloadActivity();
                    } else {
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
                    jdb.insertJInfo(comment, time, dat, job_code, strt, fnsh);
                    //jdb.updateJob(job_code, progres);
                    Toast.makeText(getApplicationContext(), "Network error " + statusCode, Toast.LENGTH_LONG).show();

                }
            });


        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void nopict(final String comment, final String dat, final String time, final int progres, final String job_code, final String strt, final String fnsh, final int cs, final String timeOnSite) {

        try {

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
            jsonObject.accumulate("cs", cs);
            jsonObject.accumulate("timeOnSite", timeOnSite);

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
                    if (Integer.parseInt(response) == 100100) {
                        jdb.deletejobcard(job_code);
                        jdb.deleteJinfo(job_code);
                        reloadActivity();
                    } else {
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
                    jdb.insertJInfo(comment, time, dat, job_code, strt, fnsh);
                    Toast.makeText(getApplicationContext(), "Network error, please check your internet connection.", Toast.LENGTH_LONG).show();

                }
            });


        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void open(String nam) {
        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/attach/" + nam);  // -> filename = maven.pdf
        Uri path = Uri.fromFile(pdfFile);
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(JobDetail.this, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }

    public void download(String url, String nam) {
        new DownloadFile().execute(url);
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
            uploadResponse(response);
        } catch (UnsupportedEncodingException e) {
            Log.e("JobDetail", "UnsupportedEncodingException");
        }
    }

    @Override
    public void onCancelled() {

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
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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

                // Detect the file length
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

                byte[] data = new byte[1024];
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

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
        Log.e("JobDetail",response);
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
                        if (deleteFile(imgPath + imageName)) {
                            jdb.deletePicturesByPath(imgPath + imageName);
                            count++;
                        }
                    }
                }
            }
            if (success == 1) {
                if (progress.equals("100")) {
                    jdb.deletejobcard(jobCode);
                    jdb.deleteJinfo(jobCode);
                } else {
                    jdb.updateJob(jobCode, Integer.parseInt(progress));
                }
            }
            showToast(message + ", with " + count + " images uploaded");
            reloadActivity();
        } catch (JSONException e) {
            Log.e("JobDetail", e.getMessage());
        }
    }
}
