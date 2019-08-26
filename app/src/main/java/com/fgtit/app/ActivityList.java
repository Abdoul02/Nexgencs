package com.fgtit.app;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

//public class ActivityList extends Application{
public class ActivityList {	
	private List<Activity> activityList = new LinkedList<Activity>();	
	private static ActivityList instance;
	private Context pcontext;
	private Context ccontext;
	public boolean IsUseNFC=false;
	public boolean IsUpImage = true;
	
	private ActivityList(){ 
    }
    
    public static ActivityList getInstance() {
    	if(null == instance) {
    		instance = new ActivityList();
    	}
    	return instance;
    }

    public void setMainContext(Context context){
    	pcontext=context;
    }
    
    public void setCurrContext(Context context){
    	ccontext=context;
    }
    
    public void Relogon(){
    	for(Activity activity:activityList) {
    		activity.finish();
    	}
    }
    
    public Context getCurrContext(){
    	return ccontext;
    }
    
    public void addActivity(Activity activity){
    	activityList.add(activity);
    }
    
    public void removeActivity(Activity activity){
    	activityList.remove(activity);
    }
    
    public void exit(){
    	for(Activity activity:activityList) {
    		activity.finish();
    	}
    	System.exit(0);
    }
    
}
