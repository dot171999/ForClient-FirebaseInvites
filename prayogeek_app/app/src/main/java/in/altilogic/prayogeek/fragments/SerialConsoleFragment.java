package in.altilogic.prayogeek.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.activities.SerialConsoleActivity;
import in.altilogic.prayogeek.service.SerialConsoleService;
import in.altilogic.prayogeek.utils.Utils;


public class SerialConsoleFragment extends Fragment implements AdapterView.OnClickListener  {

    private SerialConsoleService usbService;

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

    @Override
    public void onStart() {
        super.onStart();
        Utils.hideSoftKeyboard(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(SerialConsoleService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mUsbReceiver);
        getActivity().unbindService(usbConnection);
    }

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
            startService(startService);
        }
        Intent bindingIntent = new Intent(getActivity(), service);
        getActivity().bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SerialConsoleService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(SerialConsoleService.ACTION_NO_USB);
        filter.addAction(SerialConsoleService.ACTION_USB_DISCONNECTED);
        filter.addAction(SerialConsoleService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(SerialConsoleService.ACTION_USB_PERMISSION_NOT_GRANTED);
        getActivity().registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class SerialConsoleHandler extends Handler {
        private final WeakReference<SerialConsoleActivity> mActivity;

        public SerialConsoleHandler(SerialConsoleActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SerialConsoleService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().mTvOut.append(data);
                    mActivity.get().svConsole.post(() -> mActivity.get().svConsole.fullScroll(View.FOCUS_DOWN));
                    break;
                case SerialConsoleService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case SerialConsoleService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((SerialConsoleService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
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
            }
        }
    };

    @Override
    public void onClick(View view) {

    }
}
