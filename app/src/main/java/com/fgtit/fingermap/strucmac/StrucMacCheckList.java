package com.fgtit.fingermap.strucmac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.fgtit.adapter.ClockActivity;
import com.fgtit.data.CommonFunction;
import com.fgtit.data.MyConstants;
import com.fgtit.fingermap.JobDB;
import com.fgtit.fingermap.MenuActivity;
import com.fgtit.fingermap.R;
import com.fgtit.service.DownloadService;
import com.fgtit.service.NetworkService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.fgtit.data.MyConstants.STRUCMAC_CHECKLIST;
import static com.fgtit.data.MyConstants.STRUCMAC_DATA_URL;
import static com.fgtit.data.MyConstants.STRUCMAC_REPORT_CLOCK;
import static com.fgtit.data.MyConstants.STRUCMAC_UPLOAD;
import static com.fgtit.data.MyConstants.STRUCMAC_UPLOAD_URL;
import static com.fgtit.data.MyConstants.USERNAME;
import static com.fgtit.data.MyConstants.USER_ID;
import static com.fgtit.fingermap.JobDB.CATEGORY_TABLE;
import static com.fgtit.fingermap.JobDB.QUESTION_TABLE;

public class StrucMacCheckList extends AppCompatActivity {

    @BindView(R.id.checkListTabHost)
    TabHost mTabHost;
    @BindView(R.id.ll_engineCheckParent)
    LinearLayout engineCheckLinearLayout;
    @BindView(R.id.rl_engineCheckList)
    RelativeLayout engineCheckRelativeLayout;
    @BindView(R.id.ll_insideCheckParent)
    LinearLayout insideLinearLayout;
    @BindView(R.id.rl_insideCheckList)
    RelativeLayout insideRelativeLayout;
    @BindView(R.id.ll_toolCheckParent)
    LinearLayout toolLinearLayout;
    @BindView(R.id.rl_toolCheckList)
    RelativeLayout toolRelativeLayout;
    @BindView(R.id.cb_engine1)
    CheckBox engineCheckBox;
    @BindView(R.id.cb_engine2)
    CheckBox engineCheckBox2;

    HashMap<String, String> categoryQueryValues;
    HashMap<String, String> questionQueryValues;
    JobDB jobDB = new JobDB(this);
    CommonFunction commonFunction = new CommonFunction(this);

