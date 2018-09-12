package hu.itware.kite.service.orm.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by batorig on 2015.09.23..
 */
public class DateUtils {

	/** Long date format with milliseconds */
	public static SimpleDateFormat getDfLong() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}

	/** Short date format without milliseconds */
	public static SimpleDateFormat getDfShort() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}


	public static SimpleDateFormat getDfOnlyDate() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}
	public static final SimpleDateFormat getDfOnlyTime() {
		return new SimpleDateFormat("HH:mm");
	}

	private DateUtils() {

	}

	public static Date getDateFromString(String dateStr) {
		if (dateStr == null) {
			return null;
		}
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
		} catch (Exception e) {
			Log.e("DateUtils", "Error parsing date", e);
			return null;
		}
	}
}
