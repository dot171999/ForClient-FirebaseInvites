package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import in.altilogic.prayogeek.R;

public class ButtonsFragment extends Fragment implements View.OnClickListener  {
    private View.OnClickListener mListener;

    LinearLayout mLayout;
    List<Button> mListButtons = new ArrayList<>();

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buttons, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLayout = (LinearLayout) view .findViewById(R.id.llButtons);

        for(int i=0; i<15; i++) {
            mListButtons.add(getConfiguredButton("Button " + i));
            mLayout.addView(mListButtons.get(i));
        }
    }

    private Button getConfiguredButton(String btnName) {

        Button bt = new Button(getActivity());
        bt.setText(btnName);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,8,0,0);
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
