package com.example.yesiot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yesiot.R;
import com.example.yesiot.object.Device;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;

import java.util.List;

public class GridAdapter extends BaseAdapter {
    String TAG = "GridAdapter";
    private final Context mContext;//上下文对象
    private final List<Device> dataList;//ListView显示的数据
    private boolean editable = false;

    public GridAdapter(Context context, List<Device> list){
        mContext = context;
        dataList = list;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_item_device, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Device device = dataList.get(position);
        Log.i(TAG, new Gson().toJson(device));
        //设置图片
        Bitmap Bitmap = Utils.getAssetsBitmap(mContext,device.getImage());
        if(Bitmap != null){
            viewHolder.imageView.setImageBitmap(Bitmap);
            viewHolder.imageView.setBackground(null);
        }
        //设置名称
        viewHolder.textView.setText(device.getName());
        //设置在线状态
        int status = R.string.offline;
        int drawable = R.drawable.selector_gridview;
        int color = Color.GRAY;
        if(device.getStatus().equals("online")){
            status = R.string.online;
            drawable = R.drawable.selector_gridview_online;
            color = mContext.getColor(R.color.colorPrimary);
        }
        viewHolder.statusView.setTextColor(color);
        convertView.setBackground(mContext.getDrawable(drawable));
        viewHolder.statusView.setText(mContext.getString(status));

        Log.w(TAG, "State: " + device.getState());
        if(device.getState().equals("on")){
            viewHolder.buttonView.setColorFilter(Color.RED);
        }else if(device.getState().equals("off")){
            viewHolder.buttonView.setColorFilter(Color.BLUE);
        }else{
            viewHolder.buttonView.setColorFilter(Color.GRAY);
        }

        viewHolder.buttonView.setOnClickListener(v -> {
            if(onActionClickListener != null){
                onActionClickListener.onClick(v,position);
            }
        });

        return convertView;
    }

    public void notifyDataSetChanged(GridView gridView, int position) {
        int firstVisiblePosition = gridView.getFirstVisiblePosition();
        int lastVisiblePosition = gridView.getLastVisiblePosition();

        View view = gridView.getChildAt(position - firstVisiblePosition);
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            /**获取指定位置view对象**/
            getView(position, view, gridView);
        }
    }

    public void setEditable(boolean val){
        if(editable == val) return;
        editable = val;
        notifyDataSetChanged();
    }

    private OnActionClickListener onActionClickListener;
    public void setOnActionClickListener(OnActionClickListener clickListener){
        onActionClickListener = clickListener;
    }
    public interface OnActionClickListener{
        void onClick(View view, int position);
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;//图片
        TextView textView;  //内容
        TextView statusView; //在线或离线
        ImageButton buttonView;//开关图片

        /**
         * 构造器
         *
         * @param view 视图组件（GridView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.gridview_item_image);
            textView = view.findViewById(R.id.gridview_item_name);
            statusView = view.findViewById(R.id.gridview_item_status);
            buttonView = view.findViewById(R.id.gridview_item_button);
        }
    }
}