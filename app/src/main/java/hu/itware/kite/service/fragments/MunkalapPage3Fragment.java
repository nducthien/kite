package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.Select;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.ISaveable;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.utils.DateUtils;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.services.PipUpdateService;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.AutoCompleteData;
import hu.itware.kite.service.utils.DecimalDigitsInputFilter;
import hu.itware.kite.service.utils.NoEnterInputFilter;
import hu.itware.kite.service.utils.NumberUtils;
import hu.itware.kite.service.utils.PipValidator;
import hu.itware.kite.service.utils.SpinnerUtils;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.widget.DateTimePickerView;
import hu.itware.kite.service.widget.DoubleCheckBoxView;

public class MunkalapPage3Fragment extends Fragment implements IRefreshable, ISaveable, Validator.ValidationListener {

    public static final String TAG = "KITE.MunkalapPage3";

    private Validator validator;

    private TextView mEtPartnerMachineSummary;
    @Select(order = 1, messageResId = R.string.error_required_field)
    private Spinner mSpWorkType1;
    @Select(order = 2, messageResId = R.string.error_required_field)
    private Spinner mSpWorkType2;
    @Required(order = 3, messageResId = R.string.error_required_field)
    private DateTimePickerView mDpWorkDate;
    @Required(order = 4, messageResId = R.string.error_required_field)
    private DateTimePickerView mDpWorkStart;
    @Required(order = 5, messageResId = R.string.error_required_field)
    private AutoCompleteTextView mEtWorkPlace;
    @Required(order = 6, messageResId = R.string.error_required_field)

    @Select(order = 7, messageResId = R.string.error_required_field)
    private Spinner mSpWorkPlaceType;
    @Required(order = 8, messageResId = R.string.error_required_field)
    private DateTimePickerView mDpArrivalTime;
    @Required(order = 9, messageResId = R.string.error_required_field)
    private DateTimePickerView mDpDepartureTime;
    @Select(order = 10, messageResId = R.string.error_required_field)
    private Spinner mSpTask;
    private EditText mEtTaskDescription;
    private EditText mEtWorkTime;
    //@Required(order = 11, messageResId = R.string.error_required_field)
    private EditText mEtPipCode;
    @Required(order = 12, messageResId = R.string.error_required_field)
    private DoubleCheckBoxView mDcbUrgent;
    private EditText mEtMachinehour;
    private EditText mEtThroughput;
    @Required(order = 13, messageResId = R.string.error_required_field)
    private DoubleCheckBoxView mDcbWorkDone;
    private DateTimePickerView mDpWorkDoneDate;
    private EditText mEtServicebookPage;
    @Required(order = 14, messageResId = R.string.error_required_field)
    private EditText mEtServicecarDistance;
    @Required(order = 15, messageResId = R.string.error_required_field)
    private DoubleCheckBoxView mDcbMachineOperational;
    @Required(order = 16, messageResId = R.string.error_required_field)
    private DoubleCheckBoxView mDcbCustomerComments;
    private Button mBtAddMedia;

    private EditText mEtCustomerComments2;

    private Munkalap mMunkalap;

    private MunkalapFragmentInterface mListener;

    private KiteORM kiteORM;

    private PipKodDialog pipKodDialog;

    private FotoCsatolasDialog fotoCsatolasDialog;

    private ArrayAdapter<String> citiesAdapter;

    private PipValidator pipValidator;
    private String status = null;
    private String keyAlvazszam;

    public MunkalapPage3Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pipValidator = new PipValidator(getActivity());
        kiteORM = new KiteORM(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_munkalap_page3, container, false);

