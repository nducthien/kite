package hu.itware.kite.service.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Created by szeibert on 2017.06.22..
 */

public class DownloadUtils {

    private static final String TAG = "DownloadUtils";

    public static boolean downloadFile(Context context, String name, String baseUrl, String destDir, String description, String futurePrefName) {

        try {
            String fileUrl = baseUrl + name;
            Uri url = Uri.parse(fileUrl);
            Log.i(TAG, "downloadFile=" + url);
            String fileName = url.getLastPathSegment();
            Log.i(TAG, "downloadFile.filename=" + fileName);

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(url);

            //if (Network.getNetworkState(context) != Network.STATE_WIFI) {
            if (Network.getNetworkState(context) == Network.STATE_NONETWORK) {
                addFileToFutureDownload(context, name, futurePrefName);
                return true;
            }

            if (name.contains("/")) {
                destDir = destDir + name.substring(0, name.lastIndexOf("/"));
                fileName = name.substring(name.lastIndexOf("/"));
                new File(destDir).mkdirs();
            }

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle("KITE KÉTR").setDescription(description + fileName)
                    .setDestinationInExternalPublicDir(destDir, fileName).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            dm.enqueue(request);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error start downloading file...", e);
        }
        return false;
    }

    private static void addFileToFutureDownload(Context context, String name, String prefName) {
        Log.i(TAG, "Adding image to future download:" + name);
        SharedPreferences sp = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(name, name);
        editor.apply();
    }
}
