package hu.itware.kite.service.enums;

/**
 * Created by gyongyosit on 2015.09.16..
 */
public enum DialogType {

    LOGIN_DIALOG(1), SYNC_DIALOG(2), MAIN_MENU_DIALOG(0), LOGOUT_DIALOG(3),
    DELETE_PRODUCT_ROW_DIALOG(4), SELECT_PAY_METHOD_DIALOG(5), INFO_DIALOG(6), DELETE_CART_DIALOG(7), CLOSE_ROW_DIALOG(8), ACCEPT_ASZF_DIALOG(9),
    FILTER_DIALOG(10), SET_COMMON_DISCOUNT_DIALOG(11), ALKATRESZ_HOZZAADAS_DIALOG(12);

    private int code;

    DialogType(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
