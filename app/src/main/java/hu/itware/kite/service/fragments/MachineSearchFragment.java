package hu.itware.kite.service.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.AutocompleteGepAdapter;
import hu.itware.kite.service.adapters.ExisingPartnerAdapter;
import hu.itware.kite.service.adapters.MachineArrayAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MachineFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.widget.ClearableAutoCompleteTextView;

public class MachineSearchFragment extends android.support.v4.app.Fragment implements IRefreshable {

    private static final String TAG = "KITE.GEP.KERESES";

    private MachineFragmentInterface mListener;
    private KiteORM mKiteORM;
    private MachineArrayAdapter arrayAdapter;
    private ListView listView;
    private List<Gep> mMachines = new ArrayList<Gep>();
    private ClearableAutoCompleteTextView mGepSearchField;
    private ClearableAutoCompleteTextView mPartnerSearchField;
    private Button mSearchButton;
    private AutocompleteGepAdapter mAutocompleteGepAdapter;
    private ExisingPartnerAdapter partnerCursorAdapter;
    private Partner selectedPartner;
    private List<String> partnerkodList = new ArrayList<String>();
    private TextView mNoMachinesTv;

    public MachineSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_machine_search, container, false);
        mKiteORM = new KiteORM(getActivity());
        listView = (ListView)view.findViewById(R.id.machine_listview);
        arrayAdapter = new MachineArrayAdapter(getActivity(),R.layout.list_item_partner_machine, mMachines);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.setMachine(mMachines.get(position));
                mListener.nextPage();
            }
        });
        mAutocompleteGepAdapter = new AutocompleteGepAdapter(getActivity(), null);
        mAutocompleteGepAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                if (mPartnerSearchField.getText().toString().length() < 2) {
                    partnerkodList.clear();
                }
                String inList = KiteDAO.makeCommaSeperatedList(partnerkodList);
                return mKiteORM.query(Gep.class,
                        GepekTable.COL_ALVAZSZAM + " LIKE ?" +
                        (partnerkodList.size() > 0 ? " AND (" + GepekTable.COL_PARTNERKOD + " IN (" + inList + ") OR " + GepekTable.COL_ALVAZSZAM + " IN (SELECT " + MunkalapokTable.COL_ALVAZSZAM + " FROM " + MunkalapokTable.TABLE_NAME + " WHERE " + MunkalapokTable.COL_PARTNERKOD + " IN (" + inList + ")))" : ""),
                        new String[]{mGepSearchField.getText().toString() + "%"}, GepekTable.COL_TIPUSHOSSZUNEV + " asc");
            }
        });
        mGepSearchField = (ClearableAutoCompleteTextView) view.findViewById(R.id.machine_et_search_gep);
        mGepSearchField.setAdapter(mAutocompleteGepAdapter);
        mGepSearchField.setThreshold(2);
        mGepSearchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                performSearch(true);
            }
        });
        mGepSearchField.setListener(new ClearableAutoCompleteTextView.OnClearListener() {
            @Override
            public void onClear() {
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
            }
        });

        partnerCursorAdapter = new ExisingPartnerAdapter(getActivity(), null);
        partnerCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                selectedPartner = null;
                Cursor cursor = KiteDAO.getFilteredPartnerCursor(getActivity(), mPartnerSearchField.getText().toString());
                makePartnerkodList(cursor);
                return cursor;
            }
        });
        mPartnerSearchField = (ClearableAutoCompleteTextView) view.findViewById(R.id.machine_et_search_partner);
        mPartnerSearchField.setAdapter(partnerCursorAdapter);
        mPartnerSearchField.setThreshold(2);
        mPartnerSearchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPartner = partnerCursorAdapter.getSelectedEntity();
                mListener.setSelectedPartnerkod(selectedPartner.partnerkod);
                Log.e("SEARCH", "selectedPartner " + selectedPartner);
                performSearch(true);
            }
        });
        mPartnerSearchField.setListener(new ClearableAutoCompleteTextView.OnClearListener() {
            @Override
            public void onClear() {
                partnerkodList.clear();
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
            }
        });
        mPartnerSearchField.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
            @Override
            public void onDismiss() {
                makePartnerkodList(partnerCursorAdapter.getCursor());
            }
        });
        mSearchButton = (Button) view.findViewById(R.id.machine_btn_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch(true);
            }
        });
        mNoMachinesTv = (TextView) view.findViewById(R.id.machine_no_machines);

        return view;
    }

    private void makePartnerkodList(Cursor cursor) {
        List<Partner> partnerList = mKiteORM.getListFromCursor(cursor, TableMap.getHandlerByClass(Partner.class), false);
        partnerkodList.clear();
        for (Partner partner : partnerList) {
            partnerkodList.add(partner.partnerkod);
        }
    }

    private void performSearch(final boolean nextPageOnSingleResult) {
        if (mListener != null) {
            HashMap<String, String> filters = new HashMap<String, String>();
            filters.put("alvazszam", mGepSearchField.getText().toString());
            filters.put("partner", mPartnerSearchField.getText().toString());
            Log.e("GEP", "Setting filter: " + filters.toString());
            mListener.setFilters(filters);
        }

        final String keyword = mGepSearchField.getText().toString();
        if (selectedPartner != null) {
            partnerkodList.clear();
            if (selectedPartner.partnerkod != null) {
                partnerkodList.add(selectedPartner.partnerkod);
            } else {
                partnerkodList.add(selectedPartner.tempkod);
            }
        }

        if (partnerkodList.size() == 0 || partnerkodList.size() > 1) {
            selectedPartner = null;
            mListener.setSelectedPartnerkod(null);
            Log.e("SEARCH", "selectedPartner null");
        }

        if (mPartnerSearchField.getText().toString().length() < 2) {
            partnerkodList.clear();
        }

        new AsyncTask<Void,Void,List<Gep>>() {

            @Override
            protected List<Gep> doInBackground(Void... params) {
                String inList = KiteDAO.makeCommaSeperatedList(partnerkodList);
                Log.e("SEARCH", "inlist: " + inList);
                return mKiteORM.list(Gep.class,
                    GepekTable.COL_ALVAZSZAM + " LIKE ?" +
                    (partnerkodList.size() > 0 ? " AND " +
                            "((" + GepekTable.COL_PARTNERKOD + " IN (" + inList + ") OR " + GepekTable.COL_TEMPPARTNERKOD + " IN (" + inList + ")) " +
                            "OR " + GepekTable.COL_ALVAZSZAM + " IN (SELECT " + MunkalapokTable.COL_ALVAZSZAM + " FROM " + MunkalapokTable.TABLE_NAME + " WHERE " + MunkalapokTable.COL_PARTNERKOD + " IN (" + inList + ")))" : ""),
                    new String[]{keyword + "%"});
            }

            @Override
            protected void onPostExecute(List<Gep> result) {
                super.onPostExecute(result);
                mMachines = result;
                if (mMachines.size() == 0) {
                    if (mGepSearchField.getText().toString().length() > 0) {
                        mNoMachinesTv.setText(R.string.machine_no_machine);
                    } else {
                        mNoMachinesTv.setText(R.string.machine_no_machine_partner);
                    }
                    mNoMachinesTv.setVisibility(View.VISIBLE);
                } else {
                    mNoMachinesTv.setVisibility(View.GONE);
                }
                arrayAdapter.clear();
                arrayAdapter.addAll(mMachines);
                arrayAdapter.notifyDataSetChanged();
                if (mMachines.size() == 1 && mListener != null && nextPageOnSingleResult) {
                    mListener.setMachine(mMachines.get(0));
                    mListener.nextPage();
                }
            }
        }.execute();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (MachineFragmentInterface) activity;
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

    @Override
    public void refresh() {
        Log.e("GEP", "refresh");
        if (mListener != null) {
            Log.e("GEP", "mListener");
            HashMap<String, String> filters = mListener.getFilters();
            mNoMachinesTv.setVisibility(View.GONE);
            if (filters != null) {
                Log.e("GEP", "Restoring filters: " + filters.toString());
                mGepSearchField.setText(filters.get("alvazszam"));
                mGepSearchField.dismissDropDown();
                mPartnerSearchField.setText(filters.get("partner"));
                mPartnerSearchField.dismissDropDown();
            }
        }
    }

    @Override
    public void onPause() {

        Log.e(TAG, "onPause()");
        KiteORM.closeCursor(partnerCursorAdapter);
        KiteORM.closeCursor(mAutocompleteGepAdapter);
        super.onPause();
    }
}
