package com.example.yesiot;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.yesiot.object.Panel;
import com.example.yesiot.util.Utils;

public class PanelLayout extends LinearLayout {
    public static int[] BUTTON_OPTIONS = {
            R.layout.option_panel_text,
            R.layout.option_panel_vertical,
            R.layout.option_panel_horizontal,
            R.layout.option_panel_switch,
    };
    public static int[] DATA_OPTIONS = {
            R.layout.option_panel_data_arc,
            R.layout.option_panel_data_ring,
    };

    public static int[] BUTTON_TYPES = {
            R.layout.layout_panel_text,
            R.layout.layout_panel_vertical,
            R.layout.layout_panel_horizontal,
            R.layout.layout_panel_switch,
    };
    public static int[] DATA_TYPES = {
            R.layout.layout_panel_data_arc,
            R.layout.layout_panel_data_ring,
    };

    final int TYPE_TEXT = 0;
    final int TYPE_DEFAULT = 1;
    final int TYPE_HORIZONTAL = 2;
    final int TYPE_SWITCH = 3;

    Context mContext;
    LinearLayout mView;
    ViewHolder viewHolder;
    Panel mPanel;
    String mValue="";

    public PanelLayout(Context context) {
        super(context);
        mContext = context;
    }

    public PanelLayout(Context context, Panel panel) {
        super(context);
        mContext = context;
        setPanel(panel);
    }

    public LinearLayout getView(){
        return mView;
    }

    public Panel getPanel() {
        return mPanel;
    }
    public void setPanel(Panel panel) {
        mPanel = panel;
        int width = panel.width==0?LayoutParams.WRAP_CONTENT:panel.width;
        int height = panel.height==0?LayoutParams.WRAP_CONTENT:panel.height;
        if(panel.design>=BUTTON_TYPES.length)panel.design=BUTTON_TYPES.length-1;
        int res;
        if(panel.type==2){
            if(panel.design>=DATA_TYPES.length)panel.design=DATA_TYPES.length-1;
            res = DATA_TYPES[panel.design];
        }else{
            if(panel.design>=BUTTON_TYPES.length)panel.design=BUTTON_TYPES.length-1;
            res = BUTTON_TYPES[panel.design];
        }
        mView = (LinearLayout)LayoutInflater.from(mContext).inflate(res,null);
        setId(panel.id);
        setTag(panel.type);
        viewHolder = new ViewHolder(mView);
        setTitle(panel.title);
        setText(panel.unit);
        setBackgroundResource(R.drawable.selector_panel);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(width,height);
        addView(mView,layoutParams);

        if(panel.type == 2){
            viewHolder.dataView.setText(panel.title);
            if(!TextUtils.isEmpty(panel.unit)){
                viewHolder.dataView.setUnit(panel.unit);
            }
            if(TextUtils.isEmpty(panel.title_size))panel.title_size="0";
            if(TextUtils.isEmpty(panel.unit_size))panel.unit_size="0";
            float textSize = Float.parseFloat(panel.title_size);
            float unitSize = Float.parseFloat(panel.unit_size);
            viewHolder.dataView.setTextSize(textSize, unitSize);
        }else{
            setTitle(panel.title);
            setText(panel.unit);
            switch(panel.design){
                case TYPE_HORIZONTAL:
                    //int imgWidth = LayoutParams.MATCH_PARENT;
                    int imgHeight = LayoutParams.MATCH_PARENT;
                    int imgWidth = Math.min(width, height);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imgWidth, imgHeight);
                    viewHolder.imageView.setLayoutParams(params);
                    break;
                case TYPE_SWITCH:
                    break;
            }
            setTextSize(viewHolder.titleView, panel.title_size);
            setTextSize(viewHolder.textView, panel.unit_size);
        }
    }

    private void setTextSize(View view, String textSize){
        if(view == null || TextUtils.isEmpty(textSize) || !Utils.isDoubleOrFloat(textSize)) return;
        float size = Float.parseFloat(textSize);
        if(view instanceof TextView){
            TextView textView = (TextView) view;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
        }
    }

    public void setTitle(String text){
        if(viewHolder.titleView == null) return;
        viewHolder.titleView.setText(text);
    }
    public void setText(String text){
        if(viewHolder.textView == null)return;

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
    public String getValue(){return mValue;}
    public void setValue(String value){
        mValue = value;
        if(mPanel.type==2){
            if(Utils.isDoubleOrFloat(value)) {
                float progress = Float.parseFloat(value);
                if(viewHolder.dataView !=null){
                    viewHolder.dataView.setProgress(progress);
                }
                Log.w("PanelLayout","setValue: "+mPanel.name +" -> "+ value);
            }else{
                Log.e("PanelLayout","数据格式错误："+value);
            }
        }else{
            int color;
            if(value.equals(mPanel.on)){
                color = mContext.getColor(R.color.panel_on);
            }else if(value.equals(mPanel.off)){
                color = mContext.getColor(R.color.panel_off);
            }else{
                return;
            }
            if(viewHolder.imageView.getVisibility()==VISIBLE){
                if(mPanel.design == 4){
                    int imgRes = value.equals(mPanel.on)?R.drawable.ic_baseline_toggle_on_24:R.drawable.ic_baseline_toggle_off_24;
                    viewHolder.imageView.setImageResource(imgRes);
                }
                viewHolder.imageView.setColorFilter(color);
            }else{
                viewHolder.titleView.setTextColor(color);
            }
        }
    }

    static class ViewHolder{
        public ImageView imageView;
        public TextView titleView;
        public TextView textView;
        public LinearLayout textWrapper;
        public DataView dataView;

        public ViewHolder(View view){
            imageView = view.findViewById(R.id.panel_image);
            textWrapper = view.findViewById(R.id.panel_text_wrapper);
            titleView = view.findViewById(R.id.panel_title);
            textView = view.findViewById(R.id.panel_text);
            dataView = view.findViewById(R.id.panel_data);
        }
    }
}
