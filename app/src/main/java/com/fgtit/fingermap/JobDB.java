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

import com.fgtit.models.Delivery;
import com.fgtit.models.DrydenJobCard;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.ERDjobCard;
import com.fgtit.models.EcCustomer;
import com.fgtit.models.EcProduct;
import com.fgtit.models.JobCard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Abdoul on 29-07-2016.
 */
public class JobDB extends SQLiteOpenHelper {


    private static final String TAG = "JobDB";
    public static final String ERD_TABLE = "erd_job";
    public static final String SUB_TASK_TABLE = "sub_task_table";
    public static final String DRYDEN_TABLE = "dryden_job";
    public static final String CATEGORY_TABLE = "category";
    public static final String QUESTION_TABLE = "question";
    public static final String VEHICLE_TABLE = "vehicle";
    public static final String DELIVERY_TABLE = "delivery";
    public static final String ADDRESS_TABLE = "address";

    public JobDB(Context applicationContext) {
        super(applicationContext, "androidsqlite.db", null, 10);
    }

    //Creates Table
    @Override
    public void onCreate(SQLiteDatabase database) {
        String query, query2, query3, btScale, pine, ec_job,
                ec_job_info, ec_material, ec_customer, ec_product, erd_job_card, sub_task, local_pict,
                dryden_job, strucMacCategory, strucMacQuestion, strucMacVehicle, delivery, address;
        query = "CREATE TABLE jobcard ( jobID INTEGER PRIMARY KEY, name TEXT, description TEXT, " +
                "location TEXT, assignee TEXT, approvedBy TEXT,customer TEXT, progress INTEGER, start TEXT, end TEXT,jobCode TEXT,attachment TEXT," +
                "office TEXT)";

        query2 = "CREATE TABLE jobCard_info(jId INTEGER PRIMARY KEY,comment TEXT, dat TEXT, tim TEXT, jobCode TEXT, udpateStatus TEXT,startKm TEXT,endKm text)";

        query3 = "CREATE TABLE record ( recID INTEGER PRIMARY KEY, userId TEXT, userName TEXT, " +
                "udpateStatus TEXT, dat TEXT, lat TEXT, lng TEXT, id INTEGER, status TEXT,imei TEXT," +
                "shifts_id INTEGER, shift_type INTEGER, costCenterId INTEGER)";

        btScale = "CREATE TABLE bt_scale(scale_id INTEGER PRIMARY KEY, userId TEXT, weight TEXT, date TEXT, time TEXT,imei TEXT," +
                "status TEXT, product_id INTEGER, cost_center_id INTEGER)";

        pine = "CREATE TABLE pine(pine_id INTEGER PRIMARY KEY, userId TEXT, deliveryNote TEXT, diggersrestDN TEXT, date TEXT)";

        ec_job = "CREATE TABLE ec_job(id INTEGER PRIMARY KEY,job_id INTEGER,company TEXT)";

        ec_job_info = "CREATE TABLE ec_job_info(ec_id INTEGER PRIMARY KEY,id INTEGER, job_id INTEGER, work_undertaken TEXT, clock_time TEXT,status TEXT,km TEXT," +
                "travelling_time INTEGER)";

        ec_material = "CREATE TABLE ec_material(material_id INTEGER PRIMARY KEY,id INTEGER,job_id INTEGER, quantity INTEGER,material_used TEXT," +
                "unit_price TEXT)";

        ec_customer = "CREATE TABLE ec_customer(customer_id INTEGER PRIMARY KEY,id INTEGER,name TEXT)";

        ec_product = "CREATE TABLE ec_product(product_id INTEGER PRIMARY KEY, id INTEGER, name TEXT, price TEXT)";

        erd_job_card = "CREATE TABLE erd_job(local_id INTEGER PRIMARY KEY, id INTEGER, supervisor_id INTEGER," +
                "name TEXT, job_no TEXT, description TEXT, address TEXT, progress TEXT, from_date TEXT, to_date TEXT)";

        sub_task = "CREATE TABLE sub_task_table(task_id INTEGER PRIMARY KEY,id INTEGER, job_card_id INTEGER, name TEXT)";

        local_pict = "CREATE TABLE local_pict(pict_id INTEGER PRIMARY KEY, path TEXT,job_id INTEGER, job_no TEXT)";

        dryden_job = "CREATE TABLE dryden_job(local_id INTEGER PRIMARY KEY, id INTEGER, job_name TEXT, job_no TEXT," +
                "qc_no TEXT, description TEXT, drawing_no TEXT, issue_date TEXT, supervisor_id INTEGER,checked INTEGER)";

        strucMacCategory = "CREATE TABLE category(local_id INTEGER PRIMARY KEY, id INTEGER, category TEXT)";
        strucMacQuestion = "CREATE TABLE question(local_id INTEGER PRIMARY KEY, id INTEGER, question TEXT, category_id INTEGER)";
        strucMacVehicle = "CREATE TABLE vehicle(local_id INTEGER PRIMARY KEY,id INTEGER, reg_no TEXT,licence_disc TEXT, km TEXT)";
        delivery = "CREATE TABLE delivery(local_id INTEGER PRIMARY KEY, id INTEGER,delivery_note TEXT,customer TEXT, driver_id INTEGER," +
                "vehicle_id INTEGER,delivery_date TEXT,delivery_time TEXT,origin_address TEXT, latitude TEXT, longitude TEXT)";
        address = "CREATE TABLE address(local_id INTEGER PRIMARY KEY,id INTEGER, delivery_id INTEGER, sequence INTEGER, address TEXT," +
                "latitude TEXT, longitude TEXT)";

        database.execSQL(query);
        database.execSQL(query2);
        database.execSQL(query3);
        database.execSQL(btScale);
        database.execSQL(pine);
        database.execSQL(ec_job);
        database.execSQL(ec_job_info);
        database.execSQL(ec_material);
        database.execSQL(ec_customer);
        database.execSQL(ec_product);
        database.execSQL(erd_job_card);
        database.execSQL(sub_task);
        database.execSQL(local_pict);
        database.execSQL(dryden_job);
        database.execSQL(strucMacCategory);
        database.execSQL(strucMacQuestion);
        database.execSQL(strucMacVehicle);
        database.execSQL(delivery);
        database.execSQL(address);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {

        //If version is one, edit the table and add the following columns and add a new tables
        if (current_version == 1) {
            database.execSQL("ALTER TABLE record ADD COLUMN shifts_id INTEGER");
            database.execSQL("ALTER TABLE record ADD COLUMN shift_type INTEGER");
            database.execSQL("ALTER TABLE record ADD COLUMN costCenterId INTEGER");
            database.execSQL("CREATE TABLE bt_scale(scale_id INTEGER PRIMARY KEY, userID TEXT, weight TEXT, date TEXT, time TEXT,imei TEXT," +
                    "status TEXT)");
            database.execSQL("CREATE TABLE pine(pine_id INTEGER PRIMARY KEY, userId TEXT, deliveryNote TEXT, diggersrestDN TEXT, date TEXT)");
            database.execSQL("ALTER TABLE jobcard ADD COLUMN office TEXT");
            database.execSQL("ALTER TABLE bt_scale ADD COLUMN product_id INTEGER");
            database.execSQL("ALTER TABLE bt_scale ADD COLUMN costcenter_id INTEGER");
            database.execSQL("CREATE TABLE ec_job(id INTEGER PRIMARY KEY,job_id INTEGER,company TEXT)");
            database.execSQL("CREATE TABLE ec_job_info(ec_id INTEGER PRIMARY KEY,id INTEGER, job_id INTEGER, work_undertaken TEXT, clock_time TEXT,status TEXT,km TEXT," +
                    "travelling_time INTEGER)");
            database.execSQL("CREATE TABLE ec_material(material_id INTEGER PRIMARY KEY,id INTEGER,job_id INTEGER, quantity INTEGER,material_used TEXT," +
                    "unit_price TEXT)");

        } else if (current_version == 3) {
            database.execSQL("CREATE TABLE bt_scale(scale_id INTEGER PRIMARY KEY, userId TEXT, weight TEXT, date TEXT, time TEXT,imei TEXT," +
                    "status TEXT)");
            database.execSQL("CREATE TABLE pine(pine_id INTEGER PRIMARY KEY, userId TEXT, deliveryNote TEXT, diggersrestDN TEXT, date TEXT)");
            database.execSQL("ALTER TABLE jobcard ADD COLUMN office TEXT");
            database.execSQL("ALTER TABLE bt_scale ADD COLUMN product_id INTEGER");
            database.execSQL("ALTER TABLE bt_scale ADD COLUMN costcenter_id INTEGER");
            database.execSQL("CREATE TABLE ec_job(id INTEGER PRIMARY KEY,job_id INTEGER,company TEXT)");
            database.execSQL("CREATE TABLE ec_job_info(ec_id INTEGER PRIMARY KEY,id INTEGER, job_id INTEGER, work_undertaken TEXT, clock_time TEXT,status TEXT,km TEXT," +
                    "travelling_time INTEGER)");
            database.execSQL("CREATE TABLE ec_material(material_id INTEGER PRIMARY KEY,id INTEGER,job_id INTEGER, quantity INTEGER,material_used TEXT," +
                    "unit_price TEXT)");
        } else if (current_version == 5) {

            database.execSQL("CREATE TABLE ec_job(id INTEGER PRIMARY KEY,job_id INTEGER,company TEXT)");
            database.execSQL("CREATE TABLE ec_job_info(ec_id INTEGER PRIMARY KEY,id INTEGER, job_id INTEGER, work_undertaken TEXT, clock_time TEXT,status TEXT,km TEXT," +
                    "travelling_time INTEGER)");
            database.execSQL("CREATE TABLE ec_material(material_id INTEGER PRIMARY KEY,id INTEGER,job_id INTEGER, quantity INTEGER,material_used TEXT," +
                    "unit_price TEXT)");
        } else if (current_version == 6) {

            database.execSQL("CREATE TABLE ec_customer(customer_id INTEGER PRIMARY KEY,id INTEGER,name TEXT)");
            database.execSQL("CREATE TABLE ec_product(product_id INTEGER PRIMARY KEY, id INTEGER, name TEXT, price TEXT)");
            database.execSQL("CREATE TABLE erd_job(local_id INTEGER PRIMARY KEY, id INTEGER, supervisor_id INTEGER," +
                    "name TEXT, job_no TEXT, description TEXT, address TEXT, progress TEXT, from_date TEXT, to_date TEXT)");
            database.execSQL("CREATE TABLE sub_task_table(task_id INTEGER PRIMARY KEY,id INTEGER, job_card_id INTEGER, name TEXT)");
        } else if (current_version == 7) {
            database.execSQL("CREATE TABLE local_pict(pict_id INTEGER PRIMARY KEY, path TEXT,job_id INTEGER, job_no TEXT)");
        } else if (current_version == 8) {
            database.execSQL("CREATE TABLE dryden_job(local_id INTEGER PRIMARY KEY, id INTEGER, job_name TEXT, job_no TEXT," +
                    "qc_no TEXT, description TEXT, drawing_no TEXT, issue_date TEXT, supervisor_id INTEGER, checked INTEGER)");
        } else if (current_version == 9) {
            database.execSQL("CREATE TABLE category(local_id INTEGER PRIMARY KEY, id INTEGER, category TEXT)");
            database.execSQL("CREATE TABLE question(local_id INTEGER PRIMARY KEY, id INTEGER, question TEXT, category_id INTEGER)");
            database.execSQL("CREATE TABLE vehicle(local_id INTEGER PRIMARY KEY,id INTEGER, reg_no TEXT,licence_disc TEXT, km TEXT)");
        } else if (current_version == 10) {
            database.execSQL("CREATE TABLE delivery(local_id INTEGER PRIMARY KEY, id INTEGER,delivery_note TEXT,customer TEXT, driver_id INTEGER," +
                    "vehicle_id INTEGER,delivery_date TEXT,delivery_time TEXT,origin_address TEXT, latitude TEXT, longitude TEXT)");
            database.execSQL("CREATE TABLE address(local_id INTEGER PRIMARY KEY,id INTEGER, delivery_id INTEGER, sequence INTEGER, address TEXT," +
                    "latitude TEXT, longitude TEXT)");
        }
    }

    /**
     * Inserts User into SQLite DB
     *
     * @param
     */


    //Job Card
    public void insertjobcard(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("jobID", queryValues.get("jobID"));
        values.put("name", queryValues.get("name"));
        values.put("description", queryValues.get("description"));
        values.put("location", queryValues.get("location"));
        values.put("assignee", queryValues.get("assignee"));
        values.put("progress", queryValues.get("progress"));
        values.put("approvedBy", queryValues.get("approvedBy"));
        values.put("customer", queryValues.get("customer"));
        values.put("start", queryValues.get("start"));
        values.put("end", queryValues.get("end"));
        values.put("jobCode", queryValues.get("jobCode"));
        values.put("attachment", queryValues.get("attachment"));
        values.put("office", queryValues.get("office"));
        database.insert("jobcard", null, values);
        database.close();
    }

    public void insertJInfo(String comment, String time, String date, String jobCode, String startKm, String endKm) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("comment", comment);
        values.put("dat", date);
        values.put("tim", time);
        values.put("jobCode", jobCode);
        values.put("udpateStatus", "no");
        values.put("startKm", startKm);
        values.put("endKm", endKm);
        database.insert("jobCard_info", null, values);
        database.close();

    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("jobcard", null, null);
        db.close();
    }

