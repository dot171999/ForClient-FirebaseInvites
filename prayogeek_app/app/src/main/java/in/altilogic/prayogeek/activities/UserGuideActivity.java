package in.altilogic.prayogeek.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.RemoteButtonScreen;
import in.altilogic.prayogeek.fragments.ButtonsFragment;

public class UserGuideActivity extends AppCompatActivity implements View.OnClickListener  {
    public static final String TAG = "YOUSCOPE-USER-GUIDE";

    private FragmentManager mFragmentManager;
    private FireBaseHelper mFireBaseHelper;

    private RemoteButtonScreen mUserGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mFragmentManager = getSupportFragmentManager();
        mFireBaseHelper = new FireBaseHelper();
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
                mUserGuide = new RemoteButtonScreen(mNames);
            }
            if(mUserGuide == null)
                return;

            if(screen_parameters.contains("version")){
                String version = (String) documentSnapshot.get("version");
                mUserGuide.setVersion(version);
            }
            if(screen_parameters.contains("orientation")){
                String orientation = (String) documentSnapshot.get("orientation");
                mUserGuide.setOrientation(orientation);
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                runOnUiThread(() -> showUserGuideFragment());
            }
        })).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
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
        RemoteButtonScreen.RemoteButton clickedButton = mUserGuide.getButton(id);
        if(clickedButton != null) {
            Log.d(TAG, "onClick() press button " + clickedButton.getName() + "; Start open screen " + clickedButton.getLinkName());
        }
    }
}
