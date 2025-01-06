package com.example.yesiot.ui.bluetooth;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.example.yesiot.R;
import com.google.android.material.tabs.TabLayout;

public class RegisterModel {
    private final MutableLiveData<Boolean> mLinked = new MutableLiveData<>();

    public TextView textView;
    public EditText et_name;
    public EditText et_mac;
    public EditText et_service_uuid;
    public Button btnSave;
    public Button btnCancel;
    public RadioGroup radioGroup;
    public Spinner spinner;
    public CheckBox checkboxHex;

    public RegisterModel(View root) {
        mLinked.setValue(false);
        btnSave = root.findViewById(R.id.button_save);
        btnCancel = root.findViewById(R.id.button_cancel);
        et_name = root.findViewById(R.id.input_name);
        et_mac = root.findViewById(R.id.input_mac);
        et_service_uuid = root.findViewById(R.id.input_service_uuid);
        spinner = root.findViewById(R.id.spinner);
        radioGroup = root.findViewById(R.id.input_type);
    }

    public MutableLiveData<Boolean> getLinked() {
        return mLinked;
    }
    public void setLinked(Boolean linked) {
        mLinked.setValue(linked);
    }
    public boolean getLinkedValue() {
        return mLinked.getValue();
    }
}
