package hu.itware.kite.service.fragments;

import android.support.v4.app.DialogFragment;

public interface IDialogResult {

	public void onOkClicked(DialogFragment dialog);
	
	public void onCancelClicked(DialogFragment dialog);
	
}
