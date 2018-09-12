package hu.itware.kite.service.interfaces;

import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.Photo;

/**
 * Created by szeibert on 2015.09.22..
 */
public interface MunkalapFragmentInterface {
    void nextPage();
    void previousPage();
    Class getCurrentPage();
    void setPartner(Partner partner);
    void setGep(Gep machine, boolean forceNextPage);
    void setMunkalap(Munkalap munkalap);
    Partner getPartner();
    Gep getGep();
    Munkalap getMunkalap();
    int getMode();
    void setCurrentPhoto(Photo photo);
    void checkAllapotkod();
    void deleteMunkalap(Munkalap munkalap);
    void reload();
}
