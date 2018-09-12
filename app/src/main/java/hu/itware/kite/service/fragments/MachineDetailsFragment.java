package hu.itware.kite.service.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MachineDetailsActivity;
import hu.itware.kite.service.adapters.PartnerSzerzodesArrayAdapter;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.AlkozpontokTable;
import hu.itware.kite.service.orm.model.Alkozpont;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.GepSzerzodes;
import hu.itware.kite.service.orm.model.GepSzerzodesTetel;
import hu.itware.kite.service.orm.model.Partner;

/**
 * A simple {@link Fragment} subclass.
 */
public class MachineDetailsFragment extends Fragment implements IRefreshable {

    private EditText mEtName;
    private EditText mEtSerialNumber;
    private EditText mEtManufactureDate;
    private EditText mEtStartOfOperationDate;
    private EditText mEtWarrantyEndDate;
    private EditText mEtOwnerName;
    private EditText mEtOwnerPartnercode;
    private EditText mEtOwnerAddress;
    private EditText mEtOwnerLocation;
    private EditText mEtExtendedWarranty;
    private EditText mEtWorkhourLimit;
    private Button mBtServiceHistory;
    private Button mBtMachineContracts;
    private Button mBtPartnerContracts;
    
    private Gep mGep;
    private MachineDetailsActivity mListener;

    private boolean uiInitialized;
    private TextView mTvNoMachineContract;
    private TextView mTvNoPartnerContract;

    private Partner owner;
    private Alkozpont alkozpont;
    private AsyncTask<Void, Void, Void> task1;
    private AsyncTask<Void, Void, Void> task2;
    private ProgressBar progressBar;

    private List<GepSzerzodes> partnerGepSzerzodesList;
    private List<GepSzerzodes> gepSzerzodesList;

