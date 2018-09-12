package hu.itware.kite.service.orm.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

import hu.itware.kite.service.KiteApplication;

public class KiteDatabaseHelper extends SQLiteOpenHelper {

	public static final String TAG = "KITE.SQL";
	public static final String DB_NAME = "kite.db";
	public static final String DB_PATH = "/data/data/hu.itware.kite.service/databases/" + DB_NAME;

	protected static final int DB_VERSION = 84;

	protected static final int MUNKALAP_JEGYZOKONYV_DB_VERSION = 80;
	protected static final int DB_IMPORT_MODIFICATIONS_VERSION = 81;
	protected static final int LDT_LTM_DB_VERSION = 82;
	protected static final int MODIFY_GEPEK_INDEX_VERSION = 83;
	protected static final int MODIFY_MUNKALAPOKDATUM_VERSION = 84;

	private static KiteDatabaseHelper mInstance = null;

	protected Context mContext;

	public static KiteDatabaseHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (mInstance == null) {
			mInstance = new KiteDatabaseHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	private KiteDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
		db.setLocale(new Locale("hu_HU"));
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "onCreate()=" + db);

		for (BaseTable<?> handler : TableMap.getTableHandles()) {

			createTable(db, handler);
		}
	}

	private void createTable(SQLiteDatabase db, BaseTable<?> handler) {

		Column[] columns = handler.getColumns();
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS ").append(handler.getTableName()).append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT,");

		for (int i = 0; i < columns.length; i++) {
			createColumnCreateItem(query, columns[i]);
			if (i < columns.length - 1) {
				query.append(", ");
			}
		}
		query.append(");");
		Log.i(TAG, "Creating table=" + handler.getTableName());
		db.execSQL(query.toString());

		String indexQuery = createIndexQuery(handler.getTableName(), columns);
		if (indexQuery != null) {
			Log.i(TAG, "create index:" + indexQuery);
			db.execSQL(indexQuery);
		} else {
			Log.i(TAG, "Table don't have KEY column:" + handler.getTableName());
		}
	}

	private String createIndexQuery(String table, Column[] columns) {

		String result = "CREATE UNIQUE INDEX IF NOT EXISTS " + table + "_idx ON " + table + "(";
		int count = 0;
		for (Column c : columns) {
			if (c.key) {
				if (count > 0) {
					result += ",";
				}
				result += c.name;
				count++;
			}
		}
		result += ");";
		return count > 0 ? result : null;
	}

	protected void createColumnCreateItem(StringBuilder sb, Column column) {

		sb.append(column.name).append(" ");
		switch (column.type) {
		case TEXT:
			sb.append("TEXT");
			break;
		case INTEGER:
			sb.append("INTEGER");
			break;
		case RESOURCE:
			sb.append("TEXT");
			break;
		case DATE:
			sb.append("INTEGER"); // TODO ez szerintem TEXT
			break;
		case DOUBLE:
			sb.append("DOUBLE");
			break;
		case BOOLEAN:
			sb.append("BOOLEAN");
			break;
		default:
			break;
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.i(TAG, "onUpgrade().from=" + oldVersion + ", to=" + newVersion);

		//--- Update  KITE-792, KITE-796

		if (oldVersion < MUNKALAP_JEGYZOKONYV_DB_VERSION && newVersion >= MUNKALAP_JEGYZOKONYV_DB_VERSION) {
			db.execSQL("ALTER TABLE munkalapok ADD COLUMN jegyzokonyv TEXT DEFAULT 'N'");
			db.execSQL("ALTER TABLE munkalapokexport ADD COLUMN jzk TEXT DEFAULT 'N'");
		}

		if (oldVersion < DB_IMPORT_MODIFICATIONS_VERSION  && newVersion >= DB_IMPORT_MODIFICATIONS_VERSION) {
			//--- Gepek
			db.execSQL("ALTER TABLE gepek ADD COLUMN kjotallas TEXT DEFAULT NULL");
			db.execSQL("ALTER TABLE gepek ADD COLUMN uzemorakorlat INTEGER NOT NULL DEFAULT 0");

			//--- Munkalapok
			db.execSQL("ALTER TABLE munkalapok ADD COLUMN lezarasdatum TEXT DEFAULT NULL");

			//--- Gepszerzodesek
			db.execSQL("ALTER TABLE gepszerzodesek ADD COLUMN partnerkod TEXT DEFAULT NULL");
			db.execSQL("ALTER TABLE gepszerzodesek ADD COLUMN letrehozasdatum TEXT DEFAULT NULL");
			db.execSQL("ALTER TABLE gepszerzodesek ADD COLUMN email TEXT DEFAULT NULL");
		}

		if (oldVersion < LDT_LTM_DB_VERSION  && newVersion >= LDT_LTM_DB_VERSION) {
			db.execSQL("ALTER TABLE munkalapokexport ADD COLUMN ldt TEXT DEFAULT null");
			db.execSQL("ALTER TABLE munkalapokexport ADD COLUMN ltm TEXT DEFAULT null");
		}

		if (oldVersion < MODIFY_GEPEK_INDEX_VERSION && newVersion >= MODIFY_GEPEK_INDEX_VERSION) {
			db.execSQL("DROP INDEX gepek_idx");
			db.execSQL("CREATE UNIQUE INDEX gepek_idx ON gepek(alvazszam,tempgepkod)");
		}

		if (oldVersion < MODIFY_MUNKALAPOKDATUM_VERSION) {
			Log.i(TAG, "Upgrading to database version:" + MODIFY_MUNKALAPOKDATUM_VERSION);
			db.execSQL("update munkalapok set munkakezdesdatum = (substr(munkavegzesdatum, 0, 12)||munkakezdesdatum||':00') " +
					"where munkavegzesdatum is not null and munkakezdesdatum is not null and length(munkakezdesdatum) = 5");

			db.execSQL("update munkalapok set munkabefejezesdatum = (substr(munkavegzesdatum, 0, 12)||munkabefejezesdatum||':00') " +
					"where munkavegzesdatum is not null and munkabefejezesdatum is not null and length(munkabefejezesdatum) = 5");
		}
	}

	public void closeDatabase() {
		if (mInstance != null) {
			close();
			mInstance = null;
		}
	}
}