package hu.itware.kite.service.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.model.BaseDatabaseObject;

public abstract class KiteCursorAdapter<T extends BaseDatabaseObject> extends CursorAdapter {

	protected BaseTable<T> handler;

	protected LayoutInflater mInflater;

	protected int view_id;

	protected KiteORM helper;

	public KiteCursorAdapter(Class<T> clazz, Context context, Cursor c, int view_id) {
		super(context, c, 0);

		this.view_id = view_id;
		this.handler = TableMap.getHandlerByClass(clazz);
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.helper = new KiteORM(context);
	}

	@Override
	public void bindView(View view, Context constext, Cursor cursor) {
		T item = handler.getDataFromCursor(cursor);
		createView(view, constext, item);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(view_id, parent, false);
	}
	
	
	/** create adapter view
	 * @param view 
	 * @param constext
	 * @param item Entity item extracted from Cursor
	 */
	public abstract void createView(View view, Context constext, T item);
	
	
	@Override
	public Object getItem(int position) {		
		return super.getItem(position);
	}
	
	public T getSelectedEntity() {
		Cursor c = getCursor();
		if (c != null) {
			return handler.getDataFromCursor(c);
		}
		
		return null;
	}
}
