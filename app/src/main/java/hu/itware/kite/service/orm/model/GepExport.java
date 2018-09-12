package hu.itware.kite.service.orm.model;

import hu.itware.kite.service.orm.model.annotations.Key;

/**
 * Created by batorig on 2015.10.13..
 */
@SuppressWarnings("serial")
public class GepExport extends BaseDatabaseObject {
	
	public String SYSTEM;
	public String DATABASE;
	public String ACT;

	@Key
	public String ALSZ;

	public String PS;
	public String GNEV;
	public String KGW;
	public String IPS;

	public String NDT;
	public String NUS;
	public String NTM;
	public String N_UI;
	public String GYEV;
}
