package hu.itware.kite.service.activity;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.R;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.fragments.ConfirmDialog;
import hu.itware.kite.service.fragments.ErrorDialog;
import hu.itware.kite.service.fragments.IDialogResult;
import hu.itware.kite.service.fragments.MunkalapSummaryFragment;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.receivers.LoginReceiver;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.services.NetworkStateTimerTask;
import hu.itware.kite.service.utils.GPSTracker;

public abstract class BaseActivity extends FragmentActivity implements
		IDialogResult {

	public static final String NETWORK_STATE_CHECK_INTERVAL_KEY = "NWCHECK";
	public static final long NETWORK_STATE_CHECK_INTERVAL_DEFAULT = 10000;
	public static final String TAG = "KITE.UI.SYNC";
	public int dialogType = 0;
	public static final int LOGOUT_DIALOG = 1;
	public static final int SYNC_DIALOG = 2;
	public static final int GPS_DIALOG = 3;
	public static final int OPEN_MUNKALAP_DIALOG = 4;
	public static final int REQUEST_READ_PHONE_STATE =5;
	Toast toast;

	public static SimpleDateFormat getSdfShort(){
		return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	}

	public static SimpleDateFormat getSdf() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
	}

	public static SimpleDateFormat getSdfPicture() {
		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
	}

	public static SimpleDateFormat getSdfTime() {
		return new SimpleDateFormat("HH:mm", Locale.getDefault());
	}

	public static DecimalFormat getDf() {
		return new DecimalFormat("#,###,###");
	}
	public static DecimalFormat getDfLong() {
		return new DecimalFormat("#,###,###.##");
	}

	private GpsBroadcastReceiver receiver;

	private GPSTracker tracker;

	ErrorDialog errorDialog;
	ConfirmDialog confirmDialog;

	MenuItem openItem;
	Menu optionsMenu;

	private Timer timer;
	protected NetworkStateTimerTask networkStateTimerTask;

	private class GpsBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Log.i(TAG, "onReceive=" + intent);
			if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent
					.getAction())) {
				checkGPS(context);
			}
		}
	}

	private void checkGPS(Context context) {
		if (tracker == null) {
			tracker = new GPSTracker(context);
		}
		if (!tracker.canGetLocation()) {
			Log.i(TAG, "errorDialog= " + errorDialog);
			if (errorDialog == null) {
				dialogType = GPS_DIALOG;
				showErrorDialog(getString(R.string.dialog_gps_settings_title),
						getString(R.string.dialog_gps_is_not_enabled_message));
			}
		} else {
			if (errorDialog != null) {
				errorDialog.dismiss();
				errorDialog = null;
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//No call for super(). Bug on API Level > 11.
	}

	private void registerReceiver() {
		if (receiver != null) {
			deregisterReceiver();
		}

		Log.i(TAG, "Registering Gps Receiver.");
		receiver = new GpsBroadcastReceiver();
		IntentFilter filter = new IntentFilter(
				LocationManager.PROVIDERS_CHANGED_ACTION);
		registerReceiver(receiver, filter);
	}

	private void deregisterReceiver() {
		if (receiver != null) {
			Log.i(TAG, "Deregistering Gps Receiver.");
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	@Override
	protected void onResume() {
		checkGPS(KiteApplication.getContext());
		registerReceiver();
		if (optionsMenu != null) {
			createNetworkStateTimer();
		}
		if (openItem != null) {
			openItem.setTitle(getString(R.string.main_open_items, Integer.toString(KiteDAO.getOpenMunkalapCount(this))));
		}
		super.onResume();
	}

	private void createNetworkStateTimer() {
		resetTimer();
		Konfig konfig = KiteDAO.getKonfig(this, NETWORK_STATE_CHECK_INTERVAL_KEY);
		long interval = NETWORK_STATE_CHECK_INTERVAL_DEFAULT;
		try {
			Long.parseLong(konfig.value);
		} catch (Exception e) {
			// no konfig or not a long value, we use default
		}
		networkStateTimerTask = new NetworkStateTimerTask(this, optionsMenu);
		networkStateTimerTask.pauseCheck = false;
		timer.scheduleAtFixedRate(networkStateTimerTask, 0, interval);
	}

	@Override
	protected void onStop() {
		deregisterReceiver();
		if (tracker != null) {
			tracker.stopUsingGPS();
		}
		super.onStop();
	}

	private void resetTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		timer = new Timer();
	}

	protected int getA4Horizontal() {
		return (int) (8.27 * getResources().getDisplayMetrics().density);
	}
	
	protected int getA4Vertical() {
		return (int) (11.69 * getResources().getDisplayMetrics().density);
	}

	protected String getIMEI() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
		String tmpImei = null;
		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
		} else {
			tmpImei = tm.getDeviceId();
		}
		if (tmpImei == null) {
			try {
				Class<?> c = Class.forName("android.os.SystemProperties");
				Method get = c.getMethod("get", String.class, String.class);
				tmpImei = (String) (get.invoke(c, "ro.serialno", "unknown"));
			} catch (Exception ignored) {
				Log.i(TAG, "Could not get IMEI");
			}
		}
		return tmpImei;
	}

	public String getTodayString(boolean isShort) {
		String result;
		if (isShort) {
			result = getSdfShort().format(new Date());
		} else {
			result = getSdf().format(new Date());
		}
		return result;
	}

	protected abstract void setupUIElements();

	protected abstract void setListeners();

	public Location getGPSLocation() {
		GPSTracker tracker = new GPSTracker(this);
		Location result = null;
		if (tracker.canGetLocation()) {
			result = tracker.getLocation();
			tracker.stopUsingGPS();
			return result;
		} else {
			tracker.stopUsingGPS();
			return result;
		}
	}

	protected void startLoginActivity() {
		sendBroadcast(new Intent(this, LoginReceiver.class));
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void showErrorToast(String message) {
		showToast(message, R.drawable.ic_error_32);
	}

	public void showToast(String message, int resId) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.toast_image);
		if (resId > 0) {
			image.setImageResource(resId);
		} else {
			image.setVisibility(View.GONE);
		}

		TextView text = (TextView) layout.findViewById(R.id.toast_text);
		if (message != null) {
			text.setText(message);
		} else {
			text.setText("Valami hiba történt.");
		}

		if (toast == null) {
			toast = new Toast(getApplicationContext());
			toast.setGravity(Gravity.BOTTOM, 0, 100);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();
		} else {
			toast.cancel();
			toast = null;
		}
	}

	@Override
	protected void onPause() {
		resetTimer();
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
		super.onPause();
	}

	// hide soft input keyboard
	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		try {
			if (activity.getCurrentFocus() != null) {
				inputMethodManager.hideSoftInputFromWindow(activity
						.getCurrentFocus().getWindowToken(), 0);
			}
		} catch (Exception e) {
			Log.i(TAG, "Exception while hiding the keyboard: " + e.getMessage());
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	public void setupClickOutFromEditText(View view) {

		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard(BaseActivity.this);
					return false;
				}
			});
		}

		// If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupClickOutFromEditText(innerView);
			}
		}
	}

	public Account getAccount() {
		return ((KiteApplication) getApplication()).getAccount();
	}

	protected void showErrorDialog(Exception error) {
		String title = getString(R.string.error_dialog_title);
		String message = getString(R.string.error_dialog_message,
				error.getMessage());
		showErrorDialog(title, message);
	}

	public void showErrorDialog(String title, String message) {
		FragmentManager fm = getSupportFragmentManager();
		errorDialog = new ErrorDialog();
		errorDialog.setListener(this);

		Bundle params = new Bundle();
		params.putString("title", title);
		params.putString("message", message);

		errorDialog.setArguments(params);

		if (!isFinishing()) {
			//dialog.show(getSupportFragmentManager(), null);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(errorDialog, null);
			ft.commitAllowingStateLoss();
		} else {
			Log.e(TAG, "Dialog not shown because of the Activity is closing!!!!");
		}
		//errorDialog.show(fm, "fragment_dialog_error");
	}

	public void showQuestionDialog(String title, String message) {
		FragmentManager fm = getSupportFragmentManager();
		errorDialog = new ErrorDialog();
		errorDialog.setListener(this);

		Bundle params = new Bundle();
		params.putString("title", title);
		params.putString("message", message);
		params.putInt("type", ErrorDialog.QUESTION);

		errorDialog.setArguments(params);
		errorDialog.show(fm, "fragment_dialog_error");
	}

	public void showQuestionDialog(String title, String message, IDialogResult listener) {
		FragmentManager fm = getSupportFragmentManager();
		errorDialog = new ErrorDialog();
		errorDialog.setListener(listener);

		Bundle params = new Bundle();
		params.putString("title", title);
		params.putString("message", message);
		params.putInt("type", ErrorDialog.QUESTION);

		errorDialog.setArguments(params);
		errorDialog.show(fm, "fragment_dialog_error");
	}

	public void showQuestionDialog(String title, String message, String leftButtonText, String rightButtonText, IDialogResult listener) {
		FragmentManager fm = getSupportFragmentManager();
		errorDialog = new ErrorDialog();
		errorDialog.setListener(listener);

		Bundle params = new Bundle();
		params.putString("title", title);
		params.putString("message", message);
		params.putString("leftbutton", leftButtonText);
		params.putString("rightbutton", rightButtonText);
		params.putInt("type", ErrorDialog.QUESTION);

		errorDialog.setArguments(params);
		errorDialog.show(fm, "fragment_dialog_error");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.base_menu, menu);
		Uzletkoto uzletkoto = LoginService.getManager(this);
		if(uzletkoto != null){
			menu.findItem(R.id.action_logout).setTitle(uzletkoto.getNev());
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (optionsMenu == null) {
			optionsMenu = menu;
			createNetworkStateTimer();
		}
		openItem = menu.findItem(R.id.action_open_items);
		// ide kellett egy nullvizsgalat, mert kifagyott a szinkron screen
		if (openItem != null) {
			openItem.setTitle(getString(R.string.main_open_items, Integer.toString(KiteDAO.getOpenMunkalapCount(this))));
			if (getIntent().getIntExtra(MunkalapActivity.MUNKALAP_MODE, 0) == MunkalapActivity.MODE_OPEN) {
				menu.removeItem(R.id.action_open_items);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_open_items:
				if (KiteDAO.getOpenMunkalapCount(this) > 0) {
					Intent intent = new Intent(this, MunkalapActivity.class);
					intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_OPEN);
					startActivity(intent);
				} else {
					dialogType = 0;
					showErrorDialog(getString(R.string.dialog_no_open_items_title), getString(R.string.dialog_no_open_items_message));
				}
				return true;
			case R.id.action_logout: {
				logout();
				return true;
			}
			case R.id.action_help: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_sync: {
				Intent intent = new Intent(this, SyncActivity.class);
				startActivity(intent);
				return true;
			}
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void logout() {
		dialogType = LOGOUT_DIALOG;
		showQuestionDialog(getString(R.string.st_dialog_logout_title), getString(R.string.st_dialog_logout_message));
	}

	@Override
	public void onOkClicked(DialogFragment dialog) {
		switch (dialogType) {
			case LOGOUT_DIALOG:
				ImageUtils.deleteUploadableImage(this, MunkalapSummaryFragment.getEmployeeSignatureFilename());
				LoginService.setLoggedIn(BaseActivity.this, false);
				LoginService.deleteManager(BaseActivity.this);
				startLoginActivity();
				dialogType = 0;
				break;

			case SYNC_DIALOG:
				Intent intent = new Intent(this, SyncActivity.class);
				intent.putExtra("AUTOSTART", true);
				startActivity(intent);
				dialogType = 0;
				break;

			case GPS_DIALOG:
				Log.i(TAG, "onOkClicked=" + dialog);
				Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(gpsIntent);
				errorDialog = null;
				dialogType = 0;
				break;

			case OPEN_MUNKALAP_DIALOG:
				Log.i(TAG, "onOkClicked=" + dialog);
				errorDialog = null;
				dialogType = 0;
				break;

			default:
				dialogType = 0;
				break;
		}
	}

	@Override
	public void onCancelClicked(DialogFragment dialog) {
		Log.i(TAG, "onCancelClicked=" + dialog);
		switch (dialogType) {
			case SYNC_DIALOG:
				finish();
				dialogType = 0;
				break;
			case LOGOUT_DIALOG:
			case GPS_DIALOG:
				dialogType = 0;
				break;
			case OPEN_MUNKALAP_DIALOG:
				dialogType = 0;
				errorDialog = null;
				break;
			default:
				break;
		}
		errorDialog = null;
	}

	public void setText(int id, String text) {
		TextView t = (TextView) findViewById(id);
		if (t != null) {
			t.setText(text);
		}
	}

	public void setText(View view, int id, String text) {
		TextView t = (TextView) view.findViewById(id);
		if (t != null) {
			t.setText(text);
		}
	}

	public void show(int id) {
		View v = findViewById(id);
		if (v != null) {
			v.setVisibility(View.VISIBLE);
		}
	}

	public void hide(View view, int id) {
		if (view != null) {
			View v = view.findViewById(id);
			if (v != null) {
				v.setVisibility(View.GONE);
			}
		}
	}

	public void hide(int id) {
		View v = findViewById(id);
		if (v != null) {
			v.setVisibility(View.GONE);
		}
	}

	public void enable(int id, boolean enable) {
		View v = findViewById(id);
		if (v != null) {
			v.setEnabled(enable);
		}
	}

	public void enable(int id) {
		View v = findViewById(id);
		if (v != null) {
			v.setEnabled(true);
		}
	}

	public void disable(int id) {
		View v = findViewById(id);
		if (v != null) {
			v.setEnabled(false);
		}
	}

	protected void goToMain(){
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public boolean hasUzletkotoAzon() {
		Uzletkoto uzletkoto = LoginService.getManager(this);
		return uzletkoto != null && !TextUtils.isEmpty(uzletkoto.azon);
	}

	public void showUzletkotoAzonErrorDialog() {
		showErrorDialog(getString(R.string.dialog_azon_error_title), getString(R.string.dialog_azon_error_message));
	}
}
