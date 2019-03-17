package in.altilogic.prayogeek.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.BasicElectronicFragment;
import in.altilogic.prayogeek.fragments.DemoProjectsFragment;
import in.altilogic.prayogeek.fragments.ImageFragment;
import in.altilogic.prayogeek.fragments.ProjectsFragment;
import in.altilogic.prayogeek.fragments.TutorialFragment;
import in.altilogic.prayogeek.service.ImageDownloadService;
import in.altilogic.prayogeek.utils.Utils;

import static in.altilogic.prayogeek.utils.Utils.*;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener, ImageFragment.OnClickListener {
    private FragmentManager mFragmentManager;
    private final static String TAG = "YOUSCOPE-DB-TUTORIAL";

    public final static String CURRENT_SCREEN_SETTINGS = "TUTORIAL-SETTINGS-CURRENT-SCREEN";
    public final static String CURRENT_SCREEN_SETTINGS_PAGE = "TUTORIAL-SETTINGS-CURRENT-PAGE";
    public final static String CURRENT_SCREEN_SETTINGS_RESUME = "TUTORIAL-SETTINGS-CURRENT-SCREEN-RESUME";
    public final static String CURRENT_SCREEN_SETTINGS_PAGE_RESUME = "TUTORIAL-SETTINGS-CURRENT-PAGE-RESUME";
    private final static int SCREEN_ID_BASIC_ELECTRONIC = 1;
    private final static int SCREEN_ID_PROJECTS = 2;
    private final static int SCREEN_ID_DEMO_PROJECTS = 3;
    private final static int SCREEN_ID_BREADBOARD_USAGE = 4;
    private final static int SCREEN_ID_LED_ON_OFF = 5;
    private final static int SCREEN_ID_POWER_SUPPLY = 6;
    private final static int SCREEN_ID_TRANSISTOR_SWITCH = 7;
    private final static int SCREEN_ID_IC741 = 8;
    private final static int SCREEN_ID_IC555 = 9;
    private final static int SCREEN_ID_PROJECT1 = 10;
    private final static int SCREEN_ID_PROJECT2 = 11;
    private final static int SCREEN_ID_DEMO_PROJECT1 = 12;
    private final static int SCREEN_ID_DEMO_PROJECT2 = 13;
    private int mScreenStatus = 0;

    private final static String mBasicElectronic = "basic_electronics";
    private final static String mDemoWorkshops = "demo_workshops";
    private final static String mWorkshops = "workshops";
    private final static String mBreadboardImages = "Breadboard";
    private final static String mLedOnOffImages = "led_on_off";
    private final static String mPowerSupplyImages = "Bridge_Rectifier";
    private final static String mTransistorSwitchImages = "Transistor_Relay";
    private final static String mIC741Images = "Ic741_Integrator";
    private final static String mIC555Images = "Timer555";
    private final static String mProject1Images = "ldr_relay";
    private final static String mProject2Images = "fire_alarm_555_timer";
    private final static String mDemoProject1Images = "ldr_sensor";
    private final static String mDemoProject2Images = "arduino_pwm";

    private static int mStatusBarColor;

    private BroadcastReceiver mBroadcastReceiver;

    private boolean isStartShowingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_tutolial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mStatusBarColor = getWindow().getStatusBarColor();
        mFragmentManager = getSupportFragmentManager();
        int last_screen_id = readSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
        int last_page = readSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
        showFragment(last_screen_id, last_page);
        initBroadcastReceiver();
    }

    private void showFragment(int last_screen_id, int last_page) {
        switch(last_screen_id){
            case 0: mScreenStatus = 0; showTutorialFragment(); break;
            case SCREEN_ID_BREADBOARD_USAGE: mScreenStatus = SCREEN_ID_BREADBOARD_USAGE; showGifFragment(mBasicElectronic, mBreadboardImages, last_page); break;
            case SCREEN_ID_LED_ON_OFF: mScreenStatus = SCREEN_ID_LED_ON_OFF; showGifFragment(mBasicElectronic, mLedOnOffImages, last_page); break;
            case SCREEN_ID_POWER_SUPPLY: mScreenStatus = SCREEN_ID_POWER_SUPPLY; showGifFragment(mBasicElectronic, mPowerSupplyImages, last_page); break;
            case SCREEN_ID_TRANSISTOR_SWITCH: mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;  showGifFragment(mBasicElectronic, mTransistorSwitchImages, last_page);  break;
            case SCREEN_ID_IC741: mScreenStatus = SCREEN_ID_IC741; showGifFragment(mBasicElectronic, mIC741Images, last_page); break;
            case SCREEN_ID_IC555: mScreenStatus = SCREEN_ID_IC555; showGifFragment(mBasicElectronic, mIC555Images, last_page); break;
            case SCREEN_ID_PROJECT1: mScreenStatus = SCREEN_ID_PROJECT1; showGifFragment(mWorkshops, mProject1Images, last_page); break;
            case SCREEN_ID_PROJECT2: mScreenStatus = SCREEN_ID_PROJECT2; showGifFragment(mWorkshops, mProject2Images, last_page); break;
            case SCREEN_ID_DEMO_PROJECT1: mScreenStatus = SCREEN_ID_DEMO_PROJECT1; showGifFragment(mDemoWorkshops, mDemoProject1Images, last_page); break;
            case SCREEN_ID_DEMO_PROJECT2: mScreenStatus = SCREEN_ID_DEMO_PROJECT2; showGifFragment(mDemoWorkshops, mDemoProject2Images, last_page); break;
            default: mScreenStatus = 0; showTutorialFragment(); break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        IntentFilter statusIntentFilter = new IntentFilter(ImageDownloadService.HW_SERVICE_BROADCAST_VALUE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, statusIntentFilter);
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        Log.d(TAG, "ImageFragment::onStop");
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, ImageDownloadService.class));
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mScreenStatus == 0) {
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
            finishActivity();
        }
        else if(mScreenStatus > SCREEN_ID_DEMO_PROJECTS && mScreenStatus <= SCREEN_ID_IC555){
            mScreenStatus = SCREEN_ID_BASIC_ELECTRONIC; showBasicElectronic();
        }
        else if(mScreenStatus == SCREEN_ID_PROJECT1 || mScreenStatus == SCREEN_ID_PROJECT2) {
            mScreenStatus = SCREEN_ID_PROJECTS; showProjectsFragment();
        }
        else if(mScreenStatus == SCREEN_ID_DEMO_PROJECT1 || mScreenStatus == SCREEN_ID_DEMO_PROJECT2) {
            mScreenStatus = SCREEN_ID_DEMO_PROJECTS; showDemoProjectsFragment();
        }
        else if(mScreenStatus > 0 && mScreenStatus < SCREEN_ID_BREADBOARD_USAGE) {
            mScreenStatus = 0; showTutorialFragment();
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

    @Override
    public void onClick(View view) {
        int last_page = readSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);

        switch (view.getId()){
            case R.id.btnBasicElectronic: mScreenStatus = SCREEN_ID_BASIC_ELECTRONIC; showBasicElectronic(); break;
            case R.id.btnProjects: mScreenStatus = SCREEN_ID_PROJECTS;
                if(((Global_Var) getApplicationContext()).isProject_Access())
                    showProjectsFragment();
                else
                    Toast.makeText(getApplicationContext(), "Please Subscribe to access Projects", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnDemoProjects: mScreenStatus = SCREEN_ID_DEMO_PROJECTS;  showDemoProjectsFragment(); break;
            case R.id.btnBreadBoard: mScreenStatus = SCREEN_ID_BREADBOARD_USAGE; showGifFragment(mBasicElectronic, mBreadboardImages, last_page); break;
            case R.id.btnLedOnOFF: mScreenStatus = SCREEN_ID_LED_ON_OFF;  showGifFragment(mBasicElectronic, mLedOnOffImages, last_page);  break;
            case R.id.btnPowerSupply: mScreenStatus = SCREEN_ID_POWER_SUPPLY; showGifFragment(mBasicElectronic, mPowerSupplyImages, last_page); break;
            case R.id.btnTransistorSwitch: mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;  showGifFragment(mBasicElectronic, mTransistorSwitchImages, last_page); break;
            case R.id.btnIC741: mScreenStatus = SCREEN_ID_IC741; showGifFragment(mBasicElectronic, mIC741Images, last_page); break;
            case R.id.btnIC555: mScreenStatus = SCREEN_ID_IC555; showGifFragment(mBasicElectronic, mIC555Images, last_page); break;
            case R.id.btnProject1: mScreenStatus = SCREEN_ID_PROJECT1; showGifFragment(mWorkshops, mProject1Images, last_page); break;
            case R.id.btnProject2: mScreenStatus = SCREEN_ID_PROJECT2; showGifFragment(mWorkshops, mProject2Images, last_page); break;
            case R.id.btnDemoProject1: mScreenStatus = SCREEN_ID_DEMO_PROJECT1; showGifFragment(mDemoWorkshops, mDemoProject1Images, last_page); break;
            case R.id.btnDemoProject2:  mScreenStatus = SCREEN_ID_DEMO_PROJECT2; showGifFragment(mDemoWorkshops, mDemoProject2Images, last_page); break;
            case R.id.btnResume:
                int screen_id = readSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
                int screen_page = readSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                int last_screen_id = readSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, 0);
                int last_screen_page = readSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, 0);
                if(screen_id == 0 && screen_page == 0 && last_screen_id == 0 && last_screen_page == 0)
                    Toast.makeText(getApplicationContext(), "Nothing to Resume", Toast.LENGTH_SHORT).show();
                else
                    showFragment(last_screen_id, last_screen_page);
                break;
        }
    }

    @Override
    public void onClick(View view, int page) {
        Log.d("APP-TUTORIAL", "mScreenStatus = " + mScreenStatus + "; page = " + page);
        switch (view.getId()){
            case R.id.btnHome:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);

                mScreenStatus = 0;
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                showTutorialFragment();
                break;
            case R.id.btnDone:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);

                mScreenStatus = 0;
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                finishActivity();
            break;
            case R.id.btnMinimize:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);

                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, page);
                finishActivity();
                break;
        }
    }

    @Override
    public void onPageChanged(int page) {
        saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, page);
        saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
    }

    private void startShowGifFragment(){
    }

    private String mExperimentFolder;
    private String mExperimentType;
    private int mLastPage;
    private List<String> mImagePath;

    private void showGifFragment(String experiment_folder, String type_folder, int last_page) {
        Log.d(TAG, "showGifFragment : " + experiment_folder + "/" + type_folder + ":" + last_page);
        if(isStartShowingImage) {
            Toast.makeText(this, "Download in progress. Please wait", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cancel: showGifFragment: " + experiment_folder + "/" + type_folder + ":" + last_page );
            return;
        }

        isStartShowingImage = true;
        mExperimentFolder = experiment_folder;
        mExperimentType = type_folder;
        mLastPage = last_page;
        if(mImagePath != null) {
            mImagePath.clear();
            mImagePath = null;
        }

        mImagePath = new ArrayList<>();
        startDownload();
    }

    private void showBasicElectronic(){
        BasicElectronicFragment mBasicElectronicFragment = new BasicElectronicFragment();
        mBasicElectronicFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mBasicElectronicFragment).commit();
    }

    private void showTutorialFragment() {
        TutorialFragment mTutorialFragment = new TutorialFragment();
        mTutorialFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mTutorialFragment).commit();
    }

    private void showProjectsFragment() {
        ProjectsFragment mProjectsFragment = new ProjectsFragment();
        mProjectsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mProjectsFragment).commit();
    }

    private void showDemoProjectsFragment() {
        DemoProjectsFragment mDemoProjectsFragment = new DemoProjectsFragment();
        mDemoProjectsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mDemoProjectsFragment).commit();
    }

    private void initBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = intent.getIntExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, -1);
                switch (result){
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD");
                        Toast.makeText(getApplicationContext(), "Downloading Experiment. Please wait..", Toast.LENGTH_SHORT ).show();
                        break;
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET");
                        Toast.makeText(getApplicationContext(), "No Network Connection.\nTurn ON network and Retry", Toast.LENGTH_SHORT ).show();
                        isStartShowingImage = false;
                        break;
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE");
                        isStartShowingImage = false;
                        startImageFragment();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void startImageFragment() {
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        int number = getFilesNumber(mExperimentType);
        for(int i=0; i<number; i++) {
            String name = getFilePath(mExperimentType+(i+1));
            mImagePath.add(name);
        }

        ImageFragment mShowGifFragment = ImageFragment.newInstance((ArrayList<String>) mImagePath, mStatusBarColor, mLastPage);
        mShowGifFragment.setOnClickListener(this);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContent, mShowGifFragment)
                .commit();
    }

    private void startDownload() {
        startService(new Intent(this,ImageDownloadService.class)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT, mExperimentFolder)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, mExperimentType));
    }

    private int getFilesNumber(String settings_key) {
        return Utils.readSharedSetting(this, settings_key + "_number", 0);
    }

    private String getFilePath(String settings_key){
        String fileName = Utils.readSharedSetting(this, settings_key, null);
        Log.d(TAG, "Get file key: " + settings_key + "; name: " + fileName);
        return fileName;
    }
}
