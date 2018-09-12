package hu.itware.kite.service.adapters;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MachineDetailsActivity;

/**
 * Created by szeibert on 2017.06.21..
 */

public class PartnerSzerzodesArrayAdapter extends ArrayAdapter<PartnerSzerzodesArrayAdapter.PartnerSzerzodes> {

    public static class PartnerSzerzodes {
        public String alvazszam;
        public String machineName;
        public String contractType;
        public String contractNumber;
        public Date startDate;
        public Date endDate;
    }

    public PartnerSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    public PartnerSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public PartnerSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull PartnerSzerzodes[] objects) {
        super(context, resource, objects);
    }

    public PartnerSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull PartnerSzerzodes[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public PartnerSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PartnerSzerzodes> objects) {
        super(context, resource, objects);
    }

    public PartnerSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<PartnerSzerzodes> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_contract_partner, parent, false);

            holder = new ViewHolder();

            holder.alvazszam = (TextView) convertView.findViewById(R.id.contract_list_alvazszam);
            holder.machineName = (TextView) convertView.findViewById(R.id.contract_list_machine_name);
            holder.contractType = (TextView) convertView.findViewById(R.id.contract_list_contract_type);
            holder.contractNumber = (TextView) convertView.findViewById(R.id.contract_list_contract_number);
            holder.startDate = (TextView) convertView.findViewById(R.id.contract_list_start_date);
            holder.endDate = (TextView)convertView.findViewById(R.id.contract_list_end_date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PartnerSzerzodes contract = getItem(position);

        holder.alvazszam.setText(contract.alvazszam);
        holder.machineName.setText(contract.machineName);
        if (getContext() instanceof MachineDetailsActivity) {
            holder.contractType.setText(((MachineDetailsActivity) getContext()).getContractType(contract.contractType));
        }
        holder.contractNumber.setText(contract.contractNumber);
        holder.startDate.setText(BaseActivity.getSdfShort().format(contract.startDate));
        holder.endDate.setText(BaseActivity.getSdfShort().format(contract.endDate));

        return convertView;
    }

    static class ViewHolder {
        TextView alvazszam;
        TextView machineName;
        TextView contractType;
        TextView contractNumber;
        TextView startDate;
        TextView endDate;
    }
}

