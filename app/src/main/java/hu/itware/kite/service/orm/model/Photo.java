package hu.itware.kite.service.orm.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.utils.BitmapUtils;

public class Photo {

	public static final int DEFAULT_WIDTH = 320;
	public static final int DEFAULT_HEIGHT = 480;

	private static final String TAG = "Photo";

	private Context context;
	private File imageFile;
	private String imageFileName;

	private int bitmapWidth;
	private int bitmapHeight;

	public static Photo create(Context context){
		return createWithSize(context, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
	}

	public static Photo createWithSize(Context context, int bitmapWidth, int bitmapHeight, String name){

		Photo photo = new Photo(context,bitmapWidth,bitmapHeight);

		try {
			photo.createPhotoFile(name);
		} catch (IOException e) {
			Log.v(TAG, e.getLocalizedMessage(), e);
			return null;
		}

		return photo;
	}

	public static Photo createWithName(Context context, String name) {
		return createWithSize(context, DEFAULT_WIDTH, DEFAULT_HEIGHT, name);
	}

	protected Photo (Context context,int bitmapWidth, int bitmapHeight){
		this.context = context;
		this.bitmapWidth = bitmapWidth;
		this.bitmapHeight = bitmapHeight;
	}


	public Uri getUri(){
		return Uri.fromFile(imageFile);
	}

	public Bitmap getBitmap(){
		if (imageFile.getAbsolutePath() != null) {
			return BitmapUtils.decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), bitmapWidth, bitmapHeight);
		}
		return null;
	}

	public Bitmap getResizedBitmap(int width, int height) {
		if (imageFile.getAbsolutePath() != null) {
			return BitmapUtils.decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), width, height);
		}
		return null;
	}

	private void createPhotoFile(String name) throws IOException{

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		imageFileName = name!=null?name:"REGION_REPORT_" + timeStamp + ".jpg";
		File storageDir = ImageUtils.getPath(context, true);
		imageFile = new File(storageDir, imageFileName);
		imageFile.createNewFile();
	}
	public boolean exists() {
		return imageFile.exists() && imageFile.length() > 0;
	}

	public void clear() {
		if (imageFile != null && imageFile.exists()) {
			imageFile.delete();
		}
	}

	public String getBase64String() throws IOException {

		if (imageFile != null){
			throw new IOException("File don't exist! Please take the photo again!");
		}

		StringBuilder result = new StringBuilder();
		try {
			byte[] byteArray = new byte[(int) imageFile.length()];
			FileInputStream fis = new FileInputStream(imageFile);
			while ((fis.read(byteArray)) != -1) {
				result.append(Base64.encodeToString(byteArray, Base64.DEFAULT));
			}
			fis.close();
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage(), e);
		}

		return result.toString();
	}

	public String getImageFileName() {
		return imageFileName;
	}
}
