package hu.itware.kite.service.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public final class SystemUtils {

	private SystemUtils() {

	}

	/**
	 * Get the IMEI number of the current device.
	 * @param context context f the application
	 * @return the IMEI number (Device Identifier) of the current device.
	 */
	public static String getImei(Context context) {

		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public static String getVersion(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (Exception e) {
			Log.e("SystemUtils", "Could not get version", e);
			return "";
		}

	}

}
