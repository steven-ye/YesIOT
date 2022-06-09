package com.example.yesiot;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.yesiot.util.SPUtils;

import java.util.List;
import java.util.Map;

public class BrokerAdapter extends BaseAdapter {
    String TAG = "BrokerAdapter";
    private final Context mContext;//上下文对象
    private final List<Map<String,String>> dataList;//ListView显示的数据
    private int selectedPosition = -1;
    private int selectedId = 0;

    public BrokerAdapter(Context context, List<Map<String,String>> list){
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_listview_broker, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Map<String,String> map = dataList.get(position);
        Log.i(TAG, "Brokers: "+map.toString());

        //设置Name
        viewHolder.tvName.setText(map.get("name"));
        //设置Host
        String serverUrl = map.get("host")+":"+map.get("port")+map.get("path");
        viewHolder.tvHost.setText(serverUrl);
        int id = Integer.parseInt(map.get("id"));
        if(id == selectedId){
            viewHolder.imageView.setColorFilter(mContext.getColor(R.color.colorPrimary));
            viewHolder.radioButton.setChecked(true);
        }else{
            viewHolder.imageView.setColorFilter(mContext.getColor(R.color.grey));
            viewHolder.radioButton.setChecked(false);
        }

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

    public void setSelectedPosition(int position){
        selectedId = Integer.parseInt(dataList.get(position).get("id"));
    }
    public void setSelectedId(int id){
        selectedId = id;
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;
        TextView tvName;  //内容
        TextView tvHost;
        RadioButton radioButton;

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.listview_item_image);
            tvName = view.findViewById(R.id.listview_item_name);
            tvHost = view.findViewById(R.id.listview_item_host);
            radioButton = view.findViewById(R.id.listview_item_radio);
        }
    }
}