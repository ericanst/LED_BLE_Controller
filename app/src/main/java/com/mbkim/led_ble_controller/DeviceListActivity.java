package com.mbkim.led_ble_controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.mbkim.led_ble_controller.bluetooth.BleManager;
import com.mbkim.led_ble_controller.bluetooth.Device;

import java.util.List;
import java.util.Set;

/**
 * Created by mbkim on 2017-09-03.
 */
@TargetApi(21)
public class DeviceListActivity extends Activity{
    //
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final long SCAN_PERIOD = 10000;   // Stops scanning after a pre-defined scan period.

    // Member fields
    private Handler mHandler = null;

    private BluetoothAdapter mBtAdapter = null;
    private BleManager mBleManager = null;

    private ArrayAdapter<String> mPairedDevicesArrayAdapter = null;
    private ArrayAdapter<Device> mNewDevicesArrayAdapter = null;

    // UI stuff
    private Button mScanButton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mbkim.led_ble_controller.R.layout.activity_device_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        mHandler = new Handler();

        mScanButton = (Button) findViewById(com.mbkim.led_ble_controller.R.id.scanning_btn);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBleManager.getState() == mBleManager.STATE_SCANNING){
                    stopDiscovery();
                } else {
                    mNewDevicesArrayAdapter.clear();
                    doDiscovery();
                }

            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, com.mbkim.led_ble_controller.R.layout.adapter_device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<Device>(this, com.mbkim.led_ble_controller.R.layout.adapter_device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(com.mbkim.led_ble_controller.R.id.paired_devices_list);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(com.mbkim.led_ble_controller.R.id.new_devices_list);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get BLE Manager
        mBleManager = BleManager.getInstance(getApplicationContext(), mHandler);

        if(Build.VERSION.SDK_INT < 21){
            mBleManager.setScanCallback(LeScanCallback);
        } else {
            mBleManager.setScanCallback(scanCallback);
        }

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add("noDevices");
        }
    }


    /**
     * Destroy 된 Activity를 다시 시작하기 : 정상적인 앱의 실행 중에 Activity가 Destroy 되는 시나리오가 몇가지가 있다.
     *   사용자가 Back 버튼을 눌렀을 때
     *   Activity 안에서 finish() 함수를 호출하여 자체적으로 종료할 때
     *   Stop 된 상태에서 오랫동안 사용하지 않을 때
     *   Stop 된 상태에서 전면에 있는 Activity가 더 많은 리소스가 필요할 때 메모리 확보를 위해
     *   사용자가 화면을 회전 시켰을 때
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Make sure we're not doing discovery anymore.
        if(mBtAdapter != null){
            mBleManager.scanLeDevice(false);
            mBtAdapter.cancelDiscovery();
        }
    }


    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    // Cancel discovery because it's costly and we're about to connect
                    mBtAdapter.cancelDiscovery();

                    // Get the device MAC address.
                    String address = ((Device) adapterView.getItemAtPosition(position)).getMAC();

                    if(address != null) {
                        // Create the result Intent and include the MAC address
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                        // Set result and finish this Activity
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            };

    /**
     * Gets the scanning result after scanBledDevice method running finished.
     */
    private ScanCallback scanCallback = new ScanCallback() {
        // In this case, gets the one device information when discovered one device.
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Device sd = null;
            boolean check = true;
            int count = mNewDevicesArrayAdapter.getCount();

            System.out.println("========== onScanResult ==========");

            if(count != 0){
                for(int i = 0; i < count; i++){
                    sd = mNewDevicesArrayAdapter.getItem(i);

                    if(sd.getMAC().equals(result.getDevice().getAddress())){
                        if(sd.getRSSI() != result.getRssi()){
                            sd.setRSSI(result.getRssi());
                        }
                        check = false;
                        break;
                    }
                }
            }

            if(check){
                Device scanDevice = new Device(result);
                mNewDevicesArrayAdapter.add(scanDevice);
            }

            mNewDevicesArrayAdapter.notifyDataSetChanged();
        }

        // In this case, gets the device list when discovered devices.
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            System.out.println("======== onBatchScanResult ========");
        }

        @Override
        public void onScanFailed(int errorCode) {
            System.out.println("========= onScanFailed ===========");
        }
    };

    /**
     * If SDK_INT < 21.
     */
    private BluetoothAdapter.LeScanCallback LeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("========== LeScanCallback ==========");
                }
            });
        }
    };

    /**
     * Start device discover with the BluetoothAdapter.
     */
    private void doDiscovery(){
        setUI(true);

        // Request discover from BluetoothAdapter
        mBleManager.scanLeDevice(true);

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopDiscovery();
            }
        }, SCAN_PERIOD);
    }


    private void stopDiscovery() {
        setUI(false);

        mBleManager.scanLeDevice(false);
    }


    private void setUI(boolean state){
        if(state) {
            // Indicate scanning in the title
            setTitle("BLE device scanning");

            mScanButton.setText("Device scan stop");
        } else {
            // Indicate scanning in the title
            setTitle("BLE device scanner");

            mScanButton.setText("Device scan start");
        }
    }
}
