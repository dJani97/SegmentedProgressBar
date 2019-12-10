package com.rachitgoyal.segmented;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.rachitgoyal.segmentedprogressbar.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rachitgoyal on 8/30/16.
 * <p/>
 * A ProgressBar that can be segmented and incremented by segments.
 */
public class SegmentedProgressBar extends View {

	private static final String TAG = "SegmentedProgressBar";
	RectF bgRect;
	private Paint progressBarBackgroundPaint = new Paint();
	private Paint progressBarPaint = new Paint();
	private Paint dividerPaint = new Paint();
	private int progressBarWidth;
	private float percentCompleted;
	private float dividerWidth = 1;
	private boolean isDividerEnabled = true;
	private int divisions = 1;
	private Map<Paint, List<Integer>> enabledDivisions = new HashMap<>();
	private List<Float> dividerPositions;
	private float cornerRadius = 2f;

	public SegmentedProgressBar(Context context) {
		super(context);
		init(null);
	}

	private void init(AttributeSet attrs) {
		dividerPositions = new ArrayList<>();
		cornerRadius = 0;

		TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SegmentedProgressBar, 0, 0);

		try {
			dividerPaint.setColor(typedArray.getColor(R.styleable.SegmentedProgressBar_dividerColor,
					ContextCompat.getColor(getContext(), R.color.white)));
			progressBarBackgroundPaint.setColor(typedArray.getColor(R.styleable.SegmentedProgressBar_progressBarBackgroundColor,
					ContextCompat.getColor(getContext(), R.color.grey_light)));
			progressBarPaint.setColor(typedArray.getColor(R.styleable.SegmentedProgressBar_progressBarColor,
					ContextCompat.getColor(getContext(), R.color.progress_bar)));
			dividerWidth = typedArray.getDimension(R.styleable.SegmentedProgressBar_dividerWidth, dividerWidth);
			isDividerEnabled = typedArray.getBoolean(R.styleable.SegmentedProgressBar_isDividerEnabled, true);
			divisions = typedArray.getInteger(R.styleable.SegmentedProgressBar_divisions, divisions);
			cornerRadius = typedArray.getDimension(R.styleable.SegmentedProgressBar_cornerRadius, 2f);

		} finally {
			typedArray.recycle();
		}

		ViewTreeObserver viewTreeObserver = getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (getWidth() > 0) {
						getViewTreeObserver().removeOnGlobalLayoutListener(this);
						progressBarWidth = getWidth();
						dividerPositions.clear();
						if (divisions > 1) {
							for (int i = 1; i < divisions; i++) {
								dividerPositions.add(((float) (progressBarWidth * i) / divisions));
							}
						}
						bgRect = new RectF(0, 0, getWidth(), getHeight());
						updateProgress();
					}
				}
			});
		}
	}

	/**
	 * Updates the progress bar based on manually passed percent value.
	 */
	private void updateProgress() {
		invalidate();
	}

	public SegmentedProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public SegmentedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SegmentedProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (bgRect != null) {

			canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, progressBarBackgroundPaint);

			for (Paint currentDivisionPaint : enabledDivisions.keySet()) {
				for (Integer currentDivision : enabledDivisions.get(currentDivisionPaint)) {
					if (currentDivision < divisions) {
						float left = 0;
						if (currentDivision != 0) {
							left = dividerPositions.get(currentDivision - 1) + dividerWidth;
						}
						float right = currentDivision >= dividerPositions.size() ? progressBarWidth : dividerPositions.get(currentDivision);

						RectF rect = new RectF(left, 0, right, getHeight());
						canvas.drawRoundRect(rect, cornerRadius, cornerRadius, currentDivisionPaint);
						if (currentDivision == 0) {
							canvas.drawRect(left + cornerRadius, 0, right, getHeight(), currentDivisionPaint);
						} else if (currentDivision == divisions - 1) {
							canvas.drawRect(left, 0, right - cornerRadius, getHeight(), currentDivisionPaint);
						} else {
							canvas.drawRect(rect, currentDivisionPaint);
						}
					}

					if (divisions > 1 && isDividerEnabled) {
						for (int i = 1; i < divisions; i++) {
							float leftPosition = dividerPositions.get(i - 1);
							canvas.drawRect(leftPosition, 0, leftPosition + dividerWidth, getHeight(), dividerPaint);
						}
					}
				}
			}
		}
	}

	/**
	 * Set the color for the progress bar background
	 *
	 * @param color
	 */
	public void setBackgroundColor(int color) {
		progressBarBackgroundPaint.setColor(color);
	}

	public void reset() {
		dividerPositions.clear();
		percentCompleted = 0;
		updateProgress();
	}

	/**
	 * Set the color for the progress bar
	 *
	 * @param color
	 */
	@Deprecated
	public void setProgressBarColor(int color) {
		progressBarPaint.setColor(color);
	}

	/**
	 * Set the color for the divider bar
	 *
	 * @param color
	 */
	public void setDividerColor(int color) {
		dividerPaint.setColor(color);
	}

	/**
	 * set the width of the divider
	 *
	 * @param width
	 */
	public void setDividerWidth(float width) {
		if (width < 0) {
			Log.w(TAG, "setDividerWidth: Divider width can not be negative");
			return;
		}
		dividerWidth = width;
	}

	/**
	 * Set whether the dividers are enabled or not.
	 *
	 * @param value true or false
	 */
	public void setDividerEnabled(boolean value) {
		isDividerEnabled = value;
	}

	/**
	 * Sets the number of divisions in the ProgressBar.
	 *
	 * @param divisions number of divisions
	 */
	public void setDivisions(int divisions) {
		if (divisions < 1) {
			Log.w(TAG, "setDivisions: Number of Divisions cannot be less than 1");
			return;
		}
		this.divisions = divisions;
		dividerPositions.clear();
		if (divisions > 1) {
			for (int i = 1; i < divisions; i++) {
				dividerPositions.add(((float) (progressBarWidth * i) / divisions));
			}
		}
		updateProgress();
	}

	/**
	 * Set the enabled divisions to specified value.
	 *
	 * @param enabledDivisions number of divisions to be enabled
	 */
	public void setEnabledDivisions(Map<Paint, List<Integer>> enabledDivisions) {
		this.enabledDivisions = enabledDivisions;
		updateProgress();
	}

	public void setCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
	}
}
