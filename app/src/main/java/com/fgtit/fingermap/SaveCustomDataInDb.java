package com.fgtit.fingermap;

import android.content.Context;
import android.widget.Toast;

import com.fgtit.data.MyConstants;
import com.fgtit.models.CustomJobCard;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.SaveDataResponse;
import com.fgtit.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SaveCustomDataInDb {

    Context context;
    JobDB jobDB;
    DBHandler db;

    public SaveCustomDataInDb(Context context) {
        this.context = context;
        jobDB = new JobDB(context);
        db = new DBHandler(context);
    }

    public SaveDataResponse saveJobs(String jsonValue, String filter) {
        if (jsonValue != null && !jsonValue.isEmpty()) {
            int success;
            String message;

            try {
                JSONObject result = new JSONObject(jsonValue);
                success = result.getInt("success");
                message = result.getString("message");
                if (success == 1) {
                    JSONArray arr = result.getJSONArray("data");
                    if (arr.length() > 0) {
                        jobDB.deleteAllERD();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            CustomJobCard jobCard = new CustomJobCard();
                            jobCard.setId(obj.getInt("id"));
                            jobCard.setSupervisorId(obj.getInt("supervisor_id"));
                            jobCard.setJobNo(obj.getString("job_no"));
                            jobCard.setDescription(obj.getString("description"));
                            jobCard.setFromDate(obj.getString("from_date"));
                            jobCard.setToDate(obj.getString("to_date"));

                            if (obj.has("address")) {
                                jobCard.setAddress(obj.getString("address"));
                            }

                            if (obj.has("progress")) {
                                jobCard.setProgress(obj.getString("progress"));
                            }

                            if (obj.has("name")) {
                                jobCard.setName(obj.getString("name"));
                            }

                            //This is for Mechfit
                            if (obj.has("customer_name")) {
                                jobCard.setCustomerName(obj.getString("customer_name"));
                            }

                            if (obj.has("qty")) {
                                jobCard.setQty(obj.getString("qty"));
                            }

                            if (obj.has("drawing_no")) {
                                jobCard.setDrawingNo(obj.getString("drawing_no"));
                            }

                            if (obj.has("sub_task")) {
                                String tasks = obj.getString("sub_task");
                                JSONArray taskArray = new JSONArray(tasks);

                                if (taskArray.length() != 0) {
                                    for (int x = 0; x < taskArray.length(); x++) {
                                        JSONObject taskObject = (JSONObject) taskArray.get(x);
                                        ERDSubTask subTask = new ERDSubTask();
                                        subTask.setName(taskObject.getString("name"));
                                        subTask.setJobCardId(taskObject.getInt("job_card_id"));
                                        subTask.setId(taskObject.getInt("id"));
                                        jobDB.insertERDSubTask(subTask);
                                    }
                                }
                            }
                            if (filter.equals(MyConstants.MECHFIT_GET_JOB)) {
                                jobDB.insertMechFitJob(jobCard);
                            } else {
                                jobDB.insertERDJob(jobCard);
                            }
                        }

                        return new SaveDataResponse(message, true);
                    }
                    return new SaveDataResponse("No job card found", true);
                }
                return new SaveDataResponse(message, false);

            } catch (JSONException e) {
                return new SaveDataResponse("JSON error: " + e.getMessage(), false);
            }
        }
        return new SaveDataResponse("Error, Could not get job", false);
    }

    public SaveDataResponse saveEmployeeInfo(String employeeInfo) {
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(employeeInfo);
            System.out.println(arr.length());
            // If no of array elements is not zero
            if (arr.length() != 0) {

                db.deleteAll();
                // Loop through each array element, get JSON object which has userid and username
                for (int i = 0; i < arr.length(); i++) {
                    // Get JSON object
                    JSONObject obj = (JSONObject) arr.get(i);
                    User user = new User();
                    user.setuId(Integer.parseInt(obj.get("userId").toString()));
                    user.setIdNum(obj.get("idNum").toString());
                    user.setuName(obj.get("name").toString());
                    user.setFinger1(obj.get("finger1").toString());
                    user.setFinger2(obj.get("finger2").toString());

                    user.setCostCenterId(Integer.parseInt(obj.get("costCenterId").toString()));
                    user.setShifts_id(Integer.parseInt(obj.get("shifts_id").toString()));
                    user.setShift_type(Integer.parseInt(obj.get("shift_type").toString()));
                    user.setCard(obj.get("card").toString());
                    db.insertUser(user);
                }
                return new SaveDataResponse(arr.length() + " Employee(s) downloaded", true);
                //gotoUserList();
            }
        } catch (JSONException e) {
            Toast.makeText(context, "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return new SaveDataResponse("Could not download employee information", true);
    }
}
