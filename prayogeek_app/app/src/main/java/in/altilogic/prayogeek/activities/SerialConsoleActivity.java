package in.altilogic.prayogeek.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.SerialConsoleFragment;
import in.altilogic.prayogeek.fragments.SerialConsoleSettingsFragment;
import in.altilogic.prayogeek.service.SerialConsoleService;

public class SerialConsoleActivity  extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "YOUSCOPE-SERIAL";

    private FragmentManager mFragmentManager;
    private int mScreenStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutolial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mFragmentManager = getSupportFragmentManager();
        showSerialConsoleFragment();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnConsoleSettings:
                showSerialSettingsFragment();
                break;
        }
    }

    private void showSerialSettingsFragment() {
        mScreenStatus = TutorialActivity.SCREEN_ID_SERIAL_SETTINGS;
        SerialConsoleSettingsFragment mSettingsFragment = new SerialConsoleSettingsFragment();
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mSettingsFragment).commit();
    }

    private void showSerialConsoleFragment(){
        mScreenStatus = TutorialActivity.SCREEN_ID_SERIAL_CONSOLE;
        SerialConsoleFragment mConsoleFragment = new SerialConsoleFragment();
        mConsoleFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mConsoleFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if(mScreenStatus == TutorialActivity.SCREEN_ID_SERIAL_CONSOLE) {
            finishActivity();
        }
        else if(mScreenStatus == TutorialActivity.SCREEN_ID_SERIAL_SETTINGS){
            Intent intent = new Intent(this, SerialConsoleService.class);
            intent.putExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME, SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_PARAMETERS);
            startService(intent);
            showSerialConsoleFragment();
        }
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
