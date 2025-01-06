package com.example.yesiot.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.yesiot.R;

public class BrokerDialog extends DialogFragment implements View.OnClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_broker, container, false);

        root.findViewById(R.id.action_select).setOnClickListener(this);
        root.findViewById(R.id.action_edit).setOnClickListener(this);
        root.findViewById(R.id.action_delete).setOnClickListener(this);
        root.findViewById(R.id.action_cancel).setOnClickListener(this);
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
        if(clickListener != null) clickListener.onClick(v);
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