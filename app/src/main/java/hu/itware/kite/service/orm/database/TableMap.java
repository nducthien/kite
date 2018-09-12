package hu.itware.kite.service.orm.database;

import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import hu.itware.kite.service.orm.provider.KiteContentProvider;

import java.util.Collection;
import java.util.HashMap;

import android.content.UriMatcher;

public final class TableMap {

	private static final HashMap<String, BaseTable<? extends BaseDatabaseObject>> MAP = new HashMap<String, BaseTable<? extends BaseDatabaseObject>>();
	private static final HashMap<Integer, String> MAP_NAME = new HashMap<Integer, String>();


	private TableMap() {
		// Hidden
	}
	
	public static void addTableHandler(BaseTable<? extends BaseDatabaseObject> table) {
	    MAP.put(table.getTableName(), table);
	}
	
	public static void register(UriMatcher uriMatcher, int uriTable, int uriTableId, BaseTable<? extends BaseDatabaseObject> table, int direction) {

		uriMatcher.addURI(KiteContentProvider.PROVIDER_NAME, table.getTableName(), uriTable);
		uriMatcher.addURI(KiteContentProvider.PROVIDER_NAME, table.getTableName() + "/#", uriTableId);
		table.setDirection(direction);
		MAP.put(table.getTableName(), table);
		MAP_NAME.put(uriTable, table.getTableName());
		MAP_NAME.put(uriTableId, table.getTableName());
	}


	public static Collection<BaseTable<?>> getTableHandles() {
		return MAP.values();
	}
	
	public static BaseTable<? extends BaseDatabaseObject> getHandlerByTablename(String tablename) {
		return MAP.get(tablename);
	}

	public static BaseTable<? extends BaseDatabaseObject> getHandlerByContentId(int id) {
		String tablename = MAP_NAME.get(id);
		if (tablename != null) {
			return getHandlerByTablename(tablename);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends BaseDatabaseObject> BaseTable<T> getHandlerByClass(Class<T> clazz) {

		for (BaseTable<?> handler : MAP.values()) {
			if (handler.getModelClass() == clazz) {
				return (BaseTable<T>) handler;
			}
		}

		return null;
	}

}
