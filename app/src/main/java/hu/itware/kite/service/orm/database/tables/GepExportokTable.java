package hu.itware.kite.service.orm.database.tables;

import android.content.ContentValues;
import android.database.Cursor;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.GepExport;
import hu.itware.kite.service.orm.utils.DateUtils;

public class GepExportokTable extends BaseTable<GepExport> {

	public static final String TABLE_NAME = "gepexportok";

	public static final String COL_SYSTEM = "system";
	public static final String COL_DATABASE = "database";
	public static final String COL_ACT = "act";

	public static final String COL_ALSZ = "alsz";
	public static final String COL_PS = "ps";
	public static final String COL_GNEV = "gnev";
	public static final String COL_NDT = "ndt";
	public static final String COL_NUS = "nus";
	public static final String COL_NTM = "ntm";
	public static final String COL_NUI = "nui";
	public static final String COL_KGW = "kgw";
	public static final String COL_IPS = "ips";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";
	public static final String COL_GYEV = "gyev";


	public GepExportokTable() {
		super(GepExport.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(GepExport data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_SYSTEM, Type.TEXT),
			new Column(COL_DATABASE, Type.TEXT),
			new Column(COL_ACT, Type.TEXT),
			new Column(COL_ALSZ, Type.TEXT, true),
			new Column(COL_PS, Type.TEXT),
			new Column(COL_GNEV, Type.TEXT),
			new Column(COL_NDT, Type.TEXT),
			new Column(COL_NUS, Type.TEXT),
			new Column(COL_NTM, Type.TEXT),
			new Column(COL_NUI, Type.TEXT),
			new Column(COL_KGW, Type.TEXT),
			new Column(COL_IPS, Type.TEXT),
				new Column(COL_GYEV, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(GepExport data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_SYSTEM, data.SYSTEM);
		values.put(COL_DATABASE, data.DATABASE);
		values.put(COL_ACT, data.ACT);
		values.put(COL_ALSZ, data.ALSZ);
		values.put(COL_PS, data.PS);
		values.put(COL_GNEV, data.GNEV);
		values.put(COL_NDT, data.NDT);
		values.put(COL_NUS, data.NUS);
		values.put(COL_NTM, data.NTM);
		values.put(COL_NUI, data.N_UI);
		values.put(COL_KGW, data.KGW);
		values.put(COL_IPS, data.IPS);
		values.put(COL_GYEV, data.GYEV);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public GepExport getDataFromCursor(Cursor cursor) {

		GepExport data = new GepExport();
		data.SYSTEM = getString(cursor, COL_SYSTEM);
		data.DATABASE = getString(cursor, COL_DATABASE);
		data.ACT = getString(cursor, COL_ACT);
		data.ALSZ = getString(cursor, COL_ALSZ);
		data.PS = getString(cursor, COL_PS);
		data.GNEV = getString(cursor, COL_GNEV);
		data.NDT = getString(cursor, COL_NDT);
		data.NUS = getString(cursor, COL_NUS);
		data.NTM = getString(cursor, COL_NTM);
		data.N_UI = getString(cursor, COL_NUI);
		data.KGW = getString(cursor, COL_KGW);
		data.IPS = getString(cursor, COL_IPS);
		data._id = getInt(cursor, COL_BASE_ID);
		data.GYEV = getString(cursor, COL_GYEV);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}