package hu.itware.kite.service.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gyongyosit on 2016.08.25..
 */
public class PipUpdateService {

    public static final String TAG = "KITE.PIP";

    private static final String SHARED_PREF = "kite_pip_prefs";

    public static void setPipCodeVersion(Context mContext, String pipCode, int pipVersion) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt(pipCode, pipVersion);
        mEditor.apply();
    }

    public static int getPipCodeVersion(Context mContext, String pipCode) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(pipCode, -1);
    }

}
