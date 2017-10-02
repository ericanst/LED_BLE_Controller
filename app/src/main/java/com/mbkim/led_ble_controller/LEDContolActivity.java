package com.mbkim.led_ble_controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mbkim.led_ble_controller.bluetooth.BleManager;


/**
 * Created by mbkim on 2017-09-03.
 */
public class LEDContolActivity extends AppCompatActivity {
    // Bluetooth
    private BleManager mBleManager = null;

    private static final int REQUEST_CONNECT_DEVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mbkim.led_ble_controller.R.layout.activity_led_contol);

        mBleManager = BleManager.getInstance(this, null);
    }

    // 액션버튼 메뉴 맥션바에 집어 넣기
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.mbkim.led_ble_controller.R.menu.menu, menu);
        return true;
    }

    // 액션버튼을 클릭했을때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case com.mbkim.led_ble_controller.R.id.scan_btn:
                // Launch the DeviceListActivity to see devices and do scan
                doScan();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Receives result from external activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if(resultCode == Activity.RESULT_OK){
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    // Attempt to connect to the device.
                    if(address != null) {
                        mBleManager.connectGatt(this, true, address);
                    }
                }
                break;
        }
    }

    /**
     * Launch the DeviceListActivity to see devices and do scan
     */
    private void doScan() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }
}
