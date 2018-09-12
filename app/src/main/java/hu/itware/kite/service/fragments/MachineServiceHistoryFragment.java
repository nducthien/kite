package hu.itware.kite.service.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.MachineDetailsActivity;
import hu.itware.kite.service.adapters.MunkalapArrayAdapter;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MachineFragmentInterface;
import hu.itware.kite.service.orm.model.Munkalap;

/**
 * A simple {@link Fragment} subclass.
 */
public class MachineServiceHistoryFragment extends Fragment implements IRefreshable {


    private ListView listView;
    private MunkalapArrayAdapter mMunkalapArrayAdapter;
    private MachineFragmentInterface mListener;
    private List<Munkalap> mMunkalapok = new ArrayList<Munkalap>();

    public MachineServiceHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_machine_service_history, container, false);
        listView = (ListView) view.findViewById(R.id.munkalap_listview);
        mMunkalapArrayAdapter = new MunkalapArrayAdapter(getActivity(), R.layout.list_item_munkalap, new ArrayList<Munkalap>());
        listView.setAdapter(mMunkalapArrayAdapter);
        return view;
    }


    @Override
    public void refresh() {
        if (mListener != null) {
            new AsyncTask<Void, Void, List<Munkalap>>(){
                @Override
                protected List<Munkalap> doInBackground(Void... params) {
                    List<Munkalap> munkalapList = new ArrayList<>();
                    if (mListener != null) {
                        Log.e("GEP", mListener.getSelectedPartnerkod() != null ? mListener.getSelectedPartnerkod() : "null");
                        munkalapList = mListener.getMachine().getMunkalapList(mListener.getSelectedPartnerkod(), true);
                    }
                    return munkalapList;
                }

                @Override
                protected void onPostExecute(List<Munkalap> munkalaps) {
                    if (munkalaps != null) {
                        mMunkalapok = munkalaps;
                        if (mMunkalapArrayAdapter != null) {
                            mMunkalapArrayAdapter.clear();
                            mMunkalapArrayAdapter.addAll(mMunkalapok);
                            mMunkalapArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }.execute();
        }
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (MachineFragmentInterface) activity;
            refresh();
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
}
