package in.altilogic.prayogeek.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.BasicElectronicFragment;
import in.altilogic.prayogeek.fragments.DemoProjectsFragment;
import in.altilogic.prayogeek.fragments.ImageFragment;
import in.altilogic.prayogeek.fragments.ProjectsFragment;
import in.altilogic.prayogeek.fragments.TutorialFragment;
import in.altilogic.prayogeek.service.ImageDownloadService;

import static in.altilogic.prayogeek.utils.Utils.*;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener, ImageFragment.OnClickListener {
    private FragmentManager mFragmentManager;

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

    /**
     * TODO UPDATE pictures
     * To change screen pictures, you need to add these pictures to the DRAWABLE folder,
     * then add the ID of each image to the corresponding array below.
     */
    private final static int[] mBreadboardImages = {R.drawable.breadboard, R.drawable.breadboard_01, R.drawable.breadboard_02};
    private final static int[] mLedOnOffImages = {R.drawable.led_on_off};
    private final static int[] mPowerSupplyImages = {R.drawable.power_supply};
    private final static int[] mTransistorSwitchImages = {R.drawable.transistor_switch};
    private final static int[] mIC741Images = {R.drawable.ic741};
    private final static int[] mIC555Images = {R.drawable.ic555};
    private final static int[] mProject1Images = {R.drawable.project1};
    private final static int[] mProject2Images = {R.drawable.project2};
    private final static int[] mDemoProject1Images = {R.drawable.demo_project1, R.drawable.demo_project1_2, R.drawable.demo_project1_3};
    private final static int[] mDemoProject2Images = {R.drawable.demo_project1, R.drawable.demo_project2};

    private static int mStatusBarColor;

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
    }

    private void showFragment(int last_screen_id, int last_page) {
        switch(last_screen_id){
            case 0: mScreenStatus = 0; showTutorialFragment(); break;
            case SCREEN_ID_BREADBOARD_USAGE: mScreenStatus = SCREEN_ID_BREADBOARD_USAGE; showGifFragment(mBreadboardImages, last_page); break;
            case SCREEN_ID_LED_ON_OFF: mScreenStatus = SCREEN_ID_LED_ON_OFF; showGifFragment(mLedOnOffImages, last_page); break;
            case SCREEN_ID_POWER_SUPPLY: mScreenStatus = SCREEN_ID_POWER_SUPPLY; showGifFragment(mPowerSupplyImages, last_page); break;
            case SCREEN_ID_TRANSISTOR_SWITCH: mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;  showGifFragment(mTransistorSwitchImages, last_page);  break;
            case SCREEN_ID_IC741: mScreenStatus = SCREEN_ID_IC741; showGifFragment(mIC741Images, last_page); break;
            case SCREEN_ID_IC555: mScreenStatus = SCREEN_ID_IC555; showGifFragment(mIC555Images, last_page); break;
            case SCREEN_ID_PROJECT1: mScreenStatus = SCREEN_ID_PROJECT1; showGifFragment(mProject1Images, last_page); break;
            case SCREEN_ID_PROJECT2: mScreenStatus = SCREEN_ID_PROJECT2; showGifFragment(mProject2Images, last_page); break;
            case SCREEN_ID_DEMO_PROJECT1: mScreenStatus = SCREEN_ID_DEMO_PROJECT1; showGifFragment(mDemoProject1Images, last_page); break;
            case SCREEN_ID_DEMO_PROJECT2: mScreenStatus = SCREEN_ID_DEMO_PROJECT2; showGifFragment(mDemoProject2Images, last_page); break;
            default: mScreenStatus = 0; showTutorialFragment(); break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(ImageDownloadService.HW_SERVICE_BROADCAST_VALUE));
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
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
            case R.id.btnBreadBoard: mScreenStatus = SCREEN_ID_BREADBOARD_USAGE; showGifFragment(mBreadboardImages, last_page); break;
            case R.id.btnLedOnOFF: mScreenStatus = SCREEN_ID_LED_ON_OFF;  showGifFragment(mLedOnOffImages, last_page);  break;
            case R.id.btnPowerSupply: mScreenStatus = SCREEN_ID_POWER_SUPPLY; showGifFragment(mPowerSupplyImages, last_page); break;
            case R.id.btnTransistorSwitch: mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;  showGifFragment(mTransistorSwitchImages, last_page); break;
            case R.id.btnIC741: mScreenStatus = SCREEN_ID_IC741; showGifFragment(mIC741Images, last_page); break;
            case R.id.btnIC555: mScreenStatus = SCREEN_ID_IC555; showGifFragment(mIC555Images, last_page); break;
            case R.id.btnProject1: mScreenStatus = SCREEN_ID_PROJECT1; showGifFragment(mProject1Images, last_page); break;
            case R.id.btnProject2: mScreenStatus = SCREEN_ID_PROJECT2; showGifFragment(mProject2Images, last_page); break;
            case R.id.btnDemoProject1: mScreenStatus = SCREEN_ID_DEMO_PROJECT1; showGifFragment(mDemoProject1Images, last_page); break;
            case R.id.btnDemoProject2:  mScreenStatus = SCREEN_ID_DEMO_PROJECT2; showGifFragment(mDemoProject2Images, last_page); break;
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


    private void showGifFragment(int[] drawable_id, int last_page) {
//        startDownloadImage("Breadboard", "Documents");
//        download(fireStorePath);
        startService(new Intent(this,ImageDownloadService.class)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, "Breadboard")
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE,""));
        ImageFragment mShowGifFragment = ImageFragment.newInstance(drawable_id, mStatusBarColor, last_page);
        mShowGifFragment.setOnClickListener(this);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContent, mShowGifFragment)
                .commit();
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
}
