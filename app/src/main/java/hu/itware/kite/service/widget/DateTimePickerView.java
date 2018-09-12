package hu.itware.kite.service.widget;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.orm.utils.DateUtils;
import hu.itware.kite.service.utils.DateTimePicker;
import hu.itware.kite.service.utils.OnDateTimeSetListener;

/**
 * Created by szeibert on 2015.09.23..
 */
public class DateTimePickerView extends LinearLayout {

    private static final int TYPE_DATE = 0;
    private static final int TYPE_TIME = 1;

    private EditText text;
    private ImageView icon;
    private Calendar calendar;
    private Date date;

    private boolean enabled;
    private int pickerType;

    private OnDateChangedListener mListener;

    public DateTimePickerView(Context context) {
        super(context);
    }

    public DateTimePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DateTimePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        calendar = GregorianCalendar.getInstance();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout dateTimePickerView = (LinearLayout) inflater.inflate(R.layout.date_time_picker, this, false);
        addView(dateTimePickerView);

        // ID alapjan kereses erdekes dolgokat eredmenyezetett a DoubleCheckBoxView-ban, ugyhogy itt is inkabb index alapjan keresunk
        text = (EditText) dateTimePickerView.getChildAt(0);
        icon = (ImageView) dateTimePickerView.getChildAt(1);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DateTimePickerView);
        pickerType = typedArray.getInt(R.styleable.DateTimePickerView_picker_type, 0);
        if (pickerType == 1) {
            icon.setImageResource(R.drawable.ic_action_time);
            text.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        } else {
            icon.setImageResource(R.drawable.ic_date);
            text.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        }
        setEnabled(typedArray.getBoolean(R.styleable.DateTimePickerView_picker_enabled, true));

        if (!typedArray.getBoolean(R.styleable.DateTimePickerView_show_icon, true)) {
            icon.setVisibility(GONE);
        }
        typedArray.recycle();

        icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!enabled) {
                    return;
                }
                try {
                    try {
                        if (pickerType == 1) {
                            date = BaseActivity.getSdfTime().parse(text.getText().toString());
                        } else {
                            date = BaseActivity.getSdfShort().parse(text.getText().toString());
                        }
                    } catch (ParseException e) {
                        date = new Date();
                    }
                    calendar.setTime(date);
                    if (pickerType == 1) {
                        TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                calendar.setTime(new Date());
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);
                                text.setText(BaseActivity.getSdfTime().format(calendar.getTime()));
                                text.setError(null);
                                date = calendar.getTime();
                                if (mListener != null) {
                                    mListener.onDateChanged(calendar);
                                }
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                        dialog.show();
                    } else {
                        DateTimePicker dialog = new DateTimePicker(date.getTime(),
                                getContext(), new OnDateTimeSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                                text.setText(BaseActivity.getSdfShort().format(calendar.getTimeInMillis()));
                                text.setError(null);
                                date = calendar.getTime();
                                if (mListener != null) {
                                    mListener.onDateChanged(calendar);
                                }
                            }
                        });
                        dialog.show();
                    }
                } catch (Exception e) {
                    Log.e("DateTimePickerView", "Could not create view", e);
                }
            }
        });
    }

    public Date getDate() {
        return date;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setDate(Date date) {
        this.date = date;
        if (pickerType == TYPE_DATE) {
            text.setError(null);
            if (date != null) {
                calendar.setTime(date);
                text.setText(BaseActivity.getSdfShort().format(calendar.getTimeInMillis()));
            } else {
                text.setText("");
            }
        } else {
            setTime(date);
        }
    }

    public void setTime(Date date) {
        this.date = date;
        if (pickerType == TYPE_TIME) {
            text.setError(null);
            if (date != null) {
                calendar.setTime(date);
                text.setText(DateUtils.getDfOnlyTime().format(calendar.getTimeInMillis()));
            } else {
                text.setText("");
            }
        } else {
            setDate(date);
        }
    }

    public String getDateTimeString() {
        return text.getText().toString();
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

    public void setListener(OnDateChangedListener listener) {
        mListener = listener;
    }

    public interface OnDateChangedListener {
        void onDateChanged(Calendar calendar);
    }

    public void setError(CharSequence error) {
        text.setError(error);
    }
}
