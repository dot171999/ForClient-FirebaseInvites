package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ablanco.zoomy.Zoomy;

import in.altilogic.prayogeek.R;
import pl.droidsonroids.gif.GifImageView;

public class PlaceholderFragment extends Fragment {
    private GifImageView mGif;
    private int[] mGifImages;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_IMAGES = "arg_images";
    private static final String ARG_LAYOUT_ID = "arg_layout_id";

    public static PlaceholderFragment newInstance(int layout_id, int sectionNumber, int[] images) {
        Log.d("APP-", "PlaceholderFragment::newInstance()");
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layout_id);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putIntArray(ARG_IMAGES, images);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int num_screen = 1;
        View rootView = null;
        if(getArguments() != null) {
            int layout_id = getArguments().getInt(ARG_LAYOUT_ID);
            rootView = inflater.inflate(layout_id, container, false);
            mGif = rootView.findViewById(R.id.gif_content);

            num_screen = getArguments().getInt(ARG_SECTION_NUMBER);
            mGifImages = getArguments().getIntArray(ARG_IMAGES);
            if (mGifImages == null || num_screen < 1 || num_screen > mGifImages.length)
                throw new AssertionError();

            Log.d("APP-", "PlaceholderFragment::onCreateView() mGifImages.length = " + mGifImages.length);
        }
        else
            Log.d("APP-", "PlaceholderFragment::onCreateView() arg = 0");
        mGif.setImageResource(mGifImages[num_screen-1]);
        Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(mGif);
        builder.register();
        return rootView;
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d("APP-", "PlaceholderFragment::onStop()");
        Zoomy.unregister(mGif);
    }
}
