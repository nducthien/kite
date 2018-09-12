package hu.itware.kite.service.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.GepActivity;
import hu.itware.kite.service.adapters.MachineArrayAdapter;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.PartnerFragmentInterface;

/**
 * A simple {@link Fragment} subclass.
 */
public class PartnerMachinesFragment extends Fragment implements IRefreshable {

    private TextView header;
    private ListView machinesListView;
    private MachineArrayAdapter mMachineArrayAdapter;
    private PartnerFragmentInterface mListener;

    public PartnerMachinesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_partner_machines, container, false);
        header = (TextView) view.findViewById(R.id.partner_header);
        machinesListView = (ListView) view.findViewById(R.id.partner_lv_machines);

        refresh();
        return view;
    }


    @Override
    public void refresh() {
        if (mListener != null && mListener.getPartner() != null) {
            mMachineArrayAdapter = new MachineArrayAdapter(getActivity(), R.layout.list_item_partner_machine, mListener.getPartner().getGepek(true));
            machinesListView.setAdapter(mMachineArrayAdapter);
            machinesListView.invalidate();
            machinesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getActivity(), GepActivity.class);
                    intent.putExtra("alvazszam", mMachineArrayAdapter.getItem(i).alvazszam);
                    getActivity().startActivity(intent);
                }
            });
            header.setText(mListener.getPartner().getNev());
        }
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
