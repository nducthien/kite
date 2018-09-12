package hu.itware.kite.service.interfaces;

import hu.itware.kite.service.orm.model.Partner;

/**
 * Created by szeibert on 2015.10.07..
 */
public interface PartnerFragmentInterface {
    Partner getPartner();
    void setPartner(Partner partner);
    void nextPage();
    void previousPage();
    int getMode();
}
