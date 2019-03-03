package in.altilogic.prayogeek.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ablanco.zoomy.DoubleTapListener;
import com.ablanco.zoomy.Zoomy;

import java.io.IOException;

import in.altilogic.prayogeek.Global_Var;
import in.altilogic.prayogeek.R;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static in.altilogic.prayogeek.utils.Utils.*;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {
    private TutorialFragment mTutorialFragment;
    private BasicElectronicFragment mBasicElectronicFragment;
    private FragmentManager mFragmentManager;
    private GifShowFragment mShowGifFragment;
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

    /**
     * TODO UPDATE pictures
     * To change screen pictures, you need to add these pictures to the DRAWABLE folder,
     * then add the ID of each image to the corresponding array below.
     */
    private final static int[] mBreadboardImages = {R.drawable.breadboard};
    private final static int[] mLedOnOffImages = {R.drawable.led_on_off};
    private final static int[] mPowerSupplyImages = {R.drawable.power_supply};
    private final static int[] mTransistorSwitchImages = {R.drawable.transistor_switch};
    private final static int[] mIC741Images = {R.drawable.ic741};
    private final static int[] mIC555Images = {R.drawable.ic555};
    private final static int[] mProject1Images = {R.drawable.project1};
    private final static int[] mProject2Images = {R.drawable.project2};
    private final static int[] mDemoProject1Images = {R.drawable.demo_project1};
    private final static int[] mDemoProject2Images = {R.drawable.demo_project1, R.drawable.demo_project2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_tutolial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE); // disable snapshots
        mFragmentManager = getSupportFragmentManager();

        int last_screen_id = readSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0);
        switch(last_screen_id){
            case 0: mScreenStatus = 0; showTutorialFragment(); break;
            case SCREEN_ID_BREADBOARD_USAGE: mScreenStatus = SCREEN_ID_BREADBOARD_USAGE; showGifFragment(mBreadboardImages); break;
            case SCREEN_ID_LED_ON_OFF: mScreenStatus = SCREEN_ID_LED_ON_OFF; showGifFragment(mLedOnOffImages); break;
            case SCREEN_ID_POWER_SUPPLY: mScreenStatus = SCREEN_ID_POWER_SUPPLY; showGifFragment(mPowerSupplyImages); break;
            case SCREEN_ID_TRANSISTOR_SWITCH: mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;  showGifFragment(mTransistorSwitchImages);  break;
            case SCREEN_ID_IC741: mScreenStatus = SCREEN_ID_IC741; showGifFragment(mIC741Images); break;
            case SCREEN_ID_IC555: mScreenStatus = SCREEN_ID_IC555; showGifFragment(mIC555Images); break;
            case SCREEN_ID_PROJECT1: mScreenStatus = SCREEN_ID_PROJECT1; showGifFragment(mProject1Images); break;
            case SCREEN_ID_PROJECT2: mScreenStatus = SCREEN_ID_PROJECT2; showGifFragment(mProject2Images); break;
            case SCREEN_ID_DEMO_PROJECT1: mScreenStatus = SCREEN_ID_DEMO_PROJECT1; showGifFragment(mDemoProject1Images); break;
            case SCREEN_ID_DEMO_PROJECT2: mScreenStatus = SCREEN_ID_DEMO_PROJECT2; showGifFragment(mDemoProject2Images); break;
            default: mScreenStatus = 0; showTutorialFragment(); break;
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
            case R.id.btnBasicElectronic: showBasicElectronic(); break;
            case R.id.btnProjects:
                mScreenStatus = 0;
                if(((Global_Var) getApplicationContext()).isProject_Access())
                    showProjectsFragment();
                else
                    Toast.makeText(getApplicationContext(), "Please Subscribe to access Projects", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnDemoProjects: mScreenStatus = 0;  showDemoProjectsFragment(); break;
            case R.id.btnBreadBoard: mScreenStatus = SCREEN_ID_BREADBOARD_USAGE; showGifFragment(mBreadboardImages); break;
            case R.id.btnLedOnOFF: mScreenStatus = SCREEN_ID_LED_ON_OFF;  showGifFragment(mLedOnOffImages);  break;
            case R.id.btnPowerSupply: mScreenStatus = SCREEN_ID_POWER_SUPPLY; showGifFragment(mPowerSupplyImages); break;
            case R.id.btnTransistorSwitch: mScreenStatus = SCREEN_ID_TRANSISTOR_SWITCH;  showGifFragment(mTransistorSwitchImages); break;
            case R.id.btnIC741: mScreenStatus = SCREEN_ID_IC741; showGifFragment(mIC741Images); break;
            case R.id.btnIC555: mScreenStatus = SCREEN_ID_IC555; showGifFragment(mIC555Images); break;
            case R.id.btnProject1: mScreenStatus = SCREEN_ID_PROJECT1; showGifFragment(mProject1Images); break;
            case R.id.btnProject2: mScreenStatus = SCREEN_ID_PROJECT2; showGifFragment(mProject2Images); break;
            case R.id.btnDemoProject1: mScreenStatus = SCREEN_ID_DEMO_PROJECT1;  showGifFragment(mDemoProject1Images); break;
            case R.id.btnDemoProject2:  mScreenStatus = SCREEN_ID_DEMO_PROJECT2; showGifFragment(mDemoProject2Images); break;
            case R.id.btnHome: showTutorialFragment(); break;
            case R.id.btnMinimize: saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, mScreenStatus); finishActivity(); break;
            case R.id.btnDone: saveSharedSetting(this, CURRENT_SCREEN_SETTINGS, 0); finishActivity(); break;
        }
    }

    private void showGifFragment(int[] drawable_id) {
        mShowGifFragment = GifShowFragment.newInstance(drawable_id);
        mShowGifFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, mShowGifFragment)
                .commit();
    }

    private void showBasicElectronic(){
        mBasicElectronicFragment = new BasicElectronicFragment();
        mBasicElectronicFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, mBasicElectronicFragment)
                .commit();
    }

    private void showTutorialFragment() {
        mTutorialFragment = new TutorialFragment();
        mTutorialFragment.setOnClickListener(this, ((Global_Var) getApplicationContext()).isProject_Access());
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, mTutorialFragment)
                .commit();
    }

    private void showProjectsFragment() {
        mProjectsFragment = new ShowProjectsFragment();
        mProjectsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, mProjectsFragment)
                .commit();
    }

    private void showDemoProjectsFragment() {
        mDemoProjectsFragment = new ShowDemoProjectsFragment();
        mDemoProjectsFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, mDemoProjectsFragment)
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
            btnProjects.setOnClickListener(this);
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

    public static class GifShowFragment extends Fragment implements View.OnClickListener {
        private GifImageView mGif;
        private GifDrawable mGifDrawable;
        private int[] mDrawableId;
        private int mDrawCount = 0;

        public static GifShowFragment newInstance(int[] drawable_id) {
            GifShowFragment myFragment = new GifShowFragment();

            Bundle args = new Bundle();
            args.putIntArray("show-gif-id", drawable_id);
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
            mGif = view.findViewById(R.id.gif_content);
            mDrawableId = getArguments().getIntArray("show-gif-id");
            if(mDrawableId == null || mDrawableId.length == 0)
                return;

            try {
                mGifDrawable = new GifDrawable(getResources(), mDrawableId[mDrawCount]);
                if(++mDrawCount >= mDrawableId.length )
                    mDrawCount = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            mGif.setImageDrawable(mGifDrawable);
            Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(mGif).doubleTapListener(new DoubleTapListener() {
                @Override
                public void onDoubleTap(View v) {
                    try {
                        mGifDrawable = new GifDrawable(getResources(), mDrawableId[mDrawCount]);
                        mGif.setImageDrawable(mGifDrawable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(++mDrawCount >= mDrawableId.length )
                        mDrawCount = 0;
                }
            });
            builder.register();
        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
                mListener.onClick(view);
        }

        @Override
        public void onStop(){
            super.onStop();
            Zoomy.unregister(mGif);
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
