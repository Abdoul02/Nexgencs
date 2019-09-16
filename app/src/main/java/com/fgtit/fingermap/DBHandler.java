package com.fgtit.fingermap;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fgtit.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Abdoul on 27-06-2016.
 */
public class DBHandler  extends SQLiteOpenHelper {

    User user;



    public DBHandler(Context applicationcontext) {
        super(applicationcontext, "user.db", null, 5);
    }

    //Creates Table
    @Override
    public void onCreate(SQLiteDatabase database) {
        String query,query1,query2,pineUsers,cost_center,product;
        query = "CREATE TABLE users ( userId INTEGER,idNum TEXT, name TEXT, finger1 TEXT, finger2 TEXT, status TEXT," +
                "shifts_id INTEGER, shift_type INTEGER, costCenterId INTEGER,card TEXT)";

        query1="CREATE TABLE project ( asset TEXT,requestedBy TEXT, site TEXT, location TEXT, criticalAsset TEXT" +
                ", progress INTEGER, dateReq TEXT,workReq TEXT,id INTEGER,dateDone TEXT)";
        query2="CREATE TABLE pine (pine_id INTEGER PRIMARY KEY, deliveryN TEXT,tag TEXT,status TEXT, diameter TEXT, " +
                "date TEXT)";
        pineUsers="CREATE TABLE pine_user(pUserId INTEGER PRIMARY KEY,userId INTEGER)";
        cost_center = "CREATE TABLE cost_center(cost_center_id INTEGER PRIMARY KEY, cost_center_name TEXT)";
        product = "CREATE TABLE product(id INTEGER PRIMARY KEY,name TEXT, code TEXT)";


        database.execSQL(query);
        database.execSQL(query1);
        database.execSQL(query2);
        database.execSQL(cost_center);
        database.execSQL(product);
        database.execSQL(pineUsers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {

        if(current_version < 2){

            database.execSQL("ALTER TABLE users ADD COLUMN shifts_id INTEGER");
            database.execSQL("ALTER TABLE users ADD COLUMN shift_type INTEGER");
            database.execSQL("ALTER TABLE users ADD COLUMN costCenterId INTEGER");
            database.execSQL("CREATE TABLE project ( asset TEXT,requestedBy TEXT, site TEXT, location TEXT, criticalAsset TEXT" +
                    ", progress INTEGER, dateReq TEXT,workReq TEXT,id INTEGER,dateDone TEXT)");

            database.execSQL("CREATE TABLE pine ( pine_id INTEGER PRIMARY KEY,deliveryN TEXT, tag TEXT, status TEXT, diameter TEXT" +
                    ", date TEXT)");
            database.execSQL("CREATE TABLE pine_user(pUserId INTEGER PRIMARY KEY,userId INTEGER)");
            database.execSQL("CREATE TABLE cost_center(cost_center_id INTEGER PRIMARY KEY, cost_center_name TEXT)");
            database.execSQL("CREATE TABLE product(id INTEGER PRIMARY KEY,name TEXT, code TEXT)");
        }else if(current_version > 2 && current_version < 4){

            database.execSQL("CREATE TABLE pine ( pine_id INTEGER PRIMARY KEY,deliveryN TEXT, tag TEXT, status TEXT, diameter TEXT" +
                    ", date TEXT)");
            database.execSQL("CREATE TABLE pine_user(pUserId INTEGER PRIMARY KEY,userId INTEGER)");
            database.execSQL("CREATE TABLE cost_center(cost_center_id INTEGER PRIMARY KEY, cost_center_name TEXT)");
            database.execSQL("CREATE TABLE product(id INTEGER PRIMARY KEY,name TEXT, code TEXT)");
        }
        else if(current_version > 3 && current_version < 5){
            database.execSQL("ALTER TABLE users ADD COLUMN card TEXT");
            database.execSQL("CREATE TABLE cost_center(cost_center_id INTEGER PRIMARY KEY, cost_center_name TEXT)");
            database.execSQL("CREATE TABLE product(id INTEGER PRIMARY KEY,name TEXT, code TEXT)");
        }

        else if (current_version > 4){

            database.execSQL("CREATE TABLE cost_center(cost_center_id INTEGER PRIMARY KEY, cost_center_name TEXT)");
            database.execSQL("CREATE TABLE product(id INTEGER PRIMARY KEY,name TEXT, code TEXT)");
        }

       // database.execSQL(query);
        //database.execSQL(query1);
        //onCreate(database);
    }

    /**
     * Inserts User into SQLite DB
     *
     */

    //User
    public void insertUser(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", user.getuId());
        values.put("idNum", user.getIdNum());
        values.put("name", user.getuName());
        values.put("finger1", user.getFinger1());
        values.put("finger2", user.getFinger2());
        values.put("status", "yes");
        values.put("shifts_id",user.getShifts_id());
        values.put("shift_type", user.getShift_type());
        values.put("costCenterId", user.getCostCenterId());
        values.put("card",user.getCard());
        database.insert("users", null, values);
        database.close();
    }
    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<User> getAllUsers() {
        ArrayList<User> usersList;
        usersList = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM users";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                User user = new User();
                HashMap<String, String> map = new HashMap<String, String>();
                user.setuId(cursor.getInt((cursor.getColumnIndex("userId"))));
                user.setIdNum(cursor.getString((cursor.getColumnIndex("idNum"))));
                user.setuName(cursor.getString((cursor.getColumnIndex("name"))));
                user.setFinger1(cursor.getString(cursor.getColumnIndex("finger1")));
                user.setFinger2(cursor.getString(cursor.getColumnIndex("finger2")));
                user.setCostCenterId(cursor.getInt(cursor.getColumnIndex("costCenterId")));
                user.setShift_type(cursor.getInt(cursor.getColumnIndex("shift_type")));
                user.setShifts_id(cursor.getInt(cursor.getColumnIndex("shifts_id")));
                user.setCard(cursor.getString(cursor.getColumnIndex("card")));
               /* map.put("userId", cursor.getString(0));
                map.put("name", cursor.getString(2));*/
                usersList.add(user);

            } while (cursor.moveToNext());
        }
        database.close();
        return usersList;
    }
    public Cursor getUser(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT  * FROM users WHERE userId ='"+id+"'", null );
        return res;
    }
    public void update(User user){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", user.getuId());
        values.put("idNum", user.getIdNum());
        values.put("name", user.getuName());
        values.put("finger1", user.getFinger1());
        values.put("finger2", user.getFinger2());
        values.put("card",user.getCard());
        values.put("status","no");

        db.update("users", values, "userId = ?", new String[]{String.valueOf(user.getuId())});
    }
    public void delete(String id){

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", "idNum = ?", new String[]{id});
        db.close();
    }
    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users",null,null);
        db.close();
    }
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM users where status = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM users where status = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("userId", cursor.getString(0));
                map.put("idNum", cursor.getString(1));
                map.put("name", cursor.getString(2));
                map.put("finger1", cursor.getString(3));
                map.put("finger2", cursor.getString(4));
                map.put("card", cursor.getString(9));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public Cursor getUserId(String idNumber){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT  * FROM users WHERE idNum ='"+idNumber+"'", null );
        return res;
    }

    //Cost center
    public void insertCostCenter(int cost_center_id, String cost_center_name){

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cost_center_id", cost_center_id);
        values.put("cost_center_name", cost_center_name);
        database.insert("cost_center",null,values);
        database.close();

    }
    public void deleteCostCenter(){

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("cost_center",null,null);
        db.close();
    }
    public List<String> getAllCostCenter(){

        List<String> cost_center_list = new ArrayList<String>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from cost_center", null);

        if (cursor.moveToFirst()) {
            do {
                cost_center_list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        database.close();
        return cost_center_list;
    }
    public int getCostCenterId(String name){

        String selectQuery = "SELECT * FROM cost_center where cost_center_name ='"+name+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        int cost_center_id = 0;

        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                cost_center_id = cursor.getInt((cursor.getColumnIndex("cost_center_id")));

            } while (cursor.moveToNext());
        }
        database.close();
        return cost_center_id;

    }

    //product
    public void insertProduct(int id, String name, String code){

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("code", code);
        database.insert("product",null,values);
        database.close();
    }
    public void deleteProduct(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("product",null,null);
        db.close();

    }
    public List<String> getAllProduct(){

        List<String> product_list = new ArrayList<String>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from product", null);

        if (cursor.moveToFirst()) {
            do {
                product_list.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        database.close();
        return product_list;
    }
    public int getProductId(String name){

        String selectQuery = "SELECT * FROM product where name ='"+name+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        int product_id = 0;

        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                product_id = cursor.getInt((cursor.getColumnIndex("id")));

            } while (cursor.moveToNext());
        }
        database.close();
        return product_id;

    }

    //Pine
    public void insertPine(HashMap<String, String> queryValues){

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("deliveryN", queryValues.get("deliveryN"));
        values.put("tag", queryValues.get("tag"));
        values.put("status",queryValues.get("status"));
        values.put("diameter",queryValues.get("diameter"));
        values.put("date",queryValues.get("date"));
        database.insert("pine",null,values);
        database.close();

    }
    public void insertPineUsers(int userId){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", userId);
        database.insert("pine_user",null,values);
        database.close();
    }
    public void deletePine(int pine_id){

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("pine","pine_id = ?",new String[]{String.valueOf(pine_id)});
        db.close();
    }
    public void deletePineUsers(){

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("pine_user",null,null);
        db.close();
    }
    public List<String> getPineUsers(){

        List<String> pineUserList = new ArrayList<String>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from pine_user", null);
        if (cursor.moveToFirst()) {
            do {
                pineUserList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        database.close();
        return pineUserList;
    }
    public String pineJSON(String deliveryN){

        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM pine WHERE deliveryN ='"+deliveryN+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("pine_id", cursor.getString(0));
                map.put("deliveryN", cursor.getString(1));
                map.put("tag", cursor.getString(2));
                map.put("status", cursor.getString(3));
                map.put("diameter", cursor.getString(4));
                map.put("date", cursor.getString(5));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);

    }

    //Project
    public void insertProject(HashMap<String, String> queryValues){

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("asset", queryValues.get("asset"));
        values.put("requestedBy", queryValues.get("requestedBy"));
        values.put("site",queryValues.get("site"));
        values.put("location",queryValues.get("location"));
        values.put("criticalAsset",queryValues.get("criticalAsset"));
        values.put("progress",queryValues.get("progress"));
        values.put("dateReq", queryValues.get("dateReq"));
        values.put("workReq", queryValues.get("workReq"));
        values.put("id", queryValues.get("id"));
        values.put("dateDone", queryValues.get("dateDone"));
        database.insert("project",null,values);
        database.close();

    }
    public void deleteProject(String criticalAsset){

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("project","criticalAsset = ?",new String[]{criticalAsset});
        db.close();
    }
    public void updateProDate(int prog,String date,String reqB,String sit,String local,String asset,String dateReq, String criticalAsset){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("progress", prog);
        values.put("dateDone", date);
        values.put("dateReq", dateReq);
        values.put("requestedBy", reqB);
        values.put("site", sit);
        values.put("location", local);
        values.put("asset", asset);
        db.update("project", values, "criticalAsset = ?", new String[]{criticalAsset});

    }
    public ArrayList<HashMap<String, String>> getAllProjects() {

        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM project";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("criticalAsset",cursor.getString(4));
                // map.put("userId", cursor.getString(1));
                map.put("requestedBy", cursor.getString(1));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }
    public Cursor getProject(String criticalAsset){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT  * FROM project WHERE criticalAsset ='"+criticalAsset+"'", null );
        return res;
    }




    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update users set status = '"+ status +"' where userId="+"'"+ id +"'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }
    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */

    public int checkTag(String tag,String deliveryN){

        int count = 0;
        String selectQuery = "SELECT * FROM pine where tag = '"+tag+"' AND deliveryN = '"+deliveryN+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }
    public int checkRemainingTag(String deliveryN){
        int count = 0;
        String selectQuery = "SELECT  * FROM pine WHERE deliveryN ='"+deliveryN+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }



}
