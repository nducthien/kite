package hu.itware.kite.service.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.SyncActivity;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.tables.KonfigTable;
import hu.itware.kite.service.orm.database.tables.MetaDataTable;
import hu.itware.kite.service.orm.database.tables.MunkalapokTable;
import hu.itware.kite.service.orm.database.tables.PartnerekTable;
import hu.itware.kite.service.orm.model.Alkozpont;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.SyncData;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.utils.StringUtils;

public final class KiteDAO {

	private static final Pattern szervizesPattern = Pattern.compile(".*(\\[.{4}]).*");
	private static final String PREF_SZERVIZESKOD = "PREF_SZK";
	private static final String TAG = "KITE.DAO";

	private KiteDAO() {

	}

	/**
	 * Load Partner reladted data into Partner object: -Uzletkoto -Alkozpont
	 * -Szerzodesek -Forgalmi adatok -Gazdasagi adatok -Termelesi adatok
	 * 
	 * @param context
	 *            Context
	 * @param partner
	 *            Partner to be updated
	 * @return true
	 */
	public static boolean loadPartnerData(Context context, Partner partner) {

		KiteORM orm = new KiteORM(context);
		partner.uzletkoto = LoginService.getManager(context);
		if (partner.alkozpontkod != null) {
			partner.alkozpont = orm.loadSingle(Alkozpont.class, "alkozpontkod = ?", new String[] { partner.alkozpontkod });
		}
		return true;
	}

	public static boolean loadUzletkotoData(Context context, Uzletkoto uzletkoto) {

		KiteORM orm = new KiteORM(context);
		uzletkoto.alkozpont = orm.loadSingle(Alkozpont.class, "alkozpontkod = ?", new String[]{uzletkoto.alkozpontkod});
		return true;
	}

	public static MetaData[] loadMetaData(Context context, String type) {
		return loadMetaData(context, type, null);
	}

	public static MetaData[] loadMetaData(Context context, String type, String parentType) {

		KiteORM orm = new KiteORM(context);
		List<MetaData> datas = null;
		if (parentType == null) {
			datas = orm.listOrdered(MetaData.class, "type = ? and parentids is null", new String[] { type }, MetaDataTable.COL_POS + " asc");
		} else {
			datas = orm.listOrdered(MetaData.class, "type = ? and (parentids is null or parentids like ?)", new String[] { type, "%'" + parentType + "'%" }, MetaDataTable.COL_POS + " asc");
		}
		if (datas != null) {
			return datas.toArray(new MetaData[datas.size()]);
		}

		return new MetaData[0];
	}

	public static MetaData[] loadMetaDataByTypeId(Context context, String id, String type) {

		KiteORM orm = new KiteORM(context);
		List<MetaData> datas = orm.listOrdered(MetaData.class, "id = ? and type = ?", new String[]{id, type}, MetaDataTable.COL_POS + " asc");
		if (datas != null) {
			return datas.toArray(new MetaData[datas.size()]);
		}
		return new MetaData[0];
	}

	public static List<Munkalap> getOpenMunkalapList(Context context) {
		KiteORM kiteORM = new KiteORM(context);
		List<Munkalap> result = kiteORM.listOrdered(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND ( " + MunkalapokTable.COL_ALLAPOTKOD + " = ? OR " + MunkalapokTable.COL_ALLAPOTKOD + " = ? )", new String[]{LoginService.getManager(context).szervizeskod, "1", "3"}, MunkalapokTable.COL_LETREHOZASDATUM + " desc");
		if (result == null) {
			result = new ArrayList<Munkalap>();
		}
		for(int i=0; i<result.size(); i++){
			Log.e("MUNKALAP", "getOpenMunkalapList, munkalapkod= " + result.get(i).munkalapkod + ", tempkod= " + result.get(i).tempkod + ", elozmenykod= "+ result.get(i).elozmenykod);
		}
		return result;
	}

	public static int getOpenMunkalapCount(Context context) {
		KiteORM kiteORM = new KiteORM(context);
		//return kiteORM.getCount(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND ( " + MunkalapokTable.COL_ALLAPOTKOD + " = ? OR " + MunkalapokTable.COL_ALLAPOTKOD + " = ? )", new String[]{LoginService.getManager(context).szervizeskod, "1", "3"});
		return kiteORM.getNativeCount("select count(*) from munkalapok where " + MunkalapokTable.COL_SZERVIZES + " = ? AND ( " + MunkalapokTable.COL_ALLAPOTKOD + " = ? OR " + MunkalapokTable.COL_ALLAPOTKOD + " = ? )", new String[]{LoginService.getManager(context).szervizeskod, "1", "3"});
	}

	public static String getLastMunkalapSorszam(Context context, String munkalapKod) {
		KiteORM kiteORM = new KiteORM(context);
		Munkalap munkalap = kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_MUNKALAPSORSZAM + " LIKE ? ORDER BY " + MunkalapokTable.COL_MUNKALAPSORSZAM + " DESC LIMIT 1", new String[]{LoginService.getManager(context).szervizeskod, munkalapKod + "%"});
		return munkalap != null ? munkalap.munkalapsorszam : "";
	}

