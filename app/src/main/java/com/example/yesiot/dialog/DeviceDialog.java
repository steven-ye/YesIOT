package com.example.yesiot.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.yesiot.R;
import com.example.yesiot.util.Utils;

public class DeviceDialog extends DialogFragment implements View.OnClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_device, container, false);
        root.findViewById(R.id.add_device).setOnClickListener(this);
        root.findViewById(R.id.scan_device).setOnClickListener(this);
        root.findViewById(R.id.dialog_close).setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //params.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if(v instanceof TextView){
            TextView tv = (TextView) v;
            Utils.showToast(tv.getText().toString());
            if(clickListener != null){
                clickListener.onClick(v);
            }
        }
        dismiss();
    }

    private OnClickListener clickListener;
    public void setOnClickListener(OnClickListener listener){
        clickListener = listener;
    }

    public interface OnClickListener{
        void onClick(View v);
    }
}