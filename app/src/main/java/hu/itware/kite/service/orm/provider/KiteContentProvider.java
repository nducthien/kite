package hu.itware.kite.service.orm.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.KiteDatabaseHelper;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.database.tables.AlkatreszCikkszamokTable;
import hu.itware.kite.service.orm.database.tables.AlkatreszExportokTable;
import hu.itware.kite.service.orm.database.tables.AlkatreszekTable;
import hu.itware.kite.service.orm.database.tables.AlkozpontokTable;
import hu.itware.kite.service.orm.database.tables.GPSDataTable;
import hu.itware.kite.service.orm.database.tables.GepExportokTable;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesTetelekTable;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesekTable;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.KeszletMozgasokTable;
import hu.itware.kite.service.orm.database.tables.KonfigTable;
import hu.itware.kite.service.orm.database.tables.MetaDataTable;
import hu.itware.kite.service.orm.database.tables.MunkalapExportokTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.database.tables.SyncDataTable;
import hu.itware.kite.service.orm.database.tables.UzletkotokTable;

public class KiteContentProvider extends ContentProvider {

	public static final String TAG = "KITE.CP";

	public static final String PROVIDER_NAME = "hu.itware.service.provider.Kite";
	public static final String URL = "content://" + PROVIDER_NAME;
	public static final Uri CONTENT_URI = Uri.parse(URL);

	public static final int URI_GPSDATA = 13;

	public static final int URI_GPSDATA_ID = 14;

	public static final int URI_ALKOZPONT = 15;

	public static final int URI_ALKOZPONT_ID = 16;

	public static final int URI_PARTNER = 19;
	public static final int URI_PARTNER_ID = 20;

	public static final int URI_GEP = 27;
	public static final int URI_GEP_ID = 28;

	public static final int URI_MUNKALAP = 29;
	public static final int URI_MUNKALAP_ID = 30;

	public static final int URI_ALKATRESZ = 31;
	public static final int URI_ALKATRESZ_ID = 32;

	public static final int URI_GEPSZERZODES = 33;
	public static final int URI_GEPSZERZODES_ID = 34;

	public static final int URI_GEPSZERZODESTETEL = 35;
	public static final int URI_GEPSZERZODESTETEL_ID = 36;

	public static final int URI_METADATA = 37;
	public static final int URI_METADATA_ID = 38;

	public static final int URI_GEP_EXPORT = 39;
	public static final int URI_GEP_EXPORT_ID = 40;

	public static final int URI_MUNKALAP_EXPORT = 41;
	public static final int URI_MUNKALAP_EXPORT_ID = 42;

	public static final int URI_ALKATRESZ_EXPORT = 43;
	public static final int URI_ALKATRESZ_EXPORT_ID = 44;

	public static final int URI_KESZLETMOZGAS = 45;
	public static final int URI_KESZLETMOZGAS_ID = 46;

	public static final int URI_ALKATRESZCIKKSZAM = 47;
	public static final int URI_ALKATRESZCIKKSZAM_ID = 48;

	public static final int URI_KONFIG = 49;
	public static final int URI_KONFIG_ID = 50;

