package hu.itware.kite.service.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.LruCache;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

import hu.itware.kite.service.orm.network.ImageUtils;

/**
 * Created by gyongyosit on 2015.11.17..
 */
public class ImageThumbnailLoader {

    Context context;
    private LruCache<String, Bitmap> mMemoryCache;
    Bitmap mPlaceHolderBitmap;

    /**
     * Load downscaled image in the background
     * @param mPlaceHolderBitmap temporary background of the imageview
     */
    public ImageThumbnailLoader(Context context, Bitmap mPlaceHolderBitmap){

        this.context = context;
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        this.mPlaceHolderBitmap = mPlaceHolderBitmap;
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * Load the image in the background
     * @param file imagefile
     * @param imageView the selected imageview
     */
    public void loadBitmapBackground(File file, ImageView imageView) {
        final Bitmap bitmap = getBitmapFromMemCache(file.getAbsolutePath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (cancelPotentialWork(file, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(file);
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private static boolean cancelPotentialWork(File data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final File bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.getAbsolutePath().equals(data.getAbsolutePath())) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private File data = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(File... params) {
            data = params[0];
            final String mimeType;
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String[] fileNameSplits = data.getName().split("\\.");
            mimeType = mimeTypeMap.getMimeTypeFromExtension(fileNameSplits[fileNameSplits.length-1]);
            if (mimeType != null && mimeType.matches("video(.+)")) {
                final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(data.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                addBitmapToMemoryCache(data.getAbsolutePath(), bitmap);
                return bitmap;
            } else {
                final Bitmap bitmap = ImageUtils.loadImageWithResize(data, 110, 80);
                addBitmapToMemoryCache(data.getAbsolutePath(), bitmap);
                return bitmap;
            }

        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
               return;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
