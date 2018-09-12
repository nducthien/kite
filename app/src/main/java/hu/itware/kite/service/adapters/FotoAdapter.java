package hu.itware.kite.service.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.settings.Settings;

/**
 * Created by gyongyosit on 2015.11.06..
 */
public class FotoAdapter extends ArrayAdapter<String> {

    public static final String TAG = "FotoAdapter";
    Munkalap munkalap;
    KiteORM orm;
    List<String> objects;
    List<String> originalObjects;
    Context context;
    RefreshMunkalapDialogInterface listener;
    Bitmap mPlaceHolderBitmap;
    private LruCache<String, Bitmap> mMemoryCache;

    static class ViewHolder {
        TextView tvNev;
        ImageButton removebtn;
        ImageView typeicon, thumbIcon;
    }
    public FotoAdapter(Context context, int resource, Munkalap munkalap, RefreshMunkalapDialogInterface listener) {
        super(context, resource, munkalap.fenykepek);
        this.munkalap = munkalap;
        this.orm = new KiteORM(context);
        this.objects = munkalap.fenykepek;
        this.originalObjects = new ArrayList<String>();
        this.originalObjects.addAll(munkalap.fenykepek);
        this.context = context;
        this.listener = listener;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_photo_icon);
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

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_photo, parent, false);

            holder = new ViewHolder();
            holder.removebtn = (ImageButton)convertView.findViewById(R.id.fenykepek_list_btn_remove);
            holder.tvNev = (TextView)convertView.findViewById(R.id.tv_nev_item);
            holder.typeicon = (ImageView)convertView.findViewById(R.id.type_icon);
            holder.thumbIcon = (ImageView)convertView.findViewById(R.id.thumb_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final String item = getItem(position);
        if(item != null) {
            if(item.toLowerCase().endsWith("jpg") || item.toLowerCase().endsWith("png")){
                holder.typeicon.setBackgroundResource(R.mipmap.thumb_photo_icon);
//                loadBitmapBackground(item, holder.thumbIcon);
                final Bitmap bitmap = ImageUtils.loadImage(getContext(), item);
                //final Bitmap bitmap = getBitmapFromMemCache(item);

                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
                holder.thumbIcon.setImageBitmap(thumbnail);
                holder.tvNev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startIntent("image", item);
                    }
                });
            } else {
                holder.typeicon.setBackgroundResource(R.mipmap.thumb_video_icon);
                File videoFile = new File(ImageUtils.getPath(getContext()), item);
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                holder.thumbIcon.setImageBitmap(thumb);
                holder.tvNev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startIntent("video", item);
                    }
                });
            }
            holder.tvNev.setText(item);
            int mode = ((MunkalapFragmentInterface)getContext()).getMode();
            if (mode != MunkalapActivity.MODE_VIEW && mode != MunkalapActivity.MODE_OWN || (mode == MunkalapActivity.MODE_OWN && !originalObjects.contains(item))) {
                holder.removebtn.setEnabled(true);
                holder.removebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String imageName = objects.get(position);
                            final File imageFile = new File(ImageUtils.getPath(getContext()), imageName);
                            if (imageFile != null && imageFile.exists()) {
                                imageFile.delete();
                            }
                            objects.remove(position);
                            FotoAdapter.this.notifyDataSetChanged();
                            orm.update(munkalap);
                            if (listener != null) {
                                listener.refresh(munkalap);
                            }
                        } catch (Exception e) {
                            //--- Nice handling of ArrayIndexOfBoundException :)
                            Log.w(TAG, "Items still loading, please wait...");
                        }
                    }
                });
            } else {
                holder.removebtn.setEnabled(false);
            }
        }

        return convertView;
    }

    private void startIntent(String type, String item) {
        File path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_DIR);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path, item);
        Log.i(TAG, "open " + type + ": " + file.getAbsolutePath());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), type + "/*");
        context.startActivity(intent);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null && getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmapBackground(String name, ImageView imageView) {
        final Bitmap bitmap = getBitmapFromMemCache(name);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (cancelPotentialWork(name, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(name);
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

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(data)) {
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

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = "";

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            final Bitmap bitmap = ImageUtils.loadUploadableImageWithResize(getContext(), data, 160, 90, false);
            addBitmapToMemoryCache(data, bitmap);
            return bitmap;
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
