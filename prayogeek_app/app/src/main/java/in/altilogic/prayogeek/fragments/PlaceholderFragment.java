package in.altilogic.prayogeek.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ablanco.zoomy.Zoomy;

import java.io.IOException;

import in.altilogic.prayogeek.R;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PlaceholderFragment extends Fragment {
    private final static String TAG = "YOUSCOPE-DB-PLACEHOLDER";
    private GifImageView mGif;
    private String mFilepath;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_IMAGES = "arg_images";
    private static final String ARG_LAYOUT_ID = "arg_layout_id";

    public static PlaceholderFragment newInstance(int layout_id, int sectionNumber, String path) {
        Log.d(TAG, "PlaceholderFragment::newInstance()");
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layout_id);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_IMAGES,path);
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
            mFilepath  = getArguments().getString(ARG_IMAGES);
            if (mFilepath == null || num_screen < 1 )
                throw new AssertionError();

            Log.d(TAG, "PlaceholderFragment::onCreateView() mGifImage");
        }
        else
            Log.d(TAG, "PlaceholderFragment::onCreateView() arg = 0");

        if(mFilepath.contains(".gif")) {
            try {
                GifDrawable gifFromPath = new GifDrawable( mFilepath);
                mGif.setImageDrawable(gifFromPath);
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        else {
            Drawable jpegFromPath = Drawable.createFromPath(mFilepath);
            mGif.setImageDrawable(jpegFromPath);
        }

        Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(mGif);
        builder.register();
        return rootView;
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "PlaceholderFragment::onStop()");
        Zoomy.unregister(mGif);
    }
}
