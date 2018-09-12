package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class AlkatreszCikkszamokTable extends BaseTable<AlkatreszCikkszam> {

	public static final String TABLE_NAME = "alkatreszcikkszamok";

	public static final String COL_CIKKSZAM = "cikkszam";
	public static final String COL_SZARMAZAS = "szarmazas";
	public static final String COL_NEV = "nev";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public AlkatreszCikkszamokTable() {
		super(AlkatreszCikkszam.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(AlkatreszCikkszam data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_CIKKSZAM, Type.TEXT),
			new Column(COL_SZARMAZAS, Type.TEXT),
			new Column(COL_NEV, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(AlkatreszCikkszam data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_CIKKSZAM, data.cikkszam);
		values.put(COL_SZARMAZAS, data.szarmazas);
		values.put(COL_NEV, data.nev);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public AlkatreszCikkszam getDataFromCursor(Cursor cursor) {

		AlkatreszCikkszam data = new AlkatreszCikkszam();
		data.cikkszam = getString(cursor, COL_CIKKSZAM);
		data.szarmazas = getString(cursor, COL_SZARMAZAS);
		data.nev = getString(cursor, COL_NEV);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}