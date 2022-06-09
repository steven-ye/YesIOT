package com.example.yesiot.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.yesiot.R;

public class ConfirmDialog extends DialogFragment implements View.OnClickListener {
    ViewHolder viewHolder;
    String message;

    public static ConfirmDialog show(FragmentManager manager, String message, OnConfirmListener okayListener){
        ConfirmDialog dialog = new ConfirmDialog(message);
        dialog.setCancelable(false);
        dialog.setOnConfirm(okayListener);
        dialog.show(manager, "ConfirmDialog");
        return dialog;
    }

    public ConfirmDialog(String message){
        this.message = message;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_confirm, container, false);
        viewHolder = new ViewHolder(root);
        viewHolder.iv_close.setOnClickListener(this);
        viewHolder.btn_okay.setOnClickListener(this);
        viewHolder.btn_cancel.setOnClickListener(this);

        if(message!=null) setMessage(message);
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
        if(okayListener != null && v.getId()==R.id.btn_okay){
            okayListener.onClick(v);
        }
        dismiss();
    }

    public void setTitle(String title){
        viewHolder.tv_title.setText(title);
    }
    public void setMessage(String message){
        viewHolder.tv_message.setText(message);
    }
    public void setView(View view){
        ViewGroup.LayoutParams layoutParams = viewHolder.viewContent.getLayoutParams();
        //viewHolder.viewContent.removeAllViews();
        viewHolder.tv_message.setVisibility(View.GONE);
        viewHolder.viewContent.addView(view,layoutParams);
    }

    private OnConfirmListener okayListener;
    public void setOnConfirm(OnConfirmListener listener){
        okayListener = listener;
    }

    public interface OnConfirmListener{
        void onClick(View v);
    }

    static class ViewHolder{
        TextView tv_title;
        LinearLayout viewContent;
        TextView tv_message;
        Button btn_okay;
        Button btn_cancel;
        ImageView iv_close;

        public ViewHolder(View view){
            tv_title = view.findViewById(R.id.dialog_title);
            viewContent = view.findViewById(R.id.dialog_content);
            tv_message = view.findViewById(R.id.dialog_message);
            btn_okay = view.findViewById(R.id.btn_okay);
            btn_cancel = view.findViewById(R.id.btn_cancel);
            iv_close = view.findViewById(R.id.dialog_close);
        }
    }
}