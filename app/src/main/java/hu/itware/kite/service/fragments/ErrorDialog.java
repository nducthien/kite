package hu.itware.kite.service.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import hu.itware.kite.service.R;

public class ErrorDialog extends CustomDialog {

    public static final int INFO = 0;
    public static final int ERROR = 1;
    public static final int QUESTION = 2;
    public static final int PROGRESS = 3;
    public static final int PASSWORD = 4;

    private IDialogResult listener;

    public ErrorDialog() {

    }

    public void setListener(IDialogResult listener) {
        this.listener = listener;
    }

    public void setDialogResultListener(IDialogResult listener) {

    }

    private String password;

    private int type = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        type = getArguments().getInt("type", INFO);

        View view = inflater.inflate(R.layout.dialog_error, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);
        setCancelable(false);

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString("title"));

        TextView text = (TextView) view.findViewById(R.id.dialog_text);
        text.setText(getArguments().getString("message"));

        final EditText textPassword = (EditText) view.findViewById(R.id.dialog_password);

        Button buttonOk = (Button) view.findViewById(R.id.dialog_button_right);
        buttonOk.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
        buttonOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (textPassword != null) {
                    password = textPassword.getText().toString();
                }
                dismiss();
                if (listener != null) {
                    listener.onOkClicked(ErrorDialog.this);
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
                    listener.onCancelClicked(ErrorDialog.this);
                }
            }
        });

        switch (type) {
            case INFO:
                setVisible(view, R.id.dialog_layout_check, false);
                setVisible(view, R.id.dialog_layout_loading, false);
                setVisible(view, R.id.dialog_progress, false);
                setVisible(view, R.id.dialog_button_left, false);
                setVisible(view, R.id.dialog_layout_password, false);
                break;

            case ERROR:
                setVisible(view, R.id.dialog_layout_check, false);
                setVisible(view, R.id.dialog_layout_loading, false);
                setVisible(view, R.id.dialog_progress, false);
                setVisible(view, R.id.dialog_button_left, false);
                setVisible(view, R.id.dialog_layout_password, false);
                break;

            case PROGRESS:
                setVisible(view, R.id.dialog_layout_check, false);
                setVisible(view, R.id.dialog_layout_loading, false);
                setVisible(view, R.id.dialog_button_left, false);
                setVisible(view, R.id.dialog_button_right, false);
                setVisible(view, R.id.dialog_layout_password, false);
                break;

            case QUESTION:
                setVisible(view, R.id.dialog_layout_check, false);
                setVisible(view, R.id.dialog_layout_loading, false);
                setVisible(view, R.id.dialog_progress, false);
                setVisible(view, R.id.dialog_layout_password, false);

                break;

            case PASSWORD:
                setVisible(view, R.id.dialog_layout_check, false);
                setVisible(view, R.id.dialog_layout_loading, false);
                setVisible(view, R.id.dialog_progress, false);
                setVisible(view, R.id.dialog_layout_password, true);
                break;
        }

        return view;
    }

    public String getPassword() {
        return password;
    }

    public int getType() {
        return type;
    }

    public void setColor(View root, int id, int color) {
        TextView v = (TextView) root.findViewById(id);
        if (v != null) {
            v.setTextColor(color);
        }
    }

    public void setBackground(View root, int id, int color) {
        View v = root.findViewById(id);
        if (v != null) {
            v.setBackgroundColor(color);
        }
    }

    public void setVisible(View root, int id, boolean visible) {
        View v = root.findViewById(id);
        if (v != null) {
            v.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}