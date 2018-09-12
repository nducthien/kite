package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.GepSzerzodesTetel;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class GepSzerzodesTetelekTable extends BaseTable<GepSzerzodesTetel> {

	public static final String TABLE_NAME = "gepszerzodestetelek";

	public static final String COL_TETELKOD = "tetelkod";
	public static final String COL_SZERZODESKOD = "szerzodeskod";
	public static final String COL_ALVAZSZAM = "alvazszam";
	public static final String COL_GEPAZONOSITO = "gepazonosito";
	public static final String COL_PARTNERKOD = "partnerkod";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public GepSzerzodesTetelekTable() {
		super(GepSzerzodesTetel.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(GepSzerzodesTetel data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_TETELKOD, Type.TEXT, true),
			new Column(COL_SZERZODESKOD, Type.TEXT, true),
			new Column(COL_ALVAZSZAM, Type.TEXT),
			new Column(COL_GEPAZONOSITO, Type.TEXT),
			new Column(COL_PARTNERKOD, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(GepSzerzodesTetel data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_TETELKOD, data.tetelkod);
		values.put(COL_SZERZODESKOD, data.szerzodeskod);
		values.put(COL_ALVAZSZAM, data.alvazszam);
		values.put(COL_GEPAZONOSITO, data.gepazonosito);
		values.put(COL_PARTNERKOD, data.partnerkod);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public GepSzerzodesTetel getDataFromCursor(Cursor cursor) {

		GepSzerzodesTetel data = new GepSzerzodesTetel();
		data.tetelkod = getString(cursor, COL_TETELKOD);
		data.szerzodeskod = getString(cursor, COL_SZERZODESKOD);
		data.alvazszam = getString(cursor, COL_ALVAZSZAM);
		data.gepazonosito = getString(cursor, COL_GEPAZONOSITO);
		data.partnerkod = getString(cursor, COL_PARTNERKOD);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}