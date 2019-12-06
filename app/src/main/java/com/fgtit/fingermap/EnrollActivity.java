package com.fgtit.fingermap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import com.fgtit.models.User;
import com.fgtit.utils.ToastUtil;
import com.fgtit.utils.ExtApi;
import com.fgtit.app.ActivityList;
import com.fgtit.data.GlobalData;
import com.fgtit.data.UserItem;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.fpi.MtRfid;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.fpi.MtGpio;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.scanner.CaptureActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.AsyncFingerprint.OnRegModelListener;
import android_serialport_api.AsyncFingerprint.OnUpCharListener;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortManager;

public class EnrollActivity extends AppCompatActivity {
	
	private EditText editText1,editText2,editText6,editText7,editText8,editText9,edtCard,edtID,photo;
	private TextView text1,text2,text3;
	private ImageView imgPhoto,imgFinger1,imgFinger2;

	//updating the information
	int id_To_Update = 0;
	DBHandler myHandler;
	User user;
	
	private byte[] jpgbytes=null;
	
    private byte[] model1=new byte[512];
	private byte[] model2=new byte[512];
	private boolean isenrol1=false;
	private boolean isenrol2=false;
	private int savecount=0;
	
	private ImageView fpImage;
	private TextView  tvFpStatus;
	private AsyncFingerprint vFingerprint;
	private Dialog fpDialog;
	ProgressDialog saveDialog;
	private int	iFinger=0;
	private boolean	bIsUpImage=true;
	private int count;
	private boolean bcheck=false;
	private int mDeviceType = 0;
	
	//Barcode
	private SerialPort mSerialPort = null;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private byte[] databuf=new byte[1024];
	private int datasize=0;
	private int soundIda;
	private SoundPool soundPool;
	
	private Timer TimerBarcode=null; 
    private TimerTask TaskBarcode=null; 
    private Handler HandlerBarcode;
	
    //RFID
    private Timer TimerCard=null; 
    private TimerTask TaskCard=null; 
    private Handler HandlerCard;
    private int	rfidtype=0;
	private MtRfid rfid = null;
    
    //NFC
    private NfcAdapter nfcAdapter;       
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    
    UserItem person = new UserItem();
    public String CardSN="";
	
    private Spinner spin1,spin2;

	private boolean bIsCancel = false;
	private boolean bCapture = false;

