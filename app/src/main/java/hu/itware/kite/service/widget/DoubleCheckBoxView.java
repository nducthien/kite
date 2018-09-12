package hu.itware.kite.service.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import hu.itware.kite.service.R;

/**
 * Created by szeibert on 2015.11.19..
 */
public class DoubleCheckBoxView extends LinearLayout {

    private CheckBox positiveCheckBox;
    private CheckBox negativeCheckBox;

    private OnStateChangedListener mListener;

    private String positiveFlag;
    private String negativeFlag;

    private String positiveLabel;
    private String negativeLabel;

    public DoubleCheckBoxView(Context context) {
        super(context);
    }

    public DoubleCheckBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DoubleCheckBoxView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout doubleCheckBoxView = (LinearLayout) inflater.inflate(R.layout.double_checkbox, this, false);
        addView(doubleCheckBoxView);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DoubleCheckBoxView);
        positiveLabel = typedArray.getString(R.styleable.DoubleCheckBoxView_positive_label);
        negativeLabel = typedArray.getString(R.styleable.DoubleCheckBoxView_negative_label);
        positiveFlag = typedArray.getString(R.styleable.DoubleCheckBoxView_positive_flag);
        if (positiveFlag == null || "".equals(positiveFlag)) {
            positiveFlag = "I";
        }
        negativeFlag = typedArray.getString(R.styleable.DoubleCheckBoxView_negative_flag);
        if (negativeFlag == null || "".equals(negativeFlag)) {
            negativeFlag = "N";
        }


        // Ha ID alapjan keresem, akkor bizonyos esetekben osszeakadnak a kulonbozo DoubleCheckBoxView instance-ok, es a kepernyon a legutolso felulirja az elozoeket.
        // Az egy NAGYON erdekes kerdes, hogy ez egyaltalan hogyan lehetseges, mindenesetre ez a szitu.
        negativeCheckBox = (CheckBox) doubleCheckBoxView.getChildAt(0);
        positiveCheckBox = (CheckBox) doubleCheckBoxView.getChildAt(1);

        if (positiveLabel != null && !"".equals(positiveLabel)) {
            positiveCheckBox.setText(positiveLabel);
        }
        if (negativeLabel != null && !"".equals(negativeLabel)) {
            negativeCheckBox.setText(negativeLabel);
        }

        positiveCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    negativeCheckBox.setChecked(false);
                    positiveCheckBox.setError(null);
                    if (mListener != null) {
                        mListener.onStateChanged(getState());
                    }
                }
            }
        });

        negativeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    positiveCheckBox.setChecked(false);
                    positiveCheckBox.setError(null);
                    if (mListener != null) {
                        mListener.onStateChanged(getState());
                    }
                }
            }
        });
        typedArray.recycle();
    }

    public void setState(String state) {
        if (state == null) {
            state = "";
        }
        if (positiveFlag.equalsIgnoreCase(state)) {
            positiveCheckBox.setChecked(true);
            negativeCheckBox.setChecked(false);
        } else if (negativeFlag.equalsIgnoreCase(state)) {
            positiveCheckBox.setChecked(false);
            negativeCheckBox.setChecked(true);
        } else {
            positiveCheckBox.setChecked(false);
            negativeCheckBox.setChecked(false);
        }
        positiveCheckBox.setError(null);
    }

    public String getState() {
        String state = null;
        if (positiveCheckBox.isChecked()) {
            state = positiveFlag;
        } else if (negativeCheckBox.isChecked()) {
            state = negativeFlag;
        }
        return state;
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mListener = listener;
    }

    public interface OnStateChangedListener {
        void onStateChanged(String state);
    }

    public void setError(CharSequence error) {
        positiveCheckBox.setError(error);
    }

    public void setEnabled(boolean enabled) {
        positiveCheckBox.setEnabled(enabled);
        negativeCheckBox.setEnabled(enabled);
    }
}
