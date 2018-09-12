package hu.itware.kite.service.orm.model;

import java.io.Serializable;
import java.util.Date;

import hu.itware.kite.service.orm.model.annotations.Key;

@SuppressWarnings("serial")
public class GPSData extends BaseDatabaseObject implements Serializable {

	@Key
	public String imei;
	
	@Key
	public Date date;
	public Double latitude;
	public Double longitude;
}
