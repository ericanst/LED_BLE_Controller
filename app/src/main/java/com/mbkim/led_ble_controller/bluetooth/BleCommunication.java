package com.mbkim.led_ble_controller.bluetooth;


import android.os.Handler;
import android.os.Message;

import com.mbkim.led_ble_controller.ListViewLedItem;
import com.mbkim.led_ble_controller.R;
import com.mbkim.led_ble_controller.utils.Constants;

/**
 * Created by mbkim on 2017. 10. 7..
 */

public class BleCommunication {
    // Define
    private static final int STATE_ERROR = -1;		// Error occurred
    private static final int STATE_NONE = 0;		// Instance created
    private static final int STATE_MESSAGE_BUFFER_INIT = 1;		// Initialize send message
    private static final int STATE_SETTING_FINISHED = 2;	// End of setting parameters
    private static final int STATE_MESSAGE_SEND = 3;	// End of sending transaction data
    private static final int STATE_MESSAGE_RECEIVE = 4;

    // Bluetooth
    private BleManager mBleManger = null;
    private Handler mActivityHandler = null;

    // Transaction parameter
    private int mState = STATE_NONE;
    private byte[] msgBuffer = new byte[1024];
    private int index = 1;

    public BleCommunication(BleManager bm, Handler ActivityHandler) {
        mBleManger = bm;
        this.mActivityHandler = ActivityHandler;
    }

    /**
     * Make new transaction instance.
     */
    public void messageInit() {
        mState = STATE_MESSAGE_BUFFER_INIT;

        for(int i = 0; i < msgBuffer.length; i++){
            msgBuffer[i] = -1;
        }
    }


    /**
     * Send message to remote.
     * @return
     */
    public boolean sendBleMessage(int type, int length, byte[] buffer) {
        int msgSize = length;
        byte[] msg;

        if((buffer == null) || (buffer.length < 1)) {
            return false;
        }

        messageInit();
        setSendMessage(type, msgSize, buffer);

        msg = new byte[msgBuffer[0]];
        for(int i = 0; i < msgBuffer[0]; i++) {
            msg[i] = msgBuffer[i];
        }

        if (mBleManger != null) {
            // Check that we're actually connected before trying anything
            if (mBleManger.getState() == BleManager.STATE_CONNECTED) {
                // Check that there's actually something to send
                if (msgSize > 0) {
                    // Get the message bytes and tell the BleManager to write
                    mBleManger.write(null, msg);

                    mState = STATE_MESSAGE_SEND;

                    return true;
                }

                mState = STATE_ERROR;
            }
        }

        return false;
    }


    /**
     * Set string to send.
     * And ready to send date to remote.
     * @param buffer   String to send.
     * @return
     */
    public void setSendMessage(int type, int length, byte[] buffer) {
        int i = 0;

        index = 1;
        msgBuffer[index] = (byte) type;
        msgBuffer[index] = (byte) (msgBuffer[index] << 4);
        index++;

        while(i < length) {
            msgBuffer[index++] = buffer[i++];
        }

        msgBuffer[0] = (byte) index;
    }

    public void getReceiveMessage(byte[] buffer) {
        int bufferSize = buffer[0];
        byte type;

        mState = STATE_MESSAGE_RECEIVE;
        index = 1;

        messageInit();
        for(int i = 0; i < bufferSize; i++) {
            msgBuffer[i] = buffer[i];
        }

        type = (byte) (msgBuffer[index] >> 4);
        switch(type) {
            case Constants.LED_INFO_MESSAGE:
                int num;
                type = (byte) (type << 4);
                num = type ^ msgBuffer[index++];

                while(index < bufferSize) {
                    for(int i = 0; i < num; i++) {
                        byte[] ledInfo = new byte[2];
                        ledInfo[0] = msgBuffer[index++];    // led pin number
                        ledInfo[1] = msgBuffer[index++];    // led brightness

                        Message msg = mActivityHandler.obtainMessage();
                        msg.what = Constants.LED_INIT_MESSAGE;
                        msg.obj = ledInfo;

                        mActivityHandler.sendMessage(msg);
                    }
                }

                break;
        }
    }
}
