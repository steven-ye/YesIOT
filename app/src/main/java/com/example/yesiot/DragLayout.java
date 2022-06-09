package com.example.yesiot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

import com.example.yesiot.object.Panel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragLayout extends RelativeLayout
{
    private ViewDragHelper viewDragHelper;
    private boolean draggable = true;

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (draggable) {
            viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
                @Override
                public boolean tryCaptureView(@NonNull View child, int pointerId) {
                    Object tag = child.getTag();
                    //让视图回到顶层
                    child.bringToFront();
                    return tag!=null;
                }

                @Override
                public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                    // 如果横向滑动超过右面边界的时候，控制子视图不能够越界
                    if (left + child.getMeasuredWidth() >= getMeasuredWidth()) {
                        return getMeasuredWidth() - child.getMeasuredWidth();
                    }
                    //如果横向滑动超过左面边界的时候，控制子视图不能够越界
                    return Math.max(left, 0);
                }

                @Override
                public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                    //控制下边界，子视图不能够越界
                    if (child.getMeasuredHeight() + top > getMeasuredHeight()) {
                        return getMeasuredHeight() - child.getMeasuredHeight();
                    }
                    // 控制上边界，子视图不能够越界
                    return Math.max(top, 0);
                }

                @Override
                public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                    super.onViewPositionChanged(changedView, left, top, dx, dy);
                    //map.put((String)changedView.getTag(), left+"#"+top);
                    //System.out.println("<onViewPositionChanged>"+changedView.getTag()+">"+left+","+top);
                }

                //当手指释放的时候回调
                @Override
                public void onViewReleased(@NonNull View child, float xvel, float yvel) {
                    int left = child.getLeft();
                    int top = child.getTop();

                    int mod = top % 10;
                    top = top / 10;
                    top = top * 10;
                    if(mod > 5){
                        top = top + 10;
                    }
                    mod = left % 10;
                    left = left / 10;
                    left = left * 10;
                    if(mod > 5){
                        left = left + 10;
                    }
                    child.setLeft(left);
                    child.setTop(top);

                    Panel panel = ((PanelLayout)child).getPanel();
                    panel.pos = left+"#"+top;

                    //System.out.println("<onViewReleased>"+tag+":"+child.getLeft()+"#"+child.getTop());
                    if(listener != null){
                        listener.onPositionChanged(child, left, top);
                    }
                }

                //如果你拖动View添加了clickable = true 或者为 button 会出现拖不动的情况，原因是拖动的时候onInterceptTouchEvent方法，
                // 判断是否可以捕获，而在判断的过程中会去判断另外两个回调的方法getViewHorizontalDragRange和getViewVerticalDragRange，
                // 只有这两个方法返回大于0的值才能正常的捕获。如果未能正常捕获就会导致手势down后面的move以及up都没有进入到onTouchEvent
                @Override
                public int getViewHorizontalDragRange(@NonNull View child) {
                    return getMeasuredWidth() - child.getMeasuredWidth();
                }

                @Override
                public int getViewVerticalDragRange(@NonNull View child) {
                    return getMeasuredHeight() - child.getMeasuredHeight();
                }
            });
            viewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (draggable) {
            return viewDragHelper.shouldInterceptTouchEvent(event);
        } else {
            return super.onInterceptTouchEvent(event);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (draggable) {
            viewDragHelper.processTouchEvent(event);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (draggable && viewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child instanceof PanelLayout){
                PanelLayout view = (PanelLayout)getChildAt(i);
                Panel panel = view.getPanel();
                String[] xys = panel.pos.split("#");
                if (xys.length == 2) {
                    int left = Integer.parseInt(xys[0]);
                    int top = Integer.parseInt(xys[1]);
                    int right = left + view.getMeasuredWidth();
                    int bottom = top + view.getMeasuredHeight();
                    view.layout(left, top, right, bottom);
                }
            }
        }
    }

    public void addView(View view) {
        super.addView(view);
        if(null != itemClickListener){
            view.setOnClickListener(itemClickListener);
        }
        if(null != itemLongClickListener){
            view.setOnLongClickListener(itemLongClickListener);
        }
    }

    public void clearSelected(){
        for(int i=0;i<getChildCount();i++){
            View child = getChildAt(i);
            if(child.isSelected())child.setSelected(false);
        }
    }

    public boolean isDraggable() {
        return draggable;
    }
    public void setDraggable(boolean flag) {
        draggable = flag;
    }

    private View.OnClickListener itemClickListener;
    public void setOnItemClickListener(View.OnClickListener clickListener){
        itemClickListener = clickListener;
    }
    private View.OnLongClickListener itemLongClickListener;
    public void setOnItemLongClickListener(View.OnLongClickListener clickListener){
        itemLongClickListener = clickListener;
    }

    private OnChangeListener listener;
    public void setOnChangeListener(DragLayout.OnChangeListener onChangeListener){
        this.listener = onChangeListener;
    }
    public interface OnChangeListener{
        void onPositionChanged(View view, int left, int top);
    }
}