    public void deletejobcard(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("jobcard", "jobCode = ?", new String[]{id});
        db.close();
    }

    public void deleteJinfo(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("jobCard_info", "jobCode = ?", new String[]{id});
        db.close();
    }

    public void updateJob(String code, int prog) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("progress", prog);
        db.update("jobcard", values, "jobCode = ?", new String[]{code});
    }

    public Cursor getData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from jobcard where jobCode=" + id + "", null);
        return res;
    }

    public void insertPictPath(int job_id, String path, String job_no) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("job_id", job_id);
        values.put("path", path);
        values.put("job_no", job_no);
        database.insert("local_pict", null, values);
        database.close();
    }


    public void deletePictures(String job_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("local_pict", "job_id = ?", new String[]{job_id});
        db.close();
    }

    public void deletePicturesByPath(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("local_pict", "path = ?", new String[]{path});
        db.close();
    }

    public List<String> getPictures(String job_no) {
        List<String> listOfPictures = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT  * FROM local_pict WHERE job_no ='" + job_no + "'", null);
        if (cursor.moveToFirst()) {
            do {
                String path = cursor.getString(1);
                listOfPictures.add(path);
            } while (cursor.moveToNext());
        }
        database.close();
        return listOfPictures;
    }

    /**
     * Get list of jobcard from SQLite DB as Array List
     *
     * @return
     */
    public ArrayList<HashMap<String, String>> getAlljobcard() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM jobcard";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("jobCode", cursor.getString(10));
                // map.put("userId", cursor.getString(1));
                map.put("name", cursor.getString(1));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    public ArrayList<JobCard> getJobList() {
        ArrayList<JobCard> jobList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM jobcard";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                JobCard jobCard = new JobCard();
                jobCard.setJob_id(cursor.getString(10));
                jobCard.setName(cursor.getString(1));
                jobList.add(jobCard);

            } while (cursor.moveToNext());
        }
        database.close();
        return jobList;
    }

    public Cursor getJinfo(int id) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from jobCard_info where jId=" + id + "", null);
        return res;
    }

    public ArrayList<HashMap<String, String>> getAllJinfo() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM jobCard_info";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("jId", cursor.getString(0));
                // map.put("userId", cursor.getString(1));
                map.put("time", cursor.getString(3));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    //ERD Job Card

    //Insert
    public void insertERDJob(ERDjobCard jobCard) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", jobCard.getId());
        values.put("job_no", jobCard.getJobNo());
        values.put("supervisor_id", jobCard.getSupervisorId());
        values.put("name", jobCard.getName());
        values.put("description", jobCard.getDescription());
        values.put("address", jobCard.getAddress());
        values.put("progress", jobCard.getProgress());
        values.put("from_date", jobCard.getFromDate());
        values.put("to_date", jobCard.getToDate());
        database.insert(ERD_TABLE, null, values);
        database.close();
    }

    public void insertERDSubTask(ERDSubTask subTask) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", subTask.getId());
        values.put("job_card_id", subTask.getJobCardId());
        values.put("name", subTask.getName());
        database.insert(SUB_TASK_TABLE, null, values);
        database.close();
    }

    //Get
    public ArrayList<ERDjobCard> getERDJobList() {
        ArrayList<ERDjobCard> jobList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM erd_job";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                ERDjobCard jobCard = new ERDjobCard();
                jobCard.setLocal_id(cursor.getInt(0));
                jobCard.setId(cursor.getInt(1));
                jobCard.setSupervisorId(cursor.getInt(2));
                jobCard.setName(cursor.getString(3));
                jobCard.setJobNo(cursor.getString(4));

                jobList.add(jobCard);
            } while (cursor.moveToNext());
        }
        database.close();
        return jobList;
    }

    public Cursor getERDJobById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from erd_job where id=" + id, null);
        return res;
    }

    public Cursor getDataById(int id, String table){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + table+ " where id=" + id, null);
        return res;
    }

    public List<ERDSubTask> getSubTasks(int job_id) {

        List<ERDSubTask> subTaskList = new ArrayList<ERDSubTask>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from sub_task_table WHERE job_card_id = " + job_id, null);
        if (cursor.moveToFirst()) {
            do {
                ERDSubTask subTask = new ERDSubTask();
                subTask.setId(cursor.getInt(1));
                subTask.setJobCardId(cursor.getInt(2));
                subTask.setName(cursor.getString(3));
                subTaskList.add(subTask);
            } while (cursor.moveToNext());
        }
        database.close();
        return subTaskList;
    }

    //Delete
    public void delete_erd_job(String local_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ERD_TABLE, "local_id=? ", new String[]{local_id});
        db.close();
    }

    public void delete_erd_sub_task(String job_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SUB_TASK_TABLE, "job_card_id=? ", new String[]{job_id});
        db.close();
    }

    public void deleteAllERD() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ERD_TABLE, null, null);
        db.delete(SUB_TASK_TABLE, null, null);
        db.close();
    }

    //Dryden Combustion
    public void insertDryden(DrydenJobCard jobCard) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", jobCard.getId());
        values.put("job_no", jobCard.getJobNo());
        values.put("supervisor_id", jobCard.getSupervisorId());
        values.put("job_name", jobCard.getJobName());
        values.put("description", jobCard.getDescription());
        values.put("issue_date", jobCard.getIssueDate());
        values.put("drawing_no", jobCard.getDrawingNo());
        values.put("qc_no", jobCard.getQcNo());
        values.put("checked", jobCard.getChecklistDone());
        database.insert(DRYDEN_TABLE, null, values);
        database.close();
    }

    public ArrayList<DrydenJobCard> getDrydenJobList() {
        ArrayList<DrydenJobCard> jobList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM dryden_job";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                DrydenJobCard jobCard = new DrydenJobCard();
                jobCard.setLocal_id(cursor.getInt(0));
                jobCard.setId(cursor.getInt(1));
                jobCard.setJobName(cursor.getString(2));
                jobCard.setJobNo(cursor.getString(3));
                jobCard.setQcNo(cursor.getString(4));
                jobCard.setSupervisorId(cursor.getInt(8));
                jobList.add(jobCard);
            } while (cursor.moveToNext());
        }
        database.close();
        return jobList;
    }

    public Cursor getDrydenJobById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from dryden_job where id=" + id, null);
        return res;
    }

    public void deleteAllDryden() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DRYDEN_TABLE, null, null);
        db.close();
    }

    public void deleteDrydenById(String local_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DRYDEN_TABLE, "local_id=? ", new String[]{local_id});
        db.close();
    }

    public boolean isChecked(String local_id) {
        int id = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT  * FROM dryden_job WHERE local_id ='" + local_id + "'", null);
        if (res.moveToFirst()) {
            id = res.getInt((res.getColumnIndex("checked")));
        }
        if (id == 1) {
            return true;
        }
        return false;
    }

    public void updateCheckStatus(String local_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("checked", 1);
        db.update(DRYDEN_TABLE, values, "local_id = ?", new String[]{local_id});
    }

    //StrucMac

    public void insertCategories(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", queryValues.get("id"));
        values.put("category", queryValues.get("category"));
        database.insert(CATEGORY_TABLE, null, values);
        database.close();
    }

    public void insertQuestions(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", queryValues.get("id"));
        values.put("category_id", queryValues.get("category_id"));
        values.put("question", queryValues.get("question"));
        database.insert(QUESTION_TABLE, null, values);
        database.close();
    }

    public void insertVehicle(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", queryValues.get("id"));
        values.put("reg_no", queryValues.get("reg_no"));
        values.put("licence_disc", queryValues.get("licence_disc"));
        values.put("km", queryValues.get("km"));
        database.insert(VEHICLE_TABLE, null, values);
        database.close();
    }

    public void insertDelivery(Delivery delivery) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", delivery.getiD());
        values.put("delivery_note", delivery.getDeliveryNote());
        values.put("customer", delivery.getCustomer());
        values.put("driver_id", delivery.getDriverId());
        values.put("vehicle_id", delivery.getVehicleId());
        values.put("delivery_date", delivery.getDeliveryDate());
        values.put("delivery_time", delivery.getDeliveryTime());
        values.put("origin_address", delivery.getOriginAddress());
        values.put("latitude", delivery.getLatitude());
        values.put("longitude", delivery.getLongitude());
        database.insert(DELIVERY_TABLE, null, values);
        database.close();
    }

    public void insertAddresses(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", queryValues.get("id"));
        values.put("delivery_id", queryValues.get("delivery_id"));
        values.put("sequence", queryValues.get("sequence"));
        values.put("address", queryValues.get("address"));
        values.put("latitude", queryValues.get("latitude"));
        values.put("longitude", queryValues.get("longitude"));
        database.insert(ADDRESS_TABLE, null, values);
        database.close();
    }

    public ArrayList<HashMap<String, String>> getAllCategories() {
        ArrayList<HashMap<String, String>> categoryList;
        categoryList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM category";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(1));
                map.put("category", cursor.getString(2));
                categoryList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return categoryList;
    }

    public ArrayList<HashMap<String, String>> getQuestionByCategory(int categoryId) {
        ArrayList<HashMap<String, String>> questionList;
        questionList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM question WHERE category_id = " + categoryId;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(1));
                map.put("question", cursor.getString(2));
                map.put("category_id", cursor.getString(3));
                questionList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return questionList;
    }

    public ArrayList<HashMap<String, String>> getVehicleInfo() {
        ArrayList<HashMap<String, String>> vehicleList;
        vehicleList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM vehicle";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(1));
                map.put("reg_no", cursor.getString(2));
                map.put("licence_disc", cursor.getString(3));
                map.put("km", cursor.getString(4));
                vehicleList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return vehicleList;
    }

    public ArrayList<Delivery> getDeliveries() {
        ArrayList<Delivery> deliveryList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM delivery";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Delivery delivery = new Delivery();
                delivery.setLocalId(cursor.getInt(0));
                delivery.setiD(cursor.getInt(1));
                delivery.setDeliveryNote(cursor.getString(2));
                delivery.setCustomer(cursor.getString(3));
                delivery.setDriverId(cursor.getInt(4));
                delivery.setDeliveryDate(cursor.getString(6));
                deliveryList.add(delivery);
            } while (cursor.moveToNext());
        }
        database.close();
        return deliveryList;
    }

    public ArrayList<HashMap<String, String>> getAddressByDeliveryId(int id) {
        ArrayList<HashMap<String, String>> addressList;
        addressList = new ArrayList<>();
        String selectQuery = "SELECT * FROM address WHERE delivery_id = " + id + " ORDER BY sequence ASC";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(1));
                map.put("sequence", cursor.getString(3));
                map.put("address", cursor.getString(4));
                map.put("latitude", cursor.getString(5));
                map.put("longitude", cursor.getString(6));
                addressList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return addressList;
    }

    public Cursor getVehicleByRegNo(String reg_no) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT  * FROM vehicle WHERE reg_no ='" + reg_no + "'", null);
        return res;
    }
    public String getVehicleById(int id){
        String name = "vehicle not found";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM vehicle WHERE id ="+id, null);
        if (res.moveToFirst()) {
            name = res.getString((res.getColumnIndex("reg_no")));
        }
        return name;
    }

    public void deleteTable(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, null, null);
        db.close();
    }

    public String questionJSON() {
        ArrayList<HashMap<String, String>> questionList;
        questionList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM question";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(1));
                map.put("question", cursor.getString(2));
                map.put("category_id", cursor.getString(3));
                questionList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(questionList);
    }


    //Effective Cooling Job
    public void insert_ec_Job(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("job_id", queryValues.get("job_id"));
        values.put("company", queryValues.get("company"));
        database.insert("ec_job", null, values);
        database.close();
    }

    public ArrayList<HashMap<String, String>> get_all_ec_job() {

        ArrayList<HashMap<String, String>> jobList;
        jobList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM ec_job";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(0));
                map.put("job_id", cursor.getString(1));
                map.put("company", cursor.getString(2));
                jobList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return jobList;
    }

    public Cursor get_ec_data(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from ec_job where id = " + id + "", null);
        return res;
    }

    public void delete_ec_job(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ec_job", "id = ?", new String[]{id});
        db.close();
    }

    //ec_job_info

    public void insert_job_info(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("job_id", queryValues.get("job_id"));
        values.put("id", queryValues.get("id"));
        values.put("work_undertaken", queryValues.get("work_undertaken"));
        values.put("clock_time", queryValues.get("clock_time"));
        values.put("status", queryValues.get("status"));
        values.put("km", queryValues.get("km"));
        values.put("travelling_time", queryValues.get("travelling_time"));
        database.insert("ec_job_info", null, values);
        database.close();
    }

    public int checkSignIn(String status, String date, String id) {

        int count = 0;
        String selectQuery = "SELECT * FROM ec_job_info where status = '" + status + "' AND date(clock_time) = '" + date + "' AND id ='" + id + "' ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;

    }

    //ec_material
    public void insert_ec_material(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("job_id", queryValues.get("job_id"));
        values.put("id", queryValues.get("id"));
        values.put("quantity", queryValues.get("quantity"));
        values.put("material_used", queryValues.get("material_used"));
        values.put("unit_price", queryValues.get("unit_price"));
        database.insert("ec_material", null, values);
        database.close();
    }

    /*    public void delete_ec_material(String product,String quantity,String job_id) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("ec_material", "material_used=? AND quantity=? AND job_id=?", new String[]{product,quantity,job_id});
            db.close();
        }*/
    public void deleteAllMaterials(String job_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ec_material", "job_id=? ", new String[]{job_id});
        db.close();
    }

    public void delete_ec_material(String material_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ec_material", "material_id=? ", new String[]{material_id});
        db.close();
    }

    public String ec_material_JSON(String job_id) {
        ArrayList<HashMap<String, String>> materialList;
        materialList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM ec_material WHERE job_id ='" + job_id + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("material_id", cursor.getString(0));
                map.put("id", cursor.getString(1));
                map.put("job_id", cursor.getString(2));
                map.put("quantity", cursor.getString(3));
                map.put("material_used", cursor.getString(4));
                map.put("unit_price", cursor.getString(5));
                materialList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(materialList);
    }

    //EC customers
    public void insertCustomers(EcCustomer customer) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", customer.getId());
        values.put("name", customer.getName());
        database.insert("ec_customer", null, values);
        database.close();
    }

    public ArrayList<EcCustomer> getAllCustomers() {
        ArrayList<EcCustomer> customerList;
        customerList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM ec_customer";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                EcCustomer customer = new EcCustomer();
                HashMap<String, String> map = new HashMap<String, String>();
                customer.setId(cursor.getInt((cursor.getColumnIndex("id"))));
                customer.setName(cursor.getString((cursor.getColumnIndex("name"))));
                customerList.add(customer);
            } while (cursor.moveToNext());
        }
        database.close();
        return customerList;
    }

    public int getCustomerId(String name) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT  * FROM ec_customer WHERE name ='" + name + "'", null);
        if (res.moveToFirst()) {
            id = res.getInt((res.getColumnIndex("id")));
        }
        return id;
    }

    public String getCustomerName(String id) {
        String name = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT  * FROM ec_customer WHERE id ='" + id + "'", null);
        if (res.moveToFirst()) {
            name = res.getString((res.getColumnIndex("name")));
        }
        return name;
    }

    public void deleteAllCustomer() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ec_customer", null, null);
        db.close();
    }

    //EC Products
    public void insertProduct(EcProduct product) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", product.getId());
        values.put("name", product.getName());
        values.put("price", product.getPrice());
        database.insert("ec_product", null, values);
        database.close();
    }


    public ArrayList<EcProduct> getAllProducts() {
        ArrayList<EcProduct> productList;
        productList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM ec_product";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                EcProduct product = new EcProduct();
                HashMap<String, String> map = new HashMap<String, String>();
                product.setId(cursor.getInt((cursor.getColumnIndex("id"))));
                product.setName(cursor.getString((cursor.getColumnIndex("name"))));
                product.setPrice(cursor.getString((cursor.getColumnIndex("price"))));
                productList.add(product);
            } while (cursor.moveToNext());
        }
        database.close();
        return productList;
    }

    public int getProductId(String name) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT  * FROM ec_product WHERE name ='" + name + "'", null);
        if (res.moveToFirst()) {
            id = res.getInt((res.getColumnIndex("id")));
        }
        return id;
    }

    public String getProductPrice(String name) {
        String price = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT  * FROM ec_product WHERE name ='" + name + "'", null);
        if (res.moveToFirst()) {
            price = res.getString((res.getColumnIndex("price")));
        }
        return price;
    }

    public void deleteAllProduct() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ec_product", null, null);
        db.close();
    }

    //Records
    public void insertRecord(String id, String name, String dat, String lat, String lng, int uid, String status, String imei, int shifts_id, int shift_type, int costCenterId) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", id);
        values.put("userName", name);
        values.put("udpateStatus", "no");
        values.put("dat", dat);
        values.put("lat", lat);
        values.put("lng", lng);
        values.put("id", uid);
        values.put("status", status);
        values.put("imei", imei);
        values.put("shifts_id", shifts_id);
        values.put("shift_type", shift_type);
        values.put("costCenterId", costCenterId);
        database.insert("record", null, values);
        database.close();
    }

    public void deleteRecord(String recID) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("record", "recID = ?", new String[]{recID});
        db.close();
    }

    public Cursor getRec(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from record where recID=" + id + "", null);
        return res;
    }

    public ArrayList<HashMap<String, String>> getAllrecord() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        //String selectQuery = "SELECT  * FROM record";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from record", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("date", cursor.getString(4));
                // map.put("userId", cursor.getString(1));
                map.put("userName", cursor.getString(2));
                map.put("costCenterId", cursor.getString(12));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    public String composeJSONfromSQLite() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM record";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("recId", cursor.getString(0));
                map.put("dat", cursor.getString(4));
                map.put("status", cursor.getString(8));
                map.put("lon", cursor.getString(6));
                map.put("lat", cursor.getString(5));
                map.put("id", cursor.getString(7));
                map.put("imei", cursor.getString(9));
                //map.put("shifts_id",cursor.getString(10));
                //map.put("shift_type",cursor.getString(11));
                //map.put("costCenterId",cursor.getString(12));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public int dbSyncCoun() {
        int count = 0;
        String selectQuery = "SELECT  * FROM record";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    //Pine
    public void insertPine(String id, String deliveryNote, String diggersrestDN, String date) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", id);
        values.put("deliveryNote", deliveryNote);
        values.put("diggersrestDN", diggersrestDN);
        values.put("date", date);
        database.insert("pine", null, values);
        database.close();
    }

    public void deletePine(String diggersrestDN) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("pine", "diggersrestDN = ?", new String[]{diggersrestDN});
        db.close();

    }

    public ArrayList<HashMap<String, String>> getAllPine() {

        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM pine";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("userId", cursor.getString(1));
                map.put("deliveryNote", cursor.getString(2));
                map.put("diggersrestDN", cursor.getString(3));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    public int getPineUser(String diggersRestDN) {

        String selectQuery = "SELECT * FROM pine where diggersrestDN ='" + diggersRestDN + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        int userId = 0;

        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                userId = cursor.getInt((cursor.getColumnIndex("userId")));

            } while (cursor.moveToNext());
        }
        database.close();
        return userId;

    }

    public List<String> getPine() {

        List<String> pineList = new ArrayList<String>();
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from pine", null);
        if (cursor.moveToFirst()) {
            do {
                pineList.add(cursor.getString(3));
            } while (cursor.moveToNext());
        }
        database.close();
        return pineList;
    }


    //Scale
    public void insert_bt_scale(String id, String weight, String date, String time, String imei, String status, int product_id, int cost_center_id) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", id);
        values.put("weight", weight);
        values.put("date", date);
        values.put("time", time);
        values.put("imei", imei);
        values.put("status", status);
        values.put("product_id", product_id);
        values.put("cost_center_id", cost_center_id);
        database.insert("bt_scale", null, values);
        database.close();
    }

    public void delete_scale_rec(String scale_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("bt_scale", "scale_id = ?", new String[]{scale_id});
        db.close();

    }

    public String bt_scale_JSON() {

        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM bt_scale";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("scale_id", cursor.getString(0));
                map.put("user_id", cursor.getString(1));
                map.put("weight", cursor.getString(2));
                map.put("date", cursor.getString(3));
                map.put("time", cursor.getString(4));
                map.put("imei", cursor.getString(5));
                map.put("status", cursor.getString(6));
                map.put("product_id", cursor.getString(7));
                map.put("cost_center_id", cursor.getString(8));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public int getScaleCount() {

        int count = 0;
        String selectQuery = "SELECT  * FROM bt_scale";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }


    /**
     * Compose JSON out of SQLite jobcards
     * @return public String composeJSONfromSQLite(){
    ArrayList<HashMap<String, String>> wordList;
    wordList = new ArrayList<HashMap<String, String>>();
    String selectQuery = "SELECT  * FROM jobCard_info where udpateStatus = '"+"no"+"'";
    SQLiteDatabase database = this.getWritableDatabase();
    Cursor cursor = database.rawQuery(selectQuery, null);
    if (cursor.moveToFirst()) {
    do {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("comment", cursor.getString(1));
    map.put("dat", cursor.getString(2));
    map.put("tim", cursor.getString(3));
    map.put("jobCode", cursor.getString(4));
    wordList.add(map);
    } while (cursor.moveToNext());
    }
    database.close();
    Gson gson = new GsonBuilder().create();
    //Use GSON to serialize Array List to JSON
    return gson.toJson(wordList);
    }   */

    /**
     * Get Sync status of SQLite
     *
     * @return
     */
    public String getSyncStatus() {
        String msg = null;
        if (this.dbSyncCoun() == 0) {
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        } else {
            msg = "DB Sync needed\n";
        }
        return msg;
    }

    /**
     * Get SQLite jobcards that are yet to be Synced
     *
     * @return
     */
    public int dbSyncCount() {
        int count = 0;
        String selectQuery = "SELECT  * FROM jobCard_info where udpateStatus = '" + "no" + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Update Sync status against each User ID
     *
     * @param id
     * @param status public void updateSyncStatus(String id, String status){
     *               SQLiteDatabase database = this.getWritableDatabase();
     *               String updateQuery = "Update jobCard_info set udpateStatus = '"+ status +"' where jId="+"'"+ id +"'";
     *               Log.d("query",updateQuery);
     *               database.execSQL(updateQuery);
     *               database.close();
     *               }
     */
    public void updateSyncStatus(String id, String status) {
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update record set udpateStatus = '" + status + "' where recID=" + "'" + id + "'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


}
