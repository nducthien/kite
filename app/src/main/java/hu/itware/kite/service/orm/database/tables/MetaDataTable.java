package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.MetaData;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class MetaDataTable extends BaseTable<MetaData> {

	public static final String TABLE_NAME = "metadata";

	public static final String COL_ID = "id";
	public static final String COL_TYPE = "type";
	public static final String COL_TEXT = "text";
	public static final String COL_VALUE = "value";
	public static final String COL_POS = "pos";
	public static final String COL_PARENTIDS = "parentids";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public MetaDataTable() {
		super(MetaData.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(MetaData data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_ID, Type.TEXT, true),
			new Column(COL_TYPE, Type.TEXT, true),
			new Column(COL_TEXT, Type.TEXT, true),
			new Column(COL_VALUE, Type.TEXT),
			new Column(COL_POS, Type.INTEGER),
			new Column(COL_PARENTIDS, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(MetaData data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_ID, data.id);
		values.put(COL_TYPE, data.type);
		values.put(COL_TEXT, data.text);
		values.put(COL_VALUE, data.value);
		values.put(COL_POS, data.pos);
		values.put(COL_PARENTIDS, data.parentids);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public MetaData getDataFromCursor(Cursor cursor) {

		MetaData data = new MetaData();
		data.id = getString(cursor, COL_ID);
		data.type = getString(cursor, COL_TYPE);
		data.text = getString(cursor, COL_TEXT);
		data.value = getString(cursor, COL_VALUE);
		data.pos = getInt(cursor, COL_POS);
		data.parentids = getString(cursor, COL_PARENTIDS);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}