    public MachineDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_machine_details, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        mEtName = (EditText) view.findViewById(R.id.gep_et_name);
        mEtSerialNumber = (EditText) view.findViewById(R.id.gep_et_serial_number);
        mEtManufactureDate = (EditText) view.findViewById(R.id.gep_et_manufacture_date);
        mEtStartOfOperationDate = (EditText) view.findViewById(R.id.gep_et_start_of_operation_date);
        mEtWarrantyEndDate = (EditText) view.findViewById(R.id.gep_et_warranty_end_date);
        mEtOwnerName = (EditText) view.findViewById(R.id.gep_et_owner_name);
        mEtOwnerPartnercode = (EditText) view.findViewById(R.id.gep_et_owner_partnercode);
        mEtOwnerAddress = (EditText) view.findViewById(R.id.gep_et_owner_address);
        mEtOwnerLocation = (EditText) view.findViewById(R.id.gep_et_owner_location);
        mEtExtendedWarranty = (EditText) view.findViewById(R.id.gep_et_extended_warranty);
        mEtWorkhourLimit = (EditText) view.findViewById(R.id.gep_et_workhour_limit);
        mBtServiceHistory = (Button) view.findViewById(R.id.gep_btn_service_history);
        mBtMachineContracts = (Button) view.findViewById(R.id.gep_btn_machine_contracts);
        mTvNoMachineContract = (TextView) view.findViewById(R.id.gep_tv_no_machine_contract);
        mBtPartnerContracts = (Button) view.findViewById(R.id.gep_btn_partner_contracts);
        mTvNoPartnerContract = (TextView) view.findViewById(R.id.gep_tv_no_partner_contract);
        if (mListener.getMode() == MachineDetailsActivity.MODE_DETAILS_ONLY) {
            mBtServiceHistory.setVisibility(View.GONE);
        }
            mBtServiceHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.nextPage();
                }
            });
            mBtMachineContracts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getChildFragmentManager();
                    GepSzerzodesDialog dialog = new GepSzerzodesDialog();
                    dialog.setItems(gepSzerzodesList);
                    Bundle params = new Bundle();
                    params.putString("title", getString(R.string.label_machine_contracts_template, mGep.alvazszam));

                    dialog.setArguments(params);
                    dialog.show(fm, "fragment_dialog_gep_szerzodes");
                }
            });
            mBtPartnerContracts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getChildFragmentManager();
                    PartnerSzerzodesDialog dialog = new PartnerSzerzodesDialog();
                    List<PartnerSzerzodesArrayAdapter.PartnerSzerzodes> partnerSzerzodesList = new ArrayList<PartnerSzerzodesArrayAdapter.PartnerSzerzodes>();
                    for (GepSzerzodes contract : partnerGepSzerzodesList) {
                        for (GepSzerzodesTetel tetel : contract.getTetelek()) {
                            PartnerSzerzodesArrayAdapter.PartnerSzerzodes partnerSzerzodes = new PartnerSzerzodesArrayAdapter.PartnerSzerzodes();
                            partnerSzerzodes.alvazszam = tetel.alvazszam;
                            partnerSzerzodes.machineName = tetel.getGep() != null ? tetel.getGep().tipushosszunev : "";
                            partnerSzerzodes.contractType = contract.szerzodestipus;
                            partnerSzerzodes.contractNumber = tetel.szerzodeskod;
                            partnerSzerzodes.startDate = contract.kezdesdatum;
                            partnerSzerzodes.endDate = contract.lejaratdatum;
                            partnerSzerzodesList.add(partnerSzerzodes);
                        }
                    }
                    dialog.setItems(partnerSzerzodesList);

                    Bundle params = new Bundle();
                    params.putString("title", getString(R.string.label_partner_contracts_template, mGep.getPartner().getNev()));

                    dialog.setArguments(params);
                    dialog.show(fm, "fragment_dialog_partner_szerzodes");
                }
            });
        uiInitialized = true;
        refresh();
        return view;
    }

    public void setEmptyTextViews(){
        mEtName.setText("");
        mEtSerialNumber.setText("");
        mEtManufactureDate.setText("");
        mEtStartOfOperationDate.setText("");
        mEtWarrantyEndDate.setText("");
        mEtOwnerName.setText("");
        mEtOwnerPartnercode.setText("");
        mEtOwnerAddress.setText("");
        mEtOwnerLocation.setText("");
        mEtExtendedWarranty.setText("");
        mEtWorkhourLimit.setText("");
        mBtPartnerContracts.setVisibility(View.GONE);
        mTvNoPartnerContract.setVisibility(View.GONE);
        mBtMachineContracts.setVisibility(View.GONE);
        mTvNoMachineContract.setVisibility(View.GONE);
    }


    @Override
    public void refresh() {
        if (mListener != null) {
            if (mListener != null && mListener.getMachine() != null) {
                setEmptyTextViews();
                mGep = mListener.getMachine();
            }
            if (mGep != null && uiInitialized) {
                progressBar.setVisibility(View.VISIBLE);
                if (task1 == null || (!task1.getStatus().equals(AsyncTask.Status.RUNNING) && !task1.getStatus().equals(AsyncTask.Status.PENDING))) {
                    task1 = new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            partnerGepSzerzodesList = mGep.getPartner().getPartnerContracts();
                            gepSzerzodesList = mGep.getGepSzerzodesList();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (task2.getStatus().equals(Status.FINISHED)) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (partnerGepSzerzodesList != null && partnerGepSzerzodesList.size() == 0) {
                                mBtPartnerContracts.setVisibility(View.GONE);
                                mTvNoPartnerContract.setVisibility(View.VISIBLE);
                            } else {
                                mBtPartnerContracts.setVisibility(View.VISIBLE);
                                mTvNoPartnerContract.setVisibility(View.GONE);
                            }
                            if (gepSzerzodesList != null && gepSzerzodesList.size() == 0) {
                                mBtMachineContracts.setVisibility(View.GONE);
                                mTvNoMachineContract.setVisibility(View.VISIBLE);
                            } else {
                                mBtMachineContracts.setVisibility(View.VISIBLE);
                                mTvNoMachineContract.setVisibility(View.GONE);
                            }
                        }
                    }.execute();
                }
                mEtExtendedWarranty.setText(mGep.kjotallas != null ? BaseActivity.getSdfShort().format(mGep.kjotallas) : "");
                mEtWorkhourLimit.setText(mGep.uzemorakorlat != null ? Double.toString(mGep.uzemorakorlat) : "");
                mEtName.setText(mGep.tipushosszunev);
                mEtSerialNumber.setText(mGep.alvazszam);
                mEtManufactureDate.setText(mGep.gyartaseve);
                if (mGep.uzembehelyezesdatum != null) {
                    mEtStartOfOperationDate.setText(BaseActivity.getSdfShort().format(mGep.uzembehelyezesdatum));
                }
                if (mGep.garanciaervenyesseg != null) {
                    mEtWarrantyEndDate.setText(BaseActivity.getSdfShort().format(mGep.garanciaervenyesseg));
                }
                if (task2 == null || (!task2.getStatus().equals(AsyncTask.Status.RUNNING) && !task2.getStatus().equals(AsyncTask.Status.PENDING))) {
                    task2 = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            owner = mGep.getPartner();
                            if (owner != null && owner.alkozpontkod != null) {
                                alkozpont = owner.getAlkozpont();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (task1.getStatus().equals(Status.FINISHED)) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (owner != null) {
                                mEtOwnerName.setText(owner.getNev());
                                mEtOwnerPartnercode.setText(owner.getPartnerkod());
                                mEtOwnerAddress.setText(owner.getAddress());
                                if (alkozpont != null) {
                                    mEtOwnerLocation.setText(alkozpont.nev);
                                } else {
                                    mEtOwnerLocation.setText("-");
                                }
                            }
                        }
                    }.execute();
                }
            }
        }
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (MachineDetailsActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MachineFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setGep(Gep gep) {
        this.mGep = gep;
    }
}
