package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.adapters.AutocompleteAlkatreszcikkszamAdapter;
import hu.itware.kite.service.enums.DialogType;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.ISaveable;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.AlkatreszCikkszamokTable;
import hu.itware.kite.service.orm.database.tables.AlkatreszekTable;
import hu.itware.kite.service.orm.database.tables.KeszletMozgasokTable;
import hu.itware.kite.service.orm.database.tables.MunkalapExportokTable;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import hu.itware.kite.service.orm.model.AlkatreszExport;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.MunkalapExport;
import hu.itware.kite.service.orm.utils.DateUtils;
import hu.itware.kite.service.orm.utils.GSON;
import hu.itware.kite.service.utils.Export;
import hu.itware.kite.service.utils.NoEnterInputFilter;
import hu.itware.kite.service.utils.SpinnerUtils;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.widget.DateTimePickerView;

public class MunkalapPage4Fragment extends Fragment implements IRefreshable, ISaveable, IDialogResult, RefreshMunkalapDialogInterface, Validator.ValidationListener {

    public static final String TAG = "KITE.MunkalapPage4";
    private MunkalapFragmentInterface mListener;
    private TextView mTvPartnerMachineSummary;
    private DateTimePickerView mDpReportDate;
    private DateTimePickerView mDpReportTime;
    private DateTimePickerView mDpMalfunctionDate;
    private AutoCompleteTextView mEtBrokenPart;
    private EditText mEtMalfunctionDescription;
    private EditText mEtMalfunctionReason;
    private EditText mEtTask2;
    @Required(order = 1, messageResId = R.string.error_required_field)
    private Spinner mSpSubunit;
    private FotoCsatolasDialog fotoCsatolasDialog;
    private Button mBtClose;
    private Button mBtAddMedia;
    private Button mBtAddPart;
    private Button mBtShowSummary;

    private Munkalap mMunkalap;

    public DialogType dialogType = DialogType.MAIN_MENU_DIALOG;

    private List<Alkatresz> originalAlkatreszList = new ArrayList<Alkatresz>();

    private KiteORM kiteORM;

    private Validator validator;

    private AutocompleteAlkatreszcikkszamAdapter autocompleteAlkatreszcikkszamAdapter;

    private AsyncTask<Void, Void, List<Alkatresz>> taskAlkatreszekList;

