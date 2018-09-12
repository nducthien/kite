package hu.itware.kite.service.orm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.database.BaseTable;
import hu.itware.kite.service.orm.database.Column;
import hu.itware.kite.service.orm.database.Column.Type;
import hu.itware.kite.service.orm.database.KiteDatabaseHelper;
import hu.itware.kite.service.orm.database.TableMap;
import hu.itware.kite.service.orm.database.tables.MetaDataTable;
import hu.itware.kite.service.orm.database.tables.SyncDataTable;
import hu.itware.kite.service.orm.model.Alkatresz;
import hu.itware.kite.service.orm.model.AlkatreszCikkszam;
import hu.itware.kite.service.orm.model.AlkatreszExport;
import hu.itware.kite.service.orm.model.Alkozpont;
import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import hu.itware.kite.service.orm.model.GPSData;
import hu.itware.kite.service.orm.model.Gep;
import hu.itware.kite.service.orm.model.GepExport;
import hu.itware.kite.service.orm.model.GepSzerzodes;
import hu.itware.kite.service.orm.model.GepSzerzodesTetel;
import hu.itware.kite.service.orm.model.KeszletMozgas;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.MunkalapExport;
import hu.itware.kite.service.orm.model.Partner;
import hu.itware.kite.service.orm.model.SyncData;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.orm.network.JsonResponse;
import hu.itware.kite.service.orm.network.JsonResult;
import hu.itware.kite.service.orm.network.RestfulClient;
import hu.itware.kite.service.orm.network.RestfulResult;
import hu.itware.kite.service.orm.utils.DateUtils;
import hu.itware.kite.service.orm.utils.UriUtils;
import hu.itware.kite.service.services.LoginResult;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.services.PipUpdateService;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.DownloadUtils;
import hu.itware.kite.service.utils.FileUtils;
import hu.itware.kite.service.utils.MetaDataTypes;
import hu.itware.kite.service.utils.Network;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static final boolean TEST = true;

	private static final String TAG = "KITE.SYNC";

	public static final String MODE_UPLOAD = "UPLOAD";

	public static final String MODE_DOWNLOAD = "DOWNLOAD";

	public static final String BROADCAST_SYNC = "hu.itware.kite.service.SYNC_INFO";

	public static final int COMMAND_START_SINGLE_SYNC = 1;

	public static final int COMMAND_END_SINGLE_SYNC = 2;

	public static final int COMMAND_DOWNLOADIMAGE = 3;

	public static final int COMMAND_UPLOADIMAGE = 4;

	public static final int COMMAND_ITEMCOUNT = 5;

	public static final int COMMAND_CURRENTITEM = 6;

	public static final int COMMAND_DOWNLOADDATA = 7;

	public static final int COMMAND_UPLOADDATA = 8;

	public static final int COMMAND_START = 9;

	public static final int COMMAND_END = 10;

	public static final int COMMAND_AUTH = 11;

	public static final int COMMAND_START_INSERT = 12;

	public static final int COMMAND_END_INSERT = 13;

	public static final int COMMAND_DOWNLOADPIP = 14;

	public static final int COMMAND_UPLOADVIDEO = 15;

	public static final int COMMAND_ERROR = 99;

	private static final String FUTUREDOWNLOAD_DOCUMENTS_PREF_NAME = "PREF_DOCUMENTS";

	private static final SimpleDateFormat DFZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
		new Locale("hu"));

	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
		new Locale("hu"));

	static {
		DFZ.setTimeZone(TimeZone.getTimeZone("GMT"));
	}


	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
							  ContentProviderClient contentProviderClient, SyncResult syncResult) {

		Log.i(TAG, "onPerformSync() start.");

		try {

			int networkState = Network.getNetworkState(getContext());
			if (networkState == Network.STATE_NONETWORK) {
				info(COMMAND_START, 1);
				info(COMMAND_ERROR, "Nincs hálózati kapcsolat az adatok szinkronizációjához!");
				return;
			}

			String tablename = extras.getString("table", null);
			String direction = extras.getString("direction", null);
			Log.e(TAG, "TableName=" + tablename);

			KiteORM orm = new KiteORM(getContext());

			if (!TEST) {
				Uzletkoto manager = LoginService.getManager(getContext());
				if (manager == null) {
					info(COMMAND_START, 1);
					info(COMMAND_ERROR, "Hiba! Nincs bejelentkezett Üzletkötő!");
					return;
				}

				info(COMMAND_AUTH, null);
				LoginResult login =
					LoginService.doLogin(getContext(), manager.szervizeskod, manager.password, true, false);
				if (!login.success) {
					info(COMMAND_START, 1);
					info(COMMAND_ERROR, "Autentikációs hiba:" + login.error);
					return;
				}
			}

			if (tablename != null && direction != null) {

				info(COMMAND_START, 1);
				Log.i(TAG, "Perfrom a single sync for table:" + tablename);

				BaseTable<?> table = TableMap.getHandlerByTablename(tablename);
				if (table == null) {
					Log.w(TAG, "Cannot find table in map:" + tablename);
					return;
				}

				if (MODE_DOWNLOAD.equals(direction)) {
					syncTableDownloadStream(orm, table.getModelClass());
					syncDownloadImages();
					syncDownloadPIPPdfs();
					syncDownloadDocuments();
				} else if (MODE_UPLOAD.equals(direction)) {

					syncTableUpload(orm, table.getModelClass());
					syncUploadImages();
					syncDownloadPIPPdfs();
				} else {
					Log.w(TAG, "Unknown direction:" + direction);
				}

				return;
			}

			Log.i(TAG, "Perfrom a full sync.");


			info(COMMAND_START, 16);
			syncTableUpload(orm, GPSData.class);
			syncTableUpload(orm, AlkatreszExport.class);
			syncTableUpload(orm, GepExport.class);
			syncTableUpload(orm, MunkalapExport.class);

			syncUploadImages();

			syncTableDownloadStream(orm, Alkozpont.class);
			syncTableDownloadStream(orm, Konfig.class);
			syncTableDownloadStream(orm, MetaData.class);
			syncTableDownloadStream(orm, Partner.class);
			syncTableDownloadStream(orm, KeszletMozgas.class);

			syncTableDownloadStream(orm, Alkatresz.class);
			syncTableDownloadStream(orm, AlkatreszCikkszam.class);
			syncTableDownloadStream(orm, Munkalap.class);
			syncTableDownloadStream(orm, Gep.class);
			syncTableDownloadStream(orm, GepSzerzodes.class);
			syncTableDownloadStream(orm, GepSzerzodesTetel.class);
			syncTableDownloadStream(orm, Uzletkoto.class);

			syncDownloadImages();
			syncDownloadPIPPdfs();
			syncDownloadDocuments();

		} catch (Throwable e) {
			syncResult.hasHardError();
			Log.e(TAG, e.getMessage(), e);

		} finally {
			info(COMMAND_END, null);
			Log.i(TAG, "onPerformSync() finish.");
		}
	}

	/**
	 * Special post processing functions.
	 *
	 * @param orm
	 * @param handler
	 */
	private void syncPostProcess(KiteORM orm, BaseTable<?> handler) {

		Log.i(TAG, "Deleting records from table " + handler.getTableName() + " where status='D'");
		int count = orm.delete(handler.getModelClass(), "status = ?", new String[]{"D"});
		Log.i(TAG, count + " record(s) deleted...");
	}

	private void syncDownloadDocuments() {
		Log.e(TAG, "syncDownloadDocuments");
		Konfig konfig = KiteDAO.getKonfig(getContext(), MetaDataTypes.OKTATASI_ANYAGOK_HELYE);
		String docsDir = konfig.value != null ? konfig.value : Settings.STUDY_MATERIALS_ROOT_DIRECTORY;
		KiteORM kiteORM = new KiteORM(getContext());
		List<MetaData> documents = kiteORM.listWithoutFilterD(MetaData.class, MetaDataTable.COL_TYPE + " = ? ", new String[]{"DOC"});
		for (MetaData document : documents) {
			Log.e(TAG, "checking " + document.text);
			File file = new File(Environment.getExternalStoragePublicDirectory(docsDir), document.text);
			if (((file.exists() && document.modified.after(new Date(file.lastModified()))) || !file.exists()) && "A".equalsIgnoreCase(document.status)) {
				Log.e(TAG, "document.modified: " + BaseActivity.getSdfPicture().format(document.modified) + " file.lastmodified: " + BaseActivity.getSdfPicture().format(new Date(file.lastModified())));
				if (file.exists()) {
					file.delete(); // ha letezik, es frissiteni kell, akkor toroljuk elotte, mert a downloadmanager nem irja felul, hanem hozzacsap egy sorszamot, es uj fajlkent menti el
				}
				Log.e(TAG, document.text + " needs to be downloaded");
				DownloadUtils.downloadFile(getContext(), document.text, Settings.SERVER_DOWNLOAD_INFO_URL, docsDir, "Információs anyag letöltése: ", FUTUREDOWNLOAD_DOCUMENTS_PREF_NAME);
			}
			if (file.exists() && "D".equalsIgnoreCase(document.status)) {
				Log.e(TAG, document.text + " needs to be deleted");
				file.delete();
			}
		}
	}

	private void syncDownloadImages() {

		Log.i(TAG, "Check images to be download...");

		if (Network.getNetworkState(getContext()) == Network.STATE_WIFI) {
			Collection<String> futureImages = ImageUtils.getImagesToNeedDownload(getContext());
			if (futureImages != null) {
				for (String imageName : futureImages) {
					if (ImageUtils.isImageExists(imageName)) {
						ImageUtils.removeImageFromFutureDownload(getContext(), imageName);
					} else {
						ImageUtils.downloadImage(getContext(), imageName);
					}
				}
			}
		}
	}

	private void syncDownloadPIPPdfs(){
		Log.e(TAG, "syncDownloadPIPPdfs");
		if (Network.getNetworkState(getContext()) != Network.STATE_WIFI) {
			return;
		}
		Konfig pipKonfig = KiteDAO.getKonfig(getContext(), "PIPMAPPA");
		String pipFolder = Settings.PIP_CODE_DOCUMENT_DIRECTORY;
		if (pipKonfig != null) {
			pipFolder = pipKonfig.value;
		}
		if (!pipFolder.endsWith("/")) {
			pipFolder = pipFolder + "/";
		}
		KiteORM kiteORM = new KiteORM(getContext());
		List<MetaData> pipNames = kiteORM.listWithoutFilterD(MetaData.class, MetaDataTable.COL_ID + " = ? AND " + MetaDataTable.COL_TYPE + " = ? AND " + MetaDataTable.COL_STATUS + " = ?", new String[]{"PIP", "K2", "A"});
		if(pipNames.size() > 0){
			for(MetaData md : pipNames){
				String text = md.text;
				String version = md.value;
				if(version != null && text != null){
					try {
						Integer versionInt = Integer.valueOf(version);
						String pipkodName = text.split(":")[0].replaceAll(" ", "");
						String pipkodPdf = pipkodName + "_" + version + ".pdf";
						Integer deviceVersion = PipUpdateService.getPipCodeVersion(getContext(), pipkodName);
						Log.e(TAG, "Check if pip file " + pipkodPdf + " version updated: new version= " + versionInt + " >? device version= " + deviceVersion);
						File pipFile = new File(Environment.getExternalStoragePublicDirectory(pipFolder + pipkodPdf), "");
						if (deviceVersion < versionInt || !pipFile.exists()) {
							if(pipFile.exists()){
								Log.e(TAG, "Delete unknown pip file!");
								pipFile.delete();
							}
							Log.e(TAG, "downloading pip file! " + pipFile.toString());
							info(COMMAND_DOWNLOADPIP, pipkodPdf);
							String pipUrl = Settings.SERVER_DOWNLOAD_PIP_URL + pipkodName + ".pdf";
							ImageUtils.downloadPip(pipkodName, pipFile);
							Log.e(TAG, "Checking previous version of pipfile...");

							PipUpdateService.setPipCodeVersion(getContext(), pipkodName, versionInt);
							if (versionInt != null && versionInt > 1) {
								for (int i = 1; i < versionInt; i++) {
									String previousPipkodPdf = pipkodName + "_" + i + ".pdf";
									File previousPipFile = new File(Environment.getExternalStoragePublicDirectory(pipFolder + previousPipkodPdf), "");
									if (previousPipFile.exists()) {
										Log.e(TAG, "Delete previous version of pipfile = " + previousPipkodPdf);
										previousPipFile.delete();
									} else {
										Log.e(TAG, "No previous version of pipfile!");
									}
								}
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "error parsing version!", e);
					}
				}
			}
		}
	}

	private void syncUploadImages() {

		File[] images = ImageUtils.listUploadableImages(getContext());
		if (images != null) {
			for (File image : images) {

				if (image.length() > 1024 * 1024 * 32) {
					Log.w(TAG, "File is too big:" + image.getName() + ", size=" + image.length());
					continue;
				}

				/*if (Network.getNetworkState(getContext()) == Network.STATE_WIFI ||
					//--- Upload signature images on all network! Signature images starts with "_A_" prefix...
					(Network.getNetworkState(getContext()) != Network.STATE_NONETWORK && image.getName().startsWith("_A_"))) {*/
				if ((Network.getNetworkState(getContext()) == Network.STATE_WIFI && image.getName().endsWith(".mp4")) ||
					(Network.getNetworkState(getContext()) != Network.STATE_NONETWORK && !image.getName().endsWith(".mp4"))) {

					boolean isVideo = image.getName().endsWith(".mp4");

					// upload videos only on WIFI
					// upload all images on all network types
					if (isVideo) {
						info(COMMAND_UPLOADVIDEO, image.getName());
					} else {
						info(COMMAND_UPLOADIMAGE, image.getName());
					}

					String error = ImageUtils.uploadImage(image);
					if (error == null) {
						boolean deleted = image.delete();
						Log.d(TAG, "Image " + image + " uploaded and deleted:" + deleted);
					} else {
						Log.e(TAG, "Error uploading image:" + image + ", error=" + error);
						if (isVideo) {
							info(COMMAND_ERROR, "Hiba a videó feltöltésénél:" + image + ", Hiba:" + error);
						} else {
							info(COMMAND_ERROR, "Hiba a kép feltöltésénél:" + image + ", Hiba:" + error);
						}
					}
				}
			}
		}
	}

	private <T extends BaseDatabaseObject> void syncTableUpload(KiteORM orm, Class<T> clazz)
		throws SyncException {

		Log.i(TAG, "syncTableUpload().start()");
		BaseTable<?> handler = TableMap.getHandlerByClass(clazz);
		if (handler == null) {
			throw new SyncException("No database Handler found for class=" + clazz);
		}
		String table = handler.getTableName();
		Log.i(TAG, "syncTableUpload().table=" + table);

		SyncData syncData = loadSyncData(getContext(), table);
		if (syncData == null) {
			syncData = new SyncData();
			syncData.tablename = table;
			syncData.mode = MODE_UPLOAD;
			syncData.lastValue = -1;
			syncData.modified = null;
			insertSyncData(getContext(), syncData);
		}

		long lastId = syncData.lastValue;
		syncData.success = false;
		syncData.error = null;

		try {
			info(COMMAND_START_SINGLE_SYNC, syncData);

			Log.i(TAG, "Table last synchronized=" + syncData.updated);
			Log.i(TAG, "Last synchronized id=" + syncData.lastValue);

			List<? extends BaseDatabaseObject> data;
			data = orm.list(clazz, null, null);

			Log.i(TAG, "Items to be upload:" + (data == null ? -1 : data.size()));
			if (data == null || data.isEmpty()) {
				syncData.success = true;
				syncData.error = "No records to be upload.";
				Log.i(TAG, syncData.error);
				return;
			}

			lastId = data.isEmpty() ? data.get(data.size() - 1)._id : syncData.lastValue;
			Log.d(TAG, "Last synchronized ID=" + lastId);

			RestfulResult result = null;
			JsonResult response = null;
			try {
				info(COMMAND_UPLOADDATA, "S");
				String json = RestfulClient.toJson(data);
				Log.i(TAG, "Sending data JSON:" + json);
				result = RestfulClient.doCreate(getContext(), UriUtils.getTableRestUrl(table), json);
				info(COMMAND_UPLOADDATA, "E");

				if (result.isHttpOk()) {
					response = RestfulClient.getGSON().fromJson(result.responseData, JsonResult.class);
					if (response == null || !response.success) {
						syncData.success = false;
						syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + result;
						info(COMMAND_ERROR, syncData.error);
						Log.e(TAG, syncData.error);
						return;
					}
					syncData.success = true;
					syncData.error = null;

					// ---Remove success
					for (BaseDatabaseObject d : data) {
						Log.e(TAG, "Delete record=" + d);
						orm.delete(d);
					}
				} else {
					Log.e(TAG, "Error synchronizing table. Error:" + result);
					syncData.success = false;
					syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + result;
					info(COMMAND_ERROR, syncData.error);
					return;

				}
			} catch (Exception e) {
				Log.e(TAG, "Error synchronizing table:" + table, e);
				syncData.success = false;
				syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + e;
				info(COMMAND_ERROR, syncData.error);
				return;
			}
		} finally {
			Log.d(TAG, "Table uploaded to server:" + table);
			if (syncData.success) {
				syncData.lastValue = lastId;
				syncData.modified = new Date();
				syncData.updated = new Date();
			}

			insertSyncData(getContext(), syncData);
			info(COMMAND_END_SINGLE_SYNC, syncData);
			Log.d(TAG, "Sync data saved:" + syncData);
		}

	}


	private <T extends BaseDatabaseObject> void syncTableDownloadStream(KiteORM orm, Class<T> clazz) throws SyncException {

		Log.i(TAG, "syncTableDownloadStream().start()");
		BaseTable<T> handler = TableMap.getHandlerByClass(clazz);
		if (handler == null) {
			throw new SyncException("No database Handler found for class=" + clazz);
		}

		String table = handler.getTableName();
		Log.i(TAG, "syncTableDownload().table=" + table);


		SyncData syncData = loadSyncData(getContext(), table);
		if (syncData == null) {
			syncData = new SyncData();
			syncData.tablename = table;
			syncData.mode = MODE_DOWNLOAD;
			syncData.lastValue = 0;
			syncData.updated = null;
			syncData.modified = null;
			insertSyncData(getContext(), syncData);
		}

		syncData.success = true;
		syncData.error = null;

		// --- Broadcast info to UI
		info(COMMAND_START_SINGLE_SYNC, syncData);

		KiteDatabaseHelper db = null;
		try {

			db = KiteDatabaseHelper.getInstance(getContext());
			int processedCount = 0;
			int BUFFER_SIZE = 300;

			Log.i(TAG, "Table last synchronized=" + syncData.updated);

			// --- create HTTP Params
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 60000); // TODO config!!!
			HttpConnectionParams.setSoTimeout(httpParams, 60000); // TODO config!!!
			httpParams.setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));

			// --- create HTTP Client
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			BasicCookieStore cookieStore = new BasicCookieStore();
			httpClient.setCookieStore(cookieStore);

			long time = getTime(syncData.updated, true);
			String url = UriUtils.getTableSyncUrl(table, time) + "?limit=0";
			Log.i(TAG, "Start downloading:" + url);

			//--- Execute GET
			HttpGet request = new HttpGet(url);
			request.addHeader("Accept-Encoding", "gzip");
			HttpResponse response = httpClient.execute(request);

			Date lastUpdate = null;
			String modifiedStr = null;

			//--- Prepare image columns
			BaseTable<T> t = TableMap.getHandlerByClass(clazz);
			Column[] columns = t.getColumns();
			List<Column> imageColumns = getImageColumns(columns);

			//--- Get JSON stream
			JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(response.getEntity().getContent()), "UTF-8")));
			reader.beginObject();
			long ellapsed = System.currentTimeMillis();
			long update = System.currentTimeMillis();
			ContentValues [] buffer = new ContentValues[BUFFER_SIZE];

			while (reader.hasNext()) {
				final String name = reader.nextName();
				if ("success".equals(name)) {
					boolean success = reader.nextBoolean();
					if (!success) {
						syncData.success = false;
						syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása!";
						info(COMMAND_ERROR, syncData.error);
						return;
					}
				} else
				if ("data".equals(name)) {
					reader.beginArray();

					int bufferPos = 0;
					//--- Start parsing items...
					while (reader.hasNext()) {
						reader.beginObject();
						processedCount++;

						ContentValues map = new ContentValues();
						while (reader.hasNext()) {
							String key = reader.nextName();
							boolean isNull = reader.peek() == JsonToken.NULL;
							String value = null;
							if (!isNull) {
								value = reader.nextString();
							} else {
								reader.skipValue();
							}
							if ("modified".equals(key)) {
								modifiedStr = value;

							} else
							if ("state".equals(key)) {
								key = "status";
							} else
							if (!MetaDataTable.TABLE_NAME.equals(table) && ("id".equals(key) || "type".equals(key) || "text".equals(key))) {
								continue;
							}

							map.put(key, value);
							buffer[bufferPos] = map;
						}
						reader.endObject();

						map = handler.postUpdate(orm, map);
						if(map == null){
							buffer[bufferPos] = null;
						}

						bufferPos++;

						if (bufferPos == BUFFER_SIZE) {
							bulkInsert(db, table, buffer);
							bufferPos = 0;
							buffer = new ContentValues[BUFFER_SIZE];
						}

						// ---Check images to download
						if (imageColumns != null) {
							for (Column column : imageColumns) {
								String imageName = map.getAsString(column.name);
								if (imageName != null) {
									String[] images = imageName.split(";");
									for (String image : images) {
										if (!ImageUtils.isImageExists(image)) {
											Log.e(TAG, "Downloading image=" + imageName);
											info(COMMAND_DOWNLOADIMAGE, imageName);
											ImageUtils.downloadImage(getContext(), image);
										} else {
											Log.i(TAG, "Skipping image, already downloaded:" + image);
										}
									}
								}
							}
						}

						if (System.currentTimeMillis() - update >= 500) {
							update = System.currentTimeMillis();
							info(COMMAND_CURRENTITEM, processedCount);
						}

						if (System.currentTimeMillis() - ellapsed >= 5000) {
							lastUpdate = DateUtils.getDfLong().parse(modifiedStr);
							if (lastUpdate != null) {
								ellapsed = System.currentTimeMillis();
								Log.e(TAG, table + " LAST UPDATED[" + lastUpdate.getTime() + "]=" + lastUpdate + ", processedCount=" + processedCount);
								syncData.updated = lastUpdate;
								syncData.modified = new Date();
								syncData.success = true;
								syncData.error = "OK";
								insertSyncData(getContext(), syncData);
							} else {
								Log.e(TAG, "Modified date is null!!!");
							}
						}
					}
					reader.endArray();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();

			bulkInsert(db, table, buffer);

			if (modifiedStr != null) {
				lastUpdate = DateUtils.getDfLong().parse(modifiedStr);
				syncData.updated = lastUpdate;
			}
			syncData.modified = new Date();
			syncData.success = true;
			syncData.error = "OK";
			insertSyncData(getContext(), syncData);
			Log.e(TAG, table + " LAST UPDATED=" + lastUpdate + ", processedCount=" + processedCount);
			reader.close();
			info(COMMAND_CURRENTITEM, processedCount);

		} catch (Throwable e) {
			Log.e(TAG, "Error synchronizing table:" + table, e);
			syncData.success = false;
			syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + e;
			info(COMMAND_ERROR, syncData.error);
			info(COMMAND_END_SINGLE_SYNC, syncData);
			throw new SyncException("Error synchronizing table:" + table, e);
		} finally {
			Log.d(TAG, "Table synchronized from server:" + table);
		}
	}

	private void closeDatabase(KiteDatabaseHelper db) {
		if (db != null) {
			try {
				db.closeDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void bulkInsert(KiteDatabaseHelper db, String table, ContentValues[] buffer) {
		Log.d(TAG, "Bulk insert " + buffer.length + " records.");
		long start = System.currentTimeMillis();
		SQLiteDatabase dw = db.getWritableDatabase();
		try {
			dw.beginTransaction();
			int count = 0;
			for (int i = 0; i < buffer.length; i++) {
				ContentValues value = buffer[i];
				if (value == null) {
					break;
				}
				dw.insertWithOnConflict(table, null, value, SQLiteDatabase.CONFLICT_REPLACE);
				count++;
			}
			Log.d(TAG, count + " records inserted.");
			dw.setTransactionSuccessful();
		} finally {
			dw.endTransaction();
		}
		Log.d(TAG, "Bulk insert take " + (System.currentTimeMillis() - start) + "ms.");

	}

	private <T extends BaseDatabaseObject> void syncTableDownload(KiteORM orm, Class<T> clazz,
																  int pageCount) throws SyncException {

		Log.i(TAG, "syncTableDownload().start()");
		BaseTable<T> handler = TableMap.getHandlerByClass(clazz);
		if (handler == null) {
			throw new SyncException("No database Handler found for class=" + clazz);
		}

		String table = handler.getTableName();
		Log.i(TAG, "syncTableDownload().table=" + table);


		SyncData syncData = loadSyncData(getContext(), table);
		if (syncData == null) {
			syncData = new SyncData();
			syncData.tablename = table;
			syncData.mode = MODE_DOWNLOAD;
			syncData.lastValue = 0;
			syncData.updated = null;
			syncData.modified = null;
			insertSyncData(getContext(), syncData);
		}

		syncData.success = true;
		syncData.error = null;

		// --- Broadcast info to UI
		info(COMMAND_START_SINGLE_SYNC, syncData);

		try {

			Log.i(TAG, "Table last synchronized=" + syncData.updated);

			RestfulResult result = null;
			int iteration = 0;
			int processedCount = 0;
			long lastGet = 0;
			while (iteration++ < 100) { // MAX 100 iteration
				Date lastUpdate = null;
				try {
					info(COMMAND_DOWNLOADDATA, "S");
					long time = getTime(syncData.updated);
					if (lastGet != 0 && time == lastGet) {
						time++;
						Log.e(TAG, "INCREASING TIME DUE SAME RESULT DATE! New date:" + time);
					}

					result = RestfulClient.doRead(getContext(), UriUtils.getTableSyncUrl(table, time));
					lastGet = time;
					info(COMMAND_DOWNLOADDATA, "E");
					if (!result.isHttpOk()) {
						syncData.success = false;
						syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + result;
						info(COMMAND_ERROR, syncData.error);
						Log.e(TAG, "HTTP error " + result.httpCode + " in table sync:" + result.httpError);
						return;
					}
				} catch (Exception e) {
					Log.e(TAG, "Error synchronizing table:" + table, e);
					syncData.success = false;
					syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + e;
					info(COMMAND_ERROR, syncData.error);
					return;
				}

				result.responseData = result.responseData.replaceAll(":\"\"", ":null");

				JsonResponse response =
					RestfulClient.getGSON().fromJson(result.responseData, JsonResponse.class);
				if (response == null) {
					Log.e(TAG, "Empty response from server. Table:" + table);
					syncData.success = false;
					syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + result;
					info(COMMAND_ERROR, syncData.error);
					return;
				}

				if (!response.success) {
					Log.e(TAG, "Request failed. Server returns with error! Status=" + response.success
						+ ", Error:" + response.error);
					syncData.success = false;
					syncData.error = "Nem sikerült a " + table + " tábla szinkronizálása! Hiba: " + response;
					info(COMMAND_ERROR, syncData.error);
					return;
				}

				if (response.data != null) {

					info(COMMAND_ITEMCOUNT, response.data.size());
					if (response.data.size() == 0) {
						Log.e(TAG, "DOWNLOAD SYNCHRONIZATION END FOR TABLE=" + table + ". NO NEW RECORDS.");
						syncData.updated = new Date();
						Log.e(TAG, "+++++lastUpdate UPDATED TO:" + syncData.updated);
						insertSyncData(getContext(), syncData);
						break;
					}

					int counter = 0;
					final int length = response.data.size();
					Log.e(TAG, "Number of JSON elements in response=" + length);
					List<T> items = new ArrayList<T>();

					for (int i = 0; i < length; i++) {
						T item = null;
						try {
							item = RestfulClient.getGSON().fromJson(response.data.get(i), clazz);
							item = handler.postUpdate(orm, item);
							items.add(item);
							counter++;
							processedCount++;
						} catch (Exception e) {
							Log.e(TAG, "Invalid json:" + response.data.get(i) + ", Error=" + e);
							continue;
						}

						if (lastUpdate == null || lastUpdate.before(item.modified)) {
							lastUpdate = item.modified;
						}

						if (counter % 1000 == 0 || i == length - 1) { // Every 500 record or on the last item
							Log.i(TAG, "Bulk inserting " + items.size() + " record...");
							info(COMMAND_CURRENTITEM, processedCount);
							info(COMMAND_START_INSERT, items.size());
							orm.bulkInsert(items);
							items.clear();
							Log.i(TAG, "Bulk insert complete. Total items inserted:" + counter);
							info(COMMAND_END_INSERT, counter);
						}

						if (counter % 76 == 0 || i == length - 1) {
							info(COMMAND_CURRENTITEM, processedCount);
						}

						// ---Check images to download
						BaseTable<T> t = TableMap.getHandlerByClass(clazz);
						Column[] columns = t.getColumns();
						List<Column> imageColumns = getImageColumns(columns);
						if (imageColumns != null) {
							ContentValues values = t.getContentValues(item);
							for (Column column : imageColumns) {
								String imageName = values.getAsString(column.name);
								if (imageName != null) {
									Log.e(TAG, "Downloading image=" + imageName);
									info(COMMAND_DOWNLOADIMAGE, imageName);
									String[] images = imageName.split(";");
									for (String image : images) {
										if (!ImageUtils.isImageExists(image)) {
											ImageUtils.downloadImage(getContext(), image);
										} else {
											Log.i(TAG, "Skipping image, already downloaded:" + image);
										}
									}
								}
							}
						}
					}

					syncData.updated = lastUpdate;
					syncData.modified = new Date();
					Log.e(TAG, table + " LAST UPDATED=" + lastUpdate + ", MS="
						+ (lastUpdate == null ? 0 : lastUpdate.getTime()));
					syncData.success = true;
					syncData.error = "OK";
					insertSyncData(getContext(), syncData);

					if (length < pageCount) {
						Log.e(TAG, "DOWNLOAD SYNCHRONIZATION END FOR TABLE=" + table + ", LASTUPDATEDRECORD="
							+ lastUpdate + ", PAGECOUNT=" + pageCount + ", LENGTH=" + length);

						syncData.updated = new Date();
						Log.e(TAG, "+++++lastUpdate UPDATED TO:" + lastUpdate);
						insertSyncData(getContext(), syncData);
						break;
					}

				} else {
					Log.e(TAG, "DOWNLOAD SYNCHRONIZATION END FOR TABLE=" + table + ", LASTUPDATEDRECORD="
						+ lastUpdate);
					break;
				}
			}

			syncPostProcess(orm, handler);


		} finally {
			Log.d(TAG, "Table synchronized from server:" + table);

			if (!syncData.success) {
				insertSyncData(getContext(), syncData);
			}

			// --- Broadcast info to UI
			info(COMMAND_END_SINGLE_SYNC, syncData);
			Log.d(TAG, "Sync data saved:" + syncData);
		}
	}

	private List<Column> getImageColumns(Column[] columns) {

		if (columns == null) {
			return new ArrayList<Column>();
		}

		ArrayList<Column> result = new ArrayList<Column>();
		for (Column column : columns) {
			if (column.type == Type.RESOURCE) {
				result.add(column);
			}
		}

		return result.isEmpty() ? null : result;
	}

	@SuppressWarnings("unchecked")
	private static SyncData loadSyncData(Context context, String tablename) {

		BaseTable<SyncData> sync_handler =
			(BaseTable<SyncData>) TableMap.getHandlerByTablename(SyncDataTable.TABLE_NAME);
		KiteDatabaseHelper db = KiteDatabaseHelper.getInstance(context);
		try {
			Cursor cursor =
				db.getReadableDatabase().query(SyncDataTable.TABLE_NAME, null, "tablename = ?",
					new String[]{tablename}, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				try {
					return sync_handler.getDataFromCursor(cursor);
				} finally {
					cursor.close();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error loading record from synctable.", e);
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static void insertSyncData(Context context, SyncData data) {

		BaseTable<SyncData> syncHandler =
			(BaseTable<SyncData>) TableMap.getHandlerByTablename(SyncDataTable.TABLE_NAME);
		KiteDatabaseHelper db = KiteDatabaseHelper.getInstance(context);
		try {
			db.getWritableDatabase().beginTransaction();
			db.getWritableDatabase().insertWithOnConflict(SyncDataTable.TABLE_NAME, null,
				syncHandler.getContentValues(data), SQLiteDatabase.CONFLICT_REPLACE);
			db.getWritableDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, "Error insert syncdata=" + data, e);

		} finally {
			db.getWritableDatabase().endTransaction();
		}
	}

	protected void info(int command, Serializable data) {
		Intent intent = new Intent(BROADCAST_SYNC);
		intent.putExtra("COMMAND", command);
		intent.putExtra("DATA", data);
		getContext().sendBroadcast(intent);
	}

	public static void checkSyncDataDefaultsTable(Context context) {

		try {
			KiteORM orm = new KiteORM(context);
			for (BaseTable<?> table : TableMap.getTableHandles()) {

				String tableName = table.getTableName();
				Log.i(TAG, "Checking SYNC DATA TABLE:" + tableName);
				if (table.getDirection() == BaseTable.UPLOAD) {
					SyncData syncData = loadSyncData(context, table.getTableName());
					if (syncData == null) {
						syncData = new SyncData();
						syncData.tablename = tableName;
						syncData.mode = MODE_UPLOAD;
						syncData.lastValue = -1;
						syncData.updated = null;
						orm.insert(syncData);
					}
				} else if (table.getDirection() == BaseTable.DOWNLOAD) {
					SyncData syncData = loadSyncData(context, table.getTableName());
					if (syncData == null) {
						syncData = new SyncData();
						syncData.tablename = tableName;
						syncData.mode = MODE_DOWNLOAD;
						syncData.lastValue = 0;
						syncData.updated = null;
						orm.insert(syncData);
					}
				}

			}
		} catch (Exception e) {
			Log.e(TAG, "Error checking tables", e);
		}

	}

	public static long getTime(Date date) {
		return getTime(date, false);
	}


	public static long getTime(Date date, boolean ms) {

		if (date != null) {
			try {
				Date newDate = DFZ.parse(DF.format(date));
				if (!ms) {
					return newDate.getTime() / 1000;
				} else {
					return newDate.getTime();
				}

			} catch (Exception e) {
				Log.e(TAG, "Error getting time", e);
			}
		}

		return 0;
	}
}
