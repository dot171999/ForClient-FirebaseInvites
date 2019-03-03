package in.altilogic.prayogeek.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import pl.droidsonroids.gif.GifImageView;

import static in.altilogic.prayogeek.utils.Utils.*;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {
    private TutorialFragment mTutorialFragment;
    private BasicElectronicFragment mBasicElectronicFragment;
    private FragmentManager mFragmentManager;
    private ShowGifFragment mShowGifFragment;
    private ShowProjectsFragment mProjectsFragment;
    private ShowDemoProjectsFragment mDemoProjectsFragment;

    private final static String CURRENT_SCREEN_SETTINGS = "TUTORIAL-SETTINGS-CURRENT-SCREEN";
    private final static int SCREEN_ID_BREADBOARD_USAGE = 1;
    private final static int SCREEN_ID_LED_ON_OFF = 2;
    private final static int SCREEN_ID_POWER_SUPPLY = 3;
    private final static int SCREEN_ID_TRANSISTOR_SWITCH = 4;
    private final static int SCREEN_ID_IC741 = 5;
    private final static int SCREEN_ID_IC555 = 6;
    private final static int SCREEN_ID_PROJECT1 = 7;
    private final static int SCREEN_ID_PROJECT2 = 8;
    private final static int SCREEN_ID_DEMO_PROJECT1 = 9;
    private final static int SCREEN_ID_DEMO_PROJECT2 = 10;
    private int mScreenStatus = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_button2);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mFragmentManager = getSupportFragmentManager();

        int last_screen_id = readSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
        switch(last_screen_id){
            case 0:
                mScreenStatus = 0;
                showTutorialFragment();
                break;
            case SCREEN_ID_BREADBOARD_USAGE:
                mScreenStatus = SCREEN_ID_BREADBOARD_USAGE;
                showGifFragment(R.drawable.breadboard);
                break;
            case SCREEN_ID_LED_ON_OFF:
                mScreenStatus = SCREEN_ID_LED_ON_OFF;
                showGifFragment(R.drawable.led_on_off);
                break;
            case SCREEN_ID_POWER_SUPPLY:
                mScreenStatus = SCREEN_ID_POWER_SUPPLY;
                showGifFragment(R.drawable.power_supply);
                break;
            case SCREEN_ID_TRANSISTOR_SWITCH:
                mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;
                showGifFragment(R.drawable.transistor_switch);
                break;
            case SCREEN_ID_IC741:
                mScreenStatus = SCREEN_ID_IC741;
                showGifFragment(R.drawable.ic741);
                break;
            case SCREEN_ID_IC555:
                mScreenStatus = SCREEN_ID_IC555;
                showGifFragment(R.drawable.ic555);
                break;
            case SCREEN_ID_PROJECT1:
                mScreenStatus = SCREEN_ID_PROJECT1;
                showGifFragment(R.drawable.project1);
                break;
            case SCREEN_ID_PROJECT2:
                mScreenStatus = SCREEN_ID_PROJECT2;
                showGifFragment(R.drawable.project2);
                break;
            case SCREEN_ID_DEMO_PROJECT1:
                mScreenStatus = SCREEN_ID_DEMO_PROJECT1;
                showGifFragment(R.drawable.demo_project1);
                break;
            case SCREEN_ID_DEMO_PROJECT2:
                mScreenStatus = SCREEN_ID_DEMO_PROJECT2;
                showGifFragment(R.drawable.demo_project2);
                break;
            default:
                mScreenStatus = 0;
                showTutorialFragment();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity(){
        if (getParent() == null) {
            setResult(RESULT_OK, new Intent());
        }
        else {
            getParent().setResult(RESULT_OK, new Intent());
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnBasicElectronic:
                Toast.makeText(getApplicationContext(), "BasicElectronic", Toast.LENGTH_SHORT).show();
                mBasicElectronicFragment = new BasicElectronicFragment();
                mBasicElectronicFragment.setOnClickListener(this);
                mFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContent, mBasicElectronicFragment)
                        .commit();
                break;
            case R.id.btnProjects:
                Toast.makeText(getApplicationContext(), "Projects", Toast.LENGTH_SHORT).show();
                mScreenStatus = 0;
                showProjectsFragment();
                break;
            case R.id.btnDemoProjects:
                Toast.makeText(getApplicationContext(), "DemoProjects", Toast.LENGTH_SHORT).show();
                mScreenStatus = 0;
                showDemoProjectsFragment();
                break;
            case R.id.btnBreadBoard:
                Toast.makeText(getApplicationContext(), "Breadbroad", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_BREADBOARD_USAGE;
                showGifFragment(R.drawable.breadboard);
                break;
            case R.id.btnLedOnOFF:
                Toast.makeText(getApplicationContext(), "LED ON/OFF", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_LED_ON_OFF;
                showGifFragment(R.drawable.led_on_off);
                break;
            case R.id.btnPowerSupply:
                Toast.makeText(getApplicationContext(), "Power supply", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_POWER_SUPPLY;
                showGifFragment(R.drawable.power_supply);
                break;
            case R.id.btnTransistorSwitch:
                Toast.makeText(getApplicationContext(), "Transistor switch", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;
                showGifFragment(R.drawable.transistor_switch);
                break;
            case R.id.btnIC741:
                Toast.makeText(getApplicationContext(), "IC741", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_IC741;
                showGifFragment(R.drawable.ic741);
                break;
            case R.id.btnIC555:
                Toast.makeText(getApplicationContext(), "IC555", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_IC555;
                showGifFragment(R.drawable.ic555);
                break;
            case R.id.btnProject1:
                Toast.makeText(getApplicationContext(), "Project 1", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_PROJECT1;
                showGifFragment(R.drawable.project1);
                break;
            case R.id.btnProject2:
                Toast.makeText(getApplicationContext(), "Project 2", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_PROJECT2;
                showGifFragment(R.drawable.project2);
                break;
            case R.id.btnDemoProject1:
                Toast.makeText(getApplicationContext(), "Demo project 1", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_DEMO_PROJECT1;
                showGifFragment(R.drawable.demo_project1);
                break;
            case R.id.btnDemoProject2:
                Toast.makeText(getApplicationContext(), "Demo project 2", Toast.LENGTH_SHORT).show();
                mScreenStatus = SCREEN_ID_DEMO_PROJECT2;
                showGifFragment(R.drawable.demo_project2);
                break;
            case R.id.btnHome:
                Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                showTutorialFragment();
                break;
            case R.id.btnMinimize:
                Toast.makeText(getApplicationContext(), "Minimize", Toast.LENGTH_SHORT).show();
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, mScreenStatus);
                finishActivity();
                break;
            case R.id.btnDone:
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
                finishActivity();
                break;
        }
    }

    private void showGifFragment(int drawable_id) {
        mShowGifFragment = ShowGifFragment.newInstance(drawable_id);
        mShowGifFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, mShowGifFragment)
                .commit();
    }

    private void showTutorialFragment() {
        mTutorialFragment = new TutorialFragment();
        mTutorialFragment.setOnClickListener(this, ((Global_Var) getApplicationContext()).isProject_Access());
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mTutorialFragment)
                .commit();
    }

    private void showProjectsFragment() {
        mProjectsFragment = new ShowProjectsFragment();
        mProjectsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mProjectsFragment)
                .commit();
    }

    private void showDemoProjectsFragment() {
        mDemoProjectsFragment = new ShowDemoProjectsFragment();
        mDemoProjectsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mDemoProjectsFragment)
                .commit();
    }

    public static class TutorialFragment extends Fragment implements View.OnClickListener {

        private View.OnClickListener mListener;
        private boolean mProjectAccess= false;

        public void setOnClickListener(View.OnClickListener listener, boolean project_access){
            mListener = listener;
            mProjectAccess = project_access;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tutorial, null);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Button btnBasicElectronic = (Button) view.findViewById(R.id.btnBasicElectronic);
            Button btnProjects = (Button) view.findViewById(R.id.btnProjects);
            Button btnDemo = (Button) view.findViewById(R.id.btnDemoProjects);
            btnBasicElectronic.setOnClickListener(this);

            if(mProjectAccess)
                btnProjects.setOnClickListener(this);
            else
                btnProjects.setClickable(false);

            btnDemo.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onClick(view);
        }
    }

    public static class BasicElectronicFragment extends Fragment implements View.OnClickListener {

        private View.OnClickListener mListener;

        public void setOnClickListener(View.OnClickListener listener){
            mListener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_basic_electronic, null);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Button btnBreadBoard = (Button) view.findViewById(R.id.btnBreadBoard);
            Button btnLedOnOFF = (Button) view.findViewById(R.id.btnLedOnOFF);
            Button btnPowerSupply = (Button) view.findViewById(R.id.btnPowerSupply);
            Button btnTransistorSwitch = (Button) view.findViewById(R.id.btnTransistorSwitch);
            Button btnIC741 = (Button) view.findViewById(R.id.btnIC741);
            Button btnIC555 = (Button) view.findViewById(R.id.btnIC555);
            btnBreadBoard.setOnClickListener(this);
            btnLedOnOFF.setOnClickListener(this);
            btnPowerSupply.setOnClickListener(this);
            btnTransistorSwitch.setOnClickListener(this);
            btnIC741.setOnClickListener(this);
            btnIC555.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onClick(view);
        }
    }

    public static class ShowGifFragment extends Fragment implements View.OnClickListener {
        public static ShowGifFragment newInstance(int drawable_id) {
            ShowGifFragment myFragment = new ShowGifFragment();

            Bundle args = new Bundle();
            args.putInt("show-gif-id", drawable_id);
            myFragment.setArguments(args);

            return myFragment;
        }

        private View.OnClickListener mListener;

        public void setOnClickListener(View.OnClickListener listener){
            mListener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_show_gif, null);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Button btnHome = (Button) view.findViewById(R.id.btnHome);
            Button btnMinimize = (Button) view.findViewById(R.id.btnMinimize);
            Button btnDone = (Button) view.findViewById(R.id.btnDone);
            btnHome.setOnClickListener(this);
            btnMinimize.setOnClickListener(this);
            btnDone.setOnClickListener(this);
            GifImageView gif = view.findViewById(R.id.gif_content);
            gif.setImageResource(getArguments().getInt("show-gif-id"));
        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onClick(view);
        }
    }

    public static class ShowProjectsFragment extends Fragment implements View.OnClickListener {

        private View.OnClickListener mListener;

        public void setOnClickListener(View.OnClickListener listener){
            mListener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_projects, null);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Button btnProject1 = (Button) view.findViewById(R.id.btnProject1);
            Button btnProject2 = (Button) view.findViewById(R.id.btnProject2);
            btnProject1.setOnClickListener(this);
            btnProject2.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onClick(view);
        }
    }

    public static class ShowDemoProjectsFragment extends Fragment implements View.OnClickListener {

        private View.OnClickListener mListener;

        public void setOnClickListener(View.OnClickListener listener){
            mListener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_demoprojects, null);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Button btnDemoProject1 = (Button) view.findViewById(R.id.btnDemoProject1);
            Button btnDemoProject2 = (Button) view.findViewById(R.id.btnDemoProject2);
            btnDemoProject1.setOnClickListener(this);
            btnDemoProject2.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onClick(view);
        }
    }

}
