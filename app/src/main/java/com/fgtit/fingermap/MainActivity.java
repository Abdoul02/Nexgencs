package com.fgtit.fingermap;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import com.airbnb.android.airmapview.AirMapInterface;
import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.AirMapViewTypes;
import com.airbnb.android.airmapview.DefaultAirMapViewBuilder;
import com.airbnb.android.airmapview.GoogleChinaMapType;
import com.airbnb.android.airmapview.MapType;
import com.airbnb.android.airmapview.WebAirMapViewBuilder;
import com.airbnb.android.airmapview.listeners.OnCameraChangeListener;
import com.airbnb.android.airmapview.listeners.OnCameraMoveListener;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnLatLngScreenLocationCallback;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.fgtit.models.Loc;
import com.fgtit.models.PermissionUtils;
import com.fgtit.models.SessionManager;
import com.fgtit.fpcore.FPMatch;
import com.fgtit.utils.ExtApi;
import com.fgtit.app.ActivityList;
import com.fgtit.app.UpdateApp;
import com.fgtit.data.GlobalData;
import com.google.android.gms.maps.model.LatLng;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.PowerManager.WakeLock;
import android.Manifest;

public class MainActivity extends AppCompatActivity
		implements OnCameraChangeListener, OnMapInitializedListener,
		OnMapClickListener, OnCameraMoveListener, OnMapMarkerClickListener,
		OnInfoWindowClickListener, OnLatLngScreenLocationCallback,ActivityCompat.OnRequestPermissionsResultCallback,
		PermissionUtils.PermissionResultCallback {

	private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

	private static final int RE_WORK0 = 0;
	private static final int RE_WORK1 = 1;
	private static final int RE_WORK2 = 2;

	private String btAddress = "";

	private Menu mainMenu;
	static TextView txtView;
	private Button btn01, btn02, btn03;
	private long exitTime = 0;
	private WakeLock wakeLock;

	private Timer startTimer;
	private TimerTask startTask;
	Handler startHandler;

	private SoundPool soundPool;
	private int soundIda, soundIdb;
	private boolean soundflag = false;

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private BitmapDescriptor mCurrentMarker;
	private LocationMode mCurrentMode;

	GPSTracker gps;

	private AirMapView map;
	private DefaultAirMapViewBuilder mapViewBuilder;

	// Session Manager Class
	SessionManager session;

	//Permissions
	PermissionUtils permissionUtils;
	boolean isPermissionGranted;
	ArrayList<String> permissions=new ArrayList<>();
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

	@SuppressLint({"NewApi", "SetJavaScriptEnabled", "InvalidWakeLockTag"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setBackgroundDrawable(new
				ColorDrawable(Color.parseColor("#020969")));

		ActivityList.getInstance().IsUseNFC = ExtApi.IsSupportNFC(this);

		//�߳�
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

		txtView = (TextView) findViewById(R.id.textView1);

		//MtGpio.getInstance().FPPowerSwitch(true);
		gps = new GPSTracker(MainActivity.this);

		// Session class instance
		session = new SessionManager(getApplicationContext());
		session.checkLogin();

		// get user data from session
		//HashMap<String, String> manager = session.getUserDetails();
		//Toast.makeText(this, manager.get(SessionManager.KEY_EMAIL)+" "+ manager.get(SessionManager.KEY_COMPID) ,Toast.LENGTH_SHORT).show();

		mapViewBuilder = new DefaultAirMapViewBuilder(this);
		map = (AirMapView) findViewById(R.id.map);

		Button btnMapTypeNormal = (Button) findViewById(R.id.btnMapTypeNormal);
		Button btnMapTypeSattelite = (Button) findViewById(R.id.btnMapTypeSattelite);
		Button btnMapTypeTerrain = (Button) findViewById(R.id.btnMapTypeTerrain);


		btnMapTypeNormal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(@NonNull View v) {
				map.setMapType(MapType.MAP_TYPE_NORMAL);
			}
		});

		btnMapTypeSattelite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(@NonNull View v) {
				map.setMapType(MapType.MAP_TYPE_SATELLITE);
			}
		});

		btnMapTypeTerrain.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(@NonNull View v) {
				map.setMapType(MapType.MAP_TYPE_TERRAIN);
			}
		});

		map.setOnMapClickListener(this);
		map.setOnCameraChangeListener(this);
		map.setOnCameraMoveListener(this);
		map.setOnMarkerClickListener(this);
		map.setOnMapInitializedListener(this);
		map.setOnInfoWindowClickListener(this);
		map.initialize(getSupportFragmentManager());

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);

		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(myListener);
		mLocationClient.setLocOption(option);
		mLocationClient.start();


		btn01 = (Button) findViewById(R.id.button1);
		btn01.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SignOnActivity.class);
				//startActivity(intent);
				startActivityForResult(intent, RE_WORK1);
			}
		});

		btn02 = (Button) findViewById(R.id.button2);
		btn02.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SignOffActivity.class);
				//startActivity(intent);
				startActivityForResult(intent, RE_WORK2);
			}
		});

		btn03 = (Button) findViewById(R.id.button3);
		btn03.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MenuActivity.class);
				startActivity(intent);
			}
		});


		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "sc");
		wakeLock.acquire();

		GlobalData.getInstance().SetContext(this);
		GlobalData.getInstance().CreateDir();
		GlobalData.getInstance().LoadFileList();
		GlobalData.getInstance().LoadUsersList();
		GlobalData.getInstance().LoadConfig();
		//GlobalData.getInstance().LoadRecordsList();
		GlobalData.getInstance().LoadWorkList();
		GlobalData.getInstance().LoadLineList();
		GlobalData.getInstance().LoadDeptList();

		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		soundIda = soundPool.load(this, R.raw.start, 1);
		soundIdb = soundPool.load(this, R.raw.stop, 1);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				soundflag = true;
			}
		});

		if (FPMatch.getInstance().InitMatch() == 0) {
			Toast.makeText(getApplicationContext(), "Init Matcher Fail!", Toast.LENGTH_SHORT).show();
		} else {
			//Toast.makeText(getApplicationContext(), "Init Matcher OK!", Toast.LENGTH_SHORT).show();
		}

		UpdateApp.getInstance().setAppContext(this);



		permissionUtils=new PermissionUtils(this);
		permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
		permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
		permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		permissions.add(Manifest.permission.READ_PHONE_STATE);
		permissions.add(Manifest.permission.CAMERA);
		permissionUtils.check_permission(permissions,"Need GPS permission for getting your location",1);
	}

	// Reload MainActivity
	public void reloadActivity() {
		Intent objIntent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(objIntent);
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			StringBuffer sb = new StringBuffer(256);
			switch (location.getLocType()) {
				case 61:
					//sb.append("Satellite positioning");
					GlobalData.getInstance().glocal = true;
					break;
				case 66:
					//sb.append("Offline positioning");
					GlobalData.getInstance().glocal = true;
					break;
				case 161:
					//sb.append("Network positioning");
					GlobalData.getInstance().glocal = true;
					break;
				default:
					//sb.append("Positioning failure");
					GlobalData.getInstance().glocal = false;
					break;
			}

			/*Loc loc = new Loc();

			//if(loc.getLat() != 0.0 && loc.getLon() != 0.0){

				sb.append("Latitude: ");
				sb.append(loc.getLat());
				sb.append("  Longitude: ");
				sb.append(loc.getLon());

			//}

			else{
				sb.append("Latitude: ");
				//sb.append(gps.getLatitude());
				sb.append("empty");
				sb.append("  Longitude: ");
				sb.append("empty");
				//sb.append(gps.getLongitude());

			}  */

			//sb.append("  Time: ");
			//sb.append(location.getTime());


			//txtView.setText(sb.toString());

			GlobalData.getInstance().glat = location.getLatitude();
			GlobalData.getInstance().glng = location.getLongitude();
			MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius()).direction(location.getDirection()).latitude(location.getLatitude()).longitude(location.getLongitude()).build();


		}
	}

	@Override
	public void onStart() {
		super.onStart();
		TimerStart();
	}

	public void TimerStart() {
		startTimer = new Timer();
		startHandler = new Handler() {
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				mLocationClient.requestLocation();
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
		startTimer.schedule(startTask, 5000, 5000);
	}

	public void TimeStop() {
		if (startTimer != null) {
			startTimer.cancel();
			startTimer = null;
			startTask.cancel();
			startTask = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//map.onDestroy();
		TimeStop();

		wakeLock.release(); //���ֻ���		
		soundPool.release();
		soundPool = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//mMapView.onPause();

		wakeLock.release();//���ֻ���
	}

	@Override
	protected void onResume() {
		super.onResume();
		//	mMapView.onResume();

		wakeLock.acquire(); //���ñ��ֻ���
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			exitApplication();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exitApplication() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), getString(R.string.txt_exitinfo), Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
			//AppList.getInstance().exit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		mainMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		AirMapInterface airMapInterface = null;
		int id = item.getItemId();
		switch (id) {

			case android.R.id.home:
				exitApplication();
				return true;
			case R.id.action_refresh:
				reloadActivity();
				return true;
			case R.id.action_manage: {
				session.logoutUser();
			}
			return true;

			case R.id.action_native_map:
				try {
					airMapInterface = mapViewBuilder.builder(AirMapViewTypes.NATIVE).build();
				} catch (UnsupportedOperationException e) {
					Toast.makeText(this, "Sorry, native Google Maps are not supported by this device. " +
									"Please make sure you have Google Play Services installed.",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.action_mapbox_map:
				airMapInterface = mapViewBuilder.builder(AirMapViewTypes.WEB).build();
				break;
			case R.id.action_google_web_map:
				// force Google Web maps since otherwise AirMapViewTypes.WEB returns MapBox by default.
				airMapInterface = new WebAirMapViewBuilder().build();
				break;
			case R.id.action_google_china_web_map:
				airMapInterface = new WebAirMapViewBuilder().withOptions(new GoogleChinaMapType()).build();
				break;
		}

		if (airMapInterface != null) {
			map.initialize(getSupportFragmentManager(), airMapInterface);
		}
		return super.onOptionsItemSelected(item);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case RE_WORK0:
				break;
			case RE_WORK1:
				break;
			case RE_WORK2:
				break;
		}
	}

	private void LocationInit() {
		LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS Location", Toast.LENGTH_SHORT).show();
			GpsLocationInit();
			return;
		} else {
			Toast.makeText(this, "Net Location", Toast.LENGTH_SHORT).show();
			NetLocationInit();
		}
	}

	private void GetLocationInfo(Location location) {
		if (location != null) {
			// 	GlobalData.getInstance().glocal=true;
			//	GlobalData.getInstance().glat=location.getLatitude();
			//	GlobalData.getInstance().glng=location.getLongitude();
			//SetMapCenter(location.getLatitude(),location.getLongitude());
			//SetMapMaker(location.getLatitude(),location.getLongitude());
			//txtView.setText("Net Location :  "+String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));
		} else {
			GlobalData.getInstance().glocal = false;
			txtView.setText("No location found");
		}

	}

	private void NetLocationInit() {
		// ��ȡ��LocationManager����
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// ����һ��Criteria����
		Criteria criteria = new Criteria();
		// ���ô��Ծ�ȷ��
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		// �����Ƿ���Ҫ���غ�����Ϣ
		criteria.setAltitudeRequired(false);
		// �����Ƿ���Ҫ���ط�λ��Ϣ
		criteria.setBearingRequired(false);
		// �����Ƿ����?�ѷ���
		criteria.setCostAllowed(true);
		// ���õ�����ĵȼ�
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		// �����Ƿ���Ҫ�����ٶ���Ϣ
		criteria.setSpeedRequired(false);
		// ������õ�Criteria���󣬻�ȡ���ϴ˱�׼��provider���� 41
		String currentProvider = locationManager.getBestProvider(criteria, true);

		// ��ݵ�ǰprovider�����ȡ���һ��λ����Ϣ 44
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		Location currentLocation = locationManager.getLastKnownLocation(currentProvider);
		// ���λ����ϢΪnull�����������λ����Ϣ 46
		if (currentLocation == null) {
			locationManager.requestLocationUpdates(currentProvider, 5000, 0, netlocationListener);
		} else {
			GetLocationInfo(currentLocation);
			locationManager.requestLocationUpdates(currentProvider, 5000, 0, netlocationListener);    //LocationManager.GPS_PROVIDER
		}
	}

	private LocationListener netlocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			GetLocationInfo(location);
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	private void GpsLocationInit() {
		LocationManager gpslocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// ���ҵ�������Ϣ
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// �߾���
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		// �͹���
		String currentProvider = gpslocationManager.getBestProvider(criteria, true);
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		Location gpsLocation = gpslocationManager.getLastKnownLocation(currentProvider);
		//String gpsProvider = gpslocationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
		//Location gpsLocation = gpslocationManager.getLastKnownLocation(gpsProvider);

		// ���λ����ϢΪnull�����������λ����Ϣ
		if (gpsLocation == null) {
			//gpslocationManager.requestLocationUpdates(gpsProvider, 0, 0,gpslocationListener);
			gpslocationManager.requestLocationUpdates(currentProvider, 0, 0, gpslocationListener);
		} else {
			GetLocationInfo(gpsLocation);
			gpslocationManager.requestLocationUpdates(currentProvider, 0, 0, gpslocationListener);
		}
		gpslocationManager.addGpsStatusListener(gpsListener);
	}

	private LocationListener gpslocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			GetLocationInfo(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		// GPS״̬����仯ʱ����
		@Override
		public void onGpsStatusChanged(int event) {

			switch (event) {
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					break;
				case GpsStatus.GPS_EVENT_STARTED:
					break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					break;
			}
		}
	};


	@Override
	public void onCameraChanged(LatLng latLng, int zoom) {
		//txtView.setText("Long: "+ latLng.longitude + " Lat: "+latLng.latitude);
	}

	@Override
	public void onMapInitialized() {


		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		criteria.setCostAllowed(false);
		// get the best provider depending on the criteria
		String provider = locationManager.getBestProvider(criteria, false);

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


		MyLocation mylocalListener = new MyLocation();

		if (loc != null) {
			mylocalListener.onLocationChanged(loc);
			final LatLng airbnbLatLng = new LatLng(loc.getLatitude(),loc.getLongitude());

			addMarker(String.valueOf(loc.getLatitude())+","+String.valueOf(loc.getLongitude()), airbnbLatLng, 1);
			map.animateCenterZoom(airbnbLatLng, 10);
			// Add Circle
			//map.drawCircle(new LatLng(latitude,longitude), 10);
			// enable my location
			map.setMyLocationEnabled(true);
		} else {

			final LatLng airbnbLatLng = new LatLng(gps.getLatitude(),gps.getLongitude());

			addMarker(String.valueOf(gps.getLatitude())+","+String.valueOf(gps.getLongitude()), airbnbLatLng, 1);
			map.animateCenterZoom(airbnbLatLng, 15);
			// Add Circle
			//map.drawCircle(new LatLng(latitude,longitude), 10);
			// enable my location
			map.setMyLocationEnabled(true);
			// leads to the settings because there is no last known location
			Toast.makeText(this,"There was no last known location",Toast.LENGTH_SHORT).show();
		}
		// location updates: at least 1 meter and 30 seconds change
		locationManager.requestLocationUpdates(provider, 30*1000, 5, mylocalListener);

	}


	private void addMarker(String title, LatLng latLng, int id) {
		map.addMarker(new AirMapMarker.Builder()
				.id(id)
				.position(latLng)
				.title(title)
				.iconId(R.drawable.icon_location_pin)
				.build());
	}

	@Override public void onMapClick(LatLng latLng) {
	/*	if (latLng != null) {
			txtView.setText("Long: " + latLng.longitude + " Lat: " + latLng.latitude);

			map.getMapInterface().getScreenLocation(latLng, this);
		} else {
			txtView.setText("Map onMapClick triggered with null latLng");
		}  */
	}

	@Override public void onCameraMove() {
		//txtView.setText("Map onCameraMove triggered");
		//Toast.makeText(getApplicationContext(),"Map onCameraMove triggered ", Toast.LENGTH_SHORT).show();
	}

	@Override public void onMapMarkerClick(AirMapMarker airMarker) {
		//Toast.makeText(getApplicationContext(),"Map onMapMarkerClick triggered with id " + airMarker.getId(), Toast.LENGTH_SHORT).show();
	}

	@Override public void onInfoWindowClick(AirMapMarker airMarker) {
		//appendLog("Map onInfoWindowClick triggered with id " + airMarker.getId());
		//Toast.makeText(getApplicationContext(),"Map onInfoWindowClick triggered with id " + airMarker.getId(), Toast.LENGTH_SHORT).show();
	}

	@Override public void onLatLngScreenLocationReady(Point point) {
		//appendLog("LatLng location on screen (x,y): (" + point.x + "," + point.y + ")");
		//Toast.makeText(getApplicationContext(),"LatLng location on screen (x,y): (" + point.x + "," + point.y + ")", Toast.LENGTH_SHORT).show();

	}

	static class MyLocation implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// Initialize the location fields

			Loc loc = new Loc();
			loc.setLat(location.getLatitude());
			loc.setLon(location.getLongitude());

			txtView.setText("Latitude: "+ String.valueOf(location.getLatitude() + " Longitude: "+String.valueOf(location.getLongitude())));



			//Toast.makeText(MainActivity.this, "Latitude: " +String.valueOf(location.getLatitude()) + " Longitude: " + String.valueOf(location.getLongitude()),
				//	Toast.LENGTH_LONG).show();

         /*   LatLng nex = new LatLng(location.getLatitude(),location.getLongitude());
            Marker TP = googleMap.addMarker(new MarkerOptions().
                    position(nex).title(String.valueOf(location.getLatitude() +", "+ String.valueOf(location.getLongitude()))));

            latitude.setText("Latitude: "+String.valueOf(location.getLatitude()));
            longitude.setText("Longitude: "+String.valueOf(location.getLongitude()));
            provText.setText(provider + " provider has been selected."); */




			//Toast.makeText(MainActivity.this,  "Location changed!",
				//	Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			//Toast.makeText(MainActivity.this, provider + "'s status changed to "+status +"!",
				//	Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			//Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
				//	Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onProviderDisabled(String provider) {
			//Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
			//		Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void PermissionGranted(int request_code) {
		Log.i("PERMISSION","GRANTED");
		isPermissionGranted=true;
	}

	@Override
	public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
		Log.i("PERMISSION PARTIALLY","GRANTED");
	}

	@Override
	public void PermissionDenied(int request_code) {
		Log.i("PERMISSION","DENIED");
	}

	@Override
	public void NeverAskAgain(int request_code) {
		Log.i("PERMISSION","NEVER ASK AGAIN");
	}

}


