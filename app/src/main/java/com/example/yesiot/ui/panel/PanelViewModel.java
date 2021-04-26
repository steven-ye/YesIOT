package com.example.yesiot.ui.panel;

import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.yesiot.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

class PanelViewModel {
    final RecyclerView recyclerView;
    final TextInputLayout layout_name;
    final TextInputLayout layout_title;
    final TextInputLayout layout_width;
    final TextInputLayout layout_height;
    final TextInputEditText et_name;
    final TextInputEditText et_title;
    final TextInputEditText et_unit;
    final TextInputEditText et_topic;
    final TextInputEditText et_cmd_on;
    final TextInputEditText et_cmd_off;
    final TextInputEditText et_sub;
    final TextInputEditText et_width;
    final TextInputEditText et_height;
    final CheckedTextView expert_option;
    final View row_expert_option;
    final Button btn_okay;

    public PanelViewModel(View root){
        recyclerView = root.findViewById(R.id.rv_panel_type);
        layout_name = root.findViewById(R.id.layout_panel_name);
        layout_title = root.findViewById(R.id.layout_panel_title);
        layout_width = root.findViewById(R.id.layout_panel_width);
        layout_height = root.findViewById(R.id.layout_panel_height);
        et_name = root.findViewById(R.id.panel_name);
        et_title = root.findViewById(R.id.panel_title);
        et_unit = root.findViewById(R.id.panel_unit);
        et_topic = root.findViewById(R.id.panel_topic);
        et_cmd_on = root.findViewById(R.id.panel_cmd_on);
        et_cmd_off = root.findViewById(R.id.panel_cmd_off);
        et_sub = root.findViewById(R.id.panel_sub);
        et_width = root.findViewById(R.id.panel_width);
        et_height = root.findViewById(R.id.panel_height);
        expert_option = root.findViewById(R.id.expert_option);
        row_expert_option = root.findViewById(R.id.row_expert_option);
        btn_okay = root.findViewById(R.id.button_okay);
    }
}