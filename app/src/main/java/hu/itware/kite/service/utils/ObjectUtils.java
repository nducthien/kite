package hu.itware.kite.service.utils;

/**
 * Created by nagyg on 2016.01.12..
 */
public final class ObjectUtils {
    private ObjectUtils() {

    }

    public static boolean nullSafeEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
