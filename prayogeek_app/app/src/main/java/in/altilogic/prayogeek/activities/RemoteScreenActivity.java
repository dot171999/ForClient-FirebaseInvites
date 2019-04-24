package in.altilogic.prayogeek.activities;

import android.annotation.SuppressLint;
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
import java.util.Map;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.RemoteButtonScreen;
import in.altilogic.prayogeek.fragments.ButtonsFragment;
import in.altilogic.prayogeek.fragments.ImageFragment;
import in.altilogic.prayogeek.service.ImageDownloadService;
import in.altilogic.prayogeek.utils.Utils;

import static in.altilogic.prayogeek.utils.Utils.readSharedSetting;
import static in.altilogic.prayogeek.utils.Utils.saveSharedSetting;

public class RemoteScreenActivity extends AppCompatActivity implements View.OnClickListener, ImageFragment.OnClickListener {
    public static final String TAG = "YOUSCOPE-REMOTE_ACT";

    public final static String CURRENT_SCREEN_SETTINGS = "REMOTE-SETTINGS-CURRENT-SCREEN";
    public final static String CURRENT_SCREEN_SETTINGS_PAGE = "REMOTE-SETTINGS-CURRENT-PAGE";
    public final static String CURRENT_SCREEN_SETTINGS_RESUME = "REMOTE-SETTINGS-CURRENT-SCREEN-RESUME";
    public final static String CURRENT_SCREEN_SETTINGS_PAGE_RESUME = "REMOTE-SETTINGS-CURRENT-PAGE-RESUME";
    private static final String CURRENT_SCREEN_SETTINGS_FIELD = "REMOTE-SETTINGS-CURRENT-SCREEN-FIELD";

    private FragmentManager mFragmentManager;
    private FireBaseHelper mFireBaseHelper;

