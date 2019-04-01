package in.altilogic.prayogeek.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.utils.Utils;

public class SerialSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String SETTINGS_PARITY_CHECK = "PARITY_CHECK_SETTINGS";
    public static final String SETTINGS_BAUD_RATE = "BAUD_RATE_SETTINGS";
    public static final String SETTINGS_DATA_BITS = "DATA_BITS_SETTINGS";
    public static final String SETTINGS_STOP_BIT = "STOP_BIT_SETTINGS";
    public static final String SETTINGS_FLOW_CONTROL = "FLOW_CONTROL_SETTINGS";
    public static final String SETTINGS_LINE_FEED = "LINE_FEED_SETTINGS";

    private Spinner spBaudRate, spLineFeed, spParityCheck, spDataBits, spStopBit, spFlowControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_settings);

        spBaudRate = findViewById(R.id.spBaudRate);
        spParityCheck = findViewById(R.id.spParityCheck);
        spDataBits = findViewById(R.id.spDataBits);
        spStopBit = findViewById(R.id.spStopBit);
        spFlowControl = findViewById(R.id.spFlowControl);
        spLineFeed = findViewById(R.id.spLineFeed);

        spBaudRate.setOnItemSelectedListener(this);
        spParityCheck.setOnItemSelectedListener(this);
        spDataBits.setOnItemSelectedListener(this);
        spStopBit.setOnItemSelectedListener(this);
        spFlowControl.setOnItemSelectedListener(this);
        spLineFeed.setOnItemSelectedListener(this);

        int baud_position = Utils.readSharedSetting(this, SETTINGS_BAUD_RATE, 2);
        int parity_position = Utils.readSharedSetting(this, SETTINGS_PARITY_CHECK, 0);
        int databits_position = Utils.readSharedSetting(this, SETTINGS_DATA_BITS, 3);
        int sopbit_position = Utils.readSharedSetting(this, SETTINGS_STOP_BIT, 0);
        int flow_position = Utils.readSharedSetting(this, SETTINGS_FLOW_CONTROL, 0);
        int line_feed_position = Utils.readSharedSetting(this, SETTINGS_LINE_FEED, 0);

        spBaudRate.setAdapter(new SpinnerAdapter(this, "Baud Rate : ", R.array.baud_rate_array));
        spParityCheck.setAdapter(new SpinnerAdapter(this, "Parity Check : ", R.array.parity_check_array));
        spDataBits.setAdapter(new SpinnerAdapter(this, "Data Bits : ", R.array.data_bits_array));
        spStopBit.setAdapter(new SpinnerAdapter(this, "Stop Bit : ", R.array.stop_bit_array));
        spFlowControl.setAdapter(new SpinnerAdapter(this, "Flow Control : ", R.array.flow_control_array));
        spLineFeed.setAdapter(new SpinnerAdapter(this, "Line Feed : ", R.array.line_feed_array));
//        spBaudRate.setAdapter(SpinnerAdapter.createFromResource(this, R.array.baud_rate_array, R.layout.spinner));
//        spParityCheck.setAdapter(SpinnerAdapter.createFromResource(this, R.array.parity_check_array, R.layout.spinner));
//        spDataBits.setAdapter(SpinnerAdapter.createFromResource(this, R.array.data_bits_array, R.layout.spinner));
//        spStopBit.setAdapter(SpinnerAdapter.createFromResource(this, R.array.stop_bit_array, R.layout.spinner));
//        spFlowControl.setAdapter(SpinnerAdapter.createFromResource(this, R.array.flow_control_array, R.layout.spinner));
//        spLineFeed.setAdapter(SpinnerAdapter.createFromResource(this, R.array.line_feed_array, R.layout.spinner));

        spBaudRate.setSelection(baud_position);
        spParityCheck.setSelection(parity_position);
        spDataBits.setSelection(databits_position);
        spStopBit.setSelection(sopbit_position);
        spFlowControl.setSelection(flow_position);
        spLineFeed.setSelection(line_feed_position);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String value = adapterView.getItemAtPosition(pos).toString();
        Log.d("Serial-settings", value + " " + pos);
        switch (adapterView.getId()) {
            case R.id.spBaudRate: Utils.saveSharedSetting(this, SETTINGS_BAUD_RATE, pos); break;
            case R.id.spDataBits:Utils.saveSharedSetting(this, SETTINGS_DATA_BITS, pos); break;
            case R.id.spFlowControl: Utils.saveSharedSetting(this, SETTINGS_FLOW_CONTROL, pos); break;
            case R.id.spParityCheck: Utils.saveSharedSetting(this, SETTINGS_PARITY_CHECK, pos); break;
            case R.id.spStopBit: Utils.saveSharedSetting(this, SETTINGS_STOP_BIT, pos); break;
            case R.id.spLineFeed: Utils.saveSharedSetting(this, SETTINGS_LINE_FEED, pos); break;
            default: break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class SpinnerAdapter extends ArrayAdapter {
        private LayoutInflater inflater;
        private String mName;
        public SpinnerAdapter(Context context, String name, int textViewResourceId) {
            super(context, R.layout.spinner);
            mName = name;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.spinner, null);
                holder.text1 = (TextView)convertView.findViewById(R.id.spinnerText1);
                holder.text2 = (TextView)convertView.findViewById(R.id.spinnerText2);
                convertView.setTag(R.layout.spinner, holder);
            } else{
                holder = (ViewHolder)convertView.getTag(R.layout.spinner);
            }

            holder.text1.setText(mName);
            holder.text2.setText(position);

            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            ViewHolder2 holder;

            if(convertView == null){
                holder = new ViewHolder2();
                convertView = inflater.inflate(R.layout.spinner, null);
                holder.text1 = (TextView)convertView.findViewById(R.id.spinnerText1);
                holder.text2 = (TextView)convertView.findViewById(R.id.spinnerText2);
                convertView.setTag(R.layout.spinner, holder);
            } else{
                holder = (ViewHolder2)convertView.getTag(R.layout.spinner);
            }

            holder.text1.setText(mName);
            holder.text2.setText(position);

            return convertView;
        }

        class ViewHolder{
            TextView text1;
            TextView text2;
        }

        class ViewHolder2{
            TextView text1;
            TextView text2;
        }
    }
}
