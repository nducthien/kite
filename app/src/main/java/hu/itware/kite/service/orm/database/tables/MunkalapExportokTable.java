package hu.itware.kite.service.orm.database.tables;

import android.content.ContentValues;
import android.database.Cursor;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.model.MunkalapExport;
import hu.itware.kite.service.orm.utils.DateUtils;

public class MunkalapExportokTable extends BaseTable<MunkalapExport> {

	public static final String TABLE_NAME = "munkalapokexport";

	public static final String COL_SYSTEM = "system";
	public static final String COL_DATABASE = "database";
	public static final String COL_ACT = "act";

	public static final String COL_EGE = "ege";
	public static final String COL_GE = "ge";
	public static final String COL_GEW = "gew";
	public static final String COL_GOK = "gok";
	public static final String COL_KG = "kg";
	public static final String COL_PS = "ps";
	public static final String COL_ALSZ = "alsz";
	public static final String COL_BJTM = "bjtm";
	public static final String COL_CI = "ci";
	public static final String COL_FALU = "falu";
	public static final String COL_GEDT = "gedt";
	public static final String COL_GERV = "gerv";
	public static final String COL_GETC1 = "getc1";
	public static final String COL_GETC2 = "getc2";
	public static final String COL_GETD1 = "getd1";
	public static final String COL_GETN1 = "getn1";
	public static final String COL_GNEV = "gnev";
	public static final String COL_GNEV1 = "gnev1";
	public static final String COL_GNEV2 = "gnev2";
	public static final String COL_HALDT = "haldt";
	public static final String COL_HKOD = "hkod";
	public static final String COL_IRSZ = "irsz";
	public static final String COL_JADT = "jadt";
	public static final String COL_KAFSZ = "kafsz";
	public static final String COL_MSG1 = "msg1";
	public static final String COL_MSG2 = "msg2";
	public static final String COL_NEV = "nev";
	public static final String COL_NEV2 = "nev2";
	public static final String COL_PIP = "pip";
	public static final String COL_SZERV = "szerv";
	public static final String COL_SZJ01 = "szj01";
	public static final String COL_SZJ02 = "szj02";
	public static final String COL_SZJ03 = "szj03";
	public static final String COL_SZJ04 = "szj04";
	public static final String COL_SZJ09 = "szj09";
	public static final String COL_TRBSZS = "trbszs";
	public static final String COL_UTCA = "utca";
	public static final String COL_UZORA = "uzora";
	public static final String COL_FUSZ = "fusz";
	public static final String COL_ALK = "alk";
	public static final String COL_GETI = "geti";
	public static final String COL_GT = "gt";
	public static final String COL_HASZ = "hasz";
	public static final String COL_MESZ = "mesz";
	public static final String COL_MOSZ = "mosz";
	public static final String COL_PSOK = "psok";
	public static final String COL_UZDT = "uzdt";
	public static final String COL_UZHEKT = "uzhekt";
	public static final String COL_GETI1 = "geti1";
	public static final String COL_GETI2 = "geti2";
	public static final String COL_MHB = "mhb";
	public static final String COL_TEV1 = "tev1";
	public static final String COL_TEV2 = "tev2";
	public static final String COL_JAVOK = "javok";
	public static final String COL_VEV1 = "vev1";
	public static final String COL_EMAIL = "email";
	public static final String COL_FAX = "fax";
	public static final String COL_GEALL = "geall";
	public static final String COL_KGW = "kgw";
	public static final String COL_IPS = "ips";
	public static final String COL_ALIRO = "aliro";
	public static final String COL_SZIG = "szig";
	public static final String COL_GPS1 = "gps1";
	public static final String COL_GPS2 = "gps2";
	public static final String COL_NUS = "nus";
	public static final String COL_NUI = "nui";
	public static final String COL_NTM = "ntm";
	public static final String COL_NDT = "ndt";
	public static final String COL_MUS = "mus";
	public static final String COL_MUI = "mui";
	public static final String COL_MTM = "mtm";
	public static final String COL_MDT = "mdt";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_STATUS = "status";

