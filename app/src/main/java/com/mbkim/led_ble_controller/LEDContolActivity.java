package com.mbkim.led_ble_controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mbkim.led_ble_controller.bluetooth.BleCommunication;
import com.mbkim.led_ble_controller.bluetooth.BleManager;
import com.mbkim.led_ble_controller.utils.Constants;

import java.util.ArrayList;


/**
 * Created by mbkim on 2017-09-03.
 */
public class LEDContolActivity extends AppCompatActivity {
    // Defined
    public static final int REQUEST_ENABLE_BT = 1; // must be greater than 0

    private static final int REQUEST_CONNECT_DEVICE = 2;

    // Bluetooth
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BleManager mBleManager = null;
    private BleCommunication mBleCommunication = null;

    // System
    private ActivityHandler mActivityHandler = null;

    // UI
    private ImageView connectionImg = null;
    private TextView connectionText = null;
    private Button DisconnectButt = null;
    private Button allLedSetButt = null;
    private ListView listView = null;
    private ListViewLedAdapter ledAdapter = null;
    private ArrayList<ListViewLedItem> ledItems = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mbkim.led_ble_controller.R.layout.activity_led_contol);

        mActivityHandler = new ActivityHandler();

        // UI
        connectionImg = (ImageView) findViewById(R.id.connection_img);
        connectionText = (TextView) findViewById(R.id.connection_text);

        ledItems = new ArrayList<ListViewLedItem>();
        ledAdapter = new ListViewLedAdapter(this, R.layout.adapter_led, ledItems, mActivityHandler);

        listView = (ListView) findViewById(R.id.adapter_led_list);
        listView.setAdapter(ledAdapter);

        // Initializes Bluetooth adapter.
        mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBleManager = BleManager.getInstance(this, null);

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Can not find BluetoothAdapter.", Toast.LENGTH_SHORT).show();
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        ledAdapter.clear();
        ledAdapter.notifyDataSetChanged();
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
            case REQUEST_ENABLE_BT:
                if (mBleManager.mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "Bluetooth on.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Don't use this app, if you don't selected Bluetooth.", Toast.LENGTH_SHORT).show();
                }

                break;

            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if(resultCode == Activity.RESULT_OK){
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    // Attempt to connect to the device.
                    if(address != null) {
                        mBleManager.setActivityHandler(mActivityHandler);
                        mBleManager.connectGatt(this, true, address);

                        mBleCommunication = new BleCommunication(mBleManager, mActivityHandler);

                        ledAdapter.clear();
                        ledAdapter.notifyDataSetChanged();
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

    class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            byte[] buffer;

            switch(msg.what) {
                case Constants.MESSAGE_SEND_TO_DEVICE:
                    buffer = (byte[]) msg.obj;
                    mBleCommunication.sendBleMessage(msg.arg1, msg.arg2, buffer);   // type, length, buffer

                    break;

                case Constants.RECEIVE_BLE_DEVICE_STATE_MESSAGE:
                    buffer = (byte[]) msg.obj;
                    mBleCommunication.getReceiveMessage(buffer);

                    break;

                case Constants.LED_INIT_MESSAGE:
                    buffer = (byte[]) msg.obj;

                    Drawable drawable = null;
                    String color = null;
                    String state = null;
                    int pinNum = buffer[0];
                    int brightness = buffer[1];


                    // LED color check.
                    if(pinNum == Constants.LED_RED) {
                        color= "LED_RED";
                        drawable = getDrawable(R.drawable.led_on_red);
                    } else if(pinNum == Constants.LED_GREEN) {
                        color = "LED_GREEN";
                        drawable = getDrawable(R.drawable.led_on_green);
                    } else if(pinNum == Constants.LED_BLUE) {
                        color = "LED_BLUE";
                        drawable = getDrawable(R.drawable.led_on_blue);
                    }

                    // LED brightness check.
                    if(brightness > 0) {
                        state = "ON";
                    } else if(brightness == 0){
                        state = "OFF";
                    } else if (brightness < 0) {
                        state = "ON";
                        brightness = brightness + 255;
                    } else {
                        state = "ERROR";
                    }


                    ledAdapter.addItem(drawable, getDrawable(R.drawable.led_off), color, state, brightness, pinNum);
                    ledAdapter.notifyDataSetChanged();

                    break;

                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }
}
