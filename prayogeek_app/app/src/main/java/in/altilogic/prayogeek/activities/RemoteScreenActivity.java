package in.altilogic.prayogeek.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import in.altilogic.prayogeek.RemoteButton;
import in.altilogic.prayogeek.RemoteButtonScreen;
import in.altilogic.prayogeek.fragments.ButtonsFragment;
import in.altilogic.prayogeek.fragments.ImageFragment;
import in.altilogic.prayogeek.fragments.SerialConsoleFragment;
import in.altilogic.prayogeek.fragments.SerialConsoleSettingsFragment;
import in.altilogic.prayogeek.service.DatabaseDownloadService;
import in.altilogic.prayogeek.utils.Utils;

public abstract class RemoteScreenActivity extends AppCompatActivity implements View.OnClickListener, ImageFragment.OnClickListener {
    public static final String TAG = "YOUSCOPE-REMOTE_ACT";

    public final static String CURRENT_SCREEN_SETTINGS_PAGE = "REMOTE-SETTINGS-CURRENT-PAGE";
    public final static String CURRENT_SCREEN_SETTINGS = "REMOTE-SETTINGS-CURRENT-SCREEN";
    public final static String CURRENT_SCREEN_SETTINGS_RESUME = "REMOTE-SETTINGS-CURRENT-SCREEN-RESUME";
    private String CURRENT_SCREEN_SETTINGS_FLAG_RESUME = "REMOTE-SETTINGS-CURRENT-FLAG-RESUME";

    public final static String CURRENT_SCREEN_SETTINGS_PAGE_RESUME = "REMOTE-SETTINGS-CURRENT-PAGE-RESUME";
//    private static final String CURRENT_SCREEN_SETTINGS_FIELD = "REMOTE-SETTINGS-CURRENT-SCREEN-FIELD";

    private FragmentManager mFragmentManager;

    private List<RemoteButtonScreen> mRemoteScreens = new ArrayList<>(6);
    private boolean isStartFirebaseDownloading;
    private BroadcastReceiver mBroadcastReceiver;
    private static int mStatusBarColor;
    private int mScreenIndex;
    private String mExperimentType;
    private List<String> mImagePath;
    private String mScreenDocument;
    private String mPrevScreenDocument;
    private int mPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mFragmentManager = getSupportFragmentManager();
        mStatusBarColor = getWindow().getStatusBarColor();
        mScreenIndex = -1;
        mPage = 0;
        initBroadcastReceiver();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        IntentFilter statusIntentFilter = new IntentFilter(DatabaseDownloadService.HW_SERVICE_BROADCAST_VALUE);
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
        stopService(new Intent(this, DatabaseDownloadService.class));
        if(mRemoteScreens !=null) {
            mRemoteScreens.clear();
            mRemoteScreens = null;
        }
        if(mImagePath != null) {
            mImagePath.clear();
            mImagePath = null;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mPage = 0;

        Utils.saveSharedSetting(getApplicationContext(), CURRENT_SCREEN_SETTINGS_PAGE, 0);
        boolean backFlag = false;
        if(mExperimentType != null) {
            if(mExperimentType.equalsIgnoreCase("Serial console")){
                backFlag = true;
                mExperimentType = "";
            }
            if(mExperimentType.equalsIgnoreCase("Serial console settings")){
                mExperimentType = "Serial console";
                showSerialConsoleFragment();
                return;
            }
        }
        if(mRemoteScreens != null && mRemoteScreens.size() > 0 && mScreenIndex <= 0 && !backFlag) {
            mScreenIndex=0;
//            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
//            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mRemoteScreens.get(mScreenIndex).getStatus());
            finishActivity();
        }
        else {
            if(mPrevScreenDocument != null) {
                mScreenIndex = getScreenIndex(mPrevScreenDocument);
                mPrevScreenDocument = null;
            }
            else {
                mScreenIndex=0;
            }
            if(mRemoteScreens != null && mScreenIndex >= 0 && mRemoteScreens.size() > mScreenIndex){
                showButtonsFragment(mRemoteScreens.get(mScreenIndex));
                return;
            }
            finishActivity();
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

    private void initBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = intent.getIntExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, -1);
                switch (result){
                    case DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD");
                        Toast.makeText(getApplicationContext(), "Downloading Experiment. Please wait..", Toast.LENGTH_SHORT ).show();
                        break;
                    case DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET");
                        Toast.makeText(getApplicationContext(), "No Network Connection.\nTurn ON network and Retry", Toast.LENGTH_SHORT ).show();
                        isStartFirebaseDownloading = false;
                        break;
                    case DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE");
                        isStartFirebaseDownloading = false;
                        String experiment = intent.getStringExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT);
                        if(experiment!= null && mExperimentType == null)
                            mExperimentType = experiment;
                        if(mImagePath != null) {
                            mImagePath.clear();
                            mImagePath = null;
                        }

                        mImagePath = new ArrayList<>();

                        showImageFragment();
                        break;
                    case DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_DOWNLOAD_FAIL:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_DOWNLOAD_FAIL");
                        Toast.makeText(getApplicationContext(), "Download Fail", Toast.LENGTH_SHORT ).show();
                        isStartFirebaseDownloading = false;
                        break;
                    case DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN");
                        isStartFirebaseDownloading = false;
                        RemoteButtonScreen screen = intent.getParcelableExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_SCREEN);