	public static final String COL_ADO = "ado";
	public static final String COL_ADAZ = "adaz";
	public static final String COL_TEL = "tel";
	public static final String COL_GETI2K = "geti2k";
	public static final String COL_MHBK = "mhbk";
	public static final String COL_TEV1K = "tev1k";
	public static final String COL_JZK = "jzk";
	public static final String COL_LDT = "ldt";
	public static final String COL_LTM = "ltm";

	public MunkalapExportokTable() {
		super(MunkalapExport.class);
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
}

	@Override
	public long getDataId(MunkalapExport data) {
		return data._id;
}

	@Override
	public Column[] getColumns() {
		return new Column[] { 
			new Column(COL_SYSTEM, Type.TEXT),
			new Column(COL_DATABASE, Type.TEXT),
			new Column(COL_ACT, Type.TEXT),
			new Column(COL_GE, Type.TEXT, true),
			new Column(COL_EGE, Type.TEXT, true),
			new Column(COL_GEW, Type.TEXT, true),
			new Column(COL_GOK, Type.TEXT),
			new Column(COL_KG, Type.TEXT),
			new Column(COL_PS, Type.TEXT),
			new Column(COL_ALSZ, Type.TEXT),
			new Column(COL_BJTM, Type.TEXT),
			new Column(COL_CI, Type.TEXT),
			new Column(COL_FALU, Type.TEXT),
			new Column(COL_GEDT, Type.TEXT),
			new Column(COL_GERV, Type.TEXT),
			new Column(COL_GETC1, Type.TEXT),
			new Column(COL_GETC2, Type.TEXT),
			new Column(COL_GETD1, Type.TEXT),
			new Column(COL_GETN1, Type.TEXT),
			new Column(COL_GNEV, Type.TEXT),
			new Column(COL_GNEV1, Type.TEXT),
			new Column(COL_GNEV2, Type.TEXT),
			new Column(COL_HALDT, Type.TEXT),
			new Column(COL_HKOD, Type.TEXT),
			new Column(COL_IRSZ, Type.TEXT),
			new Column(COL_JADT, Type.TEXT),
			new Column(COL_KAFSZ, Type.TEXT),
			new Column(COL_MSG1, Type.TEXT),
			new Column(COL_MSG2, Type.TEXT),
			new Column(COL_NEV, Type.TEXT),
			new Column(COL_NEV2, Type.TEXT),
			new Column(COL_PIP, Type.TEXT),
			new Column(COL_SZERV, Type.TEXT),
			new Column(COL_SZJ01, Type.TEXT),
			new Column(COL_SZJ02, Type.TEXT),
			new Column(COL_SZJ03, Type.TEXT),
			new Column(COL_SZJ04, Type.TEXT),
			new Column(COL_SZJ09, Type.TEXT),
			new Column(COL_TRBSZS, Type.TEXT),
			new Column(COL_UTCA, Type.TEXT),
			new Column(COL_UZORA, Type.TEXT),
			new Column(COL_FUSZ, Type.TEXT),
			new Column(COL_ALK, Type.TEXT),
			new Column(COL_GETI, Type.TEXT),
			new Column(COL_GT, Type.TEXT),
			new Column(COL_HASZ, Type.TEXT),
			new Column(COL_MESZ, Type.TEXT),
			new Column(COL_MOSZ, Type.TEXT),
			new Column(COL_PSOK, Type.TEXT),
			new Column(COL_UZDT, Type.TEXT),
			new Column(COL_UZHEKT, Type.TEXT),
			new Column(COL_GETI1, Type.TEXT),
			new Column(COL_GETI2, Type.TEXT),
			new Column(COL_MHB, Type.TEXT),
			new Column(COL_TEV1, Type.TEXT),
			new Column(COL_TEV2, Type.TEXT),
			new Column(COL_JAVOK, Type.TEXT),
			new Column(COL_VEV1, Type.TEXT),
			new Column(COL_EMAIL, Type.TEXT),
			new Column(COL_FAX, Type.TEXT),
			new Column(COL_GEALL, Type.TEXT),
			new Column(COL_KGW, Type.TEXT),
			new Column(COL_IPS, Type.TEXT),
			new Column(COL_ALIRO, Type.TEXT),
			new Column(COL_SZIG, Type.TEXT),
			new Column(COL_GPS1, Type.TEXT),
			new Column(COL_GPS2, Type.TEXT),
			new Column(COL_NUS, Type.TEXT),
			new Column(COL_NUI, Type.TEXT),
			new Column(COL_NTM, Type.TEXT),
			new Column(COL_NDT, Type.TEXT),
			new Column(COL_MUS, Type.TEXT),
			new Column(COL_MUI, Type.TEXT),
			new Column(COL_MTM, Type.TEXT),
			new Column(COL_MDT, Type.TEXT),
			new Column(COL_ADO, Type.TEXT),
			new Column(COL_ADAZ, Type.TEXT),
			new Column(COL_TEL, Type.TEXT),
			new Column(COL_GETI2K, Type.TEXT),
			new Column(COL_MHBK, Type.TEXT),
			new Column(COL_TEV1K, Type.TEXT),
			new Column(COL_JZK, Type.TEXT),
			new Column(COL_LDT, Type.TEXT),
			new Column(COL_LTM, Type.TEXT),
			new Column(COL_MODIFIED, Type.DATE),
			new Column(COL_STATUS, Type.TEXT)
		};
}
	@Override
	public ContentValues getContentValues(MunkalapExport data) {
		ContentValues values = new ContentValues();
		if (data._id != -1) {
			values.put(COL_BASE_ID, data._id);
		}
		values.put(COL_SYSTEM, data.SYSTEM);
		values.put(COL_DATABASE, data.DATABASE);
		values.put(COL_ACT, data.ACT);
		values.put(COL_EGE, data.EGE);
		values.put(COL_GE, data.GE);
		values.put(COL_GEW, data.GEW);
		values.put(COL_GOK, data.GOK);
		values.put(COL_KG, data.KG);
		values.put(COL_PS, data.PS);
		values.put(COL_ALSZ, data.ALSZ);
		values.put(COL_BJTM, data.BJTM);
		values.put(COL_CI, data.CI);
		values.put(COL_FALU, data.FALU);
		values.put(COL_GEDT, data.GEDT);
		values.put(COL_GERV, data.GERV);
		values.put(COL_GETC1, data.GETC1);
		values.put(COL_GETC2, data.GETC2);
		values.put(COL_GETD1, data.GETD1);
		values.put(COL_GETN1, data.GETN1);
		values.put(COL_GNEV, data.GNEV);
		values.put(COL_GNEV1, data.GNEV1);
		values.put(COL_GNEV2, data.GNEV2);
		values.put(COL_HALDT, data.HALDT);
		values.put(COL_HKOD, data.HKOD);
		values.put(COL_IRSZ, data.IRSZ);
		values.put(COL_JADT, data.JADT);
		values.put(COL_KAFSZ, data.KAFSZ);
		values.put(COL_MSG1, data.MSG1);
		values.put(COL_MSG2, data.MSG2);
		values.put(COL_NEV, data.NEV);
		values.put(COL_NEV2, data.NEV2);
		values.put(COL_PIP, data.PIP);
		values.put(COL_SZERV, data.SZERV);
		values.put(COL_SZJ01, data.SZJ01);
		values.put(COL_SZJ02, data.SZJ02);
		values.put(COL_SZJ03, data.SZJ03);
		values.put(COL_SZJ04, data.SZJ04);
		values.put(COL_SZJ09, data.SZJ09);
		values.put(COL_TRBSZS, data.TRBSZS);
		values.put(COL_UTCA, data.UTCA);
		values.put(COL_UZORA, data.UZORA);
		values.put(COL_FUSZ, data.FUSZ);
		values.put(COL_ALK, data.ALK);
		values.put(COL_GETI, data.GETI);
		values.put(COL_GT, data.GT);
		values.put(COL_HASZ, data.HASZ);
		values.put(COL_MESZ, data.MESZ);
		values.put(COL_MOSZ, data.MOSZ);
		values.put(COL_PSOK, data.PSOK);
		values.put(COL_UZDT, data.UZDT);
		values.put(COL_UZHEKT, data.UZHEKT);
		values.put(COL_GETI1, data.GETI1);
		values.put(COL_GETI2, data.GETI2);
		values.put(COL_MHB, data.MHB);
		values.put(COL_TEV1, data.TEV1);
		values.put(COL_TEV2, data.TEV2);
		values.put(COL_JAVOK, data.JAVOK);
		values.put(COL_VEV1, data.VEV1);
		values.put(COL_EMAIL, data.EMAIL);
		values.put(COL_FAX, data.FAX);
		values.put(COL_GEALL, data.GEALL);
		values.put(COL_KGW, data.KGW);
		values.put(COL_IPS, data.IPS);
		values.put(COL_ALIRO, data.ALIRO);
		values.put(COL_SZIG, data.SZIG);
		values.put(COL_GPS1, data.GPS1);
		values.put(COL_GPS2, data.GPS2);
		values.put(COL_NUS, data.NUS);
		values.put(COL_NUI, data.N_UI);
		values.put(COL_NTM, data.NTM);
		values.put(COL_NDT, data.NDT);
		values.put(COL_MUS, data.MUS);
		values.put(COL_MUI, data.M_UI);
		values.put(COL_MTM, data.MTM);
		values.put(COL_MDT, data.MDT);
		values.put(COL_ADO, data.ADO);
		values.put(COL_ADAZ, data.ADAZ);
		values.put(COL_TEL, data.TEL);
		values.put(COL_GETI2K, data.GETI2K);
		values.put(COL_MHBK, data.MHBK);
		values.put(COL_TEV1K, data.TEV1K);
		values.put(COL_JZK, data.JZK);
		values.put(COL_LDT, data.LDT);
		values.put(COL_LTM, data.LTM);
		values.put(COL_MODIFIED, data.modified == null ? null : DateUtils.getDfShort().format(data.modified));
		values.put(COL_STATUS, data.status);
		return values;
	}

