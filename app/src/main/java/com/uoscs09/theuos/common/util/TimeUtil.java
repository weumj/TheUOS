package com.uoscs09.theuos.common.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeUtil {
	/** date format - a hh:mm:ss, Locale : default */
	public static final SimpleDateFormat sFormat_am_hms = new SimpleDateFormat(
			"a hh:mm:ss", Locale.getDefault());
}
