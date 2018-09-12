package hu.itware.kite.service.services;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.SyncDataTable;
import hu.itware.kite.service.orm.model.SyncData;
import hu.itware.kite.service.orm.network.RestfulClient;
import hu.itware.kite.service.orm.network.RestfulResult;
import hu.itware.kite.service.orm.sync.SyncAdapter;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.Network;

/**
 * Created by szeibert on 2017.05.31..
 */

public class NetworkStateTimerTask extends TimerTask {

    private static final String TAG = "NetworkStatTimerTask";

    private Activity context;
    private Menu optionsMenu;
    public static boolean serverAccessible = false;
    private boolean downloadNeeded = false;

    private static final int RED = Color.parseColor("#b71c1c");
    private static final int GREEN = Color.parseColor("#00713d");

    private KiteORM orm;

    private static HashMap<String, Boolean> downloadNeededForTable = new HashMap<String, Boolean>();
    public static boolean pauseCheck = false;

    public NetworkStateTimerTask(Activity context) {
        this.context = context;
    }

    public NetworkStateTimerTask(Activity context, Menu optionsMenu) {
        this.context = context;
        this.optionsMenu = optionsMenu;
        orm = new KiteORM(context);
    }

    public void setOptionsMenu(Menu optionsMenu) {
        this.optionsMenu = optionsMenu;
    }

    @Override
    public void run() {
        if (optionsMenu != null && !pauseCheck) {
            checkDownloadNeeded();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MenuItem item = optionsMenu.findItem(R.id.action_sync);
                    if (item != null) {
                        SpannableString s = new SpannableString(context.getString(R.string.main_sync));
                        Drawable icon = item.getIcon();
                        icon = DrawableCompat.wrap(icon);
                        int color = Color.BLACK;
                        if (downloadNeeded) {
                            color = GREEN;
                        }
                        if (isUploadNeeded()) {
                            color = RED;
                        }
                        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
                        item.setTitle(s);
                        // icon ne legyen fekete
                        if (color == Color.BLACK) {
                            color = GREEN;
                        }
                        DrawableCompat.setTint(icon, color);

                    }
                    item = optionsMenu.findItem(R.id.action_net);
                    if (item != null) {
                        String title = "";
                        Drawable icon = item.getIcon();
                        icon = DrawableCompat.wrap(icon);
                        int color;
                        if (Network.getNetworkState(context) != Network.STATE_NONETWORK) {
                            if (serverAccessible) {
                                color = GREEN;
                                item.setTitle("");
                            } else {
                                color = RED;
                                title = context.getString(R.string.net_status_vpn);
                            }
                        } else {
                            color = RED;
                            title = context.getString(R.string.net_status_internet);
                        }
                        SpannableString s = new SpannableString(title);
                        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
                        item.setTitle(s);
                        DrawableCompat.setTint(icon, color);
                    }
                }
            });
        }
    }

    private boolean isUploadNeeded() {
        boolean uploadNeeded = false;
        List<SyncData> syncList = orm.list(SyncData.class, SyncDataTable.COL_MODE + " = ?", new String[]{SyncAdapter.MODE_UPLOAD});
        for (SyncData data : syncList ) {
            if (!uploadNeeded) {
                uploadNeeded = orm.getNativeCount("select count(*) from " + data.tablename, new String[]{}) > 0;
            } else {
                break;
            }
        }
        return uploadNeeded;
    }

    private void checkDownloadNeeded() {
        downloadNeeded = false;
        try {
            RestfulResult result = RestfulClient.doRequest(context, "GET", Settings.SERVER_CHECK_SYNC_NEEDED_URL, null, true, true);
            if (result.isHttpOk()) {
                serverAccessible = true;
                JSONObject resObj = new JSONObject(result.responseData);
                JSONObject lastModifiedTimes = resObj.getJSONObject("data").getJSONObject("last_modified_times");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                JSONObject tables = lastModifiedTimes.getJSONObject("tables");
                List<SyncData> syncList = orm.list(SyncData.class, SyncDataTable.COL_MODE + " = ?", new String[]{SyncAdapter.MODE_DOWNLOAD});
                for (SyncData syncData : syncList) {
                    if (tables.has(syncData.tablename)) {
                        Date modifiedOnServer = df.parse(tables.getString(syncData.tablename));
                        if (syncData.updated != null) {
                            if (modifiedOnServer.after(syncData.updated)) {
                                downloadNeededForTable.put(syncData.tablename, true);
                                downloadNeeded = true;
                            } else {
                                downloadNeededForTable.put(syncData.tablename, false);
                            }
                        } else {
                            downloadNeeded = true;
                            downloadNeededForTable.put(syncData.tablename, true);
                        }
                    }
                }
            } else {
                serverAccessible = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            serverAccessible = false;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDownloadNeeded(String tableName) {
        if (downloadNeededForTable.containsKey(tableName)) {
            return downloadNeededForTable.get(tableName);
        } else {
            return false;
        }
    }

    public static void setDownloadNeeded(String tableName, boolean downloadNeeded) {
        downloadNeededForTable.put(tableName, downloadNeeded);
    }

}
