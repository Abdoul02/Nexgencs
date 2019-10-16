package com.fgtit.fingermap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fgtit.app.ActivityList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraExActivity extends Activity implements Callback, OnClickListener, AutoFocusCallback
{
    SurfaceView mySurfaceView;
    SurfaceHolder holder;
    Camera myCamera;
    byte[] imgdata =null;
    private boolean af;
    private boolean iscapture=false;
    private boolean isflashlight=false;
    TextView capture_top_hint;

    private String id="";
    private String fullname;



	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_captureex);

        mySurfaceView = findViewById(R.id.capture_preview_view);
        holder = mySurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        final Button btnCancel= findViewById(R.id.capture_button_cancel);
        btnCancel.setOnClickListener(this);

        final ImageView imageFlash= findViewById(R.id.capture_flashlight);
        imageFlash.setOnClickListener(this);

        final ImageView imageCapture= findViewById(R.id.capture_scan_photo);
        imageCapture.setOnClickListener(this);

        capture_top_hint= findViewById(R.id.capture_top_hint);

        Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
        id=bundle.getString("id");
        getFileName(id);
	}

	@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
	private String getFileName(String name) {
		//Changing directory from fgtit to NexgenCS
		String saveDir="/sdcard/fgtit";
		File dir = new File(saveDir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		fullname = saveDir + "/"+ name +".jpg";
		return fullname;
	}

    PictureCallback jpeg = new PictureCallback() {
    	@Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	imgdata = data;
        	savephoto();

        	Intent resultIntent = new Intent();
        	resultIntent.putExtra("id",id);
        	resultIntent.putExtra("photo", imgdata);

        	CameraExActivity.this.setResult(3,resultIntent);
        	CameraExActivity.this.finish();
        }
    };

    public void savephoto(){
        if(imgdata!=null){
            try{
            	Matrix matrix = new Matrix();
                Bitmap bm = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);
                if(ActivityList.getInstance().IsUseNFC){
                	matrix.preRotate(270);
                }else{
                	matrix.preRotate(270);
                }
                Bitmap nbm=Bitmap.createBitmap(bm ,0,0, bm .getWidth(), bm .getHeight(),matrix,true);

                    File file = new File(fullname);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    nbm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    bos.flush();
                    bos.close();
            }catch(Exception e){
                    e.printStackTrace();
            }
        }else{
        	imgdata = null;
        }
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub
		if(success)
        {
			//myCamera.takePicture(null, null, jpeg);
			/*
			takeCameraBtn.setVisibility(View.INVISIBLE);
			takephoto_text.setVisibility(View.VISIBLE);
			re_take_btn.setVisibility(View.VISIBLE);
			save_btn.setVisibility(View.VISIBLE);
			*/
        }

	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.capture_scan_photo:
			//myCamera.autoFocus(this);
			if(!iscapture){
				capture_top_hint.setText(R.string.capture_caphint);
				iscapture=true;
				myCamera.takePicture(null, null, jpeg);
			}
			break;
		case R.id.capture_button_cancel:
			finish();
			break;
		case R.id.capture_flashlight:{
				if(isflashlight){
					Camera.Parameters parameter = myCamera.getParameters();
					parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					myCamera.setParameters(parameter);
					isflashlight=false;
				}else{
					Camera.Parameters parameter = myCamera.getParameters();
					parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					myCamera.setParameters(parameter);
					isflashlight=true;
				}
			}
			break;
		}
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	//capture_top_hint.setText(String.valueOf(event.getX())+","+String.valueOf(event.getY()));
    	if(!iscapture){
    		if(event.getY()>160.0&&event.getY()<800.0){
    			if (event.getAction() == MotionEvent.ACTION_DOWN) {
    				myCamera.autoFocus(this);
    				af=true;
    			}
    			if (event.getAction() == MotionEvent.ACTION_UP && (af ==true)) {
    				capture_top_hint.setText(R.string.capture_caphint);
    				iscapture=true;
    				myCamera.takePicture(null, null, jpeg);
    				af =false;
    			}
    		}
    	}
        return true;
    }

	public void invalidateDrawable(Drawable arg0) {
		// TODO Auto-generated method stub

	}

	public void scheduleDrawable(Drawable arg0, Runnable arg1, long arg2) {
		// TODO Auto-generated method stub

	}

	public void unscheduleDrawable(Drawable arg0, Runnable arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		myCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if(myCamera == null){
            myCamera = Camera.open();
            Camera.Parameters params = myCamera.getParameters();
            try {
                params.setPictureFormat(PixelFormat.JPEG);
                params.setPreviewSize(480,320);
                params.setPictureSize(540,540);

                myCamera.setParameters(params);
                if(ActivityList.getInstance().IsUseNFC){
                	myCamera.setDisplayOrientation(270);
                }else{
                	myCamera.setDisplayOrientation(270);
                }
                myCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
            	e.printStackTrace();
            }
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		myCamera.stopPreview();
        myCamera.release();
        myCamera = null;
	}


}
