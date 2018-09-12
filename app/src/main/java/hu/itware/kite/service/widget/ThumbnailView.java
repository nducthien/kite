package hu.itware.kite.service.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import hu.itware.kite.service.R;


/**
 * Created by macbookpro_itware on 2015.09.08.
 */
public class ThumbnailView extends LinearLayout{
    private TextView tvFileName;
    private ImageView ivFileIcon;
    public ImageView background;

    public ThumbnailView(final Context context) {
        super(context);
        View view = inflate(context, R.layout.catalog_thumb_layout, null);
        addView(view);

        tvFileName = (TextView)view.findViewById(R.id.catalog_thumb_file_name);
        ivFileIcon = (ImageView)view.findViewById(R.id.catalog_thumb_icon);
        background =(ImageView)view.findViewById(R.id.catalog_thumb);
    }

    public ThumbnailView(final Context context, int iconResId, String fileName) {
        super(context);
        View view = inflate(context, R.layout.catalog_thumb_layout, null);
        addView(view);

        tvFileName = (TextView)view.findViewById(R.id.catalog_thumb_file_name);
        ivFileIcon = (ImageView)view.findViewById(R.id.catalog_thumb_icon);
        background =(ImageView)view.findViewById(R.id.catalog_thumb);

        tvFileName.setText(fileName);
        ivFileIcon.setImageResource(iconResId);
    }

    public void setIcon(int id) {
        ivFileIcon.setImageResource(id);
    }

    public void setFileName(String fileName) {
        tvFileName.setText(fileName);
    }
}
