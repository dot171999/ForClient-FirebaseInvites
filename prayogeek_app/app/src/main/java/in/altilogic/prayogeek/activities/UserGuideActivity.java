package in.altilogic.prayogeek.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

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

    private void showUserGuideFragment() {
        mScreenStatus = 1;
        ButtonsFragment buttonsFragment = new ButtonsFragment();
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, buttonsFragment).commit();
    }

    @Override
    public void onClick(View view) {

    }
}
