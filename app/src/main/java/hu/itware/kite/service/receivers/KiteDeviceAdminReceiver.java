package hu.itware.kite.service.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by batorig on 2016.03.01..
 */
public class KiteDeviceAdminReceiver extends DeviceAdminReceiver {

	private static final String TAG = "DeviceAdmin";

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i(TAG, "KiteDeviceAdminReceiver.onReceive()=" + intent);

	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		Log.i(TAG, "KiteDeviceAdminReceiver.onEnabled()=" + intent);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		Log.i(TAG, "KiteDeviceAdminReceiver.onDisabled()=" + intent);
	}

}
