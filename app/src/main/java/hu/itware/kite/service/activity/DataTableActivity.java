package hu.itware.kite.service.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.KiteSpinnerAdapter;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.TableMap;

public class DataTableActivity extends Activity {

	private Spinner spinnerTables;
	private ListView listData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_table);

		spinnerTables = (Spinner) findViewById(R.id.datatable_sp_tables);
		listData = (ListView) findViewById(R.id.datatable_list);

		spinnerTables.setAdapter(new KiteSpinnerAdapter<String>(this, R.layout.spinner_large, listTables()));

	}

	private String [] listTables() {

		ArrayList<String> tables = new ArrayList<String>();
		for (BaseTable<?> table : TableMap.getTableHandles()) {
			tables.add(table.getTableName());
		}

		return tables.toArray(new String[tables.size()]);
	}
}
