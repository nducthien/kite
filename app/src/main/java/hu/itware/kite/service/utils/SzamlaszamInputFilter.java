package hu.itware.kite.service.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by szeibert on 2016.01.04..
 */
public class SzamlaszamInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source,
                               int start,
                               int end,
                               Spanned dest,
                               int dstart,
                               int dend) {
        Pattern pattern;
        Matcher matcher;
        if (source.length() > 1) { // autocomplete
            pattern = Pattern.compile("[0-9a-zA-ZáéöüóőúűíÁÉÖÜÓŐÚŰÍ]{2}/[0-9a-zA-ZáéöüóőúűíÁÉÖÜÓŐÚŰÍ]{5}");
            matcher = pattern.matcher(source);
            if (matcher.matches()) {
                return null;
            }
        }
        pattern = Pattern.compile("[0-9a-zA-ZáéöüóőúűíÁÉÖÜÓŐÚŰÍ/]"); // gepeles
        matcher = pattern.matcher(source);
        if (!matcher.matches()) {
            return "";
        }
        if ("/".equals(source) && dest.length() != 2) {
            return "";
        }
        if (dest.length() >= 8) {
            return "";
        }
        if (dest.length() == 1 && end > start) {
            return source.toString() + "/";
        }
        if ((dest.length() == 2 && !"/".equals(source)) && end > start) {
            return "/" + source;
        }
        return null;
    }
}
