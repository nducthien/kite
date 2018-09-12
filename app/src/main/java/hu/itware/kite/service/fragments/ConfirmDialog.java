package hu.itware.kite.service.fragments;

import hu.itware.kite.service.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConfirmDialog extends DialogFragment {

	private IDialogResult listener;

	LinearLayout llCB;

	public ConfirmDialog() {
	}

	public void setListener(IDialogResult listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.dialog_confirm, container);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCancelable(false);

		TextView title = (TextView) view.findViewById(R.id.dialog_title);
		title.setText(getArguments().getString("title"));

		TextView text = (TextView) view.findViewById(R.id.dialog_text);
		text.setText(getArguments().getString("message"));

		Button buttonOk = (Button) view.findViewById(R.id.dialog_button_right);
		buttonOk.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
		buttonOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					dismiss();
					listener.onOkClicked(ConfirmDialog.this);
				}
			}
		});

		Button buttonCancel = (Button) view.findViewById(R.id.dialog_button_left);
		buttonCancel.setText(getArguments().getString("leftbutton", getString(R.string.dialog_cancel)));
		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				if (listener != null) {
					listener.onCancelClicked(ConfirmDialog.this);
				}
			}
		});

		llCB = (LinearLayout) view.findViewById(R.id.dialog_layout_cb);

		setupClickOutFromEditText(view.findViewById(R.id.parent));

		return view;
	}

	// hide soft input keyboard
	protected void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		try {
			inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
		} catch (Exception e) {
			Log.e("ConfirmDialog", "Exception", e);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	public void setupClickOutFromEditText(View view) {

		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard(getActivity());
					return false;
				}
			});
		}

		// If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupClickOutFromEditText(innerView);
			}
		}
	}

}
