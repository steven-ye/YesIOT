package com.example.yesiot;

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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false);
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
        viewHolder.textView.setText(device.getName());
        //设置状态
        viewHolder.statusView.setText(device.getStatus());
        //设置icon
        int icon = editable ? R.drawable.ic_option : R.drawable.ic_baseline_arrow_forward_ios_24;
        viewHolder.iconView.setBackgroundResource(icon);

        viewHolder.actionIcon.setOnClickListener(v -> {
            viewHolder.actionView.setVisibility(View.VISIBLE);
        });
        viewHolder.actionIcon.setClickable(editable);
        viewHolder.actionView.setVisibility(View.INVISIBLE);
        viewHolder.actionEdit.setOnClickListener(v -> {
            if(onActionClickListener != null) {
                onActionClickListener.onClick(v, position);
            }
        });
        viewHolder.actionDelete.setOnClickListener(v -> {
            if(onActionClickListener != null) {
                onActionClickListener.onClick(v, position);
            }
        });
        viewHolder.actionCancel.setOnClickListener(v-> {
            viewHolder.actionView.setVisibility(View.INVISIBLE);
        });
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
        TextView statusView;
        ImageView iconView;
        LinearLayout actionIcon;
        LinearLayout actionView;
        LinearLayout actionEdit;
        LinearLayout actionDelete;
        LinearLayout actionCancel;

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.listview_item_image);
            textView = view.findViewById(R.id.listview_item_name);
            statusView = view.findViewById(R.id.listview_item_status);
            iconView = view.findViewById(R.id.listview_item_icon);
            actionIcon = view.findViewById(R.id.listview_action_icon);
            actionView = view.findViewById(R.id.listview_item_action);
            actionEdit = view.findViewById(R.id.item_action_edit);
            actionDelete = view.findViewById(R.id.item_action_delete);
            actionCancel = view.findViewById(R.id.item_action_cancel);
        }
    }
}