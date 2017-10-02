package com.mbkim.led_ble_controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mbkim.led_ble_controller.bluetooth.BleManager;

public class MainActivity extends AppCompatActivity implements PermissionControl.Callback{
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;
    private BleManager mBleManager = null;

    private boolean checkPerm = false;

    private static final int REQUEST_ENABLE_BT = 1; // must be greater than 0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mbkim.led_ble_controller.R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    @Override
    protected void onResume(){
        super.onResume();
        PermissionControl.checkPermission(this);

        if(checkPerm){
            checkPerm = false;

            Intent intent = new Intent(this, LEDContolActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public Activity getActivity() {
        return this;
    }

    // PermissionControl에서 호출하는 초기화 홤수.
    @Override
    public void init() {
        // Bluetooth support and on/off condition check
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Can not find BluetoothAdapter.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                checkPerm = true;
            }
        }
    }
}