	@Override
	public MunkalapExport getDataFromCursor(Cursor cursor) {

		MunkalapExport data = new MunkalapExport();
		data.SYSTEM = getString(cursor, COL_SYSTEM);
		data.DATABASE = getString(cursor, COL_DATABASE);
		data.ACT = getString(cursor, COL_ACT);
		data.EGE = getString(cursor, COL_EGE);
		data.GE = getString(cursor, COL_GE);
		data.GEW = getString(cursor, COL_GEW);
		data.GOK = getString(cursor, COL_GOK);
		data.KG = getString(cursor, COL_KG);
		data.PS = getString(cursor, COL_PS);
		data.ALSZ = getString(cursor, COL_ALSZ);
		data.BJTM = getString(cursor, COL_BJTM);
		data.CI = getString(cursor, COL_CI);
		data.FALU = getString(cursor, COL_FALU);
		data.GEDT = getString(cursor, COL_GEDT);
		data.GERV = getString(cursor, COL_GERV);
		data.GETC1 = getString(cursor, COL_GETC1);
		data.GETC2 = getString(cursor, COL_GETC2);
		data.GETD1 = getString(cursor, COL_GETD1);
		data.GETN1 = getString(cursor, COL_GETN1);
		data.GNEV = getString(cursor, COL_GNEV);
		data.GNEV1 = getString(cursor, COL_GNEV1);
		data.GNEV2 = getString(cursor, COL_GNEV2);
		data.HALDT = getString(cursor, COL_HALDT);
		data.HKOD = getString(cursor, COL_HKOD);
		data.IRSZ = getString(cursor, COL_IRSZ);
		data.JADT = getString(cursor, COL_JADT);
		data.KAFSZ = getString(cursor, COL_KAFSZ);
		data.MSG1 = getString(cursor, COL_MSG1);
		data.MSG2 = getString(cursor, COL_MSG2);
		data.NEV = getString(cursor, COL_NEV);
		data.NEV2 = getString(cursor, COL_NEV2);
		data.PIP = getString(cursor, COL_PIP);
		data.SZERV = getString(cursor, COL_SZERV);
		data.SZJ01 = getString(cursor, COL_SZJ01);
		data.SZJ02 = getString(cursor, COL_SZJ02);
		data.SZJ03 = getString(cursor, COL_SZJ03);
		data.SZJ04 = getString(cursor, COL_SZJ04);
		data.SZJ09 = getString(cursor, COL_SZJ09);
		data.TRBSZS = getString(cursor, COL_TRBSZS);
		data.UTCA = getString(cursor, COL_UTCA);
		data.UZORA = getString(cursor, COL_UZORA);
		data.FUSZ = getString(cursor, COL_FUSZ);
		data.ALK = getString(cursor, COL_ALK);
		data.GETI = getString(cursor, COL_GETI);
		data.GT = getString(cursor, COL_GT);
		data.HASZ = getString(cursor, COL_HASZ);
		data.MESZ = getString(cursor, COL_MESZ);
		data.MOSZ = getString(cursor, COL_MOSZ);
		data.PSOK = getString(cursor, COL_PSOK);
		data.UZDT = getString(cursor, COL_UZDT);
		data.UZHEKT = getString(cursor, COL_UZHEKT);
		data.GETI1 = getString(cursor, COL_GETI1);
		data.GETI2 = getString(cursor, COL_GETI2);
		data.MHB = getString(cursor, COL_MHB);
		data.TEV1 = getString(cursor, COL_TEV1);
		data.TEV2 = getString(cursor, COL_TEV2);
		data.JAVOK = getString(cursor, COL_JAVOK);
		data.VEV1 = getString(cursor, COL_VEV1);
		data.EMAIL = getString(cursor, COL_EMAIL);
		data.FAX = getString(cursor, COL_FAX);
		data.GEALL = getString(cursor, COL_GEALL);
		data.KGW = getString(cursor, COL_KGW);
		data.IPS = getString(cursor, COL_IPS);
		data.ALIRO = getString(cursor, COL_ALIRO);
		data.SZIG = getString(cursor, COL_SZIG);
		data.GPS1 = getString(cursor, COL_GPS1);
		data.GPS2 = getString(cursor, COL_GPS2);
		data.NUS = getString(cursor, COL_NUS);
		data.N_UI = getString(cursor, COL_NUI);
		data.NTM = getString(cursor, COL_NTM);
		data.NDT = getString(cursor, COL_NDT);
		data.MUS = getString(cursor, COL_MUS);
		data.M_UI = getString(cursor, COL_MUI);
		data.MTM = getString(cursor, COL_MTM);
		data.MDT = getString(cursor, COL_MDT);
		data.ADO = getString(cursor, COL_ADO);
		data.ADAZ = getString(cursor, COL_ADAZ);
		data.TEL = getString(cursor, COL_TEL);
		data.GETI2K = getString(cursor, COL_GETI2K);
		data.MHBK = getString(cursor, COL_MHBK);
		data.TEV1K = getString(cursor, COL_TEV1K);
		data.JZK = getString(cursor, COL_JZK);
		data.LDT = getString(cursor, COL_LDT);
		data.LTM = getString(cursor, COL_LTM);
		data._id = getInt(cursor, COL_BASE_ID);
		data.modified = getDate(cursor, COL_MODIFIED);
		data.status = getString(cursor, COL_STATUS);
	
		return data;
	}

}