package hu.itware.kite.service.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Spinner;

import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.KiteSpinnerAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.model.MetaData;

/**
 * Async Task to load MetaData into the given Spinner's adapter...
 * Created by batorig on 2015.10.30..
 */
public class SpinnerMetaDataLoaderTask extends AsyncTask<String, Integer, MetaData[]> {

	private static final String TAG = "SPLOADER";

	private Context context;
	private Spinner spinner;
	private String defaultText;

	public SpinnerMetaDataLoaderTask(Context context, Spinner spinner) {
		this.context = context;
		this.spinner = spinner;
	}

	public SpinnerMetaDataLoaderTask(Context context, Spinner spinner, String defaultText) {
		this.context = context;
		this.spinner = spinner;
		this.defaultText = defaultText;
	}

	@Override
	protected MetaData[] doInBackground(String... params) {

		//--- Filter with parent.
		if (params.length == 3) {
			return KiteDAO.loadMetaDataByTypeId(context, params[0], params[1]);
		} else
		if (params.length == 2) {
			return KiteDAO.loadMetaData(context, params[0], params[1]);
		} else {
			return KiteDAO.loadMetaData(context, params[0]);
		}
	}

	@Override
	protected void onPostExecute(MetaData[] metaDatas) {

		KiteSpinnerAdapter<MetaData> adapter = new KiteSpinnerAdapter<MetaData>(context, R.layout.spinner_large, metaDatas);
		spinner.setAdapter(adapter);
		if (defaultText != null) {
			Log.e(TAG, "Set DefaultText=" + defaultText);
			spinner.setSelection(SpinnerUtils.getMetaDataByText(adapter, defaultText));
		}
	}

}
