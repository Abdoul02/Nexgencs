package com.fgtit.fingermap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;


public class Signature extends AppCompatActivity {


    DrawingView dv ;
    private Paint mPaint;
    String upLoadServerUri = null;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String ba1;
    int saveStatus = 0;
    String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_signature);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            URL = extras.getString("url");

        }else{
            finish();
        }


        dv = new DrawingView(this);
        dv.setDrawingCacheEnabled(true);
        dv.setBackgroundColor(Color.parseColor("#ffffff"));
        setContentView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(9);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_drawing,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch(id){

            case R.id.done:
                shareDrawing();
                Bundle extras = getIntent().getExtras();
                String value = extras.getString("code");
                final String path = Environment.getExternalStorageDirectory().toString()+"/signature/" + value+".png";
                File myFile = new File(path);

                if(myFile.exists()) {
                    //upload(path, value);
                    clientDialog(path,value);
                }else{
                    Toast.makeText(getApplicationContext(),"Please save signature first",Toast.LENGTH_SHORT).show();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareDrawing()  {
        final boolean cachePreviousState = dv.isDrawingCacheEnabled();
        final int backgroundPreviousColor = dv.getDrawingCacheBackgroundColor();
        dv.setDrawingCacheEnabled(true);
        dv.setDrawingCacheBackgroundColor(0xFFFFFF);
        final Bitmap drawing = dv.getDrawingCache();
        dv.setDrawingCacheBackgroundColor(backgroundPreviousColor);
        //drawing = ThumbnailUtils.extractThumbnail(drawing, 256, 256);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            final String value = extras.getString("code");
            final String path = Environment.getExternalStorageDirectory().toString()+"/signature/";
            OutputStream fOut = null;
            File file = new File(path, String.valueOf(value)+".png");
            file.getParentFile().mkdirs();

            try {
                file.createNewFile();
            } catch (Exception e) {
                //Log.e("log", e.getCause() + e.getMessage());
                Toast.makeText(this,e.getCause() + e.getMessage(),Toast.LENGTH_SHORT).show();
            }

            try {
                fOut = new FileOutputStream(file);
            } catch (Exception e) {
                Toast.makeText(this,e.getCause() + e.getMessage(),Toast.LENGTH_SHORT).show();
            }

            if (dv.getDrawingCache() == null) {
                //Log.e(LOG_CAT,"Unable to get drawing cache ");
                Toast.makeText(this,"Unable to get drawing cache",Toast.LENGTH_SHORT).show();
            }

            // dv.getDrawingCache()
            drawing.compress(Bitmap.CompressFormat.PNG, 50, fOut);
            dv.setDrawingCacheEnabled(cachePreviousState);



            try {
                fOut.flush();
                fOut.close();
                dv.invalidate();
            } catch (IOException e) {
                Toast.makeText(this,e.getCause() + e.getMessage(),Toast.LENGTH_SHORT).show();
            }

        }

    }
    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();


            runOnUiThread(new Runnable() {
                public void run() {
                  /*  messageText.setText("Source File not exist :"
                            +uploadFilePath + "" + uploadFileName);  */
                    Toast.makeText(getApplicationContext(),"Signature not saved, please save before uploading ",Toast.LENGTH_SHORT).show();
                }
            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                final URL url = new URL("http://www.nexgencs.co.za/api/sign.php");

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""+ fileName+ "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode+ " "+String.valueOf(url));

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(),"Signature Upload Complete",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if(serverResponseCode == 403){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "Forbidden", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                if(serverResponseCode == 500){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "Server error: "+String.valueOf(url), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                       /*  messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(UploadToServer.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show(); */

                        Toast.makeText(getApplicationContext(),"Error occurred please check connection",Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                       /* messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show(); */

                        Toast.makeText(getApplicationContext(),"Error occurred please check connection",Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("server Exception", "Exception : " + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }


    public void upload(String path,String name,String client){

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba,Base64.DEFAULT);

        // Upload image to server
        new uploadToServer().execute(name,client);
    }

    public class uploadToServer extends AsyncTask<String, Void, String> {

        private ProgressDialog pd = new ProgressDialog(Signature.this);

        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("base64", ba1));
            nameValuePairs.add(new BasicNameValuePair("ImageName", params[0] + ".png"));
            nameValuePairs.add(new BasicNameValuePair("jCode",params[0]));
            nameValuePairs.add(new BasicNameValuePair("clientName",params[1]));
            String st = "Success";
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                 st = EntityUtils.toString(response.getEntity());
                Log.v("log_tag", "In the try Loop" + st);

            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return st;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
            reload();
            //Log.v("Result",  result);
        }
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint   mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
            setDrawingCacheEnabled(true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    public void clientDialog(final String path, final String value){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.client_name,null);
        dialogBuilder.setView(dialogView);
        final EditText edtClient = (EditText) dialogView.findViewById(R.id.edtClientName);

        dialogBuilder.setTitle("Client Name");
        dialogBuilder.setMessage("Enter client name below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if(edtClient.getText().length() > 0 ){

                    String clientName = edtClient.getText().toString();
                    upload(path,value,clientName);

                }else{

                    Toast.makeText(getApplicationContext(),"enter client name",Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void reload(){
        Intent intent = new Intent(this, Project.class);
        startActivity(intent);
    }
}
