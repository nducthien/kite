package hu.itware.kite.service.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesekTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.GepSzerzodes;
import hu.itware.kite.service.orm.model.Partner;

/**
 * Created by szeibert on 2017.06.14..
 *
 * TODO: Egyelore meg nem kell, de majd be kell fejezni
 */

public class AutocompleteEmailArrayAdapter extends ArrayAdapter<String> {

    String partnerkod;

    public AutocompleteEmailArrayAdapter(Context context, String partnerkod) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        initList();
    }

    private void initList() {
        clear();
        KiteORM kiteORM = new KiteORM(getContext());
        Partner partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod});
        add(partner.email);
        add(partner.partnerkapcsolatok);
        List<GepSzerzodes> gepSzerzodesList = kiteORM.listOrdered(GepSzerzodes.class, GepSzerzodesekTable.COL_PARTNERKOD + " = ? limit 1", new String[]{partnerkod}, GepSzerzodesekTable.COL_LETREHOZASDATUMA + " desc");
        if (gepSzerzodesList.size() > 0 && gepSzerzodesList.get(0).email != null && !gepSzerzodesList.get(0).email.isEmpty()) {
            add(gepSzerzodesList.get(0).email);
        }

    }
}
