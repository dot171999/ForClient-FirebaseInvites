package in.altilogic.prayogeek.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
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

public class ImageDownloadService  extends IntentService {
    private final String TAG = "YOUSCOPE-DB-SERVICE";
    public static final String HW_SERVICE_BROADCAST_VALUE = "prayogeek.altilogic.in";
    public static final String HW_SERVICE_MESSAGE_TYPE_ID = "MESSAGE_TYPE_ID";
    public static final int HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES = 1;

    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE = "MESSAGE_TYPE_PATH_FIRESTORE";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE = "MESSAGE_TYPE_PATH_PHONE";

    private FireBaseHelper mFireBaseHelper;

    public ImageDownloadService() {
        super("ImageDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int message_type = intent.getIntExtra(HW_SERVICE_MESSAGE_TYPE_ID, -1);
        switch (message_type) {
            case HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES:
                String fireStorePath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE);
                String phonePath = intent.getStringExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE);
                Log.d(TAG, "Start download image from " + fireStorePath + " to " + phonePath);
                startDownloadImage(fireStorePath, "");
                break;
            default:
                break;
        }
    }

    private void startDownloadImage(final String base_electronis_type, String phonePath) {
        Log.d(TAG, "Start download " + base_electronis_type);
        if(mFireBaseHelper == null)
            mFireBaseHelper = new FireBaseHelper();

        mFireBaseHelper.read("Tutorials", "basic_electronics", new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                List<String> basic_electronics = mFireBaseHelper.getArray(documentSnapshot);

                if(basic_electronics.contains(base_electronis_type)) {
                    List<String> breadboard_urls = mFireBaseHelper.getArray(documentSnapshot, base_electronis_type, "imageURL");
                    if(breadboard_urls == null)
                        return;

                    int count = 1;
                    for(String path: breadboard_urls)
                        downloadUri(path, base_electronis_type, count++);

                }
            }
        });
    }

    private void downloadUri(String path, String filename, final int num){
        Log.d(TAG,"Start download: " + path);
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(path);

        File localFile = null;
        try {
            localFile = File.createTempFile(filename + num, "jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (localFile != null) {
            final File finalLocalFile = localFile;
            mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    finalLocalFile.getAbsolutePath();
                    Log.d(TAG, "Notify activity about downloaded images " + finalLocalFile.getName());
                    Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
                    intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES);
                    intentAnswer.putExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, 1);
                    sendBroadcast(intentAnswer);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }
            });
        }
    }
}
