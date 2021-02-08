/*
 * Copyright 2021 Christopher Zaborsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unary.digitalrain;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AnimatorRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * A styleable widget that recreates the classic Matrix digital rain effect. It features a number of
 * customizations for the animation and text.
 *
 * <p><strong>XML attributes</strong></p>
 * <p>The following optional attributes can be used to change the look and feel of the view:</p>
 * <pre>
 *   app:rainAlpha="float"               // How quickly the character trails fade
 *   app:rainAnimator="reference"        // Animator to use for the text rain
 *   app:rainDepth="float"               // Layer depth blur. This is drawing intensive
 *   app:rainIntensity="integer"         // Default number of layers is 3
 *   app:rainSpeed="integer"             // Time interval in milliseconds for speed
 *
 *   android:autoStart="boolean"         // If animation should start automatically
 *   android:enabled="boolean"           // Changes the view state
 *   android:textColor="reference|color" // Reference to a color selector or simple color
 *   android:textSize="dimension"        // Text size to use. Default is "14sp"
 * </pre>
 * <p>See {@link R.styleable#DigitalRain DigitalRain Attributes}, {@link R.styleable#View View Attributes}</p>
 */
public class DigitalRain extends View implements TimeAnimator.TimeListener, Runnable {

    private static final float VIEW_WIDTH = 256; // dp
    private static final float VIEW_HEIGHT = 256; // dp
    private static final float RAIN_ALPHA = 24f / 255;
    private static final float RAIN_DEPTH = 0;
    private static final int RAIN_INTENSITY = 3;
    private static final int RAIN_SPEED = 100; // ms
    private static final boolean AUTO_START = true;
    private static final int TEXT_COLOR = R.attr.colorControlNormal;
    private static final int TEXT_COLOR_DISABLED = R.attr.colorControlHighlight;
    private static final float TEXT_SIZE = 14; // sp

    private float mRainAlpha;
    private Animator mRainAnimator;
    private long mDeltaTime;
    private boolean mRunning;
    private float mRainDepth;
    private Rain[] mRainIntensity;
    private int mRainSpeed;
    private boolean mAutoStart;
    private ColorStateList mTextColor;
    private Paint mTextPaint;
    private Canvas mRainCanvas;
    private Bitmap mCanvasBitmap;
    private Rect mDrawingRect;

    /**
     * Simple constructor to use when creating the view from code.
     *
     * @param context Context given for the view. This determines the resources and theme.
     */
    public DigitalRain(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context Context given for the view. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public DigitalRain(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    /**
     * Constructor called when inflating from XML and applying a style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    public DigitalRain(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    /**
     * Constructor that is used when given a default shared style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     * @param defStyleRes  Default style resource to apply to this view.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DigitalRain(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Shared method to initialize the member variables from the XML and create the drawing objects.
     * Input values are checked for sanity.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     * @param defStyleRes  Default style resource to apply to this view.
     */
    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.DigitalRain, defStyleAttr, defStyleRes);

        int animatorRes;
        int intensity;
        boolean enabled;
        float textSize;

        try {
            mRainAlpha = typedArray.getFloat(R.styleable.DigitalRain_rainAlpha, RAIN_ALPHA);
            animatorRes = typedArray.getResourceId(R.styleable.DigitalRain_rainAnimator, 0);
            mRainDepth = typedArray.getFloat(R.styleable.DigitalRain_rainDepth, RAIN_DEPTH);
            intensity = typedArray.getInt(R.styleable.DigitalRain_rainIntensity, RAIN_INTENSITY);
            mRainSpeed = typedArray.getInt(R.styleable.DigitalRain_rainSpeed, RAIN_SPEED);
            mAutoStart = typedArray.getBoolean(R.styleable.DigitalRain_android_autoStart, AUTO_START);
            enabled = typedArray.getBoolean(R.styleable.DigitalRain_android_enabled, isEnabled());
            mTextColor = typedArray.getColorStateList(R.styleable.DigitalRain_android_textColor);
            textSize = typedArray.getDimension(R.styleable.DigitalRain_android_textSize, dpToPixels(context, TEXT_SIZE));
        } finally {
            typedArray.recycle();
        }

        // Provide a default animator
        if (animatorRes != 0) {
            setRainAnimatorResource(animatorRes);
        } else {
            setRainAnimator(new TimeAnimator());
        }

        // Provide some default colors
        if (mTextColor == null) {
            int[][] states = new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}};
            int[] colors = new int[]{getAttrColor(context, TEXT_COLOR_DISABLED), getAttrColor(context, TEXT_COLOR)};

