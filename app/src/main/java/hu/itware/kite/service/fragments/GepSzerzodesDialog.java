package hu.itware.kite.service.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.adapters.GepSzerzodesArrayAdapter;
import hu.itware.kite.service.orm.model.GepSzerzodes;

/**
 * Created by szeibert on 2017.06.20..
 */

public class GepSzerzodesDialog extends DialogFragment {

    List<GepSzerzodes> items;
    GepSzerzodesArrayAdapter adapter;
    ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gep_szerzodes, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mListView = (ListView) view.findViewById(R.id.contract_list);
        adapter = new GepSzerzodesArrayAdapter(getActivity(), R.layout.list_item_contract_machine, items);
        mListView.setAdapter(adapter);

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString("title"));

        Button buttonOk = (Button) view.findViewById(R.id.dialog_button_right);
        buttonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public void setItems(List<GepSzerzodes> items) {
        this.items = items;
    }
}
