package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.PartnerDetailsActivity;
import hu.itware.kite.service.activity.PartnerHozzaadasActivity;
import hu.itware.kite.service.adapters.ExisingPartnerAdapter;
import hu.itware.kite.service.adapters.PartnerCursorAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.PartnerFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.widget.ClearableAutoCompleteTextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PartnerSearchFragment extends Fragment {

    private static final String TAG = "KITE.PARTNER.SEARCH";

    private PartnerFragmentInterface mListener;
    private PartnerCursorAdapter partnerCursorAdapter;
    private ListView listView;
    private Cursor mPartners;
    private ClearableAutoCompleteTextView mSearchField;
    private Button mSearchButton;
    private Button mNewPartnerButton;
    private ExisingPartnerAdapter partnerAutoCompleteAdapter;

    public PartnerSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        partnerAutoCompleteAdapter = new ExisingPartnerAdapter(getActivity(), null);
        partnerAutoCompleteAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return KiteDAO.getFilteredPartnerCursor(getActivity(), charSequence);
            }
        });
        View view = inflater.inflate(R.layout.fragment_partner_search, container, false);
        mNewPartnerButton = (Button) view.findViewById(R.id.partner_btn_new_partner_top);
        mNewPartnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartnerHozzaadasActivity.class);
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                getActivity().startActivityForResult(intent, PartnerDetailsActivity.PARTNER_CREATE_REQUEST_CODE);
            }
        });
        listView = (ListView) view.findViewById(R.id.partner_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.setPartner(partnerCursorAdapter.getSelectedEntity());
            }
        });
        mSearchField = (ClearableAutoCompleteTextView) view.findViewById(R.id.partner_et_search);
        mSearchField.setThreshold(2);
        mSearchField.setAdapter(partnerAutoCompleteAdapter);
        mSearchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                search();
            }
        });
        mSearchButton = (Button) view.findViewById(R.id.partner_btn_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
        return view;
    }



    private void search() {
        Log.e(TAG, "search()=" + mSearchField.getText());
        if (mPartners != null && !mPartners.isClosed()) {
            mPartners.close();
        }
        mPartners = KiteDAO.getFilteredPartnerCursor(getActivity(), mSearchField.getText());
        partnerCursorAdapter = new PartnerCursorAdapter(getActivity(), mPartners);
        if (mPartners.getCount() == 0) {
            ((BaseActivity) getActivity()).showErrorDialog(getString(R.string.error_dialog_partner_not_found_title), getString(R.string.error_dialog_partner_not_found_message));
            if (mListener.getMode() != PartnerDetailsActivity.MODE_PARTNER_INFO) {
                mNewPartnerButton.setVisibility(View.VISIBLE);
            }
        } else {
            mNewPartnerButton.setVisibility(View.GONE);
        }
        listView.setAdapter(partnerCursorAdapter);
    }

    @Override
    public void onPause() {
        KiteORM.closeCursor(partnerCursorAdapter);
        KiteORM.closeCursor(partnerAutoCompleteAdapter);
        super.onPause();

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

}
