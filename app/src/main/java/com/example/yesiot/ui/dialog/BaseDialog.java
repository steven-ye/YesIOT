package com.example.yesiot.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.yesiot.R;
import com.example.yesiot.util.Utils;

public class BaseDialog extends DialogFragment implements View.OnClickListener {

    public static BaseDialog newInstance(String title, int unique,
                                         String strName, String strHigh) {
        BaseDialog tDialog = new BaseDialog();
        Bundle args = new Bundle();
        args.putString("SelectTemplateTitle", title);
        args.putInt("MultipleTemplate", unique);
        args.putString("TemplateName", strName);
        args.putString("TemplateHigh", strHigh);
        tDialog.setArguments(args);
        return tDialog;

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_new_device, container, false);
        root.findViewById(R.id.add_device).setOnClickListener(this);
        root.findViewById(R.id.scan_device).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        Utils.showToast(btn.getText().toString());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}