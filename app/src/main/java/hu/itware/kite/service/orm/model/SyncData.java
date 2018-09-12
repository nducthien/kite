package hu.itware.kite.service.orm.model;

import java.util.Date;

import hu.itware.kite.service.orm.model.annotations.Key;

@SuppressWarnings("serial")
public class SyncData extends BaseDatabaseObject {

	@Key
    public String tablename;
    
    public Date updated;
    
    public String mode;
    
    public long lastValue = -1;
    
    public boolean success;
    
    public String error;

	@Override
	public String toString() {
		return "SyncData[table=" + tablename + ", MODE=" + mode + ", updated=" + updated + ", modified=" + modified + ", lastValue=" + lastValue + "]";
	}
}
