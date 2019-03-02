package in.altilogic.prayogeek;

import android.app.Application;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class Global_Var extends Application {
    public static final String USER_NAME = "USER_NAME";
    public static final String EMAIL_ID = "EMAIL_ID";
    public static final String COLLEGE_NAME = "COLLEGE_NAME";
    public static final String SEMESTER = "SEMESTER";
    public static final String CONNECTION_STATUS = "CONNECTION_STATUS";
    public static final String LOCATION = "LOCATION";
    public static final String MODULE_NAME = "MODULE_NAME";
    public static final String TIMESTAMP = "TIMESTAMP";

    public static final String CS_APP_OPENED = "App Opened";
    public static final String CS_CONNECTED = "Connected";
    public static final String CS_DISCONNECTED = "Disconnected";


    private String mUsername;	//-> Got from Firebase Login
    private String mEmailId;	//-> Got from Firebase Login
    private String mCollegeName;  //-> from profile info
    private String mSemester;    //-> from profile info
    private String mCategory;    //-> value of List1
    private int mCurrent_session=0;//  -> no need to update
    private int mError_Code;    // -> no need to update
    private String mConnection_status;// -> Check Requirements
    private double mLat = 0.0, mLon = 0.0; // -> Last best Location
    private String mMAC_Address;   //-> From Database based on value of List 2
    private String mModule_Name;   //-> From Database based on value of List 2
    private int mINA1_Calibration;   //-> From Database based on value of List 2
    private int mINA2_Calibration;   //-> From Database based on value of List 2
    private Timestamp mValidity;
    private boolean mProject_Access;

    public void Set_Username(String username) {
        mUsername = username;
    }

    public String Get_Username() {
        return mUsername;
    }

    public void Set_EmailId(String emailId) {
        mEmailId = emailId;
    }

    public String Get_EmailId() {
        return mEmailId;
    }

    public void Set_College_Name(String college_name){
        mCollegeName = college_name;
    }

    public String Get_College_Name(){
        return mCollegeName;
    }

    public void Set_Semester(String semester){
        mSemester = semester;
    }

    public String Get_Semester(){
        return mSemester;
    }

    public void Set_Location(double lat, double lon){
        mLat = lat;
        mLon = lon;
    }

    public double Get_LocationLat(){
        return mLat;
    }

    public double Get_LocationLon(){
        return mLon;
    }

    public String Get_Category() {
        return mCategory;
    }

    public int Get_CurrentSession() {
        return mCurrent_session;
    }

    public String Get_ConnectionStatus() {
        return mConnection_status;
    }

    public void Set_ConnectionStatus(String status) {
        mConnection_status = status;
    }

    public String Get_MacAddress() {
        return mMAC_Address;
    }

    public String Get_ModuleName() {
        return mModule_Name;
    }

    public int Get_INA1Calibration() {
        return mINA1_Calibration;
    }

    public int Get_INA2Calibration() {
        return mINA2_Calibration;
    }
    public Map<String, Object> Get_Map(){
        Map<String, Object> globalVar = new HashMap<>();
        globalVar.put(USER_NAME, mUsername);
        globalVar.put(EMAIL_ID, mEmailId);
        globalVar.put(COLLEGE_NAME, mCollegeName);
        globalVar.put(SEMESTER, mSemester);
        globalVar.put(CONNECTION_STATUS, mConnection_status);
        globalVar.put(LOCATION, mLat + ", " + mLon);
        globalVar.put(MODULE_NAME, mModule_Name);
        globalVar.put(TIMESTAMP, Timestamp.now().toDate().toString());
        return globalVar;
    }
    public void Set_Category(String category) {
        mCategory = category;
    }

    public void Set_INA1Calibration(int ina1_cal) {
        mINA1_Calibration = ina1_cal;
    }

    public void Set_INA2Calibration(int ina2_cal) {
        mINA2_Calibration = ina2_cal;
    }

    public void Set_MacAddress(String mac_address) {
        mMAC_Address = mac_address;
    }

    public void Set_Module_Name(String moduleName) {
        mModule_Name = moduleName;
    }

    public String Get_Module_Name() {
        return mModule_Name;
    }

    public void Set_ErrorCode(int errorCode){
        mError_Code = errorCode;
    }

    public int Get_ErrorCode(){
        return mError_Code;
    }

    public void Set_Validity(Timestamp validity) {
        mValidity = validity;
    }

    public Timestamp Get_Validity() {
        return mValidity;
    }

    public boolean isProject_Access() {
        return mProject_Access;
    }

    public void Set_Project_Access(boolean project_access) {
        this.mProject_Access = project_access;
    }
}
