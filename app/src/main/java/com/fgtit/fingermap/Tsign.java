package com.fgtit.fingermap;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.fgtit.data.MyConstants.BASE_URL;

public class Tsign extends AppCompatActivity {

    DrawingView dv ;
    private Paint mPaint;
    ProgressDialog dialog = null;
    String ba1;
    int saveStatus = 0;
    public static String URL = BASE_URL + "/api/timesheet/timesheet.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_tsign);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new
                ColorDrawable(Color.parseColor("#020969")));

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

           /* case R.id.save:
                shareDrawing();
                Toast.makeText(this, "Signature saved", Toast.LENGTH_SHORT).show();
                saveStatus = 1;*/

            case R.id.done:
                Bundle extras = getIntent().getExtras();
                String value,cust,serial,hour,spar,detail,idn;
                value = extras.getString("job");
                cust = extras.getString("cust");
                serial = extras.getString("serial");
                hour = extras.getString("hour");
                spar = extras.getString("spar");
                detail = extras.getString("detail");
                idn = extras.getString("idNum");

                final String path = Environment.getExternalStorageDirectory().toString()+"/timesheet/" + String.valueOf(value)+".png";

                if(saveStatus ==1) {
                    upload(path, value,cust,serial,hour,spar,detail,idn);
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

            final String value = extras.getString("job");


            final String path = Environment.getExternalStorageDirectory().toString()+"/timesheet/";
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

    public void upload(String path,String name,String cust,String serial, String hour, String spar, String detail,String idn){

        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        // Upload image to server
        new uploadToServer().execute(name,cust,serial,hour,spar,detail,idn);
    }

    public class uploadToServer extends AsyncTask<String, Void, String> {

        private ProgressDialog pd = new ProgressDialog(Tsign.this);

        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("base64", ba1));
            nameValuePairs.add(new BasicNameValuePair("ImageName", params[0] + ".png"));
            nameValuePairs.add(new BasicNameValuePair("jCode",params[0]));
            nameValuePairs.add(new BasicNameValuePair("cust",params[1]));
            nameValuePairs.add(new BasicNameValuePair("serial",params[2]));
            nameValuePairs.add(new BasicNameValuePair("hour",params[3]));
            nameValuePairs.add(new BasicNameValuePair("spar",params[4]));
            nameValuePairs.add(new BasicNameValuePair("detail",params[5]));
            nameValuePairs.add(new BasicNameValuePair("idnum", params[6]));
            nameValuePairs.add(new BasicNameValuePair("time", currentDateandTime));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String st = EntityUtils.toString(response.getEntity());
                Log.v("log_tag", "In the try Loop" + st);

            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return "Successfully Loaded";

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
        }
    }



    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
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
}
