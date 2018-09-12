package hu.itware.kite.service.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public final class FileUtils {

	private static final String TAG = "KITE.FILE";

	private FileUtils() {

	}
	
	/**
	 * Creates the specified <code>toFile</code> as a byte for byte copy of the <code>fromFile</code>. If <code>toFile</code> already exists, then it will be replaced with a copy of
	 * <code>fromFile</code>. The name and path of <code>toFile</code> will be that of <code>toFile</code>.<br/> <br/> <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by this
	 * function.</i>
	 * 
	 * @param fromFile - FileInputStream for the file to copy from.
	 * @param toFile - FileInputStream for the file to copy to.
	 */
	public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = fromFile.getChannel();
			toChannel = toFile.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		} finally {
			try {
				if (fromChannel != null) {
					fromChannel.close();
				}
			} finally {
				if (toChannel != null) {
					toChannel.close();
				}
			}
		}
	}

	public static void copyFile(File src, File dst) throws IOException {
		Log.i(TAG, "CopyFile.src=" + src + ", dest=" + dst);
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
	public static Uri copyFile2(File src, File dst) throws IOException {
		Log.i(TAG, "CopyFile2.src=" + src + ", dest=" + dst);
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return Uri.fromFile(dst);
	}

	public static File getSDCardWritableDirectory(Context context, String directory) {
		File path = null;
		File[] dirs = ContextCompat.getExternalFilesDirs(context, directory);
		for (File dir : dirs) {
			if (dir != null && dir.getAbsolutePath().contains("sdcard")) {
				path = dir;
				break;
			}
		}
		if (path == null) {
			path = Environment.getExternalStoragePublicDirectory(directory);
		}
		return path;
	}

	public static File getSDCardReadableDirectory(String directory) {
		return new File("/storage/sdcard1" + (directory.startsWith("/")?directory:"/"+directory));
	}

}
