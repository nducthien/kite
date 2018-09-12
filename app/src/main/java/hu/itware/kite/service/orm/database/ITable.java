package hu.itware.kite.service.orm.database;

import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import android.content.ContentValues;
import android.database.Cursor;

public interface ITable<T extends BaseDatabaseObject> {
    
    public static final String COL_BASE_ID = "_id";
    
    public static final String COL_BASE_MODIFIED = "modified";
    
	/** Get the table nev1 of the handler.
	 * @return the nev1 of the current table
	 */
	public String getTableName();

	/** Get the columns of the table.
	 * @return the columns of the current table
	 */
	public Column[] getColumns();
	
	/**
	 * Get data object from Cursor
	 * @param cursor the Cursor 
	 * @return
	 */
	public T getDataFromCursor(Cursor cursor);

	/**
	 * Convert data object to ContentValues
	 * @param data Data object to be converted
	 * @return ContentValues of the data object
	 */
	public ContentValues getContentValues(T data);

	/**
	 * Get _id value from the data object
	 * @param data the data object
	 * @return _id value of the data object
	 */
	public long getDataId(T data);
}
