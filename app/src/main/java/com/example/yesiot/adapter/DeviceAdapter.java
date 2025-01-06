package com.example.yesiot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yesiot.R;
import com.example.yesiot.object.Device;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    String TAG = "DeviceAdapter";
    private final Context mContext;//上下文对象
    private final List<Device> dataList;//ListView显示的数据
    private boolean editable = false;

    public DeviceAdapter(Context context, List<Device> list){
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_device, parent, false);
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
        //设置内容
        viewHolder.tvName.setText(device.getName());
        viewHolder.tvDesc.setText(device.getCode());
        //设置状态
        viewHolder.tvStatus.setText(device.getStatus());
        //设置icon
        int icon = editable ? R.drawable.ic_option : R.drawable.ic_baseline_arrow_forward_ios_24;
        viewHolder.iconView.setImageResource(icon);

        return convertView;
    }

    public void notifyDataSetChanged(ListView listView, int position) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int lastVisiblePosition = listView.getLastVisiblePosition();

        View view = listView.getChildAt(position - firstVisiblePosition);
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            /**获取指定位置view对象**/
            getView(position, view, listView);
        }
    }

    public void setDataList(List<Device> list) {
        dataList.clear();
        dataList.addAll(list);
    }

    public void setEditable(boolean val){
        if(editable == val) return;
        editable = val;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;//图片
        TextView tvName;  //内容
        TextView tvDesc;
        TextView tvStatus;
        ImageView iconView;
        LinearLayout actionIcon;

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.listview_item_image);
            tvName = view.findViewById(R.id.listview_item_name);
            tvDesc = view.findViewById(R.id.listview_item_desc);
            tvStatus = view.findViewById(R.id.listview_item_status);
            iconView = view.findViewById(R.id.listview_item_icon);
            actionIcon = view.findViewById(R.id.listview_action_icon);
        }
    }
}