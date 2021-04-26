package com.example.yesiot;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class RingView extends View {
    Context context;
    float mRadius;
    float progress = 0;
    float nowprogress = 0;
    RectF  rect= new RectF();
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ObjectAnimator animator;

    private String mText="温度";
    private String mUnit="℃";
    private float mMaxValue=100;
    private final int mTextColor;
    private final float mTextSize;
    private final float mRingWidth;

    public RingView(Context context) {
        this(context,null);
    }

    public RingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        @SuppressLint("Recycle") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingView);
        mTextColor = typedArray.getColor(R.styleable.RingView_textColor, Color.BLACK);
        mTextSize = typedArray.getDimension(R.styleable.RingView_textSize, 0);
        mRingWidth = typedArray.getDimension(R.styleable.RingView_ringWidth, 0);
        mRadius = typedArray.getDimension(R.styleable.RingView_ringRadius, 0);
        mMaxValue = typedArray.getFloat(R.styleable.RingView_maxValue, mMaxValue);
        progress = typedArray.getFloat(R.styleable.RingView_value, progress);
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
    private void init()
    {
        this.setProgress(progress);
        animator= ObjectAnimator.ofFloat(this, "progress", nowprogress, getProgress());
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
    public void setMaxValue(float maxValue){
        mMaxValue = maxValue;
    }
    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        nowprogress = this.progress;
        this.progress = progress;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height= getHeight();
        float centerX=width/2;
        float centerY=height/2;
        int [] colors={Color.GREEN,Color.YELLOW,Color.RED};

        //如果未设置半径，则半径的值为view的宽、高一半的较小值
        float radius = mRadius == 0 ? Math.min(width / 2, height / 2) : mRadius;
        //圆环的宽度默认为半径的1／10
        float ringWidth = mRingWidth == 0 ? radius / 10 : mRingWidth;

        //由于圆环本身有宽度，所以半径要减去圆环宽度的一半，不然一部分圆会在view外面
        radius = radius - ringWidth;

        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ringWidth);
        //paint.setStrokeWidth(dpToPixel(20));
        paint.setColor(Color.parseColor("#EBEBEB"));
        //paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(rect,180,360,false,paint);

        paint.setStyle(Paint.Style.STROKE);
        @SuppressLint("DrawAllocation") Shader shader =new LinearGradient(centerX - radius,centerY,centerX+radius,centerY,colors,null,Shader.TileMode.CLAMP);
        //int[] arcColors = new int[] {Color.parseColor("#086ab5"),Color.parseColor("#21cbe2")};
        //shader = new SweepGradient(centerX, centerY, arcColors, null);
        paint.setShader(shader);

        float sweepAngle = getProgress()*(360/mMaxValue)*1.0f;

        canvas.drawArc(rect,180,sweepAngle,false,paint);


        float textSize = mTextSize == 0 ? radius/5 : mTextSize;

        paint.setShader(null);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(mTextColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(getProgress()+" "+ mUnit, centerX, centerY-getFontHeight() , paint);
        canvas.drawText(mText, centerX, centerY+getFontHeight(), paint);
    }

    public float getFontHeight() {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}
