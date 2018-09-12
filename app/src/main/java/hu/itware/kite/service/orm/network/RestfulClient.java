package hu.itware.kite.service.orm.network;

import hu.itware.kite.service.orm.model.BaseDatabaseObject;
import hu.itware.kite.service.utils.SystemUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class RestfulClient {

	private static final String TAG = "KITE.REST";

	private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	
	private static String sessionID;

	private RestfulClient() {

	}

	public static <T extends BaseDatabaseObject> String toJson(T data) {
		return GSON.toJson(data);
	}

	public static <T extends BaseDatabaseObject> String toJson(List<T> data) {
		return GSON.toJson(data);
	}

	public static <T extends BaseDatabaseObject> T fromJson(Class<T> clazz, String json) {
		return GSON.fromJson(json, clazz);
	}
	
	public static Gson getGSON() {
		return GSON;
	}

	public static RestfulResult doCreate(Context context, String url, String data) throws IOException {
		return doRequest(context, "POST", url, data, true);
	}

	public static RestfulResult doRead(Context context, String url) throws IOException {
		return doRequest(context, "GET", url, null, true);
	}

	public static RestfulResult doUpdate(Context context, String url, String data) throws IOException {
		return doRequest(context, "PUT", url, data, true);
	}

	public static RestfulResult doDelete(Context context, String url, String data) throws IOException {
		return doRequest(context, "DELETE", url, data, true);
	}

	public static final RestfulResult doRequest(Context context, String method, String url, String data, boolean simple) throws IOException {
		return	doRequest(context, method, url, data, simple, false);
	}

	public static final RestfulResult doRequest(Context context, String method, String url, String data, boolean simple, boolean nolog) throws IOException {

		//Log.e(TAG, "[" + method + "].URL=" + url);
		//Log.e(TAG, "[" + method + "].DATA=" + data);
		BufferedReader reader = null;
		try {

			// --- create HTTP Params
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000); // TODO config!!!
			HttpConnectionParams.setSoTimeout(httpParams, 10000); // TODO config!!!
			HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.BROWSER_COMPATIBILITY);
			httpParams.setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
			
			// --- create HTTP Client
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			BasicCookieStore cookieStore = new BasicCookieStore();
			httpClient.setCookieStore(cookieStore);


			// --- create HTTP Method
			HttpRequestBase request = null;			
			if ("POST".equals(method)) {
				request = new HttpPost(url);
			} else 
			if ("PUT".equals(method)) {
				request = new HttpPut(url);
			} else 
			if ("DELETE".equals(method)) {
				request = new HttpDelete(url);
			} else {
				request = new HttpGet(url);				
			}
			
			request.setHeader("imei", SystemUtils.getImei(context));
			request.setHeader("Content-Type", "application/json; charset=utf8");
			
			if (data != null) {
				
				if (simple) {
					StringEntity entity = new StringEntity(data, "UTF8");
					((HttpEntityEnclosingRequestBase) request).setEntity(entity);
				} else {
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("data", data));
	
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF8");
					((HttpEntityEnclosingRequestBase) request).setEntity(entity);
				}
			}
			
			if (sessionID != null) {
				request.setHeader("Cookie", sessionID);
				Log.i(TAG, "SessionID=" + sessionID);
			}
			 
			HttpResponse response = httpClient.execute(request);
			sessionID = getSessionId(response.getAllHeaders());
			
			RestfulResult result = new RestfulResult();
			result.httpCode = response.getStatusLine().getStatusCode();
			result.httpError = response.getStatusLine().getReasonPhrase();
			
//			if (response.getStatusLine() != null) {
//				Log.i(TAG, "\tStatusCode:" + response.getStatusLine().getStatusCode());
//				Log.i(TAG, "\tReasonPhrase:" + response.getStatusLine().getReasonPhrase());
//			}
			
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (int c; (c = reader.read()) != -1;) {
				builder.append((char) c);
	        }
//
			reader.close();
			result.responseData =  builder.toString();
			builder = null;
			return result;


		} catch (IOException e) {
			
			if (!nolog) {
				Log.e(TAG, "Error in server communication:" + e);
			}

			throw e;
			
		} finally {
			closeStream(reader);
		}
	}
	
	protected static void printHeaders(Header [] headers) {
		
		Log.i(TAG, "HEADERS:");
		for (Header h : headers) {
			Log.i(TAG, "\t" + h.getName() + ":" + h.getValue());
		}
	}
	
	private static String getSessionId(Header [] headers) {
		
		for (int i = 0; headers != null && i < headers.length; i++) {
			Header h = headers[i];
			if ("Set-Cookie".equals(h.getName())) {
				String sessionID = h.getValue();
				Log.i(TAG, "Set-cookie:" + sessionID);
				return sessionID;
			}
		}
		
		return null;
	}

	private static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (Exception e) {
				// Nothing to do
				Log.e(TAG, "Could not close stream", e);
			}
		}
	}
}
