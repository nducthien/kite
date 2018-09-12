package hu.itware.kite.service.receivers;

import hu.itware.kite.service.services.GPSService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartGPSServiceReceiver extends BroadcastReceiver {

	private static final String TAG = "StartGPSServiceReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive()");

		
		context.startService(new Intent(context, GPSService.class));

	}

}
