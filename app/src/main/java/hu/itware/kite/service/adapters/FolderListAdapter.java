package hu.itware.kite.service.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hu.itware.kite.service.R;

/**
 * Created by langa on 2015.09.30..
 */
public class FolderListAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> folderNames;

    public FolderListAdapter(Context context, List<String> objects) {
        super(context, R.layout.list_item_folder_name, objects);
        this.context = context;
        this.folderNames = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_folder_name,parent,false);
        }

        TextView tvFolderName = (TextView) convertView.findViewById(R.id.textview_producttype_name);
        tvFolderName.setText(folderNames.get(position));
        convertView.setTag(folderNames.get(position));
        return convertView;
    }
}
