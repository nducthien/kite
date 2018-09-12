package hu.itware.kite.service.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {

	protected static final float MINP = 0.25f;
	protected static final float MAXP = 0.75f;

	protected MaskFilter mEmboss;

	protected MaskFilter mBlur;

	private Paint mPaint;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	private Paint mBitmapPaint;
	private boolean mRightEventSend = false;
	private int mRightBorder;

	private int width = 100;
	private int height = 100;
	
	private volatile long points = 0;

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private OnSignatureNeedMoreSpaceListener mRightListener;

	public SignatureView(Context context) {
		super(context);
		init();
	}

	public SignatureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SignatureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPath = new Path();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFF000000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(10);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w;
		height = h;
		super.onSizeChanged(w, h, oldw, oldh);
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mRightBorder = w / 4 * 3;
		points = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xFFFFFFFF);

		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

		canvas.drawPath(mPath, mPaint);
	}

	private void touch_start(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
			points++;
		}
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		// commit the path to our offscreen
		mCanvas.drawPath(mPath, mPaint);
		// kill this so we don't double draw
		mPath.reset();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();

                if (!mRightEventSend && x > mRightBorder) {
                    mRightEventSend = true;
                    if (mRightListener != null) {
                        mRightListener.onSignatureRightOfScreen();
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                break;
		}
		return true;
	}

	public void setOnRightTouchListener(
			OnSignatureNeedMoreSpaceListener listener) {
		this.mRightListener = listener;
	}

	public void clearPath() {
		Log.d("CANVAS", "Clearing path...");
		if (mPath != null && mCanvas != null) {
			mCanvas.drawColor(0xFFFFFFFF);
			mPath.reset();
			invalidate();
			mRightEventSend = false;
			points = 0;
		}
	}
	
	public long getPointCount() {
		return points;
	}

	public Bitmap getImage() {
		if (mBitmap == null) {
			return null;
		}
		if (mBitmap.isRecycled()) {
			mBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
		}
		return mBitmap;
	}
}
