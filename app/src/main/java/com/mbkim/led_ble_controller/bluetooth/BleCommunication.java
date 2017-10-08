package com.mbkim.led_ble_controller.bluetooth;


import android.os.Handler;
import android.os.Message;

import com.mbkim.led_ble_controller.utils.Constants;

/**
 * Created by mbkim on 2017. 10. 7..
 */

public class BleCommunication {
    // Define
    private static final int STATE_ERROR = -1;		// Error occurred
    private static final int STATE_NONE = 0;		// Instance created
    private static final int STATE_SEND_MESSAGE_INIT = 1;		// Initialize send message
    private static final int STATE_RECEIVE_MESSAGE_INIT = 2;		// Initialize receive message
    private static final int STATE_SETTING_FINISHED = 3;	// End of setting parameters
    private static final int STATE_TRANSFERED = 4;	// End of sending transaction data
    private static final int STATE_RECEIVED = 5;

    // Bluetooth
    private BleManager mBleManger = null;
    private Handler mActivityHandler = null;

    // Transaction parameter
    private int mState = STATE_NONE;
    private byte[] sendBuffer = null;
    private byte[] receiveBuffer = null;
    private String sendMsg = null;
    private String receiveMsg = null;

    public BleCommunication(BleManager bm, Handler ActivityHandler) {
        mBleManger = bm;
        this.mActivityHandler = ActivityHandler;
    }

    /**
     * Make new transaction instance.
     */
    public void sendMessageInit() {
        mState = STATE_SEND_MESSAGE_INIT;
        sendBuffer = null;
        sendMsg = null;
    }


    public void receiveMessageInit(){
        mState = STATE_RECEIVE_MESSAGE_INIT;
        receiveBuffer = null;
        receiveMsg = null;
    }


    /**
     * Send message to remote.
     * @return
     */
    public boolean sendBleMessage(String msg) {
        if((msg == null) || (msg.length() < 1)) {
            return false;
        }

        setSendMessage(msg);

        if (mBleManger != null) {
            // Check that we're actually connected before trying anything
            if (mBleManger.getState() == BleManager.STATE_CONNECTED) {
                // Check that there's actually something to send
                if (sendBuffer.length > 0) {
                    // Get the message bytes and tell the BleManager to write
                    mBleManger.write(null, sendBuffer);

                    mState = STATE_TRANSFERED;

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
     * @param msg   String to send.
     * @return
     */
    public void setSendMessage(String msg) {
        this.sendMsg = msg;
        this.sendBuffer = this.sendMsg.getBytes();
        mState = STATE_SETTING_FINISHED;
    }
}
