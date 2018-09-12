package hu.itware.kite.service.orm.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.utils.DateUtils;
import hu.itware.kite.service.orm.utils.GSON;

public class MunkalapokTable extends BaseTable<Munkalap> {

	private static final String TAG = "KITE.SZ.MUNKALAPOK";

	public static final String TABLE_NAME = "munkalapok";

	public static final String COL_MUNKALAPKOD = "munkalapkod";
	public static final String COL_TEMPPARTNERKOD = "temppartnerkod";
	public static final String COL_TEMPGEPKOD = "tempgepkod";
	public static final String COL_TEMPKOD = "tempkod";
	public static final String COL_UZEMKEPES = "uzemkepes";
	public static final String COL_GEPAZONOSITO = "gepazonosito";
	public static final String COL_PARTNERKOD = "partnerkod";
	public static final String COL_ALVAZSZAM = "alvazszam";
	public static final String COL_ELOZMENYKOD = "elozmenykod";
	public static final String COL_BEJELENTESDATUM = "bejelentesdatum";
	public static final String COL_CIKKSZAM = "cikkszam";
	public static final String COL_IRSZ = "irsz";
	public static final String COL_TELEPULES = "telepules";
	public static final String COL_CIM = "cim";
	public static final String COL_MEGHIBASODASDATUM = "meghibasodasdatum";
	public static final String COL_GARANCIAERVENYESDATUM = "garanciaervenyesdatum";
	public static final String COL_MUNKAKEZDESDATUM = "munkakezdesdatum";
	public static final String COL_MUNKABEFEJEZESDATUM = "munkabefejezesdatum";
	public static final String COL_MUNKALAPSORSZAM = "munkalapsorszam";
	public static final String COL_JAVITASKEZDESDATUM = "javitaskezdesdatum";
	public static final String COL_MUNKAVEGZESJELLEGE = "munkavegzesjellege";
	public static final String COL_MEGTETTKM = "megtettkm";
	public static final String COL_TIPUSHOSSZUNEV = "tipushosszunev";
	public static final String COL_GEPTIPUS = "geptipus";
	public static final String COL_GEPNEV = "gepnev";
	public static final String COL_GEPTIPUSAZONOSITO = "geptipusazonosito";
	public static final String COL_MUNKAVEGZESDATUM = "munkavegzesdatum";
	public static final String COL_HIBAKOD = "hibakod";
	public static final String COL_JAVITASDATUM = "javitasdatum";
	public static final String COL_SZERVIZKONYV = "szervizkonyv";
	public static final String COL_MOTORSZAM = "motorszam";
	public static final String COL_HIBAJELENSEG = "hibajelenseg";
	public static final String COL_HIBAJELENSEGOKA = "hibajelensegoka";
	public static final String COL_LETREHOZASDATUM = "letrehozasdatum";
	public static final String COL_NEV = "nev";
	public static final String COL_PIPKOD = "pipkod";
	public static final String COL_SZERVIZES = "szervizes";
	public static final String COL_MUNKAVEGZESHELYE = "munkavegzeshelye";
	public static final String COL_TERHELENDO = "terhelendo";
	public static final String COL_ORACSERE = "oracsere";
	public static final String COL_VEVOPOZITIV = "vevopozitiv";
	public static final String COL_VEVOESZREVETEL = "vevoeszrevetel";
	public static final String COL_TEVEKENYSEG = "tevekenyseg";
	public static final String COL_TEVEKENYSEG1 = "tevekenyseg1";
	public static final String COL_TEVEKENYSEG2 = "tevekenyseg2";
	public static final String COL_BIZONYLATSZAM = "bizonylatszam";
	public static final String COL_UZEMBEHELYEZESDATUM = "uzembehelyezesdatum";
	public static final String COL_UZEMORA = "uzemora";
	public static final String COL_MUNKAORA = "munkaora";
	public static final String COL_ORADIJ = "oradij";
	public static final String COL_SZAMLAZASIMOD = "szamlazasimod";
	public static final String COL_SURGOS = "surgos";
	public static final String COL_TULORA = "tulora";
	public static final String COL_EMAIL = "email";
	public static final String COL_FAX = "fax";
	public static final String COL_ALLAPOTKOD = "allapotkod";
	public static final String COL_SZIG = "szig";
	public static final String COL_MUNKAVEGZESHELYBESOROLAS = "munkavegzeshelybesorolas";
	public static final String COL_JAVITASKESZ = "javitaskesz";
	public static final String COL_ORAALLAS = "oraallas";
	public static final String COL_VEVOIESZREVETELKOD = "vevoieszrevetelkod";
	public static final String COL_HIBASALKATRESZ = "hibasalkatresz";
	public static final String COL_ALAIRAS = "alairas";
	public static final String COL_ALAIRASKEP = "alairaskep";
	public static final String COL_FENYKEPEKDATA = "fenykepekdata";
	public static final String COL_GPS1 = "gps1";
	public static final String COL_GPS2 = "gps2";
	public static final String COL_SORTIPUS = "sortipus";
	public static final String COL_BAJ = "baj";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";
	public static final String COL_MUNKAVEGZESJELLEGE2 = "munkavegzesjellege2";
	public static final String COL_MUNKAVEGZESHELYSZINE = "munkavegzeshelyszine";
	public static final String COL_TEVEKENYSEGDROPDOWN = "tevekenysegdropdown";
	public static final String COL_JEGYZOKONYV = "jegyzokonyv";

