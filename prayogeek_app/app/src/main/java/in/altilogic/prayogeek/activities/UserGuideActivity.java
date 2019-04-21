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

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.RemoteButtonScreen;
import in.altilogic.prayogeek.fragments.ButtonsFragment;
import in.altilogic.prayogeek.fragments.ImageFragment;
import in.altilogic.prayogeek.service.ImageDownloadService;
import in.altilogic.prayogeek.utils.Utils;

import static in.altilogic.prayogeek.utils.Utils.saveSharedSetting;

public class UserGuideActivity extends AppCompatActivity implements View.OnClickListener, ImageFragment.OnClickListener  {
    public static final String TAG = "YOUSCOPE-USER-GUIDE";

    public final static String CURRENT_SCREEN_SETTINGS = "USERGUIDE-SETTINGS-CURRENT-SCREEN";
    public final static String CURRENT_SCREEN_SETTINGS_PAGE = "USERGUIDE-SETTINGS-CURRENT-PAGE";
    public final static String CURRENT_SCREEN_SETTINGS_RESUME = "USERGUIDE-SETTINGS-CURRENT-SCREEN-RESUME";
    public final static String CURRENT_SCREEN_SETTINGS_PAGE_RESUME = "USERGUIDE-SETTINGS-CURRENT-PAGE-RESUME";

    private FragmentManager mFragmentManager;
    private FireBaseHelper mFireBaseHelper;

    private RemoteButtonScreen mUserGuide;
    private boolean isStartShowingImage;
    private BroadcastReceiver mBroadcastReceiver;
    private static int mStatusBarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mFragmentManager = getSupportFragmentManager();
        mFireBaseHelper = new FireBaseHelper();
        mStatusBarColor = getWindow().getStatusBarColor();

        Log.d(TAG, "onCreate()");
        downloadRemoteScreen("Screen_UserGuide");
        initBroadcastReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
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
        saveSharedSetting(getApplicationContext(), CURRENT_SCREEN_SETTINGS_PAGE, 0);
        if(mUserGuide != null && mUserGuide.getStatus() == 0) {
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mUserGuide.getStatus());
            finishActivity();
        }
        else {
            showUserGuideFragment();
        }
    }

    private void showUserGuideFragment() {
        mUserGuide.setStatus(0);
        ButtonsFragment buttonsFragment = ButtonsFragment.newInstance(mUserGuide);
        buttonsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, buttonsFragment)
                .commit();
    }

    @Override
    public void onClick(View view) {
        if(view == null)
            return;
        int id = view.getId();
        RemoteButtonScreen.RemoteButton clickedButton = mUserGuide.getRemoteButton(id);
        if(clickedButton != null) {
            Log.d(TAG, "onClick() press button " + clickedButton.getName() + "; Start open screen " + clickedButton.getCollectionLinkName());
            showGifFragment(clickedButton.getCollectionLinkName(),"Images", clickedButton.getName());
        }
    }


    @Override
    public void onClick(View view, int page) {
        int mScreenStatus = mUserGuide != null ? mUserGuide.getStatus() : 0;
        Log.d(TAG, "mScreenStatus = " + mScreenStatus + "; page = " + page);
        switch (view.getId()){
            case R.id.btnHome:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
                if(mUserGuide!= null)
                    mUserGuide.setStatus(0);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                showUserGuideFragment();
                break;
            case R.id.btnDone:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
                if(mUserGuide!= null)
                    mUserGuide.setStatus(0);
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

    private String mExperimentType;
    private List<String> mImagePath;

    private void showGifFragment(String collection, String folder, String type) {
        mExperimentType = type;
        Log.d(TAG, "showGifFragment : " + collection + "/" + folder + "/" + type);
        if(isStartShowingImage) {
            Toast.makeText(this, "Download in progress. Please wait", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cancel: showGifFragment: " + collection + "/" + folder + "/" + type);
            return;
        }

        isStartShowingImage = true;
        if(mImagePath != null) {
            mImagePath.clear();
            mImagePath = null;
        }

        mImagePath = new ArrayList<>();
        startDownload(collection, folder, type);
    }

    private void startDownload(String collection, String folder, String type) {
        startService(new Intent(this,ImageDownloadService.class)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_COLLECTION, collection)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT, folder)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, type));
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

        if(mUserGuide == null || mUserGuide.getOrientation() == null || mUserGuide.getOrientation().equals("landscape")) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mUserGuide.setStatus(mUserGuide.getRemoteButton(mExperimentType).getId());
        ImageFragment mShowGifFragment = ImageFragment.newInstance((ArrayList<String>) mImagePath, mStatusBarColor, 0, false);
        mShowGifFragment.setOnClickListener(this);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContent, mShowGifFragment)
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
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_DOWNLOAD_FAIL:
                        Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_IMAGE_DOWNLOAD_FAIL");
                        Toast.makeText(getApplicationContext(), "Download Fail", Toast.LENGTH_SHORT ).show();
                        isStartShowingImage = false;
                        break;
                    default:
                        break;
                }
            }
        };
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

    private void downloadRemoteScreen(String name) {
        new Thread(() -> mFireBaseHelper.read(name, "Buttons", task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Listen failed.");
                return;
            }
            DocumentSnapshot documentSnapshot = task.getResult();

            if (documentSnapshot != null && !documentSnapshot.exists()) {
                Log.d(TAG, "No such document");
                mExperimentType = "Default";
                mFireBaseHelper.read(name, "Images", task2 -> {
                    if (!task2.isSuccessful()) {
                        Log.w(TAG, "Listen failed.");
                        return;
                    }
                    DocumentSnapshot documentSnapshot2 = task2.getResult();

                    if (documentSnapshot2 != null && !documentSnapshot2.exists()) {
                        Log.d(TAG, "No such document");
                    }
                    showGifFragment(name, "Images", "Default");
                });
                return;
            }

            List<String> screen_parameters = mFireBaseHelper.getArray(documentSnapshot);

            if(screen_parameters.contains("names")){
                List<String> mNames = (List<String>) documentSnapshot.get("names");
                if(mNames == null)
                    return;
                mUserGuide = new RemoteButtonScreen(mNames);

                for(String buttName : mNames) {
                    if(screen_parameters.contains(buttName)){
                        String buttonCollectionName = (String) documentSnapshot.get(buttName);
                        mUserGuide.getRemoteButton(buttName).setLinkName(buttonCollectionName);
                    }
                }
            }
            if(mUserGuide == null)
                return;

            if(screen_parameters.contains("version")){
                String version = (String) Objects.requireNonNull(documentSnapshot).get("version");
                mUserGuide.setVersion(version);
            }
            if(screen_parameters.contains("orientation")){
                assert documentSnapshot != null;
                String orientation = (String) documentSnapshot.get("orientation");
                mUserGuide.setOrientation(orientation);
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                runOnUiThread(() -> showUserGuideFragment());
            }
        })).start();
    }
}
