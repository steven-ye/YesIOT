package com.example.yesiot.ui.device;

import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.yesiot.R;
import com.example.yesiot.object.Device;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

class DeviceViewModel {
    final TextInputEditText et_name;
    final TextInputEditText et_code;
    final TextInputEditText et_theme;
    final TextInputEditText et_sub;
    final TextInputEditText et_topic;
    final TextInputEditText et_payload;
    final TextInputLayout layout_name;
    final TextInputLayout layout_code;
    final TextInputLayout layout_theme;
    final LinearLayout row_device_option;
    final CheckedTextView device_option;
    final ImageView device_image;
    final Button btn_okay;

    Device device;

    public DeviceViewModel(View root){
        device_image = root.findViewById(R.id.device_image);
        et_name = root.findViewById(R.id.device_name);
        et_theme = root.findViewById(R.id.device_theme);
        et_code = root.findViewById(R.id.device_code);
        et_sub = root.findViewById(R.id.device_sub);
        et_topic = root.findViewById(R.id.device_topic);
        et_payload = root.findViewById(R.id.device_payload);
        layout_name = root.findViewById(R.id.layout_device_name);
        layout_code = root.findViewById(R.id.layout_device_code);
        layout_theme = root.findViewById(R.id.layout_device_theme);
        device_option = root.findViewById(R.id.device_option);
        row_device_option = root.findViewById(R.id.row_device_option);
        btn_okay = root.findViewById(R.id.button_okay);

        device = new Device();
    }

    public Device getDevice(){
        return device;
    }
    public void setDevice(Device device){
        this.device = device;
        et_name.setText(device.getName());
        et_code.setText(device.getCode());
        et_theme.setText(device.getTheme());
        et_sub.setText(device.getSub());
        et_topic.setText(device.getTopic());
        et_payload.setText(device.getPayload());
    }

    public void setImage(String path){
        device.setImage(path);
    }

    public void invalidateAll(){
        device.setName(et_name.getText().toString());
        device.setCode(et_code.getText().toString());
        device.setTheme(et_theme.getText().toString());
        device.setSub(et_sub.getText().toString());
        device.setTopic(et_topic.getText().toString());
        device.setPayload(et_payload.getText().toString());
    }
}