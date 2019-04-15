package in.altilogic.prayogeek.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.ButtonsFragment;

public class UserGuideActivity extends AppCompatActivity implements View.OnClickListener  {
    public static final String TAG = "YOUSCOPE-USER-GUIDE";

    private FragmentManager mFragmentManager;
    private int mScreenStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mFragmentManager = getSupportFragmentManager();
        showUserGuideFragment();
        Log.d(TAG, "onCreate()");
    }

    final static String[] mButtons = {"HELP", "GUIDE", "ABOUT"};

    private void showUserGuideFragment() {
        mScreenStatus = 1;
        ArrayList<String> mArray = new ArrayList<>(Arrays.asList(mButtons));
        ButtonsFragment buttonsFragment = ButtonsFragment.newInstance(mArray);
        buttonsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, buttonsFragment).commit();
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
