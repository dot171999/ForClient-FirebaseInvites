package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import in.altilogic.prayogeek.R;

public class ProjectsFragment extends Fragment implements View.OnClickListener {

    private View.OnClickListener mListener;

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button btnProject1 = (Button) view.findViewById(R.id.btnProject1);
        Button btnProject2 = (Button) view.findViewById(R.id.btnProject2);
        btnProject1.setOnClickListener(this);
        btnProject2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(mListener != null)
            mListener.onClick(view);
    }
}