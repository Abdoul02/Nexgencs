package com.fgtit.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fgtit.fingermap.MenuActivity;

import java.util.HashMap;

/**
 * Created by Abdoul on 03-10-2018.
 */

public class AdminData {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "admin";

    // All Shared Preferences Keys
    private static final String IS_LOCKED = "IsLocked";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    public static final String PASSWORD = "password";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    public static final String KEY_COMPID = "compID";
    public static final String KEY_UID = "id";

    // Constructor
    public AdminData(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void storePassword(String password){

        //storing password ID in pref
        editor.putString(PASSWORD,password);

        // commit changes
        editor.commit();
    }

    public void lockFingerReg(boolean state){

        editor.putBoolean(IS_LOCKED,state);
        editor.commit();
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getDataDetails(){
        HashMap<String, String> data = new HashMap<String, String>();
        data.put(PASSWORD,pref.getString(PASSWORD,null));
        // return user
        return data;
    }

    /**
     * Clear session details
     * */
    public void ClearPassword(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, MenuActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    // Get Login State
    public boolean isLocked(){
        return pref.getBoolean(IS_LOCKED, true);
    }

}
