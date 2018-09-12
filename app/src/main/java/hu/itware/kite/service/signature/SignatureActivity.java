package hu.itware.kite.service.signature;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import java.io.ByteArrayOutputStream;

import hu.itware.kite.service.KiteApplication;
import hu.itware.kite.service.activity.BaseActivity;
import hu.itware.kite.service.R;
import hu.itware.kite.service.fragments.IDialogResult;
import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.services.LoginService;

public class SignatureActivity extends BaseActivity implements IDialogResult {

	public static final String CANVAS1 = "canvas1";
	public static final String CANVAS2 = "canvas2";
	public static final String NAME = "name";
	public static final int MIN_SIGN_POINTS = 30;
	public static final String SIGNATURE_MODE = "mode";
	public static final int MODE_CUSTOMER_SIGNATURE = 0;
	public static final int MODE_EMPLOYEE_SIGNATURE = 1;
	public static final String SIGNATURE_REASON = "reason";
	public static final int REASON_NOT_APPLICABLE = 0;
	public static final int REASON_DID_NOT_SIGN = 1;
	public static final int REASON_NOT_PRESENT = 2;

	private static final String TAG = "SignatureActivity";

	private SignatureView canvas1;
	private SignatureView canvas2;
	private ViewSwitcher switcher;
	private int page = 1;
	private LinearLayout buttonNext;
	private LinearLayout buttonPrev;
	private LinearLayout buttonDone;
	private LinearLayout buttonClear;
	private boolean showNextButton = false;
	Handler mHandler = new Handler();
	int dialogType = 0;
	public static final int SIGN_DIALOG = 1;
	EditText etName;

	private int mode = MODE_CUSTOMER_SIGNATURE;
	private int reason = REASON_NOT_APPLICABLE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mode = getIntent().getIntExtra(SIGNATURE_MODE, MODE_CUSTOMER_SIGNATURE);
		reason = getIntent().getIntExtra(SIGNATURE_REASON, REASON_NOT_APPLICABLE);
		setContentView(R.layout.page_signature);

		setupUIElements();

		setListeners();
		updatePaginationStates();
	}

	@Override
	protected void setupUIElements() {
		switcher = (ViewSwitcher) findViewById(R.id.signature_switcher);
		canvas1 = (SignatureView) findViewById(R.id.signature_canvas1);
		canvas2 = (SignatureView) findViewById(R.id.signature_canvas2);
		buttonNext = (LinearLayout) findViewById(R.id.signature_next);
		buttonPrev = (LinearLayout) findViewById(R.id.signature_prev);
		buttonDone = (LinearLayout) findViewById(R.id.signature_finish);
		buttonClear = (LinearLayout) findViewById(R.id.signature_clear);
		buttonClear.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));
		buttonDone.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));
		etName = (EditText)findViewById(R.id.name);
		if (mode == MODE_EMPLOYEE_SIGNATURE) {
			Uzletkoto uzletkoto = LoginService.getManager(KiteApplication.getContext());
			etName.setText(uzletkoto.getNev());
		}
	}

	@Override
	protected void setListeners() {

		canvas1.setOnRightTouchListener(new OnSignatureNeedMoreSpaceListener() {

			@Override
			public void onSignatureRightOfScreen() {
				showNextButton = true;
				updatePaginationStates();
			}
		});

		buttonNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showNextPage();
			}
		});

		buttonPrev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showPrevPage();
			}
		});

		buttonDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						saveImages();
					}
				});

			}
		});

		buttonClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				etName.setText("");
				canvas1.clearPath();
				canvas2.clearPath();
				showNextButton = false;
				if (page == 2) {
					showPrevPage();
				} else {
					updatePaginationStates();
				}
			}
		});
	}

	private void showNextPage() {
		switcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
		switcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
		switcher.showNext();
		page = page == 1 ? 2 : 1;
		updatePaginationStates();
	}

	private void showPrevPage() {
		switcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
		switcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
		switcher.showPrevious();
		page = page == 2 ? 1 : 2;
		updatePaginationStates();
	}

	private void updatePaginationStates() {
		if (page == 1) {
			hideButton(buttonPrev);
			if (showNextButton) {
				showButton(buttonNext);
			} else {
				hideButton(buttonNext);
			}
		} else {
			showButton(buttonPrev);
			hideButton(buttonNext);
		}
	}

	private void hideButton(View button) {
		if (button.getVisibility() == View.VISIBLE) {
			hide(button, AnimationUtils.loadAnimation(this, R.anim.fadeout));
		}
	}

	private void showButton(View button) {
		if (button.getVisibility() == View.GONE) {
			show(button, AnimationUtils.loadAnimation(this, R.anim.fadein));
		}
	}

	private void saveImages() {
		Log.e(TAG, "saveImages().point_count=" + canvas1.getPointCount());
		if (canvas1.getPointCount() < MIN_SIGN_POINTS) {
			dialogType = SIGN_DIALOG;
			showErrorDialog(getString(R.string.error_short_signature_title), getString(R.string.error_short_signature_message));
			return;
		}

		if(etName.getText().toString().length() == 0){
			dialogType = SIGN_DIALOG;
			showErrorDialog(getString(R.string.error_wromg_name_title), getString(R.string.error_wromg_name_message));
			return;
		} else if(etName.getText().toString().length() < 5){
			dialogType = SIGN_DIALOG;
			showErrorDialog(getString(R.string.error_wromg_name_title), getString(R.string.error_short_name_message));
			return;
		}
		
		
		try {
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_OK, resultIntent);

			if (canvas1.getImage() != null) {
				Log.v(TAG, "canvas 1 van image");
				resultIntent.putExtra(CANVAS1, getByteArrayFromBitmap(canvas1.getImage()));
				canvas1.getImage().recycle();
			}
			if (canvas2.getImage() != null) {
				Log.v(TAG, "canvas 2 van image");
				resultIntent.putExtra(CANVAS2, getByteArrayFromBitmap(canvas2.getImage()));
				canvas2.getImage().recycle();
			}

			resultIntent.putExtra(NAME, etName.getText().toString());
			resultIntent.putExtra(SIGNATURE_MODE, mode);
			resultIntent.putExtra(SIGNATURE_REASON, reason);
		} catch (Exception e) {
			Log.e(TAG, "Error saving image", e);
		}

		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case android.R.id.home:
				this.onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void hide(final View view, Animation animation) {
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// hide the view when the animation ends
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		// start the animation
		view.startAnimation(animation);
	}

	public void show(final View view, Animation animation) {
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// hide the view when the animation ends
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		// start the animation
		view.startAnimation(animation);
	}

	private byte[] getByteArrayFromBitmap(Bitmap bitmap) {

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteStream);
		return byteStream.toByteArray();

	}
	
	@Override
	public void onOkClicked(DialogFragment dialog) {
		switch (dialogType) {
			case SIGN_DIALOG:
				dialogType = 0;
				break;

			default :
				super.onOkClicked(dialog);
				break;
		}
	}

}
