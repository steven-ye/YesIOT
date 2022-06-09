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
import com.example.yesiot.object.Constants;
import com.example.yesiot.service.TcpClient;
import com.example.yesiot.util.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SocketFragment extends Fragment implements TcpClient.TcpCallback {

    private SocketViewModel viewModel;
    TcpClient client;
    private String mIP="";
    boolean isConnected = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_socket, container, false);
        viewModel = new SocketViewModel(root);
        client = TcpClient.getInstance();
        client.setCallback(this);
        initViews();
        return root;
    }

    private void initViews() {
        viewModel.et_ip.setText("192.168.1.10");
        viewModel.et_port.setText(Constants.TCP_SERVER_PORT+"");

        //设置默认滚动到底部
        viewModel.scrollView.post(() -> viewModel.scrollView.fullScroll(ScrollView.FOCUS_DOWN));

        viewModel.et_ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.layoutIP.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        viewModel.et_port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.layoutPort.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        viewModel.btn_connect.setOnClickListener(v -> {
            if(client.isConnect()){
                client.disconnect();
                isConnected = false;
            }else{
                String ip = viewModel.et_ip.getText().toString();
                String port = viewModel.et_port.getText().toString();

                if(TextUtils.isEmpty(ip)){
                    viewModel.layoutIP.setError("IP地址为空");
                    return;
                }
                if(TextUtils.isEmpty(port)){
                    viewModel.layoutPort.setError("端口号为空");
                    return;
                }
                client.connect(ip,Integer.parseInt(port));
                isConnected = true;
            }
            updateStatus();
        });

        //socket send
        viewModel.btn_send.setOnClickListener(v -> {
            String msg = viewModel.et_message.getText().toString();
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

        viewModel.btn_clear.setOnClickListener(V-> viewModel.tv_logger.setText(""));

        viewModel.cb_url.setOnClickListener(v->{
            if(viewModel.cb_hex.isChecked()){
                viewModel.cb_hex.setChecked(false);
            }
        });
        viewModel.cb_hex.setOnClickListener(v->{
            if(viewModel.cb_url.isChecked()){
                viewModel.cb_url.setChecked(false);
            }
        });
    }

    private void logAppend(String message,int type){
        switch(type){
            case 0:
                message = "系统消息: " + message;
                break;
            case 1:
                message = "发送 >>: " + message;
                break;
            default:
                message = "收到 <<: " + message;
                break;
        }
        viewModel.tv_logger.append(message+"\n");
        //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        //设置默认滚动到底部
        viewModel.scrollView.post(() -> viewModel.scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateStatus(){
        if(isConnected && client.isConnect()){
            viewModel.btn_connect.setText(getString(R.string.tcp_button_disconnect));
            viewModel.btn_connect.setTextColor(Color.RED);
            viewModel.btn_connect.setBackgroundResource(R.drawable.selector_blue);
        }else{
            viewModel.btn_connect.setText(getString(R.string.tcp_button_connect));
            viewModel.btn_connect.setTextColor(Color.WHITE);
            viewModel.btn_connect.setBackgroundResource(R.drawable.selector_grey);
        }
    }

    private void sendMsg(String msg){
        //logAppend(msg,1);
        String message = msg;
        if(viewModel.cb_url.isChecked()){
            String url = msg.indexOf("/")==0?msg: "/"+msg;
            message = "GET "+url+" HTTP/1.1\r\n";
            message += "Host: " + mIP + "\r\n";
            message += "\r\n";
            client.sendStrCmd(message, 1001);
            msg = "[HTTP] "+url;
        }else if(viewModel.cb_hex.isChecked()){
            byte[] buffer = Utils.hexStringToBytes(msg);
            client.sendByteCmd(buffer, 1001);
        }else{
            if(viewModel.cb_ln.isChecked()) message = msg + "\r\n";
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