	private String useId;
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enroll);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		
		editText1=(EditText)findViewById(R.id.editText1);
		editText2=(EditText)findViewById(R.id.editText2);
		editText6=(EditText)findViewById(R.id.editText6);
		editText7=(EditText)findViewById(R.id.editText7);
		editText8=(EditText)findViewById(R.id.editText8);
		editText9=(EditText)findViewById(R.id.editText9);
		edtCard=(EditText)findViewById(R.id.edtCard);
		photo = (EditText)findViewById(R.id.editText5);
		edtID = (EditText)findViewById(R.id.edtUID);
		
		text1=(TextView)findViewById(R.id.textView3);
		text2=(TextView)findViewById(R.id.textView4);
		text3=(TextView)findViewById(R.id.textView5);

		saveDialog = new ProgressDialog(this);
		saveDialog.setMessage("Saving. Please wait...");
		saveDialog.setCancelable(false);
		
		imgPhoto=(ImageView)findViewById(R.id.imageView1);
		imgPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EnrollActivity.this, CameraExActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("id","1");
				intent.putExtras(bundle);
				startActivityForResult(intent,0);
			}
		});
		
		imgFinger1=(ImageView)findViewById(R.id.imageView2);
		imgFinger1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				FPDialog(1);
			}
		});
		
		imgFinger2=(ImageView)findViewById(R.id.imageView3);
		imgFinger2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				FPDialog(2);
			}
		});
		
		final ImageView imgBardcode1d=(ImageView)findViewById(R.id.imageView4);
		imgBardcode1d.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				ToastUtil.showToastTop(EnrollActivity.this,"Please sweep Barcode...");
				BarcodeOpen();
			}
		});
		
		final ImageView imgBardcode2d=(ImageView)findViewById(R.id.imageView5);
		imgBardcode2d.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnrollActivity.this, CaptureActivity.class);
				startActivityForResult(intent,0);
			}
		});
		
		final ImageView imgCard=(ImageView)findViewById(R.id.imageView6);
		imgCard.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				ToastUtil.showToastTop(EnrollActivity.this,"Please put the card...");
				ReadCardSn();
			}
		});



		

		spin1=(Spinner)findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource( this, R.array.us1_array, android.R.layout.simple_spinner_item); 
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin1.setAdapter(adapter1);
		spin1.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
			public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){ 
				person.type=pos;
			}

			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
			    //nothing to do 
			} 
		});
		spin1.setSelection(1);
						
		//ʶ������
		spin2=(Spinner)findViewById(R.id.spinner2); 
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource( this, R.array.us2_array, android.R.layout.simple_spinner_item); 
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spin2.setAdapter(adapter2);
		spin2.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override 
		    public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3){ 
				//person.ident=pos;
		    }

			@Override 
			public void onNothingSelected(AdapterView<?> arg0) {  
		    } 
		});
		spin2.setSelection(0);

		//getting the bundle data from userlist
		Bundle extras = getIntent().getExtras();
		myHandler = new DBHandler(this);

		if (extras != null) {
			String Value = extras.getString("uid");

			if (Value != null) {

				//means this is the view part not the add contact part.
				Cursor rs = myHandler.getUser(Value);
				//id_To_Update = Value;
				rs.moveToFirst();
				String nam,idNumber,fing1,fing2,card;
				int costcenterId,shiftId;
				useId = rs.getString(rs.getColumnIndex("userId"));
				idNumber = rs.getString(rs.getColumnIndex("idNum"));
				nam = rs.getString(rs.getColumnIndex("name"));
				fing1 = rs.getString(rs.getColumnIndex("finger1"));
				fing2 = rs.getString(rs.getColumnIndex("finger2"));
				costcenterId = rs.getInt(rs.getColumnIndex("costCenterId"));
				shiftId = rs.getInt(rs.getColumnIndex("shifts_id"));
				card = rs.getString(rs.getColumnIndex("card"));

				if (!rs.isClosed()) {
					rs.close();
				}

				editText2.setText(nam);
				editText1.setText(idNumber);
				editText1.setClickable(false);
				editText1.setEnabled(false);
				edtID.setText(useId);
				//photo.setText(String.valueOf(costcenterId) + " ShiftId= "+String.valueOf(shiftId));
				if(fing1 != null && fing1.length() >=512){

					editText6.setText("registered");
				}

				if(fing2 != null && fing2.length() >=512){

					editText7.setText("registered");
				}

				if(card != null && card.length() > 0){
					edtCard.setText(card);
				}


			}

		}

		
		soundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
        soundIda = soundPool.load(this, R.raw.dong, 1);
        
        //Card
        InitReadCard();
        //Barcode
        //openSerialPort();
		vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
	}

	private void workExit() {
		if (SerialPortManager.getInstance().isOpen()) {
			bIsCancel = true;
			SerialPortManager.getInstance().closeSerialPort();
			CloseReadCard();
			BarcodeClose();

			//if(fpDialog.isShowing()){
			//	fpDialog.cancel();
			//}

			this.finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(resultCode){
		case 1:{ 
				Bundle bl= data.getExtras();
				String barcode=bl.getString("barcode");
				editText9.setText(barcode);
		 	}
			break;
		case 2:
			break;
		case 3:{
				Bundle bl= data.getExtras();
				String id=bl.getString("id");
				Toast.makeText(EnrollActivity.this, "Pictures Finish", Toast.LENGTH_SHORT).show();
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
		                imgPhoto.setImageBitmap(bitmap);
		                
		            }catch(Exception e){  
		            }
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data); 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.enroll, menu);
		return true;
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){  
	    	//SerialPortManager.getInstance().closeSerialPort();
			workExit();
	    	return true;  
	    }
	    return super.onKeyDown(keyCode, event);  
	} 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case android.R.id.home:			
			CloseReadCard();
			//SerialPortManager.getInstance().closeSerialPort();
			workExit();
			this.finish();
			return true;
		case R.id.action_save:{

			user = new User();


				if(CheckInputData(1)){					
				/*	person.id=(editText1.getText().toString());
					person.name=(editText2.getText().toString());
					if(isenrol1)
						person.template1=ExtApi.BytesToBase64(model1,model1.length);
					if(isenrol2)
						person.template2=ExtApi.BytesToBase64(model2,model2.length);
					if(jpgbytes!=null)
						person.photo=ExtApi.BytesToBase64(jpgbytes,jpgbytes.length);
					if(CardSN.length()>4)
						person.cardsn=(CardSN);
					else
						person.cardsn=("null");*/

					Cursor c = myHandler.getUser(useId);
					c.moveToFirst();

					//updating Sqlite
					user.setuId(Integer.parseInt(useId));
					user.setIdNum(editText1.getText().toString());
					user.setuName(editText2.getText().toString());
					if(isenrol1) {
						user.setFinger1(ExtApi.BytesToBase64(model1, model1.length));
					}
					else{
						user.setFinger1(c.getString(c.getColumnIndex("finger1")));}
					if(isenrol2) {
						user.setFinger2(ExtApi.BytesToBase64(model2, model2.length));
					}
					else{
						user.setFinger2(c.getString(c.getColumnIndex("finger2")));}

					if(edtCard.length() > 4){
						user.setCard(edtCard.getText().toString());
					}else{
						user.setCard(c.getString(c.getColumnIndex("card")));
					}

					user.setuStatus("no");
					myHandler.update(user);
					//saveInfo(user.getuName(), user.getFinger1(), user.getFinger2(),user.getIdNum());
					Toast.makeText(EnrollActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
					CloseReadCard();
					SerialPortManager.getInstance().closeSerialPort();
					finish();
				}
			}
			return true;
	/*	case R.id.action_make:{
				//if(CheckInputData(0))
				{
					if(!isenrol1){
						Toast.makeText(EnrollActivity.this, "Please Input Template One", Toast.LENGTH_SHORT).show();
						return true;
					}
					if(!isenrol2){
						Toast.makeText(EnrollActivity.this, "Please Input Template Two", Toast.LENGTH_SHORT).show();
						return true;
					}
					byte[] databuf=new byte[1024];
					int size=1024;
					System.arraycopy(model1,0, databuf, 0, 512);
					System.arraycopy(model2,0, databuf, 512, 512);
					
					//MainActivity.btReader.WriteCard(databuf,size);
				}
			}
			return true;  */
		}
		return super.onOptionsItemSelected(item);
	}

			
	private boolean CheckInputData(int type){
		int len=editText1.getText().toString().length();


		len=editText2.getText().toString().length();
		if(len<=0){
			Toast.makeText(EnrollActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
			return false;
		}


		return true;
	}
		
	//ָ�ƵǼ�
	private void FPDialog(int i){
		iFinger = i;
		Builder builder = new Builder(EnrollActivity.this);
		builder.setTitle("Registration fingerprint");
		final LayoutInflater inflater = LayoutInflater.from(EnrollActivity.this);
		View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
		fpImage = (ImageView) vl.findViewById(R.id.imageView1);
		tvFpStatus = (TextView) vl.findViewById(R.id.textview1);
		builder.setView(vl);
		builder.setCancelable(false);
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//SerialPortManager.getInstance().closeSerialPort();
				dialog.dismiss();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
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

	//Finger Print Registration
	private void FPInit(){
		//ָ�ƴ���
		vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener() {
			@Override
			public void onGetImageSuccess() {
				if (!bIsCancel) {
					if (bcheck) {
						vFingerprint.FP_GetImage();
					} else {
						if (bIsUpImage) {
							vFingerprint.FP_UpImage();
							tvFpStatus.setText(getString(R.string.txt_fpdisplay));
						} else {
							tvFpStatus.setText(getString(R.string.txt_fpprocess));
							vFingerprint.FP_GenChar(count);
						}
					}
				}
			}

			@Override
			public void onGetImageFail() {
				if (!bIsCancel) {
					if (bcheck) {
						bcheck = false;
						tvFpStatus.setText(getString(R.string.txt_fpplace));
						vFingerprint.FP_GetImage();
						count++;
					} else {
						vFingerprint.FP_GetImage();
					}
				}
			}
		});

		vFingerprint.setOnUpImageListener(new AsyncFingerprint.OnUpImageListener() {
			@Override
			public void onUpImageSuccess(byte[] data) {
				Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
				fpImage.setImageBitmap(image);
				//fpImage.setBackgroundDrawable(new BitmapDrawable(image));
				vFingerprint.FP_GenChar(count);
				tvFpStatus.setText(getString(R.string.txt_fpprocess));
			}

			@Override
			public void onUpImageFail() {
			}
		});

		vFingerprint.setOnGenCharListener(new AsyncFingerprint.OnGenCharListener() {
			@Override
			public void onGenCharSuccess(int bufferId) {
				if (bufferId == 1) {
					bcheck = true;
					tvFpStatus.setText("Please lift finger");
					vFingerprint.FP_GetImage();
				} else if (bufferId == 2) {
					vFingerprint.FP_RegModel();
				}
			}

			@Override
			public void onGenCharFail() {
				tvFpStatus.setText(getString(R.string.txt_fpfail));
			}
		});
		
		vFingerprint.setOnRegModelListener(new OnRegModelListener() {

			@Override
			public void onRegModelSuccess() {
				vFingerprint.FP_UpChar();
				tvFpStatus.setText("Synthetic template success");
			}

			@Override
			public void onRegModelFail() {
				tvFpStatus.setText("Synthetic template failure");
			}
		});

		vFingerprint.setOnUpCharListener(new OnUpCharListener() {

			@Override
			public void onUpCharSuccess(byte[] model) {
				//AdminEditActivity.this.model = model;
				if (iFinger == 1) {
					editText6.setText("Registered");
					System.arraycopy(model, 0, EnrollActivity.this.model1, 0, 512);
					isenrol1 = true;
					//EnrollActivity.this.model1 = model;
				}
				if (iFinger == 2) {
					editText7.setText("Registered");
					System.arraycopy(model, 0, EnrollActivity.this.model2, 0, 512);
					isenrol2 = true;
					//EnrollActivity.this.model2 = model;
				}
				tvFpStatus.setText("Successful registration");

				//SerialPortManager.getInstance().closeSerialPort();
				fpDialog.cancel();
			}

			@Override
			public void onUpCharFail() {
				tvFpStatus.setText("Registration Failed");
			}
		});
		
	}

	private void FPProcess() {
		count = 1;
		tvFpStatus.setText(getString(R.string.txt_fpplace));
		try {
			Thread.currentThread();
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		vFingerprint.FP_GetImage();
	}


	public void saveInfo(String name, String fing1, String fing2, String idN){


		try {
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			String json = "";
			JSONObject jsonObject = new JSONObject();


			jsonObject.accumulate("name", name);
			jsonObject.accumulate("fing1", fing1);
			jsonObject.accumulate("fing2", fing2);
			jsonObject.accumulate("idn", idN);

			json = jsonObject.toString();

			saveDialog.show();
			params.put("save", json);

			client.post("http://www.nexgencs.co.za/api/save.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					System.out.println(response);
					saveDialog.hide();
					Toast.makeText(getApplicationContext(), "save successful", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailure(int statusCode, Throwable error,
									  String content) {
					// TODO Auto-generated method stub
					saveDialog.hide();
					//mydb.insertRecord(idN, name, dat, lat, lon, id, "IN", imei);
					Toast.makeText(getApplicationContext(), "User uploaded.", Toast.LENGTH_LONG).show();

				}
			});


		}catch (Exception e){

			Log.d("InputStream", e.getLocalizedMessage());
		}





	}

	//Barcode registration
	public void BarcodeOpen() {
		if (mDeviceType == 0) {
			MtGpio mt = new MtGpio();
			mt.BCPowerSwitch(true);
			mt.BCReadSwitch(true);
			try {
				Thread.currentThread();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			datasize = 0;
			mt.BCReadSwitch(false);
		} else {
			byte[] cmd = new byte[2];
			cmd[0] = (0x1b);
			cmd[1] = (0x31);
			try {
				mOutputStream.write(cmd);
			} catch (IOException e) {
			}
		}
	}

	public void BarcodeClose() {
		if (mReadThread != null)
			mReadThread.interrupt();
		closeSerialPort();
		mSerialPort = null;
		if (mDeviceType == 0) {
			MtGpio mt = new MtGpio();
			mt.BCReadSwitch(true);
			mt.BCPowerSwitch(false);
		} else {

		}
	}

	public void openSerialPort() {
		try {
			mSerialPort = getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (Exception e) {
		}
	}

	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			String path = "/dev/ttyMT1";
			//int baudrate = 9600;	//1D
			int baudrate = 115200;    //2D
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}
			mSerialPort = new SerialPort();
			if (mSerialPort.getmodel().equals("FP07")) {
				path = "/dev/ttyMT2";
				mDeviceType = 1;
				baudrate = 9600;
			} else {
				path = "/dev/ttyMT1";
				mDeviceType = 0;
			}
			mSerialPort.OpenDevice(new File(path), baudrate, 0, SerialPort.DEVTYPE_UART);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (!isInterrupted()/*true*/) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(EnrollActivity.this, "Read barcodes fail", Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
	}
    
    protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				System.arraycopy(buffer, 0, databuf,datasize,size);					
				datasize=datasize+size;
				if(TimerBarcode==null){
					TimerBarcodeStart();
				}
			}
		});
	}

	@SuppressLint("HandlerLeak")
	public void TimerBarcodeStart() {
		TimerBarcode = new Timer();
		HandlerBarcode = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				TimerBarcodeStop();
				if (datasize > 0) {
					byte tp[] = new byte[datasize];
					System.arraycopy(databuf, 0, tp, 0, datasize);
					editText8.setText(new String(tp));
					soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
					datasize = 0;
				}
				super.handleMessage(msg);
			}
		};
		TaskBarcode = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				HandlerBarcode.sendMessage(message);
			}
		};
		TimerBarcode.schedule(TaskBarcode, 1000, 1000);
	}

	public void TimerBarcodeStop() {
		if (TimerBarcode != null) {
			TimerBarcode.cancel();
			TimerBarcode = null;
			TaskBarcode.cancel();
			TaskBarcode = null;
		}
	}

	public void InitReadCard() {
		if (ActivityList.getInstance().IsUseNFC) {
			nfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (nfcAdapter == null) {
				Toast.makeText(this, "Device does not support NFC!", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			/*if (!nfcAdapter.isEnabled()) {
				Toast.makeText(this, "Enable the NFC function in the system settings!", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}*/

			mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			mFilters = new IntentFilter[]{
					new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
					new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
					new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
		} else {
			if (rfid == null)
				rfid = new MtRfid();
			rfid.RfidInit();
			rfid.SetContext(this);
		}
	}

	public void CloseReadCard() {
		if (ActivityList.getInstance().IsUseNFC) {
		} else {
			TimerCardStop();
			rfid.RfidClose();    //Close
		}
	}

	public void ReadCardSn() {
		if (ActivityList.getInstance().IsUseNFC) {
		} else {
			TimerCardStart();
		}
	}

	//RFID
	@SuppressLint("HandlerLeak")
	public void TimerCardStart() {
		TimerCard = new Timer();
		HandlerCard = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				TimerCardStop();

				int[] sn = new int[8];
				//byte[] cardsn=rfid.IntArrayToByteArray(sn,4);

				//count++;
				if (rfid.RfidGetSn(sn) == 0) {
					String cardstr =/*Integer.toString(count)+":"+*/
							Integer.toHexString(sn[0] & 0xFF).toUpperCase() +
									Integer.toHexString(sn[1] & 0xFF).toUpperCase() +
									Integer.toHexString(sn[2] & 0xFF).toUpperCase() +
									Integer.toHexString(sn[3] & 0xFF).toUpperCase();

					for (int i = 0; i < GlobalData.getInstance().userList.size(); i++) {
						if (GlobalData.getInstance().userList.get(i).cardsn.indexOf(cardstr) >= 0) {
							Toast.makeText(EnrollActivity.this, "Failed,Duplicate registration!", Toast.LENGTH_SHORT).show();
							return;
						}
					}

					edtCard.setText(cardstr);
					CardSN = cardstr;

					int[] buffer = new int[4096];
					switch (rfidtype) {
						case 0:
							break;
						case 1:
							if (rfid.RfidReadFullCard(sn, buffer, 256) == 0) {
								byte[] b = rfid.IntArrayToByteArray(buffer, 256);
								editText1.setText(new String(b));
								Toast.makeText(EnrollActivity.this, "Read card Success", Toast.LENGTH_SHORT).show();
							}
							break;
						case 2: {
							String txt = editText1.getText().toString();
							byte[] b = txt.getBytes();
							int[] ir = rfid.ByteArrayToIntArray(b, b.length);
							for (int i = 0; i < ir.length; i++) {
								buffer[i] = ir[i];
							}
							if (rfid.RfidWriteFullCard(sn, buffer, 256) == 0) {
								Toast.makeText(EnrollActivity.this, "Write card success", Toast.LENGTH_SHORT).show();
							}
						}
						break;
					}
					soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
				}
				super.handleMessage(msg);
			}
		};
		TaskCard = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				HandlerCard.sendMessage(message);
			}
		};
		TimerCard.schedule(TaskCard, 500, 500);
	}

	public void TimerCardStop() {
		if (TimerCard != null) {
			TimerCard.cancel();
			TimerCard = null;
			TaskCard.cancel();
			TaskCard = null;
		}
	}
	
	//NFC
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent(intent);
	}
	
	private void processIntent(Intent intent){
		byte[] sn = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
		String cardstr=/*Integer.toString(count)+":"+*/
				Integer.toHexString(sn[0]&0xFF).toUpperCase()+
				Integer.toHexString(sn[1]&0xFF).toUpperCase()+
				Integer.toHexString(sn[2]&0xFF).toUpperCase()+
				Integer.toHexString(sn[3]&0xFF).toUpperCase();
		edtCard.setText(cardstr);
		CardSN=cardstr;
		//soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
	}  
	
	@Override
	public void onPause() {
		super.onPause();
		if(ActivityList.getInstance().IsUseNFC){
			if (nfcAdapter != null)
				nfcAdapter.disableForegroundDispatch(this);
		}

	}
    
	@Override
	protected void onResume() {
		super.onResume();
		if(ActivityList.getInstance().IsUseNFC){
			if (nfcAdapter != null)
				nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,null);
		}
	} 
}
