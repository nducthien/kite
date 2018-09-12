package hu.itware.kite.service.services;

import hu.itware.kite.service.fragments.MunkalapSummaryFragment;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.orm.network.RestfulClient;
import hu.itware.kite.service.orm.network.RestfulResult;
import hu.itware.kite.service.orm.utils.GSON;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.utils.SystemUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public final class LoginService {
	
	public static class LoginRequest {
		public String imei;
		public String username;
		public String password;
	}
	
	public static final String TAG = "KITE.LOGIN";
	
	private static final String LOGIN_FLAG = "login_flag";
	private static final String SHARED_PREF = "kite_user_prefs";
	private static final String MANAGER_SHARED_PREF = "manager_shared_prefs";
	private static final String MANAGER_CODE = "manager_code";
	private static final String MANAGER_NUI = "manager_nui";
	private static final String MANAGER_AZON = "manager_azon";
	private static final String MANAGER_NAME1 = "manager_name1";
	private static final String MANAGER_NAME2 = "manager_name2";
	private static final String MANAGER_SUBCENTER_ID = "manager_subcenetr_id";
	private static final String MANAGER_PASSWORD = "manager_password";	
	private static final String MANAGER_TOSZ = "manager_tosz";
	private static final String MANAGER_SZERVIZES = "manager_szervizes";
	private static final String MANAGER_JOG = "manager_jog";
	private static final String MANAGER_UGYVITELIKOD = "manager_ugyvitelikod";

	private LoginService() {

	}

	public static LoginResult doLogin(Context context, String username, String password, boolean force) {
		return doLogin(context, username, password, force, true);
	}
	
	
	public static LoginResult doLogin(Context context, String username, String password, boolean force, boolean encodepass) {
		
		Uzletkoto uzletkoto = getManager(context);
		
		if (uzletkoto != null && !force) {
			LoginResult response = new LoginResult();
			response.success = true;
			response.error = null;
			return response;			
		}

		Log.i(TAG, "doLogin[" + username + "]=" + Settings.SERVER_LOGIN_URL);
		LoginRequest request = new LoginRequest();
		request.imei = SystemUtils.getImei(context);
		request.username = username;
		request.password = encodepass ? convertPasswordMD5(password) : password;
		Log.i(TAG, "request=" + GSON.toJson(request));
		
		try {
			LoginResult response = null;
			//Log.i(TAG, "doLogin.url=" + Settings.SERVER_LOGIN_URL);
			//Log.i(TAG, "doLogin.request=" + RestfulClient.getGSON().toJson(request));

			RestfulResult result = RestfulClient.doRequest(context, "POST", Settings.SERVER_LOGIN_URL, GSON.toJson(request), true);
			
			try {
				response = RestfulClient.getGSON().fromJson(result.responseData, LoginResult.class);
			} catch (Exception e) {
				//nothing to do
				Log.e(TAG, "Error parsing JSON response", e);
			}
			
			if (response != null && response.success != null && response.success && response.data != null) {
				response.data.password = request.password;
				setManager(context, response.data);
				Log.i(TAG, "Login success, setting Uzletkoto:" + response.data);
				return response;
			}
			
			response = new LoginResult();
			response.success = false;
			response.error = result.httpError;			
			response.data = null;
			return response;
			
		} catch (Exception e) {
			Log.e(TAG, "Error login:" + e.getLocalizedMessage(), e);
			LoginResult response = new LoginResult();
			response.success = false;
			response.error = e.getMessage();
			return response;
		}
	}

	public static boolean isLoggedIn(Context mContext) {
		return mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE).getBoolean(LOGIN_FLAG, false);
	}

	public static void setLoggedIn(Context mContext, boolean value) {
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(LOGIN_FLAG, value);
		mEditor.apply();
	}
	
	public static void deleteManager(Context mContext) {
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(MANAGER_SHARED_PREF, Context.MODE_PRIVATE);
		Editor mEditor = mSharedPreferences.edit();
		mEditor.clear();
		mEditor.apply();
	}

	public static void updateManager(Context context) {
		Uzletkoto uzletkoto = getManager(context);
		if (uzletkoto != null && StringUtils.isEmpty(uzletkoto.azon)) {
			KiteORM orm = new KiteORM(context);

		}
	}

	public static void setManager(Context mContext, Uzletkoto data) {
		
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(MANAGER_SHARED_PREF, Context.MODE_PRIVATE);
		Editor mEditor = mSharedPreferences.edit();
		mEditor.putString(MANAGER_CODE, data.uzletkotokod);
		mEditor.putString(MANAGER_NUI, data.nui);
		mEditor.putString(MANAGER_AZON, data.azon);
		mEditor.putString(MANAGER_NAME1, data.nev1);
		mEditor.putString(MANAGER_NAME2, data.nev2);
		mEditor.putString(MANAGER_SUBCENTER_ID, data.alkozpontkod);
		mEditor.putString(MANAGER_PASSWORD, data.password);
		mEditor.putString(MANAGER_TOSZ, data.tosz);
		mEditor.putString(MANAGER_JOG, data.jogosultsag);
		mEditor.putString(MANAGER_SZERVIZES, data.szervizeskod);
		mEditor.putString(MANAGER_UGYVITELIKOD, data.ugyvitelikod);
		mEditor.apply();
		Log.i(TAG, "Manager saved:" + data.toString());
	}

	public static Uzletkoto getManager(Context mContext) {
		
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(MANAGER_SHARED_PREF, Context.MODE_PRIVATE);
		String szervizeskod = mSharedPreferences.getString(MANAGER_SZERVIZES, null);
		if (szervizeskod == null) {
			Log.i(TAG, "getManager().manager=NULL!!!!");
			return null;
		}
		
		Uzletkoto data = new Uzletkoto();
		data.uzletkotokod = mSharedPreferences.getString(MANAGER_CODE, null);;
		data.nui = mSharedPreferences.getString(MANAGER_NUI, "");
		data.azon = mSharedPreferences.getString(MANAGER_AZON, "");
		data.nev1 = mSharedPreferences.getString(MANAGER_NAME1, "");
		data.nev2 = mSharedPreferences.getString(MANAGER_NAME2, "");
		data.alkozpontkod = mSharedPreferences.getString(MANAGER_SUBCENTER_ID, "");
		data.password = mSharedPreferences.getString(MANAGER_PASSWORD, "");
		data.tosz = mSharedPreferences.getString(MANAGER_TOSZ, "");
		data.jogosultsag = mSharedPreferences.getString(MANAGER_JOG, "");
		data.szervizeskod = szervizeskod;
		data.ugyvitelikod = mSharedPreferences.getString(MANAGER_UGYVITELIKOD, "");

		Log.i(TAG, "getManager().manager=" + szervizeskod);
		return data;
	}
	
	public static String convertPasswordMD5(String pass) {
		String password = null;
		MessageDigest mdEnc;
		try {
			mdEnc = MessageDigest.getInstance("MD5");
			mdEnc.update(pass.getBytes(), 0, pass.length());
			String encodedPass = new BigInteger(1, mdEnc.digest()).toString(16);
			while (encodedPass.length() < 32) {
				encodedPass = "0" + encodedPass;
			}
			password = encodedPass;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Could not encrypt password", e);
		}
		return password;
	}

}
