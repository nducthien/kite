package hu.itware.kite.service.orm.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.orm.model.Konfig;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.utils.DownloadUtils;
import hu.itware.kite.service.utils.FileUtils;
import hu.itware.kite.service.utils.MetaDataTypes;

import static hu.itware.kite.service.settings.Settings.HTTP_CONNECTION_TIMEOUT_MS;

public final class ImageUtils {

	public static final URI UPLOAD_URI = URI.create(Settings.SERVER_UPLOAD_URL);
	public static final URI DOWNLOAD_URI = URI.create(Settings.SERVER_DOWNLOAD_URL);
    private static final String TAG = "KITE.IMG";
    private static final String FUTUREDOWNLOAD_IMAGES_PREF_NAME = "PREF_IMAGES";

	private ImageUtils() {

	}

	public static File[] listUploadableImages(Context context) {

		ArrayList<File> files = new ArrayList<File>();
		File path = null;

		// Return uploadable images from removable SD card (if any)
		File[] dirs = ContextCompat.getExternalFilesDirs(context, Settings.UPLOAD_IMAGES_DIR);
		for (File dir : dirs) {
			if (dir != null && dir.getAbsolutePath().contains("sdcard")) {
				path = dir;
				break;
			}
		}

		if (path != null && path.isDirectory()) {
			files.addAll(Arrays.asList(path.listFiles()));
		}

		// Return uploadable images from primary external storage (if any)
		path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_DIR);

		if (path.isDirectory()) {
			files.addAll(Arrays.asList(path.listFiles()));
		}

		return files.isEmpty() ? null : files.toArray(new File[files.size()]);
	}
	
	public static Collection<String> getImagesToNeedDownload(Context context) {
		SharedPreferences sp = context.getSharedPreferences(FUTUREDOWNLOAD_IMAGES_PREF_NAME, Context.MODE_PRIVATE);
		return sp.getAll().keySet();
	}
	
	public static void removeImageFromFutureDownload(Context context, String name) {
		Log.i(TAG, "Removing image from future download:" + name);
		SharedPreferences sp = context.getSharedPreferences(FUTUREDOWNLOAD_IMAGES_PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.remove(name);
		editor.apply();
	}
	
	public static File[] listDownloadedImages() {

		File path = Environment.getExternalStoragePublicDirectory(Settings.LOCAL_IMAGES_DIR);
		if (path.isDirectory()) {
			return path.listFiles();
		}

		return new File[0];
	}

	public static Bitmap loadImageWithResize(File imageFile, int reqWidth, int reqHeight) {

		try {
			return decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), reqWidth, reqHeight);
		} catch (Exception e) {
			Log.e(TAG, "Error loading image " + imageFile.getAbsolutePath() + ", Error:" + e, e);
			return null;
		}
	}

	public static Bitmap loadUploadableImage(String name) {

		try {
			File path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_DIR);
			if (!path.exists()) {
				path.mkdirs();
			}
			File imageFile = new File(path, name);
			FileInputStream in = new FileInputStream(imageFile);
			return BitmapFactory.decodeStream(in);

		} catch (Exception e) {
			Log.e(TAG, "Error loading image " + name + ", Error:" + e, e);
			return null;
		}
	}


	public static Bitmap loadUploadableImageWithResize(Context context, String name, int reqWidth, int reqHeight, boolean isTemp) {

		try {
			File path = getPath(context, isTemp);
			File imageFile = new File(path, name);

			// Try to find picture in another location
			if(!imageFile.exists()){
				path = getPath(context, !isTemp);
				imageFile = new File(path, name);
			}

			return decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), reqWidth, reqHeight);

		} catch (Exception e) {
			Log.e(TAG, "Error loading image " + name + ", Error:" + e, e);
			return null;
		}
	}

	public static Bitmap loadImage(Context context, String name) {

		try {
			File imageFile = new File(ImageUtils.getPath(context, false), name);
			if (!imageFile.exists()) {
				imageFile = new File(ImageUtils.getPath(context, true), name);
			}
			if (imageFile.exists()) {
				//File path = Environment.getExternalStoragePublicDirectory(Settings.LOCAL_IMAGES_DIR);
				//File imageFile = new File(path, name);
				FileInputStream in = new FileInputStream(imageFile);
				return BitmapFactory.decodeStream(in);
			}

		} catch (Exception e) {
			Log.e(TAG, "Error loading image " + name + ", Error:" + e, e);
		}
		return null;
	}

	public static Drawable loadImageDrawable(String name) {

		try {
			File path = Environment.getExternalStoragePublicDirectory(Settings.LOCAL_IMAGES_DIR);
			File imageFile = new File(path, name);
			return Drawable.createFromPath(imageFile.toString());

		} catch (Exception e) {
			Log.e(TAG, "Error loading image " + name + ", Error:" + e, e);
			return null;
		}
	}


