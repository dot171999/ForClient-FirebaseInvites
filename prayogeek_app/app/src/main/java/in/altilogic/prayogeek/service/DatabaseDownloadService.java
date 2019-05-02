package in.altilogic.prayogeek.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.RemoteButtonScreen;
import in.altilogic.prayogeek.utils.Utils;

public class DatabaseDownloadService extends IntentService {
    private final String TAG = "YOUSCOPE-DB-SERVICE";
    public static final String HW_SERVICE_BROADCAST_VALUE = "prayogeek.altilogic.in";
    public static final String HW_SERVICE_MESSAGE_TYPE_ID = "MESSAGE_TYPE_ID";
    public static final int HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES = 1;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE = 2;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD = 3;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET = 4;
    public static final int HW_SERVICE_MESSAGE_TYPE_IMAGE_DOWNLOAD_FAIL = 5;
    public static final int HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN = 6;

    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_COLLECTION = "MESSAGE_TYPE_COLLECTION";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_DOCUMENT = "MESSAGE_TYPE_DOCUMENT";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT = "MESSAGE_TYPE_EXPERIMENT";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE = "MESSAGE_TYPE_PATH_FIRESTORE";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_SCREEN = "MESSAGE_TYPE_REMOTE_SCREEN";

    private static final int MAX_DOWNLOADING_IMG_FILE_SIZE = (1024*1024); // 1MB

    private FireBaseHelper mFireBaseHelper;

    private int mFilesNumber = 0;
    private int mFilesCounter = 0;
    private boolean mIsOnline;
    private boolean mStartDownloadNotify;
    public DatabaseDownloadService() {
        super("DatabaseDownloadService");
    }
    private Thread mDownloadImagesThread;
    private Thread mDownloadScreensThread;
//    private String mExperimentType;
    private boolean mIsLocFilesNotFound;

