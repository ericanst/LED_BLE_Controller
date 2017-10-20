package com.mbkim.led_ble_controller;


import android.graphics.drawable.Drawable;

/**
 * Created by mbkim on 2017-10-17.
 */

public class ListViewLedItem{
    private Drawable iconDrawable = null;
    private Drawable defaultIcon = null;
    private String ledColor = null;
    private String ledState = null;
    private int ledBrightness = 0;
    private int pinNumber = 0;

    public void setIcon(Drawable icon) {
        this.iconDrawable = icon;
    }

    public void setDefaultIcon(Drawable icon) {
        this.defaultIcon = icon;
    }

    public void setLedColor(String color) {
        this.ledColor = color;
    }

    public void setLedState(String state) {
        this.ledState = state;
    }

    public void setLedBrightness(int brightness) {
        this.ledBrightness = brightness;
    }

    public void setPinNumber(int number) {
        this.pinNumber = number;
    }

    public Drawable getIcon() {
        return this.iconDrawable;
    }

    public Drawable getDefaultIcon() {
        return this.defaultIcon;
    }

    public String getLedColor() {
        return this.ledColor;
    }

    public String getLedState() {
        return this.ledState;
    }

    public int getLedBrightness() {
        return this.ledBrightness;
    }

    public int getPinNumber() {
        return this.pinNumber;
    }
}
