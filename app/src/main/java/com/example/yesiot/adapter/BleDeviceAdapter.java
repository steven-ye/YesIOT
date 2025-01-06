package com.example.yesiot.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yesiot.R;
import com.example.yesiot.object.BlueDevice;

import java.util.List;

public class BleDeviceAdapter extends BaseAdapter {
    //String TAG = "BluetoothAdapter";
    private final Context mContext;//上下文对象
    private final List<BlueDevice> dataList;//ListView显示的数据
    private int selectedPosition = -1;
    private String selectedAddr = "";

    public BleDeviceAdapter(Context context, List<BlueDevice> list){
        mContext = context;
        dataList = list;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public BlueDevice getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_bluetooth, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);//将viewHolder存起来，以达到代码优化的效果。
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BlueDevice blueDevice = dataList.get(position);
        String name = blueDevice.getName();
        if(TextUtils.isEmpty(name)) name = "N/A";
        //设置Name
        viewHolder.tvName.setText(name);
        //设置Address
        String address = blueDevice.getMac();
        viewHolder.tvAddr.setText(address);
        //设置Type
        String typeText = "N/A";
        switch(blueDevice.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                typeText = "经典";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                typeText = "低功";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                typeText = "双模";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                typeText = "未知";
                break;
        }
        viewHolder.tvType.setText(typeText);
        //设置Rssi
        int rssi = blueDevice.getRssi();
        String rssiText = rssi < 0 ? rssi + " dBm" : "";
        viewHolder.tvRssi.setText(rssiText);

        //设置颜色
        int color;
        if(rssi < 0){
            color = mContext.getColor(R.color.colorPrimary);
        }else{
            color = mContext.getColor(R.color.grey);
        }
        viewHolder.imageView.setColorFilter(color);

        viewHolder.imageView.setImageResource(R.drawable.ic_baseline_bluetooth_24);

        viewHolder.ibOption.setOnClickListener(view -> {
            if(null != mActionLister) mActionLister.onClick(parent, view, position);
        });

        return convertView;
    }

    public int getPosition(String address) {
        for(int i=0; i < dataList.size(); i++) {
            if(dataList.get(i).getMac().equals(address)) return i;
        }
        return -1;
    }

    public void addDevice(BlueDevice blueDevice) {
        dataList.add(blueDevice);
        notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    public void addDevice(BluetoothDevice device, int rssi) {
        BlueDevice blueDevice = new BlueDevice(device);
        blueDevice.setRssi(rssi);
        dataList.add(blueDevice);
        notifyDataSetChanged();
    }
    @SuppressLint("MissingPermission")
    public void updateDevice(BluetoothDevice device, int rssi) {
        int position = getPosition(device.getAddress());
        if(position == -1) return;
        BlueDevice blueDevice = dataList.get(position);
        blueDevice.setName(device.getName());
        blueDevice.setType(device.getType());
        blueDevice.setRssi(rssi);
        blueDevice.setBonded(device.getBondState() == BluetoothDevice.BOND_BONDED);
        notifyDataSetChanged();
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
        selectedPosition = position;
        selectedAddr = dataList.get(position).getMac();
    }

    ItemActionListener mActionLister;
    public void setItemActionLister(ItemActionListener listener){
        mActionLister = listener;
    }
    public interface ItemActionListener {
        void onClick(ViewGroup parent, View view, int position);
    }

    /**
     * ViewHolder类
     */
    private static final class ViewHolder {
        ImageView imageView;
        TextView tvName;  //内容
        TextView tvAddr;
        TextView tvType;
        TextView tvRssi;
        ImageButton ibOption;

        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            imageView = view.findViewById(R.id.listview_item_image);
            tvName = view.findViewById(R.id.listview_item_name);
            tvAddr = view.findViewById(R.id.listview_item_addr);
            tvType = view.findViewById(R.id.listview_item_type);
            tvRssi = view.findViewById(R.id.listview_item_rssi);
            ibOption = view.findViewById(R.id.listview_action_option);
        }
    }
}