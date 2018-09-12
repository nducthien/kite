package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import hu.itware.kite.service.R;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;

public class MunkalapPage2Fragment extends Fragment implements IRefreshable {

    private MunkalapFragmentInterface mListener;

    private TextView partnerName;
    private TextView partnerCode;
    private TextView partnerEmail;
    private TextView partnerAddress;
    private TextView partnerFax;

    private TextView serialNumber;
    private TextView name;
    private TextView manufactureDate;
    private TextView warrantyEnd;
    private TextView extendedWarranty;
    private TextView workhourLimit;

    private Button nextButton;

    private Partner mPartner;
    private Gep mGep;
    private Munkalap mMunkalap;

    private boolean mViewCreated = false;

    public MunkalapPage2Fragment() {
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
        View view = inflater.inflate(R.layout.fragment_munkalap_page2, container, false);

        partnerAddress = (TextView) view.findViewById(R.id.munkalap_et_partner_address);
        partnerCode = (TextView) view.findViewById(R.id.munkalap_et_partner_code);
        partnerEmail = (TextView) view.findViewById(R.id.munkalap_et_partner_email);
        partnerFax = (TextView) view.findViewById(R.id.munkalap_et_partner_fax);
        partnerName = (TextView) view.findViewById(R.id.munkalap_et_partner_name);

        serialNumber = (TextView) view.findViewById(R.id.machine_list_tv_serial_number);
        name = (TextView) view.findViewById(R.id.machine_list_tv_name);
        manufactureDate = (TextView) view.findViewById(R.id.machine_list_tv_manufacture_date);
        warrantyEnd = (TextView) view.findViewById(R.id.machine_list_tv_warranty_end_date);
        extendedWarranty = (TextView) view.findViewById(R.id.machine_list_tv_extended_warranty);
        workhourLimit = (TextView) view.findViewById(R.id.machine_list_tv_workhour_limit);

        nextButton = (Button) view.findViewById(R.id.munkalap_btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener.getCurrentPage().equals(MunkalapPage2Fragment.class)) {
                    mListener.nextPage();
                }
            }
        });

        mViewCreated = true;

        return view;
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
    public void onStart() {
        super.onStart();

        refresh();
    }

    public void refresh() {
        if(mListener == null) {
            return;
        }
        if (!this.mViewCreated) {
            return;
        }

        mGep = mListener.getGep();
        mPartner = mListener.getPartner();
        mMunkalap = mListener.getMunkalap();

        if (mPartner != null) {
            if (mPartner.getAddress() != null && partnerAddress != null) partnerAddress.setText(mPartner.getAddress());
            if (mPartner.getPartnerkod() != null && partnerCode != null) partnerCode.setText(mPartner.getPartnerkod());
            if (mPartner.getNev() != null && partnerName != null) partnerName.setText(mPartner.getNev());
            if (mPartner.fax != null && partnerFax != null) partnerFax.setText(mMunkalap.fax);
            if (mMunkalap.email != null && partnerEmail != null) partnerEmail.setText(mMunkalap.email);
        }

        if (mGep != null) {
            if (serialNumber != null) {
                serialNumber.setText(mGep.alvazszam);
            }
            name.setText(mGep.tipushosszunev);
            manufactureDate.setText(mGep.gyartaseve);
            if (mGep.garanciaervenyesseg != null) {
                warrantyEnd.setText((new SimpleDateFormat("yyyy-MM-dd")).format(mGep.garanciaervenyesseg));
            } else {
                warrantyEnd.setText("-");
            }
            if (mGep.kjotallas != null) {
                extendedWarranty.setText((new SimpleDateFormat("yyyy-MM-dd")).format(mGep.kjotallas));
            } else {
                extendedWarranty.setText("-");
            }
            if (mGep.uzemorakorlat != null) {
                workhourLimit.setText(Double.toString(mGep.uzemorakorlat));
            } else {
                workhourLimit.setText("-");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