//
//    public static boolean saveImage(Bitmap image, String name) {
//		FileOutputStream fos = null;
//		try {
//			fos = getFileOutputStream(name);
//			image.compress(Bitmap.CompressFormat.PNG, 100, fos);
//			return true;
//
//		} catch (Exception e) {
//			Log.e(TAG, "Error saving image " + name + ", Error:" + e, e);
//			return false;
//
//		} finally {
//			closeStream(fos);
//		}
//	}

	public static boolean saveImage(Context context, byte[] byteArray, String name) {
		FileOutputStream fos = null;
        try {
            fos = getFileOutputStream(context, name);
            fos.write(byteArray);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error saving image " + name + ", Error:" + e, e);
            deleteUploadableImage(context, name);
            return false;

        } finally {
            closeStream(fos);
        }
	}

	public static File getPath(Context context) {
		File path = getPath(context, false);
		if (!path.exists()) {
			return getPath(context, true);
		}
		return path;
	}

    public static File getPath(Context context, boolean isTemp) {
        File path = null;
        File[] dirs = ContextCompat.getExternalFilesDirs(context, isTemp ? Settings.UPLOAD_IMAGES_TEMP_DIR : Settings.UPLOAD_IMAGES_DIR);
        for (File dir : dirs) {
            if (dir != null && dir.getAbsolutePath().contains("sdcard")) {
                path = dir;
                break;
            }
        }
        if (path == null) {
            path = Environment.getExternalStoragePublicDirectory(isTemp ? Settings.UPLOAD_IMAGES_TEMP_DIR : Settings.UPLOAD_IMAGES_DIR);
        }
        if (!path.exists()) {
            path.mkdirs();
        }
        Log.e(TAG, "path: " + path.getAbsolutePath());
        return path;
    }

    private static FileOutputStream getFileOutputStream(Context context, String name) throws FileNotFoundException {
       /*
        File path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_DIR);
        if (!path.exists()) {
            path.mkdirs();
        }
        File imageFile = new File(path, name);
        return new FileOutputStream(imageFile);
        */
		// Return uploadable images from removable SD card (if any)
		File path = null;
		File[] dirs = ContextCompat.getExternalFilesDirs(context, Settings.UPLOAD_IMAGES_TEMP_DIR);
		for (File dir : dirs) {
			if (dir != null && dir.getAbsolutePath().contains("sdcard")) {
				path = dir;
				break;
			}
		}

		if (path != null && path.isDirectory()) {
			return new FileOutputStream(new File(path, name));
		}

		// Return uploadable images from primary external storage (if any)
		path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_TEMP_DIR);
		if (!path.exists()) {
			path.mkdirs();
		}
		Log.i(TAG, "Image path " + path);
		return new FileOutputStream(new File(path, name));
    }

	private static void closeStream(Closeable out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				Log.i(TAG, "Error closing stream=" + e);
			}
		}
	}

	public static boolean downloadImage(Context context, String imageName) {
		return DownloadUtils.downloadFile(context, imageName, Settings.SERVER_DOWNLOAD_URL, Settings.LOCAL_IMAGES_DIR, "Kép letöltése: ", FUTUREDOWNLOAD_IMAGES_PREF_NAME);
	}

	private static void closeConnection(HttpURLConnection conn) {
		try {
			if (conn != null) {
				conn.disconnect();
			}
		} catch (Exception e) {
			Log.i(TAG, "Error closing connection=" + e);
		}
	}

	public static String uploadImage(File imageFile) {

		if (imageFile == null || !imageFile.exists() || imageFile.length() < 1) {
			Log.w(TAG, "Uploadable file is invalid. File=" + imageFile + ", length=" + imageFile.length());
			if (imageFile.exists()) {
				imageFile.delete();
			}
			return null;
		}

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		FileInputStream fileInputStream = null;
		try {

			if (!imageFile.exists()) {
				Log.e(TAG, "Image not found in storage=" + imageFile);
				return "error";
			}
			
			String fileName = imageFile.getName();
			fileInputStream = new FileInputStream(imageFile);

			URL url = UPLOAD_URI.toURL();
			Log.e(TAG, "Image uploading to=" + url);
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(HTTP_CONNECTION_TIMEOUT_MS);
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);
			conn.setChunkedStreamingMode(128 * 1024);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);

			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			bytesAvailable = fileInputStream.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			//int logStepSize = 1024 * 1024; // Log every 1Mb data upload
			//int logStep = 0;

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {

				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			int statusCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			Log.i(TAG, "HTTP Response is : " + serverResponseMessage + ": " + statusCode);

			if (statusCode != HttpStatus.SC_OK) {
				Log.i(TAG, "Upload failed. Status code=" + statusCode + ", responseMessage:" + serverResponseMessage);
				return "error";
			}

			Log.i(TAG, "Upload successfull.");
			return null;

		} catch (Exception e) {
			Log.e(TAG, "Error uploading image=" + imageFile, e);
			return e.getLocalizedMessage();
		} finally {
			closeStream(fileInputStream);
		}
	}

	public static void downloadPip(String pipkodName, File pipFile){
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			String pipUrl = Settings.SERVER_DOWNLOAD_PIP_URL + pipkodName + ".pdf";
			URI urlUri = URI.create(Uri.parse(pipUrl).toString());
			URL url = urlUri.toURL();
			connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT_MS);
            connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return;
			}

			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(pipFile.toString());

			byte data[] = new byte[4096];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
		} catch (Exception e) {
			return ;
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}

			if (connection != null)
				connection.disconnect();
		}
	}

	public static boolean isUploadableImageExists(Context context, String fileName) {
		return new File(Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_TEMP_DIR), fileName).exists() ||
				new File(FileUtils.getSDCardWritableDirectory(context, Settings.UPLOAD_IMAGES_TEMP_DIR), fileName).exists() ||
				new File(FileUtils.getSDCardReadableDirectory(Settings.UPLOAD_IMAGES_TEMP_DIR), fileName).exists();
	}

	public static boolean isImageExists(String fileName) {

		File path = Environment.getExternalStoragePublicDirectory(Settings.LOCAL_IMAGES_DIR);
		File imageFile = new File(path, fileName);
		return imageFile.exists();
	}

	public static boolean isUploadableImageExists(Uri imageUri) {

		String fileName = imageUri.getLastPathSegment();
		File path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_DIR);
		File imageFile = new File(path, fileName);
		return imageFile.exists() && imageFile.length() > 0;
	}

	public static boolean isImageExists(Uri imageUri) {

		String fileName = imageUri.getLastPathSegment();
		File path = Environment.getExternalStoragePublicDirectory(Settings.LOCAL_IMAGES_DIR);
		File imageFile = new File(path, fileName);
		return imageFile.exists()  && imageFile.length() > 0;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap sourceBitmap = BitmapFactory.decodeFile(filename, options);
		try {
			ExifInterface exif = new ExifInterface(filename);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			Matrix matrix = new Matrix();
			switch (orientation){
				case 3:
					matrix.postRotate(180);
					return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
				case 6:
					matrix.postRotate(90);
					return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
				case 8:
					matrix.postRotate(270);
					return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
				default:
					return sourceBitmap;
			}
		} catch (IOException e){
			Log.e(TAG, "Could not read bitmap", e);
			return sourceBitmap;
		}
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static void deleteUploadableImage(Context context, String fileName) {
		File imageFile = new File(Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_TEMP_DIR), fileName);
		if (imageFile != null && imageFile.exists()) {
			imageFile.delete();
			Log.i(TAG, "Delete imageFile= " + imageFile);
			return;
		}
		imageFile = new File(FileUtils.getSDCardWritableDirectory(context, Settings.UPLOAD_IMAGES_TEMP_DIR), fileName);
		if (imageFile != null && imageFile.exists()) {
			imageFile.delete();
			Log.i(TAG, "Delete imageFile= " + imageFile);
			return;
		}
		imageFile = new File(FileUtils.getSDCardReadableDirectory(Settings.UPLOAD_IMAGES_TEMP_DIR), fileName);
		if (imageFile != null && imageFile.exists()) {
			imageFile.delete();
			Log.i(TAG, "Delete imageFile= " + imageFile);
			return;
		}
	}
}
