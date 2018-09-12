package hu.itware.kite.service.orm.database;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Column implements Serializable {

	public static enum Type { INTEGER, TEXT, DOUBLE, DATE, RESOURCE, BOOLEAN };
	
	public String name;
	
	public Type type;
	
	public boolean key;

	public Column(String name, Type type, boolean key) {
		this.name = name;
		this.type = type;
		this.key  = key;
	}

	public Column(String name, Type type) {
		this.name = name;
		this.type = type;
		this.key  = false;
	}
}
