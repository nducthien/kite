package hu.itware.kite.service.interfaces;

import java.util.HashMap;

import hu.itware.kite.service.orm.model.Gep;

/**
 * Created by langa on 2015.10.12..
 */
public interface MachineFragmentInterface {
    Gep getMachine();
    void setMachine(Gep machine);
    void nextPage();
    void previousPage();
    void setFilter(String filter);
    String getFilter();
    void setFilters(HashMap<String, String> filters);
    HashMap<String, String> getFilters();
    String getSelectedPartnerkod();
    void setSelectedPartnerkod(String partnerkod);
}
