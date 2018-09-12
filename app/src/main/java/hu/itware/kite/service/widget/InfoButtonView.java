package hu.itware.kite.service.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.fragments.ErrorDialog;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.model.MetaData;

/**
 * Shows a short tooltip text on click. Text is fetched from the MetaData table using informationKey attribute as key.
 *
 * Created by szeibert on 2015.09.11..
 */
public class InfoButtonView extends ImageView {

    private String infoKey;

    public InfoButtonView(Context context) {
        super(context);
    }

    public InfoButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public InfoButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.InfoButtonView);
        infoKey = typedArray.getString(R.styleable.InfoButtonView_informationKey);
        if (infoKey == null || "".equals(infoKey)) {
            infoKey = "not_specified";
        }
        typedArray.recycle();
        setImageResource(R.drawable.ic_action_about);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                KiteORM orm = new KiteORM(getContext());
                MetaData metaData = orm.loadSingle(MetaData.class, "type = ? AND value = ?", new String[]{"info", infoKey});
                if (metaData == null) {
                    metaData = new MetaData();
                    metaData.text = infoKey + " not found.";
                }
                FragmentManager fm = ((BaseActivity)getContext()).getSupportFragmentManager();
                ErrorDialog errorDialog = new ErrorDialog();

                Bundle params = new Bundle();
                params.putString("title", "");
                params.putString("message", metaData.text);
                params.putInt("type", ErrorDialog.INFO);

                errorDialog.setArguments(params);
                errorDialog.show(fm, "fragment_dialog_error");
                Toast.makeText(getContext(), metaData.text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
