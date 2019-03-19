package in.altilogic.prayogeek.fragments;

import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

//import com.ablanco.zoomy.Zoomy;
//import com.ablanco.zoomy.ZoomyConfig;

//import com.github.chrisbanes.photoview.PhotoView;
//import com.github.chrisbanes.photoview.PhotoViewAttacher;

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
//import uk.co.senab.photoview.PhotoView;
//import uk.co.senab.photoview.PhotoViewAttacher;

public class PlaceholderFragment extends Fragment implements View.OnTouchListener {
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
// 1) Option
//        ZoomyConfig config = new ZoomyConfig();
//        config.setZoomAnimationEnabled(true);
//        config.setImmersiveModeEnabled(true);
//
//        Zoomy.Builder builder = new Zoomy.Builder(getActivity()).target(mGif).interpolator(new DecelerateInterpolator());
//        Zoomy.setDefaultConfig(config);
//        builder.register();

// 2) Option
//        PhotoViewAttacher pAttacher;
//        pAttacher = new PhotoViewAttacher(mGif);
//        pAttacher.setMaximumScale((float) 4.0);
//        pAttacher.setMinimumScale((float) 1.0);
//        pAttacher.setZoomable(true);
//        pAttacher.update();

// 3) Option
        mGif.setOnTouchListener(this);

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

    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    float curScale = 1f;

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }
}
