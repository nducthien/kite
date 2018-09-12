package hu.itware.kite.service.orm.model;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.GepekTable;
import hu.itware.kite.service.orm.database.tables.KonfigTable;
import hu.itware.kite.service.orm.database.tables.MunkalapExportokTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.annotations.Key;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.utils.Export;
import hu.itware.kite.service.utils.IdGenerator;
import hu.itware.kite.service.utils.StringUtils;

/**
 * Created by szeibert on 2015.09.14..
 */
@SuppressWarnings("serial")
public class Munkalap extends BaseDatabaseObject {

    private static final String TAG = "Munkalap";
    private static final int DEFAULT_DAYS_TO_CONTINUE = 3;
    private static final int MODIFIABLE_DEFAULT_INTERVAL = 24;

	@Key
	public String munkalapkod;

	@Key
	public String tempkod;

	public String temppartnerkod;
	public String tempgepkod;


    @Expose
    private Gep gep;

    @Expose
    private Partner partner;

    @Expose
    public List<Alkatresz> alkatreszek = new ArrayList<Alkatresz>();

    public String uzemkepes;
	public String gepazonosito;
	public String partnerkod;
	public String alvazszam;
	public String elozmenykod;
    public Date bejelentesdatum;
    public String cikkszam;
	public String irsz;
	public String telepules;
	public String cim;
	public Date meghibasodasdatum;
	public Date garanciaervenyesdatum;
	public Date munkakezdesdatum;
	public Date munkabefejezesdatum;
	public String munkalapsorszam;
	public Date javitaskezdesdatum;
	public String munkavegzesjellege;
	public Double megtettkm;
	public String tipushosszunev;
	public String geptipus;
	public String gepnev;
	public String geptipusazonosito;
	public Date munkavegzesdatum;
	public String hibakod; // részegység
	public Date javitasdatum;
	public String szervizkonyv;
	public String motorszam;
	public String hibajelenseg;
	public String hibajelensegoka;
	public Date letrehozasdatum;
	public String nev;
	public String pipkod;
	public String szervizes;
	public String munkavegzeshelye;
	public String terhelendo;
	public String oracsere;
	public String vevopozitiv;
	public String vevoeszrevetel;
	public String tevekenyseg;
	public String tevekenyseg1;
	public String tevekenyseg2;
	public String bizonylatszam;
	public Date uzembehelyezesdatum;
	public Double uzemora; // gepora
	public Double munkaora;
	public String oradij;
	public String szamlazasimod;
	public String surgos;
	public Double tulora;

	public String email;
	public String fax;
	public String allapotkod;
	public String szig;

	public String munkavegzeshelybesorolas;
	public String javitaskesz;
	public Double oraallas; // teljesitmeny

	public String vevoiEszrevetelKod;
    public String hibasAlkatresz;
    public String alairas;
    public String alairaskep;

	public String fenykepekData;

	public Double gps1;
	public Double gps2;
	public String baj;
	public String sortipus;

	@Expose
    public List<String> fenykepek;

    public boolean partnerChanged;

    public String munkavegzesjellege2;
    public String munkavegzeshelyszine;
    public String tevekenysegDropdown;

    public String jegyzokonyv;
    public Date lezarasdatum;

    public Munkalap() {
        this(false);
    }

    public Munkalap(boolean init) {
        if (init) {
            munkalapkod = null;
            //tempkod = IdGenerator.generate("G", 6);
            munkalapsorszam = generateSorszam();
            Log.e("Munkalap", "munkalapsorszam: " + munkalapsorszam);
            javitaskezdesdatum = letrehozasdatum = munkavegzesdatum = new Date();
            Uzletkoto uzletkoto = LoginService.getManager(KiteApplication.getContext());
            szervizes = uzletkoto.szervizeskod;
            allapotkod = "1";
            jegyzokonyv = "N";
            //String lastTempKod = KiteDAO.getLastTempkod(KiteApplication.getContext(), "H" + uzletkoto.azon);
            tempkod = IdGenerator.generate(uzletkoto.azon); //IdGenerator.generateGEW("H" + uzletkoto.azon, lastTempKod, 3);  KITE-870
        }
    }

