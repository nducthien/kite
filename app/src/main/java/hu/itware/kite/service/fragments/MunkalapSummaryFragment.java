package hu.itware.kite.service.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.R;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.activity.MunkalapActivity;
import hu.itware.kite.service.dao.KiteDAO;
import hu.itware.kite.service.enums.DialogType;
import hu.itware.kite.service.interfaces.IRefreshable;
import hu.itware.kite.service.interfaces.MunkalapFragmentInterface;
import hu.itware.kite.service.orm.KiteORM;
import hu.itware.kite.service.orm.enums.Operation;
import hu.itware.kite.service.orm.model.MetaData;
import hu.itware.kite.service.orm.model.Munkalap;
import hu.itware.kite.service.orm.model.MunkalapExport;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.orm.network.ImageUtils;
import hu.itware.kite.service.orm.utils.GSON;
import hu.itware.kite.service.services.LoginService;
import hu.itware.kite.service.settings.Settings;
import hu.itware.kite.service.signature.SignatureActivity;
import hu.itware.kite.service.utils.Export;
import hu.itware.kite.service.utils.MetaDataTypes;

/**
 * Created by gyongyosit on 2015.11.02..
 */
public class MunkalapSummaryFragment extends Fragment implements IRefreshable, IDialogResult {

	public static final String TAG = "MunkalapSummary";

    public DialogType dialogType = DialogType.MAIN_MENU_DIALOG;
    private ImageView signatureLeft;
    TextView tvCustomerName;
    private MunkalapFragmentInterface mListener;
    private Munkalap mMunkalap;
    WebView wvForm;
    private int mode = SignatureActivity.MODE_CUSTOMER_SIGNATURE;
    private static final String PREF_SZERVIZES_ALAIRAS = "szervizes_alairas";
    private int reason;