	public static String getLastTempkod(Context context, String base) {
		KiteORM kiteORM = new KiteORM(context);
		Munkalap munkalap = kiteORM.loadSingle(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_TEMPKOD + " LIKE ? ORDER BY " + MunkalapokTable.COL_TEMPKOD + " DESC LIMIT 1", new String[]{LoginService.getManager(context).szervizeskod, base + "%"});
		return munkalap != null ? munkalap.tempkod : "";
	}

	public static List<Munkalap> getUnfinishedMunkalapList(Context context, String filter) {
		KiteORM kiteORM = new KiteORM(context);
		String likeFilter = "%" + filter + "%";
		List<Munkalap> result = kiteORM.listOrdered(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_ALLAPOTKOD + " = ? AND (" + MunkalapokTable.COL_PARTNERKOD + " LIKE ? OR " + MunkalapokTable.COL_ALVAZSZAM + " LIKE ?)", new String[]{LoginService.getManager(context).szervizeskod, "2", likeFilter, likeFilter}, MunkalapokTable.COL_LETREHOZASDATUM + " desc");
		if (result == null) {
			result = new ArrayList<Munkalap>();
		}
		return result;
	}

	public static int getUnfinishedMunkalapCount(Context context) {
		KiteORM kiteORM = new KiteORM(context);
		//return kiteORM.getCount(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_ALLAPOTKOD + " = ?", new String[]{LoginService.getManager(context).szervizeskod, "2"});
		return kiteORM.getNativeCount("select count(*) from munkalapok where " + MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_ALLAPOTKOD + " = ?", new String[]{LoginService.getManager(context).szervizeskod, "2"});
	}

	public static List<Munkalap> getPreviousMunkalapList(Context context, String filter) {
		KiteORM kiteORM = new KiteORM(context);
		String likeFilter = "%" + filter + "%";
		List<Munkalap> result = kiteORM.listOrdered(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_JAVITASDATUM + " IS NULL AND " + MunkalapokTable.COL_ALLAPOTKOD + " = ? AND (" + MunkalapokTable.COL_PARTNERKOD + " LIKE ? OR " + MunkalapokTable.COL_ALVAZSZAM + " LIKE ?)", new String[]{LoginService.getManager(context).szervizeskod, "4", likeFilter, likeFilter}, MunkalapokTable.COL_LETREHOZASDATUM + " desc");
		if (result == null) {
			result = new ArrayList<Munkalap>();
		}
		return result;
	}

	public static int getPreviousMunkalapCount(Context context) {
		KiteORM kiteORM = new KiteORM(context);
		//return kiteORM.getCount(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_JAVITASDATUM + " IS NULL AND " + MunkalapokTable.COL_ALLAPOTKOD + " > ?", new String[]{LoginService.getManager(context).szervizeskod, "1"});
		return kiteORM.getNativeCount("select count(*) from munkalapok where " + MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_JAVITASDATUM + " IS NULL AND " + MunkalapokTable.COL_ALLAPOTKOD + " = ?", new String[]{LoginService.getManager(context).szervizeskod, "4"});
	}