                        if(screen == null)
                            return;

                        if(!isScreenComtains(screen))
                            mRemoteScreens.add(screen);

                        mScreenIndex = getScreenIndex(screen.getScreenName());
                        showButtonsFragment(screen);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private int getScreenIndex(String document) {
        if(mRemoteScreens == null || mRemoteScreens.size() == 0 || document == null)
            return -1;

        for(int i = 0; i< mRemoteScreens.size() ; i++ ) {
            if(mRemoteScreens.get(i) != null) {
                String screenName = mRemoteScreens.get(i).getScreenName();
                if (screenName != null) {
                    if(document.equals(screenName)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    void findRemoteScreen(String document) {
        mScreenDocument = document;

        Log.d(TAG, "Download remote screen " + mScreenDocument);
        if(isResume()) {
            String nameScreen = Utils.readSharedSetting(this, CURRENT_SCREEN_SETTINGS, null);
            int page = Utils.readSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, -1);

            if(mRemoteScreens != null && mRemoteScreens.size() == 0 && nameScreen != null) {
                mRemoteScreens.add(Utils.loadScreen(this, mScreenDocument));
                if(!mScreenDocument.equals(nameScreen))
                    mRemoteScreens.add(Utils.loadScreen(this, nameScreen));
                else {
                    mPage = page;
                    mScreenDocument = nameScreen;
                    startDownloadScreen(mScreenDocument);
                    return;
                }

                mScreenIndex = getScreenIndex(nameScreen);
                if(mScreenIndex < 0 || page >= mRemoteScreens.get(mScreenIndex).buttonsSize()) {
                    Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_FLAG_RESUME, 0);
                    return;
                }

                RemoteButton rbs = mRemoteScreens.get(mScreenIndex).getRemoteButton(mRemoteScreens.get(mScreenIndex).getStatus());
                if(rbs == null) {
                    Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_FLAG_RESUME, 0);
                    return;
                }
                mExperimentType = rbs.getName();
                startDownloadImages(rbs.getCollection(), rbs.getDocument(), rbs.getField());
                mPage = page;
                mScreenDocument = nameScreen;
            }
        }
        else {
            startDownloadScreen(mScreenDocument);
        }
    }

    private void showSerialConsoleFragment(){
        SerialConsoleFragment mConsoleFragment = new SerialConsoleFragment();
        mConsoleFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mConsoleFragment).commit();
    }

    private void showSerialSettingsFragment() {
        mExperimentType = "Serial console settings";
        SerialConsoleSettingsFragment mSettingsFragment = new SerialConsoleSettingsFragment();
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mSettingsFragment).commit();
    }


