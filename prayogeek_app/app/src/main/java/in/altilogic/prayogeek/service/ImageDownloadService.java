package in.altilogic.prayogeek.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.utils.Utils;

public class ImageDownloadService  extends IntentService {
    private final String TAG = "YOUSCOPE-DB-SERVICE";
    public static final String HW_SERVICE_BROADCAST_VALUE = "prayogeek.altilogic.in";
    public static final String HW_SERVICE_MESSAGE_TYPE_ID = "MESSAGE_TYPE_ID";
    public static final int HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES = 1;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES = 2;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD = 3;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET = 4;

    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT = "MESSAGE_TYPE_EXPERIMENT";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE = "MESSAGE_TYPE_PATH_FIRESTORE";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE = "MESSAGE_TYPE_PATH_PHONE";
    public static final String HW_SERVICE_MESSAGE_IMAGE_FILES = "MESSAGE_TYPE_IMAGE_FILES";

    private FireBaseHelper mFireBaseHelper;

    private int mFilesNumber = 0;
    private boolean mIsOnline;
    public ImageDownloadService() {
        super("ImageDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int message_type = intent.getIntExtra(HW_SERVICE_MESSAGE_TYPE_ID, -1);
        switch (message_type) {
            case HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES:
                String experimentPath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT);
                String fireStorePath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE);
                String phonePath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE);
                Log.d(TAG, "Start download image from " + fireStorePath + " to " + phonePath);

                mFilesNumber = 0;
                mIsOnline = Utils.isOnline(this);

                startDownloadImage(experimentPath, fireStorePath, "");
                break;
            default:
                break;
        }
    }
    ConditionVariable mConditionVariable;

    private void startDownloadImage(final String experiment_folder, final String base_electronis_type, String phonePath) {
        Log.d(TAG, "Start download "+ experiment_folder +"/"+ base_electronis_type);
        if(mFireBaseHelper == null)
            mFireBaseHelper = new FireBaseHelper();

        mFireBaseHelper.read("Tutorials", experiment_folder, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);

                    if(!mIsOnline)
                        notifyActivityAboutNoInternetConnection();

                    return;
                }
                List<String> basic_electronics = mFireBaseHelper.getArray(documentSnapshot);

                if(basic_electronics.contains(base_electronis_type)) {
                    List<String> breadboard_urls = mFireBaseHelper.getArray(documentSnapshot, base_electronis_type, "imageURL");
                    if(breadboard_urls == null)
                        return;

                    int firestore_images_version = mFireBaseHelper.getLong(documentSnapshot, base_electronis_type, "version");

                    if(firestore_images_version <= 0 && !mIsOnline)
                    {
                        notifyActivityAboutNoInternetConnection();
                        return;
                    }

//                    if(( getLocaleImagesVersion(base_electronis_type) != firestore_images_version) || getLocaleImagesVersion(base_electronis_type) == 0 )
                    {
                        int count = 1;
                        notifyActivityAboutStartDownload();
                        mFilesNumber = breadboard_urls.size();
                        saveImagesVersion(base_electronis_type, firestore_images_version);
                        saveFilesNumber(base_electronis_type, breadboard_urls.size() );
                        mConditionVariable = new ConditionVariable();
                        for(String path: breadboard_urls) {
                            downloadUri(path, base_electronis_type, count++);
//                            mConditionVariable.block();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
//                    else {
//                        notifyActivityAboutNewFiles();
//                    }
                }
            }
        });
    }

    private void downloadUri(String path, final String electronic_type, final int num){
        Log.d(TAG,"Start download: " + path);
        final StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(path);
        final String fileName = mStorageRef.getName();
        File localFile = null;
        try {
            localFile = File.createTempFile(fileName, "", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localFile != null) {
            final File finalLocalFile = localFile;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mStorageRef.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "Notify activity about downloaded images " + finalLocalFile.getName());
                            saveFileName(electronic_type + num, finalLocalFile.getAbsolutePath());

                            if(num >= mFilesNumber) {
                                mFilesNumber = 0;
                                notifyActivityAboutNewFiles();
                            }
//                            mConditionVariable.open();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Exception: " + e.toString());
//                            mConditionVariable.open();
                        }
                    });
                }
            }).start();
        }
    }

    private void saveFileName(String settings_key, String filename) {
        Log.d(TAG, "Save file name: " + settings_key + "; " + filename);
        Utils.saveSharedSetting(this, settings_key, filename);
    }

    private void saveFilesNumber(String settings_key, int number) {
        Utils.saveSharedSetting(this, settings_key + "_number", number);
    }

    private void saveImagesVersion(String imagesType, int version) {
        Utils.saveSharedSetting(this, imagesType, version);
    }

    private int getLocaleImagesVersion(String imagesType) {
        return Utils.readSharedSetting(this, imagesType, 0);
    }

    private void notifyActivityAboutNewFiles() {
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }

    private void notifyActivityAboutStartDownload() {
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }

    private void notifyActivityAboutNoInternetConnection() {
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }
}
