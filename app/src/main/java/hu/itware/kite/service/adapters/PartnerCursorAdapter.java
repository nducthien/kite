package hu.itware.kite.service.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import hu.itware.kite.service.orm.model.Partner;

/**
 * Created by szeibert on 2015.10.07..
 */
public class PartnerCursorAdapter extends KiteCursorAdapter<Partner> {


    public PartnerCursorAdapter(Context context, Cursor c) {
        super(Partner.class, context, c, R.layout.list_item_partner_details);
    }

    @Override
    public void createView(View view, Context constext, Partner partner) {

        TextView partnerCode = (TextView) view.findViewById(R.id.partner_list_tv_partnercode);
        TextView name = (TextView) view.findViewById(R.id.partner_list_tv_name);
        TextView address = (TextView) view.findViewById(R.id.partner_list_tv_address);
        TextView email = (TextView) view.findViewById(R.id.partner_list_tv_email);

        partnerCode.setText(partner.getPartnerkod());
        name.setText(partner.getNev());
        address.setText(partner.getAddress());
        email.setText(partner.email);
    }
}
