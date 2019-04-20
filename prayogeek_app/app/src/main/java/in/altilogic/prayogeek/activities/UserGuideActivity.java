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

public class UserGuideActivity extends AppCompatActivity implements View.OnClickListener, ImageFragment.OnClickListener  {
    public static final String TAG = "YOUSCOPE-USER-GUIDE";

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
        new Thread(() -> mFireBaseHelper.read("Screen_UserGuide", "Buttons", task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Listen failed.");
                return;
            }
            DocumentSnapshot documentSnapshot = task.getResult();

            if (documentSnapshot != null && !documentSnapshot.exists()) {
                Log.d(TAG, "No such document");
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
//                        showMessageDownloadFail();
                        isStartShowingImage = false;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View view, int page) {

    }

    @Override
    public void onPageChanged(int page) {

    }
}
