package in.altilogic.prayogeek.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;

public class SerialSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String SETTINGS_PARITY_CHECK = "PARITY_CHECK_SETTINGS";
    private static final String SETTINGS_BAUD_RATE = "BAUD_RATE_SETTINGS";
    private static final String SETTINGS_DATA_BITS = "DATA_BITS_SETTINGS";
    private static final String SETTINGS_STOP_BIT = "STOP_BIT_SETTINGS";
    private static final String SETTINGS_FLOW_CONTROL = "FLOW_CONTROL_SETTINGS";

    private Spinner spBaudRate, spParityCheck, spDataBits, spStopBit, spFlowControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_settings);

        spBaudRate = findViewById(R.id.spBaudRate);
        spParityCheck = findViewById(R.id.spParityCheck);
        spDataBits = findViewById(R.id.spDataBits);
        spStopBit = findViewById(R.id.spStopBit);
        spFlowControl = findViewById(R.id.spFlowControl);

        spBaudRate.setOnItemSelectedListener(this);
        spParityCheck.setOnItemSelectedListener(this);
        spDataBits.setOnItemSelectedListener(this);
        spStopBit.setOnItemSelectedListener(this);
        spFlowControl.setOnItemSelectedListener(this);

        int baud_position = Utils.readSharedSetting(this, SETTINGS_BAUD_RATE, 2);
        int parity_position = Utils.readSharedSetting(this, SETTINGS_PARITY_CHECK, 0);
        int databits_position = Utils.readSharedSetting(this, SETTINGS_DATA_BITS, 3);
        int sopbit_position = Utils.readSharedSetting(this, SETTINGS_STOP_BIT, 0);
        int flow_position = Utils.readSharedSetting(this, SETTINGS_FLOW_CONTROL, 0);

        spBaudRate.setSelection(baud_position);
        spParityCheck.setSelection(parity_position);
        spDataBits.setSelection(databits_position);
        spStopBit.setSelection(sopbit_position);
        spFlowControl.setSelection(flow_position);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String value = adapterView.getItemAtPosition(pos).toString();
        Log.d("Serial-settings", value + " " + pos);
        switch (adapterView.getId()){
            case R.id.spBaudRate:
                Utils.saveSharedSetting(this, SETTINGS_BAUD_RATE, pos);
                break;
            case R.id.spDataBits:
                Utils.saveSharedSetting(this, SETTINGS_DATA_BITS, pos);
                break;
            case R.id.spFlowControl:
                Utils.saveSharedSetting(this, SETTINGS_FLOW_CONTROL, pos);
                break;
            case R.id.spParityCheck:
                Utils.saveSharedSetting(this, SETTINGS_PARITY_CHECK, pos);
                break;
            case R.id.spStopBit:
                Utils.saveSharedSetting(this, SETTINGS_STOP_BIT, pos);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
