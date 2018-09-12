package hu.itware.kite.service.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import hu.itware.kite.service.R;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.network.ImageUtils;

/**
 * Created by szeibert on 2017.06.15..
 */

public class MunkalapSummaryDialog extends DialogFragment {

    private static final String TAG = "MunkalapSummaryDialog";

    private WebView wvForm;
    private Munkalap mMunkalap;
    private ImageView signatureLeft;
    private TextView tvCustomerName;

    public MunkalapSummaryDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_munkalap_summary, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);

        Button buttonOk = (Button) view.findViewById(R.id.dialog_button_right);
        buttonOk.setText(getArguments().getString("rightbutton", getString(R.string.dialog_ok)));
        buttonOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setupClickOutFromEditText(view.findViewById(R.id.parent));
        wvForm = (WebView) view.findViewById(R.id.or_sum_wv_html);
        WebSettings settings = wvForm.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        wvForm.setLongClickable(false);
        wvForm.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        wvForm.setBackgroundColor(0x00000000);
        settings.setSupportZoom(false);
        settings.setSupportMultipleWindows(false);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(false);
        signatureLeft = (ImageView) view.findViewById(R.id.or_sum_iv_signature_left);
        tvCustomerName = (TextView) view.findViewById(R.id.or_sum_tv_signbycustomer);
        wvForm.loadData(createMunkalapForm(), "text/html; charset=utf-8", "utf-8");

        return view;
    }

    // hide soft input keyboard
    protected void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("MunkalapSummaryDialog", "Exception", e);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupClickOutFromEditText(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupClickOutFromEditText(innerView);
            }
        }
    }

    private String createMunkalapForm() {
        if (mMunkalap == null) {
            return "";
        }

        String munkalapForm = mMunkalap.createMunkalapSummary("munkalap_bovitett.html");

        if(mMunkalap.alairaskep != null){
            signatureLeft.setImageBitmap(ImageUtils.loadUploadableImageWithResize(getActivity(), mMunkalap.alairaskep, signatureLeft.getMaxWidth(), signatureLeft.getMaxHeight(), false));
            signatureLeft.setVisibility(View.VISIBLE);
        }
        if(mMunkalap.alairas != null){
            tvCustomerName.setText(mMunkalap.alairas);
        }

        return munkalapForm;
    }

    public void setMunkalap(Munkalap munkalap) {
        mMunkalap = munkalap;
    }
}
