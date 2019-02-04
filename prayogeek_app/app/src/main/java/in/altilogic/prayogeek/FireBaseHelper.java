package in.altilogic.prayogeek;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import in.altilogic.prayogeek.activities.MainActivity;

import java.util.Map;

public class FireBaseHelper {
    private FirebaseFirestore mDb;

    public FireBaseHelper(){
        mDb = FirebaseFirestore.getInstance();
    }

    public void write(Global_Var gv) {
        mDb.collection("Usage_Info").document("Category")
                .set(gv.Get_Map())
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
}
