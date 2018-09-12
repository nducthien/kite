package hu.itware.kite.service.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.model.Gep;

/**
 * Created by szeibert on 2015.12.16..
 */
public class AutocompleteGepAdapter extends KiteCursorAdapter<Gep> {

    public AutocompleteGepAdapter(Context context, Cursor c) {
        super(Gep.class, context, c, R.layout.list_item_partner_name);
    }

    @Override
    public void createView(View view, Context constext, Gep gep) {

        TextView name = (TextView) view.findViewById(R.id.cursor_item1);
        name.setText(String.format("%s - %s (%s)", gep.alvazszam, gep.tipushosszunev, gep.getPartner().getNev()));
        view.setTag(gep);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        int columnIndex = cursor.getColumnIndexOrThrow(GepekTable.COL_ALVAZSZAM);
        String str = cursor.getString(columnIndex);
        return str;
    }

}