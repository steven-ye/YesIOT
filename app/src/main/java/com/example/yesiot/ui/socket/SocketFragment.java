package com.example.yesiot.ui.socket;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.yesiot.R;
import com.example.yesiot.service.TcpClient;
import com.example.yesiot.util.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SocketFragment extends Fragment implements TcpClient.TcpCallback {

    private SocketViewModel viewModel;
    TcpClient client;
    private String mIP="";
    private int mPort=80;
    boolean isConnected = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel =
                new ViewModelProvider(this).get(SocketViewModel.class);
        View root = inflater.inflate(R.layout.fragment_socket, container, false);

        client = TcpClient.getInstance();
        client.setCallback(this);
        findViews(root);
        return root;
    }

    Button btn_connect;
    Button btn_send;
    EditText et_message;
    TextView tv_logger;
    ScrollView scrollView;
    CheckBox cb_url;
    CheckBox cb_hex;
    CheckBox cb_ln;
    private void findViews(View root) {
        final TextInputLayout layoutIP = root.findViewById(R.id.layout_tcp_ip);
        final TextInputLayout layoutPort = root.findViewById(R.id.layout_tcp_port);
        final TextInputEditText et_ip = root.findViewById(R.id.tcp_ip);
        final TextInputEditText et_port = root.findViewById(R.id.tcp_port);
        Button btn_clear = root.findViewById(R.id.tcp_button_clear);
        cb_url = root.findViewById(R.id.tcp_option_url);
        cb_hex = root.findViewById(R.id.tcp_option_hex);
        cb_ln = root.findViewById(R.id.tcp_option_ln);

        scrollView = root.findViewById(R.id.tcp_scrollview);
        tv_logger = root.findViewById(R.id.tcp_logger);
        et_message = root.findViewById(R.id.tcp_message);
        btn_connect = root.findViewById(R.id.tcp_button_connect);
        btn_send = root.findViewById(R.id.tcp_button_send);

        //设置默认滚动到底部
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

        et_ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutIP.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        et_port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutPort.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btn_connect.setOnClickListener(v -> {
            if(client.isConnect()){
                client.disconnect();
                isConnected = false;
            }else{
                String ip = et_ip.getText().toString();
                String port = et_port.getText().toString();

                if(TextUtils.isEmpty(ip)){
                    layoutIP.setError("IP地址为空");
                    return;
                }
                if(TextUtils.isEmpty(port)){
                    layoutPort.setError("端口号为空");
                    return;
                }
                client.connect(ip,Integer.parseInt(port));
                isConnected = true;
            }
            updateStatus();
        });

        //socket send
        btn_send.setOnClickListener(v -> {
            String msg = et_message.getText().toString();
            if (TextUtils.isEmpty(msg)){
                Utils.showToast("请输入发送内容");
                return;
            }
            if (client.isConnect()) {
                sendMsg(msg);
            }else{
                Utils.showToast("尚未连接，请连接Socket");
            }
        });

        btn_clear.setOnClickListener(V-> tv_logger.setText(""));

        cb_url.setOnClickListener(v->{
            if(cb_hex.isChecked()){
                cb_hex.setChecked(false);
            }
        });
        cb_hex.setOnClickListener(v->{
            if(cb_url.isChecked()){
                cb_url.setChecked(false);
            }
        });
    }

    private void logAppend(String message,int type){
        switch(type){
            case 0:
                message = "系统消息: " + message;
                break;
            case 1:
                message = "发送消息: " + message;
                break;
            default:
                message = "收到消息: " + message;
                break;
        }
        tv_logger.append(message+"\n");
        //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        //设置默认滚动到底部
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateStatus(){
        if(isConnected && client.isConnect()){
            btn_connect.setText(getString(R.string.tcp_button_disconnect));
            btn_connect.setTextColor(Color.RED);
            btn_connect.setBackgroundResource(R.drawable.selector_blue);
        }else{
            btn_connect.setText(getString(R.string.tcp_button_connect));
            btn_connect.setTextColor(Color.WHITE);
            btn_connect.setBackgroundResource(R.drawable.selector_grey);
        }
    }

    private void sendMsg(String msg){
        //logAppend(msg,1);
        String message = msg;
        if(cb_url.isChecked()){
            String url = msg.indexOf("/")==0?msg: "/"+msg;
            message = "GET "+url+" HTTP/1.1\r\n";
            message += "Host: " + mIP + "\r\n";
            message += "\r\n";
            client.sendStrCmd(message, 1001);
            msg = "[HTTP] "+url;
        }else if(cb_hex.isChecked()){
            byte[] buffer = Utils.hexStringToBytes(msg);
            client.sendByteCmd(buffer, 1001);
        }else{
            if(cb_ln.isChecked()) message = msg + "\r\n";
            client.sendStrCmd(message,1001);
        }
        logAppend(msg,1);
    }

    @Override
    public void onConnectSuccess(String ip, int port) {
        Utils.showToast("成功连接到 " +ip+":"+port);
        logAppend(getString(R.string.tcp_connect_done)+"<"+ip+":"+port+">",0);
        //logAppend("Host: "+ip+":"+port, 0);
        mIP = ip;
        mPort = port;
        isConnected = true;
        updateStatus();
    }

    @Override
    public void onConnectFail(String ip, int port) {
        logAppend(getString(R.string.tcp_connect_fail),0);
        isConnected = false;
        updateStatus();
    }

    @Override
    public void onDataReceived(String message, int requestCode) {
        logAppend(message,2);
    }
}