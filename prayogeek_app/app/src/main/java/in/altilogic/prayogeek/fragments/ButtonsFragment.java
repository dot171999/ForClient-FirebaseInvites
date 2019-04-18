package in.altilogic.prayogeek.fragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Objects;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.RemoteButtonScreen;

public class ButtonsFragment extends Fragment implements View.OnClickListener  {
    private final static String TAG = "YOUSCOPE-BUTTONS-FR";
    private static final String ARG_BUTTONS_ARRAY = "BUTTONS_ARRAY";
    private View.OnClickListener mListener;

    private LinearLayout mLayout;
    private RemoteButtonScreen mScreen;

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    public static ButtonsFragment newInstance(RemoteButtonScreen screen) {
        Log.d(TAG, "ButtonsFragment::newInstance()");
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BUTTONS_ARRAY, screen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buttons, null);
        if(getArguments() != null) {
            Parcelable parcelable = getArguments().getParcelable(ARG_BUTTONS_ARRAY);
            mScreen = new RemoteButtonScreen(parcelable);
            if (mButNames != null) {
                for(String butName : mButNames) {
                    mListButtons.add(getConfiguredButton(butName));
                }
            }
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(mLandscape ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mLayout = (LinearLayout) view .findViewById(R.id.llButtons);

        for(int i=0; i<mListButtons.size(); i++) {
            mListButtons.get(i).setOnClickListener(this);
            mListButtons.get(i).setId(i+1);
            mLayout.addView(mListButtons.get(i));
        }
    }

    private Button getConfiguredButton(String btnName) {

        int buttonStyle = R.style.AppTheme_Button;

        Button bt = new Button(new ContextThemeWrapper(getActivity(), buttonStyle), null, buttonStyle);
        bt.setText(btnName);
        bt.setTextColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.button_text_color));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8,8,8,0);
        bt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return bt;
    }

    @Override
    public void onClick(View view) {
        if(mListener != null)
            mListener.onClick(view);
    }
}
