package com.example.yesiot.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.yesiot.R;
import com.example.yesiot.ui.dialog.ConfirmDialog;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_about, container, false);
        TextView tv_version = root.findViewById(R.id.app_version);
        String app_version = "当前版本：v" + getString(R.string.app_version);
        tv_version.setText(app_version);
        return root;
    }
}