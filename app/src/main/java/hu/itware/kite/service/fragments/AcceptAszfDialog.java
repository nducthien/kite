package hu.itware.kite.service.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Regex;
import com.mobsandgeeks.saripaar.annotation.Required;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.utils.StringUtils;

/**
 * Created by gyongyosit on 2015.09.15..
 */
public class AcceptAszfDialog extends DialogFragment implements Validator.ValidationListener {

    IDialogResult listener;
    String filePath;
	Munkalap munkalap;
	Validator validator;

	//@Required(order = 1, messageResId = R.string.error_required_field)
	private EditText text_szig;

    public AcceptAszfDialog() {
    }

    public void setArguments(IDialogResult listener, String filePath,Munkalap munkalap){
        this.listener = listener;
        this.filePath = filePath;
		this.munkalap = munkalap;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_accept_aszf, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString("title"));

        TextView text = (TextView) view.findViewById(R.id.dialog_text);
        text.setText(getArguments().getString("message"));

		text_szig = (EditText) view.findViewById(R.id.aszf_szig);

		validator = new Validator(this);
		validator.setValidationListener(this);

        WebView webView = (WebView) view.findViewById(R.id.dialog_webview_aszf);
        if(filePath != null){
            webView.loadUrl(filePath);
        }

        Button button_ok = (Button) view.findViewById(R.id.dialog_button_right);
        button_ok.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
        button_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

				validator.validate();
            }
        });

        Button button_cancel = (Button) view.findViewById(R.id.dialog_button_left);
        button_cancel.setText(getArguments().getString("leftbutton", getString(R.string.dialog_cancel)));
        button_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				if (listener != null) {
					listener.onCancelClicked(AcceptAszfDialog.this);
				}
			}
		});

        return view;
    }

	@Override
	public void onValidationSucceeded() {

		if (StringUtils.isSzemlyiIgazolvanySzam(text_szig.getText().toString())) {
			dismiss();
			if (munkalap != null) {
				munkalap.szig = text_szig.getText().toString();
			}

			if (listener != null) {
				listener.onOkClicked(AcceptAszfDialog.this);
			}
		} else {
			text_szig.setError(getString(R.string.error_szig_invalid));
		}
	}

	@Override
	public void onValidationFailed(View view, Rule<?> rule) {
		if (view instanceof EditText) {
			((EditText) view).setError(rule.getFailureMessage());
		}
	}
}
