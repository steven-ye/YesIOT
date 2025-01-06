package com.example.yesiot;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ListViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class ApListDialog extends DialogFragment {
    String TAG = getClass().getSimpleName();
    List<ScanResult> mList = new ArrayList<>();
    FragmentManager fragmentManager;

    public ApListDialog(FragmentManager manager){
        fragmentManager = manager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.dialog_listview, container);
        ScanResultAdapter adapter = new ScanResultAdapter(getContext(), mList);
        adapter.setItemCLickListener((view, position)->{
            if(null != clickListener)
                clickListener.onClick(view, position);
        });
        ListView listView = root.findViewById(R.id.listview_dialog);
        TextView emptyView = root.findViewById(R.id.list_empty);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);
        return root;
    }

    public void show(List<ScanResult> list){
        mList.clear();
        mList.addAll(list);
        show(fragmentManager, TAG);
    }

    OnClickListener clickListener;
    public void setOnClickListener(OnClickListener listener){
        clickListener = listener;
    }

    public interface OnClickListener{
        void onClick(View view, int position);
    }
}