        mEtPartnerMachineSummary = (TextView) view.findViewById(R.id.munkalap_tv_partner_machine_summary);
        view.findViewById(R.id.munkalap_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener.getCurrentPage().equals(MunkalapPage3Fragment.class)) {
                    if (mListener.getMode() < MunkalapActivity.MODE_VIEW) {
                        validator.validate();
                    } else {
                        //mListener.goToPage(MunkalapPage4Fragment.class);
                        mListener.nextPage();
                    }
                }
            }
        });
        mSpWorkType1 = (Spinner) view.findViewById(R.id.munkalap_sp_work_type_1);

        mSpWorkType2 = (Spinner) view.findViewById(R.id.munkalap_et_work_type_2);
        mDpWorkDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_work_date);
        mDpWorkStart = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_work_start);
        mEtWorkPlace = (AutoCompleteTextView) view.findViewById(R.id.munkalap_et_work_place);
        citiesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, AutoCompleteData.CITIES);
        mEtWorkPlace.setAdapter(citiesAdapter);
        mSpWorkPlaceType = (Spinner) view.findViewById(R.id.munkalap_et_work_place_type);
        mDpArrivalTime = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_arrival_time);
        mDpDepartureTime = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_departure_time);
        mSpTask = (Spinner) view.findViewById(R.id.munkalap_sp_task);

        mEtTaskDescription = (EditText) view.findViewById(R.id.munkalap_et_task_description);
        mEtTaskDescription.setFilters(StringUtils.addInputFilterToArray(mEtTaskDescription.getFilters(), new NoEnterInputFilter()));
        mEtWorkTime = (EditText) view.findViewById(R.id.munkalap_et_work_time);
        mEtPipCode = (EditText) view.findViewById(R.id.munkalap_et_pip_code);
        mEtPipCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                pipKodDialog = new PipKodDialog();

                Bundle params = new Bundle();
                params.putString("title", getString(R.string.munkalap_pipkod_dialog_title));

                if (mMunkalap != null) {
                    params.putSerializable(AlkatreszekKezeleseDialog.EXTRA_MUNKALAP, mMunkalap);
                }

                pipKodDialog.setArguments(params);
                pipKodDialog.setListener(new IDialogResult() {
                    @Override
                    public void onOkClicked(DialogFragment dialog) {
                        mEtPipCode.setText(mMunkalap.pipkod);
                        mEtPipCode.setError(null);
                    }

                    @Override
                    public void onCancelClicked(DialogFragment dialog) {
                    }
                });
                pipKodDialog.show(fm, "fragment_dialog_pip_kod");
            }
        });
        view.findViewById(R.id.munkalap_btn_pip_code_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mEtPipCode.getText().toString().startsWith("-")) {
                    Konfig pipKonfig = KiteDAO.getKonfig(getActivity(), "PIPMAPPA");
                    String pipFolder = Settings.PIP_CODE_DOCUMENT_DIRECTORY;
                    if (pipKonfig != null) {
                        pipFolder = pipKonfig.value;
                    }
                    if (!pipFolder.endsWith("/")) {
                        pipFolder = pipFolder + "/";
                    }
                    String pipkod = mEtPipCode.getText().toString().replaceAll(" ", "");
                    int version = PipUpdateService.getPipCodeVersion(getActivity(), pipkod);
                    File pipFile = new File(Environment.getExternalStoragePublicDirectory(pipFolder + pipkod + "_" + version + ".pdf"), "");
                    if (pipFile.exists()) {
                        Uri uri = Uri.fromFile(pipFile);
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "No PDF activity", e);
                        }
                    } else {
                        ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_pip_file_not_found_title), getString(R.string.error_dialog_pip_file_not_found_message));
                    }
                }
            }
        });
        mDcbUrgent = (DoubleCheckBoxView) view.findViewById(R.id.munkalap_dcb_urgent);
        mEtMachinehour = (EditText) view.findViewById(R.id.munkalap_et_machinehour);
        mEtMachinehour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    validateMachinehour();
                }
            }
        });
        mEtThroughput = (EditText) view.findViewById(R.id.munkalap_et_throughput);
        mEtThroughput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    validateThroughput();
                }
            }
        });
        mDcbWorkDone = (DoubleCheckBoxView) view.findViewById(R.id.munkalap_dcb_workdone);
        mDcbWorkDone.setOnStateChangedListener(new DoubleCheckBoxView.OnStateChangedListener() {
            @Override
            public void onStateChanged(String state) {
                if ("I".equals(state)) {
                    mDpWorkDoneDate.setDate(new Date());
                    mDpWorkDoneDate.setEnabled(true);
                } else {
                    mDpWorkDoneDate.setDate(null);
                    mDpWorkDoneDate.setEnabled(false);
                }
            }
        });
        mDpWorkDoneDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_work_done_date);
        mEtServicebookPage = (EditText) view.findViewById(R.id.munkalap_et_servicebook_page);
        mEtServicecarDistance = (EditText) view.findViewById(R.id.munkalap_et_servicecar_distance);
        mEtServicecarDistance.setFilters(StringUtils.addInputFilterToArray(mEtServicecarDistance.getFilters(), new DecimalDigitsInputFilter(3, 0)));
        mDcbMachineOperational = (DoubleCheckBoxView) view.findViewById(R.id.munkalap_dcb_machine_operational);
        mDcbCustomerComments = (DoubleCheckBoxView) view.findViewById(R.id.munkalap_dcb_customer_comments_1);
        mEtCustomerComments2 = (EditText) view.findViewById(R.id.munkalap_et_customer_comments_2);
        mEtCustomerComments2.setFilters(StringUtils.addInputFilterToArray(mEtCustomerComments2.getFilters(), new NoEnterInputFilter()));

        mBtAddMedia = (Button) view.findViewById(R.id.munkalap_btn_add_media);
        mBtAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMedia();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
        Rule dateTimeRule = new Rule<DateTimePickerView>(getString(R.string.error_required_field)) {
            @Override
            public boolean isValid(DateTimePickerView o) {
                return o.getDate() != null;
            }
        };
        Rule doubleCheckBoxRule = new Rule<DoubleCheckBoxView>(getString(R.string.error_required_field)) {
            @Override
            public boolean isValid(DoubleCheckBoxView o) {
                return o.getState() != null;
            }
        };
        validator.put(mDpArrivalTime, dateTimeRule);
        validator.put(mDpDepartureTime, dateTimeRule);
        validator.put(mDcbUrgent, doubleCheckBoxRule);
        validator.put(mDcbWorkDone, doubleCheckBoxRule);
        validator.put(mDcbMachineOperational, doubleCheckBoxRule);
        validator.put(mDcbCustomerComments, doubleCheckBoxRule);

        refresh();
        return view;
    }

    private boolean validateMachinehour() {
        Munkalap lastMunkalap = mListener.getMunkalap();
        if (lastMunkalap != null && lastMunkalap.uzemora != null && lastMunkalap.uzemora > 0.0) {
            Double machineHour = NumberUtils.parseDouble(mEtMachinehour.getText().toString());
            Date dateRecord = mDpWorkDate.getDate();
            Log.i(TAG, "validateMachinehour().machineHour=" + machineHour);
            String alvazszamMunkalapContinue = lastMunkalap.alvazszam;
            Date letrehozasdatum = lastMunkalap.letrehozasdatum;
            String stringDateLetrehozasdatum = DateUtils.getDfShort().format(letrehozasdatum);
            long totalmaxUzemora = getTotalMaxUzemora(stringDateLetrehozasdatum, alvazszamMunkalapContinue);
            long totalminUzemora = getTotalMinUzemora(stringDateLetrehozasdatum, alvazszamMunkalapContinue);

            if (totalmaxUzemora == 0) {
                totalmaxUzemora = Integer.MAX_VALUE;
            }
            Toast.makeText(getActivity(), "" + totalminUzemora + "/" + totalmaxUzemora, Toast.LENGTH_SHORT).show();
            if (lastMunkalap.uzemora > totalmaxUzemora) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.dialog_machine_hour_error_title), getString(R.string.dialog_machine_hour_error_message, NumberUtils.toPreciseString(lastMunkalap.uzemora, 0)));
                mEtMachinehour.setText(NumberUtils.toPreciseString((double) totalmaxUzemora - 1, 0));
                return false;
            } else if (lastMunkalap.uzemora < totalminUzemora) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.dialog_machine_hour_error_title), getString(R.string.dialog_machine_hour_error_message, NumberUtils.toPreciseString(lastMunkalap.uzemora, 0)));
                mEtMachinehour.setText(NumberUtils.toPreciseString((double) totalminUzemora + 1, 0));
                return false;
            } else {
                mEtMachinehour.setText(NumberUtils.toPreciseString(lastMunkalap.uzemora, 0));
                return true;
            }
        }
        return true;
    }

    private long getTotalMinUzemora(String stringDateLetrehozasdatum, String alvazszamMunkalapContinue) {
        long totalminUzemora = 0;
        String queryGetMinUzemoraMunkalapok = "SELECT max(uzemora) FROM munkalapok WHERE letrehozasdatum < ? AND alvazszam = ?";
        String queryGetMinUzemoraMunkalapexport = "SELECT max(uzemora) FROM munkalapokexport WHERE letrehozasdatum < ? AND alvazszam = ?";
        long minUzemoraMunkalapok = kiteORM.getNativeCount(queryGetMinUzemoraMunkalapok, new String[]{stringDateLetrehozasdatum, alvazszamMunkalapContinue});
        long minUzemoraMunkalapexport = kiteORM.getNativeCount(queryGetMinUzemoraMunkalapexport, new String[]{stringDateLetrehozasdatum, alvazszamMunkalapContinue});
        if (minUzemoraMunkalapok > minUzemoraMunkalapexport) {
            totalminUzemora = minUzemoraMunkalapok;
        } else {
            totalminUzemora = minUzemoraMunkalapexport;
        }
        return totalminUzemora;
    }

    private long getTotalMaxUzemora(String stringDateLetrehozasdatum, String alvazszamMunkalapContinue) {
        long totalmaxUzemora = 0;
        String queryGetMaxUzemoraMunkalapok = "SELECT min(uzemora) FROM munkalapok WHERE letrehozasdatum > ? AND alvazszam = ?";
        String queryGetMaxUzemoraMunkalapexport = "SELECT min(uzemora) FROM munkalapokexport WHERE letrehozasdatum > ? AND alvazszam = ?";
        long maxUzemoraMunkalapok = kiteORM.getNativeCount(queryGetMaxUzemoraMunkalapok, new String[]{stringDateLetrehozasdatum, alvazszamMunkalapContinue});
        long maxUzemoraMunkalapexport = kiteORM.getNativeCount(queryGetMaxUzemoraMunkalapexport, new String[]{stringDateLetrehozasdatum, alvazszamMunkalapContinue});
        if (maxUzemoraMunkalapok < maxUzemoraMunkalapexport && maxUzemoraMunkalapok != 0) {
            totalmaxUzemora = maxUzemoraMunkalapok;
        } else {
            totalmaxUzemora = maxUzemoraMunkalapexport;
        }
        return totalmaxUzemora;
    }


    private boolean validateThroughput() {
        Munkalap lastMunkalap = mListener.getGep().getLastClosedMunkalap(mMunkalap.munkalapsorszam);
        if (lastMunkalap != null && lastMunkalap.oraallas != null && lastMunkalap.oraallas > 0.0) {
            Double throughput = NumberUtils.parseDouble(mEtThroughput.getText().toString());
            Date dateRecord = mDpWorkDate.getDate();
            Log.i(TAG, "validateThroughput().throughput=" + throughput);
            if (((throughput == null && lastMunkalap.oraallas != null && lastMunkalap.oraallas > 0)
                    || throughput < lastMunkalap.oraallas)
                    && DateUtils.getDfOnlyDate().format(lastMunkalap.munkavegzesdatum).equals(DateUtils.getDfOnlyDate().format(dateRecord))) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.dialog_throughput_error_title), getString(R.string.dialog_throughput_error_message, NumberUtils.toPreciseString(lastMunkalap.oraallas, 0)));
                mEtThroughput.setText(NumberUtils.toPreciseString(lastMunkalap.oraallas, 0));
                return false;
            }
        }
        return true;
    }

    private boolean validateKezdesDatum() {
        if (mMunkalap != null) {
            Date javitaskezdesdatum = mDpWorkStart.getDate();
            Date munkavegzesdatum = mDpWorkDate.getDate();
            Log.i(TAG, "validateKezdesDatum().javitaskezdesdatum=" + javitaskezdesdatum + ", munkavegzesdatum=" + munkavegzesdatum);
            if (javitaskezdesdatum != null && munkavegzesdatum != null && munkavegzesdatum.before(javitaskezdesdatum)) {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.dialog_date_error_title), getString(R.string.dialog_date_error_message));
                return false;
            }
            return true;
        }

        //--- Nincs munkalap, tehat baj van
        return false;
    }


    private boolean validatePip() {
        List<PipValidator.ElementValue> values = new ArrayList<PipValidator.ElementValue>();
        values.add(new PipValidator.ElementValue("GETI", SpinnerUtils.getValue(mSpWorkType1)));
        values.add(new PipValidator.ElementValue("GETI2", SpinnerUtils.getValue(mSpWorkType2)));
        values.add(new PipValidator.ElementValue("TEV1", SpinnerUtils.getValue(mSpTask)));
        values.add(new PipValidator.ElementValue("MHB", SpinnerUtils.getValue(mSpWorkPlaceType)));
        boolean isRequired = pipValidator.isRequired(values);
        if (!isRequired) {
            Log.e("MUNKALAP", mEtPipCode.getText().toString());
            if (!mEtPipCode.getText().toString().trim().isEmpty()) {
                mEtPipCode.setError(getString(R.string.error_pip_not_required));
                return false;
            } else {
                return true;
            }
        } else {
            if ("".equals(mEtPipCode.getText().toString().trim()) || mEtPipCode.getText().toString().startsWith("-")) {
                mEtPipCode.setError(getString(R.string.error_required_field));
                return false;
            } else {
                return true;
            }
        }
    }

    private Calendar getCorrectTime(DateTimePickerView time, DateTimePickerView date) {

        Calendar result = Calendar.getInstance();
        if (time != null && time.getDate() != null) {
            result.setTime(time.getDate());
        }
        if (date != null && date.getDate() != null) {
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date.getDate());
            result.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), dateCal.get(Calendar.DAY_OF_MONTH));
        }
        return result;
    }


    private boolean validateWorkTime() {
        if (mDpArrivalTime == null
                || mDpArrivalTime.getDate() == null
                || mDpDepartureTime == null
                || mDpDepartureTime.getDate() == null
                || mDpWorkDate == null
                || mDpWorkDate.getDate() == null) {
            return true;
        }
        mDpArrivalTime.setDate(getCorrectTime(mDpArrivalTime, mDpWorkDate).getTime());
        mDpDepartureTime.setDate(getCorrectTime(mDpDepartureTime, mDpWorkDate).getTime());

        if (mDpArrivalTime.getDate().getTime() > mDpDepartureTime.getDate().getTime()) {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_work_time_title), getString(R.string.error_dialog_wrong_work_time_message));
            mDpArrivalTime.setTime(mDpDepartureTime.getDate());
            return false;
        }

        if (mDpWorkDate.getDate() != null) {
            final Uzletkoto loginService = LoginService.getManager(getActivity());
            if (loginService == null) {
                return false;
            }
            String szervizesKod = loginService.szervizeskod;
            String arrivalDate = null;
            String departureDate = null;
            if (getValidDate(mDpArrivalTime) != null) {
                arrivalDate = getValidDate(mDpWorkDate).substring(0, 10) + " " + getValidDate(mDpArrivalTime).substring(11, getValidDate(mDpArrivalTime).length());
            }
            if (getValidDate(mDpDepartureTime) != null) {
                departureDate = getValidDate(mDpWorkDate).substring(0, 10) + " " + getValidDate(mDpDepartureTime).substring(11, getValidDate(mDpArrivalTime).length());
            }
            if (arrivalDate == null || departureDate == null) {
                //--- Don't need to do anything
                return true;
            }

            //--- KITE-873
            KiteORM orm = new KiteORM(getActivity());
            String query = "munkalapsorszam <> ? AND szervizes = ? AND " +
                    "(munkabefejezesdatum >= ?) AND (? >= munkakezdesdatum)";
            String[] params = new String[]{mMunkalap.munkalapsorszam, szervizesKod,
                    arrivalDate, departureDate};

            String queryStr = KiteORM.generateQueryString(query, params);
            Log.e(TAG, "validateWorkTime.query()=" + queryStr);
            List<Munkalap> munkalapok = orm.list(Munkalap.class, query, params);
            if (munkalapok != null && munkalapok.size() > 0) {
                StringBuffer munkalapText = new StringBuffer();
                for (int i = 0; i < munkalapok.size() && i < 5; i++) {
                    Munkalap munkalap = munkalapok.get(i);
                    if (munkalap != null && munkalap.munkakezdesdatum != null && munkalap.munkabefejezesdatum != null) {
                        munkalapText.append(munkalap.munkalapsorszam).append("[");
                        munkalapText.append(DateUtils.getDfOnlyTime().format(munkalap.munkakezdesdatum)).append("-").append(DateUtils.getDfOnlyTime().format(munkalap.munkabefejezesdatum)).append("]");
                    }
                    if (i < munkalapok.size() - 1) {
                        munkalapText.append(",");
                    }
                    status = "A";
                }
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_wrong_work_time_title), getString(R.string.error_dialog_wrong_work_time_message2, munkalapText));
                return false;
            }
        }
        return true;
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
        Log.d("SaveData", "saveData start: " + (new Date()).getTime());
        if (mListener != null) {
            mMunkalap = mListener.getMunkalap();
        }
        if (mMunkalap == null) {
            try {
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_save_data_failed_title), getString(R.string.error_dialog_save_data_failed_message));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        mMunkalap.setGep(mListener.getGep());
        mMunkalap.setPartner(mListener.getPartner());
        mMunkalap.uzemkepes = mDcbMachineOperational.getState();
        mMunkalap.javitaskezdesdatum = mDpWorkStart.getDate();
        mMunkalap.javitaskesz = mDcbWorkDone.getState();
        mMunkalap.javitasdatum = mDpWorkDoneDate.getDate();
        //set date arrival
        mDpArrivalTime.setDate(getCorrectTime(mDpArrivalTime, mDpWorkDate).getTime());
        mMunkalap.munkakezdesdatum = mDpArrivalTime.getDate();

        //setdate DpArrivalTime
        mDpDepartureTime.setDate(getCorrectTime(mDpDepartureTime, mDpWorkDate).getTime());
        mMunkalap.munkabefejezesdatum = mDpDepartureTime.getDate();
        mMunkalap.munkavegzesdatum = mDpWorkDate.getDate();
        mMunkalap.munkavegzeshelye = mEtWorkPlace.getText().toString();


        mMunkalap.munkavegzeshelybesorolas = SpinnerUtils.getText(mSpWorkPlaceType);

        mMunkalap.pipkod = mEtPipCode.getText().toString();
        mMunkalap.surgos = mDcbUrgent.getState();

        mMunkalap.tevekenyseg1 = SpinnerUtils.getText(mSpTask);

        mMunkalap.tevekenyseg = mEtTaskDescription.getText().toString();
        mMunkalap.partnerkod = mListener.getPartner().partnerkod;

        if (mSpWorkType1.getSelectedItem() != null) {
            mMunkalap.munkavegzesjellege = SpinnerUtils.getText(mSpWorkType1) + "||";
        } else {
            mMunkalap.munkavegzesjellege = "||";
        }
        if (mSpWorkType2.getSelectedItem() != null) {
            mMunkalap.munkavegzesjellege += SpinnerUtils.getText(mSpWorkType2);
        }

        mMunkalap.munkaora = NumberUtils.parseDouble(mEtWorkTime.getText().toString());
        mMunkalap.szervizkonyv = mEtServicebookPage.getText().toString();
        mMunkalap.megtettkm = NumberUtils.parseDouble(mEtServicecarDistance.getText().toString());
        mMunkalap.vevoeszrevetel = mEtCustomerComments2.getText().toString();
        mMunkalap.vevopozitiv = mDcbCustomerComments.getState();
        mMunkalap.uzemora = NumberUtils.parseDouble(mEtMachinehour.getText().toString());
        mMunkalap.oraallas = NumberUtils.parseDouble(mEtThroughput.getText().toString());

        String sortipus = null;
        if (mSpWorkType1.getSelectedItem() instanceof MetaData) {
            MetaData md = (MetaData) mSpWorkType1.getSelectedItem();
            if (md != null) {
                sortipus = md.value;
            }
        }
        mMunkalap.sortipus = sortipus;

        String munkavegzesjellege2 = null;
        if (mSpWorkType2.getSelectedItem() instanceof MetaData) {
            MetaData md = (MetaData) mSpWorkType2.getSelectedItem();
            if (md != null) {
                munkavegzesjellege2 = md.value;
            }
        }
        mMunkalap.munkavegzesjellege2 = munkavegzesjellege2;

        String munkavegzeshelyszine = null;
        if (mSpWorkPlaceType.getSelectedItem() instanceof MetaData) {
            MetaData md = (MetaData) mSpWorkPlaceType.getSelectedItem();
            if (md != null) {
                munkavegzeshelyszine = md.value;
            }
        }
        mMunkalap.munkavegzeshelyszine = munkavegzeshelyszine;

        String tevekenysegDropdown = null;
        if (mSpTask.getSelectedItem() instanceof MetaData) {
            MetaData md = (MetaData) mSpTask.getSelectedItem();
            if (md != null) {
                tevekenysegDropdown = md.value;
            }
        }
        mMunkalap.tevekenysegDropdown = tevekenysegDropdown;
        if (status == null) {
            mMunkalap.status = "B";
        } else {
            mMunkalap.status = status;
        }

        if (mMunkalap._id == -1) {
            long id = ContentUris.parseId(kiteORM.insert(mMunkalap));
            mMunkalap._id = id;
            Log.i("Munkalap3", "insert id: " + id);
        } else {
            long rowsupdated = kiteORM.update(mMunkalap);
            Log.i("Munkalap3", "rowsupdated: " + rowsupdated);
        }
        Log.e(TAG, "Spinner-->mMunkalap.munkavegzeshelybesorolas=" + mMunkalap.munkavegzeshelybesorolas);
        Log.e(TAG, "Spinner-->mMunkalap.tevekenyseg1=" + mMunkalap.tevekenyseg1);
        Log.e(TAG, "Spinner-->mMunkalap.munkavegzesjellege=" + mMunkalap.munkavegzesjellege);

        Log.d("SaveData", "saveData end: " + (new Date()).getTime() + " id: " + mMunkalap._id);
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

    private void calculateWorkTime() {
        if (mDpArrivalTime.getDate() != null && mDpDepartureTime.getDate() != null) {
            Calendar now = Calendar.getInstance();
            // fix arra, ha mas napok lennenek a ket date objectben
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(mDpArrivalTime.getDate());
            startCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            startCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            startCalendar.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(mDpDepartureTime.getDate());
            endCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            endCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            endCalendar.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));

            long diff = endCalendar.getTime().getTime() - startCalendar.getTime().getTime();
            Double diffHours = (double) diff / (60 * 60 * 1000);
            mEtWorkTime.setText(NumberUtils.toPreciseString(diffHours, 1));
        }
    }


    private String getValidDate(DateTimePickerView picker) {
        if (picker == null || picker.getDate() == null) {
            return null;
        }
        Calendar cNow = Calendar.getInstance();
        cNow.setTime(mDpWorkDate.getDate());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(picker.getDate());
        if (calendar.get(Calendar.YEAR) != cNow.get(Calendar.YEAR)) {
            calendar.set(Calendar.YEAR, cNow.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, cNow.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, cNow.get(Calendar.DAY_OF_MONTH));
        }

        String validDate = DateUtils.getDfShort().format(calendar.getTime());
        Log.d(TAG, "getValidDate()=" + validDate);
        return validDate;
    }

    public void refresh() {
        if (mListener != null) {
            mMunkalap = mListener.getMunkalap();
            if (mMunkalap != null) {
                mEtPartnerMachineSummary.setText(mMunkalap.getPartnerSummary());

                mDcbMachineOperational.setState(mMunkalap.uzemkepes);
                mDcbUrgent.setState(mMunkalap.surgos);
                mDpWorkStart.setDate(mMunkalap.javitaskezdesdatum);
                mDpArrivalTime.setTime(mMunkalap.munkakezdesdatum);
                mDpArrivalTime.setListener(new DateTimePickerView.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(Calendar calendar) {
                        validateWorkTime();
                        calculateWorkTime();
                    }
                });
                mDpDepartureTime.setTime(mMunkalap.munkabefejezesdatum);
                mDpDepartureTime.setListener(new DateTimePickerView.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(Calendar calendar) {
                        validateWorkTime();
                        calculateWorkTime();
                    }
                });

                mDcbWorkDone.setState(mMunkalap.javitaskesz);
                if (mMunkalap.javitasdatum != null) {
                    mDcbWorkDone.setState("I");
                }
                mDpWorkDoneDate.setDate(mMunkalap.javitasdatum);
                mDpWorkDate.setDate(mMunkalap.munkavegzesdatum);
                mEtWorkPlace.setAdapter(null);
                mEtWorkPlace.setText(mMunkalap.munkavegzeshelye);
                if (mListener.getMode() < MunkalapActivity.MODE_VIEW) {
                    mEtWorkPlace.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEtWorkPlace.setAdapter(citiesAdapter);
                        }
                    }, 200);
                }
                mEtPipCode.setText(mMunkalap.pipkod);
                mEtWorkTime.setText(mMunkalap.munkaora != null ? mMunkalap.munkaora.toString() : "");
                mEtServicecarDistance.setText(NumberUtils.toPreciseString(mMunkalap.megtettkm, 0));
                mEtServicebookPage.setText(mMunkalap.szervizkonyv);
                mEtMachinehour.setText(NumberUtils.toPreciseString(mMunkalap.uzemora, 0));
                mEtThroughput.setText(NumberUtils.toPreciseString(mMunkalap.oraallas, 0));
                mDcbCustomerComments.setState(mMunkalap.vevopozitiv);
                mEtCustomerComments2.setText(mMunkalap.vevoeszrevetel != null ? mMunkalap.vevoeszrevetel : "");

                final String[] munkavegzesek = mMunkalap.munkavegzesjellege == null ? null : mMunkalap.munkavegzesjellege.split("\\|\\|");
                //--- Connect the spinners
                if (mListener.getMode() < MunkalapActivity.MODE_VIEW) {
                    SpinnerUtils.createAndSetSpinner(getActivity(), mSpWorkType1, munkavegzesek != null && munkavegzesek.length > 0 ? munkavegzesek[0] : null, "GETI");
                    SpinnerUtils.createAndSetSpinner(getActivity(), mSpWorkType2, munkavegzesek != null && munkavegzesek.length > 1 ? munkavegzesek[1] : null, "GETI2");
                    SpinnerUtils.createAndSetSpinner(getActivity(), mSpTask, mMunkalap.tevekenyseg1, "TEV1");
                    SpinnerUtils.createAndSetSpinner(getActivity(), mSpWorkPlaceType, mMunkalap.munkavegzeshelybesorolas, "MHB");

                    SpinnerUtils.connectSpinners(getActivity(), mSpTask, mSpWorkPlaceType, "MHB", mMunkalap.munkavegzeshelybesorolas);
                    SpinnerUtils.connectSpinners(getActivity(), mSpWorkType2, mSpTask, "TEV1", mMunkalap.tevekenyseg1);
                    SpinnerUtils.connectSpinners(getActivity(), mSpWorkType1, mSpWorkType2, "GETI2", munkavegzesek != null && munkavegzesek.length > 1 ? munkavegzesek[1] : null);
                }

                Log.e(TAG, "Spinner<--mMunkalap.munkavegzeshelybesorolas=" + mMunkalap.munkavegzeshelybesorolas);
                Log.e(TAG, "Spinner<--mMunkalap.tevekenyseg1=" + mMunkalap.tevekenyseg1);
                Log.e(TAG, "Spinner<--mMunkalap.munkavegzesjellege=" + mMunkalap.munkavegzesjellege);
                mEtTaskDescription.setText(mMunkalap.tevekenyseg);
            }

            if (mMunkalap != null && mMunkalap.bejelentesdatum != null) {
                mDpWorkStart.setEnabled(false);
            }

            if (mListener.getMode() >= MunkalapActivity.MODE_VIEW) { // MODE_VIEW, MODE_OWN
                mSpWorkType1.setEnabled(false);
                mSpWorkType2.setEnabled(false);
                mSpWorkPlaceType.setEnabled(false);
                mSpWorkPlaceType.setFocusable(false);
                mDcbMachineOperational.setEnabled(false);
                mDcbUrgent.setEnabled(false);
                mDpWorkStart.setEnabled(false);
                mDpArrivalTime.setEnabled(false);
                mDpDepartureTime.setEnabled(false);
                mDcbWorkDone.setEnabled(false);
                mDpWorkDoneDate.setEnabled(false);
                mDpWorkDate.setEnabled(false);
                mEtWorkPlace.setEnabled(false);
                mEtPipCode.setEnabled(false);
                mEtWorkTime.setEnabled(false);
                mEtServicecarDistance.setEnabled(false);
                mEtServicebookPage.setEnabled(false);
                mEtMachinehour.setEnabled(false);
                mEtThroughput.setEnabled(false);
                mEtCustomerComments2.setEnabled(false);
                mDcbCustomerComments.setEnabled(false);
                mSpTask.setEnabled(false);
                mEtTaskDescription.setEnabled(false);

                mMunkalap = mMunkalap != null ? mMunkalap : new Munkalap();
                String[] munkavegzesek = mMunkalap.munkavegzesjellege == null ? null : mMunkalap.munkavegzesjellege.split("\\|\\|");

                mEtWorkPlace.setAdapter(SpinnerUtils.createSpinnerAdapter(getActivity(), mMunkalap.munkavegzeshelye != null ? mMunkalap.munkavegzeshelye : "-"));
                mEtWorkPlace.setSelection(0);

                Log.e(TAG, "Spinner<--mMunkalap.munkavegzeshelybesorolas=" + mMunkalap.munkavegzeshelybesorolas);
                Log.e(TAG, "Spinner<--mMunkalap.tevekenyseg1=" + mMunkalap.tevekenyseg1);
                Log.e(TAG, "Spinner<--mMunkalap.munkavegzesjellege=" + mMunkalap.munkavegzesjellege);

                SpinnerUtils.createSimpleSpinner(getActivity(), mSpWorkType1, munkavegzesek != null && munkavegzesek.length > 0 ? munkavegzesek[0] : "-");
                SpinnerUtils.createSimpleSpinner(getActivity(), mSpWorkType2, munkavegzesek != null && munkavegzesek.length > 1 ? munkavegzesek[1] : "-");
                SpinnerUtils.createSimpleSpinner(getActivity(), mSpTask, mMunkalap.tevekenyseg1 != null ? mMunkalap.tevekenyseg1 : "-");
                SpinnerUtils.createSimpleSpinner(getActivity(), mSpWorkPlaceType, mMunkalap.munkavegzeshelybesorolas != null ? mMunkalap.munkavegzeshelybesorolas : "-");
            } else if (mListener.getMode() == MunkalapActivity.MODE_COPY) {
                mDpWorkStart.setEnabled(false);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onValidationSucceeded() {
        if (!validatePip()) {
            return;
        } else {
            mEtPipCode.setError(null);
        }
        if (!validateMachinehour()) {
            return;
        }
        if (!validateThroughput()) {
            return;
        }
        if (!validateKezdesDatum()) {
            return;
        }
        if (!validateWorkTime()) {
            return;
        }
        saveData();
        //mListener.goToPage(MunkalapSummaryFragment.class);
        mListener.nextPage();
        ((MunkalapActivity) getActivity()).setMunkalap(mMunkalap);
    }

    @Override
    public void onValidationFailed(View view, Rule<?> rule) {
        if (view instanceof EditText) {
            ((EditText) view).setError(rule.getFailureMessage());
        } else if (view instanceof DateTimePickerView) {
            ((DateTimePickerView) view).setError(rule.getFailureMessage());
        } else if (view instanceof DoubleCheckBoxView) {
            ((DoubleCheckBoxView) view).setError(rule.getFailureMessage());
        } else if (view instanceof Spinner) {
            ((TextView) view.findViewById(R.id.spinner_text)).setError(rule.getFailureMessage());
        }
    }
}
