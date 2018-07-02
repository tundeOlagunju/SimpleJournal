package com.example.android.simple_journal.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.example.android.simple_journal.R;

public class ColorUtils {

    public static int getViewHolderBackgroundColorFromInstance(Context context, int instanceNum) {
        switch (instanceNum % 9) {
            case 0:
                return ContextCompat.getColor(context, R.color.purpleLight);
            case 1:
                return ContextCompat.getColor(context, R.color.yellowLight);
            case 2:
                return ContextCompat.getColor(context, R.color.blueGrayLight);
            case 3:
                return ContextCompat.getColor(context, R.color.redLight);
            case 4:
                return ContextCompat.getColor(context, R.color.greenLight);
            case 5:
                return ContextCompat.getColor(context, R.color.orangeLight);
            case 6:
                return ContextCompat.getColor(context, R.color.limeLight);
            case 7:
                return ContextCompat.getColor(context, R.color.blueLight);
            case 8:
                return ContextCompat.getColor(context, R.color.grayLight);
            case 9:
                return ContextCompat.getColor(context, R.color.purpleLight);
            case 10:
                return ContextCompat.getColor(context, R.color.yellowLight);
            case 11:
                return ContextCompat.getColor(context, R.color.blueGrayLight);
            case 12:
                return ContextCompat.getColor(context, R.color.redLight);
            case 13:
                return ContextCompat.getColor(context, R.color.greenLight);
            case 14:
                return ContextCompat.getColor(context, R.color.orangeLight);
            case 15:
                return ContextCompat.getColor(context, R.color.limeLight);
            case 16:
                return ContextCompat.getColor(context, R.color.blueLight);
            case 17:
                return ContextCompat.getColor(context, R.color.grayLight);

            default:
                return  R.color.orangeLight ;
        }
    }
}
