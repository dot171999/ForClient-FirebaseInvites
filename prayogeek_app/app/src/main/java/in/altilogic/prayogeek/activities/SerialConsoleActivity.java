package in.altilogic.prayogeek.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.SerialConsoleFragment;
import in.altilogic.prayogeek.fragments.SerialConsoleSettingsFragment;
import in.altilogic.prayogeek.service.ImageDownloadService;
import in.altilogic.prayogeek.service.SerialConsoleService;
import pub.devrel.easypermissions.EasyPermissions;

public class SerialConsoleActivity  extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    public static final String TAG = "YOUSCOPE-SERIAL";

    private FragmentManager mFragmentManager;
    private SerialConsoleFragment mConsoleFragment;
    private final static int SCREEN_ID_SERIAL_SETTINGS = 14;
    private final static int SCREEN_ID_SERIAL_CONSOLE = 15;

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
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopService(new Intent(this, ImageDownloadService.class));
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnConsoleSettings:
                showSettingsFragment();
                break;
        }
    }

    private void showSettingsFragment() {
        mScreenStatus = SCREEN_ID_SERIAL_SETTINGS;
        SerialConsoleSettingsFragment mSettingsFragment = new SerialConsoleSettingsFragment();
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mSettingsFragment).commit();
    }

    private void showSerialConsoleFragment(){
        mScreenStatus = SCREEN_ID_SERIAL_CONSOLE;
        mConsoleFragment = new SerialConsoleFragment();
        mConsoleFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mConsoleFragment).commit();
        checkWriteReadPermissions();
    }

    @Override
    public void onBackPressed() {
        if(mScreenStatus == SCREEN_ID_SERIAL_CONSOLE) {
            finishActivity();
        }
        else {
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

    private boolean checkWriteReadPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.string_describe_why_do_you_need_a_write_ext),
                    1000, perms);
        }
        return false;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(mConsoleFragment != null) {
            Button btnSave = ((Button)mConsoleFragment.getView().findViewById(R.id.btnSave));
            btnSave.setClickable(true);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(mConsoleFragment != null) {
            Button btnSave = ((Button)mConsoleFragment.getView().findViewById(R.id.btnSave));
            btnSave.setClickable(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult " + requestCode);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
