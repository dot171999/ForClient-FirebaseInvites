package in.altilogic.prayogeek.fragments;

import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.ablanco.zoomy.Zoomy;
import com.ablanco.zoomy.ZoomyConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PlaceholderFragment extends Fragment {
    private final static String TAG = "YOUSCOPE-DB-PLACEHOLDER";
    private GifImageView mGif;
    private String mFilepath;
    private List<GifImageView> mListImages;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_IMAGES = "arg_images";
    private static final String ARG_LAYOUT_ID = "arg_layout_id";
    private static final String ARG_IS_ASSETS = "arg_is_assets";

    public static PlaceholderFragment newInstance(int layout_id, int sectionNumber, String path, boolean is_assets) {
        Log.d(TAG, "PlaceholderFragment::newInstance()");
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layout_id);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_IMAGES,path);
        args.putInt(ARG_IS_ASSETS, is_assets ? 1 : 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int num_screen = 1;
        View rootView = null;
        int is_assets = 0;
        if(getArguments() != null) {
            int layout_id = getArguments().getInt(ARG_LAYOUT_ID);
            rootView = inflater.inflate(layout_id, container, false);
            mGif = rootView.findViewById(R.id.gif_content);

            num_screen = getArguments().getInt(ARG_SECTION_NUMBER);
            mFilepath  = getArguments().getString(ARG_IMAGES);
            if (mFilepath == null || num_screen < 1 )
                throw new AssertionError();

            is_assets = getArguments().getInt(ARG_IS_ASSETS);

            Log.d(TAG, "PlaceholderFragment::onCreateView() mGifImage");
        }
        else
            Log.d(TAG, "PlaceholderFragment::onCreateView() arg = 0");

        if(mFilepath.contains(".gif")) {
            try {
                GifDrawable d;
                if(is_assets == 1) {
                    AssetFileDescriptor afd = getActivity().getAssets().openFd(mFilepath);
                    d = new GifDrawable( afd );
                }
                else{
                    byte[] data = getData(mFilepath);
                    d = new GifDrawable(data);
                }
                mGif.setImageDrawable(d);
            } catch (IOException e) {
                Log.d(TAG, e.getMessage() + "; " + mFilepath);
            }
        }
        else {
            try {
                Drawable d;
                if(is_assets == 1) {
                    d = Drawable.createFromStream(getActivity().getAssets().open(mFilepath), null);
                }
                else {
                    byte[] b = getData(mFilepath);
                    d = new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
                }
                mGif.setImageDrawable(d);
            }
            catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
        ZoomyConfig config = new ZoomyConfig();
        config.setZoomAnimationEnabled(true);
        config.setImmersiveModeEnabled(true);

        Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(mGif).interpolator(new DecelerateInterpolator());
        Zoomy.setDefaultConfig(config);
        builder.register();

        mListImages = new ArrayList<>();
        mListImages.add(mGif);
        return rootView;
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "PlaceholderFragment::onStop()");
        if(mGif != null)
            Zoomy.unregister(mGif);
        mFilepath = null;
        mGif = null;
    }

    private byte[] getData(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            int sz = 1;
            int offset = 0;
            while(sz > 0 ) {
                sz = buf.read(bytes, offset, bytes.length);
                offset += sz;
                if(offset >= size)
                    break;
            }
            buf.close();
            return Base64.decode(bytes, Base64.NO_WRAP);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
