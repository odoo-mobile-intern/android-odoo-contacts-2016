package com.odoo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Base64;

import com.odoo.R;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    /**
     * Gets the bitmap image.
     *
     * @param context the context
     * @param base64  the base64
     * @return the bitmap image
     */
    public static Bitmap getBitmapImage(Context context, String base64) {
        byte[] imageAsBytes = Base64.decode(base64.getBytes(), 5);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0,
                imageAsBytes.length);

    }

    public static Bitmap getAlphabetImage(Context context, String content) {
        Resources res = context.getResources();
        Bitmap mDefaultBitmap = BitmapFactory.decodeResource(res, android.R.drawable.sym_def_app_icon);
        int width = mDefaultBitmap.getWidth();
        int height = mDefaultBitmap.getHeight();
        TextPaint mPaint = new TextPaint();
        mPaint.setTypeface(Typeface.create("sans-serif-condensed", 0));
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);
        int textSize = res.getDimensionPixelSize(R.dimen.text_size_large);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        Rect mBounds = new Rect();
        canvas.setBitmap(bitmap);
        canvas.drawColor(OStringColorUtil.getStringColor(context, content));
        if (content == null || content.trim().length() == 0) {
            content = "?";
        }
        char[] alphabet = {Character.toUpperCase(content.trim().charAt(0))};
        mPaint.setTextSize(textSize);
        mPaint.getTextBounds(alphabet, 0, 1, mBounds);
        canvas.drawText(alphabet, 0, 1, 0 + width / 2,
                0 + height / 2 + (mBounds.bottom - mBounds.top) / 2, mPaint);
        return bitmap;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, 0);
    }
}