package hu.itware.kite.service.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.utils.DecimalDigitsInputFilter;
import hu.itware.kite.service.utils.IdGenerator;
import hu.itware.kite.service.utils.ObjectUtils;

/**
 * Created by gyongyosit on 2015.10.15..
 */
public class KeszletMozgasAdapter extends KiteCursorAdapter<KeszletMozgas> {

    public static final String TAG = "KeszletMozgasAdapter";
    Munkalap munkalap;
    KiteORM orm;
    RefreshMunkalapDialogInterface listener;
    Context context;

    public KeszletMozgasAdapter(Context context, Cursor c, Munkalap munkalap, RefreshMunkalapDialogInterface listener) {
        super(KeszletMozgas.class, context, c, R.layout.list_item_alkatresz_add);
        this.munkalap = munkalap;
        this.orm = new KiteORM(context);
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void createView(View view, Context constext, final KeszletMozgas item) {
        TextView tvCikkszam = (TextView)view.findViewById(R.id.alkatresz_list_tv_cikkszam);
        TextView tvCikknev = (TextView)view.findViewById(R.id.alkatresz_list_tv_cikknev);
        EditText etMennyiseg = (EditText)view.findViewById(R.id.alkatresz_list_tv_mennyiseg);
        TextView tvEgyseg = (TextView)view.findViewById(R.id.alkatresz_list_tv_egyseg);
        TextView tvBizonylatszam = (TextView)view.findViewById(R.id.alkatresz_list_tv_bizonylatszam);

        tvCikkszam.setText("" + item.cikkszam);
        tvCikknev.setText("" + item.cikknev);
        etMennyiseg.setText("" + item.mozgomennyiseg.intValue());
        if ("db".equals(item.mennyisegiegyseg)) {
            etMennyiseg.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
        } else {
            etMennyiseg.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,2)});
        }
        tvEgyseg.setText("" + item.mennyisegiegyseg);
        tvBizonylatszam.setText("" + item.bizonylatszam);
        etMennyiseg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText etMennyiseg = (EditText) view;
                if (etMennyiseg.getText().toString().length() > 0 && (Double.valueOf(etMennyiseg.getText().toString().toString()) > item.mozgomennyiseg)) {
                    etMennyiseg.setText("" + item.mozgomennyiseg.intValue());
                    etMennyiseg.setSelection(etMennyiseg.getText().length());
                }
                return false;
            }
        });
        view.findViewById(R.id.alkatresz_list_btn_add).setOnClickListener(new AddAlkatreszToMunkalap(item, etMennyiseg));
    }

    private class AddAlkatreszToMunkalap implements View.OnClickListener {

        KeszletMozgas keszletMozgas;
        EditText tvMennyiseg;

        public AddAlkatreszToMunkalap(KeszletMozgas keszletMozgas, EditText tvMennyiseg){
            this.keszletMozgas = keszletMozgas;
            this.tvMennyiseg = tvMennyiseg;
        }

        @Override
        public void onClick(View v) {
            if(munkalap == null){
                Log.e(TAG, "munkalap is null");
                return;
            }

            if(keszletMozgas == null){
                Log.e(TAG, "keszletMozgas is null");
                return;
            }

            if(munkalap.alkatreszek == null){
                munkalap.alkatreszek = new ArrayList<Alkatresz>();
            }
            if ("".equals(tvMennyiseg.getText().toString())) {
                ((MunkalapActivity)context).showErrorDialog(context.getString(R.string.error_dialog_alkatresz_title),context.getString(R.string.dialog_alkatreszek_add_mennyiseg_error));
                return;
            }
            if(Double.valueOf(tvMennyiseg.getText().toString()) == 0){
                ((MunkalapActivity)context).showErrorDialog(context.getString(R.string.error_dialog_alkatresz_title),context.getString(R.string.error_dialog_alkatresz_message));
                return;
            }

            BaseActivity activity = (BaseActivity) context;
            if (!activity.hasUzletkotoAzon()) {
                activity.showUzletkotoAzonErrorDialog();
                return;
            }

            Alkatresz alkatresz = new Alkatresz();
			alkatresz.tempkod = IdGenerator.generate(context); //"C" + IdGenerator.generate(context, 5); KITE-870
			alkatresz.cikknev = keszletMozgas.cikknev;
            alkatresz.cikkszam = keszletMozgas.cikkszam;
            alkatresz.rogzitesdatum = new Date();
            alkatresz.modified = new Date();
            alkatresz.munkalapkod = munkalap.getMunkalapKod();
            alkatresz.munkalapsorszam = munkalap.munkalapsorszam;
            alkatresz.bizonylatszam = keszletMozgas.bizonylatszam;
            alkatresz.agazatiszam = keszletMozgas.agazatiszam;
            alkatresz.sorszam = keszletMozgas.szamlasorszam;
            alkatresz.mennyisegiegyseg = keszletMozgas.mennyisegiegyseg;
            alkatresz.mozgasazonosito = keszletMozgas.azonosito;
            try {
                alkatresz.mozgomennyiseg = Double.valueOf(tvMennyiseg.getText().toString());
            } catch (Exception e){
                Log.e(TAG, "Mennyiseg nem szam", e);
            }

            boolean found = false;
            for(Alkatresz alk : munkalap.alkatreszek){
                if(alk.cikkszam.equals(alkatresz.cikkszam) && ObjectUtils.nullSafeEquals(alk.mozgasazonosito, alkatresz.mozgasazonosito) && alk.cikknev.equals(alkatresz.cikknev) &&
                        (ObjectUtils.nullSafeEquals(alk.bizonylatszam, alkatresz.bizonylatszam) || (ObjectUtils.nullSafeEquals(alk.agazatiszam, alkatresz.agazatiszam) && ObjectUtils.nullSafeEquals(alk.sorszam, alkatresz.sorszam)))){
                    found = true;
                    break;
                }
            }
            if (!found) {
                munkalap.alkatreszek.add(alkatresz);
				munkalap.bizonylatszam = munkalap.getBizonylatSzamok();

                Log.e(TAG, alkatresz.mozgasazonosito + " alkatresz add to munkalap.");
                Toast.makeText(context, context.getString(R.string.dialog_alkatreszek_add, alkatresz.cikkszam), Toast.LENGTH_SHORT).show();
                alkatresz._id = ContentUris.parseId(orm.insert(alkatresz));

            } else {
                Log.i(TAG, alkatresz.cikkszam + " alkatresz already added to munkalap.");
            }
            munkalap.beforeSave();

            //orm.update(keszletMozgas);

            if (listener != null) {
                listener.refresh(munkalap);
            }
        }
    }
}
