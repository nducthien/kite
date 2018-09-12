package hu.itware.kite.service.orm.model;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.model.annotations.Key;

@SuppressWarnings("serial")
public class GepSzerzodesTetel extends BaseDatabaseObject {

	@Key
	public String tetelkod;
	
	@Key
	public String szerzodeskod;
	
	public String alvazszam;
	
	public String gepazonosito;
	
	public String partnerkod;

	public Gep getGep() {
		KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
		return kiteORM.loadSingle(Gep.class, GepekTable.COL_ALVAZSZAM + " = ? ", new String[]{alvazszam});
	}
	
}
