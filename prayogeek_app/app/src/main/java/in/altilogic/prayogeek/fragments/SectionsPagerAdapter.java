package in.altilogic.prayogeek.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter implements PlaceholderFragment.OnZoomListener {
    private final static String TAG = "YOUSCOPE-DB-SECTIONS-PA";
    private List<String> mImages;
    private int mLayoutId = -1;
    private long baseId = 0;

    private List<PlaceholderFragment> mFragmentList;
    private PlaceholderFragment.OnZoomListener mZoomListener;
    private SectionsPagerAdapter(){
        super(null);
    }

    public void setOnZoomListener(PlaceholderFragment.OnZoomListener listener) {
        mZoomListener = listener;
    }

    public SectionsPagerAdapter(int layout_id, FragmentManager fm, List<String> imagesPaths, boolean is_asses) {
        super(fm);
        Log.d(TAG, "SectionsPagerAdapter() " + getCount());

        mImages = imagesPaths;
        mLayoutId = layout_id;
        if(mImages == null)
            throw new AssertionError();

        mFragmentList = new ArrayList<>();
        for(int i=0; i<mImages.size(); i++) {
            mFragmentList.add(PlaceholderFragment.newInstance(mLayoutId,i+1, mImages.get(i), is_asses));
            Log.d(TAG, "SectionsPagerAdapter.add " + i);
        }

        for(int i=0; i<mFragmentList.size(); i++){
            mFragmentList.get(i).setOnZoomListener(this);
        }
        notifyDataSetChanged();
    }

    private int mCurPosition=0;

    @Override
    public Fragment getItem(int position) {
        mCurPosition = position;
        Log.d(TAG, "SectionsPagerAdapter.getItem " + position);
        if(mImages.size() != getCount()) {
            Log.d(TAG, "SectionsPagerAdapter.getItem mDrawable.length = " + mImages.size() + "; getCount = " + getCount());
        }
        if(position >= mFragmentList.size()) {
            Log.d(TAG, "SectionsPagerAdapter.getItem()  position(" + position + ") >= mFragmentList.size(" + mFragmentList.size() + ")");

            return null;
        }

        return mFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mImages == null ? 0 : mImages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "SECTION " + position;
    }

    public void notifyChangeInPosition(int n) {
        baseId += getCount() + n;
    }

    private float scale_old = 1.0f;

    @Override
    public void OnZoomChanged(float scale) {
//        if(scale != scale_old) {
//            scale_old = scale;
//            if(scale == 1.0f) {
//                Log.d(TAG, "OnZoomChanged: " + scale);
//                mFragmentList.clear();
//                mFragmentList.addAll(mFragmentListDefault);
//                notifyDataSetChanged();
//            }
//            else{
//                if(mFragmentList.size() > 1){
//                    Log.d(TAG, "OnZoomChanged: " + scale);
//                    PlaceholderFragment tempfragment = mFragmentList.get(mCurPosition);
//                    mFragmentList.clear();
//                    mFragmentList.add(tempfragment);
//                    notifyDataSetChanged();
//                }
//            }
//        }
    }
}