    private List<RemoteButtonScreen> mRemoteScreen;
    private boolean isStartShowingImage;
    private BroadcastReceiver mBroadcastReceiver;
    private static int mStatusBarColor;
    private int mScreenIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mFragmentManager = getSupportFragmentManager();
        mFireBaseHelper = new FireBaseHelper();
        mStatusBarColor = getWindow().getStatusBarColor();
        mRemoteScreen = new ArrayList<>(2);
        mScreenIndex = -1;
        initBroadcastReceiver();
        Log.d(TAG, "onCreate()");
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
        if(mRemoteScreen != null && mRemoteScreen.size() > 0 && mScreenIndex <= 0) {
            mScreenIndex=0;
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
            saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mRemoteScreen.get(mScreenIndex).getStatus());
            finishActivity();
        }
        else {
            mScreenIndex--;
            showButtonsFragment();
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

    private int getScreenIndex(List<String> buttonNames) {
        if(mRemoteScreen == null || mRemoteScreen.size() == 0 ||
                buttonNames == null || buttonNames.size() == 0)
            return -1;

        int compareCount = 0;
        int ind = 1;
        for( int i=0;i<mRemoteScreen.size() ; i++ ) {
            for(int j=0; j<buttonNames.size(); j++){
                if(mRemoteScreen.get(i).getRemoteButton(buttonNames.get(j)) != null) {
                    compareCount++;
                    ind = 1;
                }
            }
        }

        if(mRemoteScreen.size() > ind && compareCount == mRemoteScreen.get(ind).buttonsSize()) {
            return ind;
        }
        return -1;
    }

    void downloadRemoteScreen(String document) {
        Log.d(TAG, "Download remote screen " + document);
        new Thread(() ->
                mFireBaseHelper.read("Screens", document, task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Listen failed.");
                return;
            }
            DocumentSnapshot documentSnapshot = task.getResult();

            if (documentSnapshot == null) {
                Log.w(TAG, "Listen failed.");
                return;
            }

            List<String> screen_parameters = mFireBaseHelper.getArray(documentSnapshot);

            RemoteButtonScreen screen = null;
            if(screen_parameters.contains("names")){
                List<String> mNames = (List<String>) documentSnapshot.get("names");
                if(mNames == null)
                    return;

                int screenIndex = getScreenIndex(mNames);

                if(screenIndex < 0) {
                    screen = new RemoteButtonScreen(mNames);
                    mRemoteScreen.add(screen);
                    mScreenIndex++;

                    for(String buttName : mNames) {
                        if(screen_parameters.contains(Utils.checkSlashSymbols(buttName))){
                            Map<String, Object> dataMap = (Map<String, Object>) documentSnapshot.get(Utils.checkSlashSymbols(buttName));
                            screen.getRemoteButton(buttName).setParameters(dataMap);
                        }
                    }
                }
                else {
                    mScreenIndex = screenIndex;
                    screen = mRemoteScreen.get(screenIndex);
                }
            }
            if(mRemoteScreen.size() == 0 || screen == null)
                return;

            if(screen_parameters.contains("orientation")){
                String orientation = (String) documentSnapshot.get("orientation");
                screen.setOrientation(orientation);
            }
            if(screen_parameters.contains("version")){
                long version = (Long) documentSnapshot.get("version");
                screen.setVersion(version + "");
            }

            if (documentSnapshot.exists()) {
                runOnUiThread(() -> showButtonsFragment());
            }
        })).start();
    }

    private void startImageFragment() {
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        RemoteButtonScreen.RemoteButton remoteButton = mRemoteScreen.get(mScreenIndex).getRemoteButton(mExperimentType);
        int number = getFilesNumber(remoteButton.getField());
        for(int i=0; i<number; i++) {
            String name = getFilePath(remoteButton.getField()+(i+1));
            mImagePath.add(name);
        }

        if(mRemoteScreen == null || mRemoteScreen.get(mScreenIndex).getOrientation() == null ||
                mRemoteScreen.get(mScreenIndex).getOrientation().equals("landscape")) {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mRemoteScreen.get(mScreenIndex).setStatus(remoteButton.getId());
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

    @Override
    public void onClick(View view) {
        if(view == null)
            return;
        int id = view.getId();
        RemoteButtonScreen.RemoteButton clickedButton = mRemoteScreen.get(mScreenIndex).getRemoteButton(id);
        if(clickedButton != null) {
            Log.d(TAG, "onClick() press button " + clickedButton.getName() + "; Start open screen " + clickedButton.getCollection());
            mExperimentType = clickedButton.getName();
            if(clickedButton.getType().equals("picture"))
                showGifFragment(clickedButton.getCollection(),clickedButton.getDocument(), clickedButton.getField());
            else
                downloadRemoteScreen(clickedButton.getName());
        }
    }

    @Override
    public void onClick(View view, int page) {
        int mScreenStatus = mRemoteScreen != null ? mRemoteScreen.get(mScreenIndex).getStatus() : 0;
        Log.d(TAG, "mScreenStatus = " + mScreenStatus + "; page = " + page);
        switch (view.getId()){
            case R.id.btnHome:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
                if(mRemoteScreen != null)
                    mRemoteScreen.get(mScreenIndex).setStatus(0);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, 0);
                showButtonsFragment();
                break;
            case R.id.btnDone:
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_RESUME, mScreenStatus);
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
                if(mRemoteScreen != null)
                    mRemoteScreen.get(mScreenIndex).setStatus(0);
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

    private void startDownload(String collection, String folder, String type) {
        startService(new Intent(this,ImageDownloadService.class)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_COLLECTION, collection)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT, folder)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, type));
    }


    @SuppressLint("Assert")
    private void showButtonsFragment() {
        assert (mScreenIndex > mRemoteScreen.size());
        mRemoteScreen.get(mScreenIndex).setStatus(0);
        ButtonsFragment buttonsFragment = ButtonsFragment.newInstance(mRemoteScreen.get(mScreenIndex));
        buttonsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, buttonsFragment)
                .commit();
    }


    @Override
    public void onPageChanged(int page) {
        saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE, page);
        saveSharedSetting(this, CURRENT_SCREEN_SETTINGS_PAGE_RESUME, page);
    }

    private String mExperimentType;
    private List<String> mImagePath;

    private void showGifFragment(String collection, String folder, String field) {
        Log.d(TAG, "showGifFragment : " + collection + "/" + folder + "/" + field);
        if(isStartShowingImage) {
            Toast.makeText(this, "Download in progress. Please wait", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cancel: showGifFragment: " + collection + "/" + folder + "/" + field);
            return;
        }

        isStartShowingImage = true;
        if(mImagePath != null) {
            mImagePath.clear();
            mImagePath = null;
        }

        mImagePath = new ArrayList<>();
        startDownload(collection, folder, field);
    }
}
