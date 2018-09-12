package hu.itware.kite.service.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.database.tables.AlkatreszCikkszamokTable;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;

/**
 * Created by gyongyosit on 2015.11.03..
 */
public class AutocompleteAlkatreszcikkszamAdapter extends KiteCursorAdapter<AlkatreszCikkszam> {

    public AutocompleteAlkatreszcikkszamAdapter(Context context, Cursor c) {
        super(AlkatreszCikkszam.class, context, c, R.layout.list_item_partner_name);
    }

    @Override
    public void createView(View view, Context constext, AlkatreszCikkszam alkatreszCikkszam) {

        TextView name = (TextView) view.findViewById(R.id.cursor_item1);
        name.setText(alkatreszCikkszam.cikkszam);
        view.setTag(alkatreszCikkszam);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        int columnIndex = cursor.getColumnIndexOrThrow(AlkatreszCikkszamokTable.COL_CIKKSZAM);
        String str = cursor.getString(columnIndex);
        return str;
    }
}