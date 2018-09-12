package hu.itware.kite.service.fragments;

import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

/**
 * Created by szeibert on 2016.02.09..
 */
public class CustomDialog extends DialogFragment {

    public void show(int id) {
        View v = getView().findViewById(id);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public void hide(int id) {
        View v = getView().findViewById(id);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }

    public void setText(int id, int text) {
        TextView v = (TextView) getView().findViewById(id);
        if (v != null) {
            v.setText(text);
        }
    }

    public void setText(int id, String text) {
        TextView v = (TextView) getView().findViewById(id);
        if (v != null) {
            v.setText(text);
        }
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
