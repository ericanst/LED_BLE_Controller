package com.mbkim.led_ble_controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mbkim.led_ble_controller.bluetooth.BleCommunication;
import com.mbkim.led_ble_controller.bluetooth.BleManager;
import com.mbkim.led_ble_controller.utils.Constants;


/**
 * Created by mbkim on 2017-09-03.
 */
public class LEDContolActivity extends AppCompatActivity implements View.OnClickListener{
    // Defined
    private static final int REQUEST_CONNECT_DEVICE = 1;

    // Bluetooth
    private BleManager mBleManager = null;
    private BleCommunication mBleCommunication = null;

    // System
    private ActivityHandler mActivityHandler = null;

    // UI
    private TextView textView = null;
    private ImageButton imageButton = null;
    private SeekBar seekBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mbkim.led_ble_controller.R.layout.activity_led_contol);

        mActivityHandler = new ActivityHandler();

        mBleManager = BleManager.getInstance(this, null);
    }

    // 액션버튼 메뉴 션바에 집어 넣기
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

                        mBleManager.setActivityHandler(mActivityHandler);
                        mBleCommunication = new BleCommunication(mBleManager, mActivityHandler);
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

    @Override
    public void onClick(View view) {
        String sendStr = "hihihi";
        int id = view.getId();

        switch(id) {
            case R.id.led_butt:
                Message msg = mActivityHandler.obtainMessage();
                msg.what = Constants.MESSAGE_SEND_TO_DEVICE;
                msg.obj = sendStr;

                mActivityHandler.sendMessage(msg);
                break;
        }
    }

    class ActivityHandler extends Handler {
        byte[] buffer = null;
        StringBuilder sb = new StringBuilder();

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constants.MESSAGE_SEND_TO_DEVICE:
                    mBleCommunication.sendMessageInit();
                    mBleCommunication.sendBleMessage((String) msg.obj);
                    break;
                case Constants.RECEIVE_CONNECTION_MESSAGE:
                    mBleCommunication.receiveMessageInit();

                    break;
                case Constants.RECEIVE_BLE_DEVICE_STATE_MESSAGE:
                    buffer = (byte[]) msg.obj;
                    for(final byte b: buffer)
                        sb.append(String.format("%02x ", b & 0xff));
                    System.out.println(sb);
                    System.out.println(new String(buffer));
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }
}
