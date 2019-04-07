package in.altilogic.prayogeek.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.common.primitives.Bytes;

import java.util.HashMap;
import java.util.Map;

import in.altilogic.prayogeek.R;
import in.altilogic.prayogeek.fragments.SerialConsoleSettingsFragment;
import in.altilogic.prayogeek.utils.Utils;

public class SerialConsoleService extends Service {

    public static final String TAG = "YOUSCOPE-USB-SERVICE";

    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static final int CTS_CHANGE = 1;
    public static final int DSR_CHANGE = 2;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static boolean SERVICE_CONNECTED = false;
    private static final String DATE_TIME_FORMAT = "[HH:mm:ss.SSS]";

    public static final String SERIAL_SERVICE_MESSAGE_TYPE_NAME = "in.altilogic.prayogeek.serial.message.type";
    public static final String SERIAL_SERVICE_MESSAGE_TYPE_DATA = "in.altilogic.prayogeek.serial.data";
    public static final String SERIAL_SERVICE_MESSAGE_TYPE_TIMESTAMP = "in.altilogic.prayogeek.serial.timestamp";
    public static final String SERIAL_SERVICE_MESSAGE_TYPE_COLOR = "in.altilogic.prayogeek.serial.color";
    public static final int SERIAL_SERVICE_MESSAGE_TYPE_PARAMETERS = 1;
    public static final int SERIAL_SERVICE_MESSAGE_TYPE_READ_DATA = 2;
    public static final int SERIAL_SERVICE_MESSAGE_TYPE_WRITE_DATA = 3;

    public static final int SERIAL_LINE_FEED_NO_ENDING = 0;
    public static final int SERIAL_LINE_FEED_NL = 1;
    public static final int SERIAL_LINE_FEED_CR = 2;
    public static final int SERIAL_LINE_FEED_NLCR = 3;

    public static final int SERIAL_LINE_MAX_STRING_SIZE = 128;

    private int mLineFeed = SERIAL_LINE_FEED_NO_ENDING;

    private IBinder binder = new UsbBinder();

    private Context context;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;

    private boolean serialPortConnected;

    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */
    private static byte[] mLogarray = new byte[SERIAL_LINE_MAX_STRING_SIZE];
    private int mLogArrayOffset = 0;
    private boolean mCR = false;
    private boolean mNL = false;

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            if(arg0 != null) {
                if(mLineFeed == SERIAL_LINE_FEED_NO_ENDING) {
                    notifyAboutNewData(arg0, getResources().getColor(R.color.blue));
                }
                else {
                    boolean isSend = false;
                    for(byte sym : arg0) {
                        if(sym == 0x0D) {
                            mCR = true;
                            mLogarray[mLogArrayOffset++] = sym;
                        } else if(sym == 0x0A ) {
                            mNL = true;
                            mLogarray[mLogArrayOffset++] = sym;
                        }
                        else {
                            mLogarray[mLogArrayOffset++] = sym;
                        }

                        if(mLogArrayOffset >= mLogarray.length) {
                            Log.d(TAG, "Error: mLogArrayOffset > mLogarray.length");
                            mLogArrayOffset = 0;
                            mCR = false;
                            mNL = false;
                            return;
                        }

                        if(mCR && mLineFeed == SERIAL_LINE_FEED_CR) {
                            isSend = true;
                        }
                        else if(mNL && mLineFeed == SERIAL_LINE_FEED_NL) {
                            isSend = true;
                        }
                        else if (mNL && mCR && mLineFeed == SERIAL_LINE_FEED_NLCR ) {
                            isSend = true;
                        }

                        if(isSend) {
                            isSend = false;
                            byte[] data = new byte[mLogArrayOffset];
                            System.arraycopy(mLogarray, 0, data, 0, mLogArrayOffset);
                            notifyAboutNewData(data, getResources().getColor(R.color.blue));
                            mLogArrayOffset = 0;
                            mCR = false;
                            mNL = false;
                        }
                    }
                }
            }
        }
    };

    private void notifyAboutNewData(byte[] logarray, int color) {
        Intent intent = new Intent(SERIAL_SERVICE_MESSAGE_TYPE_NAME);
        intent.putExtra(SERIAL_SERVICE_MESSAGE_TYPE_NAME, SERIAL_SERVICE_MESSAGE_TYPE_READ_DATA);
        intent.putExtra(SERIAL_SERVICE_MESSAGE_TYPE_DATA, logarray);
        intent.putExtra(SERIAL_SERVICE_MESSAGE_TYPE_TIMESTAMP, System.currentTimeMillis());
        intent.putExtra(SERIAL_SERVICE_MESSAGE_TYPE_COLOR, color);
        sendBroadcast(intent);
    }


    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {// User accepted our USB connection. Try to open the device as a serial port
                    Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    arg0.sendBroadcast(intent);
                    connection = usbManager.openDevice(device);
                    new ConnectionThread().start();
                }
                else {// User not accepted our USB connection. Send an Intent to the Main Activity
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    arg0.sendBroadcast(intent);
                }
            }
            else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!serialPortConnected)
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
            }
            else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // Usb device was disconnected. send an intent to the Main Activity
                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                arg0.sendBroadcast(intent);
                if (serialPortConnected) {
                    serialPort.close();
                }
                serialPortConnected = false;
            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        this.context = this;
        serialPortConnected = false;
        SerialConsoleService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int messageType = intent.getIntExtra(SERIAL_SERVICE_MESSAGE_TYPE_NAME, -1);
            Log.d(TAG, "onStartCommand messageType = " + messageType);

            switch (messageType) {
                case SERIAL_SERVICE_MESSAGE_TYPE_WRITE_DATA:
                    byte[] bytes = intent.getByteArrayExtra(SERIAL_SERVICE_MESSAGE_TYPE_DATA);
                    if(serialPort != null && bytes != null && serialPort.isOpen()) {
                        serialPort.write(bytes);
                        notifyAboutNewData(bytes, getResources().getColor(R.color.black_trans80));
                    }
                    break;
                case SERIAL_SERVICE_MESSAGE_TYPE_PARAMETERS:
                    if(serialPort != null && serialPort.isOpen()){
                        serialPort.setBaudRate(getSavedParameter(R.array.baud_rate_array));
                        serialPort.setDataBits(getSavedParameter(R.array.data_bits_array));
                        serialPort.setStopBits(getSavedParameter(R.array.stop_bit_array));
                        serialPort.setParity(getSavedParameter(R.array.parity_check_array));
                        serialPort.setFlowControl(getSavedParameter(R.array.flow_control_array));
                        serialPort.read(mCallback);
                    }

                    if(connection != null)
                        new ConnectionThread().start();

                    break;
                    default:
                        break;
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serialPort.close();
        unregisterReceiver(usbReceiver);
        SerialConsoleService.SERVICE_CONNECTED = false;
    }

    /*
     * This function will be called from MainActivity to write data through Serial Port
     */
    public void write(byte[] data) {
        if (serialPort != null)
            serialPort.write(data);
    }

    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {

            // first, dump the hashmap for diagnostic purposes
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                Log.d(TAG, String.format("USBDevice.HashMap (vid:pid) (%X:%X)-%b class:%X:%X name:%s",
                        device.getVendorId(), device.getProductId(),
                        UsbSerialDevice.isSupported(device),
                        device.getDeviceClass(), device.getDeviceSubclass(),
                        device.getDeviceName()));
            }

            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

