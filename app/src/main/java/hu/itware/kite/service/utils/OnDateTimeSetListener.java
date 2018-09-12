package hu.itware.kite.service.utils;

import android.app.DatePickerDialog.OnDateSetListener;
import android.widget.DatePicker;

public abstract class OnDateTimeSetListener implements OnDateSetListener {

	@Override
	public abstract void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth);

}
