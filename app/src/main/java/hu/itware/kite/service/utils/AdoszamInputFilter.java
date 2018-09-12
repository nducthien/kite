package hu.itware.kite.service.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by szeibert on 2016.01.04..
 */
public class AdoszamInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source,
                               int start,
                               int end,
                               Spanned dest,
                               int dstart,
                               int dend) {
        Pattern pattern = Pattern.compile("[0-9\\-]");
        Matcher matcher = pattern.matcher(source);
        if (!matcher.matches()) {
            return "";
        }
        if ("-".equals(source) && dest.length() != 8 && dest.length() != 10) {
            return "";
        }
        if (dest.length() >= 13) {
            return "";
        }
        if ((dest.length() == 7 || dest.length() == 9) && end > start) {
            return source.toString() + "-";
        }
        if (((dest.length() == 8 || dest.length() == 10) && !"-".equals(source)) && end > start) {
            return "-" + source;
        }
        return null;
    }
}
