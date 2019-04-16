package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
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

public class ButtonsFragment extends Fragment implements View.OnClickListener  {
    private final static String TAG = "YOUSCOPE-BUTTONS-FR";
    private static final String ARG_BUTTONS_ARRAY = "BUTTONS_ARRAY";
    private View.OnClickListener mListener;

    LinearLayout mLayout;
    ArrayList<Button> mListButtons = new ArrayList<>();

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    public static ButtonsFragment newInstance(ArrayList<String> buttons) {
        Log.d(TAG, "ButtonsFragment::newInstance()");
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_BUTTONS_ARRAY, buttons);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buttons, null);
        if(getArguments() != null) {
            ArrayList<String> mButNames = getArguments().getStringArrayList(ARG_BUTTONS_ARRAY);

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
        bt.setTextColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.black_trans80));
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
