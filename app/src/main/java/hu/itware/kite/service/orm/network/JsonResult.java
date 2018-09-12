package hu.itware.kite.service.orm.network;

public class JsonResult {
	
	public String error;

	public boolean success;
	
	@Override
	public String toString() {
		return "Success:" + success + ", Error:" + error;
	}

}
