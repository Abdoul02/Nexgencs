package com.fgtit.fingermap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import org.json.JSONException;
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

import static com.fgtit.data.MyConstants.BASE_URL;

public class ProjectDetail extends AppCompatActivity {

    EditText location, asset, requestedBy, criticalAsset, dateRequired, workRequired, site, name, date, progress;
    private ImageView camBtn, pImage, fpImage;
    ProgressDialog prgDialog;
    Calendar myCal, mycal2;
    String pictName;
    String ba1;
    TabHost mTabHost;
    DBHandler mydb = new DBHandler(this);
    SessionManager session;
    HashMap<String, String> queryValues;
    private byte[] jpgbytes = null;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        //EditTexts
        location = findViewById(R.id.edtPlocation);
        asset = findViewById(R.id.edtAsset);
        requestedBy = findViewById(R.id.edtPrequest);
        criticalAsset = findViewById(R.id.edtCAsset);
        dateRequired = findViewById(R.id.edtDateReq);
        workRequired = findViewById(R.id.edtWorkReq);
        site = findViewById(R.id.edtPsite);

        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);


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
       /* final DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

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
                new DatePickerDialog(ProjectDetail.this, Rdate, myCal
                        .get(Calendar.YEAR), myCal.get(Calendar.MONTH),
                        myCal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


       /* date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(ProjectDetail.this, date2, mycal2
                        .get(Calendar.YEAR), mycal2.get(Calendar.MONTH),
                        mycal2.get(Calendar.DAY_OF_MONTH)).show();
            }
        });*/

        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Random r = new Random();
                int random = r.nextInt(100 - 1) + 1;
                if (criticalAsset.length() > 0) {
                    pictName = criticalAsset.getText().toString() + "_" + random;
                    Intent intent = new Intent(ProjectDetail.this, CameraExActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", pictName);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 0);


                } else
                    Toast.makeText(getApplicationContext(), "Please Provide the Critical Asset first", Toast.LENGTH_SHORT).show();
            }
        });


        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1: {
                Bundle bl = data.getExtras();
            }
            break;
            case 2:
                break;
            case 3: {
                Bundle bl = data.getExtras();
                String id = bl.getString("id");
                Toast.makeText(ProjectDetail.this, "Pictures Finish", Toast.LENGTH_SHORT).show();
                byte[] photo = bl.getByteArray("photo");
                if (photo != null) {
                    try {
                        Matrix matrix = new Matrix();
                        Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);
                        matrix.preRotate(90);
                        Bitmap nbm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        nbm.compress(Bitmap.CompressFormat.JPEG, 80, out);
                        jpgbytes = out.toByteArray();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(jpgbytes, 0, jpgbytes.length);
                        pImage.setImageBitmap(bitmap);

                    } catch (Exception e) {
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void workExit() {
        if (SerialPortManager.getInstance().isOpen()) {
            bIsCancel = true;
            SerialPortManager.getInstance().closeSerialPort();
            this.finish();
        }
    }


    public void upload(String path, String imgName, String loc, String asset, String rb, String ca, String dr, String wr, String sit, int pro) {

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        ///Upload image to server
        createProject(imgName, loc, asset, rb, ca, dr, wr, sit, pro);

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
            /*
             * Upload information here.
             * */
            String locatio, jobNumber, requestedB, criticalAsse, dateRequire, workRequire, sit;
            int progres;
           /* locatio = location.getText().toString();
            asse = asset.getText().toString();
            requestedB = requestedBy.getText().toString();
            criticalAsse = criticalAsset.getText().toString();
            dateRequire = dateRequired.getText().toString();
            workRequire = workRequired.getText().toString();
            sit = site.getText().toString();
            progres = Integer.parseInt(progress.getText().toString());*/


            // int loc,asset,rb,ca,wr,sit,pro,dr;
            if (location.length() > 0 && asset.length() > 0 && requestedBy.length() > 0 && criticalAsset.length() > 0 && workRequired.length() > 0
                    && site.length() > 0 && dateRequired.length() > 0) {

                //FPDialog(1);

                session = new SessionManager(getApplicationContext());
                HashMap<String, String> manager = session.getUserDetails();
                int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));


                locatio = location.getText().toString();
                jobNumber = asset.getText().toString();
                requestedB = requestedBy.getText().toString();
                criticalAsse = criticalAsset.getText().toString();
                dateRequire = dateRequired.getText().toString();
                workRequire = workRequired.getText().toString();
                sit = site.getText().toString();
                progres = 0;
                createNoPict(locatio, jobNumber, requestedB, criticalAsse, dateRequire, workRequire, sit, progres);

            } else {
                Toast.makeText(getApplicationContext(), "Please fill in fields with *", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void FPDialog(int i) {
        iFinger = i;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectDetail.this);
        builder.setTitle("fingerprint Registration ");
        final LayoutInflater inflater = LayoutInflater.from(ProjectDetail.this);
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
        //ָ�ƴ���
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
                    Toast.makeText(ProjectDetail.this, "Cancel OK", Toast.LENGTH_SHORT).show();
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

                session = new SessionManager(getApplicationContext());
                HashMap<String, String> manager = session.getUserDetails();
                int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));

                String locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit;
                int progres;
                locatio = location.getText().toString();
                asse = asset.getText().toString();
                requestedB = requestedBy.getText().toString();
                criticalAsse = criticalAsset.getText().toString();
                dateRequire = dateRequired.getText().toString();
                workRequire = workRequired.getText().toString();
                sit = site.getText().toString();
                progres = 0;
                File extf = Environment.getExternalStorageDirectory();
                File myFile = new File(extf.getAbsolutePath() + "/fgtit/" + pictName + ".jpg");

                //Check the fingerprints here
                List<User> user = empList;
                int count = 0;
                if (empList.size() > 0) {
                    for (User us : user) {

                        if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

                            byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
                            if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {

                                if (us.getuId() == userID) {
                                    fpDialog.cancel();
                                    if (myFile.exists()) {

                                        String path = "/sdcard/fgtit/" + pictName + ".jpg";
                                        String name = pictName + ".jpg";
                                        upload(path, name, locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, progres);

                                    } else {
                                        //No Picture
                                        createNoPict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, progres);
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
                                        String path = "/sdcard/fgtit/" + pictName + ".jpg";
                                        String name = pictName + ".jpg";
                                        upload(path, name, locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, progres);

                                    } else {
                                        //No Picture
                                        createNoPict(locatio, asse, requestedB, criticalAsse, dateRequire, workRequire, sit, progres);
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

    public void createProject(String imgName, String loc, String asset, String rb, final String ca, String dr, String wr, String sit, final int pro) {

        try {

            //Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String json = "";
            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

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

            jsonObject.accumulate("id", userID);
            jsonObject.accumulate("dateDone", currentDateandTime);

            queryValues = new HashMap<String, String>();

            queryValues.put("asset", asset);
            queryValues.put("requestedBy", rb);
            queryValues.put("site", sit);
            queryValues.put("location", loc);
            queryValues.put("criticalAsset", ca);
            queryValues.put("progress", String.valueOf(pro));
            queryValues.put("dateReq", dr);
            queryValues.put("workReq", wr);
            queryValues.put("id", String.valueOf(userID));
            queryValues.put("dateDone", currentDateandTime);

            json = jsonObject.toString();
            prgDialog.show();
            params.put("projectJSON", json);
            client.setTimeout(7000);
            client.post(BASE_URL + "/api/project/createProject.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                        prgDialog.hide();
                        mydb.insertProject(queryValues);
                        Toast.makeText(getApplicationContext(), "Project created", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    // TODO Auto-generated method stub
                    prgDialog.hide();
                    Toast.makeText(getApplicationContext(), "Network error " + statusCode, Toast.LENGTH_LONG).show();

                }
            });
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public void createNoPict(String loc, String asset, String rb, final String ca, String dr, String wr, String sit, final int pro) {

        try {

            //Create AsycHttpClient object
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String json = "";
            session = new SessionManager(getApplicationContext());
            HashMap<String, String> manager = session.getUserDetails();
            int userID = Integer.parseInt(manager.get(SessionManager.KEY_UID));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

            //  build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("location", loc);
            jsonObject.accumulate("asset", asset);
            jsonObject.accumulate("rb", rb);
            jsonObject.accumulate("ca", ca);
            jsonObject.accumulate("dr", dr);
            jsonObject.accumulate("wr", wr);
            jsonObject.accumulate("site", sit);
            jsonObject.accumulate("id", userID);
            jsonObject.accumulate("dateDone", currentDateandTime);


            queryValues = new HashMap<String, String>();

            queryValues.put("asset", asset);
            queryValues.put("requestedBy", rb);
            queryValues.put("site", sit);
            queryValues.put("location", loc);
            queryValues.put("criticalAsset", ca);
            queryValues.put("progress", String.valueOf(pro));
            queryValues.put("dateReq", dr);
            queryValues.put("workReq", wr);
            queryValues.put("id", String.valueOf(userID));
            queryValues.put("dateDone", currentDateandTime);

            json = jsonObject.toString();
            prgDialog.show();
            params.put("projectJSON", json);
            client.setTimeout(7000);
            client.post(BASE_URL + "/api/project/createProject.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                    try {
                        JSONObject result = new JSONObject(response);
                        int success = result.getInt("success");
                        String message = result.getString("message");

                        prgDialog.hide();
                        if(success == 1){
                            mydb.insertProject(queryValues);
                            reloadActivity();
                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    prgDialog.hide();
                    Toast.makeText(ProjectDetail.this, "Error occurred with code: " + statusCode, Toast.LENGTH_LONG).show();

                }
            });


        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            Toast.makeText(this, "A JSON error occurred", Toast.LENGTH_LONG).show();
        }
    }


    // Reload ProjectActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(this, Project.class);
        startActivity(objIntent);
    }
}
