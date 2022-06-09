package com.example.yesiot.ui.socket;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesiot.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SocketViewModel {

    final TextInputLayout layoutIP;
    final TextInputLayout layoutPort;
    final TextInputEditText et_ip;
    final TextInputEditText et_port;
    final Button btn_connect;
    final Button btn_send;
    final Button btn_clear;
    final EditText et_message;
    final TextView tv_logger;
    final ScrollView scrollView;
    final CheckBox cb_url;
    final CheckBox cb_hex;
    final CheckBox cb_ln;

    public SocketViewModel(View root) {
        layoutIP = root.findViewById(R.id.layout_tcp_ip);
        layoutPort = root.findViewById(R.id.layout_tcp_port);
        et_ip = root.findViewById(R.id.tcp_ip);
        et_port = root.findViewById(R.id.tcp_port);

        cb_url = root.findViewById(R.id.tcp_option_url);
        cb_hex = root.findViewById(R.id.tcp_option_hex);
        cb_ln = root.findViewById(R.id.tcp_option_ln);

        scrollView = root.findViewById(R.id.tcp_scrollview);
        tv_logger = root.findViewById(R.id.tcp_logger);
        et_message = root.findViewById(R.id.tcp_message);
        btn_connect = root.findViewById(R.id.tcp_button_connect);
        btn_send = root.findViewById(R.id.tcp_button_send);
        btn_clear = root.findViewById(R.id.tcp_button_clear);
    }
}