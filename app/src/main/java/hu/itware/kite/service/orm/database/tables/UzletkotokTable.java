package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Uzletkoto;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class UzletkotokTable extends BaseTable<Uzletkoto> {

	public static final String TABLE_NAME = "uzletkotok";

	public static final String COL_UZLETKOTOKOD = "uzletkotokod";
	public static final String COL_TOSZ = "tosz";
	public static final String COL_NEV1 = "nev1";
	public static final String COL_NEV2 = "nev2";
	public static final String COL_PASSWORD = "password";
	public static final String COL_ALKOZPONTKOD = "alkozpontkod";
	public static final String COL_NUI = "nui";
	public static final String COL_AZON = "azon";
	public static final String COL_JOGOSULTSAG = "jogosultsag";
	public static final String COL_SZERVIZESKOD = "szervizeskod";
	public static final String COL_UGYVITELIKOD = "ugyvitelikod";
	public static final String COL_TELJESNEV = "teljesnev";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public UzletkotokTable() {
		super(Uzletkoto.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(Uzletkoto data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_UZLETKOTOKOD, Type.TEXT, true),
			new Column(COL_TOSZ, Type.TEXT, true),
			new Column(COL_NEV1, Type.TEXT),
			new Column(COL_NEV2, Type.TEXT),
			new Column(COL_PASSWORD, Type.TEXT),
			new Column(COL_ALKOZPONTKOD, Type.TEXT),
			new Column(COL_NUI, Type.TEXT),
			new Column(COL_AZON, Type.TEXT),
			new Column(COL_JOGOSULTSAG, Type.TEXT),
			new Column(COL_SZERVIZESKOD, Type.TEXT),
			new Column(COL_UGYVITELIKOD, Type.TEXT),
			new Column(COL_TELJESNEV, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(Uzletkoto data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_UZLETKOTOKOD, data.uzletkotokod);
		values.put(COL_TOSZ, data.tosz);
		values.put(COL_NEV1, data.nev1);
		values.put(COL_NEV2, data.nev2);
		values.put(COL_PASSWORD, data.password);
		values.put(COL_ALKOZPONTKOD, data.alkozpontkod);
		values.put(COL_NUI, data.nui);
		values.put(COL_AZON, data.azon);
		values.put(COL_JOGOSULTSAG, data.jogosultsag);
		values.put(COL_SZERVIZESKOD, data.szervizeskod);
		values.put(COL_UGYVITELIKOD, data.ugyvitelikod);
		values.put(COL_TELJESNEV, data.teljesNev);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public Uzletkoto getDataFromCursor(Cursor cursor) {

		Uzletkoto data = new Uzletkoto();
		data.uzletkotokod = getString(cursor, COL_UZLETKOTOKOD);
		data.tosz = getString(cursor, COL_TOSZ);
		data.nev1 = getString(cursor, COL_NEV1);
		data.nev2 = getString(cursor, COL_NEV2);
		data.password = getString(cursor, COL_PASSWORD);
		data.alkozpontkod = getString(cursor, COL_ALKOZPONTKOD);
		data.nui = getString(cursor, COL_NUI);
		data.azon = getString(cursor, COL_AZON);
		data.jogosultsag = getString(cursor, COL_JOGOSULTSAG);
		data.szervizeskod = getString(cursor, COL_SZERVIZESKOD);
		data.ugyvitelikod = getString(cursor, COL_UGYVITELIKOD);
		data.teljesNev = getString(cursor, COL_TELJESNEV);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}