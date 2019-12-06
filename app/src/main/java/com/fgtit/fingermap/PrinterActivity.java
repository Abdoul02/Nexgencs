package com.fgtit.fingermap;

import java.util.Timer;
import java.util.TimerTask;

import com.fgtit.device.BluetoothReader;
import com.fgtit.printer.PrinterApi;
import com.fgtit.printer.PrinterCmd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PrinterActivity extends AppCompatActivity {
	
	private static final int RESET_BT_HANDLER = 0;
	private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter = null;    
    public static BluetoothReader btReader=new BluetoothReader();
    private String btAddress="";
    
    private Timer startTimer; 
    private TimerTask startTask; 
    private Handler startHandler;
    
	private TextView tv1;
	private EditText et1;
	private RadioGroup group1,group2;
	private int fontsize=0;
	private int fontstyle=0;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		tv1=(TextView)findViewById(R.id.textView1);
		et1=(EditText)findViewById(R.id.editText1);
		tv1.setText("Printer Status\n");
		et1.setText("\tPrinter Test\n");
		
		RadioGroup group1 = (RadioGroup)this.findViewById(R.id.radioGroup1);
		group1.check(R.id.radio0);
		group1.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				int radioButtonId = arg0.getCheckedRadioButtonId();
				switch(radioButtonId){
				case R.id.radio0:	fontsize=0;	break;
				case R.id.radio1:	fontsize=1;	break;
				case R.id.radio2:	fontsize=2;	break;
				case R.id.radio3:	fontsize=3;	break;
				}
			}
		});
		
		RadioGroup group2 = (RadioGroup)this.findViewById(R.id.radioGroup2);
		group2.check(R.id.radio0);
		group2.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				int radioButtonId = arg0.getCheckedRadioButtonId();
				switch(radioButtonId){
				case R.id.radio0:	fontstyle=0x00;	break;
				case R.id.radio1:	fontstyle=0x08;	break;
				case R.id.radio2:	fontstyle=0x80;	break;
				case R.id.radio3:	fontstyle=0x400;	break;
				}
			}
		});
		
		btReader.SetMessageHandler(btHandler);
		
		final Button btn1=(Button)findViewById(R.id.button1);
		btn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				byte pbcode[]=PrinterApi.TestPage();
				btReader.PrintCmd(pbcode,pbcode.length);
			}
		});
		
		final Button btn2=(Button)findViewById(R.id.button2);
		btn2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				byte pbcode[]=PrinterApi.FeedLine();
				btReader.PrintCmd(pbcode,pbcode.length);
			}
		});
		
		final Button btn3=(Button)findViewById(R.id.button3);
		btn3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				byte pbcode[]=PrinterApi.Reset();
				btReader.PrintCmd(pbcode,pbcode.length);
			}
		});
		
		final Button btn4=(Button)findViewById(R.id.button4);
		btn4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				byte pbcode[]=PrinterApi.SetBarcode("012345",0,PrinterCmd.Constant.BARCODE_TYPE_CODE39, 2,120, 0,2);
				btReader.PrintCmd(pbcode,pbcode.length);
			}
		});
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      	if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is invalid!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
      	
      	SharedPreferences sp;
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		btAddress =sp.getString("device","");        
        btReader.SetMessageHandler(btHandler);
	}
	
	private void AddStatus(String s){
		tv1.setText(tv1.getText()+"\n"+s);
	}

	@Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (btReader.mReaderService == null) {
            	btReader.Setup(this);
            	TimerStart();
            }
        }
	}
	
	@Override  
    protected void onDestroy() {
		btReader.Stop();
    	super.onDestroy();  
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		btReader.Start();
	}
		
	private final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what){
        	case BluetoothReader.MSG_DEVSTATE:
        		switch(msg.arg1){
        		case BluetoothReader.DEVSTATE_NONE:
        			Toast.makeText(PrinterActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
        			break;
        		case BluetoothReader.DEVSTATE_CONNECTING:
        			Toast.makeText(PrinterActivity.this, "Connecting ...", Toast.LENGTH_SHORT).show();
        			break;
        		case BluetoothReader.DEVSTATE_CONNECTED:
        			Toast.makeText(PrinterActivity.this, "Connected OK!", Toast.LENGTH_SHORT).show();
        			break;
        		case BluetoothReader.DEVSTATE_UNCONNECT:
        			Toast.makeText(PrinterActivity.this, "Unable to connect device!", Toast.LENGTH_SHORT).show();
        			break;
        		case BluetoothReader.DEVSTATE_LOSTCONNECT:
        			Toast.makeText(PrinterActivity.this, "Device connection was lost!", Toast.LENGTH_SHORT).show();
        			break;
        		}        		
        		break;
        	case BluetoothReader.CMD_PRINTCMD:
        	case BluetoothReader.CMD_PRINTTEXT:
        		if(msg.arg1==1)
        			AddStatus("Print OK");
        		else
        			AddStatus("Print Fail");
        		break;        	
        	}
        }
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case RESET_BT_HANDLER:
        	btReader.SetMessageHandler(btHandler);
        	break;
        case REQUEST_CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK) {
            	btAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
                btReader.mReaderService.connect(device);
                
                SharedPreferences sp;
        		sp = PreferenceManager.getDefaultSharedPreferences(this);
        		Editor edit=sp.edit();
        		edit.putString("device",btAddress);
        		edit.commit();
        		//address = sp.getString("device","");
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	btReader.Setup(this);
            	
        		if(btAddress.length()>2){
        			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
        			btReader.mReaderService.connect(device);
        		}
        		
            } else {
                //Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
	
	public void TimerStart()
    {
		startTimer = new Timer(); 
		startHandler = new Handler() { 
            @SuppressLint("HandlerLeak")
			@Override 
            public void handleMessage(Message msg) { 
            	
            	TimeStop();
            	if(btReader.mReaderService!=null){
            		if(btAddress.length()>2){
            			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
            			btReader.mReaderService.connect(device);
            		}
            	}
            	
                super.handleMessage(msg); 
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
        startTimer.schedule(startTask, 2000, 2000); 
    }
    
    public void TimeStop()
    {
    	if (startTimer!=null)
		{  
    		startTimer.cancel();  
    		startTimer = null;  
    		startTask.cancel();
    		startTask=null;
		}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.printer, menu);
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
			this.finish();
			return true;
		case R.id.action_btset:{
				btReader.SetMessageHandler(btHandler);
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
            	startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
			break;
		case R.id.action_print:{
				//byte pbString[] = PrinterApi.TextOut(et1.getText().toString());
				//btReader.PrintCmd(pbString,pbString.length);
			/*
				switch(group1.getCheckedRadioButtonId()){
				case R.id.radio0:	fontsize=0;	break;
				case R.id.radio1:	fontsize=1;	break;
				case R.id.radio2:	fontsize=2;	break;
				case R.id.radio3:	fontsize=3;	break;
				}
				switch(group2.getCheckedRadioButtonId()){
				case R.id.radio0:	fontstyle=0x00;	break;
				case R.id.radio1:	fontstyle=0x08;	break;
				case R.id.radio2:	fontstyle=0x80;	break;
				case R.id.radio3:	fontstyle=0x400;	break;
				}
			*/	
				byte pbcode[]=PrinterApi.TextOutEx(et1.getText().toString(),0,fontsize,fontsize,0,fontstyle);
				btReader.PrintCmd(pbcode,pbcode.length);
			}
			return true;
			/*
		case R.id.action_printtest:
			byte pbcode[]=PrinterApi.TextOutEx("FGTIT PRINTER\n",0,1,1,0,0x08);
			btReader.PrintCmd(pbcode,pbcode.length);
			
			
			//byte pbcode[]=PrinterApi.SetQRcode("http://www.fgtit.com",2, 2);
			//byte pbcode[]=PrinterApi.SetBarcode("012345",0,PrinterCmd.Constant.BARCODE_TYPE_CODE39, 2,120, 0,2);
			//Bitmap bmp = ExtApi.LoadBitmap(getResources(), R.drawable.msign);
			//byte pbcode[]=PrinterApi.PrintPicture(bmp,64,0);
			//btReader.PrintCmd(pbcode,pbcode.length);

			String txt="Size:"+String.valueOf(pbcode.length)+">>";
			for(int i=0;i<pbcode.length;i++){
				txt=txt+Integer.toHexString(pbcode[i]&0xFF).toUpperCase()+" ";
			}
			tv1.setText(txt);
			
			return true;
			*/
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
