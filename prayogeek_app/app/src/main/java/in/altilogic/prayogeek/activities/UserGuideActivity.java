package in.altilogic.prayogeek.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.FireBaseHelper;
import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.ButtonsFragment;

public class UserGuideActivity extends AppCompatActivity implements View.OnClickListener  {
    public static final String TAG = "YOUSCOPE-USER-GUIDE";

    private FragmentManager mFragmentManager;
    private FireBaseHelper mFireBaseHelper;
    private int mScreenStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mFragmentManager = getSupportFragmentManager();
        mFireBaseHelper = new FireBaseHelper();
        Log.d(TAG, "onCreate()");
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mFireBaseHelper.read("Screen_UserGuide", "Buttons", (documentSnapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    List<String> names = (List<String>) documentSnapshot.get("names");
                    runOnUiThread(() -> showUserGuideFragment(names));
                }
            });
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    private void showUserGuideFragment(List<String> _names) {
        mScreenStatus = 1;
        ButtonsFragment buttonsFragment = ButtonsFragment.newInstance(new ArrayList<>(_names));
        buttonsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, buttonsFragment)
                .commit();
    }

    @Override
    public void onClick(View view) {
        if(view == null)
            return;
        int id = view.getId();
        Log.d(TAG, "onClick() press button " + id);

        switch (id){
            case 1:
            case 2:
            case 3:

            default:
                break;
        }

    }
}
