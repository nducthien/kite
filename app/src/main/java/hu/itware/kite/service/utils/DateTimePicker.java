package hu.itware.kite.service.utils;

import java.util.Calendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.widget.DatePicker;

public class DateTimePicker extends Dialog {

	private OnDateTimeSetListener callback;
	private Context context;
	private long currentDate;

	// workaround for a bug that onDateSet and onTimeSet are called twice by
	// default.
	private boolean isDateSet = false;

	/**
	 * Initializes the datum time picker
	 * 
	 * @param context
	 * @param callback
	 */
	public DateTimePicker(long currentDate, Context context, OnDateTimeSetListener callback) {
		super(context);
		this.callback = callback;
		this.context = context;
		this.currentDate = currentDate;
	}

	/**
	 * Shows a datum picker with current datum as default
	 */
	private void showDatePicker() {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTimeInMillis(currentDate);
		new DatePickerDialog(context, new DateListener(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
	}

	/**
	 * Custom onDateSetListener that calls the callback's onDateSet and displays
	 * the TimePicker afterwards
	 * 
	 * @author korsosa
	 * 
	 */
	private class DateListener implements android.app.DatePickerDialog.OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// onDateSet is called twice by default, so we make sure it is
			// called only once
			if (!isDateSet) {
				isDateSet = true;
				callback.onDateSet(view, year, monthOfYear, dayOfMonth);
			}
		}

	}

	/**
	 * Shows the dialog, datum picker first
	 */
	@Override
	public void show() {
		showDatePicker();
	}

}
