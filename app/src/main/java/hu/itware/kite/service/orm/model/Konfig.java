package hu.itware.kite.service.orm.model;

import hu.itware.kite.service.orm.model.annotations.Key;

@SuppressWarnings("serial")
public class Konfig extends BaseDatabaseObject {

	@Key
	public String name;

	public String type;

	public String value;

}
