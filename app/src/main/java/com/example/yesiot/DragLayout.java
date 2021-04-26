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

import java.util.HashMap;
import java.util.Map;

public class DragLayout extends RelativeLayout
{
    private ViewDragHelper viewDragHelper;
    private boolean draggable = true;
    private Map<String,String> map=new HashMap<>();

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
                    String tag = (String) child.getTag();
                    int left = child.getLeft();
                    int top = child.getTop();
                    map.put(tag,left+"#"+top);
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
            String tag = (String)child.getTag();
            if(TextUtils.isEmpty(tag)||map.get(tag)==null){
                //child.setTag("TAG_"+count);
                //map.put("TAG_"+count,child.getLeft()+"#"+child.getTop());
                continue;
            }
            String xy = map.get(tag).toString();
            //System.out.println("<onLayout>"+tag+": "+xy);
            if (!TextUtils.isEmpty(xy)&&!xy.equals("0")) {
                String[] xys = xy.split("#");
                if (xys.length == 2) {
                    child.layout(Integer.parseInt(xys[0]), Integer.parseInt(xys[1]), child.getMeasuredWidth() + Integer.parseInt(xys[0]), child.getMeasuredHeight() + Integer.parseInt(xys[1]));
                }
            }
        }
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        int count = getChildCount();
        for(int i=0;i<count;i++){
            View child = getChildAt(i);
            String tag = (String)child.getTag();
            if(TextUtils.isEmpty(tag)){
                tag="TAG_"+count;
                child.setTag("tag");
            }
            map.put(tag,child.getLeft()+"#"+child.getRight());
            System.out.println("<onFinishInflate>"+tag+":"+child.getLeft()+"#"+child.getRight());
        }
    }

    public void setMap(Map<String,String> map){
        this.map=map;
    }
    public Map<String,String> getMap(){
        return map;
    }
    public void setPos(String tag, String pos){
        map.put(tag,pos);
    }
    public void setPos(String tag, int left, int top){
        String pos = left+"#"+top;
        map.put(tag,pos);
    }

    public boolean getDraggable() {
        return draggable;
    }
    public void setDraggable(boolean flag) {
        draggable = flag;
    }

    private OnChangeListener listener;
    public void setOnChangeListener(DragLayout.OnChangeListener onChangeListener){
        this.listener = onChangeListener;
    }
    public interface OnChangeListener{
        void onPositionChanged(View view, int left, int top);
    }
}