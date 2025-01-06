package com.example.yesiot.ui.broker;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.yesiot.R;

public class BrokerViewModel {
    private String[] protocols;
    public EditText et_name;
    public EditText et_host;
    public EditText et_port;
    public EditText et_path;
    public EditText et_clientid;
    public EditText et_alive;
    public EditText et_timeout;
    public EditText et_username;
    public EditText et_password;
    public EditText et_topic;
    public EditText et_message;
    public CheckBox cb_auto;
    public CheckBox cb_session;
    public CheckedTextView ctv_export;
    public Spinner spinner;
    public View row_ctv_export;

    public BrokerViewModel(View root){
        et_name = root.findViewById(R.id.link_name);
        et_host = root.findViewById(R.id.link_host);
        et_clientid = root.findViewById(R.id.clientid);
        et_port = root.findViewById(R.id.link_port);
        et_path = root.findViewById(R.id.link_path);
        et_alive = root.findViewById(R.id.link_alive);
        et_timeout = root.findViewById(R.id.link_timeout);
        et_username = root.findViewById(R.id.link_username);
        et_password = root.findViewById(R.id.link_password);
        et_topic = root.findViewById(R.id.link_topic);
        et_message = root.findViewById(R.id.link_message);
        cb_auto = root.findViewById(R.id.checkbox_auto);
        cb_session = root.findViewById(R.id.checkbox_session);
        ctv_export = root.findViewById(R.id.ctv_export);
        spinner = root.findViewById(R.id.link_protocol);
        row_ctv_export = root.findViewById(R.id.row_ctv_export);
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