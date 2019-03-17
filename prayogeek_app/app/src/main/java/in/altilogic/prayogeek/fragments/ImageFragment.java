package in.altilogic.prayogeek.fragments;

import android.animation.ArgbEvaluator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.service.ImageDownloadService;
import in.altilogic.prayogeek.utils.Utils;

public class ImageFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "YOUSCOPE-DB-IMAGE";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
//    private List<ImageView> mIndicatorList;
    private int mStatusBarColor;
    private TextView tvPageNumber;
    private BroadcastReceiver mBroadcastReceiver;
    private String mImagesType;
    private List<String> mImageFiles;
    private int mPage;

    public ImageFragment(){
    }

    public interface OnClickListener {
        void onClick(View view, int page);
        void onPageChanged(int page);
    }

    private OnClickListener mOnClickListener;

    public static ImageFragment newInstance(String experiment_folder, String images_type, int color, int page) {
        Log.d(TAG, "ImageFragment::newInstance");
        ImageFragment gifFragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt("show-gif-color", color);
        args.putInt("show-gif-page", page);
        args.putString("show-images-folder", experiment_folder);
        args.putString("show-images-type", images_type);
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
        initBroadcastReceiver();
        ImageButton btnHome = view.findViewById(R.id.btnHome);
        ImageButton btnMinimize = view.findViewById(R.id.btnMinimize);
        ImageButton btnDone = view.findViewById(R.id.btnDone);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        mViewPager = (ViewPager) view.findViewById(R.id.container);
        tvPageNumber.setText(" - / - ");

        btnHome.setOnClickListener(this);
        btnMinimize.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        String mExperimentFolder = getArguments().getString("show-images-folder");
        mImagesType = getArguments().getString("show-images-type");
        mStatusBarColor = getArguments().getInt("show-gif-color");
        mPage = getArguments().getInt("show-gif-page");
        mImageFiles = new ArrayList<>();

        startDownload(mExperimentFolder, mImagesType);
    }

    private void initViewPager() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(R.layout.fragment_onboarding, getActivity().getSupportFragmentManager(), mImageFiles);
//        mIndicatorList = new ArrayList<>();
//        mIndicatorList.add((ImageView) view.findViewById(R.id.gif_content));
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(mPage);
        if(mImagesType == null)
            throw new AssertionError();
        tvPageNumber.setText(" "+ (mPage+1)+"/" +"-"+ mImageFiles.size());
        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, getCurrentColor(position), getCurrentColor(position == 2 ? position : position + 1));
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
        });
    }

    private int getCurrentColor(int position) {
        return mStatusBarColor;
    }

    private void updateIndicators(int position) {
        mViewPager.setBackgroundColor(getCurrentColor(position));
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "ImageFragment::onStart");
        IntentFilter statusIntentFilter = new IntentFilter(ImageDownloadService.HW_SERVICE_BROADCAST_VALUE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, statusIntentFilter);
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

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        Log.d(TAG, "ImageFragment::onStop");
    }

    private void initBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "BroadcastReceiver:");
                int result = intent.getIntExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, -1);
                switch (result){
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_START_DOWNLOAD:
                        Toast.makeText(getActivity(), "Downloading Experiment. Please wait..", Toast.LENGTH_SHORT ).show();
                        break;
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_NO_INTERNET:
                        Toast.makeText(getActivity(), "No Network Connection. Turn ON network and Retry", Toast.LENGTH_SHORT ).show();
                        mOnClickListener.onClick(getView(), R.id.btnDone);
                        break;
                    case ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_IMAGE_FILES:
                        Log.d(TAG, "Download complete");

                        downloadImagesFromFile();
                        initViewPager();

                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void downloadImagesFromFile() {
        int number = getFilesNumber(mImagesType);
        for(int i=0; i<number; i++) {
            String name = getFilePath(mImagesType+(i+1));
            mImageFiles.add(name);
        }
    }

    private void startDownload(String experimentFolder, String name) {
        getActivity().startService(new Intent(getActivity(),ImageDownloadService.class)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_ID, ImageDownloadService.HW_SERVICE_MESSAGE_TYPE_DOWNLOAD_IMAGES)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_EXPERIMENT, experimentFolder)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_FIRESTORE, name)
                .putExtra(ImageDownloadService.HW_SERVICE_MESSAGE_DOWNLOAD_PATH_PHONE,""));
    }

    private int getFilesNumber(String settings_key) {
        return Utils.readSharedSetting(getActivity(), settings_key + "_number", 0);
    }

    private String getFilePath(String settings_key){
        String fileName = Utils.readSharedSetting(getActivity(), settings_key, null);
        Log.d(TAG, "Get file key: " + settings_key + "; name: " + fileName);
        return fileName;
    }
}