    String plantNo, workCondition, faultFound, km, vehicleId;
    String user_id, userName;
    int engineCheckCount = 0, insideCheckCount = 0, toolboxCheckCount = 0;
    ArrayList<HashMap<String, String>> engineQuestions;
    ArrayList<HashMap<String, String>> insideQuestions;
    ArrayList<HashMap<String, String>> toolboxQuestions;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            handleResponse(bundle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_struc_mac_check_list);
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                NetworkService._SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drawing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.done) {
            if (allChecked()) {
                Intent clockIntent = new Intent(this, ClockActivity.class);
                startActivityForResult(clockIntent, STRUCMAC_REPORT_CLOCK);
            } else {
                commonFunction.showToast("Please check all boxes");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mTabHost.setup();
        addTabs();
        if (jobDB.getAllCategories().size() > 0) {
            populateTabs();
        } else commonFunction.showToast("Download Checklist");
    }

    private void populateTabs() {
        ArrayList<HashMap<String, String>> categoryList;
        categoryList = jobDB.getAllCategories();
        HashMap<String, String> engineHashMap = categoryList.get(0);
        String engineCategoryId = engineHashMap.get("id");
        engineQuestions = jobDB.getQuestionByCategory(Integer.parseInt(engineCategoryId));

        for (int i = 0; i < engineQuestions.size(); i += 2) {
            RelativeLayout engineRl = new RelativeLayout(this);
            engineRl.setLayoutParams(engineCheckRelativeLayout.getLayoutParams());
            CheckBox cb1 = new CheckBox(this);
            CheckBox cb2 = new CheckBox(this);
            cb1.setText(engineQuestions.get(i).get("question"));
            cb1.setId(Integer.parseInt(engineQuestions.get(i).get("id")));
            cb1.setLayoutParams(engineCheckBox.getLayoutParams());
            cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        engineCheckCount++;
                    } else {
                        if (engineCheckCount > 0) {
                            engineCheckCount--;
                        }
                    }
                }
            });
            engineRl.addView(cb1);

            if ((i + 1) < engineQuestions.size()) {
                cb2.setText(engineQuestions.get(i + 1).get("question"));
                cb2.setId(Integer.parseInt(engineQuestions.get(i + 1).get("id")));
                cb2.setLayoutParams(engineCheckBox2.getLayoutParams());
                cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            engineCheckCount++;
                        } else {
                            if (engineCheckCount > 0) {
                                engineCheckCount--;
                            }
                        }
                    }
                });
                engineRl.addView(cb2);
            }
            engineCheckLinearLayout.addView(engineRl);
        }

        HashMap<String, String> insideHashMap = categoryList.get(1);
        String insideCategoryId = insideHashMap.get("id");
        insideQuestions = jobDB.getQuestionByCategory(Integer.parseInt(insideCategoryId));

        for (int i = 0; i < insideQuestions.size(); i += 2) {
            RelativeLayout engineRl = new RelativeLayout(this);
            engineRl.setLayoutParams(engineCheckRelativeLayout.getLayoutParams());
            CheckBox cb1 = new CheckBox(this);
            CheckBox cb2 = new CheckBox(this);
            cb1.setText(insideQuestions.get(i).get("question"));
            cb1.setId(Integer.parseInt(insideQuestions.get(i).get("id")));
            cb1.setLayoutParams(engineCheckBox.getLayoutParams());
            cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        insideCheckCount++;
                    } else {
                        if (insideCheckCount > 0) {
                            insideCheckCount--;
                        }
                    }
                }
            });
            engineRl.addView(cb1);

            if ((i + 1) < insideQuestions.size()) {
                cb2.setText(insideQuestions.get(i + 1).get("question"));
                cb2.setId(Integer.parseInt(insideQuestions.get(i + 1).get("id")));
                cb2.setLayoutParams(engineCheckBox2.getLayoutParams());
                cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            insideCheckCount++;
                        } else {
                            if (insideCheckCount > 0) {
                                insideCheckCount--;
                            }
                        }
                    }
                });
                engineRl.addView(cb2);
            }
            insideLinearLayout.addView(engineRl);
        }

        HashMap<String, String> toolboxHashMap = categoryList.get(2);
        String toolboxCategoryId = toolboxHashMap.get("id");
        toolboxQuestions = jobDB.getQuestionByCategory(Integer.parseInt(toolboxCategoryId));

        for (int i = 0; i < toolboxQuestions.size(); i += 2) {
            RelativeLayout engineRl = new RelativeLayout(this);
            engineRl.setLayoutParams(engineCheckRelativeLayout.getLayoutParams());
            CheckBox cb1 = new CheckBox(this);
            CheckBox cb2 = new CheckBox(this);
            cb1.setText(toolboxQuestions.get(i).get("question"));
            cb1.setId(Integer.parseInt(toolboxQuestions.get(i).get("id")));
            cb1.setLayoutParams(engineCheckBox.getLayoutParams());
            cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        toolboxCheckCount++;
                    } else {
                        if (toolboxCheckCount > 0) {
                            toolboxCheckCount--;
                        }
                    }
                }
            });
            engineRl.addView(cb1);

            if ((i + 1) < toolboxQuestions.size()) {
                cb2.setText(toolboxQuestions.get(i + 1).get("question"));
                cb2.setId(Integer.parseInt(toolboxQuestions.get(i + 1).get("id")));
                cb2.setLayoutParams(engineCheckBox2.getLayoutParams());
                cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            toolboxCheckCount++;
                        } else {
                            if (toolboxCheckCount > 0) {
                                toolboxCheckCount--;
                            }
                        }
                    }
                });
                engineRl.addView(cb2);
            }
            toolLinearLayout.addView(engineRl);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STRUCMAC_REPORT_CLOCK && resultCode == RESULT_OK) {
            userName = data != null ? data.getStringExtra(USERNAME) : null;
            user_id = data != null ? data.getStringExtra(USER_ID) : null;
            commonFunction.showToast("Clocked by " + userName);
            uploadData();
        } else {
            commonFunction.showToast("Operation Cancelled");
        }
    }

    private void addTabs() {

        TabHost.TabSpec mSpec = mTabHost.newTabSpec("Engine");
        mSpec.setContent(R.id.engine_Comp);
        mSpec.setIndicator("Engine Compartment");
        mTabHost.addTab(mSpec);

        mSpec = mTabHost.newTabSpec("inside");
        mSpec.setContent(R.id.Inside_and_out);
        mSpec.setIndicator("Inside & Outside");
        mTabHost.addTab(mSpec);

        mSpec = mTabHost.newTabSpec("cab_toolBox");
        mSpec.setContent(R.id.Cab_toolbox);
        mSpec.setIndicator("Cab & ToolBox");
        mTabHost.addTab(mSpec);
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {

            TextView tv = mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.BLACK);
            //tv.setPadding(10,10,10,15);
            tv.setTextSize((float) 10.0);
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
            //tv.setBackgroundResource(R.mipmap.email);
        }
    }

    public void downloadCheckList(View view) {
        commonFunction.setDialog(true);
        Intent client_intent = new Intent(this, NetworkService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "getCheckList");
        client_intent.putExtra(DownloadService.URL, STRUCMAC_DATA_URL);
        client_intent.putExtra(DownloadService.JSON_VAL, "");
        client_intent.putExtra(DownloadService.FILTER, STRUCMAC_CHECKLIST);
        startService(client_intent);
    }

    private boolean allChecked() {
        return engineCheckCount == engineQuestions.size() && insideCheckCount == insideQuestions.size() && toolboxCheckCount == toolboxQuestions.size();
    }

    private void handleResponse(Bundle bundle) {
        String filter = bundle.getString(DownloadService.FILTER);
        int resultCode = bundle.getInt(DownloadService.RESULT);
        if (resultCode == RESULT_OK && (filter != null && filter.equals(STRUCMAC_CHECKLIST))) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            int success;
            String message;
            try {
                JSONObject result = new JSONObject(response);
                success = result.getInt("success");
                message = result.getString("message");
                if (success == 1) {
                    JSONArray categoryArray = result.getJSONArray("categories");
                    if (categoryArray.length() > 0) {
                        jobDB.deleteTable(CATEGORY_TABLE);
                        jobDB.deleteTable(QUESTION_TABLE);
                        for (int i = 0; i < categoryArray.length(); i++) {
                            JSONObject obj = (JSONObject) categoryArray.get(i);
                            categoryQueryValues = new HashMap<>();
                            categoryQueryValues.put("id", obj.getString("id"));
                            categoryQueryValues.put("category", obj.getString("category"));
                            jobDB.insertCategories(categoryQueryValues);
                        }
                    }

                    JSONArray questionArray = result.getJSONArray("questions");
                    if (questionArray.length() > 0) {
                        for (int i = 0; i < questionArray.length(); i++) {
                            JSONObject obj = (JSONObject) questionArray.get(i);
                            questionQueryValues = new HashMap<>();
                            questionQueryValues.put("id", obj.getString("questionId"));
                            questionQueryValues.put("category_id", obj.getString("category_id"));
                            questionQueryValues.put("question", obj.getString("question"));
                            jobDB.insertQuestions(questionQueryValues);
                        }
                    }
                }
                commonFunction.cancelDialog();
                refresh(message, StrucMacCheckList.class);
            } catch (JSONException e) {
                e.printStackTrace();
                commonFunction.cancelDialog();
            }
        } else if (resultCode == RESULT_OK && (filter != null && filter.equals(STRUCMAC_UPLOAD))) {
            String response = bundle.getString(DownloadService.CALL_RESPONSE);
            int success;
            String message;
            try {
                JSONObject result = new JSONObject(response);
                success = result.getInt("success");
                message = result.getString("message");
                commonFunction.cancelDialog();
                if (success == 1) {
                    refresh(message, MenuActivity.class);
                } else {
                    commonFunction.showToast(message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            commonFunction.cancelDialog();
            commonFunction.showToast("Something went wrong");
        }
    }

    private void uploadData() {

        SharedPreferences prefs = getSharedPreferences(MyConstants.REPORT_SHARED_PREF, MODE_PRIVATE);
        vehicleId = prefs.getString(MyConstants.VEHICLE_ID, "");
        km = prefs.getString(MyConstants.KM, "");
        plantNo = prefs.getString(MyConstants.PLANT_NO, "");
        workCondition = prefs.getString(MyConstants.WORK_CONDITION, "");
        faultFound = prefs.getString(MyConstants.FAULT_FOUND, "");

        JSONObject postDataParams = new JSONObject();
        try {
            postDataParams.accumulate("date", commonFunction.getDateAndTime());
            postDataParams.accumulate("user_id", user_id);
            postDataParams.accumulate("vehicle_id", vehicleId);
            postDataParams.accumulate("km", km);
            postDataParams.accumulate("plant_no", plantNo);
            postDataParams.accumulate("work_condition", workCondition);
            postDataParams.accumulate("fault_found", faultFound);
            postDataParams.accumulate("answers", jobDB.questionJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        commonFunction.setDialog(true);
        Intent client_intent = new Intent(this, NetworkService.class);
        client_intent.putExtra(DownloadService.POST_JSON, "questionJSON");
        client_intent.putExtra(DownloadService.JSON_VAL, postDataParams.toString());
        client_intent.putExtra(DownloadService.FILTER, STRUCMAC_UPLOAD);
        client_intent.putExtra(DownloadService.URL, STRUCMAC_UPLOAD_URL);
        startService(client_intent);
    }

    public void refresh(String message, Class destination) {
        commonFunction.showToast(message);
        Intent intent = new Intent(this, destination);
        startActivity(intent);
    }
}
