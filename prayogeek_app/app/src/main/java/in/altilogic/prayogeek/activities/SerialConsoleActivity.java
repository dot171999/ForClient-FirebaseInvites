package in.altilogic.prayogeek.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
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
import in.altilogic.prayogeek.fragments.SerialConsoleFragment;
import in.altilogic.prayogeek.fragments.SerialConsoleSettingsFragment;
import in.altilogic.prayogeek.service.ImageDownloadService;
import in.altilogic.prayogeek.service.SerialConsoleService;
import in.altilogic.prayogeek.utils.Utils;
import pub.devrel.easypermissions.EasyPermissions;

public class SerialConsoleActivity  extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    public static final String TAG = "YOUSCOPE-SERIAL";

    private FragmentManager mFragmentManager;
    SerialConsoleFragment mConsoleFragment;
    private final static int SCREEN_ID_SERIAL_SETTINGS = 14;
    private final static int SCREEN_ID_SERIAL_CONSOLE = 15;

    private int mScreenStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutolial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mFragmentManager = getSupportFragmentManager();
        showSerialConsoleFragment();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopService(new Intent(this, ImageDownloadService.class));
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();
        startService(SerialConsoleService.class, usbConnection, null);
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
        Log.d(TAG, "onPause()");
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!SerialConsoleService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSend:
                writeSerial();
            break;
            case R.id.btnConsoleSettings:
                showSettingsFragment();
                break;
            case R.id.btnClear:
            break;
            case R.id.btnSave:
            break;
        }
    }

    private void writeSerial() {
        if(mConsoleFragment != null) {
            String sendText = ((AutoCompleteTextView)mConsoleFragment.getView().findViewById(R.id.etSerialData)).getText().toString();
            sendText += getNewLine();
            byte[] bytes;
            try {
                bytes = sendText.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return;
            }
            Intent intent = new Intent(this, SerialConsoleService.class);
            intent.putExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME, SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_WRITE_DATA);
            intent.putExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_DATA, bytes);
            startService(intent);
        }
    }

    private String getNewLine() {
        int pos = Utils.readSharedSetting(getApplicationContext(), SerialConsoleSettingsFragment.SETTINGS_LINE_FEED, 0 );

        if(pos == SerialConsoleService.SERIAL_LINE_FEED_CR)
            return "\r";
        else if(pos == SerialConsoleService.SERIAL_LINE_FEED_NL)
            return "\n";
        else if(pos == SerialConsoleService.SERIAL_LINE_FEED_NLCR)
            return "\r\n";

        return "";
    }

    private void showSettingsFragment() {
        mScreenStatus = SCREEN_ID_SERIAL_SETTINGS;
        SerialConsoleSettingsFragment mSettingsFragment = new SerialConsoleSettingsFragment();
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mSettingsFragment).commit();
    }

    private void showSerialConsoleFragment(){
        mScreenStatus = SCREEN_ID_SERIAL_CONSOLE;
        mConsoleFragment = new SerialConsoleFragment();
        mConsoleFragment.setOnClickListener(this);
        mFragmentManager.beginTransaction().replace(R.id.fragmentContent, mConsoleFragment).commit();
        checkWriteReadPermissions();
    }


    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver");

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
                            if(mConsoleFragment!= null && serial_data != null && mScreenStatus == SCREEN_ID_SERIAL_CONSOLE) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(Utils.getTimestamp(timestamp)).append(new String(serial_data));
                                TextView tvConsole = ((TextView)mConsoleFragment.getView().findViewById(R.id.tvConsoleOut));
                                if(tvConsole != null){
                                    tvConsole.setTextColor(color);
                                    String cStr = sb.toString();

                                    tvConsole.append(Html.fromHtml("<font color="+color+">"+cStr+"</font>"));
                                    tvConsole.append("\r\n");
                                    ((ScrollView)(mConsoleFragment.getView().findViewById(R.id.svConsole))).post(()->
                                            ((ScrollView)(mConsoleFragment.getView().findViewById(R.id.svConsole))).fullScroll(View.FOCUS_DOWN));
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

    @Override
    public void onBackPressed() {
        if(mScreenStatus == SCREEN_ID_SERIAL_CONSOLE) {
            finishActivity();
        }
        else {
            Intent intent = new Intent(this, SerialConsoleService.class);
            intent.putExtra(SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_NAME, SerialConsoleService.SERIAL_SERVICE_MESSAGE_TYPE_PARAMETERS);
            startService(intent);
            showSerialConsoleFragment();
        }
    }

    private void finishActivity(){
        if (getParent() == null) {
            setResult(RESULT_OK, new Intent());
        }
        else {
            getParent().setResult(RESULT_OK, new Intent());
        }
        finish();
    }

//    private SerialConsoleService usbService;

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

    private boolean checkWriteReadPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
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
        if(mConsoleFragment != null) {
            Button btnSave = ((Button)mConsoleFragment.getView().findViewById(R.id.btnSave));
            btnSave.setClickable(true);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(mConsoleFragment != null) {
            Button btnSave = ((Button)mConsoleFragment.getView().findViewById(R.id.btnSave));
            btnSave.setClickable(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult " + requestCode);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
