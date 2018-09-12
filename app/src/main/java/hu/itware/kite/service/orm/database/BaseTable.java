package hu.itware.kite.service.orm.database;

import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import hu.itware.kite.service.orm.utils.DateUtils;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public abstract class BaseTable<T extends BaseDatabaseObject> implements ITable<T> {

    public static final int NONE = 0;

    public static final int UPLOAD = 1;

    public static final int DOWNLOAD = 2;

    public static final int UPLOADDOWNLOAD = 3;

    protected Class<T> clazz;

    protected int direction = DOWNLOAD;

    public BaseTable(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getModelClass() {
        return clazz;
    }

    public int getInt(Cursor c, String column) {
        return c.getInt(c.getColumnIndex(column));
    }

    public long getLong(Cursor c, String column) {
        return c.getLong(c.getColumnIndex(column));
    }


    public Double getDouble(Cursor c, String column) {
        int colIndex = c.getColumnIndex(column);
        return c.isNull(colIndex) ? null : c.getDouble(colIndex);
    }

    public String getString(Cursor c, String column) {
        return c.getString(c.getColumnIndex(column));
    }

    public String[] getStringArray(Cursor c, String column) {
        return c.getString(c.getColumnIndex(column)).split(",");
    }

    public Boolean getBoolean(Cursor c, String column) {
        int value = c.getInt(c.getColumnIndex(column));
        return value == 1;
    }

    public Date getDate(Cursor c, String column) {
        String time = c.getString(c.getColumnIndex(column));
//        if (column.equals("lejaratdatum") || column.equals("kezdesdatum")) {
//            Log.e("TABLE", column + " time: " + time);
//        }
        if (time != null && time.length() == 5) { // ha csak idopont van megadva, akkor csapjuk hozza a mai napot is, hogy az oraszamitasnal ne legyen gond
            time = DateUtils.getDfOnlyDate().format(new Date()) + " " + time + ":00";
        }
        return DateUtils.getDateFromString(time);
    }

    public float getFloat(Cursor c, String column) {
        return c.getFloat(c.getColumnIndex(column));
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public T postUpdate(KiteORM orm, T item) {
        return item;
    }

    public ContentValues postUpdate(KiteORM orm, ContentValues item) {
        return item;
    }
}
