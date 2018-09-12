package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.adapters.FotoAdapter;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.interfaces.RefreshMunkalapDialogInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.Photo;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.utils.FileUtils;
import hu.itware.kite.service.utils.GPSTracker2;
import hu.itware.kite.service.utils.RealPathUtil;

/**
 * Created by gyongyosit on 2015.11.06..
 */
public class FotoCsatolasDialog extends DialogFragment implements RefreshMunkalapDialogInterface {

    public static final int MODIFIABLE_DEFAULT_INTERVAL = 24;

    public static final String TAG = "FotoCsatolas";
    private IDialogResult listener;

    private FotoAdapter fotoAdapter;
    private Munkalap munkalap;
    private ListView listView;
    private int pictureSequence;
    private Location location;

    public FotoCsatolasDialog() {
        this.setCancelable(false);
    }

    public void setListener(IDialogResult listener) {
        this.listener = listener;
    }

    @Override
    public void refresh(Munkalap munkalap) {
        this.munkalap = munkalap;
        setAdapter(munkalap);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_photo, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(getArguments().getString("title"));

        TextView text = (TextView) view.findViewById(R.id.dialog_text);
        text.setText(getArguments().getString("message"));

        Button buttonOk = (Button) view.findViewById(R.id.dialog_button_right);
        buttonOk.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
        buttonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        munkalap = (Munkalap) getArguments().getSerializable(AlkatreszekKezeleseDialog.EXTRA_MUNKALAP);

        listView = (ListView) view.findViewById(R.id.fenykepek_listview);

        Button buttonPhoto = (Button) view.findViewById(R.id.add_photo);
        Button buttonVideo = (Button) view.findViewById(R.id.add_video);
        final int mode = ((MunkalapFragmentInterface) getActivity()).getMode();
        if (mode != MunkalapActivity.MODE_VIEW && mode != MunkalapActivity.MODE_OWN || (mode == MunkalapActivity.MODE_OWN && munkalap.isEditableFromOwnList())) {
            buttonPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String kep = (munkalap.tempkod != null ? munkalap.tempkod : munkalap.getMunkalapKod()) + String.format("%02d", ++pictureSequence) + ".jpg";
                    Photo photo = Photo.createWithName(getActivity(), kep);
                    ((MunkalapFragmentInterface) getActivity()).setCurrentPhoto(photo);
                    createChooserForPhoto(photo);
                }
            });
            buttonVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createChooserForVideo();
                }
            });
        } else {
            buttonPhoto.setEnabled(false);
            buttonVideo.setEnabled(false);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter(munkalap);
    }

    public void setAdapter(Munkalap munkalap) {
        if (listView == null || munkalap == null || munkalap.fenykepek == null) {
            return;
        }
        Log.i(TAG, "mMunkalap fenykepek: " + munkalap.fenykepek);
        if (fotoAdapter == null) {
            fotoAdapter = new FotoAdapter(getActivity(), R.layout.list_item_photo, munkalap, this);
            listView.setAdapter(fotoAdapter);
        } else {
            this.munkalap.fenykepek = munkalap.fenykepek;
            fotoAdapter.notifyDataSetChanged();
        }
        pictureSequence = 0;
        for (String name : munkalap.fenykepek) {
            try {
                int seq = Integer.parseInt(name.substring(name.indexOf(".") - 2, name.indexOf(".")));
                if (seq > pictureSequence) {
                    pictureSequence = seq;
                }
            } catch (NumberFormatException e) {
                // continue
            }
        }
    }

    private void createChooserForPhoto(Photo photo) {
//        GPSTracker2 tracker2 = new GPSTracker2(getActivity().getApplicationContext());
//        location = tracker2.getLocation();
//        if (location != null)
//            tracker2.stopSelf();
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photo.getUri());
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.dialog_add_photo));

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, MunkalapActivity.CAMERA_REQUEST_CODE);
    }

    private void createChooserForVideo() {


        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("video/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.dialog_add_video));

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, MunkalapActivity.VIDEO_REQUEST_CODE);
    }

    private void addVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, MunkalapActivity.VIDEO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode);
        if (requestCode == MunkalapActivity.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // SAVE PHOTO
            if (munkalap.fenykepek == null) {
                munkalap.fenykepek = new ArrayList<String>();
            }
            final boolean isCamera;
            if (data == null || data.getData() == null) {
                isCamera = true;
            } else {
                final String action = data.getAction();
                if (action == null) {
                    isCamera = false;
                } else {
                    isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                }
            }
            if (isCamera) {
                Uri uri = ((MunkalapActivity) getActivity()).getCurrentPhoto().getUri();
                String filePath = uri.getPath();
                String kep = munkalap.getMunkalapKod() + String.format("%02d", pictureSequence) + ".jpg";
                try {
                    Uri newUri = FileUtils.copyFile2(new File(filePath), new File(ImageUtils.getPath(getActivity(), false), kep));
                    drawTextAndLocation(newUri);
                    munkalap.fenykepek.add(kep);
                    //delete file temp when click take photo
                    //Uri uri_temp = ((MunkalapActivity) getActivity()).getCurrentPhoto().getUri();
                    //File file_temp = new File(uri_temp.getPath());
                    //file_temp.delete();
                } catch (Exception e) {
                    Log.e(TAG, "Error copying image", e);
                }
//                drawTextAndLocation(uri);
//                munkalap.fenykepek.add(((MunkalapActivity) getActivity()).getCurrentPhoto().getImageFileName());
            } else {
                Uri uri = data.getData();
                String filePath = "";
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                    filePath = RealPathUtil.getPath(getActivity(), uri);

                    //filePath = RealPathUtil.getRealPathFromURI(getContext(), uri);
                } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    filePath = RealPathUtil.getRealPathFromURI_API11to18(getActivity(), uri);
                } else {
                    filePath = RealPathUtil.getRealPathFromURI_BelowAPI11(getActivity(), uri);
                }
                String kep = munkalap.getMunkalapKod() + String.format("%02d", pictureSequence) + ".jpg";
                try {
                    Uri newUri = FileUtils.copyFile2(new File(filePath), new File(ImageUtils.getPath(getActivity(), false), kep));
                    drawTextAndLocation(newUri);
                    munkalap.fenykepek.add(kep);
                    //delete file temp when click take photo
                    //Uri uri_temp = ((MunkalapActivity) getActivity()).getCurrentPhoto().getUri();
                    //File file_temp = new File(uri_temp.getPath());
                    //file_temp.delete();
                } catch (Exception e) {
                    Log.e(TAG, "Error copying image", e);
                }
            }
        } else if (requestCode == MunkalapActivity.VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (munkalap.fenykepek == null) {
                munkalap.fenykepek = new ArrayList<String>();
            }
            String videoPath = getRealPathFromURI(data.getData());
            String video = munkalap.getMunkalapKod() + String.format("%02d", ++pictureSequence) + ".mp4";

//            File f = new File(videoPath);
//            File path = Environment.getExternalStoragePublicDirectory(Settings.UPLOAD_IMAGES_DIR);
//            if (!path.exists()) {
//                path.mkdirs();
//            }
            try {
                FileUtils.copyFile(new File(videoPath), new File(ImageUtils.getPath(getActivity(), false), video));
                munkalap.fenykepek.add(video);
                Log.i(TAG, "Add new video: " + video);
            } catch (Exception e) {
                Log.e(TAG, "Error copying image", e);
            }
//            File newFile = new File(path, video);
//            f.renameTo(newFile);
//            Log.i(TAG, "Add new video: " + video);
//            if (newFile.exists()) {
//                munkalap.fenykepek.add(video);
//            } else {
//                ((BaseActivity)getActivity()).showErrorDialog(getString(R.string.dialog_add_video_error_title), getString(R.string.dialog_add_video_error_message));
//            }
        }
        KiteORM orm = new KiteORM(getActivity());
        orm.update(munkalap);
        ((MunkalapActivity) getActivity()).setMunkalap(munkalap);
        setAdapter(munkalap);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void storeImage(Bitmap bitmap, String filePath) {

        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void drawTextAndLocation(Uri uri) {
        String filePath = getRealPathFromURI(uri);
        Log.d("info", ReadExif(filePath));

        //get date time
        DateFormat dateFormatter1 = new SimpleDateFormat("dd-MM-yyyy ");
        dateFormatter1.setLenient(false);
        java.util.Date today = new java.util.Date();
        String caption = dateFormatter1.format(today);
        Bitmap b = bitmapDraw(uri, caption);
        if (b != null) {
            storeImage(b, filePath);
        }

    }

    String ReadExif(String file) {
        String exif = "Exif: " + file;
        try {
            ExifInterface exifInterface = new ExifInterface(file);

            exif += "\nIMAGE_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            exif += "\nIMAGE_WIDTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            exif += "\n DATETIME: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            exif += "\n TAG_MAKE: " + exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            exif += "\n TAG_MODEL: " + exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            exif += "\n TAG_ORIENTATION: " + exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            exif += "\n TAG_WHITE_BALANCE: " + exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
            exif += "\n TAG_FOCAL_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            exif += "\n TAG_FLASH: " + exifInterface.getAttribute(ExifInterface.TAG_FLASH);
            exif += "\nGPS related:";
            exif += "\n TAG_GPS_DATESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
            exif += "\n TAG_GPS_TIMESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            exif += "\n TAG_GPS_LATITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            exif += "\n TAG_GPS_LATITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            exif += "\n TAG_GPS_LONGITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            exif += "\n TAG_GPS_LONGITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            exif += "\n TAG_GPS_PROCESSING_METHOD: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

//            Toast.makeText(getActivity(),
//                    exif,
//                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return exif;
    }

    private Bitmap bitmapDraw(Uri uri, String date_time) {
        Bitmap bitmap = null;
        Bitmap newBitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(getActivity().getApplicationContext().getContentResolver().openInputStream(uri));
            Bitmap.Config config = bitmap.getConfig();
            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }

            float h = bitmap.getHeight();
            float w = bitmap.getWidth();
            float textSize = h / 30;
            newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);
            Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint1.setColor(Color.RED);
            paint1.setTextSize(textSize);
            paint1.setStyle(Paint.Style.FILL);

            Rect rect = new Rect();
            paint1.getTextBounds("" + date_time, 0, date_time.length(), rect);
            canvas.drawText("" + date_time, 0, h - 5, paint1);

            canvas.save();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return newBitmap;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Video.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null,
                null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                res = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        if (res == null) {
            if (DocumentsContract.isDocumentUri(getActivity(), contentUri) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String wholeID = DocumentsContract.getDocumentId(contentUri);

                String id = wholeID.split(":")[1];

                String[] column = {MediaStore.Video.Media.DATA};

                String sel = MediaStore.Images.Media._ID + "=?";

                cursor = getActivity().getContentResolver().
                        query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{id}, null);

                int columnIndex = cursor.getColumnIndex(column[0]);

                if (cursor.moveToFirst()) {
                    res = cursor.getString(columnIndex);
                }

                cursor.close();
            } else {
                res = contentUri.getPath();
            }
        }
        return res;
    }
}
