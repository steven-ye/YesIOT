package com.example.yesiot;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yesiot.object.Panel;

public class PanelView extends LinearLayout {
    final int TYPE_TEXT = 0;
    final int TYPE_IMAGE = 1;
    final int TYPE_VERTICAL = 2;
    final int TYPE_HORIZONTAL = 3;
    final int TYPE_SWITCH = 4;
    final int TYPE_DATA = 5;

    Context mContext;
    int mType;
    String mStatus = "";
    LinearLayout mView;
    ViewHolder viewHolder;
    Panel mPanel;

    int colorOn;
    int colorOff;
    int imgOn;
    int imgOff;
    String textOn="On";
    String textOff="Off";

    public PanelView(Context context, int type) {
        super(context);
        mContext = context;
        mType = type;

        setBackgroundResource(R.drawable.selector_panel);

        colorOn =  mContext.getColor(R.color.panel_on);
        colorOff =  mContext.getColor(R.color.panel_off);

        int resId;
        switch(mType){
            case TYPE_HORIZONTAL:
                resId = R.layout.layout_panel;
                break;
            case TYPE_SWITCH:
                resId = R.layout.layout_panel_switch;
                colorOn = Color.RED;
                colorOff =  Color.GRAY;
                imgOn = R.drawable.ic_baseline_toggle_on_24;
                imgOff = R.drawable.ic_baseline_toggle_off_24;
                break;
            case TYPE_DATA:
                resId = R.layout.layout_panel_data;
                break;
            default:
                resId = R.layout.layout_panel_vertical;
        }

        mView = (LinearLayout)LayoutInflater.from(context).inflate(resId, null);
        viewHolder = new ViewHolder(mView);

        switch(type) {
            case TYPE_TEXT:
                viewHolder.imageView.setVisibility(GONE);
                break;
            case TYPE_IMAGE:
                viewHolder.textWrapper.setVisibility(GONE);
                break;
        }
    }

    public Panel getPanel(){
        return mPanel;
    }
    public void setPanel(Panel panel) {
        mPanel = panel;
        int width = panel.width==0?LayoutParams.WRAP_CONTENT:panel.width;
        int height = panel.height==0?LayoutParams.WRAP_CONTENT:panel.height;

        this.setTag(panel.id+"");
        setTitle(panel.title);
        setText(panel.unit);
        LayoutParams params;
        switch(panel.type){
            case TYPE_TEXT:
                viewHolder.imageView.setVisibility(GONE);
                break;
            case TYPE_IMAGE:
                viewHolder.textWrapper.setVisibility(GONE);
                break;
            case TYPE_HORIZONTAL:
                if(panel.height==0)height = 100;
                params = new LayoutParams(height, height);
                viewHolder.imageView.setLayoutParams(params);
                break;
            case TYPE_DATA:
                viewHolder = new ViewHolder(mView);
                viewHolder.ringView.setText(panel.title);
                if(!TextUtils.isEmpty(panel.unit)){
                    viewHolder.ringView.setUnit(panel.unit);
                }

                width = panel.height==0?250:panel.height;
                height = panel.height==0?width:panel.height;
                break;
        }
        LayoutParams layoutParams = new LayoutParams(width,height);
        addView(mView,layoutParams);
    }

    public LinearLayout getView(){
        return mView;
    }

    public void setSize(int width, int height){
        width = width==0?LayoutParams.WRAP_CONTENT: width;
        height = height==0?LayoutParams.WRAP_CONTENT: height;
        LayoutParams params;
        switch(mType) {
            case TYPE_HORIZONTAL:
                if (height <= 0) height = 100;
                params = new LayoutParams(100, height);
                viewHolder.imageView.setLayoutParams(params);
                break;
            case TYPE_DATA:
                if (width <= 0) width = 250;
                if (height <= 0) height = 250;
                break;
            default:

        }

        mView.setLayoutParams(new LayoutParams(width,height));
    }

    public void setWidth(int width){
    }
    public void setHeight(int height){

    }
    public void setImageWidth(int width){

    }
    public void setImageHeight(int height){

    }
    public void setImage(int layouId){
        if(mType == TYPE_DATA) return;
        viewHolder.imageView.setImageResource(layouId);
    }
    public void setTitle(String text){
        if(mType == TYPE_DATA){
            viewHolder.ringView.setText(text);
        }else{
            viewHolder.titleView.setText(text);
        }
    }
    public void setText(String text){
        if(mType == TYPE_DATA) return;
        viewHolder.textView.setText(text);
        if(TextUtils.isEmpty(text)){
            viewHolder.textView.setVisibility(GONE);
        }else{
            viewHolder.textView.setVisibility(VISIBLE);
        }
    }
    public void setUnit(String unit){
        if(mType != TYPE_DATA) return;
        viewHolder.ringView.setUnit(unit);
    }
    public void setProgess(float progress){
        if(viewHolder.ringView == null) return;
        viewHolder.ringView.setProgress(progress);
    }
    public boolean isDataView(){
        return mType == TYPE_DATA;
    }

    public String getState(){
        return mStatus;
    }
    public void setState(String status){
        mStatus = status;
        int color;
        if("on".equals(status)){
            color = mContext.getColor(R.color.panel_on);
        }else if("off".equals(status)){
            color = mContext.getColor(R.color.panel_off);
        }else{
            return;
        }
        if(viewHolder.imageView.getVisibility()==VISIBLE){
            viewHolder.imageView.setColorFilter(color);
        }else{
            viewHolder.titleView.setTextColor(color);
        }
        if(TYPE_SWITCH == mType){
            int imgOn = R.drawable.ic_baseline_toggle_on_24;
            int imgOff = R.drawable.ic_baseline_toggle_off_24;
            viewHolder.imageView.setImageResource("on".equals(mStatus)?imgOn:imgOff);
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
