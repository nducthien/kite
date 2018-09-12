package hu.itware.kite.service.orm.database.tables;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Gep;
import android.content.ContentValues;
import hu.itware.kite.service.orm.utils.DateUtils;
import android.database.Cursor;

public class GepekTable extends BaseTable<Gep> {

	public static final String TABLE_NAME = "gepek";

	public static final String COL_ALVAZSZAM = "alvazszam";
	public static final String COL_MOTORSZAM = "motorszam";
	public static final String COL_PARTNERKOD = "partnerkod";
	public static final String COL_TEMPPARTNERKOD = "temppartnerkod";
	public static final String COL_TEMPGEPKOD = "tempgepkod";
	public static final String COL_GEPTIPUS = "geptipus";
	public static final String COL_NEV = "nev";
	public static final String COL_TIPUSAZONOSITO = "tipusazonosito";
	public static final String COL_TIPUSHOSSZUNEV = "tipushosszunev";
	public static final String COL_GYARTASEVE = "gyartaseve";
	public static final String COL_GARANCIAERVENYESSEG = "garanciaervenyesseg";
	public static final String COL_UZEMBEHELYEZESDATUM = "uzembehelyezesdatum";
	public static final String COL_KIADOTTAZONOSITO = "kiadottazonosito";
	public static final String COL_KIADASDATUM = "kiadasdatum";
	public static final String COL_SZERVIZES = "szervizes";
	public static final String COL_BAJ = "baj";
	public static final String COL_FULKESZAM = "fulkeszam";
	public static final String COL_HAJTOMUSZAM = "hajtomuszam";
	public static final String COL_MELLSOHIDSZAM = "mellsohidszam";

	public static final String COL_KJOTALLAS = "kjotallas";
	public static final String COL_UZEMORAKORLAT = "uzemorakorlat";

	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public GepekTable() {
		super(Gep.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(Gep data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_ALVAZSZAM, Type.TEXT, true),
			new Column(COL_TEMPGEPKOD, Type.TEXT, true),
			new Column(COL_MOTORSZAM, Type.TEXT),
			new Column(COL_PARTNERKOD, Type.TEXT),
			new Column(COL_TEMPPARTNERKOD, Type.TEXT),
			new Column(COL_GEPTIPUS, Type.TEXT),
			new Column(COL_NEV, Type.TEXT),
			new Column(COL_TIPUSAZONOSITO, Type.TEXT),
			new Column(COL_TIPUSHOSSZUNEV, Type.TEXT),
			new Column(COL_GYARTASEVE, Type.TEXT),
			new Column(COL_GARANCIAERVENYESSEG, Type.DATE),
			new Column(COL_UZEMBEHELYEZESDATUM, Type.DATE),
			new Column(COL_KIADOTTAZONOSITO, Type.TEXT),
			new Column(COL_KIADASDATUM, Type.DATE),
			new Column(COL_SZERVIZES, Type.TEXT),
			new Column(COL_BAJ, Type.TEXT),
			new Column(COL_FULKESZAM, Type.TEXT),
			new Column(COL_HAJTOMUSZAM, Type.TEXT),
			new Column(COL_MELLSOHIDSZAM, Type.TEXT),

			new Column(COL_KJOTALLAS, Type.DATE),
			new Column(COL_UZEMORAKORLAT, Type.DOUBLE),

			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(Gep data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_ALVAZSZAM, data.alvazszam);
		values.put(COL_MOTORSZAM, data.motorszam);
		values.put(COL_PARTNERKOD, data.partnerkod);
		values.put(COL_TEMPPARTNERKOD, data.temppartnerkod);
		values.put(COL_TEMPGEPKOD, data.tempgepkod);
		values.put(COL_GEPTIPUS, data.geptipus);
		values.put(COL_NEV, data.nev);
		values.put(COL_TIPUSAZONOSITO, data.tipusazonosito);
		values.put(COL_TIPUSHOSSZUNEV, data.tipushosszunev);
		values.put(COL_GYARTASEVE, data.gyartaseve);
		values.put(COL_GARANCIAERVENYESSEG, data.garanciaervenyesseg == null ? null : DateUtils.getDfShort().format(data.garanciaervenyesseg));
		values.put(COL_UZEMBEHELYEZESDATUM, data.uzembehelyezesdatum == null ? null : DateUtils.getDfShort().format(data.uzembehelyezesdatum));
		values.put(COL_KIADOTTAZONOSITO, data.kiadottazonosito);
		values.put(COL_KIADASDATUM, data.kiadasdatum == null ? null : DateUtils.getDfShort().format(data.kiadasdatum));
		values.put(COL_SZERVIZES, data.szervizes);
		values.put(COL_BAJ, data.baj);
		values.put(COL_FULKESZAM, data.fulkeszam);
		values.put(COL_HAJTOMUSZAM, data.hajtomuszam);
		values.put(COL_MELLSOHIDSZAM, data.mellsohidszam);

		//--- KITE-796
		values.put(COL_KJOTALLAS, data.kjotallas == null ? null : DateUtils.getDfShort().format(data.garanciaervenyesseg));
		values.put(COL_UZEMORAKORLAT, data.uzemorakorlat);

		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public Gep getDataFromCursor(Cursor cursor) {

		Gep data = new Gep();
		data.alvazszam = getString(cursor, COL_ALVAZSZAM);
		data.motorszam = getString(cursor, COL_MOTORSZAM);
		data.partnerkod = getString(cursor, COL_PARTNERKOD);
		data.temppartnerkod = getString(cursor, COL_TEMPPARTNERKOD);
		data.tempgepkod = getString(cursor, COL_TEMPGEPKOD);
		data.geptipus = getString(cursor, COL_GEPTIPUS);
		data.nev = getString(cursor, COL_NEV);
		data.tipusazonosito = getString(cursor, COL_TIPUSAZONOSITO);
		data.tipushosszunev = getString(cursor, COL_TIPUSHOSSZUNEV);
		data.gyartaseve = getString(cursor, COL_GYARTASEVE);
		data.garanciaervenyesseg = getDate(cursor, COL_GARANCIAERVENYESSEG);
		data.uzembehelyezesdatum = getDate(cursor, COL_UZEMBEHELYEZESDATUM);
		data.kiadottazonosito = getString(cursor, COL_KIADOTTAZONOSITO);
		data.kiadasdatum = getDate(cursor, COL_KIADASDATUM);
		data.szervizes = getString(cursor, COL_SZERVIZES);
		data.baj = getString(cursor, COL_BAJ);
		data.fulkeszam = getString(cursor, COL_FULKESZAM);
		data.hajtomuszam = getString(cursor, COL_HAJTOMUSZAM);
		data.mellsohidszam = getString(cursor, COL_MELLSOHIDSZAM);

		//--- KITE-796
		data.kjotallas = getDate(cursor, COL_KJOTALLAS);
		data.uzemorakorlat = getDouble(cursor, COL_UZEMORAKORLAT);

		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}