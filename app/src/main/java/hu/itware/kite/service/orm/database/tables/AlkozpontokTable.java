package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Alkozpont;
import hu.itware.kite.service.orm.utils.DateUtils;

import android.content.ContentValues;
import android.database.Cursor;

public class AlkozpontokTable extends BaseTable<Alkozpont> {

	public static final String TABLE_NAME = "alkozpontok";

	public static final String COL_ALKOZPONTKOD = "alkozpontkod";
	public static final String COL_NEV = "nev";
	public static final String COL_IRSZ = "irsz";
	public static final String COL_TELEPULES = "telepules";
	public static final String COL_CIM = "cim";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public AlkozpontokTable() {
		super(Alkozpont.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(Alkozpont data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_ALKOZPONTKOD, Type.TEXT, true),
			new Column(COL_NEV, Type.TEXT),
			new Column(COL_IRSZ, Type.TEXT),
			new Column(COL_TELEPULES, Type.TEXT),
			new Column(COL_CIM, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(Alkozpont data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_ALKOZPONTKOD, data.alkozpontkod);
		values.put(COL_NEV, data.nev);
		values.put(COL_IRSZ, data.irsz);
		values.put(COL_TELEPULES, data.telepules);
		values.put(COL_CIM, data.cim);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public Alkozpont getDataFromCursor(Cursor cursor) {

		Alkozpont data = new Alkozpont();
		data.alkozpontkod = getString(cursor, COL_ALKOZPONTKOD);
		data.nev = getString(cursor, COL_NEV);
		data.irsz = getString(cursor, COL_IRSZ);
		data.telepules = getString(cursor, COL_TELEPULES);
		data.cim = getString(cursor, COL_CIM);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
	return data;
	}

}