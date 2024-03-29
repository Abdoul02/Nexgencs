package com.fgtit.fingermap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.fgtit.data.GlobalData;
import com.fgtit.data.ImageSimpleAdapter;
import com.fgtit.data.UserItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import android.text.TextPaint;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class EmployeesActivity extends AppCompatActivity {

	private TextView tv1,tv2,tv3;
	private TextPaint tp1,tp2,tp3;
	
	private ListView listView;
	private List<Map<String, Object>> mData;
	private SimpleAdapter adapter;
	
	private Timer startTimer; 
    private TimerTask startTask; 
    Handler startHandler;
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_employees);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		tv1=(TextView)findViewById(R.id.textView1);
		tv2=(TextView)findViewById(R.id.textView2);
		tv3=(TextView)findViewById(R.id.textView3);
		tp1 = tv1 .getPaint();
		tp2 = tv2 .getPaint();
		tp3 = tv3 .getPaint();
		
		tv1.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				SetTvStyle(1);
				addWegit(1);
			}
		});
		
		tv2.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				SetTvStyle(2);
				addWegit(2);
			}
		});
		
		tv3.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				SetTvStyle(3);
				addWegit(3);
			}
		});
		
		listView=(ListView) findViewById(R.id.listView1);	
		mData = new ArrayList<Map<String, Object>>();
		adapter = new ImageSimpleAdapter(this,mData,R.layout.listview_personitem,
				new String[]{"title","info","img"},
				new int[]{R.id.title,R.id.info,R.id.img});
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override  
			public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {  
				//Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);  
				
				if(GlobalData.getInstance().userList.size()>0){
				
				Intent intent = new Intent(EmployeesActivity.this, EmployeeActivity.class);
					Bundle bundle = new Bundle();
			    	bundle.putInt("pos",pos);
			    	intent.putExtras(bundle);
			    	//intent.putExtra("pos",pos);
			    	//startActivity(intent);
			    	startActivityForResult(intent,0);
				}
				switch(pos)
				{
				case 0:
					{
					}
					break;
				case 1:
					{
					}
					break;
				case 2:
					{
					}
					break;
				}
			}             
		});

		SetTvStyle(1);
		//addWegit(1);
		TimerStart();
	}

	 public void TimerStart(){
			startTimer = new Timer(); 
			startHandler = new Handler() { 
	            @SuppressLint("HandlerLeak")
				@Override 
	            public void handleMessage(Message msg) { 
	            	addWegit(1);
	                super.handleMessage(msg); 
	            }
	        };
	        startTask = new TimerTask() { 
	            @Override 
	            public void run() { 
	            	TimeStop();
	            	
	                Message message = new Message(); 
	                message.what = 1; 
	                startHandler.sendMessage(message); 
	            } 
	        }; 
	        startTimer.schedule(startTask, 100, 100); 
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (resultCode)	{
		case 0:	{
				SetTvStyle(1);
				addWegit(1); 
			}
			break;
		case 1:	{

				//Bundle bl= data.getExtras();
				//refdata=bl.getByteArray("refdata");
			}
			break;
		case 2:
			break;
		default:
			break;
		}
	}
	
	private void SetTvStyle(int i){
		switch(i){
		case 1:
			tv1.setTextColor(Color.RED);
			tp1.setFakeBoldText(true);
			tv2.setTextColor(Color.BLACK);
			tp2.setFakeBoldText(false);
			tv3.setTextColor(Color.BLACK);
			tp3.setFakeBoldText(false);
			break;
		case 2:
			tv1.setTextColor(Color.BLACK);
			tp1.setFakeBoldText(false);
			tv2.setTextColor(Color.RED);
			tp2.setFakeBoldText(true);
			tv3.setTextColor(Color.BLACK);
			tp3.setFakeBoldText(false);
			break;
		case 3:
			tv1.setTextColor(Color.BLACK);
			tp1.setFakeBoldText(false);
			tv2.setTextColor(Color.BLACK);
			tp2.setFakeBoldText(false);
			tv3.setTextColor(Color.RED);
			tp3.setFakeBoldText(true);
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.employees, menu);
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
			case R.id.action_enroll:{
					Intent intent = new Intent(EmployeesActivity.this, EnrollActivity.class);
					startActivityForResult(intent,0);
				}				
				return true;
			case R.id.action_detail:{
					if(GlobalData.getInstance().userList.size()>0){
						Intent intent = new Intent(EmployeesActivity.this, EmployeeActivity.class);
						intent.putExtra("pos",0);
						startActivityForResult(intent,0);
					}
				}
				return true;				
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	public void addWegit(int tp) {  
        TableLayout table = (TableLayout) findViewById(R.id.tablelayout);  
        table.setStretchAllColumns(true);  
        
        table.removeAllViews();
        
        int n=MainActivity.personList.size();
        
        for (int i = 0; i < n; i++) {  
            TableRow tablerow = new TableRow(EmployeesActivity.this);  
            tablerow.setBackgroundColor(Color.rgb(222, 220, 210));
            
            Person person=MainActivity.personList.get(i);
            
            for (int j = 0; j < 5; j++) {  
            	TextView tv = new TextView(EmployeesActivity.this);
            	tv.setBackgroundColor(Color.rgb(255, 255,255));
            	switch(j){
            	case 0:tv.setText(person.getName());	break;
            	case 1:tv.setText(person.getId());	break;
            	case 2:tv.setText("type");	break;
            	case 3:tv.setText("M");	break;
            	case 4:tv.setText("Active");	break;
            	}            	
            	TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            	lp.setMargins(1, 1, 1, 1);
                tablerow.addView(tv,lp);  
                }  
            table.addView(tablerow, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
        }  
    } 
	*/

	public void addWegit(int tp) {  
		mData.clear();
		adapter.notifyDataSetChanged();
        
        int n=GlobalData.getInstance().userList.size();
        if(n<1){
        	Map<String, Object> map = new HashMap<String, Object>();
    		map.put("title", getString(R.string.txt_enrolinfo1));
    		map.put("info", getString(R.string.txt_enrolinfo2));
    		//map.put("img",R.drawable.guest);
    		mData.add(map);
    		adapter.notifyDataSetChanged();
        }else{
        	for (int i = 0; i < n; i++) {  
                UserItem person=GlobalData.getInstance().userList.get(i);
                
                Map<String, Object> map = new HashMap<String, Object>();
        		map.put("title", person.name);
        		map.put("info", person.id);
        		/*
        		if(person.photo.length()>1000)
        			map.put("img", Bytes2Bimap(ExtApi.Base64ToBytes(person.photo)));
        		else
        			map.put("img", ExtApi.LoadBitmap(getResources(),R.drawable.guest));
        		*/
        		mData.add(map);
        		adapter.notifyDataSetChanged();
            }
        }
        
    }
        
	private Bitmap Bytes2Bimap(byte[] b){  
        if(b.length!=0){  
            return BitmapFactory.decodeByteArray(b, 0, b.length);  
        }  
        else {  
            return null;  
        }  
	} 
}
