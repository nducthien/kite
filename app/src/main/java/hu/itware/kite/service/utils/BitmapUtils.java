package hu.itware.kite.service.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class BitmapUtils {

	private BitmapUtils() {

	}

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		float aspect = (float)options.outHeight / (float)options.outWidth;
		return Bitmap.createScaledBitmap(bitmap, reqWidth, Math.round(reqWidth * aspect), false);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}
}
