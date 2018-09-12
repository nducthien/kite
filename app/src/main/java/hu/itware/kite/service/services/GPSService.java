package hu.itware.kite.service.services;

import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.GPSData;
import hu.itware.kite.service.utils.GPSTracker;
import hu.itware.kite.service.utils.SystemUtils;

import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GPSService extends IntentService {

	private static final String TAG = "GPSService";

	public GPSService() {
		super("KITE.GPSSERVICE");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		boolean db_ok = getSharedPreferences("DBSTATE", Context.MODE_PRIVATE).getBoolean("DBINSTALLED", false);
		if (!db_ok) {
			return;
		}
		
		GPSTracker tracker = new GPSTracker(this);
		if (LoginService.isLoggedIn(this) && LoginService.getManager(this) != null) {
			Log.i(TAG, "coordinates: " + tracker.getLatitude() + ", " + tracker.getLongitude());
			
			
			
			KiteORM helper = new KiteORM(this);
			GPSData mGpsData = new GPSData();
			mGpsData.date = new Date();
			mGpsData.modified = new Date();
			Log.i(TAG, "canGetLocation: " + tracker.canGetLocation());
			if (tracker.canGetLocation()) {
				// TODO save GPS coordinates to DB
				mGpsData.latitude = tracker.getLatitude();
				mGpsData.longitude = tracker.getLongitude();
			} else {
				// TODO save that no GPS coordinates
				mGpsData.latitude = 0.0;
				mGpsData.longitude = 0.0;
			}
			
			mGpsData.imei = SystemUtils.getImei(this);
			helper.insert(mGpsData);
		}

		tracker.stopUsingGPS();
	}

}
