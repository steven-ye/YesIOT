package com.example.yesiot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.yesiot.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BrokerAdapter extends BaseAdapter {
    String TAG = "BrokerAdapter";
    private final Context mContext;//上下文对象
    private final List<Map<String,String>> dataList;//ListView显示的数据
    private int focusPosition = -1;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_broker, parent, false);
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
        String serverUrl = map.get("host")+":"+map.get("port");
        String path = map.get("path");
        if(!TextUtils.isEmpty(path)){
            serverUrl  += path;
        }
        viewHolder.tvHost.setText(serverUrl);
        int id = Integer.parseInt(Objects.requireNonNull(map.get("id")));

        if(id == selectedId) {
            viewHolder.imageView.setColorFilter(mContext.getColor(R.color.colorPrimary));
            viewHolder.radioView.setChecked(true);
        }else{
            viewHolder.radioView.setChecked(false);
            if(position == focusPosition){
                viewHolder.imageView.setColorFilter(Color.parseColor("#993366FF"));
            }else{
                viewHolder.imageView.setColorFilter(mContext.getColor(R.color.grey));
            }
        }

        //viewHolder.checkBox.setClickable(false);
        viewHolder.optionView.setOnClickListener(v -> {
            if(null != itemClickListener)
            {
                itemClickListener.onClick(v, position);
            }
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

    public void setSelectedPosition(int position){
        selectedId = Integer.parseInt(Objects.requireNonNull(dataList.get(position).get("id")));
    }
    public void setSelectedId(int id){
        selectedId = id;
    }

    public void setFocusPosition(int position){
        focusPosition = position;
        notifyDataSetChanged();
    }

    private ItemClickListener itemClickListener;
    public void setOnItemClickListener(ItemClickListener listener){
        itemClickListener=listener;
    }
    public interface ItemClickListener {
        void onClick(View v, int position);
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;
        TextView tvName;  //内容
        TextView tvHost;
        RadioButton radioView;
        ImageView optionView;

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.list_item_image);
            tvName = view.findViewById(R.id.list_item_name);
            tvHost = view.findViewById(R.id.list_item_host);
            radioView = view.findViewById(R.id.list_item_radio);
            optionView = view.findViewById(R.id.list_item_option);
        }
    }
}