package in.altilogic.prayogeek.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import in.altilogic.prayogeek.R;

public class Button2Activity extends AppCompatActivity {
    private TutorialFragment mTutorialFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_button2);
//        ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null) {
//            actionBar.hide();
//        }

//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.setTitle("Button 2");
//        }
//        TextView tvOut = findViewById(R.id.tvOut);
//        Global_Var globalVar = (Global_Var) getApplicationContext();
//        StringBuilder sb = new StringBuilder();
//        sb      .append("UserName = ").append(globalVar.Get_Username()).append("\n")
//                .append("EmailID = ").append(globalVar.Get_EmailId()).append("\n")
//                .append("CollegeName = ").append(globalVar.Get_College_Name()).append("\n")
//                .append("Semester = ").append(globalVar.Get_Semester()).append("\n")
//                .append("Category = ").append(globalVar.Get_Category()).append("\n")
//                .append("Current session = ").append(globalVar.Get_CurrentSession()).append("\n")
//                .append("Error code = ").append(globalVar.Get_ErrorCode()).append("\n")
//                .append("Connection status = ").append(globalVar.Get_ConnectionStatus()).append("\n")
//                .append("Location = ").append(globalVar.Get_LocationLat()).append(", ")
//                .append(globalVar.Get_LocationLon()).append("\n")
//                .append("MAC = ").append(globalVar.Get_MacAddress()).append("\n")
//                .append("Module name = ").append(globalVar.Get_ModuleName()).append("\n")
//                .append("INA1 calibration = ").append(globalVar.Get_INA1Calibration()).append("\n")
//                .append("INA2 calibration = ").append(globalVar.Get_INA2Calibration()).append("\n");
//        tvOut.setText(sb.toString());
        mTutorialFragment = new TutorialFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragmentContent, mTutorialFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity(){
        if (getParent() == null) {
            setResult(RESULT_OK, new Intent());
        }
        else {
            getParent().setResult(RESULT_OK, new Intent());
        }
        finish();
    }
}
