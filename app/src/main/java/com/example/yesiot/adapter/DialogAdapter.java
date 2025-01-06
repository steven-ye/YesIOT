package com.example.yesiot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yesiot.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DialogAdapter extends BaseAdapter {
    String TAG = "DialogAdapter";
    private final Context mContext;//上下文对象
    private final int mResource;
    private final List<Map<String, ?>> dataList;//ListView显示的数据

    public DialogAdapter(Context context, List<Map<String, ?>> data){
        mContext = context;
        dataList = data;
        mResource = R.layout.dialog_listview;
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
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Map<String,?> map = dataList.get(position);
        //设置图片
        int res = Integer.parseInt(Objects.requireNonNull(map.get("icon")).toString());
        viewHolder.imageView.setImageResource(res);
        //设置内容
        viewHolder.textView.setText(Objects.requireNonNull(map.get("title")).toString());
        return convertView;
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;//图片
        TextView textView;  //内容

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.listview_option_icon);
            textView = view.findViewById(R.id.listview_option_title);
        }
    }
}