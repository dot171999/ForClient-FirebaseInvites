package in.altilogic.prayogeek.fragments;

import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import uk.co.senab.photoview.PhotoViewAttacher;

public class PlaceholderFragment extends Fragment implements PhotoViewAttacher.OnScaleChangeListener
//        implements View.OnTouchListener
{
    private final static String TAG = "YOUSCOPE-DB-PLACEHOLDER";
    private GifImageView mGif;
    private String mFilepath;
    private List<GifImageView> mListImages;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_IMAGES = "arg_images";
    private static final String ARG_LAYOUT_ID = "arg_layout_id";
    private static final String ARG_IS_ASSETS = "arg_is_assets";

    @Override
    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
        Log.d(TAG, "onScaleChange: " + scaleFactor + "; " + focusX + " : " + focusY);
        if(mZoomListener!= null)
            mZoomListener.OnZoomChanged(scaleFactor);
    }

    public interface OnZoomListener{
        void OnZoomChanged(float scale);
    }

    private OnZoomListener mZoomListener;

    public void setOnZoomListener(OnZoomListener listener) {
        mZoomListener = listener;
    }

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
// 1) Option
//        ZoomyConfig config = new ZoomyConfig();
//        config.setZoomAnimationEnabled(true);
//        config.setImmersiveModeEnabled(true);
//
//        Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(mGif).interpolator(new DecelerateInterpolator());
//        Zoomy.setDefaultConfig(config);
//        builder.register();

// 2) Option
        PhotoViewAttacher pAttacher;
        pAttacher = new PhotoViewAttacher(mGif);
        pAttacher.setMaximumScale((float) 4.0);
        pAttacher.setMinimumScale((float) 1.0);
        pAttacher.setZoomable(true);
        pAttacher.setOnScaleChangeListener(this);
        pAttacher.update();

// 3) Option
//        mGif.setOnTouchListener(this);

        mListImages = new ArrayList<>();
        mListImages.add(mGif);
        return rootView;
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "PlaceholderFragment::onStop()");
//        if(mGif != null)
//            Zoomy.unregister(mGif);
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

//    @SuppressWarnings("unused")
//    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;
//
//    // These matrices will be used to scale points of the image
//    Matrix matrix = new Matrix();
//    Matrix savedMatrix = new Matrix();
//    Matrix defaultMatrix = null;
//
//    // The 3 states (events) which the user is trying to perform
//    static final int NONE = 0;
//    static final int DRAG = 1;
//    static final int ZOOM = 2;
//    int mode = NONE;
//
//    // these PointF objects are used to record the point(s) the user is touching
//    PointF start = new PointF();
//    PointF mid = new PointF();
//    float oldDist = 1f;
//    private long mDoubleClickStartTime;
//    long duration;
//    private int mDoubleClickCount = 0;
//    static final int MAX_DURATION = 500;
//    ImageView.ScaleType defScale = null;
//    @Override
//    public boolean onTouch(View v, MotionEvent event)
//    {
//        ImageView view = (ImageView) v;
//        view.setScaleType(ImageView.ScaleType.MATRIX);
//        float scale;
//
//        if(defaultMatrix == null)
//        {
//            defaultMatrix = view.getImageMatrix();
//            defScale = view.getScaleType();
//        }
//
//        switch (event.getAction() & MotionEvent.ACTION_MASK)
//        {
//            case MotionEvent.ACTION_DOWN:   // first finger down only
//                matrix.set(view.getImageMatrix());
//                savedMatrix.set(matrix);
//                start.set(event.getX(), event.getY());
//                mode = DRAG;
//
//                mDoubleClickStartTime = System.currentTimeMillis();
//                mDoubleClickCount++;
//                break;
//
//            case MotionEvent.ACTION_UP: // first finger lifted
//                long time = System.currentTimeMillis() - mDoubleClickStartTime;
//                duration =  duration + time;
//                if(mDoubleClickCount >= 2)
//                {
//                    if(duration <= MAX_DURATION)
//                    {
//                        Log.d(TAG,"double tap");
//                        oldDist = 1f;
//                        mode = NONE;
//                        start = null;
//                        mid = null;
//                        start = new PointF();
//                        mid = new PointF();
//                        view.setImageMatrix(new Matrix());
//                        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        view.invalidate();
//                        if(mZoomListener != null)
//                            mZoomListener.OnZoomChanged(1.0f);
//
//                        return true;
//                    }
//                    mDoubleClickCount = 0;
//                    duration = 0;
//                }
//
//            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
//
//                mode = NONE;
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
//
//                oldDist = spacing(event);
//                Log.d(TAG, "oldDist=" + oldDist);
//                if (oldDist > 5f) {
//                    savedMatrix.set(matrix);
//                    midPoint(mid, event);
//                    mode = ZOOM;
//                    Log.d(TAG, "mode=ZOOM");
//                }
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//
//                if (mode == DRAG)
//                {
//                    matrix.set(savedMatrix);
//                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
//                }
//                else if (mode == ZOOM)
//                {
//                    // pinch zooming
//                    float newDist = spacing(event);
//                    Log.d(TAG, "newDist=" + newDist);
//                    if (newDist > 5f)
//                    {
//                        matrix.set(savedMatrix);
//                        scale = newDist / oldDist; // setting the scaling of the
//                        // matrix...if scale > 1 means
//                        // zoom in...if scale < 1 means
//                        // zoom out
//                        matrix.postScale(scale, scale, mid.x, mid.y);
//
//                        if(mZoomListener != null)
//                            mZoomListener.OnZoomChanged(scale);
//                    }
//                }
//                break;
//        }
//
//        view.setImageMatrix(matrix); // display the transformation on screen
//
//        return true; // indicate event was handled
//    }
//
//    /*
//     * --------------------------------------------------------------------------
//     * Method: spacing Parameters: MotionEvent Returns: float Description:
//     * checks the spacing between the two fingers on touch
//     * ----------------------------------------------------
//     */
//
//    private float spacing(MotionEvent event)
//    {
//        float x = event.getX(0) - event.getX(1);
//        float y = event.getY(0) - event.getY(1);
//        return (float) Math.sqrt(x * x + y * y);
//    }
//
//    /*
//     * --------------------------------------------------------------------------
//     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
//     * Description: calculates the midpoint between the two fingers
//     * ------------------------------------------------------------
//     */
//
//    private void midPoint(PointF point, MotionEvent event)
//    {
//        float x = event.getX(0) + event.getX(1);
//        float y = event.getY(0) + event.getY(1);
//        point.set(x / 2, y / 2);
//    }
}
