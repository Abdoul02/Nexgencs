package com.fgtit.fingermap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.zyapi.CommonApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fgtit.fpengine.fpdevice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;


import com.fgtit.models.DashboardView;
import com.fgtit.models.HighlightCR;

public class Alcohol extends Activity {


    private static final int PERMISSION_REQUEST_CODE = 100;
    private fpdevice fpdev = new fpdevice();
    private CommonApi api = new CommonApi();

    private static boolean isopening = false;
    private static boolean isworking = false;

    // private TextView tvStatus = null;
    public static final String EXTRA_VALUE = "com.fgtit.fingermap.EXTRA_VALUE";


    private PendingIntent mPermissionIntent;
    private IntentFilter filter;
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            OpenDevice();
                        } else {
                            // tvStatus.setText("");
                        }
                    } else {
                        //tvStatus.setText("");
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                //final UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                //tvStatus.setText("");
                requestPermission();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                //final UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                // tvStatus.setText("");
                CloseDevice();
            }
        }
    };
    private UsbManager usbManager;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEpOut;
    private UsbEndpoint usbEpIn;
    private byte[] number;
    private SoundPool soundPool;
    private int sound;
    private boolean soundflag;
    private Button mBtnClear;
    private TextView maxText,txtStatus;
    private int max;
    private DashboardView dashboardView;
    private Handler handler1;
    private Runnable runnable;
    int maxValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alcohol);


        maxText = (TextView) findViewById(R.id.max);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        Button mBtnClear = (Button) findViewById(R.id.clear);
        dashboardView = (DashboardView) findViewById(R.id.dashboard_view);
        Button btn_ok = findViewById(R.id.btn_ok);
        Button btn_cancel = findViewById(R.id.btn_cancel);


        List<HighlightCR> highlight1 = new ArrayList<>();

        highlight1.add(new HighlightCR(150, 10, Color.parseColor("#33CC4C")));
        highlight1.add(new HighlightCR(160, 10, Color.parseColor("#33CC37")));
        highlight1.add(new HighlightCR(170, 10, Color.parseColor("#3ACC33")));
        highlight1.add(new HighlightCR(180, 10, Color.parseColor("#50CC33")));
        highlight1.add(new HighlightCR(190, 10, Color.parseColor("#5ECC33")));
        highlight1.add(new HighlightCR(200, 10, Color.parseColor("#70CC33")));
        highlight1.add(new HighlightCR(210, 10, Color.parseColor("#82CC33")));
        highlight1.add(new HighlightCR(220, 10, Color.parseColor("#91CC33")));
        highlight1.add(new HighlightCR(230, 10, Color.parseColor("#9FCC33")));
        highlight1.add(new HighlightCR(240, 10, Color.parseColor("#AACC33")));
        highlight1.add(new HighlightCR(250, 10, Color.parseColor("#B8CC33")));
        highlight1.add(new HighlightCR(260, 10, Color.parseColor("#CCC733")));
        highlight1.add(new HighlightCR(270, 10, Color.parseColor("#CCB833")));
        highlight1.add(new HighlightCR(280, 10, Color.parseColor("#CCA633")));
        highlight1.add(new HighlightCR(290, 10, Color.parseColor("#CC9433")));
        highlight1.add(new HighlightCR(300, 10, Color.parseColor("#CC8633")));
        highlight1.add(new HighlightCR(310, 10, Color.parseColor("#CC7733")));
        highlight1.add(new HighlightCR(320, 10, Color.parseColor("#CC6D33")));
        highlight1.add(new HighlightCR(330, 10, Color.parseColor("#CC5E33")));
        highlight1.add(new HighlightCR(340, 10, Color.parseColor("#CC5033")));
        highlight1.add(new HighlightCR(350, 10, Color.parseColor("#CC4133")));
        highlight1.add(new HighlightCR(360, 10, Color.parseColor("#CC3A33")));
        highlight1.add(new HighlightCR(370, 10, Color.parseColor("#CC3333")));
        highlight1.add(new HighlightCR(380, 10, Color.parseColor("#CC3333")));
        dashboardView.setStripeHighlightColorAndRange(highlight1);

        fpdev.SetInstance(this);
        fpdev.SetUpImage(true);
        setFpIoState(true);

        openGpio();
        Log.d("MainActivity", "isopening1:" + isopening);

        number = new byte[4];
        handler1 = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                max = number[1];
                int i = fpdev.GetSensorVal(number);
                Log.d("Alcohol", "i:" + i);
                Log.d("Alcohol", "number[0]:" + number[0]);
                Log.d("Alcohol", "number[1]:" + number[1]);
                Log.d("Alcohol", "number[2]:" + number[2]);
                Log.d("Alcohol", "number[3]:" + number[3]);
                if (number[1] >= 80) {
                    soundPool.play(sound, 1.0f, 1.0f, 1, 0, 1.0f);
                }
                dashboardView.setRealTimeValue(number[1], true, 100);

                if (number[1] > max) {
                    maxText.setText(String.valueOf(number[1]));
                }

                handler1.postDelayed(this, 2000);
                maxValue = Integer.parseInt(maxText.getText().toString());
                if(maxValue > 0){
                    maxText.setTextColor(Color.parseColor("#BF360C"));
                }
            }


        };
        handler1.postDelayed(runnable, 1000); //Timer


        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        sound = soundPool.load(this, R.raw.music1, 1);

        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxText.setText("0");
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = maxText.getText().toString();
                Intent data = new Intent();
                data.putExtra(EXTRA_VALUE,value);
                setResult(RESULT_OK,data);
                finish();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }

    private void openGpio() {
        if (api.getGpioIn(14) == 1) {
            CloseDevice();
            SystemClock.sleep(100);
            OpenDevice();
        } else {
            //8 LCD
            //FINGER_POWEREN
            api.setGpioMode(54, 0);
            api.setGpioDir(54, 1);
            api.setGpioOut(54, 1);
            //HOST_POWER_EN
            api.setGpioMode(53, 0);
            api.setGpioDir(53, 1);
            api.setGpioOut(53, 1);
        }
    }

    public void requestPermission() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            return;
        }
        Log.d("MainActivity", "usbManager:" + usbManager);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> devlist = usbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator();
        while (deviter.hasNext()) {
            UsbDevice tmpusbdev = deviter.next();
            if ((tmpusbdev.getVendorId() == 0x0453) && (tmpusbdev.getProductId() == 0x9005)) {
                usbDevice = tmpusbdev;
                break;
            } else if ((tmpusbdev.getVendorId() == 0x2009) && (tmpusbdev.getProductId() == 0x7638)) {
                usbDevice = tmpusbdev;
                break;
            } else if ((tmpusbdev.getVendorId() == 0x2109) && (tmpusbdev.getProductId() == 0x7638)) {
                usbDevice = tmpusbdev;
                break;
            } else if ((tmpusbdev.getVendorId() == 0x0483) && (tmpusbdev.getProductId() == 0x5720)) {
                usbDevice = tmpusbdev;
                break;
            }
        }


        if (usbDevice != null) {
            if (!usbManager.hasPermission(usbDevice)) {
                synchronized (mUsbReceiver) {
                    Log.d("MainActivity", "usbManager:" + usbManager);
                    usbManager.requestPermission(usbDevice, mPermissionIntent);

                    Log.d("MainActivity", "" + usbDevice.getDeviceName());
                    usbDeviceConnection = usbManager.openDevice(usbDevice);
                    Log.d("MainActivity", "USB:" + usbDeviceConnection);
                }
            } else {
                OpenDevice();
            }
        }
    }

    private void setFpIoState(boolean isOn) {
        int state = 0;
        if (isOn) {
            state = 1;

        } else {
            state = 0;
        }
        Intent i = new Intent("ismart.intent.action.fingerPrint_control");
        i.putExtra("state", state);
        sendBroadcast(i);
    }


    private void CloseDevice() {
        if (isopening) {
            fpdev.CloseDevice();
            Toast.makeText(this, "Alcohol tester is disconnected", Toast.LENGTH_SHORT).show();
            txtStatus.setText("Tester disconnected");
            number[1] = 0;
            dashboardView.setRealTimeValue(number[1], true, 10);
            soundPool.play(sound, 0.0f, 0.0f, 0, 0, 0.0f);
            isopening = false;
            isworking = false;
        }

    }

    private void OpenDevice() {
        if (isopening) {
            fpdev.CloseDevice();
            isopening = false;
            isworking = false;
        }
        Log.d("MainActivity", "fpdev.OpenDevice():" + fpdev.OpenDevice());
        switch (fpdev.OpenDevice()) {
            case 0:
                isopening = true;
                Toast.makeText(this, "Has opened the alcohol tester", Toast.LENGTH_SHORT).show();
                txtStatus.setText("Tester Status: Ready");
                txtStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case -1:
                Toast.makeText(this, "Connection tester failed", Toast.LENGTH_SHORT).show();
                break;
            case -2:
                Toast.makeText(this, "Version is due", Toast.LENGTH_SHORT).show();
                break;
            case -3:
                Toast.makeText(this, "Failed to open the tester", Toast.LENGTH_SHORT).show();
                txtStatus.setText("Please connect tester");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        fpdev.CloseDevice();
        if (mUsbReceiver != null) {
            unregisterReceiver(mUsbReceiver);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openGpio();
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);

        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(mUsbReceiver, filter);
        OpenDevice();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            CloseDevice();
            //8 LCD
            api.setGpioMode(54, 0);
            api.setGpioDir(54, 1);
            api.setGpioOut(54, 0);
            //HOST_POWER_EN
            api.setGpioMode(53, 0);
            api.setGpioDir(53, 1);
            api.setGpioOut(53, 0);
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void checkPermission() {
        /**
         *
         */
        boolean isAllGranted = checkPermissionAllGranted(
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                }
        );
        if (isAllGranted) {
            return;
        }

        /**
         *
         */
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                }, PERMISSION_REQUEST_CODE
        );
    }

    /**
     *
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                toast("Not granted");
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;


            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
            } else {
                toast("All Not granted");
            }
        }
    }

    public void toast(String content) {
        Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
    }
}
