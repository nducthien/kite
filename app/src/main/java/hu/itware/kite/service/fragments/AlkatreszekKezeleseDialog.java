package hu.itware.kite.service.fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.adapters.AlkatreszCikkszamAdapter;
import hu.itware.kite.service.adapters.AlkatreszekArrayAdapter;
import hu.itware.kite.service.adapters.AutocompleteAlkatreszcikkszamAdapter;
import hu.itware.kite.service.adapters.AutocompleteKeszletmozgasAdapter;
import hu.itware.kite.service.adapters.KeszletMozgasAdapter;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.AlkatreszCikkszamokTable;
import hu.itware.kite.service.orm.database.tables.AlkatreszekTable;
import hu.itware.kite.service.orm.database.tables.KeszletMozgasokTable;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.utils.DecimalDigitsInputFilter;
import hu.itware.kite.service.utils.IdGenerator;
import hu.itware.kite.service.utils.SzamlaszamInputFilter;

/**
 * Created by gyongyosit on 2015.10.14..
 */
public class AlkatreszekKezeleseDialog extends DialogFragment implements RefreshMunkalapDialogInterface {

    public static final String TAG = "AlkatreszekDialog";
    public static final String EXTRA_MUNKALAP = "EXTRA_MUNKALAP";
    public static final String[] UNITS = new String[]{"db", "g", "dkg", "kg", "mm", "cm", "dm", "m", "m2", "m3", "km", "ml", "L"};

    Munkalap munkalap;
    TabHost tabs;
    Button hozzadBizonylatBtn, hozzadCikkszamBtn, hozzadSzamlaBtn;
    AutoCompleteTextView bizonylatEt, szamlaszamEt, cikkszamEt, bizCikkszamEt, szamlaCikkszamEt;
    EditText mennyisegCikkszamEt, mennyisegBizonylatEt, mennyisegSzamlaEt;
    ListView bizonylatListView, szamlaszamListView, felhasznaltListView,cikkszamListView;
    Spinner bizonylatEgysegSp, cikkszamEgysegSp, szamlaEgysegSp;
    KeszletMozgasAdapter bizonylatAdapter, szamlaszamAdapter;
    AlkatreszekArrayAdapter felhasznaltAlkatreszekAdapter;
    KiteORM orm;
    ProgressBar bizonylatPb, szamlaszamPb,cikkszamPb;
    Boolean tempSearchByBizonylat;
    Boolean tempSearchBySzamlaszam;
    String tempSearchParamByBizonylat;
    String tempSearchParamBySzamlaszam;
    String tempSearchParam2BySzamlaszam;
    String tempSearchParamByCikkszam;
    IDialogResult listener;
    AlkatreszCikkszamAdapter alkatreszCikkszamAdapter;
    RelativeLayout rlCikkszamAdd, rlSzamlaAdd, rlBizonylatAdd;
    Handler handler;

    AutocompleteKeszletmozgasAdapter autocompleteKeszletmozgasAdapter1;
    AutocompleteKeszletmozgasAdapter autocompleteKeszletmozgasAdapter2;
    AutocompleteAlkatreszcikkszamAdapter autocompleteAlkatreszcikkszamAdapter;
    AutocompleteAlkatreszcikkszamAdapter autocompleteSzamlaAlkatreszcikkszamAdapter;
    AutocompleteAlkatreszcikkszamAdapter autocompleteBizAlkatreszcikkszamAdapter;

    public AlkatreszekKezeleseDialog() {
        this.setCancelable(false);
    }

    public void setListener(IDialogResult listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_alkatreszek_kezelese, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        orm = new KiteORM(getActivity());
        handler = new Handler();

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString("title"));

        TextView text = (TextView) view.findViewById(R.id.dialog_text);
        text.setText(getArguments().getString("message"));

