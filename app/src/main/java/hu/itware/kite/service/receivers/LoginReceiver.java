package hu.itware.kite.service.receivers;

import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.settings.Settings;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class LoginReceiver extends BroadcastReceiver {

	private static final String TAG = "LoginReceiver";
	private static final int ALARM_REQUEST_CODE = 1234567;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive()");

		if (LoginService.isLoggedIn(context)) {
			startAlarmManager(context);
		} else {
			stopAlarmManager(context);
		}
	}

	private void startAlarmManager(Context context) {
		Log.i(TAG, "startAlarmManager()");

		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, StartGPSServiceReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, i, 0);

		Log.i(TAG, "alarm interval: " + (Settings.GPS_REFRESH_INTERVAL / 1000 / 60) + " min");

		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Settings.GPS_REFRESH_INTERVAL, pi);

	}

	private void stopAlarmManager(Context context) {
		Log.i(TAG, "stopAlarmManager()");

		Intent intentstop = new Intent(context, StartGPSServiceReceiver.class);
		PendingIntent senderstop = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intentstop, 0);
		AlarmManager alarmManagerstop = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		alarmManagerstop.cancel(senderstop);
	}

}
