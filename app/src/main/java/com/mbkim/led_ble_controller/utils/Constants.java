package com.mbkim.led_ble_controller.utils;

/**
 * Created by mbkim on 2017. 10. 8..
 */

public class Constants {
    //
    public static final int PERMISSION_TRUE = 1;
    public static final int PERMISSION_DEFAULT = 0;
    public static final int PERMISSION_FALSE = -1;

    // Message types sent from Service to Activity
    public static final int MESSAGE_SEND_TO_DEVICE = 100;
    public static final int RECEIVE_CONNECTION_MESSAGE = 200;
    public static final int RECEIVE_BLE_DEVICE_STATE_MESSAGE = 201;
}
