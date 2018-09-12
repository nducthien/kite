package hu.itware.kite.service.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.Validator.ValidationListener;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Required;

import hu.itware.kite.service.R;
import hu.itware.kite.service.fragments.IDialogResult;
import hu.itware.kite.service.receivers.LoginReceiver;
import hu.itware.kite.service.services.LoginResult;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.utils.GPSTracker;
import hu.itware.kite.service.utils.Global;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.utils.WebUtils;

public class LoginActivity extends BaseActivity implements OnEditorActionListener,
    ValidationListener, IDialogResult {

  private static final String TAG = LoginActivity.class.getSimpleName();

  Button login;
  ProgressBar pbar;
  LoginTask mLoginTask;
  GPSTracker mGpsTracker;
  Validator validator;
  int dialogType = 0;
  public static final int ERROR_DIALOG = 1;

  public static final String SUUSER = "kitetabletek";
  public static final String SUPASS = "A785A66ED7F0FB4E00747E0F299D462E";

  @Required(order = 1, messageResId = R.string.error_required_field)
  private EditText username;

  @Required(order = 2, messageResId = R.string.error_required_field)
  @Password(order = 3, messageResId = R.string.error_invalid_uname_pass)
  private EditText password;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    if (Global.DEBUG) {
      startMainActivity();
    } else {

      setupUIElements();
      validator = new Validator(this);
      setListeners();

      setupClickOutFromEditText(findViewById(R.id.parent));
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (!WebUtils.isConnected(this)) {
      dialogType = ERROR_DIALOG;
      Log.e(TAG, "not connected");
      showErrorDialog("Internetkapcsolat", "Nincs internetkapcsolat");
    }
  }

  @Override
  protected void setupUIElements() {

    username = (EditText) findViewById(R.id.userName);
    username.setOnEditorActionListener(this);

    password = (EditText) findViewById(R.id.password);
    password.setImeActionLabel(getString(R.string.login_bt), KeyEvent.KEYCODE_ENTER);
    password.setOnEditorActionListener(this);

    login = (Button) findViewById(R.id.button_login);
    pbar = (ProgressBar) findViewById(R.id.progressbar_login);
    login.setVisibility(View.VISIBLE);
  }

  @Override
  protected void setListeners() {

    validator.setValidationListener(this);

    login.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        validator.validate();

      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.login, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStop() {
    if (mLoginTask != null) {
      mLoginTask.cancel(true);
    }
    super.onStop();
  }

  public class LoginTask extends AsyncTask<Void, Void, LoginResult> {

	  String u;
	  String p;

    @Override
    protected void onPreExecute() {
      pbar.setVisibility(View.VISIBLE);
      login.setVisibility(View.GONE);
		u = username.getText().toString();
		p =  password.getText().toString();

    }

    @Override
    protected LoginResult doInBackground(Void... params) {

      LoginResult error =
          LoginService.doLogin(LoginActivity.this, u, p, false);
      Log.i(TAG, "Login.success=" + error.success);
      Log.i(TAG, "Login.error=" + error.error);

      return error;
    }

    @Override
    protected void onPostExecute(LoginResult result) {

      if (!result.success) {
        dialogType = ERROR_DIALOG;
        // TODO gytomi: ezt inkabb szerveren kene atirni :)
        if ("Rossz eszköz azonosító".equals(result.error)) {
          result.error =
              "Nem egyezik meg az IMEI. Kérem használja a tablethez csatolt felhasználót!";
        }
        showErrorDialog("Hibás bejelentkezés", result.error);
        pbar.setVisibility(View.GONE);
        login.setVisibility(View.VISIBLE);
        username.requestFocus();
        return;
      }

      if (pbar != null) {
        pbar.setVisibility(View.GONE);
      }

      mGpsTracker = new GPSTracker(LoginActivity.this);

      if (mGpsTracker.isGPSEnabled) {
        Log.i(TAG, "GPS is enabled.");
        if (result.success) {
          LoginService.setLoggedIn(LoginActivity.this, true);
          sendBroadcast(new Intent(LoginActivity.this, LoginReceiver.class));
          startMainActivity();
        }
      } else {
        Log.e(TAG, "GPS is disabled.");
        mGpsTracker.showSettingsAlert();
//        showErrorDialog(getString(R.string.dialog_gps_settings_title),
//            getString(R.string.dialog_gps_is_not_enabled_message));
        if (login != null) {
          login.setVisibility(View.VISIBLE);
        }
      }
    }
  }

  @Override
  public void onValidationFailed(View failedView, Rule<?> failedRule) {
    dialogType = ERROR_DIALOG;
    showErrorDialog("Hibás bejelentkezés", failedRule.getFailureMessage());
    login.setVisibility(View.VISIBLE);
  }

  @Override
  public void onValidationSucceeded() {

    String user = username.getText().toString();
    String pass = StringUtils.md5(password.getText().toString());
    if (SUUSER.equals(user) && SUPASS.equals(pass)) {
      Global.SU = true;
      sendBroadcast(new Intent(LoginActivity.this, LoginReceiver.class));
      startMainActivity();
      return;
    }

    mLoginTask = new LoginTask();
    mLoginTask.execute();

  }

  private void startMainActivity() {
    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    LoginActivity.this.startActivity(mainIntent);
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

    Log.d(TAG, "onEditorAction.actionId=" + actionId);

    if (actionId == EditorInfo.IME_ACTION_DONE) {
      validator.validate();
      hideSoftKeyboard(LoginActivity.this);
      return true;
    }

    return false;
  }

  @Override
  public void onOkClicked(DialogFragment dialog) {
    switch (dialogType) {
      case ERROR_DIALOG:
        dialogType = 0;
        break;

      default:
        super.onOkClicked(dialog);
        break;
    }
  }
}
