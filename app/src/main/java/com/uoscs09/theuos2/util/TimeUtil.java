package com.uoscs09.theuos2.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeUtil {
    /**
     * date format - a hh:mm:ss, Locale : default
     */
    public static DateFormat getFormat_am_hms() {
        return new SimpleDateFormat("a hh:mm:ss", Locale.getDefault());
    }

    public static DateFormat getFormat_yMd_kms() {
        return new SimpleDateFormat("yyyy-MM-dd  kk:mm:ss", Locale.getDefault());
    }

}
