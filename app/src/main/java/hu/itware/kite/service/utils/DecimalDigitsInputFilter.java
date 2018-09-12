package hu.itware.kite.service.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

/**
 * Input filter that limits the number of decimal digits that are allowed to be
 * entered.
 */
public class DecimalDigitsInputFilter implements InputFilter {

    private final int digits;
    private final int decimalDigits;

    /**
     * Constructor.
     *
     * @param decimalDigits maximum decimal digits
     */
    public DecimalDigitsInputFilter(int digits, int decimalDigits) {
        this.digits = digits;
        this.decimalDigits = decimalDigits;
    }

    @Override
    public CharSequence filter(CharSequence source,
                               int start,
                               int end,
                               Spanned dest,
                               int dstart,
                               int dend) {


		source = source.toString();
        int dotPos = -1;
        int len = dest.length();
        for (int i = 0; i < len; i++) {
            char c = dest.charAt(i);
            if (c == '.' || c == ',') {
                dotPos = i;
                break;
            }
        }

        if (decimalDigits == 0 && (".".equals(source) || ",".equals(source))) {
            return "";
        }
        if (dstart == 0 && (".".equals(source) || ",".equals(source))) {
            return "";
        }
        if (end == start && dotPos == dstart && dest.length()-1 > digits) {
            // ha ki akarja torolni a tizedespontot/vesszot, es tobb jegy maradna, mint a megengedett, akkor nem engedjuk kitorolni
            return dest.toString().substring(dstart, dend); // ezt ugy lehet elerni, hogy visszaadjuk a torles helyen levo karaktert
        }
        if (dotPos >= 0) {
            // protects against many dots
            if (".".equals(source) || ",".equals(source)) {
                return "";
            }
            // if the text is entered before the dot
            if (dend <= dotPos && dotPos < digits) {
                return null;
            }
            if (len - dotPos > decimalDigits) {
                return "";
            }
        } else if (digits <= len && !".".equals(source) && !",".equals(source)) {
            return "";
        }

        return null;
    }

}