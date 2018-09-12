/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.itware.kite.service.orm.model;

import hu.itware.kite.service.orm.model.annotations.Key;
import hu.itware.kite.service.utils.StringUtils;

import java.util.Set;

/**
 * 
 * @author batorig
 */
@SuppressWarnings("serial")
public class Uzletkoto extends BaseDatabaseObject {

	@Key
	public String uzletkotokod;

	@Key
	public String tosz;

	public String nev1;
	public String nev2;
	public String password;
	public String alkozpontkod;
	public String nui;
	public String azon;
	public String jogosultsag;
	public String szervizeskod;
	public String ugyvitelikod;
	public String teljesNev;

	public Alkozpont alkozpont;
	public Set<Partner> partnerek;

	public String getNev() {
		if (StringUtils.isEmpty(this.nev2)) {
			return this.nev1;
		}
		return this.nev1 + " " + this.nev2;
	}

	@Override
	public String toString() {
		return "Uzletkoto[nev=" + getNev() + ", nui=" + nui + ", azon=" + azon + "]";
	}
}