    public MunkalapSummaryFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        signatureLeft = (ImageView) view.findViewById(R.id.or_sum_iv_signature_left);
        signatureLeft.setVisibility(View.INVISIBLE);
        tvCustomerName = (TextView) view.findViewById(R.id.or_sum_tv_signbycustomer);
        wvForm = (WebView) view.findViewById(R.id.or_sum_wv_html);
        WebSettings settings = wvForm.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        view.findViewById(R.id.or_sum_bt_summary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signatureLeft.getVisibility() == View.VISIBLE) {
                    showAszfDialog();
                }
            }
        });
        view.findViewById(R.id.or_sum_sg_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = SignatureActivity.MODE_CUSTOMER_SIGNATURE;
                Intent intent = new Intent(getActivity(), SignatureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivityForResult(intent, MunkalapActivity.SIGNANTURE_REQUEST_CODE);
            }
        });

        view.findViewById(R.id.or_sum_bt_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = SignatureActivity.MODE_EMPLOYEE_SIGNATURE;
                ((BaseActivity) getActivity()).showQuestionDialog(
                        getString(R.string.dialog_report_title),
                        getString(R.string.dialog_report_message),
                        getString(R.string.dialog_report_did_not_signed),
                        getString(R.string.dialog_report_not_present),
                        new IDialogResult() {

                            @Override
                            public void onOkClicked(DialogFragment dialog) {
                                reason = SignatureActivity.REASON_NOT_PRESENT;
                                loadSignatureOrStartSignatureActivity();
                            }

                            @Override
                            public void onCancelClicked(DialogFragment dialog) {
                                reason = SignatureActivity.REASON_DID_NOT_SIGN;
                                loadSignatureOrStartSignatureActivity();
                            }

                            private void loadSignatureOrStartSignatureActivity() {
                                Uzletkoto uzletkoto = LoginService.getManager(KiteApplication.getContext());
                                Bitmap bitmap = ImageUtils.loadUploadableImageWithResize(getActivity(), getEmployeeSignatureFilename(), signatureLeft.getMaxWidth(), signatureLeft.getMaxHeight(), false);
                                if (bitmap != null) {
                                    String temp = getEmployeeTempSignatureFilename();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    ImageUtils.saveImage(getActivity(), stream.toByteArray(), temp);
                                    signatureLeft.setImageBitmap(bitmap);
                                    signatureLeft.setVisibility(View.VISIBLE);
                                    tvCustomerName.setText(uzletkoto.getNev());
                                    mMunkalap.alairas = reason == SignatureActivity.REASON_DID_NOT_SIGN ? getString(R.string.dialog_report_did_not_signed) : getString(R.string.dialog_report_not_present);
                                    mMunkalap.alairaskep = temp;
                                    mMunkalap.jegyzokonyv = "I";
                                    updateMunkalap();
                                } else {
                                    Intent intent = new Intent(getActivity(), SignatureActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra(SignatureActivity.SIGNATURE_MODE, SignatureActivity.MODE_EMPLOYEE_SIGNATURE);
                                    intent.putExtra(SignatureActivity.SIGNATURE_REASON, reason);
                                    getActivity().startActivityForResult(intent, MunkalapActivity.SIGNANTURE_REQUEST_CODE);
                                }
                            }
                        });
            }
        });

        disableWebViewSelection(wvForm);
        wvForm.setBackgroundColor(0x00000000);
        settings.setSupportZoom(false);
        settings.setSupportMultipleWindows(false);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(false);
        refresh();
        return view;
    }

    protected void disableWebViewSelection(WebView wv) {

        if (wv != null) {
            wv.setLongClickable(false);
            wv.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (MunkalapFragmentInterface) context;
            refresh();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MunkalapFragmentInterface");
        }
    }

    private String getEmployeeTempSignatureFilename() {
        // TODO: uzletkoto.szervizeskod a GEW azonosito?
        Uzletkoto uzletkoto = LoginService.getManager(KiteApplication.getContext());
        return "_sza_" + (mMunkalap != null ? mMunkalap.tempkod : "HIBA") + "_" + BaseActivity.getSdfPicture().format(new Date()) + "thumb_pdf_ico.png";
    }

    public static String getEmployeeSignatureFilename() {
        // TODO: uzletkoto.szervizeskod a GEW azonosito?
        Uzletkoto uzletkoto = LoginService.getManager(KiteApplication.getContext());
        return "szervizes_" + (uzletkoto != null ? uzletkoto.szervizeskod : "HIBA") + ".png";
    }

    public void setSignature(Intent data) {
        if (data.getByteArrayExtra(SignatureActivity.CANVAS1) != null && mMunkalap != null) {

			if (mMunkalap.alairaskep != null && ImageUtils.isUploadableImageExists(getActivity(), mMunkalap.alairaskep)) {
				ImageUtils.deleteUploadableImage(getActivity(), mMunkalap.alairaskep);
			}

            byte[] canvasByteArray = data.getByteArrayExtra(SignatureActivity.CANVAS1);
            Bitmap bitmap = BitmapFactory.decodeByteArray(canvasByteArray, 0, canvasByteArray.length);
            String temp = "";
            if (mode == SignatureActivity.MODE_CUSTOMER_SIGNATURE) {
                mMunkalap.jegyzokonyv = "N";
                temp = "_A_" + mMunkalap.getMunkalapKod() + "_" + BaseActivity.getSdfPicture().format(new Date()) + "thumb_pdf_ico.png";
            } else {
                mMunkalap.jegyzokonyv = "I";
                temp = getEmployeeTempSignatureFilename();
                ImageUtils.saveImage(getActivity(), canvasByteArray, getEmployeeSignatureFilename());
            }
            if (ImageUtils.saveImage(getActivity(), canvasByteArray, temp)) {
                mMunkalap.alairaskep = temp;
            }
            bitmap.recycle();
            if (signatureLeft != null){
                signatureLeft.setImageBitmap(ImageUtils.loadUploadableImageWithResize(getActivity(), mMunkalap.alairaskep, signatureLeft.getMaxWidth(), signatureLeft.getMaxHeight(), false));
                signatureLeft.setVisibility(View.VISIBLE);
            }
        } else {
            if (signatureLeft != null) {
                signatureLeft.setVisibility(View.INVISIBLE);
            }
        }

        if (data.getStringExtra(SignatureActivity.NAME) != null) {
            tvCustomerName.setText(data.getStringExtra(SignatureActivity.NAME));
            mMunkalap.alairas = tvCustomerName.getText().toString();
        }

        if (mode == SignatureActivity.MODE_EMPLOYEE_SIGNATURE) {
            mMunkalap.alairas = reason == SignatureActivity.REASON_DID_NOT_SIGN ? getString(R.string.dialog_report_did_not_signed) : getString(R.string.dialog_report_not_present);
        }
        updateMunkalap();
    }

    private void updateMunkalap() {
        KiteORM orm = new KiteORM(getActivity());
        orm.update(mMunkalap);
        ((MunkalapActivity)getActivity()).setMunkalap(mMunkalap);
    }

    private void showAszfDialog() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ((BaseActivity) getActivity()).showErrorToast("Nincs SD kártya csatlakoztatva.");
            return;
        }

        String filePath = "file://" + Environment.getExternalStoragePublicDirectory(Settings.LOCAL_IMAGES_DIR).getPath() + "/";
        MetaData[] metaData = KiteDAO.loadMetaData(getActivity(), MetaDataTypes.SZERZODES_ASZF);

        if(metaData != null && metaData.length > 0){
           // TODO filepath
        } else {
            filePath += getString(R.string.dummy_aszf_name);
        }
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        AcceptAszfDialog aszfDialog = new AcceptAszfDialog();
        aszfDialog.setArguments(this, filePath, mMunkalap);

        Bundle params = new Bundle();
        params.putString("title", getString(R.string.dialog_accept_aszf_title));
        params.putString("message", getString(R.string.dialog_accept_aszf_message));

        aszfDialog.setArguments(params);
