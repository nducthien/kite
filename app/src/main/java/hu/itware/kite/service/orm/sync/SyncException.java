package hu.itware.kite.service.orm.sync;

/**
 * Created by szeibert on 2016.02.09..
 */
public class SyncException extends Exception {
    SyncException(String message) {
        super(message);
    }

    SyncException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
