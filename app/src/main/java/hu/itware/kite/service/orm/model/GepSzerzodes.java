package hu.itware.kite.service.orm.model;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.annotations.Key;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class GepSzerzodes extends BaseDatabaseObject {

	@Key
	public String szerzodeskod;

	public String szerzodestipus;
	
	public Date kezdesdatum;
	
	public Date lejaratdatum;
	
	public String ellenorizve1;
	
	public String ellenorizve2;

	//--- KITE-796
	public String partnerkod;

	public Date letrehozasdatum;

	public String email;

	private Partner partner;

	private List<GepSzerzodesTetel> tetelek;

	public List<GepSzerzodesTetel> getTetelek() {
		KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
		if (tetelek == null) {
			tetelek = kiteORM.list(GepSzerzodesTetel.class, "szerzodeskod = ?", new String[]{szerzodeskod});
			if (tetelek == null) {
				tetelek = new ArrayList<GepSzerzodesTetel>();
			}
		}
		return tetelek;
	}

	public Partner getPartner() {
		if (partner == null) {
			KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
			partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod!=null?partnerkod:""});
			if (partner == null) {
				partner = new Partner();
			}
		}
		return partner;
	}

	@Override
	public String toString() {
		return "GepSzerzodes{" +
				"szerzodeskod='" + szerzodeskod + '\'' +
				", szerzodestipus='" + szerzodestipus + '\'' +
				", kezdesdatum=" + kezdesdatum +
				", lejaratdatum=" + lejaratdatum +
				", ellenorizve1='" + ellenorizve1 + '\'' +
				", ellenorizve2='" + ellenorizve2 + '\'' +
				", partnerkod='" + partnerkod + '\'' +
				", letrehozasdatum=" + letrehozasdatum +
				", email='" + email + '\'' +
				", partner=" + partner +
				", tetelek=" + tetelek +
				'}';
	}
}
