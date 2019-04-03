package in.altilogic.prayogeek.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.SerialConsoleSettingsFragment;


public class SerialSettingsActivity extends AppCompatActivity {
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutolial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mFragmentManager = getSupportFragmentManager();
        SerialConsoleSettingsFragment serialConsoleFragment = new SerialConsoleSettingsFragment();
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, serialConsoleFragment).commit();
    }
}
