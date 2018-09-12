package hu.itware.kite.service.orm.model.json;

public class PartnerTelephely extends BaseJsonDatabaseObject {
	
	public String telephelykod;
	
	public String tempkod;
	
	public String irsz;
	
	public String telepules;
	
	public String kozteruletnev;
	
	public String kozterulettipus;
	
	public String hazszam;
	
	public String email;
	
	public String telefonszam;

	@Override
	public String toString() {
		return irsz + " " + telepules + " " + kozteruletnev + " " + kozterulettipus + " " + hazszam;
	}
}
