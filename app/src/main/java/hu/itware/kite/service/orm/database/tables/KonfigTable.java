package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Konfig;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class KonfigTable extends BaseTable<Konfig> {

	public static final String TABLE_NAME = "konfig";

	public static final String COL_NAME = "name";
	public static final String COL_TYPE = "type";
	public static final String COL_VALUE = "value";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public KonfigTable() {
		super(Konfig.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(Konfig data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_NAME, Type.TEXT, true),
			new Column(COL_TYPE, Type.TEXT),
			new Column(COL_VALUE, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(Konfig data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_NAME, data.name);
		values.put(COL_TYPE, data.type);
		values.put(COL_VALUE, data.value);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public Konfig getDataFromCursor(Cursor cursor) {

		Konfig data = new Konfig();
		data.name = getString(cursor, COL_NAME);
		data.type = getString(cursor, COL_TYPE);
		data.value = getString(cursor, COL_VALUE);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}