    public Munkalap copy() {
        Munkalap munkalap = new Munkalap(true);
        munkalap.elozmenykod = getMunkalapKod();
        munkalap.gepazonosito = gepazonosito;
        munkalap.partnerkod = partnerkod;
        munkalap.temppartnerkod = temppartnerkod;
        munkalap.alvazszam = alvazszam;
        munkalap.bejelentesdatum = bejelentesdatum;
        munkalap.cikkszam = cikkszam;
        munkalap.irsz = irsz;
        munkalap.telepules = telepules;
        munkalap.cim = cim;
        munkalap.meghibasodasdatum = meghibasodasdatum;
        munkalap.garanciaervenyesdatum = garanciaervenyesdatum;
        munkalap.javitaskezdesdatum = javitaskezdesdatum;
        munkalap.munkavegzesjellege = munkavegzesjellege;
        munkalap.tipushosszunev = tipushosszunev;
        munkalap.geptipus = geptipus;
        munkalap.gepnev = gepnev;
        munkalap.geptipusazonosito = geptipusazonosito;
        munkalap.hibakod = hibakod;
        munkalap.motorszam = motorszam;
        munkalap.letrehozasdatum = new Date();
        munkalap.nev = nev;
        munkalap.munkavegzeshelye = munkavegzeshelye;
        munkalap.uzembehelyezesdatum = uzembehelyezesdatum;
        munkalap.surgos = surgos;
        munkalap.tevekenyseg1 = tevekenyseg1;
        munkalap.email = email;
        munkalap.fax = fax;
        munkalap.munkavegzeshelybesorolas = munkavegzeshelybesorolas;
        munkalap.jegyzokonyv = jegyzokonyv;

        return munkalap;
    }

    public String generateSorszam() {

		/*String base = KiteDAO.getSzervizesMunkalapAlapKod(KiteApplication.getContext()) + "T";
		if (base == null) {
			base = "HIBA";
		}
        return IdGenerator.generate(base, 10);*/

		String lastMunkalapKod = KiteDAO.getLastMunkalapSorszam(KiteApplication.getContext(), KiteDAO.getSzervizesMunkalapAlapKod(KiteApplication.getContext()) + "Y");
        return IdGenerator.generateNextInSequence(KiteDAO.getSzervizesMunkalapAlapKod(KiteApplication.getContext()) + "Y", lastMunkalapKod, 5);
    }

    public String getPartnerSummary() {

        return String.format("(%s - %s: %s)", getPartner() == null ? "" : getPartner().getNev(), partner == null ? "" : partner.getAddress(), getGep() == null ? "" : getGep().tipushosszunev);
    }



    public Gep getGep() {
        if (gep == null) {
            KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
            gep = kiteORM.loadSingle(Gep.class, GepekTable.COL_ALVAZSZAM + " = ?", new String[]{alvazszam!=null?alvazszam:""});
            if (gep == null) {
                gep = new Gep();
            }
        }
        return gep;
    }

    public void setGep(Gep gep) {
        this.gep = gep;
        if (gep != null) {
            alvazszam = gep.alvazszam;
            gepazonosito = gep.kiadottazonosito;
            garanciaervenyesdatum = gep.garanciaervenyesseg;
            geptipus = gep.geptipus;
			gepnev = gep.nev;
            geptipusazonosito = gep.tipusazonosito;
            tipushosszunev = gep.tipushosszunev;
            motorszam = gep.motorszam;
            uzembehelyezesdatum = gep.uzembehelyezesdatum;
			tempgepkod = gep.tempgepkod;
        }
    }

