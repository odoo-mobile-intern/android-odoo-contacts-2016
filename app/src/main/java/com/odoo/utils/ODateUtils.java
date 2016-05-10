package com.odoo.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ODateUtils {

    public static final String TAG = ODateUtils.class.getCanonicalName();
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static String getCurrentDateTime() {
        Date date = new Date();
        return createDate(date, DEFAULT_FORMAT, false);
    }

    public static String parseDate(String dateTime, String dateFormat, String toFormat) {
        return createDate(createDateObject(dateTime, dateFormat, false), toFormat, true);
    }

    public static Date createDateObject(String date, String dateFormat, boolean hasDefaultTimezone) {
        Date dateObj = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            if (!hasDefaultTimezone) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            dateObj = simpleDateFormat.parse(date);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return dateObj;
    }

    private static String createDate(Date date, String defaultFormat, Boolean utc) {
        SimpleDateFormat gmtFormat = new SimpleDateFormat();
        gmtFormat.applyPattern(defaultFormat);
        TimeZone gmtTime = (utc) ? TimeZone.getTimeZone("GMT") : TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    public static String convertToUTC(String dateTime, String dateFormat) {
        return createDate(createDateObject(dateTime, dateFormat, true), dateFormat, true);
    }

}
