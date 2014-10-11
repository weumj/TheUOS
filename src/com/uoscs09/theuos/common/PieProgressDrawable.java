package com.uoscs09.theuos.common;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

/**
 * source from {@linkplain https://gist.github.com/dcow/9493477}
 */
public class PieProgressDrawable extends Drawable {

	Paint mPaint;
	RectF mBoundsF;
	RectF mInnerBoundsF;
	final float START_ANGLE = 0.f;
	float mDrawTo;

	CharSequence mText;
	Paint mTextPaint;

	public PieProgressDrawable() {
		super();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint = new Paint(mPaint);
		mTextPaint.setColor(Color.BLACK);
	}

	public void setText(CharSequence text) {
		mText = text;
	}

	public CharSequence getText() {
		return mText;
	}

	public void setTextSize(float size) {
		mTextPaint.setTextSize(size);
	}

	/**
	 * Set the border width.
	 * 
	 * @param widthDp
	 *            in dip for the pie border
	 */
	public void setBorderWidth(float widthDp, DisplayMetrics dm) {
		float borderWidth = widthDp * dm.density;
		mPaint.setStrokeWidth(borderWidth);
	}

	/**
	 * @param color
	 *            you want the pie to be drawn in
	 */
	public void setColor(int color) {
		mPaint.setColor(color);
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	@Override
	public void draw(Canvas canvas) {
		// Rotate the canvas around the center of the pie by 90 degrees
		// counter clockwise so the pie stars at 12 o'clock.
		int level = getLevel();
		canvas.rotate(-90f, getBounds().centerX(), getBounds().centerY());
		if (level != 0) {
			mPaint.setStyle(Paint.Style.STROKE);
			canvas.drawArc(mBoundsF, START_ANGLE, mDrawTo, true, mPaint);
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawArc(mInnerBoundsF, START_ANGLE, mDrawTo, true, mPaint);
		}
		if (level != 100) {
			int color = mPaint.getColor();
			mPaint.setColor(Color.parseColor("#6cafdb"));
			mPaint.setStyle(Paint.Style.STROKE);
			canvas.drawArc(mBoundsF, mDrawTo, 360f - mDrawTo, true, mPaint);
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawArc(mInnerBoundsF, mDrawTo, 360f - mDrawTo, true, mPaint);
			mPaint.setColor(color);
		}

		// Draw inner oval and text on top of the pie (or add any other
		// decorations such as a stroke) here..
		// Don't forget to rotate the canvas back if you plan to add text!
		// ...
		canvas.rotate(90f, getBounds().centerX(), getBounds().centerY());

		Rect bounds = new Rect();
		mTextPaint.getTextBounds(mText.toString(), 0, mText.length(), bounds);
		canvas.drawText(mText, 0, mText.length(), getBounds().centerX()
				- bounds.centerX(), getBounds().centerY() - bounds.centerY(),
				mTextPaint);
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mBoundsF = mInnerBoundsF = new RectF(bounds);
		final int halfBorder = (int) (mPaint.getStrokeWidth() / 2f + 0.5f);
		mInnerBoundsF.inset(halfBorder, halfBorder);
	}

	@Override
	protected boolean onLevelChange(int level) {
		final float drawTo = START_ANGLE + ((float) 360 * level) / 100f;
		boolean update = drawTo != mDrawTo;
		mDrawTo = drawTo;
		return update;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
		mTextPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return mPaint.getAlpha();
	}
}
