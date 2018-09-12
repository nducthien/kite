package hu.itware.kite.service.orm.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.utils.DateUtils;
import hu.itware.kite.service.utils.StringUtils;

public class PartnerekTable extends BaseTable<Partner> {

	public static final String TABLE_NAME = "partnerek";

	public static final String COL_PARTNERKOD = "partnerkod";
	public static final String COL_UZLETKOTOK = "uzletkotok";
	public static final String COL_ALKOZPONTKOD = "alkozpontkod";
	public static final String COL_TEMPKOD = "tempkod";
	public static final String COL_NEV1 = "nev1";
	public static final String COL_NEV2 = "nev2";
	public static final String COL_IRSZ = "irsz";
	public static final String COL_TELEPULES = "telepules";
	public static final String COL_CIM = "cim";
	public static final String COL_TELEFONSZAM = "telefonszam";
	public static final String COL_EMAIL = "email";
	public static final String COL_ADOSZAM = "adoszam";
	public static final String COL_ADOAZONOSITO = "adoazonosito";
	public static final String COL_MINOSITESDATUMA = "minositesdatuma";
	public static final String COL_MINOSITES = "minosites";
	public static final String COL_LIMIT1 = "limit1";
	public static final String COL_LIMIT2 = "limit2";
	public static final String COL_LIMIT3 = "limit3";
	public static final String COL_BAJ = "baj";
	public static final String COL_SZERVIZESKOD = "szervizeskod";
	public static final String COL_FIZETOKESZSEG = "fizetokeszseg";
	public static final String COL_FAX = "fax";
	public static final String COL_PARTNERKAPCSOLATOK = "partnerkapcsolat";
	public static final String COL_PARTNERTELEPHELYEK = "partnertelephelyek";
	public static final String COL_SEARCHNEV = "searchnev";
	public static final String COL_SEARCHCIM = "searchcim";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";


