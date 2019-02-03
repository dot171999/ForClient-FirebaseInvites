package in.altilogic.prayogeek;

import android.app.Application;

public class Global_Var extends Application {
    public static final String COLLEGE_NAME = "COLLEGE_NAME";
    public static final String SEMESTER = "SEMESTER";

    private String mUsername;	//-> Got from Firebase Login
    private String mEmailId;	//-> Got from Firebase Login
    private String mCollegeName;  //-> from profile info
    private String mSemester;    //-> from profile info
    private String mCategory;    //-> value of List1
    private int mCurrent_session=0;//  -> no need to update
    private int Error_Code;    // -> no need to update
    private String Connection_status;// -> Check Requirements
    private double gps_lat = 0.0, gps_lon = 0.0; // -> Last best Location
    private String MAC_Address;   //-> From Database based on value of List 2
    private String Module_Name;   //-> From Database based on value of List 2
    private int INA1_Calibration;   //-> From Database based on value of List 2
    private int INA2_Calibration;   //-> From Database based on value of List 2

/** Needs save to database
    mUsername
    mEmailId
    College_name
    Semester
    Connection_status
    Location
    Module_Name
    TimeStamp
*/

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
        gps_lat = lat;
        gps_lon = lon;
    }

    public double Get_LocationLat(){
        return gps_lat;
    }

    public double Get_LocationLon(){
        return gps_lon;
    }
}