        Button buttonOk = (Button) view.findViewById(R.id.dialog_button_right);
        buttonOk.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
        buttonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onOkClicked(AlkatreszekKezeleseDialog.this);
                }
                dismiss();
            }
        });

        tabs=(TabHost)view.findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec tabpage1 = tabs.newTabSpec("Bizonylattal");
        tabpage1.setContent(R.id.tab1);
        tabpage1.setIndicator("Bizonylattal", null);

        TabHost.TabSpec tabpage2 = tabs.newTabSpec("Számlaszámmal");
        tabpage2.setContent(R.id.tab2);
        tabpage2.setIndicator("Számlaszámmal", null);

        TabHost.TabSpec tabpage3 = tabs.newTabSpec("Cikkszámmal");
        tabpage3.setContent(R.id.tab3);
        tabpage3.setIndicator("Cikkszámmal", null);

        tabs.addTab(tabpage1);
        tabs.addTab(tabpage2);
        tabs.addTab(tabpage3);

        munkalap = (Munkalap)getArguments().getSerializable(EXTRA_MUNKALAP);
        if (munkalap == null) {
            Log.e(TAG, "munkalap is null");
        }
        munkalap.afterLoad();

        bizonylatEt = (AutoCompleteTextView) view.findViewById(R.id.dialog_alkatreszek_bizonylat_et_search);
        szamlaszamEt = (AutoCompleteTextView) view.findViewById(R.id.dialog_alkatreszek_cikktorzs_et_search);
        szamlaszamEt.setThreshold(2);
        cikkszamEt = (AutoCompleteTextView) view.findViewById(R.id.dialog_alkatreszek_cikkszam_et_search);
        bizCikkszamEt = (AutoCompleteTextView) view.findViewById(R.id.bizonylat_et_cikkszam);
        szamlaCikkszamEt = (AutoCompleteTextView) view.findViewById(R.id.szamla_et_cikkszam);

        bizonylatEgysegSp = (Spinner) view.findViewById(R.id.bizonylat_sp_unit);
        bizonylatEgysegSp.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, UNITS));
        bizonylatEgysegSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    mennyisegBizonylatEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,2)});
                } else {
                    mennyisegBizonylatEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
                    String mennyiseg = mennyisegBizonylatEt.getText().toString();
                    if (mennyiseg.contains(".")) {
                        mennyisegBizonylatEt.setText(mennyiseg.substring(0, mennyiseg.indexOf(".")));
                    } else if (mennyiseg.contains(",")) {
                        mennyisegBizonylatEt.setText(mennyiseg.substring(0, mennyiseg.indexOf(",")));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        cikkszamEgysegSp = (Spinner) view.findViewById(R.id.cikkszam_sp_unit);
        cikkszamEgysegSp.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, UNITS));
        cikkszamEgysegSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    mennyisegCikkszamEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,2)});
                } else {
                    mennyisegCikkszamEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
                    String mennyiseg = mennyisegCikkszamEt.getText().toString();
                    if (mennyiseg.contains(".")) {
                        mennyisegCikkszamEt.setText(mennyiseg.substring(0, mennyiseg.indexOf(".")));
                    } else if (mennyiseg.contains(",")) {
                        mennyisegCikkszamEt.setText(mennyiseg.substring(0, mennyiseg.indexOf(",")));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        szamlaEgysegSp = (Spinner) view.findViewById(R.id.szamla_sp_unit);
        szamlaEgysegSp.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, UNITS));
        szamlaEgysegSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    mennyisegSzamlaEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,2)});
                } else {
                    mennyisegSzamlaEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
                    String mennyiseg = mennyisegSzamlaEt.getText().toString();
                    if (mennyiseg.contains(".")) {
                        mennyisegSzamlaEt.setText(mennyiseg.substring(0, mennyiseg.indexOf(".")));
                    } else if (mennyiseg.contains(",")) {
                        mennyisegSzamlaEt.setText(mennyiseg.substring(0, mennyiseg.indexOf(",")));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mennyisegBizonylatEt = (EditText) view.findViewById(R.id.bizonylat_et_mennyiseg);
        mennyisegBizonylatEt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5,0)});
        rlBizonylatAdd = (RelativeLayout)view.findViewById(R.id.bizonylat_add_rl);
        rlSzamlaAdd = (RelativeLayout)view.findViewById(R.id.szamla_add_rl);
        mennyisegCikkszamEt = (EditText) view.findViewById(R.id.cikkszan_et_mennyiseg);
        mennyisegCikkszamEt.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,0)});
        mennyisegSzamlaEt = (EditText) view.findViewById(R.id.szamla_et_mennyiseg);
        mennyisegSzamlaEt.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,0)});
        rlCikkszamAdd = (RelativeLayout)view.findViewById(R.id.cikkszan_add_rl);

        autocompleteKeszletmozgasAdapter1 = new AutocompleteKeszletmozgasAdapter(getActivity(),null,true);
        autocompleteKeszletmozgasAdapter2 = new AutocompleteKeszletmozgasAdapter(getActivity(),null,false);
        autocompleteAlkatreszcikkszamAdapter = new AutocompleteAlkatreszcikkszamAdapter(getActivity(),null);
        autocompleteSzamlaAlkatreszcikkszamAdapter = new AutocompleteAlkatreszcikkszamAdapter(getActivity(),null);
        autocompleteBizAlkatreszcikkszamAdapter = new AutocompleteAlkatreszcikkszamAdapter(getActivity(),null);

        bizonylatEt.setAdapter(autocompleteKeszletmozgasAdapter1);
        szamlaszamEt.setAdapter(autocompleteKeszletmozgasAdapter2);
        cikkszamEt.setAdapter(autocompleteAlkatreszcikkszamAdapter);
        szamlaCikkszamEt.setAdapter(autocompleteSzamlaAlkatreszcikkszamAdapter);
        bizCikkszamEt.setAdapter(autocompleteBizAlkatreszcikkszamAdapter);

        szamlaCikkszamEt.setThreshold(2);
        bizCikkszamEt.setThreshold(2);
        cikkszamEt.setThreshold(2);

        autocompleteKeszletmozgasAdapter1.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorForBizonylat(str != null ? str.toString() : "");
            }
        });

        autocompleteKeszletmozgasAdapter2.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorForSzamlaszam(str != null ? str.toString() : "");
            }
        });

        autocompleteBizAlkatreszcikkszamAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence str) {
                return getCursorForCikkszam(str != null ? str.toString() : "");
            }
        });

        autocompleteSzamlaAlkatreszcikkszamAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence str) {
                return getCursorForCikkszam(str != null ? str.toString() : "");
            }
        });

        bizonylatEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bizonylatAdapter = null;
                bizonylatListView.setAdapter(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        bizonylatEt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeszletMozgas keszletMozgas = (KeszletMozgas) view.getTag();
                performSearch(true, keszletMozgas.bizonylatszam, null);
            }
        });

        szamlaszamEt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeszletMozgas keszletMozgas = (KeszletMozgas) view.getTag();
                performSearch(false, keszletMozgas.agazatiszam, keszletMozgas.szamlasorszam);
            }
        });

        szamlaszamEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                szamlaszamAdapter = null;
                szamlaszamListView.setAdapter(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cikkszamEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    tempSearchParamByCikkszam = s.toString();
                    searchByCikkszam();
                }
            }
        });

        hozzadBizonylatBtn = (Button) view.findViewById(R.id.bizonylat_btn_hozzaad);
        hozzadCikkszamBtn = (Button) view.findViewById(R.id.cikkszan_btn_hozzaad);
        hozzadSzamlaBtn = (Button) view.findViewById(R.id.szamla_btn_hozzaad);

        bizonylatListView = (ListView) view.findViewById(R.id.alkatreszek_listview_by_bizonylat);
        szamlaszamListView = (ListView) view.findViewById(R.id.alkatreszek_listview_by_cikktorzs);
        felhasznaltListView = (ListView) view.findViewById(R.id.felhasznalt_alkatreszek_listview);
        cikkszamListView = (ListView) view.findViewById(R.id.alkatreszek_listview_by_cikkszam);

        bizonylatPb = (ProgressBar) view.findViewById(R.id.bizonylat_progress_loading);
        szamlaszamPb = (ProgressBar) view.findViewById(R.id.szamlaszam_progress_loading);
        cikkszamPb = (ProgressBar) view.findViewById(R.id.cikkszam_progress_loading);

        hozzadBizonylatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempSearchByBizonylat = null;
                tempSearchParamByBizonylat = null;

                // KITE-524
                new AsyncTask<String,Void,String>(){
                    @Override
                    protected void onPreExecute() {
                        if(cikkszamPb != null){
                            cikkszamPb.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected String doInBackground(String... params) {
                        if (params.length == 0 || TextUtils.isEmpty(params[0])) return "";

                        Cursor cursor = null;
                        try {
                            cursor = orm.query(AlkatreszCikkszam.class, AlkatreszCikkszamokTable.COL_CIKKSZAM + " LIKE ?",
                                    new String[]{params[0]}, null);

                            if (cursor == null || !cursor.moveToFirst()) return "";

                            int columnIndex = cursor.getColumnIndexOrThrow(AlkatreszCikkszamokTable.COL_NEV);
                            return cursor.isNull(columnIndex) ? "" : cursor.getString(columnIndex);
                        } finally {
                            if (cursor != null) cursor.close();
                        }
                    }

                    @Override
                    protected void onPostExecute(String cikknev) {
                        if(cikkszamPb != null){
                            cikkszamPb.setVisibility(View.GONE);
                        }

                        saveAlkatresz(bizonylatEt.getText().toString(), bizCikkszamEt.getText().toString(), cikknev, mennyisegBizonylatEt.getText().toString(), (String)bizonylatEgysegSp.getSelectedItem(), null);
                    }
                }.execute(bizCikkszamEt.getText().toString());
            }
        });

        hozzadCikkszamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlkatresz("", cikkszamEt.getText().toString(), "", mennyisegCikkszamEt.getText().toString(), (String)cikkszamEgysegSp.getSelectedItem(), null);
            }
        });

        hozzadSzamlaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempSearchBySzamlaszam = null;
                tempSearchParamBySzamlaszam = null;
                tempSearchParam2BySzamlaszam = null;
                saveAlkatresz("", szamlaCikkszamEt.getText().toString(), "", mennyisegSzamlaEt.getText().toString(), (String)szamlaEgysegSp.getSelectedItem(), szamlaszamEt.getText().toString());
            }
        });

        szamlaszamEt.setFilters(new InputFilter[]{new SzamlaszamInputFilter()});

        new AsyncTask<Void, Void, List<Alkatresz>>(){
            @Override
            protected List<Alkatresz> doInBackground(Void... params) {
                List<Alkatresz> result = orm.listOrdered(Alkatresz.class, AlkatreszekTable.COL_MUNKALAPKOD + " = ? ", new String[]{ munkalap.getMunkalapKod() }, AlkatreszekTable.COL_ROGZITESDATUM + " desc");
                if (result == null || result.isEmpty() && munkalap.tempkod != null) {
                    result = orm.listOrdered(Alkatresz.class, AlkatreszekTable.COL_MUNKALAPKOD + " = ? ", new String[]{ munkalap.tempkod }, AlkatreszekTable.COL_ROGZITESDATUM + " desc");
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<Alkatresz> result) {
                Log.e(TAG, "munkalap.munkalapkodi5: " + munkalap.munkalapkod);
                munkalap.alkatreszek = result;
                loadFelhasznaltAlkatreszekAdapter();
            }
        }.execute();
        int mode = ((MunkalapFragmentInterface)getActivity()).getMode();
        if (mode == MunkalapActivity.MODE_VIEW || (mode == MunkalapActivity.MODE_OWN && !munkalap.isEditableFromOwnList())) {
            tabs.setVisibility(View.GONE);
            view.findViewById(R.id.view2).setVisibility(View.GONE);
            view.findViewById(R.id.dialog_alkatreszek_list_container).getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            text.setVisibility(View.GONE);
        }

        return view;
    }

    private void saveAlkatresz(String bizonylat, String cikkszam, String cikknev, String mennyiseg, String egyseg, String szamlaSzam) {
        boolean result = true;
        if (bizonylat.length() < 3 && bizonylat.length() > 0) {
            result = false;
            bizonylatEt.requestFocus();
        } else {
            bizonylatEt.setError(null);
        }
        if (szamlaSzam != null) {
            Pattern szamlaPattern = Pattern.compile("[a-zA-Z0-9áéöüóőúűíÁÉÖÜÓŐÚŰÍ]{2}/[a-zA-Z0-9áéöüóőúűíÁÉÖÜÓŐÚŰÍ]{5}");
            Matcher matcher = szamlaPattern.matcher(szamlaSzam);
            if (!matcher.matches()) {
                szamlaszamEt.setError(getString(R.string.dialog_alkatreszek_add_szamlaszam_error));
                if (result) szamlaszamEt.requestFocus();
                Toast.makeText(getActivity(), getString(R.string.dialog_alkatreszek_add_szamlaszam_error), Toast.LENGTH_SHORT).show();
                result = false;
            } else {
                szamlaszamEt.setError(null);
            }
        }
        if (cikkszam.length() == 0) {
            switch (tabs.getCurrentTab()) {
                case 0:
                    if (result) bizCikkszamEt.requestFocus();
                    bizCikkszamEt.setError(getString(R.string.dialog_alkatreszek_add_cikkszam_error));
                    break;
                case 1:
                    if (result) szamlaCikkszamEt.requestFocus();
                    szamlaCikkszamEt.setError(getString(R.string.dialog_alkatreszek_add_cikkszam_error));
                    break;
                default:
                    break;
            }
            result = false;
            Toast.makeText(getActivity(), getString(R.string.dialog_alkatreszek_add_cikkszam_error), Toast.LENGTH_SHORT).show();
        } else {
            bizCikkszamEt.setError(null);
            szamlaCikkszamEt.setError(null);
        }
        if (cikkszam.length() < 3) {
            switch (tabs.getCurrentTab()) {
                case 0:
                    if (result) bizCikkszamEt.requestFocus();
                    bizCikkszamEt.setError(getString(R.string.dialog_alkatreszek_add_error));
                    break;
                case 1:
                    if (result) szamlaCikkszamEt.requestFocus();
                    szamlaCikkszamEt.setError(getString(R.string.dialog_alkatreszek_add_error));
                    break;
                default:
                    break;
            }
            result = false;
            bizCikkszamEt.setError(getString(R.string.dialog_alkatreszek_add_error));
            Toast.makeText(getActivity(), getString(R.string.dialog_alkatreszek_add_error), Toast.LENGTH_SHORT).show();
        } else {
            bizCikkszamEt.setError(null);
            szamlaCikkszamEt.setError(null);
        }
        if (mennyiseg.length() < 1){
            switch (tabs.getCurrentTab()) {
                case 0:
                    if (result) mennyisegBizonylatEt.requestFocus();
                    mennyisegBizonylatEt.setError(getString(R.string.dialog_alkatreszek_add_mennyiseg_error));
                    break;
                case 1:
                    if (result) mennyisegSzamlaEt.requestFocus();
                    mennyisegSzamlaEt.setError(getString(R.string.dialog_alkatreszek_add_mennyiseg_error));
                    break;
                case 2:
                    if (result) mennyisegCikkszamEt.requestFocus();
                    mennyisegCikkszamEt.setError(getString(R.string.dialog_alkatreszek_add_mennyiseg_error));
                    break;
                default:
                    break;
            }
            result = false;
            Toast.makeText(getActivity(), getString(R.string.dialog_alkatreszek_add_mennyiseg_error), Toast.LENGTH_SHORT).show();
        } else {
            mennyisegBizonylatEt.setError(null);
            mennyisegCikkszamEt.setError(null);
            mennyisegSzamlaEt.setError(null);
        }
        if(mennyiseg.length() > 0 && Double.valueOf(mennyiseg) == 0) {
            ((MunkalapActivity) getActivity()).showErrorDialog(getActivity().getString(R.string.error_dialog_alkatresz_title), getActivity().getString(R.string.error_dialog_alkatresz_message));
            result = false;
        }

        if(result){
            addNewAlkatresz(bizonylat, cikkszam, cikknev, mennyiseg, egyseg, szamlaSzam);
            loadFelhasznaltAlkatreszekAdapter();
        }
    }

    private void searchByCikkszam(){
        new AsyncTask<String,Void,Cursor>(){

            @Override
            protected void onPreExecute() {
                if(cikkszamPb != null){
                    cikkszamPb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Cursor doInBackground(String... params) {
                if (tempSearchParamByCikkszam != null) {

                    return orm.query(AlkatreszCikkszam.class, AlkatreszCikkszamokTable.COL_STATUS + " = 'A' AND " + AlkatreszCikkszamokTable.COL_CIKKSZAM + " LIKE ? AND " +
                                    AlkatreszCikkszamokTable.COL_CIKKSZAM + " NOT IN (select alkatreszek.cikkszam from alkatreszek where alkatreszek.munkalapkod = ? AND " +
                                    "alkatreszek.bizonylatszam IS NULL AND " +
                                    "alkatreszek.agazatiszam IS NULL AND " +
                                    "alkatreszek.sorszam IS NULL)",
                            new String[]{"%" + tempSearchParamByCikkszam + "%", munkalap.getMunkalapKod()}
                            , AlkatreszCikkszamokTable.COL_CIKKSZAM + " asc");
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                if(cikkszamPb != null){
                    cikkszamPb.setVisibility(View.GONE);
                }
                if(cursor != null) {
                    if (alkatreszCikkszamAdapter == null) {
                        alkatreszCikkszamAdapter = new AlkatreszCikkszamAdapter(getActivity(), cursor, munkalap, AlkatreszekKezeleseDialog.this);
                        cikkszamListView.setAdapter(alkatreszCikkszamAdapter);
                    } else {
                        alkatreszCikkszamAdapter.changeCursor(cursor);
                    }
                }
                if(cursor == null || cursor.getCount() == 0){
                    rlCikkszamAdd.setVisibility(View.VISIBLE);
                    if(bizonylatAdapter != null){
                        bizonylatAdapter.changeCursor(null);
                    }
                } else {
                    rlCikkszamAdd.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private Cursor getCursorForBizonylat(String param) {
        if (param.length() > 2) {
            Cursor cursor = orm.query(KeszletMozgas.class, KeszletMozgasokTable.COL_BIZONYLATSZAM + " LIKE ? GROUP BY " + KeszletMozgasokTable.COL_BIZONYLATSZAM, new String[]{"%" + param + "%"}, KeszletMozgasokTable.COL_BIZONYLATSZAM + " asc");
            if(cursor != null && cursor.getCount() == 0){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        rlBizonylatAdd.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        rlBizonylatAdd.setVisibility(View.GONE);
                    }
                });
            }
            return cursor;
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    rlBizonylatAdd.setVisibility(View.VISIBLE);
                }
            });
            return null;
        }
    }

    private Cursor getCursorForSzamlaszam(String param){
        if(param.length()>3) {
            Cursor cursor = orm.query(KeszletMozgas.class, KeszletMozgasokTable.COL_AGAZATISZAM + " LIKE ? AND " + KeszletMozgasokTable.COL_SZAMLASORSZAM + " LIKE ? GROUP BY " + KeszletMozgasokTable.COL_AGAZATISZAM + ", " + KeszletMozgasokTable.COL_SZAMLASORSZAM,
                    new String[]{param.substring(0, 2) + "%", param.substring(3, param.length()) + "%"}, KeszletMozgasokTable.COL_AGAZATISZAM + " asc, " + KeszletMozgasokTable.COL_SZAMLASORSZAM + " asc");
            if(cursor != null && cursor.getCount() == 0){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        rlSzamlaAdd.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        rlSzamlaAdd.setVisibility(View.GONE);
                    }
                });
            }
            return cursor;
        } else {
            return null;
        }
    }

    private Cursor getCursorForCikkszam(String param){
        if(param.length()>3) {
            return orm.query(AlkatreszCikkszam.class,  AlkatreszCikkszamokTable.COL_CIKKSZAM + " LIKE ? ", new String[]{"%" + param + "%"}, AlkatreszCikkszamokTable.COL_CIKKSZAM + " asc");
        } else {
            return null;
        }
    }

    private void addNewAlkatresz(String bizonylat, String cikkszam, String cikknev, String mennyiseg, String egyseg, String szamlaSzam){

		Alkatresz uj = new Alkatresz();
        uj.cikknev = cikknev;
		uj.tempkod = IdGenerator.generate(getActivity()); //"C" + IdGenerator.generate(getActivity(), 5); KITE-870
        uj.cikkszam = cikkszam;
        uj.bizonylatszam = bizonylat;
        uj.rogzitesdatum = new Date();
        uj.munkalapkod = munkalap.getMunkalapKod();
        uj.munkalapsorszam = munkalap.munkalapsorszam;
        uj.modified = new Date();
        uj.mozgomennyiseg = Double.valueOf(mennyiseg);
        uj.mennyisegiegyseg = egyseg;
        uj.status = "A";
        uj.modified = new Date();

        if (szamlaSzam != null) {
            String[] szlaszam = szamlaSzam.split("/");
            if (szlaszam.length > 1) {
                uj.agazatiszam = szlaszam[0];
                uj.sorszam = szlaszam[1];
            }
        }
        if(munkalap.alkatreszek == null) {
            munkalap.alkatreszek = new ArrayList<Alkatresz>();
        }
        Log.i(TAG, uj.bizonylatszam + " alkatresz add to munkalap.");
        uj._id = ContentUris.parseId(orm.insert(uj));

        munkalap.alkatreszek.add(uj);
		munkalap.bizonylatszam = munkalap.getBizonylatSzamok();

        Toast.makeText(getActivity(), getString(R.string.dialog_alkatreszek_add, uj.bizonylatszam), Toast.LENGTH_SHORT).show();
        bizonylatEt.setText("");

		mennyisegSzamlaEt.setText("");
        szamlaCikkszamEt.setText("");
        szamlaszamEt.setText("");
        mennyisegCikkszamEt.setText("");
        cikkszamEt.setText("");
        bizCikkszamEt.setText("");
        mennyisegBizonylatEt.setText("");

        munkalap.beforeSave();
        orm.update(munkalap);
	}

    private void performSearch(final boolean searchByBizonylat, String searchParam, String searchParam2){
        if(searchByBizonylat) {
            tempSearchByBizonylat = true;
            tempSearchParamByBizonylat = searchParam;
        } else {
            tempSearchParamBySzamlaszam = searchParam;
            tempSearchParam2BySzamlaszam = searchParam2;
            tempSearchBySzamlaszam = false;
        }
        new AsyncTask<String, Void, Cursor>(){

            @Override
            protected void onPreExecute() {
                if(searchByBizonylat){
                    if(bizonylatPb != null){
                        bizonylatPb.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(szamlaszamPb != null){
                        szamlaszamPb.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            protected Cursor doInBackground(String... params) {
                String searchParam = params[0];
                String searchParam2 = params[1];
                return orm.query(KeszletMozgas.class,
                        (searchByBizonylat ? KeszletMozgasokTable.COL_BIZONYLATSZAM : (KeszletMozgasokTable.COL_AGAZATISZAM + " = ? AND " + KeszletMozgasokTable.COL_SZAMLASORSZAM)) + " = ? AND " +
                                KeszletMozgasokTable.COL_AZONOSITO + " NOT IN (select alkatreszek.mozgasazonosito from alkatreszek where alkatreszek.munkalapkod = ? and alkatreszek.mozgasazonosito is not null) AND " +
                                KeszletMozgasokTable.COL_CIKKSZAM + " NOT IN (select alkatreszek.cikkszam from alkatreszek where status = 'D' AND alkatreszek.cikkszam is not null AND alkatreszek." +
                                (searchByBizonylat ? "bizonylatszam = ?)" : "agazatiszam = ? AND alkatreszek.sorszam = ?)"),
                        searchByBizonylat ? new String[]{searchParam, munkalap.getMunkalapKod(), searchParam} : new String[]{searchParam, searchParam2, munkalap.getMunkalapKod(), searchParam, searchParam2},
                        KeszletMozgasokTable.COL_CIKKSZAM + " asc");
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                if(cursor != null){
                    if (searchByBizonylat){
                        if(bizonylatAdapter == null){
                            bizonylatAdapter = new KeszletMozgasAdapter(getActivity(), cursor, munkalap, AlkatreszekKezeleseDialog.this);
                            bizonylatListView.setAdapter(bizonylatAdapter);
                        } else {
                            bizonylatAdapter.changeCursor(cursor);
                        }
                    } else {
                        if(szamlaszamAdapter == null){
                            szamlaszamAdapter = new KeszletMozgasAdapter(getActivity(), cursor, munkalap, AlkatreszekKezeleseDialog.this);
                            szamlaszamListView.setAdapter(szamlaszamAdapter);
                        } else {
                            szamlaszamAdapter.changeCursor(cursor);
                        }
                    }
                }
                if(searchByBizonylat){
                    if(bizonylatPb != null){
                        bizonylatPb.setVisibility(View.GONE);
                    }
                    if(cursor != null && cursor.getCount() == 0){
                        rlBizonylatAdd.setVisibility(View.VISIBLE);
                    } else {
                        rlBizonylatAdd.setVisibility(View.GONE);
                    }
                } else {
                    if(szamlaszamPb != null){
                        szamlaszamPb.setVisibility(View.GONE);
                    }
                    if(cursor != null && cursor.getCount() == 0){
                        rlSzamlaAdd.setVisibility(View.VISIBLE);
                    } else {
                        rlSzamlaAdd.setVisibility(View.GONE);
                    }
                }
            }
        }.execute(searchParam, searchParam2);
    }

    public class AlkatreszComparator implements Comparator<Alkatresz> {
        @Override
        public int compare(Alkatresz o1, Alkatresz o2) {
            return o1.rogzitesdatum.compareTo(o2.rogzitesdatum);
        }
    }

    private void loadFelhasznaltAlkatreszekAdapter(){
        Log.i(TAG, "loadFelhasznaltAlkatreszekAdapter");
        if(munkalap == null){
            Log.e(TAG, "munkalap is null");
            return;
        }
        if(munkalap.alkatreszek == null || munkalap.alkatreszek.size() == 0){
            if (felhasznaltAlkatreszekAdapter != null) {
                felhasznaltAlkatreszekAdapter.notifyDataSetChanged();
            }
            return;
        }
        Collections.sort(munkalap.alkatreszek, Collections.reverseOrder(new AlkatreszComparator()));
        if(felhasznaltAlkatreszekAdapter == null){
            felhasznaltAlkatreszekAdapter = new AlkatreszekArrayAdapter(getActivity(), R.layout.list_item_alkatresz_remove, munkalap, AlkatreszekKezeleseDialog.this);
            felhasznaltListView.setAdapter(felhasznaltAlkatreszekAdapter);
        } else {
            felhasznaltAlkatreszekAdapter.setObjects(munkalap.alkatreszek);
            felhasznaltAlkatreszekAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void refresh(Munkalap munkalap) {
        this.munkalap = munkalap;
        loadFelhasznaltAlkatreszekAdapter();
        if(tempSearchByBizonylat != null && tempSearchParamByBizonylat != null){
            performSearch(true, tempSearchParamByBizonylat, null);
        }
        if(tempSearchBySzamlaszam != null && tempSearchParamBySzamlaszam != null){
            performSearch(false, tempSearchParamBySzamlaszam, tempSearchParam2BySzamlaszam);
        }
    }

    @Override
    public void onDestroy() {

        KiteORM.closeCursor(alkatreszCikkszamAdapter);
        KiteORM.closeCursor(bizonylatAdapter);
        KiteORM.closeCursor(szamlaszamAdapter);
        KiteORM.closeCursor(autocompleteAlkatreszcikkszamAdapter);
        KiteORM.closeCursor(autocompleteBizAlkatreszcikkszamAdapter);
        KiteORM.closeCursor(autocompleteKeszletmozgasAdapter1);
        KiteORM.closeCursor(autocompleteKeszletmozgasAdapter2);
        KiteORM.closeCursor(autocompleteSzamlaAlkatreszcikkszamAdapter);

        super.onDestroy();
    }
}
