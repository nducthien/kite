package hu.itware.kite.service.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.database.tables.KeszletMozgasokTable;
import hu.itware.kite.service.orm.model.KeszletMozgas;

/**
 * Created by gyongyosit on 2015.10.27..
 */
public class AutocompleteKeszletmozgasAdapter extends KiteCursorAdapter<KeszletMozgas> {

    boolean bizonylat;

    public AutocompleteKeszletmozgasAdapter(Context context, Cursor c, boolean bizonylat) {
        super(KeszletMozgas.class, context, c, R.layout.list_item_partner_name);
        this.bizonylat = bizonylat;
    }

    @Override
    public void createView(View view, Context constext, KeszletMozgas keszletMozgas) {

        TextView name = (TextView) view.findViewById(R.id.cursor_item1);
        if(bizonylat) {
            name.setText(keszletMozgas.bizonylatszam);
        } else {
            name.setText(keszletMozgas.agazatiszam+"/"+keszletMozgas.szamlasorszam);
        }
        view.setTag(keszletMozgas);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        if(bizonylat) {
            int columnIndex = cursor.getColumnIndexOrThrow(KeszletMozgasokTable.COL_BIZONYLATSZAM);
            String str = cursor.getString(columnIndex);
            return str;
        } else {
            int columnIndex = cursor.getColumnIndexOrThrow(KeszletMozgasokTable.COL_AGAZATISZAM);
            int columnIndex2 = cursor.getColumnIndexOrThrow(KeszletMozgasokTable.COL_SZAMLASORSZAM);
            String str = cursor.getString(columnIndex);
            String str2 = cursor.getString(columnIndex2);
            return str+"/"+str2;
        }
    }
}
