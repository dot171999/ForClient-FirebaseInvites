package in.altilogic.prayogeek.fragments;

import android.animation.ArgbEvaluator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;

public class ImageFragment extends Fragment implements View.OnClickListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private List<ImageView> mIndicatorList;
    private int[] mDrawableId;
    private int mStatusBarColor;
    private TextView tvPageNumber;
    public ImageFragment(){
    }

    public interface OnClickListener {
        void onClick(View view, int page);
    }

    private OnClickListener mOnClickListener;

    public static ImageFragment newInstance(int[] drawable_id, int color, int page) {
        Log.d("APP-", "ImageFragment::newInstance");
        ImageFragment gifFragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putIntArray("show-gif-id", drawable_id);
        args.putInt("show-gif-color", color);
        args.putInt("show-gif-page", page);
        gifFragment.setArguments(args);
        return gifFragment;
    }

    public void setOnClickListener(OnClickListener listener){
        mOnClickListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_gif, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d("APP-", "ImageFragment::onViewCreated");
        ImageButton btnHome = view.findViewById(R.id.btnHome);
        ImageButton btnMinimize = view.findViewById(R.id.btnMinimize);
        ImageButton btnDone = view.findViewById(R.id.btnDone);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        btnHome.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        mDrawableId = getArguments().getIntArray("show-gif-id");
        mStatusBarColor = getArguments().getInt("show-gif-color");
        int page = getArguments().getInt("show-gif-page");
        mSectionsPagerAdapter = new SectionsPagerAdapter(R.layout.fragment_onboarding, getActivity().getSupportFragmentManager(), mDrawableId);
        mIndicatorList = new ArrayList<>();
        mIndicatorList.add((ImageView) view.findViewById(R.id.gif_content));
        assert (page < mDrawableId.length);
        mViewPager = (ViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(page);
        if(mDrawableId == null || mDrawableId.length == 0)
            throw new AssertionError();
        tvPageNumber.setText(" "+ (page+1)+"/" +mDrawableId.length + " ");
        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, getCurrentColor(position), getCurrentColor(position == 2 ? position : position + 1));
                mViewPager.setBackgroundColor(colorUpdate);
                tvPageNumber.setText(" " + (position+1)+"/" +mDrawableId.length + " ");
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
        if(mOnClickListener != null)
            mOnClickListener.onClick(view, mViewPager.getCurrentItem());
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.notifyChangeInPosition(mDrawableId.length);

        Log.d("APP-", "ImageFragment::onStop");
    }
}
