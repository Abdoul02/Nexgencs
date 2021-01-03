package com.fgtit.data;

public class MyConstants {

    public static final String USERNAME = "username";
    public static final String ID_NUMBER = "id_number";
    public static final String USER_ID = "user_id";
    public static final String IMAGE = "image";
    public static final String IMAGE_NAME = "image_name";
    public static final String IMAGE_PATH = "image_path";
    public static final String DRYDEN = "dryden";
    public static final String CHECKLIST = "checklist";
    public static final String WELD_MAP = "weld_map";
    public static final String JOB_DETAIL = "job_detail";
    public static final String PROJECT_SIGNATURE = "project_signature";
    public static final String CONSUMABLE = "consumable";

    public static final int FINGERPRINT_SENSITIVITY = 55;

    //URL

    public static final String BASE_URL = "http://nexgencs.net";
    public static final String DRYDEN_GET_JOB_URL = BASE_URL + "/alos/dryden_combustion/getDrydenJobs.php";
    public static final String DRYDEN_UPLOAD = BASE_URL + "/alos/dryden_combustion/uploadData.php";
    public static final String DRYDEN_GET_JOB_CARDS_URL = BASE_URL + "/alos/dryden_combustion/getJobs.php";
    public static final String DRYDEN_CLOCK_URL = BASE_URL + "/alos/dryden_combustion/clock.php";

    public static final String STRUCMAC_DATA_URL = BASE_URL + "/alos/strucmac/getStrucMacData.php";
    public static final String STRUCMAC_UPLOAD_URL = BASE_URL + "/alos/strucmac/insertData.php";

    public static final String TURNMILL_GET_JOB_URL = BASE_URL + "/alos/turnmill/getData.php";
    public static final String TURNMILL_CLOCK_URL = BASE_URL + "/alos/turnmill/jobClock.php";

    public static final String PROJECT_SIGNATURE_URL = BASE_URL + "/alos/project_signature.php";

    //Filters
    public static final String TURNMILL_GET_JOB = "turn_mill_job";
    public static final String TURNMILL_CLOCK = "turn_mill_clock";
    public static final String DRYDEN_GET_JOB = "dryden_job";

    public static final String STRUCMAC_CHECKLIST = "strucMacCheckList";
    public static final String STRUCMAC_IMAGE_UPLOAD = BASE_URL + "/alos/strucmac/uploadPictures.php";
    public static final String STRUCMAC_VEHICLE = "strucMacVehicle";
    public static final String STRUCMAC_UPLOAD = "StrucMacUpload";
    public static final String DELIVERY = "delivery";

    //Data
    public static final String REPORT_SHARED_PREF = "reportSharedPref";
    public static final String VEHICLE_ID = "vehicle_id";
    public static final String PLANT_NO = "plant_no";
    public static final String WORK_CONDITION = "work_condition";
    public static final String FAULT_FOUND = "fault_found";
    public static final String KM = "km";

    //Activity Requestcode
    public static final int CHECKLIST_REQUEST_CLOCK = 101;
    public static final int CHECKLIST_REQUEST_SIGN = 102;
    public static final int VERIFICATION_REQUEST_CLOCK = 103;
    public static final int CONSUMABLE_REQUEST_CLOCK = 104;
    public static final int CONSUMABLE_REQUEST_SIGN = 105;
    public static final int STRUCMAC_REPORT_CLOCK = 106;
    public static final int PROJECT_SIGN = 107;

    //Companies
    public static final int COMPANY_ERD = 117;
    public static final int COMPANY_TURN_MILL = 124;
    public static final int COMPANY_DRYDEN = 132;
    public static final int COMPANY_STRUCMAC = 135;


}
