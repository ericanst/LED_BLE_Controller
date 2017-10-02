package com.mbkim.led_ble_controller.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbkim on 2017-09-04.
 */
public class BleManager {
    // Bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner BLEScanner = null;
    private ScanCallback scanCallback = null;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;

    private BluetoothDevice mDefaultDevice = null;

    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattService mDefaultService = null;
    private BluetoothGattCharacteristic mDefaultChar = null;
    private ArrayList<BluetoothGattService> mGattServices = new ArrayList<BluetoothGattService>();
    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
    private ArrayList<BluetoothGattCharacteristic> mWritableCharacteristics = new ArrayList<BluetoothGattCharacteristic>();

    // System, Management
    private static BleManager mBleManager = null;
    private static Context mContext = null;
    private Handler mHandler = null;

    // Parameters
    private int mState = -1;

    // Constants that indicate the current connection state.
    public static final int STATE_NONE = 0; //Initialized
    public static final int STATE_IDLE = 1;		// Not connected
    public static final int STATE_SCANNING = 2; // Scanning
    public static final int STATE_CONNECTING = 13;	// Connecting
    public static final int STATE_CONNECTED = 16;   // Connected

    private static final long SCAN_PERIOD = 10000;   // Stops scanning after a pre-defined scan period.


    /**
     * Constructor. Prepares a new Bluetooth session.
     * @param context  The UI Activity Context
     * @param handler  A Listener to receive messages back to the UI Activity
     */
    private BleManager(Context context, Handler handler) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mState = STATE_NONE;
        mHandler = handler;
        mContext = context;

        if(mHandler == null){
            mHandler = new Handler();
        }

        if(mContext == null){
            return;
        }

    }


    public synchronized static BleManager getInstance(Context c, Handler h) {
        if(mBleManager == null) {
            mBleManager = new BleManager(c, h);
        }

        return mBleManager;
    }


    public boolean scanLeDevice(final boolean enable){
        boolean isScanStarted = false;

        if(enable){
            if(mState == STATE_SCANNING){
                return false;
            }

            mState = STATE_SCANNING;

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mState = STATE_IDLE;

                    if(Build.VERSION.SDK_INT < 21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        BLEScanner.stopScan(scanCallback);
                    }
                }
            }, SCAN_PERIOD);

            isScanStarted = true;

            if(Build.VERSION.SDK_INT < 21){
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                BLEScanner.startScan(scanCallback);
            }
        } else {
            if(mState < STATE_CONNECTING) {
                mState = STATE_IDLE;
            }

            mState = STATE_IDLE;

            if(Build.VERSION.SDK_INT < 21){
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                BLEScanner.stopScan(scanCallback);
            }
        }

        return isScanStarted;
    }


    /**
     * In case is received of the device object.
     */
    public boolean connectGatt(Context c, boolean bAutoReconnect, BluetoothDevice device) {
        if(c == null || device == null) {
            return false;
        }

        mGattServices.clear();
        mGattCharacteristics.clear();
        mWritableCharacteristics.clear();

        mBluetoothGatt = device.connectGatt(c, bAutoReconnect, mGattCallback);
        mDefaultDevice = device;

        mState = STATE_CONNECTING;

        return true;
    }


    /**
     * in case is received of the device address.
     */
    public boolean connectGatt(Context c, boolean bAutoReconnect, String address) {
        if ((c == null) || (address == null)) {
            return false;
        }

        if ((mBluetoothGatt != null)
                && (mDefaultDevice != null)
                && (address.equals(mDefaultDevice.getAddress()))) {
            if (mBluetoothGatt.connect()) {
                mState = STATE_CONNECTING;
                return true;
            }
        }

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (device == null) {
            return false;
        }

        mGattServices.clear();
        mGattCharacteristics.clear();
        mWritableCharacteristics.clear();

        mBluetoothGatt = device.connectGatt(c, bAutoReconnect, mGattCallback);
        mDefaultDevice = device;

        mState = STATE_CONNECTING;

        return true;
    }


    // Various callback method defined my the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mState = STATE_CONNECTED;

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mState = STATE_IDLE;

                mBluetoothGatt = null;
                mDefaultChar = null;
                mDefaultDevice = null;
                mDefaultService = null;

                mGattServices.clear();
                mGattCharacteristics.clear();
                mWritableCharacteristics.clear();
            }
        }

        // New service discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                checkGattServices(gatt.getServices());
            }
        }

        // Result of a characteristic read operation.
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // We've received data form remote.
                System.out.println(characteristic.toString());

                /*
                 * onCharacteristicChanged callback receives ame message
                 *
                final byte[] data = characteristic.getValue();
                if ((data != null) && (data.length > 0)) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    //for(byte byteChar : data)
                    //	stringBuilder.append(String.format("%02X ", byteChar));

                    stringBuilder.append(data);
                }

                if((mDefaultChar == null) && (isWritableCharacteristic(characteristic))) {
                    mDefaultChar = characteristic;
                }
                */
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // We've received data form remote.
            System.out.println(characteristic.toString());

            final byte[] data = characteristic.getValue();
            if ((data != null) && (data.length > 0)) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                //for(byte byteChar : data)
                //	stringBuilder.append(String.format("%02X ", byteChar));

                stringBuilder.append(data);
            }

            if ((mDefaultChar == null) && (isWritableCharacteristic(characteristic))) {
                mDefaultChar = characteristic;
            }
        }
    };


    /**
     * Check services and looking for writable characteristics.
     */
    private int checkGattServices(List<BluetoothGattService> gattServices) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return -1;
        }

        for (BluetoothGattService gattService : gattServices) {
            // Remember service.
            mGattServices.add(gattService);

            // Extract characteristics.
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                // Remember characteristic
                mGattCharacteristics.add(gattCharacteristic);

                boolean isWritable = isWritableCharacteristic(gattCharacteristic);
                if (isWritable) {
                    mWritableCharacteristics.add(gattCharacteristic);
                }

                boolean isReadable = isReadableCharavteristic(gattCharacteristic);
                if (isReadable) {
                    readCharacteristic(gattCharacteristic);
                }

                if (isNotificationCharacteristic(gattCharacteristic)) {
                    setCharacteristicNotification(gattCharacteristic, true);

                    if (isWritable && isReadable) {
                        mDefaultChar = gattCharacteristic;
                    }
                }
            }
        }

        return mWritableCharacteristics.size();
    }


    private boolean isWritableCharacteristic(BluetoothGattCharacteristic chr) {
        if (chr == null) {
            return false;
        }

        final int charaProp = chr.getProperties();
        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
            return true;
        } else {
            return false;
        }
    }


    private boolean isReadableCharavteristic(BluetoothGattCharacteristic chr) {
        if (chr == null) {
            return false;
        }

        final int charaProp = chr.getProperties();
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            return true;
        } else {
            return false;
        }
    }


    private boolean isNotificationCharacteristic(BluetoothGattCharacteristic chr) {
        if (chr == null) {
            return false;
        }

        final int charaProp = chr.getProperties();
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }


    /**
     * If SDK_INT >= 21.
     */
    public void setScanCallback(ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    /**
     * If SDK_INT < 21.
     */
    public void setScanCallback(BluetoothAdapter.LeScanCallback mLeScanCallback) {
        this.mLeScanCallback = mLeScanCallback;
    }

    public int getState(){
        return mState;
    }
}
