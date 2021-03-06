package in.altilogic.prayogeek;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import in.altilogic.prayogeek.activities.MainActivity;
import in.altilogic.prayogeek.utils.Utils;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class FireBaseHelper {
    private final static int MAX_DOWNLOAD_INTERVAL_MS = 2000;
    private final static String GLOBAL_VARIABLE_ID = "GLOBAL_VARIABLE_ID";
    private final static String GLOBAL_VARIABLE_COLLECTION = "Usage_Info";
    private FirebaseFirestore mDb;

    public FireBaseHelper(){
        mDb = FirebaseFirestore.getInstance();
        FirebaseStorage.getInstance().setMaxDownloadRetryTimeMillis(MAX_DOWNLOAD_INTERVAL_MS);
    }


    ///////
    public void pointsChanges(TextView txtPoints){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference docRef = mDb.collection("UserWithUidAndPoints").document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@com.google.firebase.database.annotations.Nullable DocumentSnapshot snapshot,
                                @com.google.firebase.database.annotations.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("pts", "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Log.e("pts", "Current data: " + snapshot.getData());
                    txtPoints.setText("Points: "+snapshot.get("Points").toString());
                } else {
                    Log.e("pts", "Current data: null");
                }
            }
        });
    }

    public void writeInvitedBy(String referrerUid){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> data = new HashMap<>();
        data.put("InvitedBy",referrerUid);
        data.put("Invited",1);
        mDb .collection("UserWithUidAndPoints")
                .document(user.getUid()).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("Added InvitedBy","Success");
            }
        });
    }

    public void userPoints(){
        Log.e("Points","creating");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDb.collection("UserWithUidAndPoints")
                .document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d("DATAXXX", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("NO DATAXXX", "No such document");
                        createFields(user);
                    }
                } else {
                }
            }
        });
    }

    public void createFields(FirebaseUser user){
        Log.e("CheckPoints XXX","not exist");
        Map<String, Object> data = new HashMap<>();
        data.put("TotalRefers",0);
        data.put("Invited",-1);
        data.put("Points", 0);
        data.put("User", user.getDisplayName());

        mDb .collection("UserWithUidAndPoints")
                .document(user.getUid()).set(data,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("Pts field created in DB","Success");
            }
        });
    }
///////


    public void write(final Global_Var gv) {
        mDb .collection(GLOBAL_VARIABLE_COLLECTION)
                .add(gv.Get_Map())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>()  {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(MainActivity.TAG,  "DocumentSnapshot written with ID: " + documentReference.getId());
                        Utils.saveSharedSetting(gv.getApplicationContext(), GLOBAL_VARIABLE_ID, documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(MainActivity.TAG, "Error writing document", e);
                    }
                });

    }

    public void write(String collection, String document, Map<String, Object> data) {
        mDb.collection(collection).document(document)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(MainActivity.TAG, "Error writing document", e);
                    }
                });
    }

    public void read(String collection, String document, EventListener<DocumentSnapshot> eventListener) {
        mDb.collection(collection).document(document).addSnapshotListener(eventListener);
    }

    public void read(String collection, String document, OnCompleteListener<DocumentSnapshot> completeListener) {
        mDb.collection(collection).document(document).get().addOnCompleteListener(completeListener);
    }

//    public void read(String collection, String document, OnSuccessListener<DocumentSnapshot> completeListener) {
//        mDb.collection(collection).document(document).get().addOnSuccessListener(completeListener);
//    }

    public void read(String collection, String document, OnCompleteListener<DocumentSnapshot> completeListener, final OnFailureListener failListener) {
        mDb.collection(collection).document(document).get().addOnCompleteListener(completeListener).addOnFailureListener(failListener);
    }

    /**
     * Reading the global variables
     * @param context
     * @param completeListener
     */
    public void read(Context context, OnCompleteListener<DocumentSnapshot> completeListener){
        String documentId = Utils.readSharedSetting(context, GLOBAL_VARIABLE_ID, null);
        if(documentId != null) {
            mDb.collection(GLOBAL_VARIABLE_COLLECTION).document(documentId).get().addOnCompleteListener(completeListener);
        }
        else{
            Log.d(MainActivity.TAG, "Global_Vars were not saved");
        }
    }

    public List<String> getArray(@Nullable DocumentSnapshot documentSnapshot, String folder, String array) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            Map<String, Object> dataMap = (Map<String, Object>) documentSnapshot.getData();

            if(dataMap == null)
                return null;

            if(dataMap.containsKey(folder)) {
                Map<String, Object> breadboard = (Map<String, Object>) dataMap.get(folder);

                if(breadboard == null)
                    return null;

                return (List<String>) breadboard.get(array);
            }

        }
        return null;
    }

    public int getLong(@Nullable DocumentSnapshot documentSnapshot, String folder, String field) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            Map<String, Object> dataMap = (Map<String, Object>) documentSnapshot.getData();

            if(dataMap == null)
                return -1;

            if(dataMap.containsKey(folder)) {
                Map<String, Object> breadboard = (Map<String, Object>) dataMap.get(folder);

                if(breadboard == null)
                    return -1;

                Long value = (Long) breadboard.get(field);
                if(value == null)
                    return -1;

                return value.intValue();
            }

        }
        return -1;
    }

    public List<String> getArray(@Nullable DocumentSnapshot documentSnapshot) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            Map<String, Object> dataMap = (Map<String, Object>) documentSnapshot.getData();

            if(dataMap == null)
                return null;

            return new ArrayList<>(dataMap.keySet());
        }
        return null;
    }
}
