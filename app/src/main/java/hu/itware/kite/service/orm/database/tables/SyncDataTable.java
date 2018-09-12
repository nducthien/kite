package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.SyncData;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class SyncDataTable extends BaseTable<SyncData> {

	public static final String TABLE_NAME = "syncdata";

	public static final String COL_TABLENAME = "tablename";
	public static final String COL_UPDATED = "updated";
	public static final String COL_MODE = "mode";
	public static final String COL_LASTVALUE = "lastvalue";
	public static final String COL_SUCCESS = "success";
	public static final String COL_ERROR = "error";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public SyncDataTable() {
		super(SyncData.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(SyncData data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_TABLENAME, Type.TEXT, true),
			new Column(COL_UPDATED, Type.DATE),
			new Column(COL_MODE, Type.TEXT),
			new Column(COL_LASTVALUE, Type.INTEGER),
			new Column(COL_SUCCESS, Type.BOOLEAN),
			new Column(COL_ERROR, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(SyncData data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_TABLENAME, data.tablename);
		values.put(COL_UPDATED, data.updated == null ? null : DateUtils.getDfShort().format(data.updated));
		values.put(COL_MODE, data.mode);
		values.put(COL_LASTVALUE, data.lastValue);
		values.put(COL_SUCCESS, data.success);
		values.put(COL_ERROR, data.error);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public SyncData getDataFromCursor(Cursor cursor) {

		SyncData data = new SyncData();
		data.tablename = getString(cursor, COL_TABLENAME);
		data.updated = getDate(cursor, COL_UPDATED);
		data.mode = getString(cursor, COL_MODE);
		data.lastValue = getLong(cursor, COL_LASTVALUE);
		data.success = getBoolean(cursor, COL_SUCCESS);
		data.error = getString(cursor, COL_ERROR);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}