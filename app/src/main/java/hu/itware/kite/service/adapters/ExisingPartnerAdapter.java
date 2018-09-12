package hu.itware.kite.service.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.utils.StringUtils;

/**
 * Created by gyongyosit on 2015.09.08..
 */
public class ExisingPartnerAdapter extends KiteCursorAdapter<Partner> {

    public ExisingPartnerAdapter(Context context, Cursor c) {
        super(Partner.class, context, c, R.layout.list_item_partner_name);
    }

    @Override
    public void createView(View view, Context constext, Partner mCustomer) {

        TextView name = (TextView) view.findViewById(R.id.cursor_item1);
        name.setText(mCustomer.toString());
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        int nev1ColumnIndex = cursor.getColumnIndexOrThrow(PartnerekTable.COL_NEV1);
        String nev1 = cursor.getString(nev1ColumnIndex);
        int nev2ColumnIndex = cursor.getColumnIndexOrThrow(PartnerekTable.COL_NEV2);
        String nev2 = cursor.getString(nev2ColumnIndex);

        if (StringUtils.isEmpty(nev2)) {
            return nev1!=null?nev1:"";
        }
        return nev1 + " " + nev2;
    }
}
