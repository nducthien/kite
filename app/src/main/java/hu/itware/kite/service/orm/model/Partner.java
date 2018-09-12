package hu.itware.kite.service.orm.model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.AlkozpontokTable;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesekTable;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.model.annotations.JSON;
import hu.itware.kite.service.orm.model.annotations.Key;
import hu.itware.kite.service.orm.model.json.PartnerKapcsolat;
import hu.itware.kite.service.orm.model.json.PartnerTelephely;
import hu.itware.kite.service.orm.utils.GSON;
import hu.itware.kite.service.utils.StringUtils;

/**
 *
 * @author batorig
 */
@SuppressWarnings("serial")
public class Partner extends BaseDatabaseObject implements Serializable {

	public static final int PARTNER_NAME_LENGTH = 30;

	@Key
	public String partnerkod;

	@Key
	public String tempkod;

	@JSON(type = String.class)
	public String uzletkotok;

	public String alkozpontkod;

	public String nev1;
	public String nev2;
	public String irsz;
	public String telepules;
	public String cim;
	public String telefonszam;
	public String email;
	public String adoszam;
	public String adoazonosito;
	public String minosites;
	public Long limit1;
	public Long limit2;
	public Long limit3;
	public Date minositesdatuma;
	public String szervizeskod;
	public String fizetokeszseg;
	public String fax;
	public String baj;

	@JSON(type = String.class)
	public String partnerkapcsolatok;

	@JSON(type = String.class)
	public String partnertelephelyek;

	public String searchNev;
	public String searchCim;

	public List<GepSzerzodes> contractList;

	public transient Uzletkoto uzletkoto;
	public transient Alkozpont alkozpont;

	public transient List<PartnerKapcsolat> kapcsolattartokData = null;
	public transient List<PartnerTelephely> telephelyekData = null;
	public List<String> uzletkotokData = null;

	private transient List<Gep> gepek = null;

	public String getAddress() {
		return this.irsz + " " + this.telepules + " " + this.cim;
	}

	@Override
	public void beforeSave() {

		if (kapcsolattartokData != null) {
			partnerkapcsolatok = GSON.toJson(kapcsolattartokData);
		}
		if (telephelyekData != null) {
			partnertelephelyek = GSON.toJson(telephelyekData);
		}
		if (uzletkotokData != null) {
			uzletkotok = GSON.toJson(uzletkotokData);
		}
	}

	@Override
	public void afterLoad() {
		if (partnerkapcsolatok != null) {
			kapcsolattartokData = GSON.toList(partnerkapcsolatok, PartnerKapcsolat.class);
		}

		if (partnertelephelyek != null) {
			telephelyekData = GSON.toList(partnertelephelyek, PartnerTelephely.class);
		}

		if (uzletkotok != null) {
			uzletkotokData = GSON.toList(uzletkotok, String.class);
		}
	}

	public String getNev() {

		if (StringUtils.isEmpty(this.nev2)) {
			return this.nev1!=null?this.nev1:"";
		}
		return this.nev1 + " " + this.nev2;
	}


	public List<Gep> getGepek(boolean forceReload) {
		if (gepek == null || forceReload) {
			KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
			if (partnerkod != null) {
				gepek = kiteORM.list(Gep.class, GepekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod});
			} else if (tempkod != null) {
				gepek = kiteORM.list(Gep.class, GepekTable.COL_TEMPPARTNERKOD + " = ?", new String[]{tempkod});
			}
			if (gepek == null) {
				gepek = new ArrayList<Gep>();
			}
		}
		return gepek;
	}

	public Munkalap getLastMunkalap() {
		KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
		if (partnerkod != null) {
			return kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_PARTNERKOD + " = ? ORDER BY " + MunkalapokTable.COL_LETREHOZASDATUM + " DESC", new String[]{partnerkod});
		} else if (tempkod != null) {
			return kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_TEMPPARTNERKOD + " = ? ORDER BY " + MunkalapokTable.COL_LETREHOZASDATUM + " DESC", new String[]{tempkod});
		}
		return null;
	}

	public void setGepek(List<Gep> gepek) {
		this.gepek = gepek;
	}

	public String getPartnerkod() {
		if (partnerkod != null && !partnerkod.isEmpty()) {
			return partnerkod;
		}
		return tempkod;
	}

	public List<GepSzerzodes> getPartnerContracts() {
		if (contractList == null) {
			Log.e("Partner", "getPartnerContracts: " + partnerkod);
			contractList = new ArrayList<>();
			KiteORM kiteORM = KiteApplication.getKiteORM();
			List<GepSzerzodes> contracts = null;
			if (partnerkod != null) {
				contracts = kiteORM.list(GepSzerzodes.class, GepSzerzodesekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod});
			}
			if (contracts == null) {
				return contractList;
			}

			for (GepSzerzodes contract : contracts) {
				if (contract != null && contract.kezdesdatum != null && contract.lejaratdatum != null) {
					Calendar start = Calendar.getInstance();
					start.setTime(contract.kezdesdatum);
					Calendar end = Calendar.getInstance();
					end.setTime(contract.lejaratdatum);
					Calendar now = Calendar.getInstance();
					now.setTime(new Date());
					if (now.after(start) && now.before(end) && contract.ellenorizve1 != null && !"".equals(contract.ellenorizve1) && contract.ellenorizve2 != null && !"".equals(contract.ellenorizve2)) {
						contractList.add(contract);
					}
				}
			}
		}
		return contractList;
	}

	public Alkozpont getAlkozpont() {
		if (alkozpont == null) {
			KiteORM kiteORM = KiteApplication.getKiteORM();
			alkozpont = kiteORM.loadSingle(Alkozpont.class, AlkozpontokTable.COL_ALKOZPONTKOD + " = ?", new String[]{alkozpontkod});
		}
		return alkozpont;
	}

	@Override
	public String toString() {
		return this.getNev() + " " + getAddress() + " " + this.adoszam;
	}
}