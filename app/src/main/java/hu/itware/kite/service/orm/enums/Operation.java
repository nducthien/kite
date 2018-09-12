package hu.itware.kite.service.orm.enums;

public enum Operation {

	MODIFY("MOD"), DELETE("MOD"), NEW("NEW");

	private String sign;

	Operation(String sign){
		this.sign = sign;
	}

	public String getSign() {
		return sign;
	}
}
