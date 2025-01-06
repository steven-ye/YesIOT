package com.example.yesiot.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yesiot.R;
import com.example.yesiot.object.BlueButton;
import com.example.yesiot.util.Utils;
import com.google.gson.Gson;

import java.util.List;

public class ButtonAdapter extends BaseAdapter {
    String TAG = "GridAdapter";
    private final Context mContext;//上下文对象
    private final List<BlueButton> dataList;//ListView显示的数据
    private boolean editable = false;

    public ButtonAdapter(Context context, List<BlueButton> list){
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_item_button, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BlueButton button = dataList.get(position);
        Log.i(TAG, new Gson().toJson(button));
        //设置图片
        Bitmap Bitmap = Utils.getAssetsBitmap(mContext, button.image);
        if(Bitmap != null){
            viewHolder.imageView.setImageBitmap(Bitmap);
            viewHolder.imageView.setBackground(null);
        }
        //设置名称
        if(TextUtils.isEmpty(button.title))
            viewHolder.tvName.setText(button.name);
        else
            viewHolder.tvName.setText(button.title);
        //设置说明
        viewHolder.tvDesc.setText(button.caption);
        //设置状态
        int color = Color.GRAY;
        if(button.cmd_on.equals(button.value)){
            color = Color.RED;
        }else if(button.cmd_off.equals(button.value)){
            color = Color.BLUE;
        }
        viewHolder.imageView.setColorFilter(color);
        viewHolder.tvName.setTextColor(color);
        //Log.d("ButtonAdapter", button.name + ", value=" + button.value +", color=" + color);
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

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;//图片
        TextView tvName;  //内容
        TextView tvDesc; //在线或离线

        /**
         * 构造器
         *
         * @param view 视图组件（GridView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.gridview_item_image);
            tvName = view.findViewById(R.id.gridview_item_name);
            tvDesc = view.findViewById(R.id.gridview_item_desc);
        }
    }
}