	//--- KITE-792
	public static final String COL_LEZARASDATUM = "lezarasdatum";



	public MunkalapokTable() {
		super(Munkalap.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(Munkalap data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_MUNKALAPKOD, Type.TEXT, true),
			new Column(COL_TEMPPARTNERKOD, Type.TEXT),
			new Column(COL_TEMPGEPKOD, Type.TEXT),
			new Column(COL_TEMPKOD, Type.TEXT, true),
			new Column(COL_UZEMKEPES, Type.TEXT),
			new Column(COL_GEPAZONOSITO, Type.TEXT),
			new Column(COL_PARTNERKOD, Type.TEXT),
			new Column(COL_ALVAZSZAM, Type.TEXT),
			new Column(COL_ELOZMENYKOD, Type.TEXT),
			new Column(COL_BEJELENTESDATUM, Type.DATE),
			new Column(COL_CIKKSZAM, Type.TEXT),
			new Column(COL_IRSZ, Type.TEXT),
			new Column(COL_TELEPULES, Type.TEXT),
			new Column(COL_CIM, Type.TEXT),
			new Column(COL_MEGHIBASODASDATUM, Type.DATE),
			new Column(COL_GARANCIAERVENYESDATUM, Type.DATE),
			new Column(COL_MUNKAKEZDESDATUM, Type.DATE),
			new Column(COL_MUNKABEFEJEZESDATUM, Type.DATE),
			new Column(COL_MUNKALAPSORSZAM, Type.TEXT),
			new Column(COL_JAVITASKEZDESDATUM, Type.DATE),
			new Column(COL_MUNKAVEGZESJELLEGE, Type.TEXT),
			new Column(COL_MEGTETTKM, Type.DOUBLE),
			new Column(COL_TIPUSHOSSZUNEV, Type.TEXT),
			new Column(COL_GEPTIPUS, Type.TEXT),
			new Column(COL_GEPNEV, Type.TEXT),
			new Column(COL_GEPTIPUSAZONOSITO, Type.TEXT),
			new Column(COL_MUNKAVEGZESDATUM, Type.DATE),
			new Column(COL_HIBAKOD, Type.TEXT),
			new Column(COL_JAVITASDATUM, Type.DATE),
			new Column(COL_SZERVIZKONYV, Type.TEXT),
			new Column(COL_MOTORSZAM, Type.TEXT),
			new Column(COL_HIBAJELENSEG, Type.TEXT),
			new Column(COL_HIBAJELENSEGOKA, Type.TEXT),
			new Column(COL_LETREHOZASDATUM, Type.DATE),
			new Column(COL_NEV, Type.TEXT),
			new Column(COL_PIPKOD, Type.TEXT),
			new Column(COL_SZERVIZES, Type.TEXT),
			new Column(COL_MUNKAVEGZESHELYE, Type.TEXT),
			new Column(COL_TERHELENDO, Type.TEXT),
			new Column(COL_ORACSERE, Type.TEXT),
			new Column(COL_VEVOPOZITIV, Type.TEXT),
			new Column(COL_VEVOESZREVETEL, Type.TEXT),
			new Column(COL_TEVEKENYSEG, Type.TEXT),
			new Column(COL_TEVEKENYSEG1, Type.TEXT),
			new Column(COL_TEVEKENYSEG2, Type.TEXT),
			new Column(COL_BIZONYLATSZAM, Type.TEXT),
			new Column(COL_UZEMBEHELYEZESDATUM, Type.DATE),
			new Column(COL_UZEMORA, Type.DOUBLE),
			new Column(COL_MUNKAORA, Type.DOUBLE),
			new Column(COL_ORADIJ, Type.TEXT),
			new Column(COL_SZAMLAZASIMOD, Type.TEXT),
			new Column(COL_SURGOS, Type.TEXT),
			new Column(COL_TULORA, Type.DOUBLE),
			new Column(COL_EMAIL, Type.TEXT),
			new Column(COL_FAX, Type.TEXT),
			new Column(COL_ALLAPOTKOD, Type.TEXT),
			new Column(COL_SZIG, Type.TEXT),
			new Column(COL_MUNKAVEGZESHELYBESOROLAS, Type.TEXT),
			new Column(COL_JAVITASKESZ, Type.TEXT),
			new Column(COL_ORAALLAS, Type.DOUBLE),
			new Column(COL_VEVOIESZREVETELKOD, Type.TEXT),
			new Column(COL_HIBASALKATRESZ, Type.TEXT),
			new Column(COL_ALAIRAS, Type.TEXT),
			new Column(COL_ALAIRASKEP, Type.TEXT),
			new Column(COL_FENYKEPEKDATA, Type.TEXT),
			new Column(COL_GPS1, Type.DOUBLE),
			new Column(COL_GPS2, Type.DOUBLE),
			new Column(COL_JEGYZOKONYV, Type.TEXT),
			new Column(COL_BAJ, Type.TEXT),
			new Column(COL_SORTIPUS, Type.TEXT),
			new Column(COL_MUNKAVEGZESJELLEGE2, Type.TEXT),
			new Column(COL_MUNKAVEGZESHELYSZINE, Type.TEXT),
			new Column(COL_TEVEKENYSEGDROPDOWN, Type.TEXT),

			//--- KITE-792
			new Column(COL_LEZARASDATUM, Type.DATE),

			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(Munkalap data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_MUNKALAPKOD, data.munkalapkod);
		values.put(COL_TEMPPARTNERKOD, data.temppartnerkod);
		values.put(COL_TEMPGEPKOD, data.tempgepkod);
		values.put(COL_TEMPKOD, data.tempkod);
		values.put(COL_UZEMKEPES, data.uzemkepes);
		values.put(COL_GEPAZONOSITO, data.gepazonosito);
		values.put(COL_PARTNERKOD, data.partnerkod);
		values.put(COL_ALVAZSZAM, data.alvazszam);
		values.put(COL_ELOZMENYKOD, data.elozmenykod);
		values.put(COL_BEJELENTESDATUM, data.bejelentesdatum == null ? null : DateUtils.getDfShort().format(data.bejelentesdatum));
		values.put(COL_CIKKSZAM, data.cikkszam);
		values.put(COL_IRSZ, data.irsz);
		values.put(COL_TELEPULES, data.telepules);
		values.put(COL_CIM, data.cim);
		values.put(COL_MEGHIBASODASDATUM, data.meghibasodasdatum == null ? null : DateUtils.getDfShort().format(data.meghibasodasdatum));
		values.put(COL_GARANCIAERVENYESDATUM, data.garanciaervenyesdatum == null ? null : DateUtils.getDfShort().format(data.garanciaervenyesdatum));
		values.put(COL_MUNKAKEZDESDATUM, data.munkakezdesdatum == null ? null : DateUtils.getDfShort().format(data.munkakezdesdatum));
		values.put(COL_MUNKABEFEJEZESDATUM, data.munkabefejezesdatum == null ? null : DateUtils.getDfShort().format(data.munkabefejezesdatum));
		values.put(COL_MUNKALAPSORSZAM, data.munkalapsorszam);
		values.put(COL_JAVITASKEZDESDATUM, data.javitaskezdesdatum == null ? null : DateUtils.getDfShort().format(data.javitaskezdesdatum));
		values.put(COL_MUNKAVEGZESJELLEGE, data.munkavegzesjellege);
		values.put(COL_MEGTETTKM, data.megtettkm);
		values.put(COL_TIPUSHOSSZUNEV, data.tipushosszunev);
		values.put(COL_GEPTIPUS, data.geptipus);
		values.put(COL_GEPNEV, data.gepnev);
		values.put(COL_GEPTIPUSAZONOSITO, data.geptipusazonosito);
		values.put(COL_MUNKAVEGZESDATUM, data.munkavegzesdatum == null ? null : DateUtils.getDfShort().format(data.munkavegzesdatum));
		values.put(COL_HIBAKOD, data.hibakod);
		values.put(COL_JAVITASDATUM, data.javitasdatum == null ? null : DateUtils.getDfShort().format(data.javitasdatum));
		values.put(COL_SZERVIZKONYV, data.szervizkonyv);
		values.put(COL_MOTORSZAM, data.motorszam);
		values.put(COL_HIBAJELENSEG, data.hibajelenseg);
		values.put(COL_HIBAJELENSEGOKA, data.hibajelensegoka);
		values.put(COL_LETREHOZASDATUM, data.letrehozasdatum == null ? null : DateUtils.getDfShort().format(data.letrehozasdatum));
		values.put(COL_NEV, data.nev);
		values.put(COL_PIPKOD, data.pipkod);
		values.put(COL_SZERVIZES, data.szervizes);
		values.put(COL_MUNKAVEGZESHELYE, data.munkavegzeshelye);
		values.put(COL_TERHELENDO, data.terhelendo);
		values.put(COL_ORACSERE, data.oracsere);
		values.put(COL_VEVOPOZITIV, data.vevopozitiv);
		values.put(COL_VEVOESZREVETEL, data.vevoeszrevetel);
		values.put(COL_TEVEKENYSEG, data.tevekenyseg);
		values.put(COL_TEVEKENYSEG1, data.tevekenyseg1);
		values.put(COL_TEVEKENYSEG2, data.tevekenyseg2);
		values.put(COL_BIZONYLATSZAM, data.bizonylatszam);
		values.put(COL_UZEMBEHELYEZESDATUM, data.uzembehelyezesdatum == null ? null : DateUtils.getDfShort().format(data.uzembehelyezesdatum));
		values.put(COL_UZEMORA, data.uzemora);
		values.put(COL_MUNKAORA, data.munkaora);
		values.put(COL_ORADIJ, data.oradij);
		values.put(COL_SZAMLAZASIMOD, data.szamlazasimod);
		values.put(COL_SURGOS, data.surgos);
		values.put(COL_TULORA, data.tulora);
		values.put(COL_EMAIL, data.email);
		values.put(COL_FAX, data.fax);
		values.put(COL_ALLAPOTKOD, data.allapotkod);
		values.put(COL_SZIG, data.szig);
		values.put(COL_MUNKAVEGZESHELYBESOROLAS, data.munkavegzeshelybesorolas);
		values.put(COL_JAVITASKESZ, data.javitaskesz);
		values.put(COL_ORAALLAS, data.oraallas);
		values.put(COL_VEVOIESZREVETELKOD, data.vevoiEszrevetelKod);
		values.put(COL_HIBASALKATRESZ, data.hibasAlkatresz);
		values.put(COL_ALAIRAS, data.alairas);
		values.put(COL_ALAIRASKEP, data.alairaskep);
		values.put(COL_FENYKEPEKDATA, data.fenykepekData);
		values.put(COL_GPS1, data.gps1);
		values.put(COL_GPS2, data.gps2);
		values.put(COL_BAJ, data.baj);
		values.put(COL_SORTIPUS, data.sortipus);
		values.put(COL_MUNKAVEGZESJELLEGE2, data.munkavegzesjellege2);
		values.put(COL_MUNKAVEGZESHELYSZINE, data.munkavegzeshelyszine);
		values.put(COL_TEVEKENYSEGDROPDOWN, data.tevekenysegDropdown);
		values.put(COL_JEGYZOKONYV, data.jegyzokonyv);
		//--- KITE-792
		values.put(COL_LEZARASDATUM, data.lezarasdatum == null ? null : DateUtils.getDfLong().format(data.lezarasdatum));
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfLong().format(data.modified));
		values.put(COL_STATUS, data.status);

		if(data.fenykepek != null){
			values.put(COL_FENYKEPEKDATA, GSON.toJson(data.fenykepek));
		}

		return values;
	}

