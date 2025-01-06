package com.example.yesiot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yesiot.R;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    Context mContext;
    List<String> mList;
    public SpinnerAdapter(@NonNull Context context, @NonNull List<String> objects) {
        mContext = context;
        mList = objects;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_gatt_spinner, parent, false);
        }
        String uuid = getItem(position);
        TextView textView = (TextView) convertView;
        textView.setText(uuid);
        return convertView;
    }
}
