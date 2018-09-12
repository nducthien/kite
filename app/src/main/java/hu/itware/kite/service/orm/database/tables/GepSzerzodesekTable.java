package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.GepSzerzodes;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;
import android.util.Log;

public class GepSzerzodesekTable extends BaseTable<GepSzerzodes> {

	public static final String TABLE_NAME = "gepszerzodesek";

	public static final String COL_SZERZODESKOD 	= "szerzodeskod";
	public static final String COL_SZERZODESTIPUS 	= "szerzodestipus";
	public static final String COL_KEZDESDATUM 		= "kezdesdatum";
	public static final String COL_LEJARATDATUM 	= "lejaratdatum";
	public static final String COL_ELLENORIZVE1 	= "ellenorizve1";
	public static final String COL_ELLENORIZVE2 	= "ellenorizve2";

	//--- KITE-796
	public static final String COL_PARTNERKOD 		= "partnerkod";
	public static final String COL_LETREHOZASDATUMA = "letrehozasdatum";
	public static final String COL_EMAIL 			= "email";

	public static final String COL_MODIFIED 		= "modified";
	public static final String COL_STATUS 			= "status";


	public GepSzerzodesekTable() {
		super(GepSzerzodes.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(GepSzerzodes data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_SZERZODESKOD, Type.TEXT, true),
			new Column(COL_SZERZODESTIPUS, Type.TEXT),
			new Column(COL_KEZDESDATUM, Type.DATE),
			new Column(COL_LEJARATDATUM, Type.DATE),
			new Column(COL_ELLENORIZVE1, Type.TEXT),
			new Column(COL_ELLENORIZVE2, Type.TEXT),
			new Column(COL_PARTNERKOD, Type.TEXT),
			new Column(COL_LETREHOZASDATUMA, Type.DATE),
			new Column(COL_EMAIL, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
	}

	@Override
	public ContentValues getContentValues(GepSzerzodes data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_SZERZODESKOD, data.szerzodeskod);
		values.put(COL_SZERZODESTIPUS, data.szerzodestipus);
		values.put(COL_KEZDESDATUM, data.kezdesdatum == null ? null : DateUtils.getDfShort().format(data.kezdesdatum));
		values.put(COL_LEJARATDATUM, data.lejaratdatum == null ? null : DateUtils.getDfShort().format(data.lejaratdatum));
		values.put(COL_ELLENORIZVE1, data.ellenorizve1);
		values.put(COL_ELLENORIZVE2, data.ellenorizve2);

		//--- KITE-796
		values.put(COL_PARTNERKOD, data.partnerkod);
		values.put(COL_LETREHOZASDATUMA, data.letrehozasdatum == null ? null : DateUtils.getDfShort().format(data.letrehozasdatum));
		values.put(COL_EMAIL, data.email);

		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public GepSzerzodes getDataFromCursor(Cursor cursor) {

		GepSzerzodes data = new GepSzerzodes();
		data.szerzodeskod = getString(cursor, COL_SZERZODESKOD);
		data.szerzodestipus = getString(cursor, COL_SZERZODESTIPUS);
		data.kezdesdatum = getDate(cursor, COL_KEZDESDATUM);
		data.lejaratdatum = getDate(cursor, COL_LEJARATDATUM);
		data.ellenorizve1 = getString(cursor, COL_ELLENORIZVE1);
		data.ellenorizve2 = getString(cursor, COL_ELLENORIZVE2);

		//--- KITE-796
		data.letrehozasdatum = getDate(cursor, COL_LETREHOZASDATUMA);
		data.partnerkod = getString(cursor, COL_PARTNERKOD);
		data.email = getString(cursor, COL_EMAIL);

		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}