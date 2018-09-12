/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.itware.kite.service.orm.model;

import java.util.Set;

import hu.itware.kite.service.orm.model.annotations.Key;

/**
 *
 * @author batorig
 */
@SuppressWarnings("serial")
public class Alkozpont extends BaseDatabaseObject {

	@Key
	public String alkozpontkod;
	public String nev;
	public String irsz;
	public String telepules;
	public String cim;
	public Set<Uzletkoto> uzletkotSet;
	public Set<Partner> partnerSet;

	public String getAddress() {
		return this.irsz + " " + this.telepules + " " + this.cim;
	}
}
