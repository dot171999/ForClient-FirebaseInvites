package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Objects;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;


public class SerialConsoleSettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final String SETTINGS_PARITY_CHECK = "PARITY_CHECK_SETTINGS";
    public static final String SETTINGS_BAUD_RATE = "BAUD_RATE_SETTINGS";
    public static final String SETTINGS_DATA_BITS = "DATA_BITS_SETTINGS";
    public static final String SETTINGS_STOP_BIT = "STOP_BIT_SETTINGS";
    public static final String SETTINGS_FLOW_CONTROL = "FLOW_CONTROL_SETTINGS";
    public static final String SETTINGS_LINE_FEED = "LINE_FEED_SETTINGS";
    public static final String SETTINGS_DATA_FORMAT = "DATA_FORMAT_SETTINGS";
    public static final String SETTINGS_LOG_FILE_NAME = "LOG_FILE_SETTINGS";

    private EditText etFileName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_serial_settings, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Spinner spBaudRate = view.findViewById(R.id.spBaudRate);
        Spinner spParityCheck = view.findViewById(R.id.spParityCheck);
        Spinner spDataBits = view.findViewById(R.id.spDataBits);
        Spinner spStopBit = view.findViewById(R.id.spStopBit);
        Spinner spFlowControl = view.findViewById(R.id.spFlowControl);
        Spinner spLineFeed = view.findViewById(R.id.spLineFeed);
        Spinner spDataFormat = view.findViewById(R.id.spDataFormat);
        etFileName = view.findViewById(R.id.etFileName);

        spBaudRate.setOnItemSelectedListener(this);
        spParityCheck.setOnItemSelectedListener(this);
        spDataBits.setOnItemSelectedListener(this);
        spStopBit.setOnItemSelectedListener(this);
        spFlowControl.setOnItemSelectedListener(this);
        spLineFeed.setOnItemSelectedListener(this);
        spDataFormat.setOnItemSelectedListener(this);

        int baud_position = Utils.readSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_BAUD_RATE, 2);
        int parity_position = Utils.readSharedSetting(getActivity(), SETTINGS_PARITY_CHECK, 0);
        int databits_position = Utils.readSharedSetting(getActivity(), SETTINGS_DATA_BITS, 3);
        int stopbit_position = Utils.readSharedSetting(getActivity(), SETTINGS_STOP_BIT, 0);
        int flow_position = Utils.readSharedSetting(getActivity(), SETTINGS_FLOW_CONTROL, 0);
        int line_feed_position = Utils.readSharedSetting(getActivity(), SETTINGS_LINE_FEED, 0);
        int data_format_position = Utils.readSharedSetting(getActivity(), SETTINGS_DATA_FORMAT, 0);

        spBaudRate.setSelection(baud_position < getResources().getIntArray(R.array.baud_rate_array).length ? baud_position : 0);
        spParityCheck.setSelection(parity_position < getResources().getIntArray(R.array.parity_check_array).length ? parity_position : 0);
        spDataBits.setSelection(databits_position < getResources().getIntArray(R.array.data_bits_array).length ? databits_position : 0);
        spStopBit.setSelection(stopbit_position < getResources().getIntArray(R.array.stop_bit_array).length ? stopbit_position : 0);
        spFlowControl.setSelection(flow_position < getResources().getIntArray(R.array.flow_control_array).length ? flow_position : 0);
        spLineFeed.setSelection(line_feed_position < getResources().getIntArray(R.array.line_feed_array).length ? line_feed_position : 0);
        spDataFormat.setSelection(data_format_position < getResources().getIntArray(R.array.data_format_array).length ? data_format_position : 0);

        etFileName.setText(Utils.readSharedSetting(getActivity(), SETTINGS_LOG_FILE_NAME, ""));
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.hideSoftKeyboard(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String value = adapterView.getItemAtPosition(pos).toString();
        Log.d("Serial-settings", value + " " + pos);
        switch (adapterView.getId()) {
            case R.id.spBaudRate: Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_BAUD_RATE, pos); break;
            case R.id.spDataBits:Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_DATA_BITS, pos); break;
            case R.id.spFlowControl: Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_FLOW_CONTROL, pos); break;
            case R.id.spParityCheck: Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_PARITY_CHECK, pos); break;
            case R.id.spStopBit: Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_STOP_BIT, pos); break;
            case R.id.spLineFeed: Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_LINE_FEED, pos); break;
            case R.id.spDataFormat: Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_DATA_FORMAT, pos); break;
            default: break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.saveSharedSetting(Objects.requireNonNull(getActivity()), SETTINGS_LOG_FILE_NAME, etFileName.getText().toString());
    }
}
