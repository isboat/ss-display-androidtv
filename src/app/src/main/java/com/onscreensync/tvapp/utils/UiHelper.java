package com.onscreensync.tvapp.utils;

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
        textView.setTextColor(parseColor(textColor));
    }

    public static int parseColor(String textColor) {

        if(ObjectExtensions.isNullOrEmpty(textColor)) return Color.WHITE;

        switch (textColor.toLowerCase())
        {
            case "red":
                return Color.RED;
            case "blue":
                return Color.BLUE;
            case "grey":
            case "gray":
                return Color.GRAY;
            case "black":
                return Color.BLACK;
            case "yellow":
                return Color.YELLOW;
            default:
                try {
                    return Color.parseColor(textColor.toLowerCase());
                } catch(Exception ex) {
                    return Color.WHITE;
                }
        }
    }
}