	public static final int URI_UZLETKOTOK = 51;
	public static final int URI_UZLETKOTOK_ID = 52;

	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {

		Log.i(TAG, "Creating Kite Content Provider's TableMap...");
		TableMap.register(uriMatcher, URI_GPSDATA, URI_GPSDATA_ID, new GPSDataTable(), BaseTable.UPLOAD);
		TableMap.register(uriMatcher, URI_GEP_EXPORT, URI_GEP_EXPORT_ID, new GepExportokTable(), BaseTable.UPLOAD);
		TableMap.register(uriMatcher, URI_MUNKALAP_EXPORT, URI_MUNKALAP_EXPORT_ID, new MunkalapExportokTable(), BaseTable.UPLOAD);
		TableMap.register(uriMatcher, URI_ALKATRESZ_EXPORT, URI_ALKATRESZ_EXPORT_ID, new AlkatreszExportokTable(), BaseTable.UPLOAD);

		TableMap.register(uriMatcher, URI_ALKOZPONT, URI_ALKOZPONT_ID, new AlkozpontokTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_PARTNER, URI_PARTNER_ID, new PartnerekTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_METADATA, URI_METADATA_ID, new MetaDataTable(), BaseTable.DOWNLOAD);

		TableMap.register(uriMatcher, URI_GEP, URI_GEP_ID, new GepekTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_MUNKALAP, URI_MUNKALAP_ID, new MunkalapokTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_ALKATRESZ, URI_ALKATRESZ_ID, new AlkatreszekTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_GEPSZERZODES, URI_GEPSZERZODES_ID, new GepSzerzodesekTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_GEPSZERZODESTETEL, URI_GEPSZERZODESTETEL_ID, new GepSzerzodesTetelekTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_KESZLETMOZGAS, URI_KESZLETMOZGAS_ID, new KeszletMozgasokTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_ALKATRESZCIKKSZAM, URI_ALKATRESZCIKKSZAM_ID, new AlkatreszCikkszamokTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_KONFIG, URI_KONFIG_ID, new KonfigTable(), BaseTable.DOWNLOAD);
		TableMap.register(uriMatcher, URI_UZLETKOTOK, URI_UZLETKOTOK_ID, new UzletkotokTable(), BaseTable.DOWNLOAD);

		TableMap.register(uriMatcher, 99, 100, new SyncDataTable(), BaseTable.NONE);

		TableMap.addTableHandler(new SyncDataTable());
		Log.i(TAG, "Kite Content Provider's TableMap created...");
	}

	@Override
	public boolean onCreate() {
		Log.i(TAG, "Kite Content Provider created...");
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		int code = uriMatcher.match(uri);
		BaseTable<?> handler = TableMap.getHandlerByContentId(code);
		if (handler != null) {
			int count = (int) delete(handler.getTableName(), selection, selectionArgs);
			if (count > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
				return count;
			}
		}

		return 0;
	}

