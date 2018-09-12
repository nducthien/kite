package hu.itware.kite.service.orm;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.CursorAdapter;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.KiteDatabaseHelper;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.Photo;
import hu.itware.kite.service.orm.utils.UriUtils;

public final class KiteORM {

    public static final String TAG = "KITE.ORM";

    protected Context context;

    private ContentResolver resolver;

    public KiteORM(Context context) {
        this.context = context.getApplicationContext();
        this.resolver = this.context.getContentResolver();
    }


    private String filterStateD(String selection) {
        if (selection != null) {
            selection = "(status is null OR status != 'D') AND " + selection;
        } else {
            selection = "status is null OR status != 'D'";
        }
        return selection;
    }

    public int getCount(String tablename, String selection, String[] selectionArgs) {

        BaseTable<?> table = TableMap.getHandlerByTablename(tablename);
        if (table != null) {
            return getCount(table.getModelClass(), selection, selectionArgs);
        }
        return 0;
    }

    public int getCount(String tablename) {

        BaseTable<?> table = TableMap.getHandlerByTablename(tablename);
        if (table != null) {
            return getCount(table.getModelClass());
        }
        return 0;
    }

    public <T extends BaseDatabaseObject> int getCount(Class<T> clazz) {
        return getCount(clazz, null, null);
    }

