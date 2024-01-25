package com.example.screenservicetvapp.utils;

import android.graphics.Color;
import android.widget.TextView;

public class UiHelper {

    public static void setTextViewFont(TextView textView, String textFont) {
        if(ObjectExtensions.isNullOrEmpty(textFont)) return;

        int toInt = ObjectExtensions.convertToInt(textFont);
        if(toInt == 0) return;

        textView.setTextSize(toInt);
    }
    public static void setTextViewColor(TextView textView, String textColor) {

        if(ObjectExtensions.isNullOrEmpty(textColor)) return;

        switch (textColor.toLowerCase())
        {
            case "red":
                textView.setTextColor(Color.RED);
                break;
            case "blue":
                textView.setTextColor(Color.BLUE);
                break;
            case "grey":
            case "gray":
                textView.setTextColor(Color.GRAY);
                break;
            case "black":
                textView.setTextColor(Color.BLACK);
                break;
            case "yellow":
                textView.setTextColor(Color.YELLOW);
                break;
            default:
                try {
                    textView.setTextColor(Color.parseColor(textColor.toLowerCase()));
                } catch(Exception ex) {
                    textView.setTextColor(Color.WHITE);
                }
                break;
        }
    }
}