//        aszfDialog.show(fm, "fragment_dialog_aszf");
        transaction.add(aszfDialog,"fragment_dialog_aszf").commitAllowingStateLoss();
        dialogType = DialogType.ACCEPT_ASZF_DIALOG;
    }

    @Override
    public void refresh() {
        if (mListener != null) {
            mMunkalap = mListener.getMunkalap();
            if (mMunkalap != null && isAdded()) {
                mMunkalap = ((MunkalapActivity) getActivity()).getMunkalap();
                Log.i("mMunkalap: ", "" + mMunkalap);
                if (mMunkalap != null && wvForm != null) {
                    wvForm.loadData(createMunkalapForm(), "text/html; charset=utf-8", "utf-8");
                }
            }
        }
    }

    private String createMunkalapForm() {
        String munkalapForm = mMunkalap.createMunkalapSummary("munkalap.html");

        if(mMunkalap.alairaskep != null){
            //signatureLeft.setImageBitmap(ImageUtils.loadUploadableImage(mMunkalap.alairaskep));
            signatureLeft.setImageBitmap(ImageUtils.loadUploadableImageWithResize(getActivity(), mMunkalap.alairaskep, signatureLeft.getMaxWidth(), signatureLeft.getMaxHeight(), false));
            signatureLeft.setVisibility(View.VISIBLE);
        }
        if(mMunkalap.alairas != null){
            tvCustomerName.setText(mMunkalap.alairas);
        }

        return munkalapForm;
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {
        switch (dialogType) {
            case ACCEPT_ASZF_DIALOG:
                aszfAccepted();
                break;
            default:
                break;
        }
    }

    private void aszfAccepted() {
        mMunkalap.allapotkod = "2";

        //--- Save munkalap Export
        MunkalapExport export = Export.createMunkalapFullExport(getActivity(), mMunkalap, mMunkalap.getGep(), Operation.NEW);
        Log.i(TAG, "Creating EXPORT for munkalap=" + GSON.toJson(export));
        KiteORM kiteORM = new KiteORM(getActivity());
        kiteORM.insert(export);

        if (mMunkalap.javitasdatum != null) {
            mMunkalap.closeMunkalap();
        } else {
            kiteORM.update(mMunkalap);
        }

        FragmentManager fm = getActivity().getSupportFragmentManager();
        ErrorDialog errorDialog = new ErrorDialog();
        errorDialog.setListener(new IDialogResult() {
            @Override
            public void onOkClicked(DialogFragment dialog) {
                mMunkalap.allapotkod = "3";
                KiteORM kiteORM = new KiteORM(getActivity());
                kiteORM.update(mMunkalap);
                if (mListener.getCurrentPage().equals(MunkalapSummaryFragment.class)) {
                    mListener.nextPage();
                }
            }

            @Override
            public void onCancelClicked(DialogFragment dialog) {

				getActivity().finish();
            }
        });

        Bundle params = new Bundle();
        params.putString("title", "Megerősítés");
        params.putString("message", "Akarod folytatni?");
        params.putInt("type", ErrorDialog.QUESTION);

        errorDialog.setArguments(params);
        errorDialog.show(fm, "fragment_dialog_error");
    }

    @Override
    public void onCancelClicked(DialogFragment dialog) {
        switch (dialogType) {
            case ACCEPT_ASZF_DIALOG:
                break;
            default:
                break;
        }
    }
}
