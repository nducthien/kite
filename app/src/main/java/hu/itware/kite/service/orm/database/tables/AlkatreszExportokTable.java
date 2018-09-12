package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.AlkatreszExport;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class AlkatreszExportokTable extends BaseTable<AlkatreszExport> {

	public static final String TABLE_NAME = "alkatreszexport";

	public static final String COL_SYSTEM = "system";
	public static final String COL_DATABASE = "database";
	public static final String COL_GECW = "gecw";
	public static final String COL_GE = "ge";
	public static final String COL_GEW = "gew";
	public static final String COL_CI = "ci";
	public static final String COL_CNEV = "cnev";
	public static final String COL_MOME = "mome";
	public static final String COL_MEE = "mee";
	public static final String COL_BSZ = "bsz";
	public static final String COL_GETC2 = "getc2";
	public static final String COL_SZ = "sz";
	public static final String COL_SZTS = "szts";
	public static final String COL_AG = "ag";
	public static final String COL_BTS = "bts";
	public static final String COL_K0 = "k0";
	public static final String COL_MOTI = "moti";
	public static final String COL_NDT = "ndt";
	public static final String COL_NTM = "ntm";
	public static final String COL_NUI = "nui";
	public static final String COL_NUS = "nus";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public AlkatreszExportokTable() {
		super(AlkatreszExport.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(AlkatreszExport data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_SYSTEM, Type.TEXT),
			new Column(COL_DATABASE, Type.TEXT),
			new Column(COL_GECW, Type.TEXT, true),
			new Column(COL_GE, Type.TEXT, true),
			new Column(COL_GEW, Type.TEXT, true),
			new Column(COL_CI, Type.TEXT),
			new Column(COL_CNEV, Type.TEXT),
			new Column(COL_MOME, Type.TEXT),
			new Column(COL_MEE, Type.TEXT),
			new Column(COL_BSZ, Type.TEXT),
			new Column(COL_GETC2, Type.TEXT),
			new Column(COL_SZ, Type.TEXT),
			new Column(COL_SZTS, Type.TEXT),
			new Column(COL_AG, Type.TEXT),
			new Column(COL_BTS, Type.TEXT),
			new Column(COL_K0, Type.TEXT),
			new Column(COL_MOTI, Type.TEXT),
			new Column(COL_NDT, Type.TEXT),
			new Column(COL_NTM, Type.TEXT),
			new Column(COL_NUI, Type.TEXT),
			new Column(COL_NUS, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(AlkatreszExport data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_SYSTEM, data.SYSTEM);
		values.put(COL_DATABASE, data.DATABASE);
		values.put(COL_GECW, data.GECW);
		values.put(COL_GE, data.GE);
		values.put(COL_GEW, data.GEW);
		values.put(COL_CI, data.CI);
		values.put(COL_CNEV, data.CNEV);
		values.put(COL_MOME, data.MOME);
		values.put(COL_MEE, data.MEE);
		values.put(COL_BSZ, data.BSZ);
		values.put(COL_GETC2, data.GETC2);
		values.put(COL_SZ, data.SZ);
		values.put(COL_SZTS, data.SZTS);
		values.put(COL_AG, data.AG);
		values.put(COL_BTS, data.BTS);
		values.put(COL_K0, data.K0);
		values.put(COL_MOTI, data.MOTI);
		values.put(COL_NDT, data.NDT);
		values.put(COL_NTM, data.NTM);
		values.put(COL_NUI, data.N_UI);
		values.put(COL_NUS, data.NUS);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public AlkatreszExport getDataFromCursor(Cursor cursor) {

		AlkatreszExport data = new AlkatreszExport();
		data.SYSTEM = getString(cursor, COL_SYSTEM);
		data.DATABASE = getString(cursor, COL_DATABASE);
		data.GECW = getString(cursor, COL_GECW);
		data.GE = getString(cursor, COL_GE);
		data.GEW = getString(cursor, COL_GEW);
		data.CI = getString(cursor, COL_CI);
		data.CNEV = getString(cursor, COL_CNEV);
		data.MOME = getString(cursor, COL_MOME);
		data.MEE = getString(cursor, COL_MEE);
		data.BSZ = getString(cursor, COL_BSZ);
		data.GETC2 = getString(cursor, COL_GETC2);
		data.SZ = getString(cursor, COL_SZ);
		data.SZTS = getString(cursor, COL_SZTS);
		data.AG = getString(cursor, COL_AG);
		data.BTS = getString(cursor, COL_BTS);
		data.K0 = getString(cursor, COL_K0);
		data.MOTI = getString(cursor, COL_MOTI);
		data.NDT = getString(cursor, COL_NDT);
		data.NTM = getString(cursor, COL_NTM);
		data.N_UI = getString(cursor, COL_NUI);
		data.NUS = getString(cursor, COL_NUS);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}