    public int getNativeCount(String selection, String[] args) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        if(db.isOpen()) {
            try {
                cursor = db.rawQuery(selection, args);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int count = cursor.getInt(0);
                    return count;
                }
            }catch (Exception e){
                Log.d("Error Close db",e.toString());
            }finally {
                if(cursor != null) {
                    cursor.close();
                }
            }
        }
        return 0;
    }

    public <T extends BaseDatabaseObject> int getCount(Class<T> clazz, String selection, String[] selectionArgs) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()){
            selection = filterStateD(selection);
            Cursor cursor = query(clazz, selection, selectionArgs, null);
            try {
                if (cursor != null) {
                    return cursor.getCount();
                }
            } finally {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "Could not close cursor", e);
                }
            }
        }
        return 0;
    }

    public <T extends BaseDatabaseObject> Cursor query(Class<T> clazz, String selection, String[] selectionArgs, String sortOrder) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            selection = filterStateD(selection);
            Log.d(TAG, "query()=" + clazz.getSimpleName() + ", selection=" + selection + ", args=" + printArgs(selectionArgs));
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);
            if (table != null) {
                Uri uri = UriUtils.getTableUri(table.getTableName());
                return resolver.query(uri, null, selection, selectionArgs, sortOrder);
            }
        }
        return null;

    }

    public <T extends BaseDatabaseObject> Cursor query(Class<T> clazz, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            selection = filterStateD(selection);
            Log.d(TAG, "query()=" + clazz + ", selection=" + selection + ", args=" + printArgs(selectionArgs));
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);
            if (table != null) {
                Uri uri = UriUtils.getTableUri(table.getTableName());
                return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
            }
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    public <T extends BaseDatabaseObject> T loadSingle(Class<T> clazz, String selection, String[] selectionArgs) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);
            if (table != null) {
                selection = filterStateD(selection);
                //Log.d(TAG, "loadSingle()=" + selection + ", args=" + printArgs(selectionArgs));
                Uri uri = UriUtils.getTableUri(table.getTableName());
                Cursor cursor = resolver.query(uri, null, selection, selectionArgs, null);

                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        return (T) table.getDataFromCursor(cursor);
                    }
                } finally {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Could not close cursor", e);
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseDatabaseObject> T loadSingleWithoutStatusD(Class<T> clazz, String selection, String[] selectionArgs) {

        Log.d(TAG, "loadSingle()=" + selection + ", args=" + printArgs(selectionArgs));
        BaseTable<?> table = TableMap.getHandlerByClass(clazz);
        if (table != null) {
            Uri uri = UriUtils.getTableUri(table.getTableName());
            Cursor cursor = resolver.query(uri, null, selection, selectionArgs, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    return (T) table.getDataFromCursor(cursor);
                }
            } finally {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "Could not close cursor", e);
                }
            }
        }

        return null;
    }


    @SuppressWarnings({"unchecked"})
    public <T extends BaseDatabaseObject> long updateOrInsert(T data) {

        Log.d(TAG, "updateOrInsert()=" + data);
        T item = (T) loadSingle(data.getClass(), "_id = ?", new String[]{"" + data._id});
        if (item == null) {
            Uri uri = insert(data);
            return ContentUris.parseId(uri);
        }

        return update(data);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends BaseDatabaseObject> long update(T data) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            Log.d(TAG, "update()=" + data);
            BaseTable table = TableMap.getHandlerByClass(data.getClass());
            if (table != null) {
                Uri uri = UriUtils.getTableIdUri(table.getTableName(), data._id);
                return resolver.update(uri, table.getContentValues(data), "_id = ?", new String[]{"" + data._id});
            }
        }
        return -1;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends BaseDatabaseObject> Uri insert(T data) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            BaseTable table = TableMap.getHandlerByClass(data.getClass());
            if (table != null) {
                Uri uri = UriUtils.getTableUri(table.getTableName());
                return resolver.insert(uri, table.getContentValues(data));
            }
        }
        return null;
    }

    public Uri insert(BaseTable table, ContentValues data) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            Uri uri = UriUtils.getTableUri(table.getTableName());
            return resolver.insert(uri, data);
        }
        return null;
    }

    public int bulkInsert(BaseTable table, ContentValues[] data) {
        Log.d(TAG, "bulkInsert()=" + data.length);
        Uri uri = UriUtils.getTableUri(table.getTableName());
        return resolver.bulkInsert(uri, data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends BaseDatabaseObject> int bulkInsert(List<T> datas) {

        if (datas == null || datas.isEmpty()) {
            return 0;
        }

        Log.d(TAG, "bulkInsert()=" + datas.size());
        BaseTable table = TableMap.getHandlerByClass(datas.get(0).getClass());
        if (table != null) {
            int count = datas.size();
            Uri uri = UriUtils.getTableUri(table.getTableName());
            ContentValues[] values = new ContentValues[count];
            for (int i = 0; i < count; i++) {
                values[i] = table.getContentValues(datas.get(i));
            }
            Log.d(TAG, "bulkInsert().inserting records...");
            int rowCount = resolver.bulkInsert(uri, values);
            Log.d(TAG, "bulkInsert().success, inserted record count:" + rowCount);
            return rowCount;
        }

        return -1;
    }


    public <T extends BaseDatabaseObject> int delete(T data) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            Log.d(TAG, "delete()=" + data);

            BaseTable<?> table = TableMap.getHandlerByClass(data.getClass());
            Uri uri = UriUtils.getTableIdUri(table.getTableName(), data._id);
            return resolver.delete(uri, "_id = ?", new String[]{"" + data._id});
        }
        return 0;
    }

    public <T extends BaseDatabaseObject> int delete(Class<T> clazz, String selection, String[] selectionArgs) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);

            Uri uri = UriUtils.getTableUri(table.getTableName());
            return resolver.delete(uri, selection, selectionArgs);
        }
        return 0;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BaseDatabaseObject> List<T> list(Class<T> clazz, String selection, String[] selectionArgs) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            selection = filterStateD(selection);
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);

            Uri uri = UriUtils.getTableUri(table.getTableName());
            Cursor cursor = resolver.query(uri, null, selection, selectionArgs, null);
            return getListFromCursor(cursor, table);
        } else {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BaseDatabaseObject> List<T> listWithoutFilterD(Class<T> clazz, String selection, String[] selectionArgs) {

        BaseTable<?> table = TableMap.getHandlerByClass(clazz);

        Uri uri = UriUtils.getTableUri(table.getTableName());
        Cursor cursor = resolver.query(uri, null, selection, selectionArgs, null);
        return getListFromCursor(cursor, table);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BaseDatabaseObject> List<T> listOrdered(Class<T> clazz, String selection, String[] selectionArgs, String sortOrder) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            selection = filterStateD(selection);
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);
            Log.e(TAG, "selection= " + selection);
            Uri uri = UriUtils.getTableUri(table.getTableName());
            Cursor cursor = resolver.query(uri, null, selection, selectionArgs, sortOrder);
            return getListFromCursor(cursor, table);
        } else {
            return null;
        }
    }

    public <T extends BaseDatabaseObject> List<T> getListFromCursor(Cursor cursor, BaseTable<?> table) {
        return getListFromCursor(cursor, table, true);
    }

    public <T extends BaseDatabaseObject> List<T> getListFromCursor(Cursor cursor, BaseTable<?> table, boolean close) {
        ArrayList<T> results = new ArrayList<T>();
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            try {
                for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
                    T data = (T) table.getDataFromCursor(cursor);
                    results.add(data);
                }
            } finally {
                try {
                    if (close) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Could not close cursor", e);
                }
            }
        }
        return results;
    }


    private String printArgs(String[] args) {
        if (args == null) {
            return "NULL";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    public static String generateQueryString(String query, String[] params) {
        if (params == null) {
            return query;
        }

        for (int i = 0; i < params.length; i++) {
            query = query.replaceFirst("\\?", "'" + params[i] + "'");
        }
        return query;
    }

    public static void closeCursor(CursorAdapter adapter) {
//        if (adapter != null && adapter.getCursor()!= null && !adapter.getCursor().isClosed()) {
//            adapter.getCursor().close();
//        }
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public <T extends BaseDatabaseObject> List<T> getListMunkalapfirst(Class<T> clazz, String selection, String[] selectionArgs) {
        KiteDatabaseHelper helper = KiteDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            selection = filterStateD(selection);
            BaseTable<?> table = TableMap.getHandlerByClass(clazz);

            Uri uri = UriUtils.getTableUri(table.getTableName());
            Cursor cursor = resolver.query(null, null, selection, selectionArgs, null);
            return getListFromCursor(cursor, table);
        } else {
            return new ArrayList<>();
        }
    }
}