    private File createFile(String fileName) throws IOException {
        return  new File(getFilesDir(), fileName);
//        return  File.createTempFile(fileName, ""); // TODO Default app folder
//        return File.createTempFile(fileName, "",
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)); // TODO uncomment for writing image files to the documents folder
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int message_type = intent.getIntExtra(HW_SERVICE_MESSAGE_TYPE_ID, -1);
        switch (message_type) {
            case HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES: {
                String collection = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_COLLECTION);
                String document = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT);
                String field = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE);
                Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES: " + document + "/" + field);
                startDownloadImage(collection, document, field);
                break;
            }
            case HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN: {
                final String document = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_DOCUMENT);
                Log.d(TAG, "HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN: " + document);
                startDownloadScreen(document);
                break;
            }
            default:
                break;
        }
    }

    private void startDownloadImage(final String collection, final String document, final String field) {
        Log.d(TAG, "Start download Images "+ document +"/"+ field);

        mFilesNumber = 0;
        mFilesCounter = 0;
        mIsOnline = Utils.isOnline(this);
        mStartDownloadNotify = false;
        mIsLocFilesNotFound = isLocalFilesNotFound(field);
        if(getLocaleImagesVersion(field) <= 0 || mIsLocFilesNotFound)
            notifyActivityAboutStartDownload();


        if(mDownloadImagesThread != null){
            try {
                mDownloadImagesThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDownloadImagesThread = null;
        }

        mDownloadImagesThread = new Thread(() -> {
            if(mFireBaseHelper == null)
                mFireBaseHelper = new FireBaseHelper();

            mFireBaseHelper.read(collection, document, task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Listen failed." + task.getException());

                    if (!mIsOnline)
                        notifyActivityAboutNoInternetConnection();
                    return;
                }
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && !documentSnapshot.exists()) {
                    Log.d(TAG, "No such document");
                    return;
                }
                List<String> basic_electronics = mFireBaseHelper.getArray(documentSnapshot);

                if (basic_electronics.contains(field)) {
                    List<String> imagesUrls = mFireBaseHelper.getArray(documentSnapshot, field, "imageURL");
                    if (imagesUrls == null)
                        return;

                    int firestore_images_version = mFireBaseHelper.getLong(documentSnapshot, field, "version");

                    if (firestore_images_version <= 0 && !mIsOnline) {
                        notifyActivityAboutNoInternetConnection();
                        return;
                    }
                    boolean isNewVersion = false;
                    if (getLocaleImagesVersion(field) != firestore_images_version) {
                        isNewVersion = true;
                        deleteOldImages(field);
                    }

                    if (isNewVersion || mIsLocFilesNotFound) {
                        int count = 1;
                        notifyActivityAboutStartDownload();
                        mFilesNumber = imagesUrls.size();
                        saveImagesVersion(field, firestore_images_version);
                        saveFilesNumber(field, imagesUrls.size());
                        for (String path : imagesUrls) {
                            downloadUri(path, field, count++);
                        }
                    } else {
                        notifyActivityAboutNewFiles(field);
                    }
                }
                else {
                    notifyActivityAboutDownloadFail();
                }
            }, e -> notifyActivityAboutDownloadFail());
        });

        mDownloadImagesThread.start();
    }

    private void startDownloadScreen(final String document) {
        Log.d(TAG, "Start download screen"+ document);

        if( mDownloadScreensThread != null) {
            try {
                mDownloadScreensThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDownloadScreensThread = null;
        }
        mDownloadScreensThread = new Thread(() -> {

            if(mFireBaseHelper == null)
                mFireBaseHelper = new FireBaseHelper();

            mFireBaseHelper.read("Screens", document, task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Listen failed.");
                    notifyActivityAboutDownloadFail();
                    return;
                }
                DocumentSnapshot documentSnapshot = task.getResult();

                if (documentSnapshot == null) {
                    Log.w(TAG, "Listen failed.");
                    notifyActivityAboutDownloadFail();
                    return;
                }

                List<String> screen_parameters = mFireBaseHelper.getArray(documentSnapshot);

                String screenType=null;
                if(screen_parameters.contains("type")) {
                    screenType = (String) documentSnapshot.get("type");
                }

                long version = 1;
                if(screen_parameters.contains("version")){
                    version = (Long) documentSnapshot.get("version");
                }

                if(screenType == null) {
                    notifyActivityAboutDownloadFail();
                    return;
                }

                if(screenType.equals("buttons")) {
                    RemoteButtonScreen screen = null;
                    if(screen_parameters.contains("names")){
                        List<String> mNames = (List<String>) documentSnapshot.get("names");
                        if(mNames == null){
                            notifyActivityAboutDownloadFail();
                            return;
                        }

                        screen = new RemoteButtonScreen(document, mNames);

                        for(String buttName : mNames) {
                            if(screen_parameters.contains(Utils.checkSlashSymbols(buttName))){
                                Map<String, Object> dataMap = (Map<String, Object>) documentSnapshot.get(Utils.checkSlashSymbols(buttName));
                                screen.getRemoteButton(buttName).setParameters(dataMap);
                            }
                        }
                    }
                    if(screen == null) {
                        notifyActivityAboutDownloadFail();
                        return;
                    }

                    screen.setVersion(version + "");

                    RemoteButtonScreen savedScreen = Utils.loadScreen(this, document);
                    if(savedScreen == null || !savedScreen.getVersion().equals(screen.getVersion())) {
                        Utils.saveScreen(this, document, screen);
                    }

                    if (documentSnapshot.exists()) {
                        notifyActivityAboutDownloadScreen(screen);
                    }
                    else {
                        notifyActivityAboutDownloadFail();
                    }
                }
                else if(screenType.equals("picture")){
                    if(screen_parameters.contains("collection")){
                        String collection = (String) documentSnapshot.get("collection");
                        if(collection != null && screen_parameters.contains("document")){
                            String doc = (String) documentSnapshot.get("document");
                            if(doc != null && screen_parameters.contains("field")){
                                String field = (String) documentSnapshot.get("field");

//                                notifyActivityAboutNewFiles(field);
                                startDownloadImage(collection, doc, field);
//                            mScreenDocument = doc;
//                            mExperimentType = fld;
//
//                            showGifFragment(collection, doc, fld);
                            }
                        }
                    }
                }
            }, task2 -> notifyActivityAboutDownloadFail());
        });

        mDownloadScreensThread.start();
    }

    private void deleteOldImages(String field) {
        int number = getFilesNumber(field);

        for(int i=0; i<number; i++) {
            String filePath = getFilePath(field + (i+1));
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

    private boolean isLocalFilesNotFound(String field) {
        int number = getFilesNumber(field);

        for(int i=0; i<number; i++) {
            String filePath = getFilePath(field + (i+1));
            if(filePath == null)
                return true;
            File file = new File(filePath);
            if(!file.exists())
                return true;
        }

        return false;
    }

    private void downloadUri(String path, final String field, final int num){
        if(path == null || path.length() == 0) {
            notifyActivityAboutDownloadFail();
            return;
        }
        Log.d(TAG,"Start download: " + path);
        final StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(path);
        final String fileName = mStorageRef.getName();
        File localFile = null;
        try {
            localFile = createFile(fileName);

            String abspath = localFile.getAbsolutePath();
            Log.d(TAG,"Start download: AbsPath: " + abspath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localFile != null) {
            final File finalLocalFile = localFile;
            mStorageRef.getBytes(MAX_DOWNLOADING_IMG_FILE_SIZE).addOnCompleteListener(task -> {
                BufferedOutputStream bos;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(finalLocalFile));

                    byte[] encBytes = Base64.encode(task.getResult(), Base64.NO_WRAP);
                    bos.write(encBytes);
                    bos.flush();
                    bos.close();

                    saveFileName(field + num, finalLocalFile.getAbsolutePath());

                    if(++mFilesCounter >= mFilesNumber) {
                        mFilesNumber = 0;
                        mFilesCounter = 0;
                        notifyActivityAboutNewFiles(field);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Exception: " + e.toString());
                    notifyActivityAboutDownloadFail();
                }
            })

            .addOnFailureListener(e -> {
            Log.d(TAG, "Exception: " + e.toString());
            notifyActivityAboutDownloadFail();
            })
            .addOnCanceledListener(() -> {
                Log.d(TAG, "Download canceled");
                notifyActivityAboutDownloadFail();
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

    private int getLocaleImagesVersion(String field) {
        return Utils.readSharedSetting(this, field, 0);
    }

    private int getFilesNumber(String settings_key) {
        return Utils.readSharedSetting(this, settings_key + "_number", 0);
    }

    private String getFilePath(String settings_key){
        String fileName = Utils.readSharedSetting(this, settings_key, null);
        Log.d(TAG, "Get file key: " + settings_key + "; name: " + fileName);
        return fileName;
    }

    private void notifyActivityAboutNewFiles(String field) {
        Log.d(TAG, "Notify activity about downloaded images " + field);
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES_COMPLETE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT, field);
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

    private void notifyActivityAboutDownloadFail() {
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_IMAGE_DOWNLOAD_FAIL);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }

    private void notifyActivityAboutDownloadScreen(RemoteButtonScreen screen) {
        Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_SCREEN);
        intentAnswer.putExtra(HW_SERVICE_MESSAGE_DOWNLOAD_SCREEN, screen);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAnswer);
    }
}
