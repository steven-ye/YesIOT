package com.example.yesiot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class ArcView extends DataView {
    protected float mMaxValue=180;

    public ArcView(Context context) {
        this(context,null);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs,defStyleAttr,defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height= getHeight();
        float centerX=width/2;
        float centerY=height/2;
        int [] colors={Color.GREEN,Color.YELLOW,Color.RED};

        //如果未设置半径，则半径的值为view的宽一半、高的较小值
        float radius = mRadius == 0 ? Math.min(width / 2, height) : mRadius;
        //圆环的宽度默认为半径的1／10
        float ringWidth = mRingWidth == 0 ? radius / 10 : mRingWidth;

        //由于圆环本身有宽度，所以半径要减去圆环宽度的一半，不然一部分圆会在view外面
        radius = radius - ringWidth;

        rect.set(centerX - radius, centerY - radius/2, centerX + radius, centerY + radius * 3 /2 + ringWidth);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ringWidth);
        //paint.setStrokeWidth(dpToPixel(20));
        paint.setColor(Color.parseColor("#C0C0E0"));
        //paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(rect,180,180,false,paint);

        paint.setStyle(Paint.Style.STROKE);
        @SuppressLint("DrawAllocation") Shader shader =new LinearGradient(centerX - radius,centerY,centerX+radius,centerY,colors,null,Shader.TileMode.CLAMP);
        //int[] arcColors = new int[] {Color.parseColor("#086ab5"),Color.parseColor("#21cbe2")};
        //shader = new SweepGradient(centerX, centerY, arcColors, null);
        paint.setShader(shader);

        //mProgress = 80;
        float sweepAngle = getProgress()*(180/mMaxValue)*1.0f;
        if(sweepAngle>180) sweepAngle = 180;
        canvas.drawArc(rect,180,sweepAngle,false,paint);

        float unitSize = mUnitSize == 0 ? radius/5 : dp2px(mUnitSize);
        float textSize = mTextSize == 0 ? (float)(unitSize * 1.2) : dp2px(mTextSize);

        paint.setShader(null);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(mTextColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(mText, centerX, centerY+radius/2, paint);
        paint.setTextSize(unitSize);
        canvas.drawText(getProgress()+" "+ mUnit, centerX, centerY+radius/2-textSize, paint);
    }
}
