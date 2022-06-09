package com.example.yesiot.ui.panel;

import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.yesiot.R;

class PanelViewModel {
    final RecyclerView recyclerView;
    final EditText et_name;
    final EditText et_title;
    final EditText et_unit;
    final EditText et_title_size;
    final EditText et_unit_size;
    final EditText et_payload;
    final EditText et_cmd_on;
    final EditText et_cmd_off;
    final EditText et_width;
    final EditText et_height;
    final RadioGroup radioGroup;
    final View row_payload;
    final View row_on_off;
    final View row_panel_icon;
    final TextView tv_error;

    public PanelViewModel(View root){
        recyclerView = root.findViewById(R.id.rv_panel_type);
        et_name = root.findViewById(R.id.panel_name);
        et_title = root.findViewById(R.id.panel_title);
        et_unit = root.findViewById(R.id.panel_unit);
        et_title_size = root.findViewById(R.id.panel_title_size);
        et_unit_size = root.findViewById(R.id.panel_unit_size);
        et_payload = root.findViewById(R.id.panel_payload);
        et_cmd_on = root.findViewById(R.id.panel_cmd_on);
        et_cmd_off = root.findViewById(R.id.panel_cmd_off);
        et_width = root.findViewById(R.id.panel_width);
        et_height = root.findViewById(R.id.panel_height);
        radioGroup = root.findViewById(R.id.panel_radioGroup);
        row_payload = root.findViewById(R.id.row_payload);
        row_on_off = root.findViewById(R.id.row_cmd_on_off);
        row_panel_icon = root.findViewById(R.id.row_panel_icon);
        tv_error = root.findViewById(R.id.textinput_error);
    }
}