	public PartnerekTable() {
		super(Partner.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public long getDataId(Partner data) {
		return data._id;
	}

	@Override
	public Column[] getColumns() {
		return new Column[] {
				new Column(COL_PARTNERKOD, Type.TEXT, true),
				new Column(COL_UZLETKOTOK, Type.TEXT),
				new Column(COL_ALKOZPONTKOD, Type.TEXT),
				new Column(COL_TEMPKOD, Type.TEXT),
				new Column(COL_NEV1, Type.TEXT),
				new Column(COL_NEV2, Type.TEXT),
				new Column(COL_IRSZ, Type.TEXT),
				new Column(COL_TELEPULES, Type.TEXT),
				new Column(COL_CIM, Type.TEXT),
				new Column(COL_TELEFONSZAM, Type.TEXT),
				new Column(COL_EMAIL, Type.TEXT),
				new Column(COL_ADOSZAM, Type.TEXT),
				new Column(COL_ADOAZONOSITO, Type.TEXT),
				new Column(COL_MINOSITESDATUMA, Type.DATE),
				new Column(COL_MINOSITES, Type.TEXT),
				new Column(COL_LIMIT1, Type.INTEGER),
				new Column(COL_LIMIT2, Type.INTEGER),
				new Column(COL_LIMIT3, Type.INTEGER),
				new Column(COL_BAJ, Type.TEXT),
				new Column(COL_SZERVIZESKOD, Type.TEXT),
				new Column(COL_FIZETOKESZSEG, Type.TEXT),
				new Column(COL_FAX, Type.TEXT),
				new Column(COL_PARTNERKAPCSOLATOK, Type.TEXT),
				new Column(COL_PARTNERTELEPHELYEK, Type.TEXT),
				new Column(COL_SEARCHNEV, Type.TEXT),
				new Column(COL_SEARCHCIM, Type.TEXT),
				new Column(COL_MODIFIED, Type.DATE),
				new Column(COL_STATUS, Type.TEXT)
		};
	}
	@Override
	public ContentValues getContentValues(Partner data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_PARTNERKOD, data.partnerkod);
		values.put(COL_UZLETKOTOK, data.uzletkotok);
		values.put(COL_ALKOZPONTKOD, data.alkozpontkod);
		values.put(COL_TEMPKOD, data.tempkod);
		values.put(COL_NEV1, data.nev1);
		values.put(COL_NEV2, data.nev2);
		values.put(COL_IRSZ, data.irsz);
		values.put(COL_TELEPULES, data.telepules);
		values.put(COL_CIM, data.cim);
		values.put(COL_TELEFONSZAM, data.telefonszam);
		values.put(COL_EMAIL, data.email);
		values.put(COL_ADOSZAM, data.adoszam);
		values.put(COL_ADOAZONOSITO, data.adoazonosito);
		values.put(COL_MINOSITESDATUMA, data.minositesdatuma == null ? null : DateUtils.getDfShort().format(data.minositesdatuma));
		values.put(COL_MINOSITES, data.minosites);
		values.put(COL_LIMIT1, data.limit1);
		values.put(COL_LIMIT2, data.limit2);
		values.put(COL_LIMIT3, data.limit3);
		values.put(COL_BAJ, data.baj);
		values.put(COL_SZERVIZESKOD, data.szervizeskod);
		values.put(COL_FIZETOKESZSEG, data.fizetokeszseg);
		values.put(COL_FAX, data.fax);
		values.put(COL_PARTNERKAPCSOLATOK, data.partnerkapcsolatok);
		values.put(COL_PARTNERTELEPHELYEK, data.partnertelephelyek);
		values.put(COL_SEARCHNEV, data.searchNev);
		values.put(COL_SEARCHCIM, data.searchCim);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public Partner getDataFromCursor(Cursor cursor) {

		Partner data = new Partner();
		data.partnerkod = getString(cursor, COL_PARTNERKOD);
		data.uzletkotok = getString(cursor, COL_UZLETKOTOK);
		data.alkozpontkod = getString(cursor, COL_ALKOZPONTKOD);
		data.tempkod = getString(cursor, COL_TEMPKOD);
		data.nev1 = getString(cursor, COL_NEV1);
		data.nev2 = getString(cursor, COL_NEV2);
		data.irsz = getString(cursor, COL_IRSZ);
		data.telepules = getString(cursor, COL_TELEPULES);
		data.cim = getString(cursor, COL_CIM);
		data.telefonszam = getString(cursor, COL_TELEFONSZAM);
		data.email = getString(cursor, COL_EMAIL);
		data.adoszam = getString(cursor, COL_ADOSZAM);
		data.adoazonosito = getString(cursor, COL_ADOAZONOSITO);
		data.minositesdatuma = getDate(cursor, COL_MINOSITESDATUMA);
		data.minosites = getString(cursor, COL_MINOSITES);
		data.limit1 = getLong(cursor, COL_LIMIT1);
		data.limit2 = getLong(cursor, COL_LIMIT2);
		data.limit3 = getLong(cursor, COL_LIMIT3);
		data.baj = getString(cursor, COL_BAJ);
		data.szervizeskod = getString(cursor, COL_SZERVIZESKOD);
		data.fizetokeszseg = getString(cursor, COL_FIZETOKESZSEG);
		data.fax = getString(cursor, COL_FAX);
		data.partnerkapcsolatok = getString(cursor, COL_PARTNERKAPCSOLATOK);
		data.partnertelephelyek = getString(cursor, COL_PARTNERTELEPHELYEK);
		data.searchNev = getString(cursor, COL_SEARCHNEV);
		data.searchCim = getString(cursor, COL_SEARCHCIM);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);

		return data;
	}


	@Override
	public Partner postUpdate(KiteORM orm, Partner item) {

		if (item.tempkod != null && item.tempkod.length() > 5) {
			Log.i("PARTNER", "Partner.postUpdate(). Delete temp partner=" + item.tempkod);
			orm.delete(Partner.class, "partnerkod = ?", new String [] { item.tempkod });
			item.tempkod = null;
		}

		item.searchNev = StringUtils.collateName(item.nev1, item.nev2);
		item.searchCim = StringUtils.collateName(item.telepules, item.cim, item.irsz);

		return item;
	}

	@Override
	public ContentValues postUpdate(KiteORM orm, ContentValues item) {

		String tempkod = item.getAsString("tempkod");
		if (tempkod != null && tempkod.length() > 5) {
			Log.i("PARTNER", "Partner.postUpdate(). Delete temp partner=" + tempkod);
			orm.delete(Partner.class, "partnerkod = ?", new String [] { tempkod });
			item.put("tempkod", (String)null);
		}

		item.put("searchNev", StringUtils.collateName(item.getAsString("nev1"), item.getAsString("nev2")));
		item.put("searchCim", StringUtils.collateName(item.getAsString("telepules"), item.getAsString("cim"), item.getAsString("irsz")));

		return item;
	}
}