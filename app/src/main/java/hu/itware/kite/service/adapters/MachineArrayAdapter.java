package hu.itware.kite.service.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.MachineDetailsActivity;
import hu.itware.kite.service.orm.model.Gep;

/**
 * Created by szeibert on 2015.09.21..
 */
public class MachineArrayAdapter extends ArrayAdapter<Gep> {

    private int resId;

    public MachineArrayAdapter(Context context, int resource) {
        super(context, resource);
        resId = resource;
    }

    public MachineArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        resId = resource;
    }

    public MachineArrayAdapter(Context context, int resource, Gep[] objects) {
        super(context, resource, objects);
        resId = resource;
    }

    public MachineArrayAdapter(Context context, int resource, int textViewResourceId, Gep[] objects) {
        super(context, resource, textViewResourceId, objects);
        resId = resource;
    }

    public MachineArrayAdapter(Context context, int resource, List<Gep> objects) {
        super(context, resource, objects);
        resId = resource;
    }

    public MachineArrayAdapter(Context context, int resource, int textViewResourceId, List<Gep> objects) {
        super(context, resource, textViewResourceId, objects);
        resId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Gep gep = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resId, parent, false);

            holder = new ViewHolder();

            holder.serialNumber = (TextView) convertView.findViewById(R.id.machine_list_tv_serial_number);
            holder.name = (TextView) convertView.findViewById(R.id.machine_list_tv_name);
            holder.manufactureDate = (TextView) convertView.findViewById(R.id.machine_list_tv_manufacture_date);
            holder.warrantyEnd = (TextView) convertView.findViewById(R.id.machine_list_tv_warranty_end_date);
            holder.details = (Button) convertView.findViewById(R.id.gep_btn_service_history);
            holder.extendedWarranty = (TextView) convertView.findViewById(R.id.machine_list_tv_extended_warranty);
            holder.workhourLimit = (TextView) convertView.findViewById(R.id.machine_list_tv_workhour_limit);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.serialNumber.setText(gep.alvazszam);
        holder.name.setText(gep.tipushosszunev);
        holder.manufactureDate.setText(gep.gyartaseve);
        holder.warrantyEnd.setText(gep.garanciaervenyesseg != null ? (new SimpleDateFormat("yyyy-MM-dd")).format(gep.garanciaervenyesseg) : "");
        holder.extendedWarranty.setText(gep.kjotallas != null ? (new SimpleDateFormat("yyyy-MM-dd")).format(gep.kjotallas) : "");
        holder.workhourLimit.setText(gep.uzemorakorlat != null ? Double.toString(gep.uzemorakorlat) : "");
        if (holder.details != null) {
            holder.details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), MachineDetailsActivity.class);
                    intent.putExtra(MachineDetailsActivity.DETAILS_MODE, MachineDetailsActivity.MODE_DETAILS_ONLY);
                    intent.putExtra(MachineDetailsActivity.GEP_ALVAZSZAM, gep.alvazszam);
                    getContext().startActivity(intent);
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {
        TextView serialNumber, name, manufactureDate, warrantyEnd, extendedWarranty, workhourLimit;
        Button details;
    }
}
