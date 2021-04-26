package com.example.yesiot;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yesiot.object.Panel;

public class PanelLayout extends LinearLayout {
    final int TYPE_TEXT = 0;
    final int TYPE_IMAGE = 1;
    final int TYPE_VERTICAL = 2;
    final int TYPE_HORIZONTAL = 3;
    final int TYPE_SWITCH = 4;
    final int TYPE_DATA = 5;

    Context mContext;
    LinearLayout mView;
    ViewHolder viewHolder;
    Panel mPanel;

    public PanelLayout(Context context) {
        super(context);
        mContext = context;
        mView = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.layout_panel_vertical,this);
        viewHolder = new ViewHolder(mView);
    }

    public PanelLayout(Context context, Panel panel) {
        super(context);
        mContext = context;
        mPanel = panel;
        int width = panel.width==0?LayoutParams.WRAP_CONTENT:panel.width;
        int height = panel.height==0?LayoutParams.WRAP_CONTENT:panel.height;

        mView = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.layout_panel_vertical,null);
        this.setTag(panel.id+"");
        viewHolder = new ViewHolder(mView);
        setTitle(panel.title);
        setText(panel.unit);
        LinearLayout.LayoutParams params;
        switch(panel.type){
            case TYPE_TEXT:
                viewHolder.imageView.setVisibility(GONE);
                viewHolder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                break;
            case TYPE_IMAGE:
                viewHolder.textWrapper.setVisibility(GONE);
                break;
            case TYPE_HORIZONTAL:
                mView = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.layout_panel_horizontal,null);
                viewHolder = new ViewHolder(mView);
                if(panel.height==0)height = 100;
                //params = new LinearLayout.LayoutParams(height, height);
                //viewHolder.imageView.setLayoutParams(params);
                setTitle(panel.title);
                setText(panel.unit);
                break;
            case TYPE_SWITCH:
                mView = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.layout_panel_switch,null);
                viewHolder = new ViewHolder(mView);
                setTitle(panel.title);
                setText(panel.unit);
                break;
            case TYPE_DATA:
                mView = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.layout_panel_data,null);
                viewHolder = new ViewHolder(mView);
                viewHolder.ringView.setText(panel.title);
                if(!TextUtils.isEmpty(panel.unit)){
                    viewHolder.ringView.setUnit(panel.unit);
                }

                width = panel.height==0?250:panel.height;
                height = panel.height==0?width:panel.height;
                break;
        }

        setBackgroundResource(R.drawable.selector_panel);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(width,height);
        addView(mView,layoutParams);
    }

    public boolean isDataView(){
        return mPanel.type == TYPE_DATA;
    }

    public LinearLayout getView(){
        return mView;
    }

    public Panel getPanel() {
        return mPanel;
    }

    public void setTitle(String text){
        viewHolder.titleView.setText(text);
    }
    public void setText(String text){
        viewHolder.textView.setText(text);
        if(TextUtils.isEmpty(text)){
            viewHolder.textView.setVisibility(GONE);
        }else{
            viewHolder.textView.setVisibility(VISIBLE);
        }
    }
    public void setImage(int layouId){
        viewHolder.imageView.setImageResource(layouId);
    }
    public void setProgess(float progress){
        if(viewHolder.ringView == null) return;
        viewHolder.ringView.setProgress(progress);
    }
    public void setState(String status){
        mPanel.status = status;
        int color;
        if("on".equals(status)){
            color = mContext.getColor(R.color.panel_on);
        }else if("off".equals(status)){
            color = mContext.getColor(R.color.panel_off);
        }else{
            return;
        }
        if(viewHolder.imageView.getVisibility()==VISIBLE){
            if(mPanel.type == 4){
                int imgRes = "on".equals(status)?R.drawable.ic_baseline_toggle_on_24:R.drawable.ic_baseline_toggle_off_24;
                viewHolder.imageView.setImageResource(imgRes);
            }
            viewHolder.imageView.setColorFilter(color);
        }else{
            viewHolder.titleView.setTextColor(color);
        }
    }

    static class ViewHolder{
        public ImageView imageView;
        public TextView titleView;
        public TextView textView;
        public LinearLayout textWrapper;
        public RingView ringView;

        public ViewHolder(View view){
            imageView = view.findViewById(R.id.panel_image);
            textWrapper = view.findViewById(R.id.panel_text_wrapper);
            titleView = view.findViewById(R.id.panel_title);
            textView = view.findViewById(R.id.panel_text);
            ringView = view.findViewById(R.id.panel_data_ring);
        }
    }
}
