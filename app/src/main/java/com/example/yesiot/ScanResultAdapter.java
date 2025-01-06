package com.example.yesiot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ScanResultAdapter extends BaseAdapter {
    String TAG = "DialogAdapter";
    private final Context mContext;//上下文对象
    private final List<ScanResult> mList;//ListView显示的数据

    public ScanResultAdapter(Context context, List<ScanResult> data){
        mContext = context;
        mList = data;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_scan_blue, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ScanResult result = mList.get(position);
        viewHolder.ssidView.setText(result.SSID);
        viewHolder.bssidView.setText(result.BSSID);
        convertView.setOnClickListener(view -> {
            if(null != itemCLickListener)
                itemCLickListener.onCLick(view, position);
        });

        return convertView;
    }

    OnItemCLickListener itemCLickListener;

    public void setItemCLickListener(OnItemCLickListener itemCLickListener) {
        this.itemCLickListener = itemCLickListener;
    }

    public interface OnItemCLickListener{
        void onCLick(View v, int position);
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        TextView ssidView;//图片
        TextView bssidView;  //内容

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            ssidView = view.findViewById(R.id.listview_item_ssid);
            bssidView = view.findViewById(R.id.listview_item_bssid);
        }
    }
}