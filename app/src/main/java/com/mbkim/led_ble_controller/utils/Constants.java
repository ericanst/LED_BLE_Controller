package com.mbkim.led_ble_controller.utils;

/**
 * Created by mbkim on 2017. 10. 8..
 */

public class Constants {
    // Permission state
    public static final int PERMISSION_TRUE = 1;
    public static final int PERMISSION_DEFAULT = 0;
    public static final int PERMISSION_FALSE = -1;

    // Message types sent from Service to Activity
    public static final int MESSAGE_SEND_TO_DEVICE = 100;
    public static final int RECEIVE_BLE_DEVICE_STATE_MESSAGE = 201;
    public static final int LED_INIT_MESSAGE = 300;
    public static final int LED_SETTING_MESSAGE = 301;

    // LED Color
    public static final int LED_RED = 0x09;
    public static final int LED_GREEN = 0x0a;
    public static final int LED_BLUE = 0x0b;

    // Message type sent from Android to Arduino
    // Received message type
    public static final int LED_INFO_MESSAGE = 0x01;
    public static final int LED_STATE_MESSAGE = 0x02;
    // Send message type
    public static final int EACH_LED_ON_OFF_MESSAGE = 0x06;
    public static final int EACH_LED_SETTING_MESSAGE = 0x07;
    public static final int ALL_LED_ON_OFF_MESSAGE = 0x08;
    public static final int REQUEST_DEVICE_INFO_MESSAGE = 0x0f;
}
