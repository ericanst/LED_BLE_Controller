package com.mbkim.led_ble_controller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mbkim.led_ble_controller.utils.Constants;

import java.util.ArrayList;

/**
 * Created by mbkim on 2017-10-17.
 */

public class ListViewLedAdapter extends ArrayAdapter {
    private int resourceId;
    private ArrayList<ListViewLedItem> listViewLedList = null;
    private LEDContolActivity.ActivityHandler mActivityHandler = null;
    private byte[] buffer = new byte[1024];
    private int index = 0;

    public ListViewLedAdapter(Context context, int resource, ArrayList<ListViewLedItem> objects, LEDContolActivity.ActivityHandler activityHandler) {
        super(context, resource, objects);

        this.resourceId = resource;
        this.listViewLedList = objects;
        mActivityHandler = activityHandler;
    }

    public void addItem(Drawable ledIcon, Drawable defaultIcon, String color, String state, int brightness, int number){
        ListViewLedItem ledItem = new ListViewLedItem();

        ledItem.setIcon(ledIcon);
        ledItem.setDefaultIcon(defaultIcon);
        ledItem.setLedColor(color);
        ledItem.setLedState(state);
        ledItem.setLedBrightness(brightness);
        ledItem.setPinNumber(number);

        listViewLedList.add(ledItem);
    }

    public void msgSendToActivityHandler(int handlerType, int msgType, byte[] buffer){
        Message msg = mActivityHandler.obtainMessage();
        msg.what = handlerType;
        msg.arg1 = msgType;
        msg.arg2 = index;
        msg.obj = buffer;

        mActivityHandler.sendMessage(msg);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final  int pos = position;
        final  Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resourceId, parent, false);
        }

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final ListViewLedItem ledItem = (ListViewLedItem) getItem(position);

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        final TextView colorTextView = (TextView) convertView.findViewById(R.id.led_color);
        final TextView stateTextView = (TextView) convertView.findViewById(R.id.led_info);
        final TextView brightnessTextView = (TextView) convertView.findViewById(R.id.led_brightness);
        final ImageButton ledButton = (ImageButton) convertView.findViewById(R.id.led_butt);
        final SeekBar ledControlBar = (SeekBar) convertView.findViewById(R.id.led_control);

        colorTextView.setText(ledItem.getLedColor());
        stateTextView.setText(ledItem.getLedState());
        brightnessTextView.setText(String.valueOf(ledItem.getLedBrightness()));

        setIcon(ledItem,ledButton);
        ledButton.setBackgroundColor(000000);
        ledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = 0;
                buffer[index++] = (byte) ledItem.getPinNumber();

                if(ledItem.getLedState().equals("ON")) {
                    buffer[index++] = 0x00;   // Brightness = 0

                    ledItem.setLedState("OFF");
                    stateTextView.setText(ledItem.getLedState());
                    ledControlBar.setProgress(0x00);
                    ledButton.setImageDrawable(ledItem.getDefaultIcon());
                } else if(ledItem.getLedState().equals("OFF")) {
                    int brightness = ledItem.getLedBrightness();

                    if(brightness > 0) {
                        buffer[index++] = (byte) brightness;
                    } else if(brightness == 0) {
                        buffer[index++] = (byte) 0x7f;
                        ledItem.setLedBrightness(0x7f);
                    }

                    ledItem.setLedState("ON");
                    stateTextView.setText(ledItem.getLedState());
                    ledControlBar.setProgress(ledItem.getLedBrightness());
                    setIcon(ledItem, ledButton);
                }

                msgSendToActivityHandler(Constants.MESSAGE_SEND_TO_DEVICE, Constants.EACH_LED_ON_OFF_MESSAGE, buffer);
            }
        });

        ledControlBar.setProgress(ledItem.getLedBrightness());
        ledControlBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessTextView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {   }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                index = 0;

                ledItem.setLedBrightness(seekBar.getProgress());    //

                buffer[index++] = (byte) ledItem.getPinNumber();
                buffer[index++] = (byte) ledItem.getLedBrightness();

                if(ledItem.getLedBrightness() == 0){
                    ledItem.setLedState("OFF");
                } else {
                    ledItem.setLedState("ON");
                }
                stateTextView.setText(ledItem.getLedState());
                setIcon(ledItem, ledButton);

                msgSendToActivityHandler(Constants.MESSAGE_SEND_TO_DEVICE, Constants.EACH_LED_SETTING_MESSAGE, buffer);
            }
        });

        return  convertView;
    }

    public void setIcon(ListViewLedItem ledItem, ImageButton ledButton){
        if(ledItem.getLedBrightness() == 0) {
            ledButton.setImageDrawable(ledItem.getDefaultIcon());
        } else {
            ledButton.setImageDrawable(ledItem.getIcon());
        }
    }
}
