package hu.itware.kite.service.fragments;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.adapters.ExisingPartnerAdapter;
import hu.itware.kite.service.adapters.MunkalapArrayAdapter;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.utils.StringUtils;
import hu.itware.kite.service.widget.ClearableAutoCompleteTextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MunkalapListFragment extends Fragment {

    private KiteORM mKiteOrm;
    private MunkalapFragmentInterface mListener;
    private ListView listView;
    private List<Munkalap> mMunkalapList = new ArrayList<Munkalap>();
    private List<Munkalap> mFilteredMunkalapList = new ArrayList<Munkalap>();
    private ClearableAutoCompleteTextView mSearchField;
    private Button mSearchButton;
    private MunkalapArrayAdapter mMunkalapArrayAdapter;
    private ExisingPartnerAdapter partnerCursorAdapter;

    private List<String> partnerkodList;
    public ProgressBar progressBar;

    public MunkalapListFragment() {
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
                List<String> args = new ArrayList<String>();
                args.addAll(partnerkodList);
                args.add(StringUtils.clearText(mSearchField.getText().toString()) + "%");
                args.add(StringUtils.clearText(mSearchField.getText().toString()) + "%");
                StringBuilder inList = new StringBuilder(partnerkodList.size()*2);
                for(int i=0;i<partnerkodList.size();i++) {
                    if(i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                }
                return mKiteOrm.query(Partner.class, PartnerekTable.COL_PARTNERKOD + " IN (" + inList + ") AND (" + PartnerekTable.COL_SEARCHNEV + " LIKE ? OR " + PartnerekTable.COL_SEARCHCIM + " LIKE ? )", args.toArray(new String[args.size()]), PartnerekTable.COL_NEV1 + " asc");
            }
        });

        View view = inflater.inflate(R.layout.fragment_munkalap_list, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.progressbar_list);
        mFilteredMunkalapList = new ArrayList<Munkalap>();
        listView = (ListView) view.findViewById(R.id.munkalap_listview);
        mMunkalapArrayAdapter = new MunkalapArrayAdapter(getActivity(), R.layout.list_item_munkalap, mFilteredMunkalapList, progressBar);
        listView.setAdapter(mMunkalapArrayAdapter);
        mSearchField = (ClearableAutoCompleteTextView) view.findViewById(R.id.munkalap_et_search);
        mSearchField.setAdapter(partnerCursorAdapter);
        mSearchField.setThreshold(2);
        mSearchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                filterData();
            }
        });
        mSearchButton = (Button) view.findViewById(R.id.munkalap_btn_search);
        if (mListener.getMode() == MunkalapActivity.MODE_OPEN) {
            listView.setVisibility(View.VISIBLE);
            LinearLayout search = (LinearLayout) view.findViewById(R.id.munkalap_search_containter);
            search.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            mSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    filterData();
                }
            });
        }
        mMunkalapList = loadData(""); // betoltjuk az osszes rekordot, mivel ebbol a tipusbol csak nehany tucat van.
        partnerkodList = new ArrayList<String>();
        for (Munkalap munkalap : mMunkalapList) {
            partnerkodList.add(munkalap.partnerkod);
        }
        filterData();
        return view;
    }

    private void filterData() {
        listView.setVisibility(View.VISIBLE);
        mFilteredMunkalapList.clear();
        for (Munkalap munkalap : mMunkalapList) {
            if (StringUtils.clearText(munkalap.getPartner().getNev()).startsWith(StringUtils.clearText(mSearchField.getText().toString()))) {
                mFilteredMunkalapList.add(munkalap);
            }
        }
        mMunkalapArrayAdapter.notifyDataSetChanged();
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
        if(mMunkalapArrayAdapter != null && mMunkalapArrayAdapter.checkMunkalapTask != null){
            mMunkalapArrayAdapter.checkMunkalapTask.cancel(true);
            mMunkalapArrayAdapter.checkMunkalapTask = null;
        }
    }

    private List<Munkalap> loadData(String searchText) {
        List<Munkalap> result = new ArrayList<Munkalap>();
        if (mKiteOrm == null && getContext()!=null) {
            mKiteOrm = new KiteORM(getActivity());
        }
        switch (mListener.getMode()) {
            case MunkalapActivity.MODE_CONTINUE:
                result = KiteDAO.getUnfinishedMunkalapList(getContext(), searchText);
                break;
            case MunkalapActivity.MODE_COPY:
                result = KiteDAO.getPreviousMunkalapList(getContext(), searchText);
                break;
            case MunkalapActivity.MODE_OPEN:
                result = KiteDAO.getOpenMunkalapList(getContext());
                break;
            default:
                break;
        }
        return result;
    }

    public void reload() {
        mMunkalapList = loadData("");
        filterData();
    }


    @Override
    public void onPause() {
        KiteORM.closeCursor(partnerCursorAdapter);
        super.onPause();
    }
}
