package hu.itware.kite.service.orm.model;

import android.util.Log;

import java.util.Date;

import hu.itware.kite.service.orm.model.annotations.Key;

/**
 * Created by szeibert on 2015.09.14..
 */
public class Alkatresz extends BaseDatabaseObject {

	@Key
	public String munkalapkod;

	@Key
	public String tempkod;

	@Key
	public String alkatreszkod;

	@Key
    public String cikkszam;

	public String cikknev;

    public String rendszer;

	public Double mozgomennyiseg;

	public String mennyisegiegyseg;

	public String sorszam;

	public String agazatiszam;

	public Date rogzitesdatum;

	@Key
	public String bizonylatszam;

	public String munkalapsorszam;

	@Key
    public String mozgasazonosito;


    public Alkatresz() {
    }

    public Alkatresz(String cikknev, String rendszer, String cikkszam) {
        this.cikknev = cikknev;
        this.rendszer = rendszer;
        this.cikkszam = cikkszam;
    }

    @Override
    public String toString() {
        return "Alkatresz{" +
                "cikkszam='" + cikkszam + '\'' +
                ", rendszer='" + rendszer + '\'' +
                ", cikknev='" + cikknev + '\'' +
                '}';
    }

	@Override
	public boolean equals(Object alkatresz) {

    	if (alkatresz instanceof Alkatresz) {
			Alkatresz a = (Alkatresz)alkatresz;
			boolean eq = equals(alkatreszkod, a.alkatreszkod) &&
					equals(agazatiszam, a.agazatiszam) &&
					equals(bizonylatszam, a.bizonylatszam) &&
					equals(cikkszam, a.cikkszam) &&
					equals(cikknev, a.cikknev) &&
					equals(munkalapkod, a.munkalapkod) &&
					equals(munkalapsorszam, a.munkalapsorszam) &&
					equals(rendszer, a.rendszer) &&
					equals(sorszam, a.sorszam) &&
					equals(tempkod, a.tempkod);
			Log.i("KITE.ALKATRESZ", "Equals[" + this + "]=" + a + ", EQ=" + eq);
			return eq;
		}

		return false;
	}

	private static boolean equals(String a, String b) {
    	return (a == null && b == null) || (a != null && a.equals(b));
	}
}
