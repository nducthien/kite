package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Alkatresz;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class AlkatreszekTable extends BaseTable<Alkatresz> {

	public static final String TABLE_NAME = "alkatreszek";

	public static final String COL_MUNKALAPKOD = "munkalapkod";
	public static final String COL_TEMPKOD = "tempkod";
	public static final String COL_ALKATRESZKOD = "alkatreszkod";
	public static final String COL_CIKKSZAM = "cikkszam";
	public static final String COL_CIKKNEV = "cikknev";
	public static final String COL_RENDSZER = "rendszer";
	public static final String COL_MOZGOMENNYISEG = "mozgomennyiseg";
	public static final String COL_MENNYISEGIEGYSEG = "mennyisegiegyseg";
	public static final String COL_SORSZAM = "sorszam";
	public static final String COL_AGAZATISZAM = "agazatiszam";
	public static final String COL_ROGZITESDATUM = "rogzitesdatum";
	public static final String COL_BIZONYLATSZAM = "bizonylatszam";
	public static final String COL_MUNKALAPSORSZAM = "munkalapsorszam";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";
	public static final String COL_MOZGASAZONOSITO = "mozgasazonosito";


	public AlkatreszekTable() {
		super(Alkatresz.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(Alkatresz data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_MUNKALAPKOD, Type.TEXT, true),
			new Column(COL_TEMPKOD, Type.TEXT, true),
			new Column(COL_ALKATRESZKOD, Type.TEXT, true),
			new Column(COL_CIKKSZAM, Type.TEXT, true),
			new Column(COL_CIKKNEV, Type.TEXT),
			new Column(COL_RENDSZER, Type.TEXT),
			new Column(COL_MOZGOMENNYISEG, Type.DOUBLE),
			new Column(COL_MENNYISEGIEGYSEG, Type.TEXT),
			new Column(COL_SORSZAM, Type.TEXT),
			new Column(COL_AGAZATISZAM, Type.TEXT),
			new Column(COL_ROGZITESDATUM, Type.DATE),
			new Column(COL_BIZONYLATSZAM, Type.TEXT, true),
			new Column(COL_MUNKALAPSORSZAM, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT),
			new Column(COL_MOZGASAZONOSITO, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(Alkatresz data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_MUNKALAPKOD, data.munkalapkod);
		values.put(COL_TEMPKOD, data.tempkod);
		values.put(COL_ALKATRESZKOD, data.alkatreszkod);
		values.put(COL_CIKKSZAM, data.cikkszam);
		values.put(COL_CIKKNEV, data.cikknev);
		values.put(COL_RENDSZER, data.rendszer);
		values.put(COL_MOZGOMENNYISEG, data.mozgomennyiseg);
		values.put(COL_MENNYISEGIEGYSEG, data.mennyisegiegyseg);
		values.put(COL_SORSZAM, data.sorszam);
		values.put(COL_AGAZATISZAM, data.agazatiszam);
		values.put(COL_ROGZITESDATUM, data.rogzitesdatum == null ? null : DateUtils.getDfShort().format(data.rogzitesdatum));
		values.put(COL_BIZONYLATSZAM, data.bizonylatszam);
		values.put(COL_MUNKALAPSORSZAM, data.munkalapsorszam);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		values.put(COL_MOZGASAZONOSITO, data.mozgasazonosito);
		return values;
	}

	@Override
	public Alkatresz getDataFromCursor(Cursor cursor) {

		Alkatresz data = new Alkatresz();
		data.munkalapkod = getString(cursor, COL_MUNKALAPKOD);
		data.tempkod = getString(cursor, COL_TEMPKOD);
		data.alkatreszkod = getString(cursor, COL_ALKATRESZKOD);
		data.cikkszam = getString(cursor, COL_CIKKSZAM);
		data.cikknev = getString(cursor, COL_CIKKNEV);
		data.rendszer = getString(cursor, COL_RENDSZER);
		data.mozgomennyiseg = getDouble(cursor, COL_MOZGOMENNYISEG);
		data.mennyisegiegyseg = getString(cursor, COL_MENNYISEGIEGYSEG);
		data.sorszam = getString(cursor, COL_SORSZAM);
		data.agazatiszam = getString(cursor, COL_AGAZATISZAM);
		data.rogzitesdatum = getDate(cursor, COL_ROGZITESDATUM);
		data.bizonylatszam = getString(cursor, COL_BIZONYLATSZAM);
		data.munkalapsorszam = getString(cursor, COL_MUNKALAPSORSZAM);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
		data.mozgasazonosito = getString(cursor, COL_MOZGASAZONOSITO);

		return data;
	}

}