            mTextColor = new ColorStateList(states, colors);
        }

        // Sanitize the input values
        intensity = Math.max(intensity, 0);
        mRainSpeed = Math.max(mRainSpeed, 0);

        // Initialize the drawing objects
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(textSize);

        mDrawingRect = new Rect();
        mRainIntensity = new Rain[intensity];

        // Set a stateful text color
        setEnabled(enabled);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), dpToPixels(getContext(), VIEW_WIDTH));
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), dpToPixels(getContext(), VIEW_HEIGHT));
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        if (mAutoStart && mRainAnimator != null) {
            switch (visibility) {
                case VISIBLE:
                    mRainAnimator.start();
                    break;
                default:
                    mRainAnimator.end();
                    break;
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int paddingStart = getPaddingLeft();
        int paddingEnd = getPaddingRight();

        // Use RTL if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isLtrLayout(this)) {
                paddingStart = getPaddingStart();
                paddingEnd = getPaddingEnd();
            } else {
                paddingStart = getPaddingEnd();
                paddingEnd = getPaddingStart();
            }
        }

        // Similar to getDrawingRect()
        mDrawingRect.set(paddingStart, getPaddingTop(),
                getWidth() - paddingEnd, getHeight() - getPaddingBottom());

        if (mDrawingRect.width() < 1 || mDrawingRect.height() < 1) return;

        // Allocate here for padding
        mCanvasBitmap = Bitmap.createBitmap(mDrawingRect.width(), mDrawingRect.height(), Bitmap.Config.ARGB_8888);
        mRainCanvas = new Canvas(mCanvasBitmap);

        for (int i = 0; i < mRainIntensity.length; i++) {
            Paint paint = new Paint(mTextPaint);

            if (mRainDepth > 0) {
                float radius = i * mRainDepth + 0.001f;
                paint.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL));
            }

            mRainIntensity[i] = new Rain(mRainCanvas, paint);
        }

        // Start the rain immediately
        mDeltaTime = mRainSpeed;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Animator might leak context
        if (mRainAnimator != null) {
            mRainAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCanvasBitmap != null) {
            canvas.drawBitmap(mCanvasBitmap, mDrawingRect.left, mDrawingRect.top, null);
        }
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        mDeltaTime += deltaTime;

        // Don't pile up the updates
        if (mDeltaTime > mRainSpeed && !mRunning && mRainCanvas != null) {
            mDeltaTime = 0;
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        mRunning = true;

        // Fade out old characters
        mRainCanvas.drawColor((int) (mRainAlpha * 255) << 24, PorterDuff.Mode.DST_OUT);

        for (int i = 0; i < mRainIntensity.length; i++) {
            mRainIntensity[i].draw();
        }

        // Post updates to the UI
        if (getHandler() != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }

        mRunning = false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setTextColor(mTextColor);
    }

    /**
     * Check if the layout direction for the given view or configuration is left-to-right.
     *
     * @param view View to check.
     * @return True if likely LTR.
     */
    protected static boolean isLtrLayout(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (view.isLayoutDirectionResolved()) {
                return view.getLayoutDirection() == LAYOUT_DIRECTION_LTR;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return view.getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_LTR;
        }

        return true;
    }

    /**
     * Utility method to find the pixel resolution of a density pixel (dp) value.
     *
     * @param context Context given for the metrics.
     * @param dp      Density pixels to convert.
     * @return The pixel resolution.
     */
    private static int dpToPixels(Context context, @Dimension float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * Utility method to find the density pixel (dp) value of a pixel resolution.
     *
     * @param context Context given for the metrics.
     * @param px      Pixel resolution to convert.
     * @return The density pixels.
     */
    @Dimension
    private static float pixelsToDp(Context context, int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * Utility method to find a color integer as defined in a theme attribute.
     *
     * @param context   Context given for the theme.
     * @param attrResId The color attribute.
     * @return Resolved color integer.
     */
    @ColorInt
    private static int getAttrColor(Context context, @AttrRes int attrResId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrResId, typedValue, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(typedValue.resourceId, context.getTheme());
        }

        return context.getResources().getColor(typedValue.resourceId);
    }

    /**
     * Utility method to find the preferred measurements of this view for the view parent.
     *
     * @param defaultSize Default size of the view.
     * @param measureSpec Constraints imposed by the parent.
     * @return Preferred size for this view.
     * @see View#getDefaultSize(int, int)
     */
    public static int getDefaultSize(int defaultSize, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.AT_MOST:
                return Math.min(size, defaultSize);
            case MeasureSpec.UNSPECIFIED:
            default:
                return defaultSize;
        }
    }

    /**
     * Get the opacity used for the trailing text effect. The range is from 0 to 1 for none.
     *
     * @return Falling rain alpha.
     */
    public float getRainAlpha() {
        return mRainAlpha;
    }

    /**
     * Set the opacity used for the trailing text effect. The range is from 0 to 1 for none.
     *
     * @param rainAlpha Falling rain alpha.
     */
    public void setRainAlpha(float rainAlpha) {
        mRainAlpha = rainAlpha;
    }

    /**
     * Get the rain text animator. An initial default animator is assigned if one has not been
     * provided by the client.
     *
     * @return Animator for the text rain.
     */
    @Nullable
    public Animator getRainAnimator() {
        return mRainAnimator;
    }

    /**
     * Set the rain text animator. An initial default animator is assigned if one has not been
     * provided by the client.
     *
     * @param rainAnimator Animator for the text rain.
     */
    public void setRainAnimator(@Nullable Animator rainAnimator) {
        if (mRainAnimator instanceof TimeAnimator) {
            ((TimeAnimator) mRainAnimator).setTimeListener(null);
        }

        if (rainAnimator instanceof TimeAnimator) {
            ((TimeAnimator) rainAnimator).setTimeListener(this);
        }

        mRainAnimator = rainAnimator;
    }

    /**
     * Set the rain text animator resource. A default animator is assigned if one has not been
     * provided by the client.
     *
     * @param animatorResId Resource for the animator.
     */
    public void setRainAnimatorResource(@AnimatorRes int animatorResId) {
        setRainAnimator(AnimatorInflater.loadAnimator(getContext(), animatorResId));
        mRainAnimator.setTarget(this);
    }

    /**
     * Get the depth blur used for layers. This is drawing intensive and best not used.
     *
     * @return The layer depth blur.
     */
    public float getRainDepth() {
        return mRainDepth;
    }

    /**
     * Set the depth blur used for layers. This is drawing intensive and best not used.
     *
     * @param rainDepth The layer depth blur.
     */
    public void setRainDepth(float rainDepth) {
        mRainDepth = rainDepth;
        requestLayout();
    }

    /**
     * Get the number of layers used for the rain. Sanity values are between 1 and 10.
     *
     * @return Number of rain layers.
     */
    public int getRainIntensity() {
        return mRainIntensity.length;
    }

    /**
     * Get the number of layers used for the rain. Sanity values are between 1 and 10.
     *
     * @param rainIntensity Number of rain layers.
     */
    public void setRainIntensity(int rainIntensity) {
        mRainIntensity = new Rain[Math.max(rainIntensity, 0)];
        requestLayout();
    }

    /**
     * Get the time interval in milliseconds for rain speed. This is how fast it updates.
     *
     * @return Falling rain speed.
     */
    public int getRainSpeed() {
        return mRainSpeed;
    }

    /**
     * Set the time interval in milliseconds for rain speed. This is how fast it updates.
     *
     * @param rainSpeed Falling rain speed.
     */
    public void setRainSpeed(int rainSpeed) {
        mRainSpeed = Math.max(rainSpeed, 0);
    }

    /**
     * Get the animator auto start status. This determines if it will run when layout is completed.
     *
     * @return Auto start status.
     */
    public boolean isAutoStart() {
        return mAutoStart;
    }

    /**
     * Set the animator auto start status. This determines if it will run when layout is completed.
     *
     * @param autoStart Auto start status.
     */
    public void setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
    }

    /**
     * Get the rain text color. The default and disabled states are used for the paint color.
     *
     * @return ColorStateList color.
     */
    @NonNull
    public ColorStateList getTextColor() {
        return mTextColor;
    }

    /**
     * Set the rain text color. The default and disabled states are used for the paint color.
     *
     * @param textColor ColorStateList color.
     */
    public void setTextColor(@NonNull ColorStateList textColor) {
        mTextColor = textColor;

        int statefulColor = textColor.getColorForState(getDrawableState(), textColor.getDefaultColor());
        mTextPaint.setColor(statefulColor);

        requestLayout();
    }

    /**
     * Set the rain text color. This is a convenience method for setting the color state.
     *
     * @param color Color integer.
     */
    public void setTextColor(@ColorInt int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Get the rain text size. This gets the equivalent property in the text paint object.
     *
     * @return Size of the rain text.
     */
    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    /**
     * Set the rain text size. This sets the equivalent property in the text paint object.
     *
     * @param textSize Size of the rain text.
     */
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        requestLayout();
    }

    /**
     * Get the rain text paint. It can be used to set other properties not available directly.
     *
     * @return Paint for the text rain.
     */
    @NonNull
    public Paint getTextPaint() {
        return mTextPaint;
    }

    /**
     * Set the rain text paint. It can be used to set other properties not available directly.
     *
     * @param textPaint Paint for the text rain.
     */
    public void setTextPaint(@NonNull Paint textPaint) {
        mTextPaint = textPaint;
        requestLayout();
    }
}