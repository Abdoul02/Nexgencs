package com.fgtit.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.fgtit.data.CommonFunction;
import com.fgtit.fingermap.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fgtit.data.MyConstants.IMAGE;
import static com.fgtit.data.MyConstants.IMAGE_NAME;
import static com.fgtit.data.MyConstants.IMAGE_PATH;


public class DrawingActivity extends AppCompatActivity {

    DrawingView dv;
    private Paint mPaint;
    String fileName, image;
    boolean isSaved = true;
    boolean drew = false;
    CommonFunction commonFunction = new CommonFunction(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        getMenuInflater().inflate(R.menu.drawing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {

            case R.id.completed:
                if(drew){
                    saveSignature();
                }else{
                    setResult(RESULT_CANCELED);
                    finish();
                }
                return true;

            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSignature() {
        final boolean cachePreviousState = dv.isDrawingCacheEnabled();
        final int backgroundPreviousColor = dv.getDrawingCacheBackgroundColor();
        dv.setDrawingCacheEnabled(true);
        dv.setDrawingCacheBackgroundColor(0xFFFFFF);
        final Bitmap drawing = dv.getDrawingCache();
        dv.setDrawingCacheBackgroundColor(backgroundPreviousColor);
        fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        final String path = Environment.getExternalStorageDirectory().toString() + "/signature/";
        OutputStream fOut = null;
        File file = new File(path, fileName + ".png");
        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch (Exception e) {
            isSaved = false;
            Log.e("log", e.getCause() + e.getMessage());
            commonFunction.showToast("Error Creating file");
        }

        try {
            fOut = new FileOutputStream(file);
        } catch (Exception e) {
            isSaved = false;
            Log.e("log", e.getCause() + e.getMessage());
            commonFunction.showToast("Error Creating file");
        }

        if (dv.getDrawingCache() == null) {
            isSaved = false;
            commonFunction.showToast("Unable to get drawing cache");
        }

        drawing.compress(Bitmap.CompressFormat.PNG, 50, fOut);
        dv.setDrawingCacheEnabled(cachePreviousState);
        try {
            fOut.flush();
            fOut.close();
            dv.invalidate();
        } catch (IOException e) {
            Log.e("log", e.getCause() + e.getMessage());
            commonFunction.showToast("Error Creating file");
        }

        if (isSaved) {
            String ImagePath = path + fileName + ".png";
            goBack(ImagePath, fileName);
        }
    }

    private void goBack(String path, String name) {
        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 50, bao);
        byte[] ba = bao.toByteArray();
        image = Base64.encodeToString(ba, Base64.DEFAULT);
        Intent data = new Intent();
        data.putExtra(IMAGE, image);
        data.putExtra(IMAGE_NAME, name);
        data.putExtra(IMAGE_PATH, path);
        setResult(RESULT_OK, data);
        finish();
    }

    public class DrawingView extends View {

        public int width;
        public int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context = c;
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
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(circlePath, circlePaint);
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
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
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
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            drew = true;
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
