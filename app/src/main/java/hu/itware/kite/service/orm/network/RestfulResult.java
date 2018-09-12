package hu.itware.kite.service.orm.network;

public class RestfulResult {

	public int httpCode;
	
	public String httpError;
	
	public String responseData;
	
	public boolean isHttpOk() {
		return httpCode >= 200 && httpCode < 300;
	}
	
	@Override
	public String toString() {
		return "ErrorCode:" + httpCode + ", Error:" + httpError  + ", Data:" + responseData;
	}
	
}
