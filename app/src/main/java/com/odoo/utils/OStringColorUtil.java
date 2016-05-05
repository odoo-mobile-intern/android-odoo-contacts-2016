package com.odoo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;

import com.odoo.R;

import java.util.Locale;

/**
 * Created by sha on 3/5/16.
 */
public class OStringColorUtil {

    public static int getStringColor(Context context, String content) {
        Resources res = context.getResources();
        TypedArray mColors = res.obtainTypedArray(R.array.letter_tile_colors);
        int MAX_COLORS = mColors.length();
        int firstCharAsc = content.toUpperCase(Locale.getDefault()).charAt(0);
        int index = (firstCharAsc % MAX_COLORS);
        if (index > MAX_COLORS - 1) {
            index = index / 2;
        }
        int color = mColors.getColor(index, Color.WHITE);
        mColors.recycle();
        return color;
    }

}
