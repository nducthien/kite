package hu.itware.kite.service.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import hu.itware.kite.service.R;

public class KiteSpinnerAdapter<T> extends ArrayAdapter<T> {

	private T[] objects;
	private LayoutInflater inflater;
	private int textViewResourceId;

	public KiteSpinnerAdapter(Context context, int textViewResourceId, T[] objects) {
		super(context, textViewResourceId, objects);
		this.objects = objects;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View row = inflater.inflate(textViewResourceId, parent, false);
		TextView label = (TextView) row.findViewById(R.id.spinner_text);
		label.setText("" + objects[position]);
		return row;
	}

	public void update(T [] objects) {
		this.objects = objects;
		notifyDataSetChanged();
	}
}