    public MunkalapPage4Fragment() {
        // Required empty public constructor
        Log.e(TAG, "MunkalapPage4Fragment()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_munkalap_page4, container, false);
        mTvPartnerMachineSummary = (TextView) view.findViewById(R.id.munkalap_tv_partner_machine_summary);
        mBtClose = (Button) view.findViewById(R.id.munkalap_btn_close);


        mBtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener.getMode() < MunkalapActivity.MODE_VIEW) {
                    validator.validate();
                } else if (mListener.getMode() == MunkalapActivity.MODE_OWN) {

                    //if (isMunkalapModified() && !((MunkalapActivity)getActivity()).isReadonly()) {
                    boolean needMunkalapExport = createAlkatreszExport();
                    if (needMunkalapExport) {
                        createMunkalapExport();
                    }
                    Export.copyImagesFromTempToUpload(mMunkalap, getActivity());
                    //}
                    mListener.previousPage();
                    mListener.previousPage();
                } else {
                    getActivity().finish();
                }
            }
        });
        mDpReportDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_report_date);
        mDpReportDate.setListener(new DateTimePickerView.OnDateChangedListener() {
            @Override
            public void onDateChanged(Calendar calendar) {
                if (mMunkalap.munkakezdesdatum != null) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Calendar workStartDate = Calendar.getInstance();
                    workStartDate.setTime(mMunkalap.munkakezdesdatum);


                    if (calendar.after(workStartDate)) {
                        ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_report_date_title),
                                getString(R.string.error_dialog_wrong_report_date_message,
                                        DateUtils.getDfShort().format(calendar.getTime()),
                                        DateUtils.getDfShort().format(mMunkalap.munkakezdesdatum)));
                    }
                }
            }
        });

        mDpReportTime = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_report_time);
        mDpMalfunctionDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_malfunction_date);
        mDpMalfunctionDate.setListener(new DateTimePickerView.OnDateChangedListener() {
            @Override
            public void onDateChanged(Calendar calendar) {
                if (mDpReportDate.getDate() != null) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Calendar reportDate = Calendar.getInstance();
                    reportDate.setTime(mDpReportDate.getDate());
                    if (calendar.after(reportDate)) {
                        ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_malfunction_date_title), getString(R.string.error_dialog_wrong_malfunction_date_message));
                    }
                }
            }
        });

        mEtBrokenPart = (AutoCompleteTextView) view.findViewById(R.id.munkalap_et_broken_part);
        autocompleteAlkatreszcikkszamAdapter = new AutocompleteAlkatreszcikkszamAdapter(getActivity(), null);
        autocompleteAlkatreszcikkszamAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return getCursorForCikkszam(charSequence == null ? null : charSequence.toString());
            }
        });
        mEtBrokenPart.setAdapter(autocompleteAlkatreszcikkszamAdapter);
        mEtBrokenPart.setThreshold(2);

        mEtMalfunctionDescription = (EditText) view.findViewById(R.id.munkalap_et_malfunction_description);
        mEtMalfunctionDescription.setFilters(StringUtils.addInputFilterToArray(mEtMalfunctionDescription.getFilters(), new NoEnterInputFilter()));
        mEtMalfunctionReason = (EditText) view.findViewById(R.id.munkalap_et_malfunction_reason);
        mEtMalfunctionReason.setFilters(StringUtils.addInputFilterToArray(mEtMalfunctionReason.getFilters(), new NoEnterInputFilter()));
        mEtTask2 = (EditText) view.findViewById(R.id.munkalap_et_task_2);
        mEtTask2.setFilters(StringUtils.addInputFilterToArray(mEtTask2.getFilters(), new NoEnterInputFilter()));
        mSpSubunit = (Spinner) view.findViewById(R.id.munkalap_sp_subunit);

        mSpSubunit.setAdapter(SpinnerUtils.createSpinnerAdapter(getActivity(), "REGK", "K2", null));

        mBtAddPart = (Button) view.findViewById(R.id.munkalap_btn_add_part);
        mBtAddPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mMunkalap = ((MunkalapActivity) getActivity()).getMunkalap();
                BaseActivity activity = (BaseActivity) getActivity();
                if (!activity.hasUzletkotoAzon()) {
                    activity.showUzletkotoAzonErrorDialog();
                    return;
                }

                if (mMunkalap != null && (mMunkalap.javitasdatum != null || mListener.getMode() >= MunkalapActivity.MODE_VIEW)) { // MODE_VIEW, MODE_OWN
                    addPart();
                } else {
                    ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_no_end_date_title), getString(R.string.error_dialog_no_end_date_message));
                }
            }
        });
        mBtAddMedia = (Button) view.findViewById(R.id.munkalap_btn_add_media);
        mBtAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMedia();
            }
        });
        mBtShowSummary = (Button) view.findViewById(R.id.munkalap_btn_show_summary);
        mBtShowSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getChildFragmentManager();
                MunkalapSummaryDialog dialog = new MunkalapSummaryDialog();
                dialog.setMunkalap(mListener.getMunkalap());
                //dialog.setListener(this);

                Bundle params = new Bundle();

                dialog.setArguments(params);
                dialog.show(fm, "fragment_dialog_summary");
            }
        });
        kiteORM = new KiteORM(getActivity());
        loadAlkatreszekList();

        validator = new Validator(this);
        validator.setValidationListener(this);
        Rule dateTimeRule = new Rule<DateTimePickerView>(getString(R.string.error_required_field)) {
            @Override
            public boolean isValid(DateTimePickerView o) {
                return o.getDate() != null;
            }
        };
        validator.put(mDpReportDate, dateTimeRule);
        validator.put(mDpReportTime, dateTimeRule);
        validator.put(mDpMalfunctionDate, dateTimeRule);
        refresh();
        return view;
    }


    /**
     * Check if the Munkalap was modified or not
     *
     * @return
     */
    private boolean isMunkalapModified() {
        mMunkalap = ((MunkalapActivity) getContext()).getMunkalap();
        String mMunkalapOld = "";
        if (getContext() != null && getContext() instanceof MunkalapActivity) {
            mMunkalapOld = ((MunkalapActivity) getContext()).getMunkalapOldJson();
        } else if (getActivity() != null && getActivity() instanceof MunkalapActivity) {
            mMunkalapOld = ((MunkalapActivity) getActivity()).getMunkalapOldJson();
        } else {
            return false;
        }
        if (mMunkalap != null && mMunkalapOld != null) {
            boolean equals = mMunkalapOld.equals(GSON.toJson(mMunkalap));
            Log.e(TAG, "isMunkalapModified().modified=" + !equals);
            return !equals;
        }

        return false;
    }

    private void addPart() {

        originalAlkatreszList.clear();
        originalAlkatreszList.addAll(mMunkalap.alkatreszek);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        AlkatreszekKezeleseDialog alkatreszekKezeleseDialog = new AlkatreszekKezeleseDialog();

        Bundle params = new Bundle();
        params.putString("title", getString(R.string.munkalap_alkatresz_dialog_title));
        params.putString("message", getString(R.string.munkalap_alkatresz_dialog_message));
        if (((MunkalapActivity) getActivity()).getMunkalap() != null) {
            mMunkalap = ((MunkalapActivity) getActivity()).getMunkalap();
        }
        if (mMunkalap != null) {
            params.putSerializable(AlkatreszekKezeleseDialog.EXTRA_MUNKALAP, mMunkalap);
        } else {
            Log.e(TAG, "mMunkalap is null");
        }
        alkatreszekKezeleseDialog.setArguments(params);
        alkatreszekKezeleseDialog.show(fm, "fragment_dialog_alkatresz_kezeles");
        dialogType = DialogType.ALKATRESZ_HOZZAADAS_DIALOG;
        alkatreszekKezeleseDialog.setListener(this);
    }

    private void addMedia() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fotoCsatolasDialog = new FotoCsatolasDialog();

        Bundle params = new Bundle();
        params.putString("title", getString(R.string.munkalap_photo_dialog_title));
        params.putString("message", getString(R.string.munkalap_photo_dialog_message));
        mMunkalap = ((MunkalapActivity) getActivity()).getMunkalap();
        if (mMunkalap != null) {
            params.putSerializable(AlkatreszekKezeleseDialog.EXTRA_MUNKALAP, mMunkalap);
        } else {
            Log.e(TAG, "mMunkalap is null");
        }
        fotoCsatolasDialog.setArguments(params);
        fotoCsatolasDialog.show(fm, "fragment_dialog_foto_kezeles");
    }

    public void saveData() {

        Log.e(TAG, "page3.saveData().isMunkalapModified()=" + isMunkalapModified());
        if (mDpReportDate.getDate() != null && mDpReportTime.getDate() != null) {
            Calendar reportDate = Calendar.getInstance();
            reportDate.setTime(mDpReportDate.getDate());
            Calendar reportTime = Calendar.getInstance();
            reportTime.setTime(mDpReportTime.getDate());
            reportDate.set(Calendar.HOUR_OF_DAY, reportTime.get(Calendar.HOUR_OF_DAY));
            reportDate.set(Calendar.MINUTE, reportTime.get(Calendar.MINUTE));
            reportDate.set(Calendar.SECOND, 0);
            reportDate.set(Calendar.MILLISECOND, 0);
            mMunkalap.bejelentesdatum = reportDate.getTime();
        }
        mMunkalap.meghibasodasdatum = mDpMalfunctionDate.getDate();
        mMunkalap.cikkszam = mEtBrokenPart.getText().toString();
        mMunkalap.hibajelenseg = mEtMalfunctionDescription.getText().toString();
        mMunkalap.hibajelensegoka = mEtMalfunctionReason.getText().toString();
        mMunkalap.tevekenyseg2 = mEtTask2.getText().toString();
        mMunkalap.status = "A";

        String subunit = SpinnerUtils.getText(mSpSubunit);
        if (subunit != null) {
            if (subunit.length() > 1) {
                mMunkalap.hibakod = subunit.substring(0, 2);
            } else {
                mMunkalap.hibakod = subunit;
            }
        }

        MunkalapActivity activity = (MunkalapActivity) getActivity();
        if ("4".equals(mMunkalap.allapotkod) && isMunkalapModified() && !activity.isReadonly()) {
            boolean needMunkalapExport = createAlkatreszExport(); // alkatreszeket csak akkor kell exportaljuk, ha lezartuk a munkalapot
            boolean munkalapNeverExported = checkMunkalapNeverExported(mMunkalap);
            boolean munkalapModified = isMunkalapModified();

            Log.i(TAG, "saveData().needMunkalapExport=" + needMunkalapExport);
            Log.i(TAG, "saveData().munkalapNeverExported=" + munkalapNeverExported);
            Log.i(TAG, "saveData().munkalapModified=" + munkalapModified);
            Log.e(TAG, "saveData().munkalapExport=" + (munkalapNeverExported || needMunkalapExport || munkalapModified));

            if (munkalapNeverExported || needMunkalapExport || munkalapModified) {
                createMunkalapExport();
            } else {
                Log.i(TAG, "MunkalapExport skipped.");
            }
        }

        kiteORM.update(mMunkalap);
    }

    private boolean checkMunkalapNeverExported(Munkalap munkalap) {

        Map<String, String> values = new LinkedHashMap<>();
        if (munkalap.munkalapkod != null) {
            values.put(MunkalapExportokTable.COL_GE, munkalap.munkalapkod);
        }
        if (munkalap.elozmenykod != null) {
            values.put(MunkalapExportokTable.COL_EGE, munkalap.elozmenykod);
        }
        if (munkalap.tempkod != null) {
            values.put(MunkalapExportokTable.COL_GEW, munkalap.tempkod);
        }
        if (munkalap.munkalapsorszam != null) {
            values.put(MunkalapExportokTable.COL_GETC2, munkalap.munkalapsorszam);
        }
        if (munkalap.tipushosszunev != null) {
            values.put(MunkalapExportokTable.COL_GNEV, munkalap.tipushosszunev);
        }
        if (munkalap.geptipus != null) {
            values.put(MunkalapExportokTable.COL_GNEV1, munkalap.geptipus);
        }
        if (munkalap.gepnev != null) {
            values.put(MunkalapExportokTable.COL_GNEV2, munkalap.gepnev);
        }

        StringBuffer query = new StringBuffer();
        int andCount = values.size() - 1;
        for (String queryColumn : values.keySet()) {
            String queryValue = values.get(queryColumn);
            query.append(queryColumn + "='" + queryValue + "'");
            if (andCount > 0) {
                query.append(" AND ");
                andCount--;
            }
        }
        Log.i(TAG, "checkMunkalapNeverExported().query=" + query);
        int exportedCount = kiteORM.getCount(MunkalapExport.class, query.toString(), null);
        Log.i(TAG, "checkMunkalapNeverExported().exportedCount=" + exportedCount);
        return exportedCount == 0;
    }

    private void createMunkalapExport() {
        Log.i(TAG, "createMunkalapExport()=" + mMunkalap.munkalapsorszam);
        MunkalapExport export = Export.createMunkalap4Per4Export(getActivity(), mMunkalap, mMunkalap.getGep());
        kiteORM.insert(export);

        Log.i(TAG, "MunkalapExport.count=" + kiteORM.getCount(MunkalapExportokTable.TABLE_NAME));
        Log.d(TAG, "Munkalap EXPORT saved.");
    }

    private boolean createAlkatreszExport() {
        Log.i(TAG, "createAlkatreszExport()");
        boolean needMunkalapExport = false;
        if (mMunkalap != null) {
            for (Alkatresz alkatresz : mMunkalap.alkatreszek) {
                if (!findAlkatreszInMunkalap(originalAlkatreszList, alkatresz)) {

                    if (!StringUtils.isEmpty(alkatresz.bizonylatszam)) {
                        Log.i(TAG, "MunkalapExport=true! Alkatresz hozzaadva bizonylatszammal:" + alkatresz);
                        needMunkalapExport = true;
                    }
                    if (!StringUtils.isEmpty(alkatresz.agazatiszam) && !StringUtils.isEmpty(alkatresz.sorszam)) {
                        Log.i(TAG, "MunkalapExport=true! Alkatresz hozzaadva szamlaszammal:" + alkatresz.agazatiszam + "/" + alkatresz.sorszam);
                        needMunkalapExport = true;
                    }

                    KeszletMozgas mozgas = null;
                    if (alkatresz.mozgasazonosito != null && !alkatresz.mozgasazonosito.isEmpty()) {
                        mozgas = kiteORM.loadSingle(KeszletMozgas.class, KeszletMozgasokTable.COL_AZONOSITO + " = ?", new String[]{alkatresz.mozgasazonosito});
                    }
                    AlkatreszExport export = Export.createAlkatreszExport(getActivity(), mMunkalap, alkatresz, mozgas);
                    kiteORM.insert(export);
                }
            }
        }
        Log.i(TAG, "createAlkatreszExport().needMunkalapExport=" + needMunkalapExport);
        return needMunkalapExport;
    }

    private boolean findAlkatreszInMunkalap(List<Alkatresz> alkatreszek, Alkatresz alkatresz) {
        if (alkatreszek == null || alkatresz == null) {
            return false;
        }

        for (Alkatresz a : alkatreszek) {
            if (a != null && a.equals(alkatresz)) {
                Log.i(TAG, "equals=" + GSON.toJson(a) + "\n" + GSON.toJson(alkatresz));
                return true;
            }
        }

        return false;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (MunkalapFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MunkalapFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (taskAlkatreszekList != null && !taskAlkatreszekList.isCancelled()) {
            taskAlkatreszekList.cancel(true);
        }
    }

    private void disableFieldsForCopiedRecord() {
//        if(!StringUtils.isEmpty(mDpReportDate.getDateTimeString())) {
//            mDpReportDate.setEnabled(false);
//        } else {
        mDpReportDate.setEnabled(true);
//        }
//        if(!StringUtils.isEmpty(mDpReportTime.getDateTimeString())) {
//            mDpReportTime.setEnabled(false);
//        } else {
        mDpReportTime.setEnabled(true);
//        }
//        if(!StringUtils.isEmpty(mDpMalfunctionDate.getDateTimeString())) {
//            mDpMalfunctionDate.setEnabled(false);
//        } else {
        mDpMalfunctionDate.setEnabled(true);
//        }
    }

    private void loadAlkatreszekList() {
        taskAlkatreszekList = new AsyncTask<Void, Void, List<Alkatresz>>() {
            @Override
            protected List<Alkatresz> doInBackground(Void... params) {
                final Munkalap munkalap = mMunkalap;
                if (munkalap != null && !isCancelled()) {
                    List<Alkatresz> result = kiteORM.listOrdered(Alkatresz.class, AlkatreszekTable.COL_MUNKALAPKOD + " = ? ", new String[]{munkalap.getMunkalapKod()}, AlkatreszekTable.COL_ROGZITESDATUM + " desc");
                    if (result == null || result.isEmpty() && munkalap.tempkod != null) {
                        result = kiteORM.listOrdered(Alkatresz.class, AlkatreszekTable.COL_MUNKALAPKOD + " = ? ", new String[]{munkalap.tempkod}, AlkatreszekTable.COL_ROGZITESDATUM + " desc");
                    }
                    return result;
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Alkatresz> result) {
                final Munkalap munkalap = mMunkalap;
                if (munkalap != null && result != null && !isCancelled()) {
                    Log.e(TAG, "munkalap.alkatreszek=" + result);
                    munkalap.alkatreszek = result;
                }
            }
        };
        taskAlkatreszekList.execute();
    }

    @Override
    public void refresh() {
        if (mListener != null) {
            mMunkalap = mListener.getMunkalap();
            if (mMunkalap != null) {
                mTvPartnerMachineSummary.setText(mMunkalap.getPartnerSummary());
                if (mListener.getMode() == MunkalapActivity.MODE_OWN) {
                    originalAlkatreszList.clear();
                    originalAlkatreszList.addAll(mMunkalap.alkatreszek);
                }

                mDpReportDate.setDate(null);
                mDpReportTime.setTime(null);
                mDpMalfunctionDate.setDate(null);
                mEtBrokenPart.setText("");
                mEtMalfunctionDescription.setText("");
                mEtMalfunctionReason.setText("");
                mEtTask2.setText("");
            }
            if (mMunkalap != null && (mListener.getMode() < MunkalapActivity.MODE_VIEW || (mListener.getMode() >= MunkalapActivity.MODE_VIEW && "34".contains(mMunkalap.allapotkod != null ? mMunkalap.allapotkod : "")))) {
                mDpReportDate.setDate(mMunkalap.bejelentesdatum);
                mDpReportTime.setTime(mMunkalap.bejelentesdatum);
                mDpMalfunctionDate.setDate(mMunkalap.meghibasodasdatum);
                mEtBrokenPart.setText(mMunkalap.cikkszam);
                mEtMalfunctionDescription.setText(mMunkalap.hibajelenseg);
                mEtMalfunctionReason.setText(mMunkalap.hibajelensegoka);
                mEtTask2.setText(mMunkalap.tevekenyseg2);

                if (mListener.getMode() < MunkalapActivity.MODE_VIEW) {
                    SpinnerUtils.selectAtStart(mSpSubunit, mMunkalap.hibakod);
                } else {
                    SpinnerUtils.createSimpleSpinner(getActivity(), mSpSubunit, mMunkalap.hibakod != null ? mMunkalap.hibakod : "-");
                }
//                public static final int MODE_CREATE_NEW = 0;    // Uj munkalap
//                public static final int MODE_CONTINUE = 1;      // Nem befejezett munka folytatasa
//                public static final int MODE_OPEN = 2;          // Nyitott munkalap folytatasa
//                public static final int MODE_COPY = 3;          // Nem befejezett munka uj munkalapon
//                public static final int MODE_VIEW = 4;          // Munkalap reszletei
//                public static final int MODE_OWN = 5;
                switch (mListener.getMode()) {
                    case MunkalapActivity.MODE_CONTINUE:
                        if (!StringUtils.isEmpty(mDpReportDate.getDateTimeString())) {
                            mDpReportDate.setEnabled(false);
                        } else {
                            mDpReportDate.setEnabled(true);
                        }
                        if (!StringUtils.isEmpty(mDpReportTime.getDateTimeString())) {
                            mDpReportTime.setEnabled(false);
                        } else {
                            mDpReportTime.setEnabled(true);
                        }
                        if (!StringUtils.isEmpty(mDpMalfunctionDate.getDateTimeString())) {
                            mDpMalfunctionDate.setEnabled(false);
                        } else {
                            mDpMalfunctionDate.setEnabled(true);
                        }
                        break;
                    case MunkalapActivity.MODE_COPY:
                        mDpReportDate.setEnabled(false);
                        mDpReportTime.setEnabled(false);
                        mDpMalfunctionDate.setEnabled(false);
                        break;
                }
            }


            switch (mListener.getMode()) {
                case MunkalapActivity.MODE_OWN:
                case MunkalapActivity.MODE_VIEW:
                    mBtClose.setText(getString(R.string.label_exit));
                    mTvPartnerMachineSummary.setEnabled(false);
                    mDpReportDate.setEnabled(false);
                    mDpReportTime.setEnabled(false);
                    mDpMalfunctionDate.setEnabled(false);
                    mEtBrokenPart.setEnabled(false);
                    mEtMalfunctionDescription.setEnabled(false);
                    mEtMalfunctionReason.setEnabled(false);
                    mEtTask2.setEnabled(false);
                    mSpSubunit.setEnabled(false);
                    break;
                default:
                    //mBtClose.setText(getString(R.string.label_exit));
//                    mTvPartnerMachineSummary.setEnabled(true);
//                    mDpReportDate.setEnabled(true);
//                    mDpReportTime.setEnabled(true);
//                    mDpMalfunctionDate.setEnabled(true);
//                    mEtBrokenPart.setEnabled(true);
//                    mEtMalfunctionDescription.setEnabled(true);
//                    mEtMalfunctionReason.setEnabled(true);
//                    mEtTask2.setEnabled(true);
//                    mSpSubunit.setEnabled(true);
                    break;
            }
        }
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {
        switch (dialogType) {
            case ALKATRESZ_HOZZAADAS_DIALOG:
                break;
            default:
                break;
        }
    }

    @Override
    public void onCancelClicked(DialogFragment dialog) {
        switch (dialogType) {
            case ALKATRESZ_HOZZAADAS_DIALOG:
                break;
            default:
                break;
        }
    }

    private Cursor getCursorForCikkszam(String param) {
        if (param != null && param.length() > 3) {
            return kiteORM.query(AlkatreszCikkszam.class, AlkatreszCikkszamokTable.COL_CIKKSZAM + " LIKE ? ", new String[]{"%" + param + "%"}, AlkatreszCikkszamokTable.COL_CIKKSZAM + " asc");
        } else {
            return null;
        }
    }

    @Override
    public void refresh(Munkalap munkalap) {
        // not used for now
    }

    @Override
    public void onValidationSucceeded() {
        if (validate()) {
            mMunkalap.allapotkod = "4";
            mMunkalap.lezarasdatum = new Date();
            saveData();
            getActivity().finish();
        }
    }

    @Override
    public void onValidationFailed(View view, Rule<?> rule) {
        if (view instanceof DateTimePickerView) {
            ((DateTimePickerView) view).setError(rule.getFailureMessage());
        }
    }

    private boolean validate() {

        if (null == mMunkalap.munkakezdesdatum) {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_work_date_title), getString(R.string.error_dialog_wrong_work_date_message_empty));
            return false;
        }

        if (mDpMalfunctionDate.getDate() == null) {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_malfunction_date_title), getString(R.string.error_dialog_wrong_malfunction_date_message_empty));
            return false;
        }

        if (mDpReportDate.getDate() == null) {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_report_date_title), getString(R.string.error_dialog_wrong_report_date_message_empty));
            return false;
        }

        Calendar calendar = mDpReportDate.getCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar workStartDate = Calendar.getInstance();
        workStartDate.setTime(mMunkalap.munkakezdesdatum);
        Log.e(TAG, "calendar= " + calendar.getTime() + ", munkakezdesdatum=" + workStartDate.getTime());
        if (calendar.after(workStartDate)) {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_report_date_title),
                    getString(R.string.error_dialog_wrong_report_date_message,
                            DateUtils.getDfShort().format(mDpReportDate.getDate()),
                            DateUtils.getDfShort().format(mMunkalap.munkakezdesdatum)));
            return false;
        } else {
            if (mDpMalfunctionDate.getDate() == null) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_malfunction_date_title), getString(R.string.error_dialog_wrong_malfunction_date_message_empty));
                return false;
            }

            calendar = mDpMalfunctionDate.getCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Calendar reportDate = Calendar.getInstance();

            if (mDpReportDate.getDate() == null) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_report_date_title), getString(R.string.error_dialog_wrong_report_date_message_empty));
                return false;
            }
            reportDate.setTime(mDpReportDate.getDate());
            if (calendar.after(reportDate)) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_malfunction_date_title), getString(R.string.error_dialog_wrong_malfunction_date_message));
                return false;
            }
        }

        MetaData su = (MetaData) mSpSubunit.getSelectedItem();
        if (su != null && su.text != null) {
            if (su.text.length() < 2) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_subunit_title), getString(R.string.error_dialog_wrong_subunit_message));
                return false;
            }
        } else {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_subunit_title), getString(R.string.error_dialog_wrong_subunit_message));
            return false;
        }

        return true;
    }


    @Override
    public void onPause() {
        Log.e(TAG, "onPause()");
        KiteORM.closeCursor(autocompleteAlkatreszcikkszamAdapter);
        super.onPause();
    }
}
