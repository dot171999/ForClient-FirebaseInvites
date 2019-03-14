package in.altilogic.prayogeek.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class ImageDownloadService  extends IntentService {
    private final String TAG = "YOUSCOPE-DB-SERVICE";
    public static final String HW_SERVICE_BROADCAST_VALUE = "prayogeek.altilogic.in";
    public static final String HW_SERVICE_MESSAGE_TYPE_ID = "MESSAGE_TYPE_ID";
    public static final int HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES = 1;

    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE = "MESSAGE_TYPE_PATH_FIRESTORE";
    public static final String HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE = "MESSAGE_TYPE_PATH_PHONE";

    private StorageReference mStorageRef;

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
                download(fireStorePath);
                break;
            default:
                break;
        }
    }

    private void download(String path){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = mStorageRef.child(path);

        pathReference.getBytes(1024*1024*20).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "Notify activity about downloaded images");
                Intent intentAnswer = new Intent(HW_SERVICE_BROADCAST_VALUE);
                intentAnswer.putExtra(HW_SERVICE_MESSAGE_TYPE_ID, HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES);
                intentAnswer.putExtra(HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, 1);
                sendBroadcast(intentAnswer);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Download fail");
            }
        });
    }
}
