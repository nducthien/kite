package hu.itware.kite.service.orm.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public abstract class BaseDatabaseObject implements Serializable {

    public long _id = -1;
    
    public Date modified;
	
    @SerializedName("state")
	public String status = "A";

    /**
     * Called after the model loaded from Database...
     */
    public void afterLoad() {

    }

    /**
     * Called before the data to be stored in the Database.
     */
    public void beforeSave() {

    }
}