	public static Cursor getOwnMunkalapList(Context context, List<String> alvazszamList, List<String> partnerList, Date from, Date to) {
		if (context == null) {
			return null;
		}
		KiteORM kiteORM = new KiteORM(context);
		if (from == null) {
			try {
				from = BaseActivity.getSdfShort().parse("2000-01-01");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (to == null) {
			try {
				to = BaseActivity.getSdfShort().parse("2100-01-01");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		List<String> args = new ArrayList<String>();
		args.add(LoginService.getManager(context).szervizeskod);
		args.add(BaseActivity.getSdfShort().format(from) + " 00:00:00");
		args.add(BaseActivity.getSdfShort().format(to) + " 23:59:59");
		String alvazszamInList = "";
		if (alvazszamList.size() > 0) {
			alvazszamInList = makeCommaSeperatedList(alvazszamList);
		}
		String partnerKodInList = "";
		if (partnerList.size() > 0) {
			partnerKodInList = makeCommaSeperatedList(partnerList);
		}
		Cursor result = kiteORM.query(Munkalap.class,
				MunkalapokTable.COL_SZERVIZES + " = ? AND (" + MunkalapokTable.COL_MUNKAVEGZESDATUM + " BETWEEN ? AND ?)" +
				(partnerList.size() > 0 ? " AND " + MunkalapokTable.COL_PARTNERKOD + " IN (" + partnerKodInList + ")" : "") +
				(alvazszamList.size() > 0 ? " AND " + MunkalapokTable.COL_ALVAZSZAM + " IN (" + alvazszamInList + ")" : ""),
			args.toArray(new String[args.size()]), MunkalapokTable.COL_MUNKAVEGZESDATUM + " desc");
		return result;
	}

	private static String getSzervizesKod(String text) {
		Matcher matcher = szervizesPattern.matcher(text);
		if (matcher.matches()) {
			String group = matcher.group(1);
			return group.substring(1, group.length() - 1);
		}
		return null;
	}

	public static String getSzervizesMunkalapAlapKod(Context context) {

		Uzletkoto uzletkoto = LoginService.getManager(context);
		if (uzletkoto == null) {
			return null;
		}

		//--- Check cached version
		/*SharedPreferences sp = context.getSharedPreferences(PREF_SZERVIZESKOD, Context.MODE_PRIVATE);
		String munkalapKod = sp.getString(uzletkoto.szervizeskod, null);
		if (munkalapKod != null) {
			return munkalapKod;
		}*/

		//--- Munkalapkod not found in cache
		KiteORM orm = new KiteORM(context);
		MetaData metaData = orm.loadSingle(MetaData.class, "id = ? and type = ? and text like ?", new String[]{"JRA", "K2", uzletkoto.szervizeskod + "%"});
		if (metaData != null) {
			String munkalapKod = getSzervizesKod(metaData.text);

			//--- Save to cache (e.g. > sharedpreferences)
			/*if (munkalapKod != null) {
				SharedPreferences.Editor edit = sp.edit();
				edit.putString(uzletkoto.szervizeskod, munkalapKod);
				edit.commit();
			}*/
			return munkalapKod;
		}
		return null;
	}

	public static Konfig getKonfig(Context context, String key) {
		KiteORM kiteORM = new KiteORM(context);
		return kiteORM.loadSingle(Konfig.class, KonfigTable.COL_NAME + " = ?", new String[]{key});
	}

	public static Cursor getFilteredPartnerCursor(Context context, CharSequence charSequence) {
		return getFilteredPartnerCursor(context, charSequence, new ArrayList<String>());
	}

	public static Cursor getFilteredPartnerCursor(Context context, CharSequence charSequence, List<String> partnerkodList) {
		KiteORM kiteORM = new KiteORM(context);
		Cursor cursor = null;
		if (charSequence != null) {
			if (charSequence.toString().contains(" ")) {
                String filter = charSequence.toString();
				// megnezzuk van-e olyan nev, ami a filterrel kezdodik
                cursor = kiteORM.query(Partner.class, (partnerkodList.size() > 0 ? PartnerekTable.COL_PARTNERKOD + " IN (" + makeCommaSeperatedList(partnerkodList) + ") AND " : "") + PartnerekTable.COL_SEARCHNEV + " LIKE ? ", new String[]{StringUtils.clearText(charSequence.toString()) + "%"}, PartnerekTable.COL_NEV1 + " asc" + (charSequence.toString().length() < 2 ? " LIMIT 500" : ""));
				if (cursor != null && cursor.getCount() == 0) {
					cursor.close();
					// ha nincs, megnezzuk van-e olyan, ami tartalmazza a szoveget
					cursor = kiteORM.query(Partner.class, (partnerkodList.size() > 0 ? PartnerekTable.COL_PARTNERKOD + " IN (" + makeCommaSeperatedList(partnerkodList) + ") AND " : "") + PartnerekTable.COL_SEARCHNEV + " LIKE ? ", new String[]{"%" + StringUtils.clearText(charSequence.toString()) + "%"}, PartnerekTable.COL_NEV1 + " asc" + (charSequence.toString().length() < 2 ? " LIMIT 500" : ""));
				}
				if (cursor != null && cursor.getCount() == 0) {
					cursor.close();
					String[] filterParts = filter.split(" ");
					String nevFilter = "";
					for (int i = 0; i < filterParts.length; i++) {
						nevFilter += filter.substring(0, filter.indexOf(" ") + 1);
						filter = filter.substring(filter.indexOf(" ") + 1);
						cursor = kiteORM.query(Partner.class, (partnerkodList.size() > 0 ? PartnerekTable.COL_PARTNERKOD + " IN (" + makeCommaSeperatedList(partnerkodList) + ") AND " : "") + PartnerekTable.COL_SEARCHNEV + " LIKE ? AND " + PartnerekTable.COL_SEARCHCIM + " LIKE ? ", new String[]{"%" + StringUtils.clearText(nevFilter) + "%", StringUtils.clearText(filter) + "%"}, PartnerekTable.COL_NEV1 + " asc" + (charSequence.toString().length() < 2 ? " LIMIT 500" : ""));
						if (cursor.getCount() > 0) {
							Log.e("searchfilter", "nev: " + nevFilter + " cim: " + filter);
							break;
						}
					}
				}
			} else {
				// megnezzuk van-e olyan nev, ami a filterrel kezdodik
				cursor = kiteORM.query(Partner.class, (partnerkodList.size() > 0 ? PartnerekTable.COL_PARTNERKOD + " IN (" + makeCommaSeperatedList(partnerkodList) + ") AND " : "") + PartnerekTable.COL_SEARCHNEV + " LIKE ? ", new String[]{StringUtils.clearText(charSequence.toString()) + "%"}, PartnerekTable.COL_NEV1 + " asc" + (charSequence.toString().length() < 2 ? " LIMIT 500" : ""));
				if (cursor.getCount() == 0) {
					cursor.close();
					// ha nincs, megnezzuk van-e olyan, ami tartalmazza a szoveget
					cursor = kiteORM.query(Partner.class, (partnerkodList.size() > 0 ? PartnerekTable.COL_PARTNERKOD + " IN (" + makeCommaSeperatedList(partnerkodList) + ") AND " : "") + PartnerekTable.COL_SEARCHNEV + " LIKE ? ", new String[]{"%" + StringUtils.clearText(charSequence.toString()) + "%"}, PartnerekTable.COL_NEV1 + " asc" + (charSequence.toString().length() < 2 ? " LIMIT 500" : ""));
				}
				if (cursor.getCount() == 0) {
					cursor.close();
					// ha nincs talalat, keresunk cimre
					cursor = kiteORM.query(Partner.class, (partnerkodList.size() > 0 ? PartnerekTable.COL_PARTNERKOD + " IN (" + makeCommaSeperatedList(partnerkodList) + ") AND " : "") + PartnerekTable.COL_SEARCHCIM + " LIKE ? ", new String[]{StringUtils.clearText(charSequence.toString()) + "%"}, PartnerekTable.COL_NEV1 + " asc" + (charSequence.toString().length() < 2 ? " LIMIT 500" : ""));
				}
			}
		}
		return cursor;
	}

	public static boolean getAlreadyOpenMunkalap(Context context, String alvazszam, String partnerkod) {
		KiteORM kiteORM = new KiteORM(context);
		List<Munkalap> result = kiteORM.list(Munkalap.class, MunkalapokTable.COL_SZERVIZES + " = ? AND " + MunkalapokTable.COL_ALLAPOTKOD + " = ? AND " + MunkalapokTable.COL_ALVAZSZAM + " = ? AND (" + MunkalapokTable.COL_PARTNERKOD + " = ? OR " + MunkalapokTable.COL_TEMPPARTNERKOD + " = ?)", new String[]{LoginService.getManager(context).szervizeskod, "1", alvazszam, partnerkod, partnerkod});
		return result.size() > 0;
	}

	public static String makeCommaSeperatedList(List<String> list) {
		StringBuilder result = new StringBuilder();
		for(int i=0;i<list.size();i++) {
			if(i > 0) {
				result.append(",");
			}
			result.append("'" + list.get(i) + "'");
		}
		return result.toString();
	}


	public static boolean needSynchronization(Context context) {

		KiteORM orm = new KiteORM(context);
		SyncData metaDataSyncData = orm.loadSingle(SyncData.class, "tablename = ?", new String [] { "metadata" });
		Log.e(TAG, "needSynchronization()=" + metaDataSyncData);
		if (metaDataSyncData == null || metaDataSyncData.updated == null || metaDataSyncData.success == false) {
			return true;
		}
		return false;
	}
}
