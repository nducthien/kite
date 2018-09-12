package hu.itware.kite.service.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.KiteSpinnerAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.model.MetaData;


/**
 * Spinner utilities. Connect spinners, select MetaData in Spinner, etc...
 * Created by batorig on 2015.11.18..
 */
public final class SpinnerUtils {

	private static final String TAG = "SPUTILS";

	private SpinnerUtils() {

	}

	/**
	 * Source frissitest kuld TargetType-al a Targetnek, ha kivalsztunk Sourceben egy elemet
	 * @param source
	 * @param target
	 * @param targetType
	 */
	public static void connectSpinners(final Context context, final Spinner source, final Spinner target, final String targetType, final String def) {

		target.setTag(targetType);
		source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				MetaData data = (MetaData) source.getSelectedItem();
				createAndSetSpinner(context, target, def, targetType, data == null ? null : data.id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				createAndSetSpinner(context, target, null, targetType);
			}
		});
		if (def != null) {
			select(target, def);
		}
	}

	public static KiteSpinnerAdapter<MetaData> createSpinnerAdapter(Context context, String... params) {

		MetaData[] data = null;
		if (params.length == 3) {
			data = KiteDAO.loadMetaDataByTypeId(context, params[0], params[1]);
		} else
		if (params.length == 2) {
			data = KiteDAO.loadMetaData(context, params[0], params[1]);
		} else {
			data = KiteDAO.loadMetaData(context, params[0]);
		}

		return new KiteSpinnerAdapter<MetaData>(context, R.layout.spinner_large, data);
	}

	public static boolean createAndSetSpinner(Context context, Spinner spinner, String def, String... params) {
		Log.e(TAG, def != null ? def : "def is null");
		if (params != null) {
			for (String param : params) {
				Log.e(TAG, param != null ? param : "param is null");
			}
		} else {
			Log.e(TAG, "params is null");
		}
		spinner.setTag(params[0]);
		KiteSpinnerAdapter<MetaData> adapter = createSpinnerAdapter(context, params);
		spinner.setAdapter(adapter);
		if (def != null) {
			int selectedPos = SpinnerUtils.getMetaDataByText(adapter, def);
			spinner.setSelection(selectedPos);
			return spinner.getSelectedItemPosition() == selectedPos;
		}
		return false;
	}

	public static boolean createSimpleSpinner(Context context, Spinner spinner, String value) {
		spinner.setAdapter(createSimpleAdapter(context, value));
		spinner.setSelection(0);
		return true;
	}

	public static ArrayAdapter<String> createSimpleAdapter(Context context, String... values) {
		return new ArrayAdapter<String>(context, R.layout.spinner_large, values);
	}

	/**
	 * Source frissitest kuld Targeteknek, ha kivalsztunk Sourceben egy elemet.
	 * @param source
	 * @param targets
	 * @param targetTypes
	 */
	public static void connectSpinners(final Context context, final Spinner source, final Spinner[] targets, final String[] targetTypes) {

		source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				MetaData data = (MetaData) source.getSelectedItem();
				for (int it = 0; it < targets.length; it++) {
					new SpinnerMetaDataLoaderTask(context, targets[it]).execute(targetTypes[it], data == null ? null : data.id);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				for (int it = 0; it < targets.length; it++) {
					new SpinnerMetaDataLoaderTask(context, targets[it]).execute(targetTypes[it]);
				}
			}
		});
	}


	public static void printAdapter(Spinner spinner) {
		KiteSpinnerAdapter<MetaData> adapter = (KiteSpinnerAdapter<MetaData>) spinner.getAdapter();
		if (adapter != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				MetaData data = adapter.getItem(i);
			}
		}
	}

	public static int getMetaDataByValue(KiteSpinnerAdapter<MetaData> adapter, String value) {

		if (value == null) {
			return -1;
		}

		for (int i = 0; i < adapter.getCount(); i++) {
			MetaData data = adapter.getItem(i);
			if (value.equals(data.value)) {
				return i;
			}
		}
		return -1;
	}

	public static int getMetaDataById(KiteSpinnerAdapter<MetaData> adapter, String id) {

		if (id == null) {
			return -1;
		}

		for (int i = 0; i < adapter.getCount(); i++) {
			MetaData data = adapter.getItem(i);
			if (id.equals(data.id)) {
				return i;
			}
		}
		return -1;
	}

	public static int getMetaDataByText(KiteSpinnerAdapter<MetaData> adapter, String text) {

		if (text == null) {
			return -1;
		}

		for (int i = 0; i < adapter.getCount(); i++) {
			MetaData data = adapter.getItem(i);
			if (text.equals(data.text)) {
				return i;
			}
		}
		return -1;
	}

	public static void select(Spinner spinner, String def) {
		KiteSpinnerAdapter<MetaData> adapter = (KiteSpinnerAdapter<MetaData>) spinner.getAdapter();
		if (adapter != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				MetaData data = adapter.getItem(i);
				if (data != null && def != null && def.equals(data.text)) {
					spinner.setSelection(i);
				}
			}
		}
	}

	/**
	 * Select text started with start string...
	 * @param spinner sprinner which selection to be set
	 * @param start the start of the text which will be selected
	 */
	public static void selectAtStart(Spinner spinner, String start) {
		if (spinner.getAdapter() instanceof  KiteSpinnerAdapter) {
			KiteSpinnerAdapter<MetaData> adapter = (KiteSpinnerAdapter<MetaData>) spinner.getAdapter();
			if (adapter != null) {
				for (int i = 0; i < adapter.getCount(); i++) {
					MetaData data = adapter.getItem(i);
					if (data.text != null && start != null && data.text.startsWith(start)) {
						spinner.setSelection(i);
						return;
					}
				}
			}
			spinner.setSelection(0);
		}
	}

	public static String getText(Spinner spinner) {
		Object o = spinner.getSelectedItem();
		if (o != null && o instanceof MetaData) {
			return ((MetaData)o).text;
		} else if (o != null && o instanceof String) {
			return (String)o;
		}
		return null;
	}

	public static String getValue(Spinner spinner) {
		Object o = spinner.getSelectedItem();
		if (o != null && o instanceof MetaData) {
			return ((MetaData)o).value;
		} else if (o != null && o instanceof String) {
			return (String)o;
		}
		return null;
	}
}
