package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Regex;

import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.GepActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.activity.PartnerDetailsActivity;
import hu.itware.kite.service.activity.PartnerHozzaadasActivity;
import hu.itware.kite.service.adapters.AutocompleteGepAdapter;
import hu.itware.kite.service.adapters.ExisingPartnerAdapter;
import hu.itware.kite.service.adapters.MachineArrayAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.widget.ClearableAutoCompleteTextView;
import hu.itware.kite.service.widget.DateTimePickerView;
import hu.itware.kite.service.widget.DoubleCheckBoxView;


public class MunkalapPage1Fragment extends Fragment implements IRefreshable, Validator.ValidationListener, IDialogResult {

    public static final String TAG = MunkalapPage1Fragment.class.getSimpleName();

    private MunkalapFragmentInterface mListener;

    private Validator validator;

    private ClearableAutoCompleteTextView searchText;

    private EditText partnerName;
    private EditText partnerCode;

    @Email(order = 1, messageResId = R.string.error_regexp_email)
    private EditText partnerEmail;
    private EditText partnerAddress;

    @Regex(order = 2, pattern = "^\\+?(?=(?:\\D*\\d){0,15}\\D*$)[0-9 \\-()\\\\\\/]{0,15}$", messageResId = R.string.error_regexp_fax)
    private EditText partnerFax;

    private EditText machineFilter;

    private Button newPartnerButton;
    private Button changePartnerButton;
    private Button newMachineButton;
    private Button newMachineTopButton;

    private ListView machinesListView;

    private LinearLayout formContainer;

    private KiteORM mKiteOrm;
    private Gep mGep;
    private Partner mPartner;
    private List<Gep> mGepek = new ArrayList<Gep>();
    private MachineArrayAdapter mMachineArrayAdapter;
    private AutocompleteGepAdapter gepCursorAdapter;
    private ExisingPartnerAdapter partnerCursorAdapter;

    private Spinner searchType;
    private Partner mPartnerCoppy = null;

