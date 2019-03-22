package in.altilogic.prayogeek.fragments;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;

public class ImageFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = "YOUSCOPE-DB-IMAGE";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ScrollableViewPager mViewPager;
    private int mStatusBarColor;
    private TextView tvPageNumber;
    private List<String> mImageFiles;
    private int mPage;
    private boolean mIsAssets;
    private ArgbEvaluator mEvaluator;
    public ImageFragment(){
    }

    public interface OnClickListener {
        void onClick(View view, int page);
        void onPageChanged(int page);
    }

    private OnClickListener mOnClickListener;

    public static ImageFragment newInstance(ArrayList<String> files_path, int color, int page, boolean is_asses) {
        Log.d(TAG, "ImageFragment::newInstance");
        ImageFragment gifFragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt("show-gif-color", color);
        args.putInt("show-gif-page", page);
        args.putInt("show-gif-is-assets", is_asses ? 1 : 0);
        args.putStringArrayList("show-images-list", files_path);
        gifFragment.setArguments(args);
        return gifFragment;
    }

    public void setOnClickListener(OnClickListener listener){
        mOnClickListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "ImageFragment::onCreateView");

        return inflater.inflate(R.layout.fragment_show_gif, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "ImageFragment::onViewCreated");
        ImageButton btnHome = view.findViewById(R.id.btnHome);
        ImageButton btnMinimize = view.findViewById(R.id.btnMinimize);
        ImageButton btnDone = view.findViewById(R.id.btnDone);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        mViewPager = (ScrollableViewPager) view.findViewById(R.id.container);
        tvPageNumber.setText(" - / - ");

        btnHome.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        mImageFiles = getArguments().getStringArrayList("show-images-list");
        mStatusBarColor = getArguments().getInt("show-gif-color");
        mIsAssets = getArguments().getInt("show-images-list") == 1;
        mPage = getArguments().getInt("show-gif-page");
        if(mImageFiles == null || mImageFiles.size() == 0)
            throw new AssertionError();

        mEvaluator = new ArgbEvaluator();

        initViewPager();
    }

    private void initViewPager() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(R.layout.fragment_onboarding, getActivity().getSupportFragmentManager(), mImageFiles, mIsAssets);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(mPage);
        tvPageNumber.setText(" "+ (mPage+1)+"/" +"-"+ mImageFiles.size());
        mViewPager.addOnPageChangeListener(mViewPageListener);
    }

    private int getCurrentColor(int position) {
        return mStatusBarColor;
    }

    private void updateIndicators(int position) {
        mViewPager.setBackgroundColor(getCurrentColor(position));
    }

    @Override
    public void onClick(View view) {
        if(mOnClickListener != null)
            mOnClickListener.onClick(view, mViewPager.getCurrentItem());
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.notifyChangeInPosition(mImageFiles.size());
        Log.d(TAG, "ImageFragment::onStop");
    }

    private ViewPager.OnPageChangeListener mViewPageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int colorUpdate = (Integer) mEvaluator.evaluate(positionOffset, getCurrentColor(position), getCurrentColor(position == 2 ? position : position + 1));
            mViewPager.setBackgroundColor(colorUpdate);
            tvPageNumber.setText(" " + (position+1)+"/" +mImageFiles.size() + " ");
            if(mOnClickListener != null)
                mOnClickListener.onPageChanged(position);
        }

        @Override
        public void onPageSelected(int position) {
            updateIndicators(position);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };
}