	@Override
	public Munkalap getDataFromCursor(Cursor cursor) {

		Munkalap data = new Munkalap();
		data.munkalapkod = getString(cursor, COL_MUNKALAPKOD);
		data.temppartnerkod = getString(cursor, COL_TEMPPARTNERKOD);
		data.tempgepkod = getString(cursor, COL_TEMPGEPKOD);
		data.tempkod = getString(cursor, COL_TEMPKOD);
		data.uzemkepes = getString(cursor, COL_UZEMKEPES);
		data.gepazonosito = getString(cursor, COL_GEPAZONOSITO);
		data.partnerkod = getString(cursor, COL_PARTNERKOD);
		data.alvazszam = getString(cursor, COL_ALVAZSZAM);
		data.elozmenykod = getString(cursor, COL_ELOZMENYKOD);
		data.bejelentesdatum = getDate(cursor, COL_BEJELENTESDATUM);
		data.cikkszam = getString(cursor, COL_CIKKSZAM);
		data.irsz = getString(cursor, COL_IRSZ);
		data.telepules = getString(cursor, COL_TELEPULES);
		data.cim = getString(cursor, COL_CIM);
		data.meghibasodasdatum = getDate(cursor, COL_MEGHIBASODASDATUM);
		data.garanciaervenyesdatum = getDate(cursor, COL_GARANCIAERVENYESDATUM);
		data.munkakezdesdatum = getDate(cursor, COL_MUNKAKEZDESDATUM);
		data.munkabefejezesdatum = getDate(cursor, COL_MUNKABEFEJEZESDATUM);
		data.munkalapsorszam = getString(cursor, COL_MUNKALAPSORSZAM);
		data.javitaskezdesdatum = getDate(cursor, COL_JAVITASKEZDESDATUM);
		data.munkavegzesjellege = getString(cursor, COL_MUNKAVEGZESJELLEGE);
		data.megtettkm = getDouble(cursor, COL_MEGTETTKM);
		data.tipushosszunev = getString(cursor, COL_TIPUSHOSSZUNEV);
		data.geptipus = getString(cursor, COL_GEPTIPUS);
		data.gepnev = getString(cursor, COL_GEPNEV);
		data.geptipusazonosito = getString(cursor, COL_GEPTIPUSAZONOSITO);
		data.munkavegzesdatum = getDate(cursor, COL_MUNKAVEGZESDATUM);
		data.hibakod = getString(cursor, COL_HIBAKOD);
		data.javitasdatum = getDate(cursor, COL_JAVITASDATUM);
		data.szervizkonyv = getString(cursor, COL_SZERVIZKONYV);
		data.motorszam = getString(cursor, COL_MOTORSZAM);
		data.hibajelenseg = getString(cursor, COL_HIBAJELENSEG);
		data.hibajelensegoka = getString(cursor, COL_HIBAJELENSEGOKA);
		data.letrehozasdatum = getDate(cursor, COL_LETREHOZASDATUM);
		data.nev = getString(cursor, COL_NEV);
		data.pipkod = getString(cursor, COL_PIPKOD);
		data.szervizes = getString(cursor, COL_SZERVIZES);
		data.munkavegzeshelye = getString(cursor, COL_MUNKAVEGZESHELYE);
		data.terhelendo = getString(cursor, COL_TERHELENDO);
		data.oracsere = getString(cursor, COL_ORACSERE);
		data.vevopozitiv = getString(cursor, COL_VEVOPOZITIV);
		data.vevoeszrevetel = getString(cursor, COL_VEVOESZREVETEL);
		data.tevekenyseg = getString(cursor, COL_TEVEKENYSEG);
		data.tevekenyseg1 = getString(cursor, COL_TEVEKENYSEG1);
		data.tevekenyseg2 = getString(cursor, COL_TEVEKENYSEG2);
		data.bizonylatszam = getString(cursor, COL_BIZONYLATSZAM);
		data.uzembehelyezesdatum = getDate(cursor, COL_UZEMBEHELYEZESDATUM);
		data.uzemora = getDouble(cursor, COL_UZEMORA);
		data.munkaora = getDouble(cursor, COL_MUNKAORA);
		data.oradij = getString(cursor, COL_ORADIJ);
		data.szamlazasimod = getString(cursor, COL_SZAMLAZASIMOD);
		data.surgos = getString(cursor, COL_SURGOS);
		data.tulora = getDouble(cursor, COL_TULORA);
		data.email = getString(cursor, COL_EMAIL);
		data.fax = getString(cursor, COL_FAX);
		data.allapotkod = getString(cursor, COL_ALLAPOTKOD);
		data.szig = getString(cursor, COL_SZIG);
		data.munkavegzeshelybesorolas = getString(cursor, COL_MUNKAVEGZESHELYBESOROLAS);
		data.javitaskesz = getString(cursor, COL_JAVITASKESZ);
		data.oraallas = getDouble(cursor, COL_ORAALLAS);
		data.vevoiEszrevetelKod = getString(cursor, COL_VEVOIESZREVETELKOD);
		data.hibasAlkatresz = getString(cursor, COL_HIBASALKATRESZ);
		data.alairas = getString(cursor, COL_ALAIRAS);
		data.alairaskep = getString(cursor, COL_ALAIRASKEP);
		data.fenykepekData = getString(cursor, COL_FENYKEPEKDATA);
		data.gps1 = getDouble(cursor, COL_GPS1);
		data.gps2 = getDouble(cursor, COL_GPS2);
		data.baj = getString(cursor, COL_BAJ);
		data._id = getInt(cursor, COL_BASE_ID);
		data.sortipus = getString(cursor, COL_SORTIPUS);
		data.munkavegzesjellege2 = getString(cursor, COL_MUNKAVEGZESJELLEGE2);
		data.munkavegzeshelyszine = getString(cursor, COL_MUNKAVEGZESHELYSZINE);
		data.tevekenysegDropdown = getString(cursor, COL_TEVEKENYSEGDROPDOWN);
		data.jegyzokonyv = getString(cursor, COL_JEGYZOKONYV);

		//--- KITE-792
		data.lezarasdatum = getDate(cursor, COL_LEZARASDATUM);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);

