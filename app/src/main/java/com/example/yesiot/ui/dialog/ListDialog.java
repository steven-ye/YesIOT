package com.example.yesiot.ui.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.yesiot.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListDialog extends DialogFragment {
    private final List<Map<String, Object>> mList;
    public ListDialog(List<Map<String, Object>> mapList){
        mList = mapList;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_listview, container, false);
        ListView listView= root.findViewById(R.id.listview_dialog);
        SimpleAdapter adapter = new SimpleAdapter(getContext(),mList,R.layout.dialog_listview_item,
                new String[]{"icon","title"},new int[]{R.id.listview_option_icon, R.id.listview_option_title}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Map<String,Object> map = mList.get(position);
                int color = Integer.parseInt(map.get("color").toString());
                ImageView imageView = view.findViewById(R.id.listview_option_icon);
                TextView textView = view.findViewById(R.id.listview_option_title);
                imageView.setColorFilter(color);
                textView.setTextColor(color);
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(null != clickListener){
                clickListener.onClick(view, position);
            }
            dismiss();
        });

        return root;
    }

    @Override
    public void onStart() {
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //params.gravity = Gravity.BOTTOM;
        window.setAttributes((WindowManager.LayoutParams) params);
        super.onStart();
    }

    private OnClickListener clickListener;
    public void setOnClickListener(OnClickListener listener){
        clickListener = listener;
    }

    public interface OnClickListener{
        void onClick(View v, int position);
    }
}