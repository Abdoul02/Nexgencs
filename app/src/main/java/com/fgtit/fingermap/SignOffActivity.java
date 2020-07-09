package com.fgtit.fingermap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fgtit.models.User;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;
import com.fgtit.app.ActivityList;
import com.fgtit.data.ImageSimpleAdapter;
import com.fgtit.data.RecordItem;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.fpi.MtRfid;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.AsyncFingerprint.OnUpCharListener;
import android_serialport_api.SerialPortManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignOffActivity extends AppCompatActivity {

	private ListView listView1;
	private ArrayList<HashMap<String, Object>> mData1;
	private SimpleAdapter adapter1;

	private ListView listView2;
	private ImageView imgFeed;
	private ArrayAdapter<String> mListArrayAdapter2;

	private TextView tvFpStatus;
	private ImageView fpImage;

	private AsyncFingerprint vFingerprint;
	private boolean bIsUpImage = true;
	private boolean bIsCancel = false;
	private boolean bfpWork = false;

	private Timer startTimer;
	private TimerTask startTask;
	private Handler startHandler;

	//RFID
	private int rfidtype = 0;
	private Timer xTimer = null;
	private TimerTask xTask = null;
	private Handler xHandler;
	private MtRfid rfid = null;

	//NFC
	private NfcAdapter nfcAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	GPSTracker gps;
	//Get Users from SQLite
	private ArrayList<User> empList;
	DBHandler db = new DBHandler(this);
	JobDB mydb = new JobDB(this);
	ProgressDialog prgDialog;
	int test;
	Dialog dialog;

	private static final int REQUEST_ALCOHOL_TEST = 100;
	String alcohol_val = "0";

	String db_user_id_number, db_name, db_longitude, db_latitude, db_date, db_imei, db_status;


	int db_user_id, db_shift_id, db_shift_type, db_cost_center_id;
	public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
	String clockURL = "http://www.nexgencs.co.za/alos/alcohol.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_local);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		this.getSupportActionBar().setBackgroundDrawable(new
				ColorDrawable(Color.parseColor("#020969")));
		gps = new GPSTracker(SignOffActivity.this);
		empList = db.getAllUsers();

		if (checkAndRequestPermissions()) {
			// carry on the normal flow, as the case of  permissions  granted.
		} else {

			showToast("Please ensure all permission are granted");
		}

		//Progress Dialog
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Clocking in. Please wait...");
		prgDialog.setCancelable(false);


		listView1 = (ListView) findViewById(R.id.listView1);
		mData1 = new ArrayList<HashMap<String, Object>>();
		adapter1 = new ImageSimpleAdapter(this, mData1, R.layout.listview_signitem,
				new String[]{"title", "info", "dts", "img"},
				new int[]{R.id.title, R.id.info, R.id.dts, R.id.img});
		listView1.setAdapter(adapter1);

		//AddItem();

		//Person person=MainActivity.personList.get(0);
		//AddPersonItem(person);

		mListArrayAdapter2 = new ArrayAdapter<String>(this, R.layout.list_item);
		listView2 = (ListView) findViewById(R.id.listView2);
		listView2.setAdapter(mListArrayAdapter2);
		imgFeed = (ImageView) findViewById(R.id.imgfeed);

		tvFpStatus = (TextView) findViewById(R.id.textView1);
		tvFpStatus.setText("");
		fpImage = (ImageView) findViewById(R.id.imageView1);
		fpImage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});

		//Card
		InitReadCard();
		ReadCardSn();

		//Fingerprint
		AddStatus(getString(R.string.txt_fpbegin));
		vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
		FPInit();
		FPProcess();
	}

	private void workExit() {
		if (SerialPortManager.getInstance().isOpen()) {
			bIsCancel = true;
			SerialPortManager.getInstance().closeSerialPort();
			CloseReadCard();
			this.finish();
		}
	}

	private void AddPersonItem(User person) {

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		criteria.setCostAllowed(false);
		// get the best provider depending on the criteria
		String provider = locationManager.getBestProvider(criteria, false);

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		Location loc = locationManager.getLastKnownLocation(provider);


		MainActivity.MyLocation mylocalListener = new MainActivity.MyLocation();


		//Loc loc = new Loc();
		RecordItem rs=new RecordItem();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateandTime = sdf.format(new Date());

		final String identifier;
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null)
			identifier = telephonyManager.getDeviceId();
		else
			identifier = "Not available";


		rs.id=person.getIdNum();
		rs.name=person.getuName();
		rs.datetime=currentDateandTime;
		rs.costCenterId = person.getCostCenterId();
		rs.shift_type = person.getShift_type();
		rs.shifts_id = person.getShifts_id();
		if (loc != null) {

			mylocalListener.onLocationChanged(loc);
			rs.lat=String.valueOf(loc.getLatitude());
			rs.lng=String.valueOf(loc.getLongitude());
		}else {
			rs.lat=String.valueOf(gps.getLatitude());
			rs.lng=String.valueOf(gps.getLongitude());

		}

		rs.type="0";
		rs.worktype="Climber";
		rs.linetype="anyValue";
		rs.depttype="Electrical";


		db_user_id = person.getuId();
		db_user_id_number = person.getIdNum();
		db_longitude = rs.lng;
		db_latitude = rs.lat;
		db_date = rs.datetime;
		db_name = rs.name;
		db_status = "OUT";
		db_imei = identifier;
		db_shift_id = person.getShifts_id();
		db_shift_type= person.getShift_type();
		db_cost_center_id = person.getCostCenterId();

		JSONObject postDataParams = new JSONObject();
		try{

			postDataParams.accumulate("dat", currentDateandTime);
			postDataParams.accumulate("status", db_status);
			postDataParams.accumulate("lon", rs.lng);
			postDataParams.accumulate("lat", rs.lat);
			postDataParams.accumulate("id",  person.getuId());
			postDataParams.accumulate("imei",identifier);
			postDataParams.accumulate("alcohol",alcohol_val);


		}catch (Exception e){
			e.printStackTrace();
		}

		try {

			postRequest(postDataParams.toString());
			alcohol_val = "0";

		}catch (IOException e){
			e.printStackTrace();
			mydb.insertRecord(db_user_id_number,db_name,db_date,db_latitude,db_longitude,db_user_id,db_status,db_imei,db_shift_id,db_shift_type,db_cost_center_id);
			alcohol_val = "0";
		}

		//clockOut(person.getuId(), rs.datetime, rs.lng, rs.lat, rs.name, rs.id,rs.costCenterId,rs.shift_type,rs.shifts_id);

		//GlobalData.getInstance().AppendRecord(rs);
		//GlobalData.getInstance().recordList.add(rs);

		//GlobalData.getInstance().AppendLocalRecord(person, 1);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("title", rs.name);
		map.put("info", rs.id);
		map.put("dts", rs.datetime);
		/*if(person.photo.length()>1000)
			map.put("img", ExtApi.Bytes2Bimap(ExtApi.Base64ToBytes(person.photo)));
		else
			map.put("img", ExtApi.LoadBitmap(getResources(),R.drawable.guest)); */
		mData1.add(map);
		listView2.setVisibility(View.GONE);
		imgFeed.setImageResource(R.drawable.green_trans);
		imgFeed.setVisibility(View.VISIBLE);
		adapter1.notifyDataSetChanged();
		ScrollListViewToBottom();
		locationManager.requestLocationUpdates(provider, 300 * 1000, 5, mylocalListener);
	}


	private  boolean checkAndRequestPermissions() {
		int permissionIMEI = ContextCompat.checkSelfPermission(this,
				Manifest.permission.READ_PHONE_STATE);
		int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
		int locationCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

		List<String> listPermissionsNeeded = new ArrayList<>();
		if (locationPermission != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
		}
		if (permissionIMEI != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
		}

		if (locationCoarse != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
		}

		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
			return false;
		}
		return true;
	}

	private void ScrollListViewToBottom() {
		listView1.post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				listView1.setSelection(adapter1.getCount() - 1);
			}
		});
	}

	private void AddStatus(String text){
		mListArrayAdapter2.add(text);
	}

	private void SetStatus(String text){
		mListArrayAdapter2.clear();
		mListArrayAdapter2.add(text);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_off, menu);
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
				//CloseReadCard();
				//SerialPortManager.getInstance().closeSerialPort();
				workExit();
				this.finish();
				return true;
			case R.id.action_screen:
				mData1.clear();
				adapter1.notifyDataSetChanged();
				mListArrayAdapter2.clear();
				break;
			case R.id.action_alcohol:
				Intent alcoholIntent = new Intent(this,Alcohol.class);
				startActivityForResult(alcoholIntent,REQUEST_ALCOHOL_TEST);
				break;
			case R.id.action_view:
				Intent intent = new Intent(SignOffActivity.this, RecordList.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			//bIsCancel=true;
			//CloseReadCard();
			//SerialPortManager.getInstance().closeSerialPort();
			workExit();
			this.finish();
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_HOME){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void FPProcess(){
		if(!bfpWork){
			try {
				Thread.currentThread();
				Thread.sleep(500);
			}catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			tvFpStatus.setText(getString(R.string.txt_fpplace));
			imgFeed.setVisibility(View.GONE);
			listView2.setVisibility(View.VISIBLE);
			//imgFeed.setImageResource(R.drawable.green_trans);

			vFingerprint.FP_GetImage();
			bfpWork=true;
		}
	}

	private void FPInit(){

		vFingerprint.setOnGetImageListener(new AsyncFingerprint.OnGetImageListener()  {
			@Override
			public void onGetImageSuccess() {
				if(bIsUpImage){
					vFingerprint.FP_UpImage();
					tvFpStatus.setText(getString(R.string.txt_fpdisplay));
				}else{
					tvFpStatus.setText(getString(R.string.txt_fpprocess));
					vFingerprint.FP_GenChar(1);
				}
			}

			@Override
			public void onGetImageFail() {
				if(!bIsCancel){
					vFingerprint.FP_GetImage();
					//SignLocalActivity.this.AddStatus("Error");
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

		vFingerprint.setOnUpCharListener(new OnUpCharListener() {

			@Override
			public void onUpCharSuccess(byte[] model) {

				List<User> user = empList;
				int count =0;
				if (empList.size() > 0) {

					//for(int i=0; i<emplist.size(); i++)
					for (User us : user)
					{

						if (us.getFinger1() != null && us.getFinger1().length() >= 512) {

							byte[] ref = ExtApi.Base64ToBytes(us.getFinger1());
							if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
								AddPersonItem(us);
								tvFpStatus.setText(getString(R.string.txt_fpmatchok) + "with "+FPMatch.getInstance().MatchTemplate(model, ref) + "%");
								break;
							}
						}
						if (us.getFinger2() != null && us.getFinger2().length() >= 512) {

							byte[] ref = ExtApi.Base64ToBytes(us.getFinger2());
							if (FPMatch.getInstance().MatchTemplate(model, ref) > 60) {
								AddPersonItem(us);
								tvFpStatus.setText(getString(R.string.txt_fpmatchok)+ "with "+FPMatch.getInstance().MatchTemplate(model, ref) + "%");
								break;
							}
						}

						count ++;
					}

					if(count == empList.size()){
						Toast.makeText(getApplicationContext(),"fingerprint not found",Toast.LENGTH_SHORT).show();
						listView2.setVisibility(View.GONE);
						imgFeed.setImageResource(R.drawable.red);
						imgFeed.setVisibility(View.VISIBLE);
					}
				} else
					Toast.makeText(getApplicationContext(),"Please download employee information",Toast.LENGTH_SHORT).show();

				bfpWork=false;
				TimerStart();
			}

			@Override
			public void onUpCharFail() {
				tvFpStatus.setText(getString(R.string.txt_fpmatchfail)+":-1");
				bfpWork=false;
				TimerStart();
			}
		});

	}

	public void TimerStart(){
		if(startTimer==null){
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

	public void TimeStop(){
		if (startTimer!=null)
		{
			startTimer.cancel();
			startTimer = null;
			startTask.cancel();
			startTask=null;
		}
	}

	//Card
	//Card
	public void InitReadCard() {
		if (ActivityList.getInstance().IsUseNFC) {
			nfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (nfcAdapter == null) {
				Toast.makeText(this, "Device does not support NFC!", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		/*	if (!nfcAdapter.isEnabled()) {
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

			rfid.RfidInit();    //Open
			rfid.SetContext(this);
		}
	}

	public void CloseReadCard() {
		if (ActivityList.getInstance().IsUseNFC) {
		} else {
			xTimerStop();
			rfid.RfidClose();    //Close
		}
	}

	public void ReadCardSn() {
		if (ActivityList.getInstance().IsUseNFC) {
		} else {
			xTimerStart();
		}
	}

	//RFID
	public void xTimerStart() {
		xTimer = new Timer();
		xHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int[] sn = new int[8];
				if (rfid.RfidGetSn(sn) == 0) {
					//xTimerStop();
					int[] buffer = new int[4096];
					switch (rfidtype) {
						case 0: {
							String cardsn =
									Integer.toHexString(sn[0] & 0xFF).toUpperCase() +
											Integer.toHexString(sn[1] & 0xFF).toUpperCase() +
											Integer.toHexString(sn[2] & 0xFF).toUpperCase() +
											Integer.toHexString(sn[3] & 0xFF).toUpperCase();

						/*	for (int i = 0; i < GlobalData.getInstance().userList.size(); i++) {
								if (GlobalData.getInstance().userList.get(i).cardsn.indexOf(cardsn) >= 0) {
									AddPersonItem(GlobalData.getInstance().userList.get(i));
								}
							}*/
						}
						break;
						case 1: {
							if (rfid.RfidReadFullCard(sn, buffer, 256) == 0) {
								byte[] b = rfid.IntArrayToByteArray(buffer, 256);
								//editText1.setText(new String(b));
								//textView1.setText("Read Data OK");
							} else {
								//textView1.setText("Read Data Fail");
							}
						}
						break;
						case 2: {
							String txt = "Test";//editText1.getText().toString();
							byte[] b = txt.getBytes();
							int[] ir = rfid.ByteArrayToIntArray(b, b.length);
							for (int i = 0; i < ir.length; i++) {
								buffer[i] = ir[i];
							}
							if (rfid.RfidWriteFullCard(sn, buffer, 256) == 0) {
								//textView1.setText("Write Data OK");
							} else {
								//textView1.setText("Write Data Fail");
							}
						}
						break;
					}
					//soundPool.play(soundIda, 1.0f, 0.5f, 1, 0, 1.0f);
				}

				super.handleMessage(msg);
			}
		};
		xTask = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				xHandler.sendMessage(message);
			}
		};
		xTimer.schedule(xTask, 1000, 1000);
	}

	public void xTimerStop() {
		if (xTimer!=null) {
			xTimer.cancel();
			xTimer = null;
			xTask.cancel();
			xTask=null;
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
		String cardsn=
				Integer.toHexString(sn[0]&0xFF).toUpperCase()+
						Integer.toHexString(sn[1]&0xFF).toUpperCase()+
						Integer.toHexString(sn[2]&0xFF).toUpperCase()+
						Integer.toHexString(sn[3]&0xFF).toUpperCase();
		//SetStatus(getString(R.string.txt_cardsn) + cardsn);
		//SetStatus(cardsn);

		List<User> user = empList;
		int count =0;
		if (empList.size() > 0) {

			for (User us : user)
			{
				if (us.getCard() != null && us.getCard().length() > 0) {

					if(us.getCard().equals(cardsn)){
						AddPersonItem(us);
						tvFpStatus.setText(getString(R.string.txt_fpidentify));
						break;
					}
				}
				count ++;
			}

			if(count == empList.size()){
				Toast.makeText(getApplicationContext(),"Card not found",Toast.LENGTH_SHORT).show();
				listView2.setVisibility(View.GONE);
				imgFeed.setImageResource(R.drawable.red);
				imgFeed.setVisibility(View.VISIBLE);
			}

		}else
			Toast.makeText(getApplicationContext(),"Please download employee information",Toast.LENGTH_SHORT).show();
		bfpWork=false;
		TimerStart();
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){

		super.onActivityResult(requestCode,resultCode,data);

		if(requestCode == REQUEST_ALCOHOL_TEST && resultCode == RESULT_OK){
			String value = data.getStringExtra(Alcohol.EXTRA_VALUE);
			showToast(value);
			if(Integer.parseInt(value) > 0){
				alcohol_val = "1";
			}else{
				alcohol_val = "0";
			}

			showToast(alcohol_val);
		}else{
			showToast("Alcohol test not provided");
		}
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
				nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
		}
	}

	public Object postRequest(String param) throws IOException {

		//System.out.println("PARAM==="+param);
		setDialog(true);


		OkHttpClient client = new OkHttpClient();
		RequestBody body = new FormBody.Builder()
				.add("recordJSON",param)
				.build();
		Request request = new Request.Builder()
				.url(clockURL)
				.post(body)
				.build();
		//Log.d("PARAM:+++",param[0]+ " "+param[1]);


		client.newCall(request).enqueue(new Callback() {

			Handler handler = new Handler(SignOffActivity.this.getMainLooper());
			@Override
			public void onFailure(Call call, final IOException e) {
				call.cancel();
				mydb.insertRecord(db_user_id_number,db_name,db_date,db_latitude,db_longitude,db_user_id,db_status,db_imei,db_shift_id,db_shift_type,db_cost_center_id);
				if(dialog != null && dialog.isShowing()){
					dialog.dismiss();
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						showToast("Error: "+e.getMessage() + " Record saved");
					}
				});
			}

			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				handler.post(new Runnable() {
					@Override
					public void run() {

						if(!response.isSuccessful()){
							mydb.insertRecord(db_user_id_number,db_name,db_date,db_latitude,db_longitude,db_user_id,db_status,db_imei,db_shift_id,db_shift_type,db_cost_center_id);

							if(dialog != null && dialog.isShowing()){
								dialog.dismiss();
							}
							showToast("Unexpected error: "+response.message());
							showToast("Clock Saved in device");
							clearInfo();

						}else{

							try {

								apiFeedback(response.body().string());
								if(dialog != null && dialog.isShowing()){
									dialog.dismiss();
								}
								clearInfo();

							}catch (IOException e){
								e.printStackTrace();
								mydb.insertRecord(db_user_id_number,db_name,db_date,db_latitude,db_longitude,db_user_id,db_status,db_imei,db_shift_id,db_shift_type,db_cost_center_id);
								showToast("Clock Saved in device");
								clearInfo();
							}

						}


					}
				});
			}
		});

		return null;
	}

	public void apiFeedback(String response){

		//System.out.println("RESPONSE ==="+response);

		try {

			JSONArray arr = new JSONArray(response);
			for(int i=0; i<arr.length();i++){
				JSONObject obj = (JSONObject)arr.get(i);
				//System.out.println(obj.get("id"));
				//System.out.println(obj.get("status"));
				//Toast.makeText(getApplicationContext(), obj.get("id").toString()+ " "+ obj.get("status").toString(), Toast.LENGTH_LONG).show();
				String status = obj.get("status").toString();
				String msg = obj.getString("message");
				if(status.equals("yes")){
					//Toast.makeText(getApplicationContext(), "Clock-out successful", Toast.LENGTH_LONG).show();

						showToast(msg);
				}else{

					showToast(msg);
					//Toast.makeText(getApplicationContext(), "Error, record saved in device please upload records later.", Toast.LENGTH_LONG).show();
					mydb.insertRecord(db_user_id_number,db_name,db_date,db_latitude,db_longitude,db_user_id,db_status,db_imei,db_shift_id,db_shift_type,db_cost_center_id);

				}
				//db.updateSyncStatus(obj.get("idNum").toString(),obj.get("status").toString());
			}

		}catch (JSONException e){
			e.printStackTrace();
			showToast("JSON Error, record saved in device");
			mydb.insertRecord(db_user_id_number,db_name,db_date,db_latitude,db_longitude,db_user_id,db_status,db_imei,db_shift_id,db_shift_type,db_cost_center_id);
		}


	}
	private void setDialog(boolean show){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final LayoutInflater inflater = LayoutInflater.from(SignOffActivity.this);
		View vl = inflater.inflate(R.layout.progress, null);
		builder.setView(vl);
		dialog = builder.create();
		if(show) {
			dialog.show();
		}else{
			dialog.cancel();
		}

	}
	public void showToast(String message)
	{
		Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
	}

	/*public void clockOut(final int id, final String dat, final String lon, final String lat, final String name,final String idN,final int costId, final int stype,final int sId){


		final String identifier;
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null)
			identifier = telephonyManager.getDeviceId();
		else
			identifier = "Not available";

		try {
			//Create AsycHttpClient object
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			String json = "";

			//  build jsonObject
			JSONObject jsonObject = new JSONObject();
			jsonObject.accumulate("dat", dat);
			jsonObject.accumulate("status", "OUT");
			jsonObject.accumulate("lon", lon);
			jsonObject.accumulate("lat", lat);
			jsonObject.accumulate("id", id);
			jsonObject.accumulate("imei",identifier);
			jsonObject.accumulate("shifts_id",sId);
			jsonObject.accumulate("shift_type",stype);
			jsonObject.accumulate("costCenterId",costId);

			//  convert JSONObject to JSON to String
			json = jsonObject.toString();

			//  set json to StringEntity
			//StringEntity se = new StringEntity(json);
			prgDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Save to device", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					//mydb.insertRecord(idN, name, dat, lat, lon, id, "OUT", identifier);
					test = 0;
					//Toast.makeText(getApplicationContext(), "Record saved in device please upload records later.", Toast.LENGTH_LONG).show();
				}
			});
			prgDialog.show();
			params.put("recordJSON", json);
			//client.setTimeout(5000);
			client.post("http://www.nexgencs.co.za/alos/clock.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					//System.out.println(response);
					prgDialog.hide();
					//Toast.makeText(getApplicationContext(), "Clock-out successful", Toast.LENGTH_LONG).show();
					try {
						JSONArray arr = new JSONArray(response);
						System.out.println(arr.length());
						for(int i=0; i<arr.length();i++){
							JSONObject obj = (JSONObject)arr.get(i);
							//System.out.println(obj.get("id"));
							//System.out.println(obj.get("status"));
							//Toast.makeText(getApplicationContext(), obj.get("id").toString()+ " "+ obj.get("status").toString(), Toast.LENGTH_LONG).show();
							String status = obj.get("status").toString();
							if(status.equals("yes")){
								Toast.makeText(getApplicationContext(), "Clock-out successful", Toast.LENGTH_LONG).show();
								test = 1;

							}else{

								Toast.makeText(getApplicationContext(), "Error, record saved in device please upload records later.", Toast.LENGTH_LONG).show();
								mydb.insertRecord(idN, name, dat, lat, lon, id,"OUT",identifier,sId,stype,costId);

							}
							//db.updateSyncStatus(obj.get("idNum").toString(),obj.get("status").toString());
						}
						clearInfo();
						//Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						//mydb.insertRecord(idN, name, dat, lat, lon, id,"OUT",identifier);
						test = 0;
						//Toast.makeText(getApplicationContext(), "Network error, record saved in device please upload records later.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
						clearInfo();
					}
				}

				@Override
				public void onFailure(Throwable error,
									  String content) {
					// TODO Auto-generated method stub
					prgDialog.hide();
					test = 0;
					//mydb.insertRecord(idN, name, dat, lat, lon, id,"OUT",identifier);
					//Toast.makeText(getApplicationContext(), "Network error, record saved in device please upload records later.", Toast.LENGTH_LONG).show();

					clearInfo();
				}
			});

		}catch (Exception e){

			Log.d("InputStream", e.getLocalizedMessage());
		}

		//Stop after 5 seconds.
		//Toast.makeText(getApplicationContext(), String.valueOf(test), Toast.LENGTH_LONG).show();
		//TimeOutStart(id, dat, lon, lat, name, idN, identifier,sId,stype,costId);
	}*/

	public void TimeOutStart(final int id, final String dat, final String lon, final String lat, final String name,final String idN,final String identifier,final int sId, final int stype,final int costId,final String status) {

		final Handler tOut = new Handler();
		tOut.postDelayed(new Runnable() {
			@Override
			public void run() {

				//Toast.makeText(getApplicationContext(), String.valueOf(test), Toast.LENGTH_SHORT).show();
				prgDialog.hide();
				if(test == 0){
					mydb.insertRecord(idN, name, dat, lat, lon, id,status,identifier,sId,stype,costId);
					Toast.makeText(getApplicationContext(), "Error, record saved in device please upload records later.", Toast.LENGTH_LONG).show();
					clearInfo();
				}
				//mydb.insertRecord(idN, name, dat, lat, lon, id, "IN", identifier);
				//sendCom("f");
			}
		}, 5000);
	}

	public void clearInfo(){

		final Handler tOut = new Handler();
		tOut.postDelayed(new Runnable() {
			@Override
			public void run() {

				mData1.clear();
				adapter1.notifyDataSetChanged();
				mListArrayAdapter2.clear();

			}
		},2000);
	}


}
