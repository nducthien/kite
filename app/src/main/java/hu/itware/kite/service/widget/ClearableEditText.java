package hu.itware.kite.service.widget;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import hu.itware.kite.service.R;

/**
 * Created by szeibert on 2017.06.09..
 */

public class ClearableEditText extends LinearLayout {

    private EditText text;
    private ImageView icon;

    private boolean enabled;

    public ClearableEditText(Context context) {
        super(context);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout clearableEditTextView = (LinearLayout) inflater.inflate(R.layout.clearable_edittext, this, false);
        addView(clearableEditTextView);

        // ID alapjan kereses erdekes dolgokat eredmenyezetett a DoubleCheckBoxView-ban, ugyhogy itt is inkabb index alapjan keresunk
        text = (EditText) clearableEditTextView.getChildAt(0);
        icon = (ImageView) clearableEditTextView.getChildAt(1);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!enabled) {
                    return;
                }
                text.setText("");
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        text.setEnabled(enabled);
        text.setClickable(enabled);
        text.setFocusable(enabled);
        icon.setClickable(enabled);
        icon.setFocusable(enabled);
    }

    public EditText getEditText() {
        return text;
    }

    public Editable getText() {
        return text.getText();
    }

    public void setText(String value) {
        text.setText(value);
    }

    public void setText(int resId) {
        text.setText(resId);
    }

    public void setError(CharSequence error) {
        text.setError(error);
    }


}