		if(data.fenykepekData != null && !"".equals(data.fenykepekData)){
			data.fenykepek = GSON.toList(data.fenykepekData, String.class);
		} else {
			data.fenykepek = new ArrayList<String>();
		}
	
		return data;
	}

	@Override
	public ContentValues postUpdate(KiteORM orm, ContentValues item) {

		String tempkod = item.getAsString(MunkalapokTable.COL_TEMPKOD);
		String munkalapkod = item.getAsString(MunkalapokTable.COL_MUNKALAPKOD);
		if (tempkod != null && tempkod.length() > 5 && munkalapkod != null && munkalapkod.length() > 5) {
			Munkalap munkalap = orm.loadSingle(Munkalap.class, MunkalapokTable.COL_TEMPKOD + " = ? AND " + MunkalapokTable.COL_MUNKALAPKOD + " is null", new String[]{tempkod});
			if(munkalap != null) {
				munkalap.updateTempMunkalap(munkalapkod);
				return null;
			}
		}

		String munkavegzesdatum = item.getAsString(MunkalapokTable.COL_MUNKAVEGZESDATUM);
		String munkakezdesdatum = item.getAsString(MunkalapokTable.COL_MUNKAKEZDESDATUM);
		String munkabefejezesdatum = item.getAsString(MunkalapokTable.COL_MUNKABEFEJEZESDATUM);
		//Log.i(TAG, "munkavegzesdatum:" + munkavegzesdatum + ", munkakezdesdatum:" + munkakezdesdatum + ", munkabefejezesdatum:" + munkabefejezesdatum);
		if (munkavegzesdatum != null && munkakezdesdatum != null) {
			munkakezdesdatum = convertDate(munkavegzesdatum, munkakezdesdatum);
			//Log.e(TAG, "munkakezdesdatum.new()=" + munkakezdesdatum);
			item.put(MunkalapokTable.COL_MUNKAKEZDESDATUM, munkakezdesdatum);
		}

		if (munkavegzesdatum != null && munkabefejezesdatum != null) {
			munkabefejezesdatum = convertDate(munkavegzesdatum, munkabefejezesdatum);
			//Log.e(TAG, "munkabefejezesdatum.new()=" + munkabefejezesdatum);
			item.put(MunkalapokTable.COL_MUNKABEFEJEZESDATUM, munkabefejezesdatum);
		}

		return item;
	}

	private String convertDate(String dateString, String timeString) {
		//--- Faster way
		return dateString.split(" ")[0] + " " + timeString + ":00";
	}
}