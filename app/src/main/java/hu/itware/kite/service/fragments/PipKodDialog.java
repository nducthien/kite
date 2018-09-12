package hu.itware.kite.service.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.itware.kite.service.R;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.MetaDataTable;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;

/**
 * Created by gyongyosit on 2015.11.06..
 */
public class PipKodDialog extends DialogFragment implements RefreshMunkalapDialogInterface {

    public static final String TAG = "Pipkod";
    private static final int DEFAULT_FILTER_DAYS = 3;
    IDialogResult listener;

    private ArrayAdapter<String> pipkodAdapter;
    private Munkalap munkalap;
    private ListView listView;
    private EditText filterEditText;
    private List<String> pipkodList = new ArrayList<>();

    public PipKodDialog() {
        this.setCancelable(true);
    }

    public void setListener(IDialogResult listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pip_kod, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString("title"));

        munkalap = (Munkalap)getArguments().getSerializable(AlkatreszekKezeleseDialog.EXTRA_MUNKALAP);

        listView = (ListView)view.findViewById(R.id.dialog_pip_kod_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                munkalap.pipkod = pipkodAdapter.getItem(i).split(":")[0];
                if (listener != null) {
                    listener.onOkClicked(PipKodDialog.this);
                }
                dismiss();
            }
        });

        view.findViewById(R.id.dialog_button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onCancelClicked(PipKodDialog.this);
                }
                dismiss();
            }
        });

        filterEditText = (EditText) view.findViewById(R.id.dialog_pip_kod_filter);
        filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterPipkodList();
            }
        });

        view.findViewById(R.id.pip_kod_delete_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterEditText.setText("");
                filterPipkodList();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter(munkalap);
    }

    public void setAdapter(Munkalap munkalap){
        if(listView == null || munkalap == null){
            return;
        }
        if(pipkodAdapter == null){
            KiteORM kiteORM = new KiteORM(getActivity());
            List<MetaData> pipMetaList = kiteORM.listWithoutFilterD(MetaData.class, MetaDataTable.COL_ID + " = ? AND " + MetaDataTable.COL_TYPE + " = ? AND " + MetaDataTable.COL_STATUS + " = ?", new String[]{"PIP", "K2", "A"});
            pipkodList = new ArrayList<String>();

            for (MetaData pipMeta : pipMetaList) {
                // Ha van datum a szovegben, akkor csak akkor kell megjeleniteni, ha a datum + konfigbol jovo nap kesobbi, mint a mai datum
                Pattern pattern = Pattern.compile(".*((19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])).*");
                Matcher matcher = pattern.matcher(pipMeta.text);
                if (matcher.matches()) {
                    String dateString = matcher.group(1);
                    char separator = dateString.charAt(4);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + separator + "MM" +separator + "dd");
                    try {
                        Konfig konfig = KiteDAO.getKonfig(getActivity(), "PKNSZ");
                        int days = DEFAULT_FILTER_DAYS;
                        if (konfig != null) {
                            try {
                                days = Integer.parseInt(konfig.value);
                            } catch (NumberFormatException e) {
                                days = DEFAULT_FILTER_DAYS;
                            }
                        }
                        Date date = sdf.parse(dateString);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.add(Calendar.DAY_OF_YEAR, days);
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);

                        Calendar now = Calendar.getInstance();
                        now.setTime(new Date());

                        if (calendar.after(now)) {
                            pipkodList.add(pipMeta.text);
                        }
                    } catch (ParseException e) {
                        pipkodList.add(pipMeta.text);
                    }
                } else {
                    pipkodList.add(pipMeta.text);
                }
            }
            pipkodAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(pipkodList));
            listView.setAdapter(pipkodAdapter);
        }
    }

    private void filterPipkodList() {
        pipkodAdapter.clear();
        for (String kod : pipkodList) {
            if (kod.toLowerCase().contains(filterEditText.getText().toString().toLowerCase())) {
                pipkodAdapter.add(kod);
            }
        }
        pipkodAdapter.notifyDataSetChanged();
    }

    @Override
    public void refresh(Munkalap munkalap) {
        this.munkalap = munkalap;
        setAdapter(munkalap);
    }
}
