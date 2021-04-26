package com.example.yesiot.ui.broker;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesiot.R;

public class BrokerViewModel {
    private String[] protocols;
    public EditText et_name;
    public EditText et_ip;
    public EditText et_clientid;
    public EditText et_port;
    public EditText et_alive;
    public EditText et_timeout;
    public EditText et_username;
    public EditText et_password;
    public EditText et_topic;
    public EditText et_message;
    public CheckBox cb_auto;
    public CheckBox cb_notice;
    public CheckedTextView ctv_lastwill;
    public Button btn_okay;
    public Button btn_cancel;
    public Spinner spinner;
    public View row_lastwill;

    public BrokerViewModel(View root){
        et_name = root.findViewById(R.id.link_name);
        et_ip = root.findViewById(R.id.link_ip);
        et_clientid = root.findViewById(R.id.clientid);
        et_port = root.findViewById(R.id.port);
        et_alive = root.findViewById(R.id.link_alive);
        et_timeout = root.findViewById(R.id.link_timeout);
        et_username = root.findViewById(R.id.link_username);
        et_password = root.findViewById(R.id.link_password);
        et_topic = root.findViewById(R.id.link_topic);
        et_message = root.findViewById(R.id.link_message);
        cb_auto = root.findViewById(R.id.checkbox_auto);
        cb_notice = root.findViewById(R.id.checkbox_notice);
        ctv_lastwill = root.findViewById(R.id.lastwill);
        btn_okay = root.findViewById(R.id.button_okay);
        spinner = root.findViewById(R.id.protocol);
        row_lastwill = root.findViewById(R.id.row_lastwill);
    }

    public String getProtocol(int position){
        return protocols[position];
    }
    public int getProtocolPosition(String protocol){
        for(int i=0;i<protocols.length;i++){
            if(protocols[i].equals(protocol)){
                return i;
            }
        }
        return 0;
    }
    public void setProtocols(String[] protocols){
        this.protocols = protocols;
    }
}