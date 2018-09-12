package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.GPSData;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class GPSDataTable extends BaseTable<GPSData> {

	public static final String TABLE_NAME = "gpsdata";

	public static final String COL_IMEI = "imei";
	public static final String COL_DATE = "date";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public GPSDataTable() {
		super(GPSData.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(GPSData data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_IMEI, Type.TEXT, true),
			new Column(COL_DATE, Type.DATE, true),
			new Column(COL_LATITUDE, Type.DOUBLE),
			new Column(COL_LONGITUDE, Type.DOUBLE),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(GPSData data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_IMEI, data.imei);
		values.put(COL_DATE, data.date == null ? null : DateUtils.getDfShort().format(data.date));
		values.put(COL_LATITUDE, data.latitude);
		values.put(COL_LONGITUDE, data.longitude);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public GPSData getDataFromCursor(Cursor cursor) {

		GPSData data = new GPSData();
		data.imei = getString(cursor, COL_IMEI);
		data.date = getDate(cursor, COL_DATE);
		data.latitude = getDouble(cursor, COL_LATITUDE);
		data.longitude = getDouble(cursor, COL_LONGITUDE);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}