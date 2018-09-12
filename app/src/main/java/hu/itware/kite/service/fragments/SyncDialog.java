package hu.itware.kite.service.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.model.SyncData;
import hu.itware.kite.service.orm.sync.SyncAdapter;
import hu.itware.kite.service.services.NetworkStateTimerTask;

public class SyncDialog extends CustomDialog {
	
	public static final int TEXT_COMMAND_TITLE = 100;
	
	public static final int TEXT_COMMAND_TABLENAME = 101;
	
	public static final int TEXT_COMMAND_COUNT = 102;
	
	public static final int TEXT_COMMAND_MESSAGE  = 103;
	
	public static final int TEXT_COMMAND_PROGRESS  = 104;
	
	private static final String TAG = "KITE.DLG.SYNC";

	private IDialogResult listener;
	
	private TextView table;
	
	private TextView message;
	
	private TextView count;
	
	private TextView title;
	
	private ProgressBar progress;
	
	private Button button;
	
	private int max;
	
	protected int sumCount = 0;
	protected int recordSum = 0;
	
	private int progressCount = 0;
	
	private static class Summary {
		boolean hasError = false;
		String error = "";
		int count;
	}

	private Summary summary = new Summary();

	public SyncDialog() {
		this.setCancelable(false);
	}

	public void setListener(IDialogResult listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.dialog_sync_progress, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCancelable(false);

		title = (TextView) view.findViewById(R.id.dialog_sync_title);
		title.setText(getArguments().getString("title"));
		
		table = (TextView) view.findViewById(R.id.dialog_sync_table_text);
		table.setText(getArguments().getString("table"));

		count = (TextView) view.findViewById(R.id.dialog_sync_table_count);
		count.setText(getArguments().getString("count"));
		
		message = (TextView) view.findViewById(R.id.dialog_sync_text);
		message.setText(getArguments().getString("message"));
		message.setMovementMethod(new ScrollingMovementMethod());
		
		progress = (ProgressBar) view.findViewById(R.id.dialog_sync_progressbar);
		
		button = (Button) view.findViewById(R.id.dialog_sync_button_right);
		button.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
		button.setVisibility(View.GONE);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onOkClicked(SyncDialog.this);
				}
				dismiss();
			}
		});
		
		setVisible(view, R.id.dialog_sync_button_right, false);

		return view;
	}
	
	public void doProgress(int command, Serializable data) {
		
		Log.d(TAG, "Info Command:" + command + ", data=" + data);
		SyncData sync;
		switch (command) {
			case SyncAdapter.COMMAND_START:
				progress.setMax((Integer)data);
				progress.setProgress(0);
				progressCount = 0;
				break;

			case SyncAdapter.COMMAND_START_SINGLE_SYNC:
				sumCount = 0;
				summary.hasError = false;

				sync = (SyncData) data;
				if (sync != null) {
					table.setText(sync.tablename);
					message.setText("Tábla szinkronizálása:" + sync.tablename);
				} else {
					table.setText("");
					message.setText("");
				}
				progress.setProgress(progressCount++);
				break;

			case SyncAdapter.COMMAND_END_SINGLE_SYNC:
				sync = (SyncData) data;
				if (sync != null) {
					table.setText(sync.tablename + " - Kész!");
					message.setText("Tábla szinkronizálása kész:" + sync.tablename);
					NetworkStateTimerTask.setDownloadNeeded(sync.tablename, false);
					break;
				}
				table.setText("");
				message.setText("");
				break;

			case SyncAdapter.COMMAND_ITEMCOUNT:
				max = (Integer) data;
				summary.count += max;
				count.setText(max + " db");
				break;

			case SyncAdapter.COMMAND_CURRENTITEM:
				sumCount++;
				count.setText(data + " db");
				recordSum = (Integer) data;
				break;

			case SyncAdapter.COMMAND_START_INSERT:
				message.setText(data + " db adat beszúrása az adatbázisba...");
				break;

			case SyncAdapter.COMMAND_END_INSERT:
				message.setText("Adat beszúrása sikeres! (" + data + " db)");
				break;

			case SyncAdapter.COMMAND_DOWNLOADDATA:
				String t = (String)data;
				if ("S".equals(t)) {
					message.setText("Adatok letöltése a szerverről: folyamatban...");
				} else {
					message.setText("Adatok letöltése a szerverről: SIKERES!");
				}
				break;

			case SyncAdapter.COMMAND_DOWNLOADIMAGE:
				String downloadImageName = (String)data;
				message.setText("Kép letöltése a szerverről: " + downloadImageName);
				break;

			case SyncAdapter.COMMAND_UPLOADIMAGE:
				String uploadImageName = (String)data;
				message.setText("Kép feltöltése a szerverre: " + uploadImageName);
				break;

			case SyncAdapter.COMMAND_UPLOADVIDEO:
				message.setText("Video feltöltése a szerverre: " + (String)data);
				break;

			case SyncAdapter.COMMAND_DOWNLOADPIP:
				String downloadPipName = (String)data;
				message.setText("PIP pdf letöltése a szerverről: " + downloadPipName);
				break;

			case SyncAdapter.COMMAND_ERROR:
				String error = (String)data;
				message.setText("Hiba történt: " + error);
				summary.hasError = true;
				summary.error += error + "\n";
				break;

			case SyncAdapter.COMMAND_AUTH:
				message.setText("Üzletkötő azonosítása...");
				break;

			case TEXT_COMMAND_COUNT:
				count.setText((String)data);
				break;

			case TEXT_COMMAND_TITLE:
				title.setText((String)data);
				break;

			case TEXT_COMMAND_TABLENAME:
				table.setText((String)data);
				break;

			case TEXT_COMMAND_MESSAGE:
				message.setText((String)data);
				break;

			case TEXT_COMMAND_PROGRESS:
				progress.setProgress((Integer)data);
				break;

			default:
				break;
		}		
		
		if (command == SyncAdapter.COMMAND_END) {
			showSummary();
		}
	}

	private void showSummary() {
		progress.setProgress(progress.getMax());
		hide(R.id.dialog_sync_progress);
		String text;
		if (!summary.hasError) {
			text = "Adatok szinkronizálása SIKERESEN befejeződött.\n";
			text += recordSum + " rekord szinkronizálása sikeresen megtörtént.";
		} else {
			text = "Adatok szinkronizálása befejeződött.\n";
			text += "Hiba történt a szinkronizáció során:\n";
			text += summary.error;
		}
		message.setText(text);		
		button.setVisibility(View.VISIBLE);
}

	public TextView getTitle() {
		return title;
	}
	
	public TextView getCount() {
		return count;
	}
	
	public TextView getMessage() {
		return message;
	}
	
	public TextView getTable() {
		return table;
	}
	
	public ProgressBar getProgress() {
		return progress;
	}
}
