package com.fgtit.fingermap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fgtit.fingermap.erd.ERDJobActivity;
import com.fgtit.models.SessionManager;
import com.fgtit.models.User;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class MenuActivity extends AppCompatActivity {

	private ListView listView;
	private List<Map<String, Object>> mData;

	DBHandler myDB = new DBHandler(this);
	private ArrayList<User> userList;
	String versionName,apkPath;
	ProgressDialog prgDialog,pDialog;
	private ImageView fpImage;
	private Dialog fpDialog;
	private TextView tvFpStatus;
	int responseCode;
	int companyID;
	SessionManager session;
	HashMap<String, String> manager;
	String serverURL = "http://www.nexgencs.co.za/alos/checkUpdate.php";
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setBackgroundDrawable(new
				ColorDrawable(Color.parseColor("#020969")));

		session = new SessionManager(getApplicationContext());
		 manager = session.getUserDetails();
		companyID = Integer.parseInt(manager.get(SessionManager.KEY_COMPID));

		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Checking for update...");
		prgDialog.setCancelable(false);

		//Downloading app
		pDialog = new ProgressDialog(MenuActivity.this);
		pDialog.setMessage("Downloading Attachment...");
		pDialog.setIndeterminate(false);
		pDialog.setMax(100);
		pDialog.setProgressStyle(pDialog.STYLE_HORIZONTAL);
		pDialog.setCancelable(true);


		versionName = BuildConfig.VERSION_NAME;
		listView=(ListView) findViewById(R.id.listView1);	
		SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.listview_menuitem,
				new String[]{"title","info","img"},
				new int[]{R.id.title,R.id.info,R.id.img});
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override  
			public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {  
				//Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);  
				switch(pos){

					case 0:{

						Intent intent = new Intent(MenuActivity.this, Project.class);
						startActivity(intent);
					}
					break;
				case 1:{

					if(companyID == 117 || companyID == 3){

						Intent intent = new Intent(MenuActivity.this, ERDJobActivity.class);
						startActivity(intent);
					}else{
						Intent intent = new Intent(MenuActivity.this, JobActivity.class);
						startActivity(intent);
					}

					}
					break;
				case 2:{

					// Get User records from SQLite DB
					userList = myDB.getAllUsers();
					if (userList.size() != 0) {
						Intent intent = new Intent(MenuActivity.this, UserList.class);
						startActivity(intent);
					}else{

						Toast.makeText(getApplicationContext(), "List is empty please download employee record first", Toast.LENGTH_SHORT).show();
					}
					}
					break;
				case 3:{
						Intent intent = new Intent(MenuActivity.this, UtilitiesActivity.class);
						startActivity(intent);
					}
					break;
				case 4:{

					//Intent intent = new Intent(MenuActivity.this, AppSettings.class);
					//startActivity(intent);
					passwordDialog();
					}
					break;


				case 5:{
						AlertDialog.Builder dialog = new AlertDialog.Builder(MenuActivity.this);
						dialog.setTitle("About - NexGen Clocking System");
						dialog.setMessage("\nBiometric System "+versionName+"\n" +
			           		"\t  \n"
			        		   +"\tAll rights reserved, 2015-2018.\n "
			        		   );
						dialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						dialog.show();
					}
					break;

					case 6:{

						new checkUpdate().execute();
					}
					break;

					case 7:{

						//Go to Bluetooth scale activity
						Intent intent = new Intent(MenuActivity.this, BTScale.class);
						startActivity(intent);
					}
					break;

					case 8:{

						//Go to Alcohol  activity
						Intent intent = new Intent(MenuActivity.this, Alcohol.class);
						startActivity(intent);
					}
					break;

				}
			}             
		});
        
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();

		if(companyID == 3 || companyID == 116){

			map = new HashMap<String, Object>();
			map.put("title", "Machine Job Card");
			map.put("info", "Create Job card from machine");
			map.put("img", R.drawable.view_details);
			list.add(map);

		}else{

			map = new HashMap<String, Object>();
			map.put("title", "Projects");
			map.put("info", "Create Projects");
			map.put("img", R.drawable.timesheet);
			list.add(map);
		}


		map = new HashMap<String, Object>();
		map.put("title", "Cloud Job Card");
		map.put("info", "View or Download available Job Card(s) from Cloud");
		map.put("img", R.drawable.view_details);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_02));
		map.put("info", getString(R.string.txt_info_02));
		map.put("img", R.drawable.group);
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_03));
		map.put("info", getString(R.string.txt_info_03));
		map.put("img", R.drawable.reload);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_04));
		map.put("info", getString(R.string.txt_info_04));
		map.put("img", R.drawable.engineering);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.txt_title_05));
		map.put("info", getString(R.string.txt_info_05));
		map.put("img", R.drawable.about);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "Check for update");
		map.put("info", ",your current version is "+versionName);
		map.put("img", R.drawable.check_update);
		list.add(map);

		if(companyID == 3 || companyID == 8 || companyID == 29){

			map = new HashMap<String, Object>();
			map.put("title", "Bluetooth Scale");
			map.put("info", "Gets weight from bluetooth scale");
			map.put("img", R.drawable.bluetooth);
			list.add(map);
		}

		if(companyID == 3 || companyID == 8){

			map = new HashMap<String, Object>();
			map.put("title", "Alcohol Reader");
			map.put("info", "Detect Alcohol usage");
			map.put("img", R.drawable.alcohol);
			list.add(map);
		}


				
		mData=list;		
		return list;
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.system, menu);
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
			//this.setResult(1);
			//this.finish();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_settings:
			//Toast.makeText(getApplicationContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
			//Intent intent = new Intent(this, SystemActivity.class);
			//startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override  
 	public boolean onKeyDown(int keyCode, KeyEvent event) {  
 	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
 			//this.setResult(1);
 			//this.finish();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
 	    	return true;  
 	    }
 	    return super.onKeyDown(keyCode, event);  
 	}

	public class checkUpdate extends AsyncTask<String, Void, String> {

		protected void onPreExecute() {

			prgDialog.show();

		}

		protected String doInBackground(String... args) {


			try {


				 String identifier ="N/A";
				if (ContextCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.READ_PHONE_STATE)
						!= PackageManager.PERMISSION_GRANTED) {

					ActivityCompat.requestPermissions(MenuActivity.this,new String[]{ Manifest.permission.READ_PHONE_STATE}, 1);
					// Permission is not granted
				}else{

					TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
					if (telephonyManager != null)
						identifier = telephonyManager.getDeviceId();
					else
						identifier = "Not available";

				}


				/*TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (telephonyManager != null) {
					identifier = telephonyManager.getDeviceId();
				} else {
					identifier = "Not available";
				}*/


				URL url = new URL(serverURL);
				JSONObject postDataParams = new JSONObject();
				postDataParams.put("version", versionName);
				postDataParams.put("identifier", identifier);
				postDataParams.put("app", "alos");
				Log.e("params", postDataParams.toString());

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(5000 /* milliseconds */);
				conn.setConnectTimeout(5000 /* milliseconds */);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Accept", "application/json");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(getPostDataString(postDataParams));
				writer.flush();
				writer.close();
				os.close();
				responseCode = conn.getResponseCode();
				if (responseCode == HttpsURLConnection.HTTP_OK) {

					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuffer sb = new StringBuffer("");
					String line = "";

					while ((line = in.readLine()) != null) {

						sb.append(line);
						break;
					}

					in.close();
					return sb.toString();

				} else {
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
					StringBuffer sb = new StringBuffer("");
					String line = "";

					while ((line = in.readLine()) != null) {

						sb.append(line);
						break;
					}

					in.close();
					return sb.toString();
					//return new String("false : "+responseCode);
				}


			} catch (Exception e) {
				return new String("Exception: " + e.getMessage());
			}
		}

		@Override
		protected void onPostExecute(String result) {

			if (prgDialog != null && prgDialog.isShowing()) {
				prgDialog.dismiss();
			}

			if (result != null) {
				Log.e("RESULT+++++++++:", result);

				try{
					JSONArray arr = new JSONArray(result);
					for(int i=0; i<arr.length();i++){

						JSONObject obj = (JSONObject)arr.get(i);
						int success = obj.getInt("success");
						if(success == 1){

							String version = obj.get("version").toString();
							String url = obj.get("url").toString();

							if(version.equals(versionName)){

								//downloadDialog("","",1);
								//showToast("App is up to date");
								msgDialog("App is up to date",1);

							}else{

								downloadDialog(url,"",0);
							}
						}else{

							String msg = obj.getString("msg");
							msgDialog(msg,0);
						}


					}
				}catch (JSONException e){
					e.printStackTrace();
				}
				//showToast(result);
				if (responseCode == 404) {
					showToast("Requested resource not found");
				} else if (responseCode == 500) {
					showToast("Something went wrong at server end");
				}else if (responseCode !=200){
					showToast("Please check internet connection ");
				}
			}
		}
	}
	public String getPostDataString(JSONObject params) throws Exception {

		StringBuilder result = new StringBuilder();
		boolean first = true;

		Iterator<String> itr = params.keys();

		while(itr.hasNext()){

			String key= itr.next();
			Object value = params.get(key);

			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(value.toString(), "UTF-8"));

		}
		return result.toString();
	}

	public void passwordDialog(){

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.client_name,null);
		dialogBuilder.setView(dialogView);

		final EditText edtClient = (EditText) dialogView.findViewById(R.id.edtClientName);
		edtClient.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		final String password = manager.get(SessionManager.PASSWORD);//"Admin2018";


		dialogBuilder.setTitle("Password");
		dialogBuilder.setMessage("Enter password below");
		dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//do something with edt.getText().toString();
				if(edtClient.getText().length() > 0 ){

					String userInput = edtClient.getText().toString();
					if(userInput.equals(password)){

						//Bundle dataBundle = new Bundle();
						//dataBundle.putString("uid", uid);
						Intent intent = new Intent(getApplicationContext(), AppSettings.class);
						//intent.putExtras(dataBundle);
						startActivity(intent);
					}else{
						Toast.makeText(getApplicationContext(),"Incorrect password",Toast.LENGTH_SHORT).show();
					}


				}else{

					Toast.makeText(getApplicationContext(),"Please enter password to proceed",Toast.LENGTH_SHORT).show();
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

	public void showToast(String message)
	{
		Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
	}


	public void downloadDialog(final String url,String path,int status){

		AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
		builder.setTitle("Application update ");
		final LayoutInflater inflater = LayoutInflater.from(MenuActivity.this);
		View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
		fpImage = (ImageView) vl.findViewById(R.id.imageView1);
		tvFpStatus= (TextView) vl.findViewById(R.id.textview1);

		if(status == 1){
			fpImage.setImageResource(R.drawable.approve);
			tvFpStatus.setText("You have the latest version");
		}else if(status == 0){
			fpImage.setImageResource(R.drawable.caution);
			tvFpStatus.setText("Please download latest version");
		}
		builder.setView(vl);
		builder.setCancelable(false);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//SerialPortManager.getInstance().closeSerialPort();
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Download here
				new DownloadFile().execute(url);
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
	}

	public void msgDialog(String message,int status){
		AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
		builder.setTitle("Application update");
		final LayoutInflater inflater = LayoutInflater.from(MenuActivity.this);
		View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
		fpImage = (ImageView) vl.findViewById(R.id.imageView1);
		tvFpStatus= (TextView) vl.findViewById(R.id.textview1);

		if(status == 1){
			fpImage.setImageResource(R.drawable.approve);
			tvFpStatus.setText(message);
		}else if(status == 0){
			fpImage.setImageResource(R.drawable.caution);
			tvFpStatus.setText(message);
		}
		builder.setView(vl);
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

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

	}

	public class DownloadFile extends AsyncTask<String, Integer, String> {


		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pDialog.show();

		}

		@Override
		protected String doInBackground(String... Url) {
			try {


				Log.e("URL+++++++++:", Url[0]);

				URL url = new URL(Url[0]);
				// String fileName = Url[1];
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				// URLConnection connection = url.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				connection.connect();

				String val = String.valueOf(url);
				int indexOfString = val.indexOf("alos");
				int nxtSla = val.indexOf("/", indexOfString) + 1;
				String value = val.substring(nxtSla);
				String fileName = value;



				// Detect the file lenghth
				int fileLength = connection.getContentLength();

				// Locate storage location

				String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
				File folder = new File(extStorageDirectory, "ALOS");
				folder.mkdir();

                /*String filepath = Environment.getExternalStorageDirectory()
                        .getPath();*/

				// Download the file
				InputStream input = new BufferedInputStream(url.openStream());

				// Save the downloaded file
				OutputStream output = new FileOutputStream(folder + "/"
						+ fileName);

				apkPath =extStorageDirectory+"/ALOS/"+fileName;

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					// Publish the progress
					publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}

				// Close connection
				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				// Error Log
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// super.onProgressUpdate(progress);
			// Update the progress dialog
			pDialog.setProgress(progress[0]);
			// Dismiss the progress dialog
			//mProgressDialog.dismiss();
		}

		protected void onPostExecute(String file_url) {


			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
				startActivity(intent);

				Toast.makeText(MenuActivity.this, apkPath, Toast.LENGTH_SHORT).show();
				//msgDialog("Install app",0);
			}

			/*Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					// Actions to do after 10 seconds


				}
			}, 5000);*/
		}

	}

	public void openFile(){

		String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		String path=extStorageDirectory+"ALOS/app-debug.apk";
		File myFile = new File(path);

		if(myFile.exists()){

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
			startActivity(intent);

			//Toast.makeText(MenuActivity.this, apkPath, Toast.LENGTH_SHORT).show();
		}else{

			showToast(path + " missing");
		}

	}
}
