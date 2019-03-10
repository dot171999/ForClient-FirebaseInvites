package in.altilogic.prayogeek.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private int[] mDrawable;
    private int mLayoutId = -1;
    private long baseId = 0;

    private List<PlaceholderFragment> mFragmentList;

    private SectionsPagerAdapter(){
        super(null);
    }

    public SectionsPagerAdapter(int layout_id, FragmentManager fm, int[] drawable) {
        super(fm);
        Log.d("APP-", "SectionsPagerAdapter() " + getCount());

        mDrawable = drawable;
        mLayoutId = layout_id;
        if(mDrawable == null || mDrawable.length == 0)
            throw new AssertionError();

        mFragmentList = new ArrayList<>();
        for(int i=0; i<drawable.length; i++) {
            mFragmentList.add(PlaceholderFragment.newInstance(mLayoutId,i+1, mDrawable));
            Log.d("APP-", "SectionsPagerAdapter.add " + i);
        }
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("APP-", "SectionsPagerAdapter.getItem " + position);
        if(mDrawable.length != getCount())
        {
            Log.d("APP-", "SectionsPagerAdapter.getItem mDrawable.length = " + mDrawable.length + "; getCount = " + getCount());
        }
        return mFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

//    @Override
//    public long getItemId(int position) {
//        // give an ID different from position when position has been changed
//        return baseId + position;
//    }

    @Override
    public int getCount() {
        return mDrawable == null ? 0 : mDrawable.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "SECTION " + position;
    }

    public void notifyChangeInPosition(int n) {
        baseId += getCount() + n;
    }
}
