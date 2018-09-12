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

import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MachineDetailsActivity;
import hu.itware.kite.service.orm.model.GepSzerzodes;

/**
 * Created by szeibert on 2017.06.21..
 */

public class GepSzerzodesArrayAdapter extends ArrayAdapter<GepSzerzodes> {

    public GepSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    public GepSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public GepSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull GepSzerzodes[] objects) {
        super(context, resource, objects);
    }

    public GepSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull GepSzerzodes[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public GepSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<GepSzerzodes> objects) {
        super(context, resource, objects);
    }

    public GepSzerzodesArrayAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<GepSzerzodes> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_contract_machine, parent, false);

            holder = new ViewHolder();

            holder.partnerName = (TextView) convertView.findViewById(R.id.contract_list_partner_name);
            holder.contractType = (TextView) convertView.findViewById(R.id.contract_list_contract_type);
            holder.contractNumber = (TextView) convertView.findViewById(R.id.contract_list_contract_number);
            holder.startDate = (TextView) convertView.findViewById(R.id.contract_list_start_date);
            holder.endDate = (TextView)convertView.findViewById(R.id.contract_list_end_date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GepSzerzodes contract = getItem(position);

        holder.partnerName.setText(contract.getPartner().getNev());
        if (getContext() instanceof MachineDetailsActivity) {
            holder.contractType.setText(((MachineDetailsActivity) getContext()).getContractType(contract.szerzodestipus));
        }
        holder.contractNumber.setText(contract.szerzodeskod);
        holder.startDate.setText(BaseActivity.getSdfShort().format(contract.kezdesdatum));
        holder.endDate.setText(BaseActivity.getSdfShort().format(contract.lejaratdatum));

        return convertView;
    }

    static class ViewHolder {
        TextView partnerName;
        TextView contractType;
        TextView contractNumber;
        TextView startDate;
        TextView endDate;
    }
}
