package hu.itware.kite.service.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by szeibert on 2016.01.04..
 */
public class NoEnterInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source,
                               int start,
                               int end,
                               Spanned dest,
                               int dstart,
                               int dend) {
        if (source.toString().contains("\n")) {
            return "";
        } else {
            return null;
        }
    }
}
