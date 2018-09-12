package hu.itware.kite.service.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import hu.itware.kite.service.R;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.PartnerFragmentInterface;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;

/**
 * A simple {@link Fragment} subclass.
 */
public class PartnerDetailsFragment extends Fragment implements IRefreshable {

    private PartnerFragmentInterface mListener;
    private TextView mHeader;
    private EditText mTvPartnerCode;
    private EditText mTvName;
    private EditText mTvPartnerAlkozpont;
    private EditText mTvAddress;
    private EditText mTvEmail;
    private EditText mTvMinosites;
    private EditText mTvSzabadFeherlistaKeret;
    private EditText mTvAtutalasosFeherlistaKeret;

    private Partner mPartner;

    public PartnerDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_partner_details, container, false);
        mHeader = (TextView) view.findViewById(R.id.partner_header);
        mTvPartnerCode = (EditText) view.findViewById(R.id.partner_et_partnercode);
        mTvName = (EditText) view.findViewById(R.id.partner_et_name);
        mTvPartnerAlkozpont = (EditText) view.findViewById(R.id.partner_et_alkozpont);
        mTvAddress = (EditText) view.findViewById(R.id.partner_et_address);
        mTvEmail = (EditText) view.findViewById(R.id.partner_et_email);
        mTvMinosites = (EditText) view.findViewById(R.id.partner_et_minosites);
        mTvSzabadFeherlistaKeret = (EditText) view.findViewById(R.id.partner_et_szabad_feherlista_keret);
        mTvAtutalasosFeherlistaKeret = (EditText) view.findViewById(R.id.partner_et_atutalasos_feherlista_keret);
        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (PartnerFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PartnerFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void refresh() {
        if (mListener != null && mListener.getPartner() != null) {
            mPartner = mListener.getPartner();
            Log.e("PARTNER DETAILS", "adoazon= " + mPartner.adoazonosito);

			if (mPartner.alkozpontkod != null) {
				KiteDAO.loadPartnerData(getActivity(), mPartner);
			}

            Munkalap lastMunkalap = mPartner.getLastMunkalap();

            mHeader.setText(mPartner.getNev());
            mTvPartnerCode.setText(mPartner.getPartnerkod());
            mTvAddress.setText(mPartner.getAddress());
            mTvName.setText(mPartner.getNev());
            if (lastMunkalap != null && mPartner.email != null && !"".equals(mPartner.email)) {
                mTvEmail.setText(getString(R.string.partner_email_template, mPartner.email, lastMunkalap.email));
            } else {
                mTvEmail.setText(mPartner.email);
            }
            mTvMinosites.setText(mPartner.minosites);
			mTvPartnerAlkozpont.setText(mPartner.alkozpont == null ? "-" : mPartner.alkozpont.nev);

			if (mPartner.minositesdatuma == null) {
				mTvMinosites.setText(R.string.partner_message_no_rating);
			} else {
				Calendar calendar = Calendar.getInstance();
				int currentYear = calendar.get(Calendar.YEAR);
				calendar.setTime(mPartner.minositesdatuma);
				int ratingYear = calendar.get(Calendar.YEAR);
				if (currentYear != ratingYear) {
					mTvMinosites.setText(R.string.partner_message_old_rating);
				}
			}

            if (mPartner.limit3 > 0) {
                mTvSzabadFeherlistaKeret.setText(mPartner.limit3.toString());
            } else {
                mTvSzabadFeherlistaKeret.setText(getString(R.string.partner_message_cash_work_only));
            }
            if (mPartner.limit1 > 0) {
                mTvAtutalasosFeherlistaKeret.setText(mPartner.limit1.toString());
            } else {
                mTvAtutalasosFeherlistaKeret.setText(getString(R.string.partner_message_cash_bill_only));
            }
        }
    }
}