    public MunkalapPage1Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mKiteOrm = new KiteORM(getActivity());
        gepCursorAdapter = new AutocompleteGepAdapter(getActivity(), null);
        gepCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                mPartner = null;
                Cursor cursor = mKiteOrm.query(Gep.class, GepekTable.COL_ALVAZSZAM + " LIKE ? and (baj <> 'I' or baj is null)", new String[]{searchText.getText().toString() + "%"}, GepekTable.COL_TIPUSHOSSZUNEV + " asc");
                hideFormIfCursorIsEmpty(cursor);
                return cursor;
            }
        });
        partnerCursorAdapter = new ExisingPartnerAdapter(getActivity(), null);
        partnerCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                mPartner = null;
                Cursor cursor = KiteDAO.getFilteredPartnerCursor(getActivity(), charSequence);
                hideFormIfCursorIsEmpty(cursor);
                return cursor;
            }
        });
        View view = inflater.inflate(R.layout.fragment_munkalap_page1, container, false);
        searchType = (Spinner) view.findViewById(R.id.munkalap_sp_search_type);
        searchType.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.munkalap_search_types)));
        searchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                searchText.setText("");
                switch (i) {
                    case 0:
                        searchText.setAdapter(gepCursorAdapter);
                        break;
                    case 1:
                        searchText.setAdapter(partnerCursorAdapter);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        searchText = (ClearableAutoCompleteTextView) view.findViewById(R.id.munkalap_et_search);
        searchText.setThreshold(2);
        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BaseActivity.hideSoftKeyboard(getActivity());
                String filter;
                if (searchType.getSelectedItemPosition() == 0) {
                    filter = gepCursorAdapter.getSelectedEntity().alvazszam;
                } else {
                    if (partnerCursorAdapter.getSelectedEntity().partnerkod != null) {
                        filter = partnerCursorAdapter.getSelectedEntity().partnerkod;
                    } else {
                        filter = partnerCursorAdapter.getSelectedEntity().tempkod;
                    }
                }
                search(filter);
            }
        });
        this.formContainer = (LinearLayout) view.findViewById(R.id.munkalap_ll_container);
        this.formContainer.setVisibility(View.GONE);
        partnerAddress = (EditText) view.findViewById(R.id.munkalap_et_partner_address);
        partnerCode = (EditText) view.findViewById(R.id.munkalap_et_partner_code);
        partnerEmail = (EditText) view.findViewById(R.id.munkalap_et_partner_email);
        partnerFax = (EditText) view.findViewById(R.id.munkalap_et_partner_fax);
        partnerName = (EditText) view.findViewById(R.id.munkalap_et_partner_name);
        machinesListView = (ListView) view.findViewById(R.id.munkalap_lv_machines);
        changePartnerButton = (Button) view.findViewById(R.id.munkalap_btn_switch_partner);
        changePartnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartnerDetailsActivity.class);
                intent.putExtra(PartnerDetailsActivity.MODE_PARTNER, PartnerDetailsActivity.MODE_PARTNER_SELECT);
                getActivity().startActivityForResult(intent, MunkalapActivity.PARTNER_SELECT_REQUEST_CODE);
            }
        });
        newPartnerButton = (Button) view.findViewById(R.id.munkalap_btn_new_partner);
        newPartnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity activity = (BaseActivity) getActivity();
                if (activity.hasUzletkotoAzon()) {
                    Intent intent = new Intent(getActivity(), PartnerHozzaadasActivity.class);
                    getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    getActivity().startActivityForResult(intent, MunkalapActivity.PARTNER_CREATE_REQUEST_CODE);
                } else {
                    activity.showUzletkotoAzonErrorDialog();
                }
            }
        });
        View.OnClickListener newMachineOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: ");

                BaseActivity activity = (BaseActivity) getActivity();
                if (activity.hasUzletkotoAzon()) {
                    Intent intent = new Intent(getActivity(), GepActivity.class);
                    if (mPartner != null) {
                        intent.putExtra("partnerkod", mPartner.getPartnerkod());
                    }
                    getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    getActivity().startActivityForResult(intent, MunkalapActivity.MACHINE_CREATE_REQUEST_CODE);
                } else {
                    activity.showUzletkotoAzonErrorDialog();
                }
            }
        };
        newMachineButton = (Button) view.findViewById(R.id.munkalap_btn_new_machine);
        newMachineButton.setOnClickListener(newMachineOnClickListener);
        newMachineTopButton = (Button) view.findViewById(R.id.munkalap_btn_new_machine_top);
        newMachineTopButton.setOnClickListener(newMachineOnClickListener);
        machineFilter = (EditText) view.findViewById(R.id.munkalap_et_machine_filter);
        machineFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterMachines();
            }
        });
        view.findViewById(R.id.munkalap_iv_delete_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                machineFilter.setText("");
                filterMachines();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);

        return view;
    }

    private void hideFormIfCursorIsEmpty(Cursor cursor) {
        if ((cursor == null || cursor.getCount() == 0) && mPartner == null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    formContainer.setVisibility(View.GONE);
                    newMachineTopButton.setVisibility(View.VISIBLE);
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    newMachineTopButton.setVisibility(View.GONE);
                }
            });
        }
    }

    public void search(String filter) {
        if (searchText != null) {
            mGep = null;
            if (searchType.getSelectedItemPosition() == 0) {
                mGep = mKiteOrm.loadSingle(Gep.class, GepekTable.COL_ALVAZSZAM + " = ?", new String[]{filter});
            }
            mGepek.clear();
            machineFilter.setText("");
            if (mGep != null) {
                newMachineButton.setVisibility(View.GONE);
                machineFilter.setText(mGep.alvazszam);
                mPartner = mGep.getPartner();
                mPartnerCoppy = mPartner;
                if (mListener != null) {
                    if (mPartner != null) {
                        mGepek = mPartner.getGepek(true);
                    }
                    mListener.setGep(mGep, false);
                    mListener.setPartner(mPartner);
                }
            } else {
                newMachineButton.setVisibility(View.VISIBLE);
                mPartner = mKiteOrm.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{filter});
                if (mPartner == null) {
                    mPartner = mKiteOrm.loadSingle(Partner.class, PartnerekTable.COL_TEMPKOD + " = ?", new String[]{filter});
                }
                if (mListener != null) {
                    if (mPartner != null) {
                        mGepek = mPartner.getGepek(true);
                    }
                    mListener.setPartner(mPartner);
                }
            }
            if (mPartner == null && mGep == null) {
                formContainer.setVisibility(View.GONE);
                newMachineTopButton.setVisibility(View.VISIBLE);
                ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.munkalap_title_error), getString(R.string.munkalap_message_machine_not_found));
            } else {
                formContainer.setVisibility(View.VISIBLE);
                newMachineTopButton.setVisibility(View.GONE);
            }
            refreshWithoutSearch();
        }
    }

    private void filterMachines() {
        if (mMachineArrayAdapter != null) {
            mMachineArrayAdapter.clear();
            for (Gep gep : mGepek) {
                if ((gep.alvazszam != null && gep.alvazszam.toUpperCase().contains(machineFilter.getText().toString().toUpperCase())) ||
                        (gep.tipushosszunev != null && gep.tipushosszunev.toUpperCase().contains(machineFilter.getText().toString().toUpperCase()))) {
                    mMachineArrayAdapter.add(gep);
                }
            }
            mMachineArrayAdapter.notifyDataSetChanged();
        }
    }

    public void refresh() {
        if (mListener != null) {
            //machineFilter.setText("");
            if (mPartner != null && machineFilter.getText().toString().isEmpty()) {
                mGepek = mPartner.getGepek(true);
            }
        }
        refreshWithoutSearch();
    }

    private void refreshWithoutSearch() {
        if (mListener != null) {
            mPartner = mListener.getPartner();
            if (mPartner != null) {
                Munkalap lastMunkalap = mPartner.getLastMunkalap();
                formContainer.setVisibility(View.VISIBLE);
                newMachineTopButton.setVisibility(View.GONE);
                partnerAddress.setText(mPartner.getAddress());
                partnerCode.setText(mPartner.getPartnerkod());
                partnerName.setText(mPartner.getNev());
                if (mListener.getMunkalap() != null) {
                    if (mListener.getMunkalap().email != null && !mListener.getMunkalap().email.isEmpty() && !mListener.getMunkalap().partnerChanged) {
                        partnerEmail.setText(mListener.getMunkalap().email);
                    } else {
                        if (lastMunkalap != null && lastMunkalap.email != null && !lastMunkalap.email.isEmpty()) {
                            partnerEmail.setText(lastMunkalap.email);
                        } else {
                            partnerEmail.setText(mPartner.email);
                        }
                    }
                    if (mListener.getMunkalap().fax != null && !mListener.getMunkalap().fax.isEmpty() && !mListener.getMunkalap().partnerChanged) {
                        partnerFax.setText(mListener.getMunkalap().fax);
                    } else {
                        if (lastMunkalap != null && lastMunkalap.fax != null && !lastMunkalap.fax.isEmpty()) {
                            partnerFax.setText(lastMunkalap.fax);
                        } else {
                            partnerFax.setText(mPartner.fax);
                        }
                    }
                    mListener.getMunkalap().partnerChanged = false;
                }
                mMachineArrayAdapter = new MachineArrayAdapter(getActivity(), R.layout.list_item_partner_machine_details_button, new ArrayList<Gep>(mGepek));
                if (machineFilter.getText().length() > 0) {
                    filterMachines();
                }
                machinesListView.setAdapter(mMachineArrayAdapter);
                machinesListView.invalidate();
                machinesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        mGep = mMachineArrayAdapter.getItem(i);
                        validator.validate();
                    }
                });
                machinesListView.setItemsCanFocus(true);
            } else {
                formContainer.setVisibility(View.GONE);
                partnerAddress.setText("");
                partnerCode.setText("");
                partnerName.setText("");
                partnerFax.setText("");
                partnerEmail.setText("");
                if (mMachineArrayAdapter != null) {
                    mMachineArrayAdapter.clear();
                    mMachineArrayAdapter.notifyDataSetChanged();
                }
            }
        }
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
    }

    public void machineCreated() {
        newMachineTopButton.setVisibility(View.GONE);
        mGepek.clear();
        mGepek.add(mListener.getGep());
        refreshWithoutSearch();
    }

    @Override
    public void onValidationSucceeded() {
        if (mPartner != null) {
            if (!KiteDAO.getAlreadyOpenMunkalap(getActivity(), mGep.alvazszam, mPartner.getPartnerkod())) {
                setGep();
            } else {
                ((BaseActivity) getActivity()).showQuestionDialog(getString(R.string.warning_dialog_already_open_munkalap_title), getString(R.string.warning_dialog_already_open_munkalap_message), this);
            }
        }else {
            if (!KiteDAO.getAlreadyOpenMunkalap(getActivity(), mGep.alvazszam, mPartnerCoppy.getPartnerkod())) {
                setGep();
            } else {
                ((BaseActivity) getActivity()).showQuestionDialog(getString(R.string.warning_dialog_already_open_munkalap_title), getString(R.string.warning_dialog_already_open_munkalap_message), this);
            }
        }
    }

    private void setGep() {
        if (mListener != null) {
            mListener.getMunkalap().fax = partnerFax.getText().toString();
            mListener.getMunkalap().email = partnerEmail.getText().toString();
            mListener.setGep(mGep, true);
        }
    }

    @Override
    public void onValidationFailed(View view, Rule<?> rule) {
        if (view instanceof EditText) {
            ((EditText) view).setError(rule.getFailureMessage());
        }
        if (view instanceof DateTimePickerView) {
            ((DateTimePickerView) view).setError(rule.getFailureMessage());
        }
        if (view instanceof DoubleCheckBoxView) {
            ((DoubleCheckBoxView) view).setError(rule.getFailureMessage());
        }
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {
        setGep();
        return;
    }

    @Override
    public void onCancelClicked(DialogFragment dialog) {
        return;
    }


    @Override
    public void onPause() {
        KiteORM.closeCursor(partnerCursorAdapter);
        KiteORM.closeCursor(gepCursorAdapter);
        super.onPause();
    }
}
