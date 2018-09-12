package android.support.v4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Azert kell ez igy, mert a ViewPager-ben el van rontva a smoothScrollTo() metodus, ami amitt ugral setCurrentItem animacioja.
 * Mivel a smoothScrollT() package-private, ezert az android.support.v4.view csomagban kell lenni ennek az osztalynak ahhoz,
 * hogy override-olni tudja.
 *
 * @author szeibert
 */
public class FixedViewPager extends ViewPager {

    private boolean pagingEnabled;

    public FixedViewPager(Context context) {
        super(context);
    }

    public FixedViewPager(Context context, AttributeSet attr) {
        super(context, attr);
    }

    void smoothScrollTo(int x, int y, int velocity) {
        // animacio sebesseg bugfix
        super.smoothScrollTo(x, y, 1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.pagingEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.pagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return false;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return false;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public void setPagingEnabled(boolean enabled) {
        this.pagingEnabled = enabled;
    }
}