package hu.itware.kite.service.orm.model.json;

import java.io.Serializable;
import java.util.Date;

public abstract class BaseJsonDatabaseObject implements Serializable {

	public String state;
	
	public Date modified;
}