//                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003) && deviceVID != 0x5c6 && devicePID != 0x904c) {
                if (UsbSerialDevice.isSupported(device)) {
                    // There is a supported device connected - request permission to access it.
                    requestUserPermission();
                    break;
                } else {
                    connection = null;
                    device = null;
                }
            }
            if (device==null) {
                // There are no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
        } else {
            Log.d(TAG, "findSerialPortDevice() usbManager returned empty device list." );
            // There is no USB devices connected. Send an intent to MainActivity
            Intent intent = new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        Log.d(TAG, String.format("requestUserPermission(%X:%X)", device.getVendorId(), device.getProductId() ) );
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    public class UsbBinder extends Binder {
        public SerialConsoleService getService() {
            return SerialConsoleService.this;
        }
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    mLineFeed = Utils.readSharedSetting(getApplicationContext(), SerialConsoleSettingsFragment.SETTINGS_LINE_FEED, 0 );
                    serialPortConnected = true;
                    serialPort.setBaudRate(getSavedParameter(R.array.baud_rate_array));
                    serialPort.setDataBits(getSavedParameter(R.array.data_bits_array));
                    serialPort.setStopBits(getSavedParameter(R.array.stop_bit_array));
                    serialPort.setParity(getSavedParameter(R.array.parity_check_array));
                    /**
                     * Current flow control Options:
                     * UsbSerialInterface.FLOW_CONTROL_OFF
                     * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
                     * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
                     */
                    serialPort.setFlowControl(getSavedParameter(R.array.flow_control_array));
                    serialPort.read(mCallback);

                    //
                    // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
                    // to be uploaded or not
                    //Thread.sleep(2000); // sleep some. YMMV with different chips.

                    // Everything went as expected. Send an intent to MainActivity
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (serialPort instanceof CDCSerialDevice) {
                        Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        context.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        context.sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(intent);
            }
        }
    }

    private byte getAscii(byte data) {
        if(data < 10)
            return (byte)(0x30 + 0x0f&(data));
        else
            return (byte)(0x41 + 0x0f&(data-10));
    }

    private int getSavedParameter(int param_id) {
        String[] params = getResources().getStringArray(param_id);
        int pos;
        switch (param_id){
            case R.array.baud_rate_array:
                pos = Utils.readSharedSetting(this, SerialConsoleSettingsFragment.SETTINGS_BAUD_RATE, 0 );
                if(pos < params.length){
                    String valueS = params[pos];
                    return Integer.parseInt(valueS);
                }
                break;
            case R.array.data_bits_array:
                pos = Utils.readSharedSetting(this, SerialConsoleSettingsFragment.SETTINGS_DATA_BITS, 0 );
                if(pos < params.length){
                    String valueS = params[pos];
                    return Integer.parseInt(valueS);
                }
                break;
            case R.array.stop_bit_array:
                pos = Utils.readSharedSetting(this, SerialConsoleSettingsFragment.SETTINGS_STOP_BIT, 0 );
                if(pos == 0)
                    return UsbSerialInterface.STOP_BITS_1;
                else if(pos == 1)
                    return UsbSerialInterface.STOP_BITS_15;
                else if(pos == 2)
                    return UsbSerialInterface.STOP_BITS_2;
                break;
            case R.array.parity_check_array:
                pos = Utils.readSharedSetting(this, SerialConsoleSettingsFragment.SETTINGS_PARITY_CHECK, 0 );
                if(pos == 0)
                    return UsbSerialInterface.PARITY_NONE;
                else if(pos == 1)
                    return UsbSerialInterface.PARITY_ODD;
                else if(pos == 2)
                    return UsbSerialInterface.PARITY_EVEN;
                break;
            case R.array.flow_control_array:
                return UsbSerialInterface.FLOW_CONTROL_OFF;
            default:
                break;
        }
        return 0;
    }
}