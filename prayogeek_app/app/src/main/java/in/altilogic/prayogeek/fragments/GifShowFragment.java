package in.altilogic.prayogeek.fragments;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;

public class GifShowFragment extends Fragment implements View.OnClickListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private List<ImageView> mIndicatorList;
    private int[] mDrawableId;
    private int mStatusBarColor;

    public GifShowFragment(){
    }

    public static GifShowFragment newInstance(int[] drawable_id, FragmentManager fragmentManager, int color) {
        Log.d("APP-", "GifShowFragment::newInstance");
        GifShowFragment gifFragment = new GifShowFragment();
        Bundle args = new Bundle();
        args.putIntArray("show-gif-id", drawable_id);
        args.putInt("show-gif-color", color);
        gifFragment.setArguments(args);
        return gifFragment;
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
        Log.d("APP-", "GifShowFragment::onViewCreated");
        Button btnHome = (Button) view.findViewById(R.id.btnHome);
        Button btnMinimize = (Button) view.findViewById(R.id.btnMinimize);
        Button btnDone = (Button) view.findViewById(R.id.btnDone);
        btnHome.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        mDrawableId = getArguments().getIntArray("show-gif-id");
        mStatusBarColor = getArguments().getInt("show-gif-color");
        mSectionsPagerAdapter = new SectionsPagerAdapter(R.layout.fragment_onboarding, getActivity().getSupportFragmentManager(), mDrawableId);
        mIndicatorList = new ArrayList<>();
        mIndicatorList.add((ImageView) view.findViewById(R.id.gif_content));

        mViewPager = (ViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0);
        if(mDrawableId == null || mDrawableId.length == 0)
            throw new AssertionError();

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, getCurrentColor(position), getCurrentColor(position == 2 ? position : position + 1));
                mViewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private int getCurrentColor(int position) {
        return mStatusBarColor;
    }

    private void updateIndicators(int position) {
        mViewPager.setBackgroundColor(getCurrentColor(position));
    }

    @Override
    public void onClick(View view) {
        if(mListener != null)
            mListener.onClick(view);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.notifyChangeInPosition(mDrawableId.length);

        Log.d("APP-", "GifShowFragment::onStop");
    }
}
