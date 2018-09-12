package hu.itware.kite.service.utils;

import java.util.HashMap;

import hu.itware.kite.service.orm.model.Partner;

public class Global {

  public static final boolean DEBUG = false;

  public static HashMap<String, String> termekFizetesiMod;

  public static boolean SU = false;

  public static Partner selectedPartner;

  private Global() {

  }

  public static HashMap<String, String> getTermekFizetesiMod() {
    if (termekFizetesiMod == null) {
      termekFizetesiMod = new HashMap<String, String>();
    }
    return termekFizetesiMod;
  }

  public static void deleteTermekFizetesiMod() {
    termekFizetesiMod = null;
  }

  public static boolean isInTermekFizetesiMod(String cikkszam, String fizetesiMod) {

    if (cikkszam == null) {
      return false;
    }

    if (getTermekFizetesiMod().get(cikkszam) != null && fizetesiMod.equals(getTermekFizetesiMod().get(cikkszam))) {
        return true;
    }

    return false;
  }

}
