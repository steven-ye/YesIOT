package com.example.yesiot;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.yesiot.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyPopupWindow extends PopupWindow {
    protected ListAdaptor mAdaptor;
    protected final List<Map<String, String>> mDataList = new ArrayList<>();

    public MyPopupWindow(Context context) {
        super(context);
        final View contentView = LayoutInflater.from(context).inflate(R.layout.listview_container, null, false);

        ListView mListView = contentView.findViewById(R.id.popup_listview);
        View emptyView = contentView.findViewById(R.id.list_empty);
        mListView.setEmptyView(emptyView);
        mAdaptor = new ListAdaptor(context, mDataList);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> map = mDataList.get(position);
            Utils.showToast(context, "clicked " + map.get("title"));
            Log.d("MyPopupWindow", "clicked " + map.get("title"));
            dismiss();
        });

        setContentView(contentView);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0xcc000000));    //要为popWindow设置一个背景才有效
    }

    public void setDataList(List<Map<String, String>> dataList)
    {
        mDataList.clear();
        mDataList.addAll(dataList);
        mAdaptor.notifyDataSetChanged();
    }

    public static class ListAdaptor extends BaseAdapter
    {
        private final Context mContext;//上下文对象
        private final List<Map<String, String>> mList;//ListView显示的数据

        public ListAdaptor(Context context, List<Map<String, String>> data){
            mContext = context;
            mList = data;
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_uuid, parent, false);
                viewHolder = new ListAdaptor.ViewHolder(convertView);
                convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
            }else{
                viewHolder = (ListAdaptor.ViewHolder) convertView.getTag();
            }

            Map<String, String> map = mList.get(position);
            viewHolder.textView.setText(map.get("title"));

            return convertView;
        }

        private static final class ViewHolder {
            RadioButton radioButton;//图片
            TextView textView;  //内容

            ViewHolder(View view) {
                radioButton = view.findViewById(R.id.list_option_radio);
                textView = view.findViewById(R.id.list_option_title);
            }
        }
    }
}
