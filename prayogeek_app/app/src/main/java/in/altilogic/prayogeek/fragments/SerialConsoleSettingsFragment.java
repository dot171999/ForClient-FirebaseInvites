package in.altilogic.prayogeek.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Objects;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.activities.SerialSettingsActivity;
import in.altilogic.prayogeek.utils.Utils;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class SerialConsoleSettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    public static final String SETTINGS_PARITY_CHECK = "PARITY_CHECK_SETTINGS";
    public static final String SETTINGS_BAUD_RATE = "BAUD_RATE_SETTINGS";
    public static final String SETTINGS_DATA_BITS = "DATA_BITS_SETTINGS";
    public static final String SETTINGS_STOP_BIT = "STOP_BIT_SETTINGS";
    public static final String SETTINGS_FLOW_CONTROL = "FLOW_CONTROL_SETTINGS";
    public static final String SETTINGS_LINE_FEED = "LINE_FEED_SETTINGS";
    public static final String SETTINGS_DATA_FORMAT = "DATA_FORMAT_SETTINGS";
    public static final String SETTINGS_LOG_FILE_NAME = "LOG_FILE_SETTINGS";

    private Spinner spBaudRate, spLineFeed, spParityCheck, spDataBits, spStopBit, spFlowControl, spDataFormat;
    private EditText etFileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_serial_settings, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        spBaudRate = view.findViewById(R.id.spBaudRate);
        spParityCheck = view.findViewById(R.id.spParityCheck);
        spDataBits = view.findViewById(R.id.spDataBits);
        spStopBit = view.findViewById(R.id.spStopBit);
        spFlowControl = view.findViewById(R.id.spFlowControl);
        spLineFeed = view.findViewById(R.id.spLineFeed);
        spDataFormat = view.findViewById(R.id.spDataFormat);
        etFileName = view.findViewById(R.id.etFileName);

        spBaudRate.setOnItemSelectedListener(this);
        spParityCheck.setOnItemSelectedListener(this);
        spDataBits.setOnItemSelectedListener(this);
        spStopBit.setOnItemSelectedListener(this);
        spFlowControl.setOnItemSelectedListener(this);
        spLineFeed.setOnItemSelectedListener(this);

        int baud_position = Utils.readSharedSetting(getActivity(), SETTINGS_BAUD_RATE, 2);
        int parity_position = Utils.readSharedSetting(getActivity(), SETTINGS_PARITY_CHECK, 0);
        int databits_position = Utils.readSharedSetting(getActivity(), SETTINGS_DATA_BITS, 3);
        int sopbit_position = Utils.readSharedSetting(getActivity(), SETTINGS_STOP_BIT, 0);
        int flow_position = Utils.readSharedSetting(getActivity(), SETTINGS_FLOW_CONTROL, 0);
        int line_feed_position = Utils.readSharedSetting(getActivity(), SETTINGS_LINE_FEED, 0);

//        spBaudRate.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.baud_rate_array, R.layout.spinner));
//        spParityCheck.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.parity_check_array, R.layout.spinner));
//        spDataBits.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.data_bits_array, R.layout.spinner));
//        spStopBit.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.stop_bit_array, R.layout.spinner));
//        spFlowControl.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.flow_control_array, R.layout.spinner));
//        spLineFeed.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.line_feed_array, R.layout.spinner));

//        spBaudRate.setSelection(baud_position);
//        spParityCheck.setSelection(parity_position);
//        spDataBits.setSelection(databits_position);
//        spStopBit.setSelection(sopbit_position);
//        spFlowControl.setSelection(flow_position);
//        spLineFeed.setSelection(line_feed_position);

//        etFileName.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onStart() {
        super.onStart();
        hideSoftKeyboard();

    }
    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
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
            default: break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
