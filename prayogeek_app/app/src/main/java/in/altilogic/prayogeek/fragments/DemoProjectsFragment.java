package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import in.altilogic.prayogeek.R;

public class DemoProjectsFragment extends Fragment implements View.OnClickListener {

    private View.OnClickListener mListener;

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demoprojects, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button btnDemoProject1 = (Button) view.findViewById(R.id.btnDemoProject1);
        Button btnDemoProject2 = (Button) view.findViewById(R.id.btnDemoProject2);
        btnDemoProject1.setOnClickListener(this);
        btnDemoProject2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(mListener != null)
            mListener.onClick(view);
    }
}
