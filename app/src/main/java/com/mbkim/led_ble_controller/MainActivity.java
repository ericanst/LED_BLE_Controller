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
import com.mbkim.led_ble_controller.utils.Constants;

public class MainActivity extends AppCompatActivity implements PermissionControl.Callback{
    private BluetoothManager mBluetoothManager = null;

    private int checkPerm = Constants.PERMISSION_DEFAULT;

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
    }

    @Override
    protected void onResume(){
        super.onResume();

        PermissionControl.checkPermission(this);

        if(checkPerm == Constants.PERMISSION_TRUE){
            Intent intent = new Intent(this, LEDContolActivity.class);
            startActivity(intent);
        } else if(checkPerm == Constants.PERMISSION_FALSE){
            Toast.makeText(this, "권한을 하나라도 허용하지 않으시면 프로그램을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    // PermissionControl에서 호출하는 초기화 홤수.
    @Override
    public void setCheckPerm(int checkPerm) {
        this.checkPerm = checkPerm;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean checkResult = true;

        if (requestCode == PermissionControl.REQ_PERMISSION) {
            // 권한쿼리 결과값을 모두 확인한 후 하나라도 승인되지 않았다면 false를 리턴.
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false;
                    break;
                }
            }

            if (checkResult) {
                checkPerm = Constants.PERMISSION_TRUE;
            } else {
                checkPerm = Constants.PERMISSION_FALSE;
            }
        }
    }
}
