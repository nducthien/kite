package hu.itware.kite.service.orm.model;

import java.util.Date;

import hu.itware.kite.service.orm.model.annotations.Key;

/**
 * Created by batorig on 2015.10.13..
 */
@SuppressWarnings("serial")
public class KeszletMozgas extends BaseDatabaseObject {

	@Key
	public String azonosito;

	public Date datum;

	public String bizonylatszam;

	public String szamlasorszam;

	public String mozgastipus;

	public Double mozgomennyiseg;

	public String mennyisegiegyseg;

	public String cikkszam;

	public String cikknev;

	public String geptipusazonosito;

	public String agazatiszam;

	public String bizonylattetelsorszam;

	public String k0azonosito;

	public String szamlatetelsorszam;
}
