package hu.itware.kite.service.activity;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.florent37.viewtooltip.ViewTooltip;

import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.SyncData;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.sync.SyncAdapter;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.utils.Global;
import hu.itware.kite.service.utils.SystemUtils;

public class MainActivity extends BaseActivity {

	public static final String TAG = "KITE.UI";
	Button newMunkalapButton;
	Button continueMunkalapButton;
	Button copyMunkalapButton;
	Button ownListButton;
	Button machineInfoButton;
	Button partnerInfoButton;
	Button infoButton;

	private int backPressCount = 0;
	private AsyncTask<Integer, Void, Boolean> backTask;

	int dialogType = 0;
	public static boolean activityIsVisible = false;

	private boolean encrypted = true;

	private int encryptStatus = -1;

	private static final int REQUEST_CODE_START_ENCRYPTION = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String version = getString(R.string.version_string, SystemUtils.getVersion(this));
		setText(R.id.text_version, version);

		setupUIElements();
		setListeners();
	}

	private void initPage() {
		Log.e(TAG, "MUNKALAPKOD=" + KiteDAO.getSzervizesMunkalapAlapKod(this));
	}

	private void enableDisablePage(boolean enable) {
		enable(R.id.button_main_continue_munkalap, enable);
		enable(R.id.button_main_copy_munkalap, enable);
		enable(R.id.button_main_info, enable);
		enable(R.id.button_main_machine_info, enable);
		enable(R.id.button_main_new_munkalap, enable);
		enable(R.id.button_main_partner_info, enable);
		if (enable) {
			hide(R.id.main_text_noencrypt);
		} else {
			show(R.id.main_text_noencrypt);
		}
	}


	private void initPageSync(List<SyncData> result) {

		boolean ok = true;
		if (result != null) {
			for (SyncData d : result) {
				if (SyncAdapter.MODE_DOWNLOAD.equals(d.mode) && d.lastValue == 0) {
					ok = false;
					break;
				}
			}
		}

		if (!ok) {
			dialogType = SYNC_DIALOG;
			if (activityIsVisible) {
				showQuestionDialog(getString(R.string.dialog_sync_needed_title),
					getString(R.string.dialog_sync_needed_message));
			}
		} else {

			Editor edit = getSharedPreferences("DBSTATE", Context.MODE_PRIVATE).edit();
			edit.putBoolean("DBINSTALLED", true);
			edit.apply();
		}
	}

	@Override
	protected void setupUIElements() {
		newMunkalapButton = (Button) findViewById(R.id.button_main_new_munkalap);
		continueMunkalapButton = (Button) findViewById(R.id.button_main_continue_munkalap);
		copyMunkalapButton = (Button) findViewById(R.id.button_main_copy_munkalap);
		ownListButton = (Button) findViewById(R.id.button_main_own_munkalap);
		machineInfoButton = (Button) findViewById(R.id.button_main_machine_info);
		partnerInfoButton = (Button) findViewById(R.id.button_main_partner_info);
		infoButton = (Button) findViewById(R.id.button_main_info);

		final Uzletkoto uzletkoto = LoginService.getManager(this);

		ImageButton buttonInfo = (ImageButton) findViewById(R.id.main_button_info);
		if (buttonInfo != null) {
			buttonInfo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ViewTooltip
							.on(view)
							.autoHide(true, 3000)
							.corner(20)
							.textColor(Color.WHITE)
							.color(Color.BLACK)
							.clickToHide(true)
							.position(ViewTooltip.Position.LEFT)
							.align(ViewTooltip.ALIGN.START)
							.text("<b>Szervizeskód:" + uzletkoto.szervizeskod + "</b><br>Üzletkötőkód:" +
									(TextUtils.isEmpty(uzletkoto.uzletkotokod) ? "-" : uzletkoto.uzletkotokod) + "<br><i>Azonosító:" + (TextUtils.isEmpty(uzletkoto.azon) ? "-" : uzletkoto.azon) + "</i>")
							.show();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		activityIsVisible = true;
		Log.e(TAG, "MainActivity.onResume()");
		refreshStates();
	}

	private void refreshStates() {
//		continueMunkalapButton.setText(getString(R.string.main_continue_munkalap_with_count, Integer.toString(KiteDAO.getUnfinishedMunkalapCount(this))));
//		copyMunkalapButton.setText(getString(R.string.main_copy_munkalap_with_count, Integer.toString(KiteDAO.getPreviousMunkalapCount(this))));
//		initPage();

		AsyncTask<Void, Void, ArrayList<Integer>> task = new AsyncTask<Void, Void, ArrayList<Integer>>() {
			@Override
			protected ArrayList<Integer> doInBackground(Void... voids) {
				ArrayList<Integer> results = new ArrayList<>();
				results.add(KiteDAO.getUnfinishedMunkalapCount(MainActivity.this));
				results.add(KiteDAO.getPreviousMunkalapCount(MainActivity.this));
				return results;
			}

			@Override
			protected void onPostExecute(ArrayList<Integer> results) {
				continueMunkalapButton.setText(getString(R.string.main_continue_munkalap_with_count, Integer.toString(results.get(0))));
				copyMunkalapButton.setText(getString(R.string.main_copy_munkalap_with_count, Integer.toString(results.get(1))));
				initPage();
			}
		};
		task.execute();
	}



	private void startActivityByButton(Intent i) {
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_main, menu);
		Log.e(TAG, "Encrypt=" + encrypted + ", status=" + encryptStatus);
		boolean created = super.onCreateOptionsMenu(menu);

		if (encrypted) {
			menu.findItem(R.id.action_encrypt).setVisible(false);
			hide(R.id.main_text_noencrypt);
		} else {
			show(R.id.main_text_noencrypt);
			menu.findItem(R.id.action_encrypt).setVisible(true);
			menu.findItem(R.id.action_sync).setVisible(false);
			menu.findItem(R.id.action_open_items).setVisible(false);
		}

		return created;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id) {
			case R.id.action_encrypt: {
				Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
				startActivityForResult(intent, REQUEST_CODE_START_ENCRYPTION);
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void doTest() {

		try {
			KiteORM orm = new KiteORM(this);
			Partner partner = orm.loadSingle(Partner.class, "partnerkod = ?", new String[]{"999999"});
			Log.e(TAG, "Partner.999999.id=" + partner._id);
			partner.partnerkod = "100001";
			long id = orm.update(partner);
			Log.e(TAG, "Updated.id=" + id);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}

	private void logout() {
		dialogType = LOGOUT_DIALOG;
		showQuestionDialog(getString(R.string.st_dialog_logout_title),
			getString(R.string.st_dialog_logout_message));
	}

	@Override
	protected void onDestroy() {

		if (Global.SU) {
			Log.i(TAG, "Sign out SU");
			Global.SU = false;
		}

		super.onDestroy();
	}

	@Override
	public void onCancelClicked(DialogFragment dialog) {
		super.onCancelClicked(dialog);
	}

	@Override
	protected void onPause() {
		activityIsVisible = false;
		super.onPause();

		if (backTask != null) {
			backTask.cancel(true);
		}
	}

	@Override
	public void onBackPressed() {

		backPressCount++;

		if (backPressCount == 1) {
			Toast.makeText(this, R.string.main_exit_message, Toast.LENGTH_SHORT).show();
			backTask = new AsyncTask<Integer, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Integer... params) {

					try {
						Thread.sleep(2000);
					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					}

					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					backPressCount = 0;
				}
			};
			backTask.execute(2000);
		}

		if (backPressCount == 2) {
			if (backTask != null) {
				backTask.cancel(true);
			}
			finish();
		}
	}

	@Override
	protected void setListeners() {
		newMunkalapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				if (KiteDAO.needSynchronization(MainActivity.this)) {
					showErrorDialog(getString(R.string.dialog_sync_needed_title), getString(R.string.dialog_sync_needed_message2));
					return;
				}

				if (hasUzletkotoAzon()) {
					Intent intent = new Intent(getApplicationContext(), MunkalapActivity.class);
					intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_CREATE_NEW);
					startActivity(intent);
				} else {
					showUzletkotoAzonErrorDialog();
				}
			}
		});

		continueMunkalapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), MunkalapActivity.class);
				intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_CONTINUE);
				startActivity(intent);
			}
		});

		copyMunkalapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (hasUzletkotoAzon()) {
					Intent intent = new Intent(getApplicationContext(), MunkalapActivity.class);
					intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_COPY);
					startActivity(intent);
				} else {
					showUzletkotoAzonErrorDialog();
				}
			}
		});

		machineInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MachineDetailsActivity.class);
				startActivity(intent);
			}
		});

		ownListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), MunkalapActivity.class);
				intent.putExtra(MunkalapActivity.MUNKALAP_MODE, MunkalapActivity.MODE_OWN);
				intent.putExtra(MunkalapActivity.MUNKALAP_READONLY, true);
				startActivity(intent);
			}
		});

		partnerInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), PartnerDetailsActivity.class);
				startActivity(intent);
			}
		});

		infoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), InformaciosAnyagokActivity.class);
				startActivity(intent);
			}
		});
	}
}
