package hu.itware.kite.service.activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hu.itware.kite.service.R;
import hu.itware.kite.service.fragments.ErrorDialog;
import hu.itware.kite.service.fragments.SyncDialog;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.KiteDatabaseHelper;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.database.tables.SyncDataTable;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import hu.itware.kite.service.orm.model.Alkozpont;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.GepSzerzodes;
import hu.itware.kite.service.orm.model.GepSzerzodesTetel;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.SyncData;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.provider.KiteContentProvider;
import hu.itware.kite.service.orm.sync.SyncAdapter;
import hu.itware.kite.service.services.NetworkStateTimerTask;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.FileUtils;
import hu.itware.kite.service.utils.Network;
import hu.itware.kite.service.utils.StringUtils;

public class SyncActivity extends BaseActivity {

    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("hu"));
    private static final String DBSTATE = "DBSTATE";
    private static final String DBINSTALLED = "DBINSTALLED";

    private static final long HOUR = 1000 * 60 * 60L;
    private static final long DAY = HOUR * 24;
    private SyncBroadcastReceiver receiver;

    private SyncDialog dialog;

    private AsyncTask<String, String, List<SyncData>> task;

    public static volatile boolean syncinProgress = false;

    private MenuItem syncMenuButton;

    private volatile boolean hasAdminJogForSync = false;

    private String ADMIN_PASS = "ODk3NzFuNA==";

    private class SyncBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive=" + intent);
            if (SyncAdapter.BROADCAST_SYNC.equals(intent.getAction())) {
                int command = intent.getIntExtra("COMMAND", -1);
                if (command == SyncAdapter.COMMAND_END) {

                    if (syncinProgress) {
                        changeSyncStatusButtonState(false);
                    }

                    syncinProgress = false;
                } else {
                    if (!syncinProgress) {
                        changeSyncStatusButtonState(true);
                    }
                    syncinProgress = true;
                }
                handleSyncInfo(command, intent.getSerializableExtra("DATA"));
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sync);

        TextView tvServerAddress = (TextView) findViewById(R.id.server_address_textview);
        tvServerAddress.setText(getString(R.string.server_address_info, Settings.SERVER_BASE_URL));

        setListeners();
        refreshSyncList();
    }

    @Override
    protected void setupUIElements() {

    }

    @Override
    protected void setListeners() {

        Button button = (Button) findViewById(R.id.sync_button_all);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                boolean dbOk = getSharedPreferences(DBSTATE, Context.MODE_PRIVATE).getBoolean(DBINSTALLED, false);
                if (dbOk) {
                    CheckBox delete = (CheckBox) findViewById(R.id.sync_check_all);
                    performFullSync(hasAdminJogForSync && delete.isChecked());
                } else {
                    downloadDB();
                }
            }
        });

        hide(R.id.sync_text_nodata);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//		if (!Settings.DEBUG && !Global.SU) {
