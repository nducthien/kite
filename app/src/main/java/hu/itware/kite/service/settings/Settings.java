package hu.itware.kite.service.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import hu.itware.kite.service.BuildConfig;

public final class Settings {


	/**
	 * Admin mode
	 */
	public static volatile boolean ADMIN_MODE = false;

	/**
	 * If true, run on emulator without encryption and use internal IP for server
	 */

	public static final boolean DEBUG = false;
	public static final boolean ENCRYPTION_ENABLED = true;
	public static final String LOCAL_IMAGES_DIR = "/KITEServiceImages";
	public static final String UPLOAD_IMAGES_DIR = "/KITEServiceImages/upload";
	// Átmeneti könyvtár, amíg nem készül munkalap export, addig ide kerülnek a csatolt képek, hogy ne töltődjenek fel
	public static final String UPLOAD_IMAGES_TEMP_DIR = "/KITEServiceImages/temp";
	//public static final String SERVER_BASE_URL = DEBUG ? "http://192.168.3.115/kitev2" : "http://192.168.3.90/kitev2";

	//---TODO: KITE VPN LIVE
	//public static final String SERVER_BASE_URL = "http://192.168.3.90/kitev2";
	//---TODO: KITE VPN TEST
	public static final String SERVER_PATH_SEGMENT = "";

	//--- TODO: KITE VPN LIVE v3
	//public static final String SERVER_BASE_URL = "http://192.168.3.90/kitev3";

	//public static final String SERVER_BASE_URL = DEBUG ? "http://192.168.0.165:8085/kitev2" : "http://192.168.0.165:8081/kitev2";
	//public static final String SERVER_BASE_URL = "http://192.168.0.165";

	// -- TODO: ITWARE LOCAL
	public static final String SERVER_BASE_URL_DEFAULT = "http://192.168.0.165:8085/kitev2";//"http://192.168.3.115/kitev2";
	//public static final String SERVER_BASE_URL_DEFAULT = "http://192.168.3.90/kitev4";
	public static final String SERVER_BASE_URL = TextUtils.isEmpty(BuildConfig.SERVER_BASE_URL) ? SERVER_BASE_URL_DEFAULT : BuildConfig.SERVER_BASE_URL;

	//--- TODO: ITWARE LOCAL TUNEL TEST
	//public static final String SERVER_BASE_URL = "http://192.168.0.165:8085/kitev2";
	//--- TODO: ITWARE LOCAL TUNEL LIVE
	//public static final String SERVER_BASE_URL = "http://192.168.0.165:8081/kitev2";
	public static final String SERVER_REST_URL = SERVER_BASE_URL + SERVER_PATH_SEGMENT + "/szerviz/rest";
	public static final String SERVER_LOGIN_URL = SERVER_BASE_URL + SERVER_PATH_SEGMENT + "/szerviz/mobil/user/login.json";
	public static final String SERVER_UPDATE_URL = SERVER_BASE_URL + "/szerviz/rest/update_available/";
	public static final String SERVER_CHECK_SYNC_NEEDED_URL = SERVER_BASE_URL + "/szerviz/rest/lastmodifiedtimes/";
	public static final String SERVER_UPLOAD_URL = Settings.SERVER_BASE_URL + "/szerviz/rest/file_upload";
	public static final String SERVER_DOWNLOAD_URL = Settings.SERVER_BASE_URL + "/public/szerviz/images/";
	public static final String SERVER_DOWNLOAD_PIP_URL = Settings.SERVER_BASE_URL + "/public/szerviz/pip/";
	public static final String SERVER_DOWNLOAD_INFO_URL = Settings.SERVER_BASE_URL + "/public/szerviz/docs/";
	public static final long SPLASH_DELAY_TIME = 2000;
	// 30 min
	public static final int GPS_REFRESH_INTERVAL = 1800000;
	public static final String STUDY_MATERIALS_ROOT_DIRECTORY = "/Kite_study_test/";
	public static final String PIP_CODE_DOCUMENT_DIRECTORY = STUDY_MATERIALS_ROOT_DIRECTORY + "PIP/";
	public static final int HTTP_CONNECTION_TIMEOUT_MS = 60000;
	private static final String NAME = "KITE.SETTINGS";

    private Settings() {

	}

	public static void setServerUrl(Context context, String url) {
		SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString("SERVERURL", url);
		edit.apply();
	}
	
	public static String getServerUrl(Context context) {
		SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		return sp.getString("SERVERURL", SERVER_REST_URL);
	}
}
