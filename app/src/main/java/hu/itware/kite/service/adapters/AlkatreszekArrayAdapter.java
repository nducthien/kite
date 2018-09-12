package hu.itware.kite.service.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.AlkatreszekTable;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.Munkalap;

/**
 * Created by gyongyosit on 2015.10.15..
 */
public class AlkatreszekArrayAdapter extends ArrayAdapter<Alkatresz> {

    public static final String TAG = "AlkatreszekArrayAdapter";
    Munkalap munkalap;
    KiteORM orm;
    List<Alkatresz> objects;
    Context context;
    RefreshMunkalapDialogInterface listener;
    List<Alkatresz> originalObjects;

    static class ViewHolder {
        TextView tvCikkszam, tvCikknev, tvMennyiseg, tvBizonylatszam, tvEgyseg;
        ImageButton removebtn;
    }

    public AlkatreszekArrayAdapter(Context context, int resource, Munkalap munkalap, RefreshMunkalapDialogInterface listener) {
        super(context, resource, munkalap.alkatreszek);
        this.munkalap = munkalap;
        this.orm = new KiteORM(context);
        this.objects = munkalap.alkatreszek;
        this.context = context;
        this.listener = listener;
        this.originalObjects = new ArrayList<Alkatresz>();
        this.originalObjects.addAll(munkalap.alkatreszek);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_alkatresz_remove, parent, false);

            holder = new ViewHolder();

            holder.tvCikkszam = (TextView) convertView.findViewById(R.id.alkatresz_list_tv_cikkszam);
            holder.tvCikknev = (TextView) convertView.findViewById(R.id.alkatresz_list_tv_cikknev);
            holder.tvMennyiseg = (TextView) convertView.findViewById(R.id.alkatresz_list_tv_mennyiseg);
            holder.tvBizonylatszam = (TextView) convertView.findViewById(R.id.alkatresz_list_tv_bizonylatszam);
            holder.removebtn = (ImageButton)convertView.findViewById(R.id.alkatresz_list_btn_remove);
            holder.tvEgyseg = (TextView)convertView.findViewById(R.id.alkatresz_list_tv_egyseg);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Alkatresz alkatresz = getItem(position);
        Log.e(TAG, "state= " + alkatresz.status);
        if(alkatresz != null) {
            holder.tvCikkszam.setText(alkatresz.cikkszam == null ? "-" : alkatresz.cikkszam);
            holder.tvCikknev.setText(alkatresz.cikknev == null ? "-" : alkatresz.cikknev);
            holder.tvMennyiseg.setText(alkatresz.mozgomennyiseg == null ? "-" : BaseActivity.getDfLong().format(alkatresz.mozgomennyiseg));
            holder.tvBizonylatszam.setText(alkatresz.bizonylatszam == null ? "-" : alkatresz.bizonylatszam);
            holder.tvEgyseg.setText(alkatresz.mennyisegiegyseg == null ? "-" : alkatresz.mennyisegiegyseg);
            int mode = ((MunkalapFragmentInterface)getContext()).getMode();
            if ((mode != MunkalapActivity.MODE_VIEW && mode != MunkalapActivity.MODE_OWN) || (mode == MunkalapActivity.MODE_OWN && !originalObjects.contains(alkatresz))) {
                holder.removebtn.setOnClickListener(new RemoveAlkatreszFromMunkalap(position));
                holder.removebtn.setVisibility(View.VISIBLE);
            } else {
                holder.removebtn.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private class RemoveAlkatreszFromMunkalap implements View.OnClickListener {

        int pos;

        public RemoveAlkatreszFromMunkalap(int pos){
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            if(munkalap == null){
                Log.e(TAG, "munkalap is null");
                return;
            }

            if(munkalap.alkatreszek == null){
                return;
            }
            if (munkalap.alkatreszek.size() <= pos) {
                return;
            }
            Alkatresz alkatresz = munkalap.alkatreszek.get(pos);
            Toast.makeText(context, context.getString(R.string.dialog_alkatreszek_remove), Toast.LENGTH_SHORT).show();
            Log.i(TAG, pos + " alkatresz remove from munkalap. cikkszam: " + alkatresz.cikkszam + " sorszam: " + alkatresz.sorszam);
            orm.delete(Alkatresz.class, AlkatreszekTable.COL_BASE_ID + " = ? ", new String[]{Long.toString(alkatresz._id)});
            orm.update(munkalap);

            munkalap.alkatreszek.remove(pos);
			munkalap.bizonylatszam = munkalap.getBizonylatSzamok();
			orm.update(munkalap);

            if (listener != null) {
                listener.refresh(munkalap);
            }
        }
    }

    public void setObjects(List<Alkatresz> objects){
        this.objects = objects;
        notifyDataSetChanged();
    }
}
