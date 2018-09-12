package hu.itware.kite.service.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.fragments.AlkatreszekKezeleseDialog;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.utils.DecimalDigitsInputFilter;
import hu.itware.kite.service.utils.IdGenerator;
import hu.itware.kite.service.utils.NumberUtils;

/**
 * Created by gyongyosit on 2015.11.03..
 */
public class AlkatreszCikkszamAdapter extends KiteCursorAdapter<AlkatreszCikkszam> {

    public static final String TAG = "AlkatreszCikkszam";

    Munkalap munkalap;
    KiteORM orm;
    RefreshMunkalapDialogInterface listener;
    Context context;

    public AlkatreszCikkszamAdapter(Context context, Cursor c, Munkalap munkalap, RefreshMunkalapDialogInterface listener) {
        super(AlkatreszCikkszam.class, context, c, R.layout.list_item_alkatreszcikk_add);
        this.munkalap = munkalap;
        this.orm = new KiteORM(context);
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void createView(View view, Context constext, final AlkatreszCikkszam item) {
        TextView tvCikkszam = (TextView)view.findViewById(R.id.alkatresz_list_tv_cikkszam);
        TextView tvNev = (TextView)view.findViewById(R.id.alkatresz_list_tv_cikknev);
        final EditText etMennyiseg = (EditText)view.findViewById(R.id.alkatresz_list_tv_mennyiseg);
        etMennyiseg.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
        final Spinner mennyisegiEgyseg = (Spinner) view.findViewById(R.id.alkatresz_list_sp_unit);
        mennyisegiEgyseg.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, AlkatreszekKezeleseDialog.UNITS));
        mennyisegiEgyseg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    etMennyiseg.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,2)});
                } else {
                    etMennyiseg.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
                    String mennyiseg = etMennyiseg.getText().toString();
                    if (mennyiseg.contains(".")) {
                        etMennyiseg.setText(mennyiseg.substring(0, mennyiseg.indexOf(".")));
                    } else if (mennyiseg.contains(",")) {
                        etMennyiseg.setText(mennyiseg.substring(0, mennyiseg.indexOf(",")));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvCikkszam.setText("" + item.cikkszam);
        Log.e("cikkszam", item.cikkszam +", status= " + item.status);
        etMennyiseg.setText("1");
        tvNev.setText("" + item.nev);

        view.findViewById(R.id.alkatresz_list_btn_add).setOnClickListener(new AddAlkatreszToMunkalap(item, etMennyiseg, mennyisegiEgyseg));
    }

    private class AddAlkatreszToMunkalap implements View.OnClickListener {

        AlkatreszCikkszam alkatreszCikkszam;
        EditText tvMennyiseg;
        Spinner spEgyseg;

        public AddAlkatreszToMunkalap(AlkatreszCikkszam alkatreszCikkszam, EditText tvMennyiseg, Spinner spEgyseg){
            this.alkatreszCikkszam = alkatreszCikkszam;
            this.tvMennyiseg = tvMennyiseg;
            this.spEgyseg = spEgyseg;
        }

        @Override
        public void onClick(View v) {
            if(munkalap == null){
                Log.e(TAG, "munkalap is null");
                return;
            }

            if(alkatreszCikkszam == null){
                Log.e(TAG, "alkatreszCikkszam is null");
                return;
            }

            BaseActivity activity = (BaseActivity) context;
            if (!activity.hasUzletkotoAzon()) {
                activity.showUzletkotoAzonErrorDialog();
                return;
            }

            if(munkalap.alkatreszek == null){
                munkalap.alkatreszek = new ArrayList<Alkatresz>();
            }
            if ("".equals(tvMennyiseg.getText().toString())) {
                ((MunkalapActivity)context).showErrorDialog(context.getString(R.string.error_dialog_alkatresz_title),context.getString(R.string.dialog_alkatreszek_add_mennyiseg_error));
                return;
            }
            try {
                if (Double.valueOf(tvMennyiseg.getText().toString()) == 0) {
                    ((MunkalapActivity) context).showErrorDialog(context.getString(R.string.error_dialog_alkatresz_title), context.getString(R.string.error_dialog_alkatresz_message));
                    return;
                }
            } catch (NumberFormatException nfe) {
                ((MunkalapActivity) context).showErrorDialog(context.getString(R.string.error_dialog_alkatresz_title), context.getString(R.string.error_dialog_mennyiseg_format));
                return;
            }


			Alkatresz alkatresz = new Alkatresz();
			alkatresz.tempkod = IdGenerator.generate(context);//"C" + IdGenerator.generate(context, 5); KITE-870
            alkatresz.cikknev = alkatreszCikkszam.nev;
            alkatresz.cikkszam = alkatreszCikkszam.cikkszam;
            alkatresz.rogzitesdatum = new Date();
            alkatresz.modified = new Date();
            alkatresz.munkalapkod = munkalap.getMunkalapKod();

            alkatresz.munkalapsorszam = munkalap.munkalapsorszam;
            alkatresz.mozgomennyiseg = NumberUtils.parseDouble(tvMennyiseg.getText().toString());
            alkatresz.mennyisegiegyseg = (String)spEgyseg.getSelectedItem();

            boolean found = false;
            for(Alkatresz alk : munkalap.alkatreszek){
                if(alk.cikkszam.equals(alkatresz.cikkszam) && alk.cikknev.equals(alkatresz.cikknev) &&
                        alk.bizonylatszam == null && alk.agazatiszam == null && alk.sorszam == null) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                alkatresz._id = ContentUris.parseId(orm.insert(alkatresz));
                munkalap.alkatreszek.add(alkatresz);
				munkalap.bizonylatszam = munkalap.getBizonylatSzamok();
				orm.update(munkalap);

                Log.i(TAG, alkatresz.cikkszam + " alkatresz add to munkalap.");
                Toast.makeText(context, context.getString(R.string.dialog_alkatreszek_add, alkatresz.cikkszam), Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, alkatresz.cikkszam + " alkatresz already added to munkalap.");
            }

            orm.update(alkatreszCikkszam);

            if (listener != null) {
                listener.refresh(munkalap);
            }
        }
    }
}
