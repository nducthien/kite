package hu.itware.kite.service.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class Network {
	
	public static final int STATE_NONETWORK = 0;
	
	public static final int STATE_WIFI 	 = 1;
	
	public static final int STATE_MOBILE = 2;
	

	private Network() {
	}

	/** Check witch connection type is available
	 * @param context application context
	 * @return connected network state: STATE_NONETWORK, STATE_WIFI, STATE_MOBILE
	 */
	public static int getNetworkState(Context context) {
		
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//For 3G check
		NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (wifi.isAvailable() && wifi.isConnectedOrConnecting()) {
			return STATE_WIFI;
		}
		
		if (mobile != null && mobile.isAvailable() && mobile.isConnectedOrConnecting()) {
			return STATE_MOBILE;
		}
		
		return STATE_NONETWORK;
	}
}