//			getMenuInflater().inflate(R.menu.sync, menu);
//			MenuItem imp = menu.findItem(R.id.sync_action_import_sd);
//			MenuItem exp = menu.findItem(R.id.sync_action_export_sd);
//			imp.setVisible(false);
//			exp.setVisible(false);
//		}

        getMenuInflater().inflate(R.menu.sync_admin, menu);
        if (hasAdminJogForSync) {
            MenuItem adminMenu = menu.findItem(R.id.sync_action_login_admin);
            adminMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.sync_action_showsyncdialog:
                startSyncDialog(null);
                return true;
            case R.id.sync_action_import_sd:
                importDB();
                refreshSyncList();
                return true;
            case R.id.sync_action_export_sd:
                exportDB();
                return true;
            case R.id.sync_action_login_admin:
                showAdminPasswordDialog();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean exportDB() {
        Log.i(TAG, "exportDB()");

        // ---Copy DB TO SDCARD ROOT
        try {
            File kiteDBFile = new File(KiteDatabaseHelper.DB_PATH);
            File sdDBFile = new File(Environment.getExternalStorageDirectory(), KiteDatabaseHelper.DB_NAME);

            if (!kiteDBFile.exists()) {
                Log.w(TAG, "No KITE DB file found in app:" + Environment.getExternalStorageDirectory());
                Toast.makeText(this, R.string.sync_export_database_failed, Toast.LENGTH_LONG).show();
                return false;
            }

            Log.i(TAG, "Copy DB from " + kiteDBFile + " to " + sdDBFile);
            FileUtils.copyFile(new FileInputStream(kiteDBFile), new FileOutputStream(sdDBFile));

            Toast.makeText(this, R.string.sync_export_database_success, Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error import database=" + e, e);
            Toast.makeText(this, R.string.sync_export_database_failed, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean importDB() {
        Log.i(TAG, "importDB()");
        try {
            KiteDatabaseHelper db = KiteDatabaseHelper.getInstance(this);
            db.closeDatabase();

            File kiteDBFile = new File(KiteDatabaseHelper.DB_PATH);
            File sdDBFile = new File(Environment.getExternalStorageDirectory(), KiteDatabaseHelper.DB_NAME);

            if (!sdDBFile.exists()) {
                Log.w(TAG, "No KITE DB file found on root od SD storage:" + Environment.getExternalStorageDirectory());
                Toast.makeText(this, R.string.sync_import_database_failed, Toast.LENGTH_LONG).show();
                return false;
            }

            Log.i(TAG, "Copy DB from " + sdDBFile + " to " + kiteDBFile);
            FileUtils.copyFile(new FileInputStream(sdDBFile), new FileOutputStream(kiteDBFile));
            db.getWritableDatabase().close();
            Toast.makeText(this, R.string.sync_import_database_success, Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error import database=" + e, e);
            Toast.makeText(this, R.string.sync_import_database_failed, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void registerReceiver() {
        if (receiver != null) {
            deregisterReceiver();
        }

        Log.i(TAG, "Registering Sync Receiver.");
        receiver = new SyncBroadcastReceiver();
        IntentFilter filter = new IntentFilter(SyncAdapter.BROADCAST_SYNC);
        registerReceiver(receiver, filter);
    }

    private void deregisterReceiver() {
        if (receiver != null) {
            Log.i(TAG, "Deregistering Sync Receiver.");
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onStop() {
        deregisterReceiver();
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            task = null;
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startSyncDialog(String table) {
        NetworkStateTimerTask.pauseCheck = false;
        FragmentManager fm = getSupportFragmentManager();
        dialog = new SyncDialog();
        dialog.setListener(this);

        Bundle params = new Bundle();
        params.putString("title", getString(R.string.sync_dialog_title));
        params.putString("message", getString(R.string.sync_dialog_message, table == null ? "összes" : table));

        dialog.setArguments(params);
        dialog.show(fm, "fragment_dialog_sync");
    }

    private void startDownloadDBDialog() {
        FragmentManager fm = getSupportFragmentManager();
        dialog = new SyncDialog();
        dialog.setListener(this);

        Bundle params = new Bundle();
        params.putString("title", getString(R.string.sync_dialog_db_title));
        params.putString("message", getString(R.string.sync_dialog_db_message));
        params.putString("table", "kite.db");
        params.putString("count", "");

        dialog.setArguments(params);
        dialog.show(fm, "fragment_dialog_sync");
    }

    private void refreshSyncList() {
        task = new AsyncTask<String, String, List<SyncData>>() {

            @Override
            protected void onPreExecute() {
                show(R.id.sync_progress_loading);
                TableLayout container = (TableLayout) findViewById(R.id.sync_scroll_items);
                container.removeAllViews();
            }

            @Override
            protected List<SyncData> doInBackground(String... params) {

                KiteORM orm = new KiteORM(SyncActivity.this);

                Konfig konfig = orm.loadSingle(Konfig.class, "name = ?", new String[]{"ADMPASS"});
                if (konfig != null && konfig.value != null) {
                    ADMIN_PASS = konfig.value;
                }
                //Log.e(TAG, "refreshSyncList().adminPass=" + ADMIN_PASS);

                List<SyncData> syncList = orm.list(SyncData.class, null, null);
                for (SyncData data : syncList) {

                    if (SyncAdapter.MODE_UPLOAD.equals(data.mode)) {
                        //data.error = "" + orm.getCount(data.tablename);
                        data.error = "" + orm.getNativeCount("select count(*) from " + data.tablename, new String[]{});
                        //data.lastValue = orm.getCount(data.tablename, "status = ?", new String[] { "A" });
                        data.lastValue = orm.getNativeCount("select count(*) from " + data.tablename + " where status = ?", new String[]{"A"});
                    } else {
                        // data.lastValue = orm.getCount(data.tablename);
                        data.lastValue = orm.getNativeCount("select count(*) from " + data.tablename, new String[]{});
                    }
                }

                return syncList;
            }

            @Override
            protected void onPostExecute(List<SyncData> result) {
                createSyncList(result);
                hide(R.id.sync_progress_loading);
            }

        };
        task.execute();
    }

    private void createSyncList(List<SyncData> sync_list) {

        invalidateOptionsMenu();

        LayoutInflater inflanter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableLayout container = (TableLayout) findViewById(R.id.sync_scroll_items);
        container.removeAllViews();
        LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutParams layoutSpace = new LayoutParams(LayoutParams.MATCH_PARENT, 5);

        //--- Sort by UPLOAD first....
        Collections.sort(sync_list, new Comparator<SyncData>() {
            @Override
            public int compare(SyncData s1, SyncData s2) {
                if (SyncAdapter.MODE_UPLOAD.equals(s1.mode)) {
                    return -1;
                }

                if (SyncAdapter.MODE_UPLOAD.equals(s2.mode)) {
                    return +1;
                }
                return 0;
            }
        });

        int networkState = Network.getNetworkState(this);
        if (networkState == Network.STATE_NONETWORK) {
            disable(R.id.sync_button_all);
        }

        if (!hasAdminJogForSync) {
            disable(R.id.sync_check_all);
        } else {
            enable(R.id.sync_check_all);
        }
        Date now = new Date();

        if (sync_list == null || sync_list.isEmpty()) {
            show(R.id.sync_text_nodata);
            return;
        } else {
            hide(R.id.sync_text_nodata);
        }

        for (final SyncData data : sync_list) {

            if (data != null && SyncDataTable.TABLE_NAME.equals(data.tablename)) {
                continue;
            }
            Log.e(TAG, "SYNCDATA=" + data);

            long lastEllapsed = 0;
            if (data.modified != null) {
                lastEllapsed = now.getTime() - data.modified.getTime();
            }

            View view = inflanter.inflate(R.layout.view_sync_item, null);
            setText(view, R.id.sync_client_tablename, data.tablename);
            setText(view, R.id.sync_client_lastupdate, data.modified == null ? "SOHA" : DF.format(data.modified));
            setText(view, R.id.sync_server_tablename, data.tablename);

            Button syncButton = (Button) view.findViewById(R.id.sync_button_sync);

            if (networkState == Network.STATE_NONETWORK || !NetworkStateTimerTask.serverAccessible) {
                syncButton.setEnabled(false);
                setText(view, R.id.sync_server_message, getString(R.string.sync_server_state_nonetwork));
            } else if (SyncAdapter.MODE_UPLOAD.equals(data.mode) && networkState == Network.STATE_NONETWORK) {
                syncButton.setEnabled(false);
                setText(view, R.id.sync_server_message, getString(R.string.sync_server_state_nowiwi));
            } else {
                setText(view, R.id.sync_server_message, getString(R.string.sync_server_state_ready));
            }
            final CheckBox cbDelete = (CheckBox) view.findViewById(R.id.sync_check_delete);
            cbDelete.setEnabled(hasAdminJogForSync);
            syncButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    performSync(data.tablename, data.mode, cbDelete.isChecked());
                }
            });

            if (SyncAdapter.MODE_DOWNLOAD.equals(data.mode)) {

                setText(view, R.id.sync_client_records, "" + data.lastValue + " darab");
                syncButton.setText("<<<<<<<");

                if (data.updated == null || lastEllapsed == 0 || (NetworkStateTimerTask.isDownloadNeeded(data.tablename) && lastEllapsed >= DAY) || !data.success) {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_needrefresh));
                    setColor(view, R.id.sync_client_text_status, 0xFFAA0000);
                } else if (NetworkStateTimerTask.isDownloadNeeded(data.tablename) && lastEllapsed >= HOUR * 4) {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_shoudrefresh));
                    setColor(view, R.id.sync_client_text_status, 0xFFFFB03B);
                } else if (NetworkStateTimerTask.isDownloadNeeded(data.tablename)) {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_dataavailable));
                    setColor(view, R.id.sync_client_text_status, 0xFFFFB03B);
                } else {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_ok));
                    setColor(view, R.id.sync_client_text_status, 0xFF468966);
                }
            } else if (SyncAdapter.MODE_UPLOAD.equals(data.mode)) {

                hide(view, R.id.sync_check_delete);
                syncButton.setText(">>>>>>>");
                setText(view, R.id.sync_client_text_records, getString(R.string.sync_client_itemcount_upload));
                setText(view, R.id.sync_client_records, "" + data.error + "/" + data.lastValue + " darab");

                setBackground(view, R.id.sync_root, R.drawable.kite_table_background_clicked);
                if (data.lastValue == 0) {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_ok));
                    setColor(view, R.id.sync_client_text_status, 0xFF468966);
                } else if (data.lastValue <= 3) {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_shoudrefresh));
                    setColor(view, R.id.sync_client_text_status, 0xFFFFB03B);
                } else {
                    setText(view, R.id.sync_client_text_status, getString(R.string.sync_client_state_needrefresh));
                    setColor(view, R.id.sync_client_text_status, 0xFFAA0000);
                }
            }

            container.addView(view, layout);

            View spacer = new View(this);
            container.addView(spacer, layoutSpace);
        }
    }

    public void setColor(View root, int id, int color) {
        TextView v = (TextView) root.findViewById(id);
        if (v != null) {
            v.setTextColor(color);
        }
    }

    public void setBackground(View root, int id, int resid) {
        View v = root.findViewById(id);
        if (v != null) {
            v.setBackgroundResource(resid);
        }
    }

    private void performFullSync(boolean delete) {

        if (dialog != null && dialog.isVisible()) {
            dialog.dismiss();
            dialog = null;
        }

        if (delete) {
            Log.i(TAG, "Deleting databases for full sync.");
            KiteORM orm = new KiteORM(this);
            orm.delete(Alkozpont.class, null, null);
            orm.delete(Partner.class, null, null);
            orm.delete(MetaData.class, null, null);
            orm.delete(Alkatresz.class, null, null);
            orm.delete(Munkalap.class, null, null);
            orm.delete(KeszletMozgas.class, null, null);
            orm.delete(AlkatreszCikkszam.class, null, null);
            orm.delete(Gep.class, null, null);
            orm.delete(GepSzerzodes.class, null, null);
            orm.delete(GepSzerzodesTetel.class, null, null);
            orm.delete(Konfig.class, null, null);
            orm.delete(Uzletkoto.class, null, null);
            orm.delete(SyncData.class, "mode = ?", new String[]{SyncAdapter.MODE_DOWNLOAD});
        }

        performSync(null, null, false);

    }

    private void performSync(String table, String direction, boolean delete) {

        Log.i(TAG, "Start performSync()");
        startSyncDialog(table);

        if (delete) {

            BaseTable<?> handler = TableMap.getHandlerByTablename(table);
            if (handler != null) {
                Log.i(TAG, "Delete table data:" + table);
                KiteORM orm = new KiteORM(this);
                orm.delete(handler.getModelClass(), null, null);
                orm.delete(SyncData.class, "tablename = ?", new String[]{table});
            }
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putString("table", table);
        settingsBundle.putString("direction", direction);

        ContentResolver.requestSync(getAccount(), KiteContentProvider.PROVIDER_NAME, settingsBundle);

    }

    protected void handleSyncInfo(int command, Serializable data) {
        if (dialog != null && dialog.isVisible()) {
            dialog.doProgress(command, data);
        }
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {

        if (dialog instanceof ErrorDialog) {
            ErrorDialog errorDialog = (ErrorDialog) dialog;
            int type = errorDialog.getType();
            if (type == ErrorDialog.PASSWORD) {

                String password = StringUtils.encodeToBase64(errorDialog.getPassword());
                if (ADMIN_PASS != null && password != null && ADMIN_PASS.equals(password)) {
                    hasAdminJogForSync = true;
                    Settings.ADMIN_MODE = true;
                } else {
                    hasAdminJogForSync = false;
                    Toast.makeText(this, R.string.invalid_admin_password, Toast.LENGTH_LONG).show();
                    Settings.ADMIN_MODE = false;
                }
            }
            refreshSyncList();
        } else if (dialog instanceof SyncDialog) {
            refreshSyncList();
        }
    }

    private void changeSyncStatusButtonState(boolean enable) {

        if (syncMenuButton != null) {
            syncMenuButton.setEnabled(enable);
        }
    }

    public static boolean exists(String urlName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(urlName).openConnection();
            con.setRequestMethod("HEAD");
            return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Log.d(TAG, "Error getting " + urlName, e);
            return false;
        }
    }

    private void downloadDB() {

        startDownloadDBDialog();

        AsyncTask<String, Integer, Boolean> downloadTask = new AsyncTask<String, Integer, Boolean>() {

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    // --- create HTTP Params
                    String dbUrl = Settings.SERVER_BASE_URL + "/szerviz/db/kite.db";

                    if (!exists(dbUrl)) {
                        Log.e(TAG, "Kite DB not exists on server, perform normal sync. KiteDBURL=" + dbUrl);

                        Editor edit = getSharedPreferences(DBSTATE, Context.MODE_PRIVATE).edit();
                        edit.putBoolean(DBINSTALLED, true);
                        edit.apply();

                        performFullSync(false);
                        return true;
                    }
                    URL url = new URL(dbUrl);

                    Log.v(TAG, "Downloading database:" + dbUrl);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    int length = connection.getContentLength();
                    publishProgress(-1, length);

                    Log.v(TAG, "Database size to download = " + length + " bytes");
                    InputStream is = url.openStream();

                    File kiteDBFile = new File(KiteDatabaseHelper.DB_PATH);
                    FileOutputStream fos = new FileOutputStream(kiteDBFile);

                    byte data[] = new byte[1024 * 1024];

                    int count = 0;
                    int total = 0;

                    int lastTotal = -1;

                    while ((count = is.read(data)) != -1) {
                        total += count;
                        if (lastTotal == -1 || total - lastTotal > 100000) { // 100.000
                            // bytes
                            publishProgress(total);
                            lastTotal = total;
                        }
                        fos.write(data, 0, count);
                    }
                    Log.v(TAG, "Database downloaded. Total length:" + total);
                    is.close();
                    fos.close();
                    return true;

                } catch (Exception e) {
                    Log.e(TAG, "Error downloading DB:" + e, e);
                }

                return false;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {

                if (values[0] == -1) {
                    dialog.getCount().setText(values[1].intValue() + " bytes");
                    dialog.getProgress().setMax(values[1].intValue());
                    dialog.getProgress().setProgress(0);
                    dialog.getMessage().setText("Letöltés folyamaban...");
                } else {
                    dialog.doProgress(SyncDialog.TEXT_COMMAND_COUNT, values[0] + " bytes");
                    dialog.doProgress(SyncDialog.TEXT_COMMAND_PROGRESS, values[0]);
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {

                Editor edit = getSharedPreferences(DBSTATE, Context.MODE_PRIVATE).edit();
                edit.putBoolean(DBINSTALLED, result);
                edit.apply();

                dialog.doProgress(SyncAdapter.COMMAND_END, null);
            }

        };
        downloadTask.execute();
    }

    private void showAdminPasswordDialog() {
        FragmentManager fm = getSupportFragmentManager();
        errorDialog = new ErrorDialog();
        errorDialog.setListener(this);

        Bundle params = new Bundle();
        params.putString("title", "Szinkronizációs admin jog");
        params.putString("message", "");
        params.putInt("type", ErrorDialog.PASSWORD);

        errorDialog.setArguments(params);
        errorDialog.show(fm, "fragment_dialog_question");
    }

}
