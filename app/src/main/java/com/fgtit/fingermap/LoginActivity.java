package com.fgtit.fingermap;

import java.util.HashMap;
import com.fgtit.models.JSONParser;
import com.fgtit.models.SessionManager;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

	private EditText editText1,editText2;

	JSONParser jsonParser = new JSONParser();

	// Session Manager Class
	SessionManager session;

	private long exitTime = 0;

	private ProgressDialog pDialog;

	private static final String LOGIN_URL = "http://www.nexgencs.co.za/alos/login.php";

	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_UID = "id";
	private static final String TAG_COMP ="compID";
	private static final String TAG_NAME = "name";
	private static final String TAG_PASSWORD = "password";
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logon);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setBackgroundDrawable(new
				ColorDrawable(Color.parseColor("#020969")));

		// Session Manager
		session = new SessionManager(getApplicationContext());

		Button btn01=(Button)findViewById(R.id.button1);
		editText1=(EditText)findViewById(R.id.editText1);
		editText2=(EditText)findViewById(R.id.editText2);
		Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

		btn01.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {

				String username = editText1.getText().toString().trim();
				String password = editText2.getText().toString().trim();
				if(username.isEmpty() || password.isEmpty()){

				Toast.makeText(getApplicationContext(),"Please enter both email and password",Toast.LENGTH_SHORT).show();
				}
				else
				new PostAsync().execute(username,password);
			}
		});

	}




	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			exitApplication();
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_HOME){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void exitApplication(){
		if((System.currentTimeMillis()-exitTime) > 2000){
			Toast.makeText(getApplicationContext(), getString(R.string.txt_exitinfo), Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		}
		else{
			finish();
			System.exit(0);
			//AppList.getInstance().exit();
		}
	}
	
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
		case android.R.id.home:
			finish();
			System.exit(0);
			//return true;
		case R.id.action_reture:
			finish();
			System.exit(0);
			//return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (resultCode){
		case 0:{
				this.setResult(0);
				this.finish();
			}
			break;
		case 1:{
				this.setResult(1);
				this.finish();
			}
			break;
		case 2:
			break;
		default:
			break;
		}
	}


	class PostAsync extends AsyncTask<String, String, JSONObject> {

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... args) {

			try {

				HashMap<String, String> params = new HashMap<>();
				params.put("name", args[0]);
				params.put("password", args[1]);

				Log.d("request", "starting");

				JSONObject json = jsonParser.makeHttpRequest(
						LOGIN_URL, "POST", params);

				if (json != null) {
					Log.d("JSON result", json.toString());

					return json;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(JSONObject json) {

			int success = 0;
			String message = "";
			String name = "";
			String password="";
			int comp = 0;
			String email = "";
			int uID = 0;

			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}

			if (json != null) {

				/*Toast.makeText(LoginActivity.this, json.toString(),
						Toast.LENGTH_LONG).show();*/

				try {
					success = json.getInt(TAG_SUCCESS);
					message = json.getString(TAG_MESSAGE);
					comp = json.getInt(TAG_COMP);
					email = json.getString(TAG_EMAIL);
					uID = json.getInt(TAG_UID);
					name = json.getString(TAG_NAME);
					password = json.getString(TAG_PASSWORD);


				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (success == 1) {
				Log.d("Success!", message);

				session.createLoginSession(email,String.valueOf(comp),String.valueOf(uID),name,password);
				Intent i = new Intent(LoginActivity.this, MainActivity.class);
				finish();
				startActivity(i);
				//return json.getString(TAG_MESSAGE);
			}else if (success == 0){
				Log.d("Failure", message);
				Toast.makeText(LoginActivity.this, message,
						Toast.LENGTH_LONG).show();
			}else{

				Toast.makeText(LoginActivity.this, "Login fail, please ensure you have data connection",
						Toast.LENGTH_LONG).show();
			}
		}

	}
    
}
