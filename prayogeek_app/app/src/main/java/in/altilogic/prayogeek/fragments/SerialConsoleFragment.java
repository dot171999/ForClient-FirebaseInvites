package in.altilogic.prayogeek.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.service.SerialConsoleService;
import in.altilogic.prayogeek.utils.FileSaver;
import in.altilogic.prayogeek.utils.Utils;
import pub.devrel.easypermissions.EasyPermissions;


public class SerialConsoleFragment extends Fragment implements AdapterView.OnClickListener, EasyPermissions.PermissionCallbacks   {
    public static final String TAG = "YOUSCOPE-SERIAL-FR";

    private View.OnClickListener mListener;
    public void setOnClickListener(View.OnClickListener listener){
        mListener = listener;
    }

    private TextView mTvOut;
    private Button btnSave;
    private ScrollView svConsole;
    private AutoCompleteTextView etSerialData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_serial_console, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mTvOut = (TextView) view. findViewById(R.id.tvConsoleOut);
        Button btnSend = (Button) view.findViewById(R.id.btnSend);
        Button btnSettings = (Button) view.findViewById(R.id.btnConsoleSettings);
        Button btnClear = (Button) view.findViewById(R.id.btnClear);
        btnSave = (Button) view.findViewById(R.id.btnSave);
        svConsole = (ScrollView) view. findViewById(R.id.svConsole);
        etSerialData = (AutoCompleteTextView) view. findViewById(R.id.etSerialData);
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
        checkWriteReadPermissions();
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
                return;
            }
            case R.id.btnSend:
                writeSerial();
                break;
        }

        if(mListener != null)
            mListener.onClick(view);
    }

    private boolean isHex() {
        return 0 < Utils.readSharedSetting(getActivity().getApplicationContext(), SerialConsoleSettingsFragment.SETTINGS_DATA_FORMAT, 0 );
    }


    private void writeSerial() {
        String sendText = etSerialData.getText().toString();
        sendText += getNewLine();
        byte[] bytes;
        try {
            bytes = sendText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        Intent intent = new Intent(getActivity(), SerialConsoleService.class);
        intent.putExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME, SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_WRITE_DATA);
        intent.putExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_DATA, bytes);
        getActivity().startService(intent);
    }

    private String getNewLine() {
        int pos = Utils.readSharedSetting(getActivity().getApplicationContext(), SerialConsoleSettingsFragment.SETTINGS_LINE_FEED, 0 );

        if(pos == SerialConsoleService.SERIAL_LINE_FEED_CR)
            return "\r";
        else if(pos == SerialConsoleService.SERIAL_LINE_FEED_NL)
            return "\n";
        else if(pos == SerialConsoleService.SERIAL_LINE_FEED_NLCR)
            return "\r\n";

        return "";
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
//            usbService = ((SerialConsoleService.UsbBinder) arg1).getService();
//            usbService.setHandler(mHandler);
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");

//            usbService = null;
        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!SerialConsoleService.SERVICE_CONNECTED) {
            Intent startService = new Intent(getActivity(), service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            getActivity().startService(startService);
        }
        Intent bindingIntent = new Intent(getActivity(), service);
        getActivity().bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if( isHex() ) {
            etSerialData.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        else {
            etSerialData.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        setFilters();
        startService(SerialConsoleService.class, usbConnection, null);
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getActivity()).unregisterReceiver(mUsbReceiver);
        getActivity().unbindService(usbConnection);
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        Objects.requireNonNull(getActivity()).stopService(new Intent(getActivity(), SerialConsoleService.class));
        super.onDestroy();
    }


    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SerialConsoleService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(SerialConsoleService.ACTION_NO_USB);
        filter.addAction(SerialConsoleService.ACTION_USB_DISCONNECTED);
        filter.addAction(SerialConsoleService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(SerialConsoleService.ACTION_USB_PERMISSION_NOT_GRANTED);
        filter.addAction(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME);
        filter.addAction(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_DATA);
        getActivity().registerReceiver(mUsbReceiver, filter);
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case SerialConsoleService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case SerialConsoleService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case SerialConsoleService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case SerialConsoleService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case SerialConsoleService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
                case SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME:
                    int type = intent.getIntExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME, -1);
                    switch(type){
                        case SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_PARAMETERS:
                            break;
                        case SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_READ_DATA:
                            byte[] serial_data = intent.getByteArrayExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_DATA);
                            long timestamp = intent.getLongExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_TIMESTAMP, 0);
                            int color = intent.getIntExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_COLOR, 0);
                            if(serial_data != null) {
                                StringBuilder sb = new StringBuilder();
                                boolean isHex = 0 < Utils.readSharedSetting(getActivity().getApplicationContext(), SerialConsoleSettingsFragment.SETTINGS_DATA_FORMAT, 0 );

                                String receive = new String(serial_data);

                                sb      .append(Utils.getTimestamp(timestamp))
                                        .append(isHex ? Utils.asciiToHex(receive) : receive );
                                if(mTvOut != null){
                                    mTvOut.setTextColor(color);
                                    String cStr = sb.toString();

                                    mTvOut.append(Html.fromHtml("<font color="+color+">"+cStr+"</font>"));
                                    mTvOut.append("\r\n");
                                    svConsole.post(()-> svConsole.fullScroll(View.FOCUS_DOWN));
                                }
                            }
                            break;
                        case SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_WRITE_DATA:
                            break;
                    }
                    break;
            }
        }
    };


    private boolean checkWriteReadPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            return true;
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.string_describe_why_do_you_need_a_write_ext),
                    1000, perms);
        }
        return false;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        btnSave.setClickable(true);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        btnSave.setClickable(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult " + requestCode);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}

