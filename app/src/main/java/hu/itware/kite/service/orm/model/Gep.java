package hu.itware.kite.service.orm.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesTetelekTable;
import hu.itware.kite.service.orm.database.tables.GepSzerzodesekTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.annotations.Key;
import hu.itware.kite.service.orm.utils.DateUtils;

/**
 * Created by szeibert on 2015.09.14..
 */
public class Gep extends BaseDatabaseObject {

    @Key
    public String alvazszam;

	@Key
	public String tempgepkod;

	public String motorszam;

	public String partnerkod;

	public String temppartnerkod;

public String geptipus;
	public String nev;
	public String tipusazonosito;
	public String tipushosszunev;
	public String gyartaseve;
    public Date garanciaervenyesseg;
	public Date uzembehelyezesdatum;
    public String kiadottazonosito;
    public Date kiadasdatum;
    public String szervizes;
    public String baj;

    private Partner partner;
	public String fulkeszam;
	public String hajtomuszam;
	public String mellsohidszam;

    //--- KITE-796
    public Date kjotallas;
    public Double uzemorakorlat;

    private List<GepSzerzodes> gepSzerzodesList;
    private List<Munkalap> munkalapList;

    public Gep() {
    }

    @Override
    public String toString() {
        return "Gep{" +
                "alvazszam='" + alvazszam + '\'' +
                ", garanciaErvenyesseg=" + garanciaervenyesseg +
                ", geptipusHosszuNev='" + tipushosszunev + '\'' +
                ", geptipus='" + geptipus + '\'' +
                ", geptipusAzonosito='" + tipusazonosito + '\'' +
                ", nev='" + nev + '\'' +
                ", gyartasEve='" + gyartaseve + '\'' +
                ", kiadottGepAzonosito='" + kiadottazonosito + '\'' +
                ", kiadasDatuma=" + kiadasdatum +
                ", motorszam='" + motorszam + '\'' +
                ", partnerKod='" + partnerkod + '\'' +
                ", szervizes='" + szervizes + '\'' +
                ", uzembeHelyezesDatuma=" + uzembehelyezesdatum +
                ", baj='" + baj + '\'' +
                ", partner=" + partner +
                '}';
    }