    public Partner getPartner() {
        if (partner == null) {
            KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
            if (partnerkod != null && !partnerkod.isEmpty()) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_PARTNERKOD + " = ?", new String[]{partnerkod});
            }
            if (partner == null && temppartnerkod != null && !temppartnerkod.isEmpty()) {
                partner = kiteORM.loadSingle(Partner.class, PartnerekTable.COL_TEMPKOD + " = ?", new String[]{temppartnerkod});
            }
            if (partner == null) {
                partner = new Partner();
            }
        }
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
        this.partnerChanged = true;
        if (partner != null) {
            partnerkod = partner.partnerkod;
			temppartnerkod = partner.tempkod;
            cim = partner.cim;
            irsz = partner.irsz;
            telepules = partner.telepules;
			nev = partner.getNev();
        }
    }

    public void updateTempMunkalap(String munkalapkod){
        KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
        this.munkalapkod = munkalapkod;
        kiteORM.update(this);
        Log.e("MUNKALAP", "updateTempMunkalap, tempkod= " + tempkod + ", munkalapkod= " + munkalapkod + ", allapotkod= " + allapotkod);
        List<Munkalap> munkalapList = new ArrayList<Munkalap>();
        // megkeressuk az almunkalapokat
        munkalapList.addAll(kiteORM.list(Munkalap.class, MunkalapokTable.COL_ELOZMENYKOD + " = ?", new String[]{tempkod}));
        for (Munkalap munkalap : munkalapList) {
            if (munkalap != null) {
                munkalap.elozmenykod = munkalapkod;
                Log.e("MUNKALAP", "updateTempMunkalap, tempkod= " + tempkod + ", elozmenykod= " + elozmenykod);
                kiteORM.update(munkalap);
            }
        }
    }

    public void closeMunkalap() {
        closeMunkalap(javitasdatum, 0);
    }

    private void closeMunkalap(Date endDate, int depth) {
        if (javitasdatum == null || depth == 0) {
            KiteORM kiteORM = new KiteORM(KiteApplication.getContext());

			if (depth > 0) { // az eredeti rekordhoz semmikepp ne keszuljon uj export
                MunkalapExport export = null;
                if (this.munkalapkod != null) {
                    export = kiteORM.loadSingle(MunkalapExport.class, MunkalapExportokTable.COL_GE + " = ?", new String[]{this.munkalapkod});
                } else if (this.tempkod != null) {
                    export = kiteORM.loadSingle(MunkalapExport.class, MunkalapExportokTable.COL_GEW + " = ?", new String[]{this.tempkod});
                }

                if (export != null) {
                    export.JADT = Export.DF.format(endDate);
                    export.JAVOK = "I";
                    kiteORM.update(export);
                } else {
                    export = Export.createMunkalapClosedExport(KiteApplication.getContext(), munkalapkod, tempkod, endDate, this);
                    kiteORM.insert(export);
                }
            }

            javitasdatum = endDate;
            kiteORM.update(this);

            if (depth < 20) {
                depth++;
                List<Munkalap> munkalapList = new ArrayList<Munkalap>();
                if (elozmenykod != null) { // ha volt elozmeny, megkeressuk a fomunkalapot
                    munkalapList.add(kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_MUNKALAPKOD + " = ? OR " + MunkalapokTable.COL_TEMPKOD + " = ?", new String[]{elozmenykod, elozmenykod}));
                }
				// megkeressuk az almunkalapokat
				munkalapList.addAll(kiteORM.list(Munkalap.class, MunkalapokTable.COL_ELOZMENYKOD + " = ?", new String[]{getMunkalapKod()}));
                for (Munkalap munkalap : munkalapList) {
                    if (munkalap != null) {
                        munkalap.closeMunkalap(endDate, depth);
                    }
                }
            }
        }
    }
    public boolean findOpenMunkalapInRelatedMunkalapArray(){
        return this.findOpenMunkalapInRelatedMunkalapArray(0, null);
    }


    private boolean findOpenMunkalapInRelatedMunkalapArray(int depth, ArrayList<String> previouslyChekedMunkalap){
        //return false;
        KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
        boolean result = false;
        if (depth < 10) {
            depth++;
            if(previouslyChekedMunkalap == null){
                previouslyChekedMunkalap = new ArrayList<String>();
            } else {

            }
            List<Munkalap> nextList = new ArrayList<Munkalap>();
            List<Munkalap> munkalapList = new ArrayList<Munkalap>();
            if (elozmenykod != null) { // ha volt elozmeny, megkeressuk a fomunkalapot
                munkalapList.add(kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_MUNKALAPKOD + " = ? OR " + MunkalapokTable.COL_TEMPKOD + " = ?", new String[]{elozmenykod, elozmenykod}));
            }
            // megkeressuk az almunkalapokat
            if(getMunkalapKod()!= null) {
                munkalapList.addAll(kiteORM.list(Munkalap.class, MunkalapokTable.COL_ELOZMENYKOD + " = ?", new String[]{getMunkalapKod()}));
            }
            for (Munkalap munkalap : munkalapList) {
                if (munkalap != null && !previouslyChekedMunkalap.contains(munkalap.getMunkalapKod())) {
                    previouslyChekedMunkalap.add(munkalap.getMunkalapKod());
                    if ("1".equals(munkalap.allapotkod)) {
                        result = true;
                    } else {
                        nextList.add(munkalap);
                    }
                }
            }
            if (!result) {
                for (Munkalap munkalap : nextList) {
                    result = result || munkalap.findOpenMunkalapInRelatedMunkalapArray(depth, previouslyChekedMunkalap);
                }
            }
        }
        return result;
    }

    public boolean canContinue() {
                boolean result = true;
                int days = getDaysToContinue();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(letrehozasdatum);
                calendar.add(Calendar.DAY_OF_YEAR, days);
                Calendar now = Calendar.getInstance();
                if (calendar.before(now)) {
                    result = false;
        }
        return result;
    }

    public static int getDaysToContinue() {
        KiteORM kiteORM = new KiteORM(KiteApplication.getContext());
        Konfig konfig = kiteORM.loadSingle(Konfig.class, KonfigTable.COL_NAME + " = ?", new String[]{"MFNSZ"});
        int days;
        if (konfig == null) {
            days = DEFAULT_DAYS_TO_CONTINUE;
        } else {
            try {
                days = Integer.parseInt(konfig.value);
            } catch (Exception e) {
                days = DEFAULT_DAYS_TO_CONTINUE;
            }
        }
        return days;
    }

	public String getMunkalapKod() {
		if (munkalapkod != null) {
			return munkalapkod;
		}
		return tempkod;
	}

	public String getBizonylatSzamok() {
		if (alkatreszek == null || alkatreszek.isEmpty()) {
			return null;
		}

		LinkedHashSet<String> sets = new LinkedHashSet<String>();
		for (Alkatresz a : alkatreszek) {
			if (a != null && !StringUtils.isEmpty(a.bizonylatszam)) {
				sets.add(a.bizonylatszam);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String a : sets) {
			sb.append(a).append(",");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		}

		return sb.toString();
	}
	
	public String createMunkalapSummary(String templateName) {
            String munkalapSummary = new String();

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(KiteApplication.getContext().getAssets().open(templateName)));

                String mLine;
                while ((mLine = reader.readLine()) != null) {
                    munkalapSummary += mLine;
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not read template", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close template", e);
                    }
                }
            }

            munkalapSummary = munkalapSummary.replace("%partner%", getPartner().getNev());
            munkalapSummary = munkalapSummary.replace("%sorszam%", munkalapsorszam!=null?munkalapsorszam:tempkod);
            munkalapSummary = munkalapSummary.replace("%szerelo%", szervizes!=null?szervizes:"");
            munkalapSummary = munkalapSummary.replace("%cim%", getPartner().getAddress());
            munkalapSummary = munkalapSummary.replace("%kelt%", BaseActivity.getSdfShort().format(munkavegzesdatum));
            munkalapSummary = munkalapSummary.replace("%javitaskezdes%", BaseActivity.getSdfShort().format(javitaskezdesdatum));
            munkalapSummary = munkalapSummary.replace("%geptipus%", getGep().tipushosszunev!=null?getGep().tipushosszunev:"");
            munkalapSummary = munkalapSummary.replace("%erkezes%", munkakezdesdatum!=null?BaseActivity.getSdfTime().format(munkakezdesdatum):"");
            munkalapSummary = munkalapSummary.replace("%tavozas%", munkabefejezesdatum!=null?BaseActivity.getSdfTime().format(munkabefejezesdatum):"");
            munkalapSummary = munkalapSummary.replace("%alvazszam%", alvazszam!=null?alvazszam:"");
            munkalapSummary = munkalapSummary.replace("%munkaora%", munkaora!=null?munkaora.toString():"");
            munkalapSummary = munkalapSummary.replace("%uzemora%", uzemora!=null?uzemora.toString():"");
            munkalapSummary = munkalapSummary.replace("%oraallas%", oraallas!=null?oraallas.toString():"");
            munkalapSummary = munkalapSummary.replace("%megtettkm%", megtettkm!=null?megtettkm.toString():"");
            if (munkavegzesjellege != null) {
                String[] munkavegzesArray = munkavegzesjellege.split("\\|\\|");
                if (munkavegzesArray.length > 0) {
                    munkalapSummary = munkalapSummary.replace("%munkavegzesjellege1%", munkavegzesArray[0]);
                } else {
                    munkalapSummary = munkalapSummary.replace("%munkavegzesjellege1%", "");
                }

                if (munkavegzesArray.length > 1) {
                    munkalapSummary = munkalapSummary.replace("%munkavegzesjellege2%", munkavegzesArray[1]);
                } else {
                    munkalapSummary = munkalapSummary.replace("%munkavegzesjellege2%", "");
                }
            } else {
                munkalapSummary = munkalapSummary.replace("%munkavegzesjellege1%", "");
                munkalapSummary = munkalapSummary.replace("%munkavegzesjellege2%", "");
            }
            munkalapSummary = munkalapSummary.replace("%helyszin%", munkavegzeshelye!=null?munkavegzeshelye:"");
            munkalapSummary = munkalapSummary.replace("%besorolas%", munkavegzeshelybesorolas!=null?munkavegzeshelybesorolas:"");
            munkalapSummary = munkalapSummary.replace("%surgos%", "I".equals(surgos)?"Igen":"Nem");
            munkalapSummary = munkalapSummary.replace("%javitasbefejezodott%", "I".equals(javitaskesz)?"Igen":"Nem");
            if (javitasdatum != null) {
                munkalapSummary = munkalapSummary.replace("%javitasbefejezesdatuma%", BaseActivity.getSdfShort().format(javitasdatum));
            } else {
                munkalapSummary = munkalapSummary.replace("%javitasbefejezesdatuma%", "-");
            }
            munkalapSummary = munkalapSummary.replace("%tevekenyseg%", tevekenyseg1!=null?tevekenyseg1:"");
            munkalapSummary = munkalapSummary.replace("%tevekenysegleiras%", tevekenyseg!=null?StringUtils.breakLongString(tevekenyseg, 90):"");
            munkalapSummary = munkalapSummary.replace("%pipkod%", pipkod!=null?pipkod:"");
            if (vevoeszrevetel != null){
                munkalapSummary = munkalapSummary.replace("%vevoieszrevetel1%", vevopozitiv!=null?vevopozitiv:"");
                munkalapSummary = munkalapSummary.replace("%vevoieszrevetel2%", vevoeszrevetel);
            }
            munkalapSummary = munkalapSummary.replace("%uzemkepes%", "I".equals(uzemkepes)?"Igen":"Nem");
        
        return munkalapSummary;
    }

    public boolean isEditableFromOwnList() {
        int hours = MODIFIABLE_DEFAULT_INTERVAL;
        Konfig konfig = KiteDAO.getKonfig(KiteApplication.getContext(), "FAMODINT");
        try {
            hours = Integer.parseInt(konfig.value);
        } catch (Exception e) {
            // default
        }
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.HOUR_OF_DAY, -hours);
        final Date editableAfter = calendar.getTime();
        if (!"4".equals(allapotkod)) {
            return false;
        } else if (lezarasdatum != null) {
            return lezarasdatum.after(editableAfter);
        } else if ("4".equals(allapotkod) || "".equals(allapotkod)){
            return letrehozasdatum.after(editableAfter);
        } else {
            return true;
        }
    }


}
