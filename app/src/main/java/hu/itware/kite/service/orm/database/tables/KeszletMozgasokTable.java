package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class KeszletMozgasokTable extends BaseTable<KeszletMozgas> {

	public static final String TABLE_NAME = "keszletmozgasok";

	public static final String COL_AZONOSITO = "azonosito";
	public static final String COL_DATUM = "datum";
	public static final String COL_BIZONYLATSZAM = "bizonylatszam";
	public static final String COL_SZAMLASORSZAM = "szamlasorszam";
	public static final String COL_MOZGASTIPUS = "mozgastipus";
	public static final String COL_MOZGOMENNYISEG = "mozgomennyiseg";
	public static final String COL_MENNYISEGIEGYSEG = "mennyisegiegyseg";
	public static final String COL_CIKKSZAM = "cikkszam";
	public static final String COL_CIKKNEV = "cikknev";
	public static final String COL_GEPTIPUSAZONOSITO = "geptipusazonosito";
	public static final String COL_AGAZATISZAM = "agazatiszam";
	public static final String COL_BIZONYLATTETELSORSZAM = "bizonylattetelsorszam";
	public static final String COL_K0AZONOSITO = "k0azonosito";
	public static final String COL_SZAMLATETELSORSZAM = "szamlatetelsorszam";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public KeszletMozgasokTable() {
		super(KeszletMozgas.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(KeszletMozgas data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_AZONOSITO, Type.TEXT, true),
			new Column(COL_DATUM, Type.DATE),
			new Column(COL_BIZONYLATSZAM, Type.TEXT),
			new Column(COL_SZAMLASORSZAM, Type.TEXT),
			new Column(COL_MOZGASTIPUS, Type.TEXT),
			new Column(COL_MOZGOMENNYISEG, Type.DOUBLE),
			new Column(COL_MENNYISEGIEGYSEG, Type.TEXT),
			new Column(COL_CIKKSZAM, Type.TEXT),
			new Column(COL_CIKKNEV, Type.TEXT),
			new Column(COL_GEPTIPUSAZONOSITO, Type.TEXT),
			new Column(COL_AGAZATISZAM, Type.TEXT),
			new Column(COL_BIZONYLATTETELSORSZAM, Type.TEXT),
			new Column(COL_K0AZONOSITO, Type.TEXT),
			new Column(COL_SZAMLATETELSORSZAM, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(KeszletMozgas data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_AZONOSITO, data.azonosito);
		values.put(COL_DATUM, data.datum == null ? null : DateUtils.getDfShort().format(data.datum));
		values.put(COL_BIZONYLATSZAM, data.bizonylatszam);
		values.put(COL_SZAMLASORSZAM, data.szamlasorszam);
		values.put(COL_MOZGASTIPUS, data.mozgastipus);
		values.put(COL_MOZGOMENNYISEG, data.mozgomennyiseg);
		values.put(COL_MENNYISEGIEGYSEG, data.mennyisegiegyseg);
		values.put(COL_CIKKSZAM, data.cikkszam);
		values.put(COL_CIKKNEV, data.cikknev);
		values.put(COL_GEPTIPUSAZONOSITO, data.geptipusazonosito);
		values.put(COL_AGAZATISZAM, data.agazatiszam);
		values.put(COL_BIZONYLATTETELSORSZAM, data.bizonylattetelsorszam);
		values.put(COL_K0AZONOSITO, data.k0azonosito);
		values.put(COL_SZAMLATETELSORSZAM, data.szamlatetelsorszam);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public KeszletMozgas getDataFromCursor(Cursor cursor) {

		KeszletMozgas data = new KeszletMozgas();
		data.azonosito = getString(cursor, COL_AZONOSITO);
		data.datum = getDate(cursor, COL_DATUM);
		data.bizonylatszam = getString(cursor, COL_BIZONYLATSZAM);
		data.szamlasorszam = getString(cursor, COL_SZAMLASORSZAM);
		data.mozgastipus = getString(cursor, COL_MOZGASTIPUS);
		data.mozgomennyiseg = getDouble(cursor, COL_MOZGOMENNYISEG);
		data.mennyisegiegyseg = getString(cursor, COL_MENNYISEGIEGYSEG);
		data.cikkszam = getString(cursor, COL_CIKKSZAM);
		data.cikknev = getString(cursor, COL_CIKKNEV);
		data.geptipusazonosito = getString(cursor, COL_GEPTIPUSAZONOSITO);
		data.agazatiszam = getString(cursor, COL_AGAZATISZAM);
		data.bizonylattetelsorszam = getString(cursor, COL_BIZONYLATTETELSORSZAM);
		data.k0azonosito = getString(cursor, COL_K0AZONOSITO);
		data.szamlatetelsorszam = getString(cursor, COL_SZAMLATETELSORSZAM);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}