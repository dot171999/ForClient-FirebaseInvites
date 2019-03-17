package in.altilogic.prayogeek.activities;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.SectionsPagerAdapter;
import in.altilogic.prayogeek.utils.Utils;

/**
 * TODO for updating onboarding images you need use "onboard_" name prefix and add onboarding images to assets folder
 */
public class OnBoardingActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ImageButton mNextBtn;
    private Button mSkipBtn, mFinishBtn;
    private final static int[] mOnBoardingImageViews = {R.id.intro_indicator_0, R.id.intro_indicator_1, R.id.intro_indicator_2};
    private final static int[] mOnBoardingColors = {R.color.cyan, R.color.orange, R.color.green};

    private List<ImageView> mIndicatorList;
    private List<String> mImagesPath;
    int page = 0;   //  to track page position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black_trans80));
        }

        mImagesPath = new ArrayList<>();

        String[] assets = Utils.getAssetsList(this);

        if(assets != null) {
            for(String onboard :assets){
                if(onboard.contains("onboard_")){
                    mImagesPath.add(onboard);
                }
            }
        }

        assert(mImagesPath.size() == mOnBoardingColors.length);
        assert(mImagesPath.size() == mOnBoardingImageViews.length);

        setContentView(R.layout.activity_onboarding);
        mSectionsPagerAdapter = new SectionsPagerAdapter(R.layout.fragment_onboarding, getSupportFragmentManager(), mImagesPath, true);

        mNextBtn = (ImageButton) findViewById(R.id.intro_btn_next);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
            mNextBtn.setImageDrawable(
                    Utils.tintMyDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chevron_right_24dp), Color.WHITE)
            );

        mSkipBtn = (Button) findViewById(R.id.intro_btn_skip);
        mFinishBtn = (Button) findViewById(R.id.intro_btn_finish);
        initIndicators();

        // Set up the ViewPager with the sections adapterList1.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(page);
        updateIndicators(page);
        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, getCurrentColor(position), getCurrentColor(position == (mOnBoardingImageViews.length-1) ? position : position + 1));
                mViewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                page = position;
                updateIndicators(page);

                mNextBtn.setVisibility(position == (mOnBoardingImageViews.length-1)  ? View.GONE : View.VISIBLE);
                mFinishBtn.setVisibility(position == (mOnBoardingImageViews.length-1)  ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                mViewPager.setCurrentItem(page, true);
            }
        });

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  update 1st time pref
                Utils.saveSharedSetting(OnBoardingActivity.this, MainActivity.PREF_USER_FIRST_TIME, "false");
                finishActivity();
            }
        });
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

    private int getCurrentColor(int position) {
        return ContextCompat.getColor(this, mOnBoardingColors[position]);
    }

    private void initIndicators() {
        mIndicatorList = new ArrayList<>();
        for (int mOnBoardingImage : mOnBoardingImageViews) {
            mIndicatorList.add((ImageView) findViewById(mOnBoardingImage));
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < mIndicatorList.size(); i++) {
            mIndicatorList.get(i).setBackgroundResource( i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected );
        }
        mViewPager.setBackgroundColor(getCurrentColor(position));
    }
}
