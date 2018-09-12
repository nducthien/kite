package hu.itware.kite.service.activity;

import hu.itware.kite.service.R;
import hu.itware.kite.service.fragments.ConfirmDialog;
import hu.itware.kite.service.fragments.IDialogResult;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesTetelekTable;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesekTable;
import hu.itware.kite.service.orm.model.GepSzerzodes;
import hu.itware.kite.service.orm.model.GepSzerzodesTetel;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.services.UpdateService;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.Network;
import hu.itware.kite.service.utils.SystemUtils;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SplashActivity extends BaseActivity {

    AccountManager accountManager;

    private AsyncTask<Long, Long, Boolean> task;

	private View updatePanel;
	private TextView updateMessage;
	private TextView updateError;
	private ProgressBar updateProgress;
	private Button updateButtonLogin;
    private ProgressBar progress;
    private HashMap<String, Boolean> listPermission = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listPermission.put(Manifest.permission.CAMERA, false);
        listPermission.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
        listPermission.put(Manifest.permission.ACCESS_FINE_LOCATION, false);
        listPermission.put(Manifest.permission.READ_PHONE_STATE, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getRuntimePermission();
        }

        Log.e(TAG, "+++++Mobile Device IMEI:" + getIMEI());

        // full screen mode, remove action bar and success bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        setupUIElements();
        setListeners();
    }


    private void startWaitTask(long delay) {
        task = new AsyncTask<Long, Long, Boolean>() {

            @Override
            protected Boolean doInBackground(Long... params) {
                try {
                    Thread.sleep(params[0]);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                doCheckUpdates();
            }

        };

        task.execute(delay);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void getRuntimePermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 111);
        } else {
            listPermission.put(Manifest.permission.CAMERA, true);

        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 222);
        } else {
            listPermission.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 333);
        } else {
            listPermission.put(Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 444);
        } else {
            listPermission.put(Manifest.permission.READ_PHONE_STATE, true);
        }


    }
    public boolean checkAllPermision() {
        ArrayList<String> listKey = new ArrayList<>();
        for (String key : listPermission.keySet()) {
            listKey.add(key);
        }
        for (String key : listKey) {
            if (listPermission.get(key) == false)
                return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (task == null && checkAllPermision()) {
                startWaitTask(Settings.SPLASH_DELAY_TIME);
            } else {
                getRuntimePermission();
            }
        }else{
            if (task == null) {
                startWaitTask(Settings.SPLASH_DELAY_TIME);
            }
        }
    }

    @Override
    protected void onStop() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            task = null;
        }

        super.onStop();
    }

    protected void doCheckUpdates() {
        try {
            UpdateService.checkUpdates(this, "" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode, new UpdateService.UpdateAvailableListener() {
                @Override
                public void onUpdateAvailable(final String path) {
                    Log.e(TAG, "onUpdateAvailable()!=" + path);
                    Log.e(TAG, "NetWork State=" + Network.getNetworkState(SplashActivity.this));
                    if (Network.getNetworkState(SplashActivity.this) == Network.STATE_WIFI) {
                        Log.e(TAG, "onUpdateAvailable().showDialog()");
                        ConfirmDialog dialog = new ConfirmDialog();
                        Bundle arguments = new Bundle();
                        arguments.putString("title", getString(R.string.label_update_available_title));
                        arguments.putString("message", getString(R.string.label_update_available_description));
                        dialog.setArguments(arguments);
                        dialog.setListener(new IDialogResult() {
                            @Override
                            public void onOkClicked(DialogFragment dialog) {

								updatePanel.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.push_up_in));
								updatePanel.setVisibility(View.VISIBLE);

                                UpdateService.downloadUpdate(SplashActivity.this, path, updateProgress, new UpdateService.UpdateDownloadedListener() {
                                    @Override
                                    public void onSuccess(String localPath) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.fromFile(new File(localPath)),
                                                "application/vnd.android.package-archive");
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onFailure(final Throwable e) {
                                        Log.e(TAG, "Error installing new version APK. Reason=" + e, e);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateProgress.setVisibility(View.INVISIBLE);
                                                progress.setVisibility(View.INVISIBLE);
                                                updateError.setVisibility(View.VISIBLE);
                                                updateButtonLogin.setVisibility(View.VISIBLE);
                                                updateError.setText("Hiba a frissítés letöltésekor! Hiba:" + e.getLocalizedMessage());
                                                updateMessage.setText("Frissítés sikertelen...");
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onCancelClicked(DialogFragment dialog) {
                                doLogin();
                            }
                        });
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.add(dialog,"fragment_dialog_cancel").commitAllowingStateLoss();
//                        dialog.show(fm, "fragment_dialog_cancel");
                    } else {
                        Log.e(TAG, "onUpdateAvailable().networkNotWifi! doLogin()");
                        doLogin();
                    }
                }

                @Override
                public void onUpdateNotAvailable() {
                    doLogin();
                }

                @Override
                public void onFailure(Throwable e) {
                    doLogin();
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
            doLogin();
        }
    }

    protected void doLogin() {
        if (!LoginService.isLoggedIn(SplashActivity.this)) {
            Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        } else {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        }
    }

    @Override
    protected void setupUIElements() {
        String version = getString(R.string.version_string, SystemUtils.getVersion(this));
        setText(R.id.text_version, version);

        progress = (ProgressBar) findViewById(R.id.splash_progress);
        updatePanel = findViewById(R.id.splash_update_layout);
        updateProgress = (ProgressBar) findViewById(R.id.splash_progress_update);
        updatePanel.setVisibility(View.INVISIBLE);
        updateMessage = (TextView) findViewById(R.id.update_message);
        updateError = (TextView) findViewById(R.id.update_error);
        updateError.setVisibility(View.INVISIBLE);
        updateButtonLogin = (Button) findViewById(R.id.update_button_login);
        updateButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
        updateButtonLogin.setVisibility(View.INVISIBLE);

        ImageView logo = (ImageView) findViewById(R.id.splash_logo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        anim.setDuration(1500);
        logo.startAnimation(anim);

    }

    @Override
    protected void setListeners() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 111:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listPermission.put(Manifest.permission.CAMERA, true);

                }
                break;
            case 222:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listPermission.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
                }
                break;
            case 333:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listPermission.put(Manifest.permission.ACCESS_FINE_LOCATION, true);
                }
                break;
            case 444:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.e(TAG, "+++++Mobile Device IMEI:" + getIMEI());
                    listPermission.put(Manifest.permission.READ_PHONE_STATE, true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
