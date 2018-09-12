package hu.itware.kite.service.fragments;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.AutocompleteGepAdapter;
import hu.itware.kite.service.adapters.ExisingPartnerAdapter;
import hu.itware.kite.service.adapters.MunkalapCursorAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.KiteDatabaseHelper;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.widget.ClearableAutoCompleteTextView;
import hu.itware.kite.service.widget.DateTimePickerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class OwnMunkalapListFragment extends Fragment {

    private static final String TAG = "KITE.MUNKALAP.SAJAT";

    private KiteORM mKiteOrm;
    private MunkalapFragmentInterface mListener;
    private ListView listView;
    private List<Munkalap> mMunkalapList = new ArrayList<Munkalap>();
    private ClearableAutoCompleteTextView mParterSearch;
    private ClearableAutoCompleteTextView mGepSearch;
    private DateTimePickerView mFromDate;
    private DateTimePickerView mToDate;
    private Button mSearchButton;
    private MunkalapCursorAdapter mMunkalapCursorAdapter;
    private ExisingPartnerAdapter partnerCursorAdapter;
    private AutocompleteGepAdapter mAutocompleteGepAdapter;

    private Partner selectedPartner;
    private List<String> partnerkodList = new ArrayList<String>();
    private Gep selectedGep;
    private List<String> alvazszamList = new ArrayList<String>();

    private List<String> partnerkodFilter;
    private List<String> alvazszamFilter;
    public ProgressBar progressBar;

    private Cursor munkalapCursor;
    private  KiteDatabaseHelper helper;

    public OwnMunkalapListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mKiteOrm = new KiteORM(getActivity());
        partnerCursorAdapter = new ExisingPartnerAdapter(getActivity(), null);
        partnerCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                selectedPartner = null;
                Cursor cursor = KiteDAO.getFilteredPartnerCursor(getActivity(), mParterSearch.getText().toString(), partnerkodFilter);
                List<Partner> partnerList = mKiteOrm.getListFromCursor(cursor, TableMap.getHandlerByClass(Partner.class), false);
                partnerkodList.clear();
                for (Partner partner : partnerList) {
                    partnerkodList.add(partner.partnerkod);
                }
                return cursor;
            }
        });
        mAutocompleteGepAdapter = new AutocompleteGepAdapter(getActivity(), null);
        mAutocompleteGepAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                selectedGep = null;
                String inList = KiteDAO.makeCommaSeperatedList(alvazszamFilter);
                Cursor cursor = mKiteOrm.query(Gep.class, GepekTable.COL_ALVAZSZAM + " LIKE ? AND " + GepekTable.COL_ALVAZSZAM + " IN (" + inList + ")", new String[]{mGepSearch.getText().toString() + "%"}, GepekTable.COL_TIPUSHOSSZUNEV + " asc");
                List<Gep> gepList = mKiteOrm.getListFromCursor(cursor, TableMap.getHandlerByClass(Gep.class), false);
                alvazszamList.clear();
                for (Gep gep : gepList) {
                    alvazszamList.add(gep.alvazszam);
                }
                return cursor;
            }
        });

        View view = inflater.inflate(R.layout.fragment_own_munkalap_list, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar_list);
        listView = (ListView) view.findViewById(R.id.munkalap_listview);
        mMunkalapCursorAdapter = new MunkalapCursorAdapter(getActivity(), munkalapCursor);
        listView.setAdapter(mMunkalapCursorAdapter);
        mParterSearch = (ClearableAutoCompleteTextView) view.findViewById(R.id.munkalap_et_search_partner);
        mParterSearch.setAdapter(partnerCursorAdapter);
        mParterSearch.setDropDownWidth(1000);
        mParterSearch.setThreshold(1);
        mParterSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPartner = partnerCursorAdapter.getSelectedEntity();
                reload();
            }
        });
        mGepSearch = (ClearableAutoCompleteTextView) view.findViewById(R.id.munkalap_et_search_gep);
        mGepSearch.setAdapter(mAutocompleteGepAdapter);
        mGepSearch.setDropDownWidth(1000);
        mGepSearch.setThreshold(1);
        mGepSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGep = mAutocompleteGepAdapter.getSelectedEntity();
                reload();
            }
        });
        mFromDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_from_date);
        mToDate = (DateTimePickerView) view.findViewById(R.id.munkalap_dp_to_date);
        mSearchButton = (Button) view.findViewById(R.id.munkalap_btn_search);
        listView.setVisibility(View.GONE);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        });
        reload();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mMunkalapCursorAdapter != null && mMunkalapCursorAdapter.checkMunkalapTask != null) {
            mMunkalapCursorAdapter.checkMunkalapTask.cancel(true);
            mMunkalapCursorAdapter.checkMunkalapTask = null;
        }
    }

    public void reload() {
        progressBar.setVisibility(View.VISIBLE);
        new AsyncTask<Date, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Date... dates) {
                if (selectedGep != null) {
                    alvazszamList.clear();
                    alvazszamList.add(selectedGep.alvazszam);
                }

                if (selectedPartner != null) {
                    partnerkodList.clear();
                    partnerkodList.add(selectedPartner.partnerkod);
                }
                return KiteDAO.getOwnMunkalapList(getActivity(), alvazszamList, partnerkodList, dates[0], dates[1]);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                super.onPostExecute(cursor);
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                munkalapCursor = cursor;
                mMunkalapCursorAdapter.changeCursor(munkalapCursor);
//                if (oldCursor != null) {
//                    oldCursor.close();
//                }
                mMunkalapCursorAdapter.notifyDataSetChanged();
                if (mMunkalapList.size() == 0) {
                    partnerkodFilter = new ArrayList<String>();
                    alvazszamFilter = new ArrayList<String>();
                    mMunkalapList = mKiteOrm.getListFromCursor(munkalapCursor, TableMap.getHandlerByClass(Munkalap.class), false);
                    for (Munkalap munkalap : mMunkalapList) {
                        partnerkodFilter.add(munkalap.partnerkod);
                        alvazszamFilter.add(munkalap.alvazszam);
                    }
                }
            }
        }.execute(mFromDate.getDate(), mToDate.getDate());
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause()");

//        KiteORM.closeCursor(partnerCursorAdapter);
//        KiteORM.closeCursor(mMunkalapCursorAdapter);
//        KiteORM.closeCursor(munkalapCursor);

        super.onPause();
    }
}