    public Partner getPartner() {
        if (partner == null) {
            KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
            partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod!=null?partnerkod:""});
            if (partner == null) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_TEMPKOD + " = ?", new String[]{temppartnerkod!=null?temppartnerkod:""});
            }
            if (partner == null) {
                partner = new Partner();
            }
        }
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
        partnerkod = partner.partnerkod;
		temppartnerkod = partner.tempkod;
    }

    public List<GepSzerzodes> getGepSzerzodesList() {
        Log.e("GEP", "getGepszerzodesList " + alvazszam);
        if (gepSzerzodesList == null) {
            gepSzerzodesList = new ArrayList<GepSzerzodes>();
            KiteORM kiteORM = KiteApplication.getKiteORM();
            //KITE 742, status D check
            /*List<GepSzerzodesTetel> szerzodesTetelListWithStatusD = kiteORM.listWithoutFilterD(GepSzerzodesTetel.class, GepSzerzodesTetelekTable.COL_ALVAZSZAM + " = ? AND status is not null AND status = 'D'", new String[]{alvazszam});
            if(szerzodesTetelListWithStatusD != null && szerzodesTetelListWithStatusD.size() > 0){
                return null;
            }*/
            List<GepSzerzodesTetel> szerzodesTetelList = kiteORM.list(GepSzerzodesTetel.class, GepSzerzodesTetelekTable.COL_ALVAZSZAM + " = ?", new String[]{alvazszam});
            Log.e("GEP", "getGepSzerzodesList().szerzodesTetelList: " + szerzodesTetelList.size());
            if (szerzodesTetelList != null && !szerzodesTetelList.isEmpty()) {
                for (GepSzerzodesTetel tetel : szerzodesTetelList) {
                    //Log.e("GEP", "\tgep szezodes tetel=" + tetel.szerzodeskod);
                    if (tetel.szerzodeskod != null) {
                        //KITE 742, status D check
                        /*GepSzerzodes contractWithStatusD = kiteORM.loadSingleWithoutStatusD(GepSzerzodes.class, GepSzerzodesekTable.COL_SZERZODESKOD + " = ? AND status is not null AND status = 'D'", new String[]{tetel.szerzodeskod});
                        if(contractWithStatusD != null){
                            return null;
                        }*/
                        GepSzerzodes contract = kiteORM.loadSingle(GepSzerzodes.class, GepSzerzodesekTable.COL_SZERZODESKOD + " = ?", new String[]{tetel.szerzodeskod});
                        Log.e("GEP", "\tgeptetel[" + tetel.tetelkod + "].szezodes=" + contract);
                        SimpleDateFormat DF = DateUtils.getDfOnlyDate();
                        if (contract != null && contract.kezdesdatum != null && contract.lejaratdatum != null) {
                            Log.e("GEP", "\t\tEllenőrzés[" + contract.szerzodeskod + "]:" + DF.format(contract.kezdesdatum) + "-" + DF.format(contract.lejaratdatum) + ",  mostani datum=" + DF.format(new Date()));
                            Calendar start = Calendar.getInstance();
                            start.setTime(contract.kezdesdatum);
                            Calendar end = Calendar.getInstance();
                            end.setTime(contract.lejaratdatum);
                            Calendar now = Calendar.getInstance();
                            now.setTime(new Date());
                            Log.e("GEP", "\t\tValid[" + contract.szerzodeskod + "]:" + (now.after(start) && now.before(end)));
                            if (now.after(start) && now.before(end) && contract.ellenorizve1 != null && !"".equals(contract.ellenorizve1) && contract.ellenorizve2 != null && !"".equals(contract.ellenorizve2)) {
                                Log.e("GEP", "\t\t$$$$Found=" + contract.szerzodeskod);
                                gepSzerzodesList.add(contract);
                            }
                        }
                    }
                }
            }
        }
        Log.e("GEP", "Gép[" + alvazszam + "] élő szerződéseinek a száma:" + (gepSzerzodesList == null ? -1 : gepSzerzodesList.size()));
		return gepSzerzodesList;
    }

    public List<Munkalap> getMunkalapList(String partnerFilter, boolean forceLoad) {
        if (munkalapList == null || forceLoad) {
            KiteORM kiteORM = KiteApplication.getKiteORM();
            List<String> args = new ArrayList<String>();
            if (partnerFilter != null) {
                args.add(partnerFilter);
            }
            args.add(alvazszam);
            //munkalapList = kiteORM.listOrdered(Munkalap.class, (partnerFilter != null ? MunkalapokTable.COL_PARTNERKOD + " = ? AND " : "") + MunkalapokTable.COL_ALVAZSZAM + " = ? AND (munkalapkod is null OR munkalapkod = '' OR munkalapkod NOT IN (select munkalapok.munkalapkod from munkalapok where status = 'D')) AND (tempkod is null OR tempkod = '' OR tempkod NOT IN (select munkalapok.tempkod from munkalapok where status = 'D'))", args.toArray(new String[args.size()]), MunkalapokTable.COL_LETREHOZASDATUM + " desc");
            munkalapList = kiteORM.listOrdered(Munkalap.class, (partnerFilter != null ? MunkalapokTable.COL_PARTNERKOD + " = ? AND " : "") + MunkalapokTable.COL_ALVAZSZAM + " = ?", args.toArray(new String[args.size()]), MunkalapokTable.COL_LETREHOZASDATUM + " desc");
        }
        return munkalapList;
    }

    public Munkalap getLastClosedMunkalap(String munkalapsorszam) {
        KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
        if(alvazszam != null) {
            return kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_MUNKALAPSORSZAM + " <> ? AND " + MunkalapokTable.COL_ALVAZSZAM + " = ? ORDER BY " + MunkalapokTable.COL_LETREHOZASDATUM + " DESC", new String[]{munkalapsorszam, alvazszam});
        } else {
            return null;
        }
    }
}
