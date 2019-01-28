package in.altilogic.prayogeek;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import in.altilogic.prayogeek.activities.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class FireBaseHelper {
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_EMAILID = "key_emailid";
    private static final String KEY_COLLEGE_NAME = "key_college_name";
    private static final String Key_SEMESTER = "key_semester";
    private static final String KEY_CONNECTION_STATUS = "key_connection_status";
    private static final String KEY_LOCATION = "key_location";
    private static final String KEY_MODULE_NAME = "key_module_name";
    private static final String KEY_TIMESTAMP = "key_timestamp";

    private DatabaseReference mDatabase;

    public FireBaseHelper(DatabaseReference databaseReference) {
//        mDatabase = databaseReference;
        String main_key = databaseReference.getKey();
        FirebaseDatabase firebaseDatabase = databaseReference.getDatabase();
        DatabaseReference databaseReference1 = firebaseDatabase.getReference("Colleges");
        String key = databaseReference1.getKey();
        int i =10;
    }


    public void WriteGlobalVar(Global_Var global_var) {
//        mDatabase.child("Usage_Info").child("Category").child(KEY_USERNAME).setValue(global_var.Get_Username());
//        mDatabase.child("Usage_Info").child("Category").child(KEY_EMAILID).setValue(global_var.Get_EmailId());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put(KEY_USERNAME, global_var.Get_Username());
        user.put(KEY_EMAILID, global_var.Get_EmailId());

// Add a new document with a generated ID
        db.collection("Usage_Info")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(MainActivity.TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(MainActivity.TAG, "Error adding document", e);
                    }
                });
    }

}
