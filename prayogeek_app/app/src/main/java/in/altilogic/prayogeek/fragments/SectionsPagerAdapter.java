package in.altilogic.prayogeek.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private List<String> mImages;
    private int mLayoutId = -1;
    private long baseId = 0;

    private List<PlaceholderFragment> mFragmentList;

    private SectionsPagerAdapter(){
        super(null);
    }

    public SectionsPagerAdapter(int layout_id, FragmentManager fm, List<String> imagesPaths) {
        super(fm);
        Log.d("APP-", "SectionsPagerAdapter() " + getCount());

        mImages = imagesPaths;
        mLayoutId = layout_id;
        if(mImages == null)
            throw new AssertionError();

        mFragmentList = new ArrayList<>();
        for(int i=0; i<mImages.size(); i++) {
            mFragmentList.add(PlaceholderFragment.newInstance(mLayoutId,i+1, mImages.get(i)));
            Log.d("APP-", "SectionsPagerAdapter.add " + i);
        }
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("APP-", "SectionsPagerAdapter.getItem " + position);
        if(mImages.size() != getCount())
        {
            Log.d("APP-", "SectionsPagerAdapter.getItem mDrawable.length = " + mImages.size() + "; getCount = " + getCount());
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
}
