package com.mbkim.led_ble_controller.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * Created by mbkim on 2017-09-05.
 */
public class Device {
    private BluetoothDevice mBluetoothDevice = null;

    private String name = null;
    private String MAC = null;
    private int RSSI = 0;

    public Device(ScanResult result){
        mBluetoothDevice= result.getDevice();

        this.name = mBluetoothDevice.getName();
        this.MAC = mBluetoothDevice.getAddress();
        this.RSSI = result.getRssi();
    }

    public void setRSSI(int RSSI){
        if(this.RSSI != RSSI){
            this.RSSI = RSSI;
        }
    }

    public BluetoothDevice getBluetoothDevice(){
        return this.mBluetoothDevice;
    }

    public String getName(){
        return this.name;
    }

    public String getMAC(){
        return this.MAC;
    }

    public int getRSSI(){
        return this.RSSI;
    }

    public String toString(){
        return this.name + "\n" + this.MAC + "     (" + this.RSSI + " dbm)";
    }
}