	@Override
	public String getType(Uri uri) {
		Log.i(TAG, "getType()=" + uri);
		Log.i(TAG, "getType().matchId=" + uriMatcher.match(uri));

		switch (uriMatcher.match(uri)) {
		case URI_GPSDATA:
			return "vnd.android.cursor.dir/vnd.kite.gpsdata";
		case URI_GPSDATA_ID:
			return "vnd.android.cursor.item/vnd.kite.gpsdata";
		case URI_ALKOZPONT:
			return "vnd.android.cursor.dir/vnd.kite.alkozpontok";
		case URI_ALKOZPONT_ID:
			return "vnd.android.cursor.item/vnd.kite.alkozpontok";
		case URI_PARTNER:
			return "vnd.android.cursor.dir/vnd.kite.partnerek";
		case URI_PARTNER_ID:
			return "vnd.android.cursor.item/vnd.kite.partnerek";
		case URI_METADATA:
			return "vnd.android.cursor.dir/vnd.kite.metadata";
		case URI_METADATA_ID:
			return "vnd.android.cursor.item/vnd.kite.metadata";
		case URI_GEP:
			return "vnd.android.cursor.dir/vnd.kite.gepek";
		case URI_GEP_ID:
			return "vnd.android.cursor.item/vnd.kite.gepek";
		case URI_ALKATRESZ:
			return "vnd.android.cursor.dir/vnd.kite.alkatreszek";
		case URI_ALKATRESZ_ID:
			return "vnd.android.cursor.item/vnd.kite.alkatreszek";
		case URI_MUNKALAP:
			return "vnd.android.cursor.dir/vnd.kite.munkalapok";
		case URI_MUNKALAP_ID:
			return "vnd.android.cursor.item/vnd.kite.munkalapok";
		case URI_GEP_EXPORT:
			return "vnd.android.cursor.dir/vnd.kite.gepexportok";
		case URI_GEP_EXPORT_ID:
			return "vnd.android.cursor.item/vnd.kite.gepexportok";
		case URI_ALKATRESZ_EXPORT:
			return "vnd.android.cursor.dir/vnd.kite.alkatreszexportok";
		case URI_ALKATRESZ_EXPORT_ID:
			return "vnd.android.cursor.item/vnd.kite.alkatreszexportok";
		case URI_MUNKALAP_EXPORT:
			return "vnd.android.cursor.dir/vnd.kite.munkalapexportok";
		case URI_MUNKALAP_EXPORT_ID:
			return "vnd.android.cursor.item/vnd.kite.munkalapexportok";
		case URI_GEPSZERZODES:
			return "vnd.android.cursor.dir/vnd.kite.gepszerzodesek";
		case URI_GEPSZERZODES_ID:
			return "vnd.android.cursor.item/vnd.kite.gepszerzodesek";
		case URI_GEPSZERZODESTETEL:
			return "vnd.android.cursor.dir/vnd.kite.gepszerzodestetelek";
		case URI_GEPSZERZODESTETEL_ID:
			return "vnd.android.cursor.item/vnd.kite.gepszerzodestetelek";
		case URI_KESZLETMOZGAS:
			return "vnd.android.cursor.dir/vnd.kite.keszletmozgasok";
		case URI_KESZLETMOZGAS_ID:
			return "vnd.android.cursor.item/vnd.kite.keszletmozgasok";
		case URI_ALKATRESZCIKKSZAM:
			return "vnd.android.cursor.dir/vnd.kite.alkatreszcikkszamok";
		case URI_ALKATRESZCIKKSZAM_ID:
			return "vnd.android.cursor.item/vnd.kite.alkatreszcikkszamok";
		case URI_KONFIG:
			return "vnd.android.cursor.dir/vnd.kite.konfig";
		case URI_KONFIG_ID:
			return "vnd.android.cursor.item/vnd.kite.konfig";
		case URI_UZLETKOTOK:
			return "vnd.android.cursor.dir/vnd.kite.uzletkotok";
		case URI_UZLETKOTOK_ID:
			return "vnd.android.cursor.item/vnd.kite.uzletkotok";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		int code = uriMatcher.match(uri);
		BaseTable<?> handler = TableMap.getHandlerByContentId(code);
		if (handler != null) {
			long rowId = insert(handler.getTableName(), values);
			if (rowId > 0) {
				Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(resultUri, null);
				return resultUri;
			}
		}

		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int code = uriMatcher.match(uri);
		BaseTable<?> handler = TableMap.getHandlerByContentId(code);
		if (handler != null) {
			return query(handler.getTableName(), projection, selection, selectionArgs, sortOrder);
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int code = uriMatcher.match(uri);
		BaseTable<?> handler = TableMap.getHandlerByContentId(code);
		if (handler != null) {
			return update(handler.getTableName(), values, selection, selectionArgs);
		}
		return 0;
	}

	public long insert(String table, ContentValues values) {

		try {
			beginTransaction();
			long res = KiteDatabaseHelper.getInstance(getContext()).getWritableDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			setTransactionSuccess();
			return res;
		} finally {
			endTransaction();
		}
	}

	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {

		try {
			beginTransaction();
			int res = KiteDatabaseHelper.getInstance(getContext()).getWritableDatabase().update(table, values, whereClause, whereArgs);
			setTransactionSuccess();
			return res;
		} finally {
			endTransaction();
		}
	}

	public Cursor query(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return KiteDatabaseHelper.getInstance(getContext()).getReadableDatabase().query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	public long delete(String table, String where, String[] whereArgs) {
		long res = 0;
		try {
			beginTransaction();
			res = KiteDatabaseHelper.getInstance(getContext()).getWritableDatabase().delete(table, where, whereArgs);
			setTransactionSuccess();
		} finally {
			endTransaction();
		}
		return res;
	}

	public void beginTransaction() {
		KiteDatabaseHelper.getInstance(getContext()).getWritableDatabase().beginTransaction();
	}

	public void endTransaction() {
		KiteDatabaseHelper.getInstance(getContext()).getWritableDatabase().endTransaction();
	}

	public void setTransactionSuccess() {
		KiteDatabaseHelper.getInstance(getContext()).getWritableDatabase().setTransactionSuccessful();
	}

}
