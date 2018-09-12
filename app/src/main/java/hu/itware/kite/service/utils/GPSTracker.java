package hu.itware.kite.service.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import hu.itware.kite.service.R;

public class GPSTracker implements LocationListener {

	private final Context mContext;

	private static final String TAG = "KITE.GPS";
	// flag for GPS success
	public boolean isGPSEnabled = false;
	// flag for network success
	boolean isNetworkEnabled = false;
	// flag for GPS success
	boolean canGetLocation = false;
	Location location; // location
	double latitude; // gps2
	double longitude; // gps1
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1L; // 1 minute
	// Declaring a Location Uzletkoto
	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	/**
	 * Function to get the user's current location
	 * 
	 * @return
	 */
	public Location getLocation() {
		try {
			if (!canGetLocation()) {
				return location; // return the last known location
			}
			if (locationManager == null) {
				locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			}
			if (locationManager != null) {
				locationManager.requestLocationUpdates(
					isGPSEnabled ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER,
					MIN_TIME_BW_UPDATES,
					MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				Log.d(TAG, isGPSEnabled ? "GPS" : "Network");
				location = locationManager
						.getLastKnownLocation(isGPSEnabled?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				} else if(isGPSEnabled){
					// try NETWORK_PROVIDER if GPS_PROVIDER location is null
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error getting location", e);
		}
		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get gps2
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		// return gps2
		return latitude;
	}

	/**
	 * Function to get gps1
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		// return gps1
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		if (locationManager == null) {
			locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		}
		// getting GPS success
		isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Log.v(TAG, "=" + isGPSEnabled);
		// getting network success
		isNetworkEnabled = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		Log.v(TAG, "=" + isNetworkEnabled);
		this.canGetLocation = isGPSEnabled || isNetworkEnabled;
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		// Setting Dialog Title
		alertDialog.setTitle(R.string.dialog_gps_settings_title);
		// Setting Dialog Message
		alertDialog.setMessage(R.string.dialog_gps_is_not_enabled_message);
		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});
		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
