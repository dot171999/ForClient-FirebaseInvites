package in.altilogic.prayogeek.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.utils.Utils;

public class ImageDownloadService  extends IntentService {
    private final String TAG = "YOUSCOPE-DB-SERVICE";
    public static final String HW_SERVICE_BROADCAST_VALUE = "prayogeek.altilogic.in";
    public static final String HW_SERVICE_MESSAGE_TYPE_ID = "MESSAGE_TYPE_ID";
    public static final int HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES = 1;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE = 2;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD = 3;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET = 4;

    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT = "MESSAGE_TYPE_EXPERIMENT";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE = "MESSAGE_TYPE_PATH_FIRESTORE";

    private FireBaseHelper mFireBaseHelper;

    private int mFilesNumber = 0;
    private int mFilesCounter = 0;
    private boolean mIsOnline;
    private boolean mStartDownloadNotify;
    public ImageDownloadService() {
        super("ImageDownloadService");
    }
    private Thread mDownloadImagesThread;
    private String mExperimentType;
    private boolean mIsLocFilesNotFound;
    @Override
    protected void onHandleIntent(Intent intent) {
        int message_type = intent.getIntExtra(HW_SERVICE_MESSAGE_TYPE_ID, -1);
        switch (message_type) {
            case HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES:
                final String experimentPath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT);
                String fireStorePath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE);
                Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES: " + experimentPath + "/"+fireStorePath);

                mExperimentType = fireStorePath;
                mFilesNumber = 0;
                mFilesCounter = 0;
                mIsOnline = Utils.isOnline(this);
                mStartDownloadNotify = false;
                mIsLocFilesNotFound = isLocalFilesNotFound();
                if(getLocaleImagesVersion() <= 0 || mIsLocFilesNotFound)
                    notifyActivityAboutStartDownload();


                if(mDownloadImagesThread != null)
                    mDownloadImagesThread = null;

                mDownloadImagesThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startDownloadImage(experimentPath);
                    }
                });

                mDownloadImagesThread.start();

                break;
            default:
                break;
        }
    }

    private void startDownloadImage(final String experiment_folder) {
        Log.d(TAG, "Start download "+ experiment_folder +"/"+ mExperimentType);
        if(mFireBaseHelper == null)
            mFireBaseHelper = new FireBaseHelper();

        mFireBaseHelper.read("Tutorials", experiment_folder, new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Listen failed." + task.getException());

                    if(!mIsOnline)
                        notifyActivityAboutNoInternetConnection();
                        return;
                }
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {
                    Log.d(TAG, "No such document");
                    return;
                }
                List<String> basic_electronics = mFireBaseHelper.getArray(documentSnapshot);

                if(basic_electronics.contains(mExperimentType)) {
                    List<String> breadboard_urls = mFireBaseHelper.getArray(documentSnapshot, mExperimentType, "imageURL");
                    if(breadboard_urls == null)
                        return;

                    int firestore_images_version = mFireBaseHelper.getLong(documentSnapshot, mExperimentType, "version");

                    if(firestore_images_version <= 0 && !mIsOnline) {
                        notifyActivityAboutNoInternetConnection();
                        return;
                    }
                    boolean isNewVersion = false;
                    if( getLocaleImagesVersion() != firestore_images_version)  {
                        isNewVersion = true;
                        deleteOldImages();
                    }

                    if(isNewVersion || mIsLocFilesNotFound ) {
                            int count = 1;
                        notifyActivityAboutStartDownload();
                        mFilesNumber = breadboard_urls.size();
                        saveImagesVersion(mExperimentType, firestore_images_version);
                        saveFilesNumber(mExperimentType, breadboard_urls.size() );
                        for(String path: breadboard_urls) {
                            downloadUri(path, mExperimentType, count++);
                        }
                    }
                    else {
                        notifyActivityAboutNewFiles();
                    }
                }
            }
        });
    }

    private void deleteOldImages() {
        int number = getFilesNumber(mExperimentType);

        for(int i=0; i<number; i++) {
            String filePath = getFilePath(mExperimentType + (i+1));
            if(filePath == null)
                continue;

            File file = new File(filePath);
            if(file.exists()) {
                String fName = file.getName();
                boolean deleted = file.delete();
                if(deleted)
                    Log.d(TAG, "Delete: " + fName);
            }
        }
    }

    private boolean isLocalFilesNotFound() {
        int number = getFilesNumber(mExperimentType);

        for(int i=0; i<number; i++) {
            String filePath = getFilePath(mExperimentType + (i+1));
            if(filePath == null)
                return true;
            File file = new File(filePath);
            if(!file.exists())
                return true;
        }

        return false;
    }

    private void downloadUri(String path, final String electronic_type, final int num){
        Log.d(TAG,"Start download: " + path);
        final StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(path);
        final String fileName = mStorageRef.getName();
        File localFile = null;
        try {
            localFile = File.createTempFile(fileName, "");
//            localFile = File.createTempFile(fileName, "", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
            String abspath = localFile.getAbsolutePath();
            Log.d(TAG,"Start download: AbsPath: " + abspath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localFile != null) {
            final File finalLocalFile = localFile;
            mStorageRef.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    saveFileName(electronic_type + num, finalLocalFile.getAbsolutePath());

                    if(++mFilesCounter >= mFilesNumber) {
                        mFilesNumber = 0;
                        mFilesCounter = 0;
                        notifyActivityAboutNewFiles();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
                }
            });
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

    private int getLocaleImagesVersion() {
        return Utils.readSharedSetting(this, mExperimentType, 0);
    }

    private int getFilesNumber(String settings_key) {
        return Utils.readSharedSetting(this, settings_key + "_number", 0);
    }

    private String getFilePath(String settings_key){
        String fileName = Utils.readSharedSetting(this, settings_key, null);
        Log.d(TAG, "Get file key: " + settings_key + "; name: " + fileName);
        return fileName;
    }

    private void notifyActivityAboutNewFiles() {
        Log.d(TAG, "Notify activity about downloaded images " + mExperimentType);
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }

    private void notifyActivityAboutStartDownload() {
        if(!mIsOnline) {
            notifyActivityAboutNoInternetConnection();
            return;
        }
        if(mStartDownloadNotify)
            return;
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
        mStartDownloadNotify = true;
    }

    private void notifyActivityAboutNoInternetConnection() {
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }
}