    private void showImageFragment() {
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if(mScreenIndex >= 0 && mExperimentType != null) {
            RemoteButton remoteButton = mRemoteScreens.get(mScreenIndex).getRemoteButton(mExperimentType);
            int number = getFilesNumber(remoteButton.getField());
            for(int i=0; i<number; i++) {
                String name = getFilePath(remoteButton.getField()+(i+1));
                mImagePath.add(name);
            }

            mRemoteScreens.get(mScreenIndex).setStatus(remoteButton.getId());
        }
        else { // picture only
            int number = getFilesNumber(mExperimentType);
            for(int i=0; i<number; i++) {
                String name = getFilePath(mExperimentType+(i+1));
                mImagePath.add(name);
            }
        }

        if(mImagePath == null || mImagePath.size() == 0)
            return;

        ImageFragment showGifFragment = ImageFragment.newInstance((ArrayList<String>) mImagePath, mStatusBarColor, mPage, false);
        showGifFragment.setOnClickListener(this);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContent, showGifFragment)
                .commit();
    }

    private int getFilesNumber(String settings_key) {
        return Utils.readSharedSetting(this, settings_key + "_number", 0);
    }

    private String getFilePath(String settings_key){
        String fileName = Utils.readSharedSetting(this, settings_key, null);
        Log.d(TAG, "Get file key: " + settings_key + "; name: " + fileName);
        return fileName;
    }

    @Override
    public void onClick(View view) {
        mPage = 0;
        if(view == null)
            return;
        int id = view.getId();
        RemoteButton clickedButton = mRemoteScreens.get(mScreenIndex).getRemoteButton(id);
        if(clickedButton != null) {
            Log.d(TAG, "onClick() press button " + clickedButton.getName() + "; Start open screen " + clickedButton.getCollection());
            mPrevScreenDocument = mRemoteScreens.get(mScreenIndex).getScreenName();
            mExperimentType = clickedButton.getName();

            if(mExperimentType.equals("Projects")) {
                if(!((Global_Var) getApplicationContext()).isProject_Access()) {
                    Toast.makeText(getApplicationContext(), "Please Subscribe to access Projects", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else if(mExperimentType.equalsIgnoreCase("Serial console")) {
                showSerialConsoleFragment();
                return;
            }
            else if(mExperimentType.equalsIgnoreCase("Resume")) {
                String nameScreen = Utils.readSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, null);
                int last_screen_page = Utils.readSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, 0);
                if(nameScreen != null) {
                    RemoteButtonScreen rbs = Utils.loadScreen(this, nameScreen);
                    if(rbs != null) {
                        mPage = last_screen_page;
                        RemoteButton rb = rbs.getRemoteButton(rbs.getStatus());
                        if(rb != null) {
                            if(!isScreenComtains(rbs))
                                mRemoteScreens.add(rbs);

                            mExperimentType = rb.getName();
                            mScreenDocument = nameScreen;
                            mScreenIndex = getScreenIndex(nameScreen);
                            startDownloadImages(rb.getCollection(), rb.getDocument(), rb.getField());
                        }
                    }
                }
                return;
            }

            if(clickedButton.getType() != null && clickedButton.getType().equals("picture"))
                startDownloadImages(clickedButton.getCollection(),clickedButton.getDocument(), clickedButton.getField());
            else
                findRemoteScreen(clickedButton.getName());
        }
        else {
            if(id == R.id.btnConsoleSettings)
                showSerialSettingsFragment();
        }
    }

    @Override
    public void onClick(View view, int page) {
        mPage = 0;
        if(mRemoteScreens == null) {
            finishActivity();
            return;
        }

        switch (view.getId()){
            case R.id.btnHome: {
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenDocument);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
                int ind = getScreenIndex(mScreenDocument);
                if(ind >= 0) {
                    RemoteButtonScreen rbs = mRemoteScreens.get(ind);
                    Utils.saveScreen(this, rbs.getScreenName(), rbs);
                }

                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_FLAG_RESUME, 0);

                if(mScreenIndex < 0 || mScreenIndex >= mRemoteScreens.size()) {
                    finishActivity();
                }
                else {
                    mRemoteScreens.get(mScreenIndex).setStatus(0);
                    mPrevScreenDocument = null;
                    mScreenIndex=0;
                    showButtonsFragment(mRemoteScreens.get(mScreenIndex));
                }
                break;
            }
            case R.id.btnDone: {
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenDocument);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
                int ind = getScreenIndex(mScreenDocument);
                if(ind >= 0) {
                    RemoteButtonScreen rbs = mRemoteScreens.get(ind);
                    Utils.saveScreen(this, rbs.getScreenName(), rbs);
                }
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_FLAG_RESUME, 0);
                if(mRemoteScreens.size() > mScreenIndex && mScreenIndex >= 0) {
                    mRemoteScreens.get(mScreenIndex).setStatus(0);
                }
                finishActivity();
                break;
            }
            case R.id.btnMinimize: {
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenDocument);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);

                int ind = getScreenIndex(mScreenDocument);
                if(ind >= 0) {
                    RemoteButtonScreen rbs = mRemoteScreens.get(ind);
                    Utils.saveScreen(this, rbs.getScreenName(), rbs);
                }
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, mScreenDocument);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, page);
                Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_FLAG_RESUME, 1);

                finishActivity();
                break;
            }
        }
    }

    private void showButtonsFragment(RemoteButtonScreen screen) {
        if(screen == null)
            return;
        Log.d(TAG, "Show button fragment " + screen.getScreenName());
        screen.setStatus(0);
        ButtonsFragment buttonsFragment = ButtonsFragment.newInstance(screen);
        buttonsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, buttonsFragment)
                .commit();
    }

    @Override
    public void onPageChanged(int page) {
        Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, page);
        Utils.saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
    }

    private void startDownloadImages(String collection, String folder, String field) {
        Log.d(TAG, "showGifFragment : " + collection + "/" + folder + "/" + field);
        if(isStartFirebaseDownloading) {
            Toast.makeText(this, "Download in progress. Please wait", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cancel: showGifFragment: " + collection + "/" + folder + "/" + field);
            return;
        }

        isStartFirebaseDownloading = true;
        startService(new Intent(this, DatabaseDownloadService.class)
                .putExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES)
                .putExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_COLLECTION, collection)
                .putExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT, folder)
                .putExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, field));
    }

    private void startDownloadScreen(String document) {
        Log.d(TAG, "Start download screen " + document);
        startService(new Intent(this, DatabaseDownloadService.class)
                .putExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, DatabaseDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN)
                .putExtra(DatabaseDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_DOCUMENT, document));
    }

    boolean isScreenComtains(RemoteButtonScreen screen) {
        for(int i=0; i<mRemoteScreens.size(); i++) {
            if(mRemoteScreens.get(i).getScreenName().equals(screen.getScreenName())) {
                return true;
            }
        }
        return false;
    }

    boolean isResume() {
        return 1 == Utils.readSharedSetting(this, CURRENT_SCREEN_SETTINGS_FLAG_RESUME, 0);
    }
}
