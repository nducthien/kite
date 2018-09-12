package hu.itware.kite.service.services;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import hu.itware.kite.service.orm.network.JsonResult;
import hu.itware.kite.service.orm.network.RestfulClient;
import hu.itware.kite.service.orm.network.RestfulResult;
import hu.itware.kite.service.settings.Settings;

/**
 * Created by szeibert on 2015.09.09..
 */
public class UpdateService {

    private static final String TAG = "KITE.UPDATE";

    public static void checkUpdates(final Activity context, final String version, final UpdateAvailableListener updateAvailableListener) {
        Log.i(TAG, "checkUpdates()=" + version);
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "checkUpdates().url=" + Settings.SERVER_UPDATE_URL + version);
                    RestfulResult result = RestfulClient.doRequest(context, "GET", Settings.SERVER_UPDATE_URL + version, null, true, true);

                    Log.i(TAG, "checkUpdates().httpOk()=" + result.isHttpOk());
                    if (!result.isHttpOk()) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAvailableListener.onUpdateNotAvailable();
                            }
                        });
                        return;
                    }

                    final UpdateAvailableResponse updateAvailableResponse = RestfulClient.getGSON().fromJson(result.responseData, UpdateAvailableResponse.class);
                    if (updateAvailableResponse.data.updateAvailable) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAvailableListener.onUpdateAvailable(updateAvailableResponse.data.path);
                            }
                        });
                    } else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAvailableListener.onUpdateNotAvailable();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.i(TAG, "CheckUpdate failed: " + (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException?"timeout":"unknown error"));
                    updateAvailableListener.onFailure(e);
                }
            }
        });
        updateThread.start();
    }

    public static void downloadUpdate(final Activity context, final String path, final ProgressBar progressBar, final UpdateDownloadedListener updateDownloadedListener) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        Thread downloadThred = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int timeout = 5000;
                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
                    HttpConnectionParams.setSoTimeout(httpParams, timeout);
                    HttpClient client = new DefaultHttpClient(httpParams);
                    URL tempUrl = new URL(Settings.SERVER_BASE_URL);
                    String url = tempUrl.getProtocol() + "://" + tempUrl.getHost();
                    if (tempUrl.getPort() > 88) {
                        url += ":" + tempUrl.getPort();
                    }
                    url += path;

                    Log.i(TAG, "downloadUpdate().from=" + url);

                    HttpGet httpGet = new HttpGet(url);

                    HttpResponse response = client.execute(httpGet);
                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode();

                    Log.i(TAG, "downloadUpdate().responseCode=" + statusCode);

                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream is = entity.getContent();

						final int file_length = (int) entity.getContentLength();
						if (progressBar != null) {
							progressBar.post(new Runnable() {
								@Override
								public void run() {
									progressBar.setMax(file_length / 1024);
								}
							});
						}

                        String PATH = Environment.getExternalStorageDirectory() + "/download/";
                        Log.i(TAG, "downloadUpdate().saveTo().path=" + PATH);

                        File file = new File(PATH);
                        file.mkdirs();
                        final File outputFile = new File(file, path.substring(path.lastIndexOf("/")));
                        Log.i(TAG, "downloadUpdate().saveTo().file=" + outputFile);
                        FileOutputStream fos = new FileOutputStream(outputFile);

                        byte[] buffer = new byte[1024];
						int size = 0;
                        int len1 = 0;
						int counter_buff = 0;
                        while ((len1 = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len1);
							size += len1;

							if (progressBar != null) {
								counter_buff += len1;
								if (counter_buff > size / 1024 * 33) {
									counter_buff = 0;
									final int counter = size;
									progressBar.post(new Runnable() {
										@Override
										public void run() {
											progressBar.setProgress(counter / 1024);
										}
									});
								}
							}
                        }
                        fos.close();
                        is.close();

						if (progressBar != null) {
							progressBar.post(new Runnable() {
								@Override
								public void run() {
									progressBar.setProgress(file_length / 1024);
								}
							});
						}
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDownloadedListener.onSuccess(outputFile.getAbsolutePath());
                            }
                        });
                    } else {
                        updateDownloadedListener.onFailure(new IOException("Hiba a frissítésnél: " + path + "! HTTP Status code=" + statusCode));
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error downloading update", e);
                    updateDownloadedListener.onFailure(e);
                } finally {
                    if (progressBar != null) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }
        });
        downloadThred.start();

    }

    public interface UpdateAvailableListener {
        void onUpdateAvailable(String path);
        void onUpdateNotAvailable();
        void onFailure(Throwable e);
    }

    public interface UpdateDownloadedListener {
        void onSuccess(String localPath);
        void onFailure(Throwable e);
    }

    public static class UpdateAvailableResponse extends JsonResult{
        public UpdateAvailableData data;
    }

    public static class UpdateAvailableData {
        public boolean updateAvailable;
        public String version;
        public String path;
    }
}
