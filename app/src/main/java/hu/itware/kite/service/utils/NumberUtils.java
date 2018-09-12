package hu.itware.kite.service.utils;

import android.util.Log;

import java.math.BigDecimal;

/**
 * Created by szeibert on 2015.12.10..
 */
public class NumberUtils {

    private NumberUtils() {

    }

    public static Double parseDouble(String number) {
        try {
            return Double.parseDouble(number);
        } catch (Exception e) {
            Log.w("KITE.UTILS", "Not a number:" + number + ", return with null.");
            return null;
        }
    }

    public static String toPreciseString(Double number, int precision) {

		if (number != null) {
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(number)).setScale(precision, BigDecimal.ROUND_HALF_UP);
            return bigDecimal.toPlainString();
        }
		return null;
    }
}
