package com.example.yesiot;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public abstract class DataView extends View {
    protected Context mContext;
    protected float mRadius;
    protected float mProgress = 0;
    protected float nowProgress = 0;
    protected RectF  rect= new RectF();
    protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected ObjectAnimator animator;

    protected String mText="温度";
    protected String mUnit="℃";
    protected float mMaxValue=180;
    protected final int mTextColor;
    protected float mTextSize;
    protected float mUnitSize = 0;
    protected final float mRingWidth;

    public DataView(Context context) {
        this(context,null);
    }

    public DataView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingView);
        mTextColor = typedArray.getColor(R.styleable.RingView_textColor, Color.BLACK);
        mTextSize = typedArray.getDimension(R.styleable.RingView_textSize, 0);
        mRingWidth = typedArray.getDimension(R.styleable.RingView_ringWidth, 0);
        mRadius = typedArray.getDimension(R.styleable.RingView_ringRadius, 0);
        mMaxValue = typedArray.getFloat(R.styleable.RingView_maxValue, mMaxValue);
        mProgress = typedArray.getFloat(R.styleable.RingView_value, mProgress);
        String text = typedArray.getString(R.styleable.RingView_text);
        String unit = typedArray.getString(R.styleable.RingView_unit);
        typedArray.recycle();
        mText = text==null?mText:text;
        mUnit = unit==null?mUnit:unit;
        init();
    }
    /**
     * 初始化
     */
    protected void init()
    {
        this.setProgress(mProgress);
        animator= ObjectAnimator.ofFloat(this, "progress", nowProgress, getProgress());
        //     animator.setRepeatCount(-1);
        animator.setDuration(1000);
        animator.setInterpolator(new FastOutSlowInInterpolator());
    }

    public void setText(String text){
        mText = text;
    }
    public void setUnit(String unit){
        mUnit = unit;
    }
    public void setTextSize(float textSize){
        mTextSize = textSize;
    }
    public void setTextSize(float textSize, float unitSize){
        mTextSize = textSize;
        mUnitSize = unitSize;
    }
    public void setMaxValue(float maxValue){
        mMaxValue = maxValue;
    }
    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        nowProgress = mProgress;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.end();
    }

    public float getFontHeight() {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    protected int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, mContext.getResources().getDisplayMetrics());
    }
}
