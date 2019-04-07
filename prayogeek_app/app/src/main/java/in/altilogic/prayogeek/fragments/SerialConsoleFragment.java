package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Objects;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.FileSaver;
import in.altilogic.prayogeek.utils.Utils;


public class SerialConsoleFragment extends Fragment implements AdapterView.OnClickListener  {

    private View.OnClickListener mListener;
    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    private TextView mTvOut;
    private Button btnSend, btnSettings, btnClear, btnSave;
    private ScrollView svConsole;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_serial_console, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mTvOut = (TextView) view. findViewById(R.id.tvConsoleOut);
        btnSend = (Button) view. findViewById(R.id.btnSend);
        btnSettings = (Button) view. findViewById(R.id.btnConsoleSettings);
        btnClear = (Button) view. findViewById(R.id.btnClear);
        btnSave = (Button) view. findViewById(R.id.btnSave);
        svConsole = (ScrollView) view. findViewById(R.id.svConsole);
        btnSend.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    void updateConsole(String data){
        mTvOut.append(data);
        svConsole.post(() -> svConsole.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.hideSoftKeyboard(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnClear:
                mTvOut.setText("");
                svConsole.post(() -> svConsole.fullScroll(View.FOCUS_DOWN));
                return;
            case R.id.btnSave:
            {
                FileSaver fileSaver = new FileSaver(Utils.readSharedSetting(getActivity(), SerialConsoleSettingsFragment.SETTINGS_LOG_FILE_NAME, getActivity().getPackageName()));
                fileSaver.write(mTvOut.getText().toString());
                mTvOut.setText("");
            }
            return;
        }

        if(mListener != null)
            mListener.onClick(view);
    }
}
