package hu.itware.kite.service.orm.utils;

import hu.itware.kite.service.orm.provider.KiteContentProvider;
import hu.itware.kite.service.settings.Settings;
import android.content.ContentUris;
import android.net.Uri;

public final class UriUtils {

	private UriUtils() {

	}

	/**
	 * Get download URL for table from the given ID.
	 * @param tablename nev1 of the table to be synchronized from server
	 * @param timestamp last sync time
	 * @return
	 */
	public static String getTableSyncUrl(String tablename, long timestamp) {
		return Settings.SERVER_REST_URL + "/" + tablename + "/refresh/" + timestamp;
	}

	public static String getTableRestUrl(String tablename) {
		return Settings.SERVER_REST_URL + "/" + tablename;
	}

	public static String getTableIdRestUrl(String tablename, String id) {
		return Settings.SERVER_REST_URL + "/" + tablename + "/" + id;
	}

	public static Uri getTableUri(String tableName, String path) {

		if (path == null) {
			return getTableUri(tableName);
		}

		if (path.startsWith("/")) {
			return Uri.parse("content://" + KiteContentProvider.PROVIDER_NAME + "/" + tableName + path);
		}

		return Uri.parse("content://" + KiteContentProvider.PROVIDER_NAME + "/" + tableName + "/" + path);
	}

	public static Uri getTableUri(String tableName) {
		return Uri.parse("content://" + KiteContentProvider.PROVIDER_NAME + "/" + tableName);
	}

	public static Uri getTableIdUri(String tableName, long id) {
		Uri uri = Uri.parse("content://" + KiteContentProvider.PROVIDER_NAME + "/" + tableName);
		uri = ContentUris.withAppendedId(uri, id);
		return uri;
	}
}
