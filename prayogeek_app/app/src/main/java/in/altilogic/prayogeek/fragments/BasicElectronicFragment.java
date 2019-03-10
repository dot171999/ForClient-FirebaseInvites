package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import in.altilogic.prayogeek.R;

public class BasicElectronicFragment extends Fragment implements View.OnClickListener {

    private View.OnClickListener mListener;

    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basic_electronic, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button btnBreadBoard = (Button) view.findViewById(R.id.btnBreadBoard);
        Button btnLedOnOFF = (Button) view.findViewById(R.id.btnLedOnOFF);
        Button btnPowerSupply = (Button) view.findViewById(R.id.btnPowerSupply);
        Button btnTransistorSwitch = (Button) view.findViewById(R.id.btnTransistorSwitch);
        Button btnIC741 = (Button) view.findViewById(R.id.btnIC741);
        Button btnIC555 = (Button) view.findViewById(R.id.btnIC555);
        btnBreadBoard.setOnClickListener(this);
        btnLedOnOFF.setOnClickListener(this);
        btnPowerSupply.setOnClickListener(this);
        btnTransistorSwitch.setOnClickListener(this);
        btnIC741.setOnClickListener(this);
        btnIC555.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(mListener != null)
            mListener.onClick(view);
    }
}
