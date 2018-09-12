package hu.itware.kite.service.orm.model;

import hu.itware.kite.service.orm.model.annotations.JSON;
import hu.itware.kite.service.orm.model.annotations.Key;

@SuppressWarnings("serial")
public class MetaData extends BaseDatabaseObject {

	@Key
	public String id;

	@Key
	public String type;

	@Key
	public String text;

	public String value;

	public Integer pos;

	@JSON(type = String.class)
	public String parentids;

	@Override
	public String toString() {
		return text;
	}


}
