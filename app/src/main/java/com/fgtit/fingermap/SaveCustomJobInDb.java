package com.fgtit.fingermap;

import android.content.Context;

import com.fgtit.data.MyConstants;
import com.fgtit.models.CustomJobCard;
import com.fgtit.models.ERDSubTask;
import com.fgtit.models.SaveJobResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SaveCustomJobInDb {

    Context context;
    JobDB jobDB;

    public SaveCustomJobInDb(Context context) {
        this.context = context;
        jobDB = new JobDB(context);
    }

    public SaveJobResponse saveJobs(String jsonValue, String filter) {
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

                        return new SaveJobResponse(message, true);
                    }
                    return new SaveJobResponse("No job card found", true);
                }
                return new SaveJobResponse(message, false);

            } catch (JSONException e) {
                return new SaveJobResponse("JSON error: " + e.getMessage(), false);
            }
        }
        return new SaveJobResponse("Error, Could not get job", false);
    }
}
