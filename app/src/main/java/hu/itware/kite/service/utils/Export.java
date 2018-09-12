package hu.itware.kite.service.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.itware.kite.service.orm.enums.Operation;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.AlkatreszExport;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.GepExport;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.MunkalapExport;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.orm.utils.GSON;
import hu.itware.kite.service.services.LoginService;

@SuppressLint("SimpleDateFormat")
public final class Export {

    private static final String TAG = "KITE.UTIL.EXPORT";

    public static final SimpleDateFormat DF = new SimpleDateFormat("yyyy.MM.dd");

    public static final SimpleDateFormat TF = new SimpleDateFormat("HH:mm");

    public static final SimpleDateFormat TFS = new SimpleDateFormat("HH:mm:ss");

    public static final DecimalFormat NF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));

    private Export() {

    }

    private static String e(String s) {

        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return s;
    }

    public static AlkatreszExport createAlkatreszExport(Context context, Munkalap munkalap, Alkatresz alkatresz, KeszletMozgas mozgas) {

        Log.i(TAG, "createAlkatreszExport()=" + alkatresz + ", mozgas=" + mozgas);
        Uzletkoto uzletkoto = LoginService.getManager(context);

        AlkatreszExport e = new AlkatreszExport();
        try {
            e.SYSTEM = "ssk";
            e.DATABASE = "GEC";

            e.GECW = e(alkatresz.tempkod);
            e.GE = e(munkalap.munkalapkod);
            e.GEW = e(munkalap.tempkod);

            e.CI = e(alkatresz.cikkszam);
            e.CNEV = e(alkatresz.cikknev);
            e.MOME = alkatresz.mozgomennyiseg == null ? null : NumberUtils.toPreciseString(alkatresz.mozgomennyiseg, 2);
            e.MEE = e(alkatresz.mennyisegiegyseg);
            e.BSZ = e(alkatresz.bizonylatszam);
            e.GETC2 = e(alkatresz.munkalapsorszam);
            e.NDT = alkatresz.rogzitesdatum == null ? null : DF.format(alkatresz.rogzitesdatum);
            e.NTM = alkatresz.rogzitesdatum == null ? null : TFS.format(alkatresz.rogzitesdatum);
            e.N_UI = e(uzletkoto.nui);
            e.NUS = e(uzletkoto.ugyvitelikod);
            e.SZ = e(alkatresz.sorszam);
            e.AG = e(alkatresz.agazatiszam);

            if (mozgas != null) {
                e.SZTS = e(mozgas.szamlatetelsorszam);
                e.BTS = e(mozgas.bizonylattetelsorszam);
                e.K0 = e(mozgas.k0azonosito);
                e.MOTI = e(mozgas.mozgastipus);
            }
            return e;
        } finally {
            Log.d(TAG, "AlkatreszExport.JSON=" + GSON.toJson(e));
        }
    }

    public static GepExport createGepExport(Context context, Gep gep) {

        Log.i(TAG, "createGepExport()=" + gep);
        Uzletkoto uzletkoto = LoginService.getManager(context);

        GepExport e = new GepExport();
        try {
            e.SYSTEM = "ssk";
            e.DATABASE = "KG";
            e.ALSZ = e(gep.alvazszam);
            e.GNEV = e(gep.tipushosszunev);
            e.PS = e(gep.partnerkod);
            e.KGW = e(gep.tempgepkod);
            e.IPS = e(gep.temppartnerkod);
            if (gep.modified != null) {
                e.NTM = e(TFS.format(gep.modified));
                e.NDT = e(DF.format(gep.modified));
            }
            e.N_UI = e(uzletkoto.nui);
            e.NUS = e(uzletkoto.ugyvitelikod);
            ;
            e.GYEV = gep.gyartaseve;
            return e;
        } finally {
            Log.d(TAG, "GepExport.JSON=" + GSON.toJson(e));
        }
    }

    public static MunkalapExport createMunkalap4Per4Export(Context context, Munkalap munkalap, Gep gep) {

        Log.i(TAG, "createMunkalap4Per4Export().munkalapsorszam=" + munkalap.munkalapsorszam + ", gep.alvazszam=" + gep.alvazszam);
        Uzletkoto uzletkoto = LoginService.getManager(context);

        MunkalapExport e = new MunkalapExport();
        try {
            e.SYSTEM = "ssk";
            e.DATABASE = "GE";
            e.ACT = Operation.MODIFY.getSign(); // 4/4-es kepernyo adatai mindig modify rekordban mennek (KITE-660)
            //--- KITE-862 - GE nem kell exportba
            //e.GE = e(munkalap.munkalapkod);
            e.GEW = e(munkalap.tempkod);

            e.BJTM = munkalap.bejelentesdatum == null ? null : TF.format(munkalap.bejelentesdatum);
            e.SZJ02 = createSZJ02(munkalap);
            e.GEDT = munkalap.meghibasodasdatum == null ? null : DF.format(munkalap.meghibasodasdatum);
            e.CI = e(munkalap.cikkszam);
            e.MSG1 = e(munkalap.hibajelenseg);
            e.MSG2 = e(munkalap.hibajelensegoka);
            e.TEV2 = e(munkalap.tevekenyseg2);
            e.HKOD = e(munkalap.hibakod);
            e.GEALL = e(munkalap.allapotkod);
            e.TRBSZS = e(munkalap.bizonylatszam);

            munkalap.modified = new Date();

            e.MDT = DF.format(munkalap.modified);
            e.MTM = TFS.format(munkalap.modified);
            e.M_UI = e(uzletkoto.nui);
            e.MUS = e(uzletkoto.ugyvitelikod);

            if (munkalap.lezarasdatum != null) {
                e.LDT = DF.format(munkalap.lezarasdatum);
                e.LTM = TFS.format(munkalap.lezarasdatum);
            }

            copyImagesFromTempToUpload(munkalap, context);
            return e;
        } finally {
            Log.d(TAG, "Munkalap4Per4Export.JSON=" + GSON.toJson(e));
        }
    }

    public static MunkalapExport createMunkalapFullExport(Context context, Munkalap munkalap, Gep gep, Operation operation) {

        Log.i(TAG, "createMunkalapFullExport().munkalapsorszam=" + munkalap.munkalapsorszam + ", gep.alvazszam=" + gep.alvazszam);
        Uzletkoto uzletkoto = LoginService.getManager(context);

        MunkalapExport e = new MunkalapExport();
        try {
            e.SYSTEM = "ssk";
            e.DATABASE = "GE";
            e.ACT = operation.getSign();
            e.GE = e(munkalap.munkalapkod);
            e.EGE = e(munkalap.elozmenykod);
            e.GEW = e(munkalap.tempkod);

            e.GOK = e(munkalap.uzemkepes);
            e.KG = e(munkalap.gepazonosito);
            e.PS = e(munkalap.partnerkod);
            e.ALSZ = e(munkalap.alvazszam);
            e.BJTM = munkalap.bejelentesdatum == null ? null : TF.format(munkalap.bejelentesdatum);
            e.CI = e(munkalap.cikkszam);
            e.FALU = e(munkalap.telepules);
            e.GEDT = munkalap.meghibasodasdatum == null ? null : DF.format(munkalap.meghibasodasdatum);
            e.GERV = munkalap.garanciaervenyesdatum == null ? null : DF.format(munkalap.garanciaervenyesdatum);
            e.GETC1 = createGETC1(munkalap);
            e.VEV1 = munkalap.vevopozitiv;
            e.GETC2 = e(munkalap.munkalapsorszam);
            e.GETD1 = munkalap.javitaskezdesdatum == null ? null : DF.format(munkalap.javitaskezdesdatum);
            e.GETN1 = NumberUtils.toPreciseString(munkalap.megtettkm, 0);

            e.GNEV = e(munkalap.tipushosszunev);
            e.GNEV1 = e(munkalap.geptipus);
            e.GNEV2 = e(munkalap.gepnev);

            e.HALDT = munkalap.munkavegzesdatum == null ? null : DF.format(munkalap.munkavegzesdatum);
            e.HKOD = e(munkalap.hibakod);
            e.IRSZ = e(munkalap.irsz);
            e.JADT = munkalap.javitasdatum == null ? null : DF.format(munkalap.javitasdatum);
            e.KAFSZ = e(munkalap.szervizkonyv);
            e.MSG1 = e(munkalap.hibajelenseg);
            e.MSG2 = e(munkalap.hibajelensegoka);
            String[] names = StringUtils.splitName(munkalap.nev);
            e.NEV = names[0];
            e.NEV2 = names[1];
            e.PIP = e(munkalap.pipkod);
            e.SZERV = e(munkalap.szervizes);
            e.SZJ01 = e(munkalap.munkavegzeshelye);

            e.SZJ02 = createSZJ02(munkalap);
            e.SZJ03 = createSZJ03(munkalap);

            e.SZJ04 = e(munkalap.vevoeszrevetel);
            e.SZJ09 = e(munkalap.tevekenyseg);
            e.UTCA = e(munkalap.cim);
            e.UZORA = NumberUtils.toPreciseString(munkalap.uzemora, 0);
            e.ALK = e(uzletkoto.alkozpontkod);
            e.UZHEKT = NumberUtils.toPreciseString(munkalap.oraallas, 0);
            e.GETI = e(munkalap.sortipus);

            e.JZK = e(munkalap.jegyzokonyv);

            if (gep != null) {
                e.GT = e(gep.tipusazonosito);

                e.FUSZ = e(gep.fulkeszam);
                e.HASZ = e(gep.hajtomuszam);
                e.MESZ = e(gep.mellsohidszam);

                e.MOSZ = StringUtils.isEmpty(gep.motorszam) || "0".equals(gep.motorszam) ? null : gep.motorszam;
                e.UZDT = gep.uzembehelyezesdatum == null ? null : DF.format(gep.uzembehelyezesdatum);

                //--- PSOK= Az egyeztetett módon KG.PS=GE.PS esetén "I" a mező tartalma, egyébként "N"
                e.PSOK = gep.partnerkod != null && gep.partnerkod.equals(munkalap.partnerkod) ? "I" : "N";
            }

            String[] items = null;
            if (munkalap.munkavegzesjellege != null) {
                items = munkalap.munkavegzesjellege.split("\\|\\|");
                e.GETI1 = items.length > 0 ? items[0] : "";
                e.GETI2 = items.length > 1 ? items[1] : "";
            }

            e.MHB = e(munkalap.munkavegzeshelybesorolas);
            e.TEV1 = e(munkalap.tevekenyseg1);
            e.TEV2 = e(munkalap.tevekenyseg2);
            e.JAVOK = e(munkalap.javitaskesz);
            e.VEV1 = e(munkalap.vevopozitiv);
            e.EMAIL = e(munkalap.email);
            e.FAX = e(munkalap.fax);
            e.GEALL = e(munkalap.allapotkod);
            if (StringUtils.isEmpty(e.KG)) {
                e.KGW = e(munkalap.tempgepkod);
            }
            e.IPS = e(munkalap.temppartnerkod);
            e.ALIRO = e(munkalap.alairas);
            e.SZIG = e(munkalap.szig);

            Location gps = getGPSLocation(context);
            e.GPS1 = gps == null ? null : "" + NumberUtils.toPreciseString(gps.getLatitude(), 8);
            e.GPS2 = gps == null ? null : "" + NumberUtils.toPreciseString(gps.getLongitude(), 8);

            munkalap.modified = new Date();

            if (operation == Operation.NEW) {
                e.NDT = DF.format(munkalap.modified);
                e.NTM = TFS.format(munkalap.modified);
                e.N_UI = e(uzletkoto.nui);
                e.NUS = e(uzletkoto.ugyvitelikod);
                if (munkalap.getPartner() != null) {
                    e.ADO = !StringUtils.isEmpty(munkalap.getPartner().adoszam) ? munkalap.getPartner().adoszam : null;
                    e.ADAZ = !StringUtils.isEmpty(munkalap.getPartner().adoazonosito) ? munkalap.getPartner().adoazonosito : null;
                    e.TEL = !StringUtils.isEmpty(munkalap.getPartner().telefonszam) ? munkalap.getPartner().telefonszam : null;
                    Log.e(TAG, "ADAZ= " + e.ADAZ);
                }
                e.GETI2K = munkalap.munkavegzesjellege2;
                e.MHBK = munkalap.munkavegzeshelyszine;
                e.TEV1K = munkalap.tevekenysegDropdown;
            } else {
                e.MDT = DF.format(munkalap.modified);
                e.MTM = TFS.format(munkalap.modified);
                e.M_UI = e(uzletkoto.nui);
                e.MUS = e(uzletkoto.ugyvitelikod);
            }

            if ("2".equals(munkalap.allapotkod)) {
                e.BJTM = null;
                e.CI = null;
                e.GEDT = null;
                e.HKOD = null;
                e.SZJ02 = null;
                copySignaturesFromTempToUpload(munkalap, context);
            }

            if (munkalap.lezarasdatum != null) {
                e.LDT = DF.format(munkalap.lezarasdatum);
                e.LTM = TFS.format(munkalap.lezarasdatum);
            }

            return e;
        } finally {
            Log.d(TAG, "MunkalapFullExport.JSON=" + GSON.toJson(e));
        }
    }

    public static MunkalapExport createMunkalapClosedExport(Context context, String munkalapkod, String tempkod, Date endDate, Munkalap munkalap) {

        Log.i(TAG, "createMunkalapClosedExport().munkalapkod=" + munkalapkod);
        Uzletkoto uzletkoto = LoginService.getManager(context);
        Date now = new Date();

        MunkalapExport e = new MunkalapExport();
        try {
            e.SYSTEM = "ssk";
            e.DATABASE = "GE";
            e.ACT = Operation.MODIFY.getSign();
            e.JAVOK = "I";
            e.JADT = DF.format(endDate);
            e.GE = e(munkalapkod);
            e.GEW = e(tempkod);

            e.MDT = DF.format(now);
            e.MTM = TFS.format(now);
            e.M_UI = e(uzletkoto.nui);
            e.MUS = e(uzletkoto.ugyvitelikod);

            copyImagesFromTempToUpload(munkalap, context);
            return e;
        } finally {
            Log.d(TAG, "MunkalapClosedExport.JSON=" + GSON.toJson(e));
        }
    }

    public static Location getGPSLocation(Context context) {
        GPSTracker tracker = new GPSTracker(context);
        Location result = null;
        if (tracker.canGetLocation()) {
            result = tracker.getLocation();
            tracker.stopUsingGPS();
            return result;
        } else {
            tracker.stopUsingGPS();
            return result;
        }
    }

    private static String createGETC1(Munkalap munkalap) {

        StringBuilder sb = new StringBuilder();
        if (munkalap.munkakezdesdatum != null) {
            sb.append(TF.format(munkalap.munkakezdesdatum));
        } else {
            sb.append(StringUtils.emptyString(5, ' '));
        }

        if (munkalap.munkabefejezesdatum != null) {
            sb.append(TF.format(munkalap.munkabefejezesdatum));
        } else {
            sb.append(StringUtils.emptyString(5, ' '));
        }
        return sb.toString();
    }

    /**
     * SZJ02 mezo letrehozasa
     *
     * @param munkalap
     * @return
     */
    private static String createSZJ02(Munkalap munkalap) {

        StringBuilder sb = new StringBuilder();
        sb.append(munkalap.bejelentesdatum == null ? "-" + StringUtils.emptyString(9, ' ') : DF.format(munkalap.bejelentesdatum));
        sb.append("=bejelentés dátuma ");
        sb.append(munkalap.terhelendo == null ? "-" + StringUtils.emptyString(8, ' ') : StringUtils.fillLength(munkalap.terhelendo, 9));
        sb.append("=terhelendő ");
        sb.append(munkalap.oracsere == null ? " " + StringUtils.emptyString(9, ' ') : StringUtils.fillLength(munkalap.oracsere, 10));
        sb.append("=óracsere ");
        return sb.toString();
    }

    /**
     * SZJ03 mezo letrehozasa
     *
     * @param munkalap
     * @return
     */
    private static String createSZJ03(Munkalap munkalap) {
        StringBuilder sb = new StringBuilder();
        sb.append(munkalap.munkaora == null ? StringUtils.fillLength("0.0", 4) : StringUtils.fillLength(NF.format(munkalap.munkaora), 4, true));
        sb.append("=munkaóra ");
        sb.append(munkalap.oradij == null ? "-" : munkalap.oradij);
        sb.append("=             ");
        sb.append(munkalap.szamlazasimod == null ? "-" : munkalap.szamlazasimod);
        sb.append("=               ");
        sb.append(munkalap.surgos == null ? "-" : munkalap.surgos);
        sb.append("=sürgös ");
        sb.append(munkalap.tulora == null ? StringUtils.fillLength(" 0.0", 4) : StringUtils.fillLength(NF.format(munkalap.tulora), 4, true));
        sb.append("=túlóra    ");
        return sb.toString();
    }

    protected static String getIMEILast3Digit(Context context) {
        String imei = SystemUtils.getImei(context);
        if (imei != null && imei.length() > 2) {
            return imei.substring(imei.length() - 3);
        }

        return "XXX";
    }

    public static void copyImagesFromTempToUpload(Munkalap munkalap, Context context) {
        if (munkalap.fenykepek != null) {
            for (String kep : munkalap.fenykepek) {
                try {
                    File src = new File(ImageUtils.getPath(context, true), kep);
                    File dest = new File(ImageUtils.getPath(context, false), kep);
                    if (src != null && !dest.exists()) {
                        src.renameTo(dest);
                        Log.i(TAG, "Copy image from " + src + " to " + dest);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error renaming image = " + kep, e);
                }
            }
        }

        copySignaturesFromTempToUpload(munkalap, context);
    }

    private static void copySignaturesFromTempToUpload(Munkalap munkalap, Context context) {
        if (munkalap.alairaskep != null) {
            try {
                File src = new File(ImageUtils.getPath(context, true), munkalap.alairaskep);
                File dest = new File(ImageUtils.getPath(context, false), munkalap.alairaskep);
                if (src != null && !dest.exists()) {
                    src.renameTo(dest);
                    Log.i(TAG, "Copy image from " + src + " to " + dest);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error renaming image = " + munkalap.alairaskep, e);
            }
        }
    }

}
